package cn.kmbeast.service.impl;

import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.mapper.InteractionMapper;
import cn.kmbeast.mapper.ContentMapper;
import cn.kmbeast.mapper.MessageMapper;
import cn.kmbeast.mapper.OperationLogMapper;
import cn.kmbeast.mapper.OrdersMapper;
import cn.kmbeast.mapper.ProductMapper;
import cn.kmbeast.mapper.UserMapper;
import cn.kmbeast.pojo.api.ApiResult;
import cn.kmbeast.pojo.api.PageResult;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.ContentQueryDto;
import cn.kmbeast.pojo.dto.query.extend.MessageQueryDto;
import cn.kmbeast.pojo.dto.query.extend.OperationLogQueryDto;
import cn.kmbeast.pojo.dto.query.extend.OrdersQueryDto;
import cn.kmbeast.pojo.dto.query.extend.UserQueryDto;
import cn.kmbeast.pojo.dto.update.UserLoginDTO;
import cn.kmbeast.pojo.dto.update.UserRegisterDTO;
import cn.kmbeast.pojo.dto.update.UserUpdateDTO;
import cn.kmbeast.pojo.em.LoginStatusEnum;
import cn.kmbeast.pojo.em.RoleEnum;
import cn.kmbeast.pojo.em.WordStatusEnum;
import cn.kmbeast.pojo.entity.Interaction;
import cn.kmbeast.pojo.entity.User;
import cn.kmbeast.pojo.vo.ContentVO;
import cn.kmbeast.pojo.vo.MessageVO;
import cn.kmbeast.pojo.vo.OperationLogVO;
import cn.kmbeast.pojo.vo.OrdersVO;
import cn.kmbeast.pojo.vo.UserVO;
import cn.kmbeast.service.UserService;
import cn.kmbeast.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private ProductMapper productMapper;

    @Resource
    private InteractionMapper interactionMapper;

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private ContentMapper contentMapper;

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private OperationLogMapper operationLogMapper;

    /**
     * 用户注册
     *
     * @param userRegisterDTO 注册入参
     * @return Result<String> 响应结果
     */
    @Override
    public Result<String> register(UserRegisterDTO userRegisterDTO) {
        User entity = userMapper.getByActive(
                User.builder().userAccount(userRegisterDTO.getUserAccount()).build()
        );
        if (Objects.nonNull(entity)) {
            return ApiResult.error("账号不可用");
        }
        User saveEntity = User.builder()
                .userRole(RoleEnum.USER.getRole())
                .userName(userRegisterDTO.getUserName())
                .userAccount(userRegisterDTO.getUserAccount())
                .userAvatar(userRegisterDTO.getUserAvatar())
                .userPwd(userRegisterDTO.getUserPwd())
                .userEmail(userRegisterDTO.getUserEmail())
                .createTime(LocalDateTime.now())
                .isLogin(LoginStatusEnum.USE.getFlag())
                .isWord(WordStatusEnum.USE.getFlag()).build();
        userMapper.insert(saveEntity);
        return ApiResult.success("注册成功");
    }

    /**
     * 用户登录
     *
     * @param userLoginDTO 登录入参
     * @return Result<String> 响应结果
     */
    @Override
    public Result<Object> login(UserLoginDTO userLoginDTO) {
        User user = userMapper.getByActive(
                User.builder().userAccount(userLoginDTO.getUserAccount()).build()
        );
        if (!Objects.nonNull(user)) {
            return ApiResult.error("账号不存在");
        }
        if (!Objects.equals(userLoginDTO.getUserPwd(), user.getUserPwd())) {
            return ApiResult.error("密码错误");
        }
        if (Boolean.TRUE.equals(user.getIsLogin())) {
            return ApiResult.error("登录状态异常");
        }
        String token = JwtUtil.toToken(user.getId(), user.getUserRole());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("role", user.getUserRole());
        // 设置上一次登录时间
        User userEntity = new User();
        userEntity.setId(user.getId());
        userEntity.setLastLoginTime(LocalDateTime.now());
        userMapper.update(userEntity);
        return ApiResult.success("登录成功", map);
    }

    /**
     * 令牌检验 -- 认证成功返回用户信息
     *
     * @return Result<UserVO>
     */
    @Override
    public Result<UserVO> auth() {
        Integer userId = LocalThreadHolder.getUserId();
        User queryEntity = User.builder().id(userId).build();
        User user = userMapper.getByActive(queryEntity);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ApiResult.success(userVO);
    }

    /**
     * 分页查询用户数据
     *
     * @param userQueryDto 分页参数
     * @return Result<List < User>> 响应结果
     */
    @Override
    public Result<List<User>> query(UserQueryDto userQueryDto) {
        List<User> users = userMapper.query(userQueryDto);
        Integer count = userMapper.queryCount(userQueryDto);
        return PageResult.success(users, count);
    }

    /**
     * 用户信息修改
     *
     * @param userUpdateDTO 修改信息入参
     * @return Result<String> 响应结果
     */
    @Override
    public Result<String> update(UserUpdateDTO userUpdateDTO) {
        User updateEntity = User.builder().id(LocalThreadHolder.getUserId()).build();
        BeanUtils.copyProperties(userUpdateDTO, updateEntity);
        userMapper.update(updateEntity);
        return ApiResult.success();
    }


    /**
     * 批量删除用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchDelete(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return ApiResult.success();
        }
        List<Integer> productIds = queryOwnedProductIds(ids);
        if (!productIds.isEmpty()) {
            deleteProductInteractions(productIds);
            deleteProductOrders(productIds);
            productMapper.batchDelete(productIds);
            log.info("Cascade deleted products for removed users. userIds={}, productIds={}", ids, productIds);
        }

        deleteUserContents(ids);
        deleteUserMessages(ids);
        deleteUserOperationLogs(ids);
        userMapper.batchDelete(ids);
        return ApiResult.success();
    }

    private List<Integer> queryOwnedProductIds(List<Integer> userIds) {
        List<Integer> productIds = new ArrayList<>();
        for (Integer userId : userIds) {
            if (userId == null) {
                continue;
            }
            List<Integer> ownedProductIds = productMapper.queryProductIds(userId);
            if (ownedProductIds == null || ownedProductIds.isEmpty()) {
                continue;
            }
            for (Integer productId : ownedProductIds) {
                if (productId != null && !productIds.contains(productId)) {
                    productIds.add(productId);
                }
            }
        }
        return productIds;
    }
    private void deleteProductInteractions(List<Integer> productIds) {
        List<Interaction> interactionList = interactionMapper.queryByProductIds(productIds);
        if (interactionList == null || interactionList.isEmpty()) {
            return;
        }
        List<Integer> interactionIds = new ArrayList<>();
        for (Interaction interaction : interactionList) {
            if (interaction.getId() != null) {
                interactionIds.add(interaction.getId());
            }
        }
        if (!interactionIds.isEmpty()) {
            interactionMapper.batchDelete(interactionIds);
        }
    }

    private void deleteProductOrders(List<Integer> productIds) {
        OrdersQueryDto ordersQueryDto = new OrdersQueryDto();
        ordersQueryDto.setProductIds(productIds);
        List<OrdersVO> ordersVOList = ordersMapper.queryByProductIds(ordersQueryDto);
        if (ordersVOList == null || ordersVOList.isEmpty()) {
            return;
        }
        List<Integer> orderIds = new ArrayList<>();
        for (OrdersVO ordersVO : ordersVOList) {
            if (ordersVO.getId() != null) {
                orderIds.add(ordersVO.getId());
            }
        }
        if (!orderIds.isEmpty()) {
            ordersMapper.batchDelete(orderIds);
        }
    }

    private void deleteUserContents(List<Integer> userIds) {
        List<Integer> contentIds = new ArrayList<>();
        for (Integer userId : userIds) {
            if (userId == null) {
                continue;
            }
            ContentQueryDto contentQueryDto = new ContentQueryDto();
            contentQueryDto.setUserId(userId);
            List<ContentVO> contentVOList = contentMapper.query(contentQueryDto);
            if (contentVOList == null || contentVOList.isEmpty()) {
                continue;
            }
            for (ContentVO contentVO : contentVOList) {
                if (contentVO.getId() != null && !contentIds.contains(contentVO.getId())) {
                    contentIds.add(contentVO.getId());
                }
            }
        }
        if (!contentIds.isEmpty()) {
            contentMapper.batchDelete(contentIds);
            log.info("Cascade deleted contents for removed users. userIds={}, contentIds={}", userIds, contentIds);
        }
    }

    private void deleteUserMessages(List<Integer> userIds) {
        List<Integer> messageIds = new ArrayList<>();
        for (Integer userId : userIds) {
            if (userId == null) {
                continue;
            }
            MessageQueryDto messageQueryDto = new MessageQueryDto();
            messageQueryDto.setUserId(userId);
            List<MessageVO> messageVOList = messageMapper.query(messageQueryDto);
            if (messageVOList == null || messageVOList.isEmpty()) {
                continue;
            }
            for (MessageVO messageVO : messageVOList) {
                if (messageVO.getId() != null && !messageIds.contains(messageVO.getId())) {
                    messageIds.add(messageVO.getId());
                }
            }
        }
        if (!messageIds.isEmpty()) {
            messageMapper.batchDelete(messageIds);
            log.info("Cascade deleted messages for removed users. userIds={}, messageIds={}", userIds, messageIds);
        }
    }

    private void deleteUserOperationLogs(List<Integer> userIds) {
        List<Integer> operationLogIds = new ArrayList<>();
        for (Integer userId : userIds) {
            if (userId == null) {
                continue;
            }
            OperationLogQueryDto operationLogQueryDto = new OperationLogQueryDto();
            operationLogQueryDto.setUserId(userId);
            List<OperationLogVO> operationLogVOList = operationLogMapper.query(operationLogQueryDto);
            if (operationLogVOList == null || operationLogVOList.isEmpty()) {
                continue;
            }
            for (OperationLogVO operationLogVO : operationLogVOList) {
                if (operationLogVO.getId() != null && !operationLogIds.contains(operationLogVO.getId())) {
                    operationLogIds.add(operationLogVO.getId());
                }
            }
        }
        if (!operationLogIds.isEmpty()) {
            operationLogMapper.batchDelete(operationLogIds);
            log.info("Cascade deleted operation logs for removed users. userIds={}, operationLogIds={}", userIds, operationLogIds);
        }
    }

    /**
     * 用户信息修改密码
     *
     * @param map 修改信息入参
     * @return Result<String> 响应结果
     */
    @Override
    public Result<String> updatePwd(Map<String, String> map) {
        String oldPwd = map.get("oldPwd");
        String newPwd = map.get("newPwd");
        String againPwd = map.get("againPwd");
        if (Objects.isNull(oldPwd)) {
            return ApiResult.error("原始密码输入不能为空");
        }
        if (Objects.isNull(newPwd)) {
            return ApiResult.error("请输入新密码");
        }
        if (Objects.isNull(againPwd)) {
            return ApiResult.error("请补充确认密码");
        }
        if (!newPwd.equals(againPwd)) {
            return ApiResult.error("前后密码输入不一致");
        }
        User user = userMapper.getByActive(
                User.builder().id(LocalThreadHolder.getUserId()).build()
        );
        if (!user.getUserPwd().equals(oldPwd)) {
            return ApiResult.error("原始密码验证失败");
        }
        user.setUserPwd(newPwd);
        userMapper.update(user);
        return ApiResult.success();
    }

    /**
     * 通过ID查询用户信息
     *
     * @param id 用户ID
     */
    @Override
    public Result<UserVO> getById(Integer id) {
        User user = userMapper.getByActive(User.builder().id(id).build());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ApiResult.success(userVO);
    }

    /**
     * 后台新增用户
     *
     * @param userRegisterDTO 注册入参
     * @return Result<String> 响应结果
     */
    @Override
    public Result<String> insert(UserRegisterDTO userRegisterDTO) {
        return register(userRegisterDTO);
    }

    /**
     * 后台用户信息修改
     *
     * @param user 信息实体
     * @return Result<String> 响应结果
     */
    @Override
    public Result<String> backUpdate(User user) {
        userMapper.update(user);
        return ApiResult.success();
    }

}
