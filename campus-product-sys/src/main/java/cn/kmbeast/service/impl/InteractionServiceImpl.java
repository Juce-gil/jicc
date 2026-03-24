package cn.kmbeast.service.impl;

import cn.kmbeast.aop.Protector;
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
 * Interaction behavior service implementation.
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
            return ApiResult.error("product id cannot be empty");
        }
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("login expired, please login again");
        }
        InteractionQueryDto interactionQueryDto = createInteractionQueryDto(
                productId, InteractionEnum.LOVE.getType(), currentUserId
        );
        if (interactionMapper.queryCount(interactionQueryDto) > 0) {
            return ApiResult.error("duplicate operation is not allowed");
        }
        ProductVO productVO = queryProduct(productId);
        if (productVO == null) {
            return ApiResult.error("product not found");
        }
        if (Objects.equals(productVO.getUserId(), currentUserId)) {
            return ApiResult.error("you cannot like your own product");
        }
        Integer publisherId = productVO.getUserId();
        if (publisherId == null) {
            return ApiResult.error("product owner not found");
        }
        User operator = userMapper.getByActive(User.builder().id(currentUserId).build());
        if (operator == null) {
            return ApiResult.error("current user not found");
        }
        Message message = new Message();
        message.setUserId(publisherId);
        message.setIsRead(false);
        message.setCreateTime(LocalDateTime.now());
        message.setContent(
                "user [" + operator.getUserName() + "] is interested in your product [" + productVO.getName() + "]"
        );
        messageMapper.save(message);
        Interaction interaction = createInteraction(productId, InteractionEnum.LOVE.getType(), currentUserId);
        interactionMapper.save(interaction);
        return ApiResult.success("like recorded successfully");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> save(Interaction interaction) {
        if (interaction == null) {
            return ApiResult.error("payload cannot be empty");
        }
        if (interaction.getCreateTime() == null) {
            interaction.setCreateTime(LocalDateTime.now());
        }
        interactionMapper.save(interaction);
        return ApiResult.success("interaction created successfully");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchDelete(List<Integer> ids) {
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("login expired, please login again");
        }
        List<Integer> deleteIds = sanitizeIds(ids);
        if (deleteIds.isEmpty()) {
            return ApiResult.success("no interaction records need to be deleted");
        }
        if (!isAdmin(LocalThreadHolder.getRoleId())) {
            deleteIds = filterOwnedInteractionIds(deleteIds, currentUserId);
            if (deleteIds.isEmpty()) {
                return ApiResult.error("you can only delete your own interaction records");
            }
        }
        interactionMapper.batchDelete(deleteIds);
        return ApiResult.success("interaction records deleted successfully");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> saveOperation(Integer productId) {
        if (productId == null) {
            return ApiResult.error("product id cannot be empty");
        }
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("login expired, please login again");
        }
        if (queryProduct(productId) == null) {
            return ApiResult.error("product not found");
        }
        InteractionQueryDto interactionQueryDto = createInteractionQueryDto(
                productId, InteractionEnum.SAVE.getType(), currentUserId
        );
        List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);
        if (interactionList == null || interactionList.isEmpty()) {
            Interaction interaction = createInteraction(productId, InteractionEnum.SAVE.getType(), currentUserId);
            interactionMapper.save(interaction);
            return ApiResult.success("favorite created", true);
        }
        List<Integer> interactionIds = interactionList.stream()
                .map(Interaction::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!interactionIds.isEmpty()) {
            interactionMapper.batchDelete(interactionIds);
        }
        return ApiResult.success("favorite removed", false);
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
            return ApiResult.error("login expired, please login again");
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
            return ApiResult.error("product id cannot be empty");
        }
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("login expired, please login again");
        }
        if (queryProduct(productId) == null) {
            return ApiResult.error("product not found");
        }
        InteractionQueryDto interactionQueryDto = createInteractionQueryDto(
                productId, InteractionEnum.VIEW.getType(), currentUserId
        );
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
            return ApiResult.error("login expired, please login again");
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
            return ApiResult.error("login expired, please login again");
        }
        InteractionQueryDto interactionQueryDto = new InteractionQueryDto();
        interactionQueryDto.setUserId(currentUserId);
        interactionQueryDto.setType(InteractionEnum.VIEW.getType());
        List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);
        if (interactionList == null || interactionList.isEmpty()) {
            return ApiResult.success("no browsing records need to be deleted");
        }
        List<Integer> ids = interactionList.stream()
                .map(Interaction::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (!ids.isEmpty()) {
            interactionMapper.batchDelete(ids);
        }
        return ApiResult.success("browsing records deleted successfully");
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

    private List<Integer> filterOwnedInteractionIds(List<Integer> ids, Integer currentUserId) {
        List<Integer> result = new ArrayList<>();
        if (ids == null || ids.isEmpty() || currentUserId == null) {
            return result;
        }
        for (Integer id : ids) {
            InteractionQueryDto queryDto = new InteractionQueryDto();
            queryDto.setId(id);
            queryDto.setUserId(currentUserId);
            if (interactionMapper.queryCount(queryDto) > 0) {
                result.add(id);
            }
        }
        return result;
    }

    private boolean isAdmin(Integer roleId) {
        return Objects.equals(roleId, Protector.ROLE_ADMIN);
    }
}
