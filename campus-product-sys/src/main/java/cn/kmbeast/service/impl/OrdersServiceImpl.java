package cn.kmbeast.service.impl;

import cn.kmbeast.aop.Protector;
import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.mapper.OrdersMapper;
import cn.kmbeast.mapper.ProductMapper;
import cn.kmbeast.pojo.api.ApiResult;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.OrdersQueryDto;
import cn.kmbeast.pojo.dto.update.OrdersDTO;
import cn.kmbeast.pojo.em.TradeStatusEnum;
import cn.kmbeast.pojo.entity.Orders;
import cn.kmbeast.pojo.entity.Product;
import cn.kmbeast.pojo.vo.OrderActionResultVO;
import cn.kmbeast.pojo.vo.OrdersVO;
import cn.kmbeast.pojo.vo.ProductVO;
import cn.kmbeast.service.OrdersService;
import cn.kmbeast.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Orders service implementation.
 */
@Service
public class OrdersServiceImpl implements OrdersService {

    private static final Integer ORDER_STATUS_PENDING_CONFIRM = TradeStatusEnum.PENDING_CONFIRM.getCode();
    private static final Integer ORDER_STATUS_RESERVED = TradeStatusEnum.RESERVED.getCode();
    private static final Integer ORDER_STATUS_PARTIAL_CONFIRMED = TradeStatusEnum.PARTIAL_CONFIRMED.getCode();
    private static final Integer ORDER_STATUS_COMPLETED = TradeStatusEnum.COMPLETED.getCode();
    private static final Integer ORDER_STATUS_CANCELLED = TradeStatusEnum.CANCELLED.getCode();

    private static final String PRODUCT_STATUS_ON_SALE = "ON_SALE";
    private static final String PRODUCT_STATUS_SOLD = "SOLD";

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private ProductService productService;

    @Override
    public Result<String> save(Orders orders) {
        if (orders == null) {
            return ApiResult.error("reservation payload cannot be empty");
        }
        OrdersDTO ordersDTO = new OrdersDTO();
        BeanUtils.copyProperties(orders, ordersDTO);
        Result<OrderActionResultVO> actionResult = productService.buyProduct(ordersDTO);
        if (!Objects.equals(actionResult.getCode(), 200)) {
            return ApiResult.error(actionResult.getMsg());
        }
        return ApiResult.success(actionResult.getMsg());
    }

    @Override
    public Result<String> update(Orders orders) {
        if (!isAdmin(LocalThreadHolder.getRoleId())) {
            return ApiResult.error("only admin can update order records");
        }
        ordersMapper.update(orders);
        return ApiResult.success("order updated successfully");
    }

    @Override
    public Result<String> batchDelete(List<Integer> ids) {
        if (!isAdmin(LocalThreadHolder.getRoleId())) {
            return ApiResult.error("only admin can batch delete order records");
        }
        if (ids == null || ids.isEmpty()) {
            return ApiResult.success("orders deleted successfully");
        }
        ordersMapper.batchDelete(ids);
        return ApiResult.success("orders deleted successfully");
    }

    @Override
    public Result<List<OrdersVO>> query(OrdersQueryDto ordersQueryDto) {
        int totalCount = ordersMapper.queryCount(ordersQueryDto);
        List<OrdersVO> ordersVOList = ordersMapper.query(ordersQueryDto);
        return ApiResult.success(ordersVOList, totalCount);
    }

    @Override
    public Result<List<OrdersVO>> queryOrdersList(OrdersQueryDto ordersQueryDto) {
        if (ordersQueryDto == null) {
            ordersQueryDto = new OrdersQueryDto();
        }
        List<Integer> productIds = productMapper.queryProductIds(LocalThreadHolder.getUserId());
        if (productIds == null || productIds.isEmpty()) {
            return ApiResult.success(new ArrayList<OrdersVO>());
        }
        ordersQueryDto.setProductIds(productIds);
        List<OrdersVO> ordersVOList = ordersMapper.queryByProductIds(ordersQueryDto);
        return ApiResult.success(ordersVOList);
    }

    /**
     * Legacy close-order alias, now used as a direct reservation cancellation entry.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<OrderActionResultVO> returnMoney(Integer ordersId) {
        OrdersQueryDto queryDto = new OrdersQueryDto();
        queryDto.setId(ordersId);
        List<OrdersVO> orders = ordersMapper.query(queryDto);
        if (orders == null || orders.isEmpty()) {
            return ApiResult.error("order not found");
        }
        OrdersVO ordersVO = orders.get(0);
        if (Objects.equals(ordersVO.getTradeStatus(), ORDER_STATUS_COMPLETED)) {
            return ApiResult.error("completed orders cannot be closed");
        }
        if (Objects.equals(ordersVO.getTradeStatus(), ORDER_STATUS_CANCELLED)) {
            return ApiResult.error("order has already been closed");
        }

        Orders updateEntity = new Orders();
        updateEntity.setId(ordersId);
        updateEntity.setTradeStatus(ORDER_STATUS_CANCELLED);
        updateEntity.setCancelTime(LocalDateTime.now());
        updateEntity.setCancelReason("closed by admin");
        ordersMapper.update(updateEntity);

        restoreProductToSaleIfNeeded(ordersVO.getProductId());
        String message = "reservation order closed";
        return ApiResult.success(
                message,
                OrderActionResultVO.of(
                        ordersVO.getId(),
                        ordersVO.getCode(),
                        ordersVO.getTradeStatus(),
                        ORDER_STATUS_CANCELLED,
                        "ADMIN_CLOSE_RESERVATION",
                        message
                )
        );
    }

    private void restoreProductToSaleIfNeeded(Integer productId) {
        if (productId == null || hasActiveReservation(productId)) {
            return;
        }
        ProductVO productVO = productMapper.queryById(productId);
        if (productVO == null || PRODUCT_STATUS_SOLD.equals(productVO.getStatus())) {
            return;
        }
        Product product = new Product();
        product.setId(productVO.getId());
        product.setStatus(PRODUCT_STATUS_ON_SALE);
        product.setInventory(1);
        productMapper.update(product);
    }

    private boolean hasActiveReservation(Integer productId) {
        OrdersQueryDto queryDto = new OrdersQueryDto();
        queryDto.setProductId(productId);
        List<OrdersVO> orders = ordersMapper.query(queryDto);
        if (orders == null || orders.isEmpty()) {
            return false;
        }
        for (OrdersVO order : orders) {
            if (isActiveOrderStatus(order.getTradeStatus())) {
                return true;
            }
        }
        return false;
    }

    private boolean isActiveOrderStatus(Integer tradeStatus) {
        return Objects.equals(tradeStatus, ORDER_STATUS_PENDING_CONFIRM)
                || Objects.equals(tradeStatus, ORDER_STATUS_RESERVED)
                || Objects.equals(tradeStatus, ORDER_STATUS_PARTIAL_CONFIRMED);
    }

    private boolean isAdmin(Integer roleId) {
        return Objects.equals(roleId, Protector.ROLE_ADMIN);
    }
}
