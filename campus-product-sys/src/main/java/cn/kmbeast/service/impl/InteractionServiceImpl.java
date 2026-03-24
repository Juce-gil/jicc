package cn.kmbeast.service.impl;

import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.mapper.InteractionMapper;
import cn.kmbeast.mapper.MessageMapper;
import cn.kmbeast.mapper.ProductMapper;
import cn.kmbeast.mapper.UserMapper;
import cn.kmbeast.pojo.api.ApiResult;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.InteractionQueryDto;
import cn.kmbeast.pojo.dto.query.extend.ProductQueryDto;
import cn.kmbeast.pojo.em.InteractionEnum;
import cn.kmbeast.pojo.entity.Interaction;
import cn.kmbeast.pojo.entity.Message;
import cn.kmbeast.pojo.entity.User;
import cn.kmbeast.pojo.vo.ProductVO;
import cn.kmbeast.service.InteractionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 互动行为业务逻辑接口实现类
 */
@Service
public class InteractionServiceImpl implements InteractionService {

    @Resource
    private InteractionMapper interactionMapper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> likeProduct(Integer productId) {
        if (productId == null) {
            return ApiResult.error("商品ID不能为空");
        }
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("登录已失效，请重新登录");
        }
        InteractionQueryDto interactionQueryDto = createInteractionQueryDto(productId, InteractionEnum.LOVE.getType(), currentUserId);
        if (interactionMapper.queryCount(interactionQueryDto) > 0) {
            return ApiResult.error("请勿重复操作");
        }
        ProductVO productVO = queryProduct(productId);
        if (productVO == null) {
            return ApiResult.error("商品信息查询异常");
        }
        if (Objects.equals(productVO.getUserId(), currentUserId)) {
            return ApiResult.error("别自卖自夸!");
        }
        Integer publisherId = productVO.getUserId();
        if (publisherId == null) {
            return ApiResult.error("商品发布者不存在");
        }
        User operator = userMapper.getByActive(User.builder().id(currentUserId).build());
        if (operator == null) {
            return ApiResult.error("当前用户不存在或已删除");
        }
        Message message = new Message();
        message.setUserId(publisherId);
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        message.setContent("用户【" + operator.getUserName() + "】对你的【" + productVO.getName() + "】感兴趣!");
        messageMapper.save(message);
        Interaction interaction = createInteraction(productId, InteractionEnum.LOVE.getType(), currentUserId);
        interactionMapper.save(interaction);
        return ApiResult.success("卖家已感受到你的热情，快下单吧!");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> save(Interaction interaction) {
        if (interaction == null) {
            return ApiResult.error("请求参数不能为空");
        }
        if (interaction.getCreateTime() == null) {
            interaction.setCreateTime(LocalDateTime.now());
        }
        interactionMapper.save(interaction);
        return ApiResult.success("互动行为记录成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchDelete(List<Integer> ids) {
        List<Integer> deleteIds = sanitizeIds(ids);
        if (deleteIds.isEmpty()) {
            return ApiResult.success("没有需要删除的互动行为");
        }
        interactionMapper.batchDelete(deleteIds);
        return ApiResult.success("互动行为删除成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> saveOperation(Integer productId) {
        if (productId == null) {
            return ApiResult.error("商品ID不能为空");
        }
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("登录已失效，请重新登录");
        }
        if (queryProduct(productId) == null) {
            return ApiResult.error("商品不存在或已删除");
        }
        InteractionQueryDto interactionQueryDto = createInteractionQueryDto(productId, InteractionEnum.SAVE.getType(), currentUserId);
        List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);
        if (interactionList == null || interactionList.isEmpty()) {
            Interaction interaction = createInteraction(productId, InteractionEnum.SAVE.getType(), currentUserId);
            interactionMapper.save(interaction);
            return ApiResult.success("收藏成功", true);
        }
        List<Integer> interactionIds = interactionList.stream()
                .map(Interaction::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!interactionIds.isEmpty()) {
            interactionMapper.batchDelete(interactionIds);
        }
        return ApiResult.success("取消收藏成功", false);
    }

    private InteractionQueryDto createInteractionQueryDto(Integer productId, Integer type, Integer currentUserId) {
        InteractionQueryDto queryDto = new InteractionQueryDto();
        queryDto.setUserId(currentUserId);
        queryDto.setType(type);
        queryDto.setProductId(productId);
        return queryDto;
    }

    private Interaction createInteraction(Integer productId, Integer type, Integer currentUserId) {
        Interaction interaction = new Interaction();
        interaction.setUserId(currentUserId);
        interaction.setType(type);
        interaction.setProductId(productId);
        interaction.setCreateTime(LocalDateTime.now());
        return interaction;
    }

    @Override
    public Result<List<Interaction>> query(InteractionQueryDto interactionQueryDto) {
        InteractionQueryDto safeQueryDto = interactionQueryDto == null ? new InteractionQueryDto() : interactionQueryDto;
        int totalCount = interactionMapper.queryCount(safeQueryDto);
        List<Interaction> interactionList = interactionMapper.query(safeQueryDto);
        return ApiResult.success(interactionList == null ? new ArrayList<>() : interactionList, totalCount);
    }

    @Override
    public Result<List<ProductVO>> queryUser() {
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("登录已失效，请重新登录");
        }
        InteractionQueryDto interactionQueryDto = new InteractionQueryDto();
        interactionQueryDto.setUserId(currentUserId);
        interactionQueryDto.setType(InteractionEnum.SAVE.getType());
        List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);
        if (interactionList == null || interactionList.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        List<Integer> productIds = interactionList.stream()
                .map(Interaction::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        List<ProductVO> productVOS = productMapper.queryProductList(productIds);
        return ApiResult.success(productVOS == null ? new ArrayList<>() : productVOS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> view(Integer productId) {
        if (productId == null) {
            return ApiResult.error("商品ID不能为空");
        }
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("登录已失效，请重新登录");
        }
        if (queryProduct(productId) == null) {
            return ApiResult.error("商品不存在或已删除");
        }
        InteractionQueryDto interactionQueryDto = createInteractionQueryDto(productId, InteractionEnum.VIEW.getType(), currentUserId);
        List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);
        if (interactionList == null || interactionList.isEmpty()) {
            Interaction interaction = createInteraction(productId, InteractionEnum.VIEW.getType(), currentUserId);
            interactionMapper.save(interaction);
        }
        return ApiResult.success();
    }

    @Override
    public Result<List<ProductVO>> myView() {
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("登录已失效，请重新登录");
        }
        InteractionQueryDto interactionQueryDto = new InteractionQueryDto();
        interactionQueryDto.setUserId(currentUserId);
        interactionQueryDto.setType(InteractionEnum.VIEW.getType());
        List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);
        if (interactionList == null || interactionList.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        List<Integer> productIds = interactionList.stream()
                .map(Interaction::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (productIds.isEmpty()) {
            return ApiResult.success(new ArrayList<>());
        }
        List<ProductVO> productVOS = productMapper.queryProductList(productIds);
        return ApiResult.success(productVOS == null ? new ArrayList<>() : productVOS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchDeleteInteraction() {
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("登录已失效，请重新登录");
        }
        InteractionQueryDto interactionQueryDto = new InteractionQueryDto();
        interactionQueryDto.setUserId(currentUserId);
        interactionQueryDto.setType(InteractionEnum.VIEW.getType());
        List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);
        if (interactionList == null || interactionList.isEmpty()) {
            return ApiResult.success("没有需要删除的浏览记录");
        }
        List<Integer> ids = interactionList.stream()
                .map(Interaction::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            interactionMapper.batchDelete(ids);
        }
        return ApiResult.success("浏览记录删除成功");
    }

    private ProductVO queryProduct(Integer productId) {
        ProductQueryDto productQueryDto = new ProductQueryDto();
        productQueryDto.setId(productId);
        List<ProductVO> productVOS = productMapper.query(productQueryDto);
        if (productVOS == null || productVOS.isEmpty()) {
            return null;
        }
        return productVOS.get(0);
    }

    private List<Integer> sanitizeIds(List<Integer> ids) {
        Set<Integer> idSet = new LinkedHashSet<>();
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        for (Integer id : ids) {
            if (id != null) {
                idSet.add(id);
            }
        }
        return new ArrayList<>(idSet);
    }
}
