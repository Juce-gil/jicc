package cn.kmbeast.service;

import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.ProductQueryDto;
import cn.kmbeast.pojo.dto.update.OrdersDTO;
import cn.kmbeast.pojo.entity.Product;
import cn.kmbeast.pojo.vo.ChartVO;
import cn.kmbeast.pojo.vo.OrderActionResultVO;
import cn.kmbeast.pojo.vo.OrdersDeliverDto;
import cn.kmbeast.pojo.vo.ProductVO;

import java.util.List;

/**
 * Product service.
 */
public interface ProductService {

    Result<String> save(Product product);

    Result<String> update(Product product);

    Result<String> batchDelete(List<Integer> ids);

    Result<List<ProductVO>> query(ProductQueryDto productQueryDto);

    Result<OrderActionResultVO> buyProduct(OrdersDTO ordersDTO);

    Result<OrderActionResultVO> placeAnOrder(Integer ordersId);

    Result<OrderActionResultVO> refund(Integer ordersId);

    Result<List<ChartVO>> queryProductInfo(ProductQueryDto productQueryDto);

    Result<OrderActionResultVO> getGoods(Integer ordersId);

    Result<List<ProductVO>> queryProductList(Integer id);

    /**
     * Legacy alias: seller confirms trade completion.
     */
    Result<OrderActionResultVO> deliverGoods(OrdersDeliverDto ordersDeliverDto);

    /**
     * Seller confirms the offline trade has been completed.
     */
    Result<OrderActionResultVO> confirmTradeBySeller(Integer ordersId);
}
