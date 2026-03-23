package cn.kmbeast.service.impl;

import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.mapper.AddressMapper;
import cn.kmbeast.mapper.ContentMapper;
import cn.kmbeast.mapper.EvaluationsMapper;
import cn.kmbeast.mapper.EvaluationsUpvoteMapper;
import cn.kmbeast.mapper.InteractionMapper;
import cn.kmbeast.mapper.MessageMapper;
import cn.kmbeast.mapper.OperationLogMapper;
import cn.kmbeast.mapper.OrdersMapper;
import cn.kmbeast.mapper.ProductMapper;
import cn.kmbeast.mapper.UserMapper;
import cn.kmbeast.pojo.api.ApiResult;
import cn.kmbeast.pojo.api.PageResult;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.AddressQueryDto;
import cn.kmbeast.pojo.dto.query.extend.ContentQueryDto;
import cn.kmbeast.pojo.dto.query.extend.InteractionQueryDto;
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
import cn.kmbeast.pojo.entity.Address;
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
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 鐢ㄦ埛鏈嶅姟瀹炵幇绫? */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private AddressMapper addressMapper;

    @Resource
    private EvaluationsMapper evaluationsMapper;

    @Resource
    private EvaluationsUpvoteMapper evaluationsUpvoteMapper;

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
     * 鐢ㄦ埛娉ㄥ唽
     *
     * @param userRegisterDTO 娉ㄥ唽鍏ュ弬
     * @return Result<String> 鍝嶅簲缁撴灉
     */
    @Override
    public Result<String> register(UserRegisterDTO userRegisterDTO) {
        if (userRegisterDTO == null) {
            return ApiResult.error("璇锋眰鍙傛暟涓嶈兘涓虹┖");
        }
        if (!StringUtils.hasText(userRegisterDTO.getUserAccount())) {
            return ApiResult.error("璐﹀彿涓嶈兘涓虹┖");
        }
        if (!StringUtils.hasText(userRegisterDTO.getUserName())) {
            return ApiResult.error("鐢ㄦ埛鍚嶄笉鑳戒负绌?);
        }
        if (!StringUtils.hasText(userRegisterDTO.getUserPwd())) {
            return ApiResult.error("瀵嗙爜涓嶈兘涓虹┖");
        }

        String userAccount = userRegisterDTO.getUserAccount().trim();
        if (existsOtherUserWithAccount(userAccount, null)) {
            return ApiResult.error("璐﹀彿涓嶅彲鐢?);
        }

        User saveEntity = User.builder()
                .userRole(RoleEnum.USER.getRole())
                .userName(userRegisterDTO.getUserName().trim())
                .userAccount(userAccount)
                .userAvatar(normalizeOptionalText(userRegisterDTO.getUserAvatar()))
                .userPwd(userRegisterDTO.getUserPwd())
                .userEmail(normalizeOptionalText(userRegisterDTO.getUserEmail()))
                .createTime(LocalDateTime.now())
                .isLogin(LoginStatusEnum.USE.getFlag())
                .isWord(WordStatusEnum.USE.getFlag())
                .build();
        userMapper.insert(saveEntity);
        return ApiResult.success("娉ㄥ唽鎴愬姛");
    }

    /**
     * 鐢ㄦ埛鐧诲綍
     *
     * @param userLoginDTO 鐧诲綍鍏ュ弬
     * @return Result<String> 鍝嶅簲缁撴灉
     */
    @Override
    public Result<Object> login(UserLoginDTO userLoginDTO) {
        if (userLoginDTO == null) {
            return ApiResult.error("璇锋眰鍙傛暟涓嶈兘涓虹┖");
        }
        if (!StringUtils.hasText(userLoginDTO.getUserAccount())) {
            return ApiResult.error("璐﹀彿涓嶈兘涓虹┖");
        }
        if (!StringUtils.hasText(userLoginDTO.getUserPwd())) {
            return ApiResult.error("瀵嗙爜涓嶈兘涓虹┖");
        }

        User user = userMapper.getByActive(
                User.builder().userAccount(userLoginDTO.getUserAccount().trim()).build()
        );
        if (user == null) {
            return ApiResult.error("璐﹀彿涓嶅瓨鍦?);
        }
        if (!Objects.equals(userLoginDTO.getUserPwd(), user.getUserPwd())) {
            return ApiResult.error("瀵嗙爜閿欒");
        }
        if (Boolean.TRUE.equals(user.getIsLogin())) {
            return ApiResult.error("鐧诲綍鐘舵€佸紓甯?);
        }

        String token = JwtUtil.toToken(user.getId(), user.getUserRole());
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("role", user.getUserRole());

        User userEntity = new User();
        userEntity.setId(user.getId());
        userEntity.setLastLoginTime(LocalDateTime.now());
        userMapper.update(userEntity);
        return ApiResult.success("鐧诲綍鎴愬姛", map);
    }

    /**
     * 浠ょ墝鏍￠獙锛岃璇佹垚鍔熷悗杩斿洖鐢ㄦ埛淇℃伅
     *
     * @return Result<UserVO>
     */
    @Override
    public Result<UserVO> auth() {
        Integer userId = LocalThreadHolder.getUserId();
        if (userId == null) {
            return ApiResult.error("鐧诲綍宸插け鏁堬紝璇烽噸鏂扮櫥褰?);
        }

        User user = userMapper.getByActive(User.builder().id(userId).build());
        if (user == null) {
            return ApiResult.error("鐢ㄦ埛涓嶅瓨鍦ㄦ垨宸插垹闄?);
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ApiResult.success(userVO);
    }

    /**
     * 鍒嗛〉鏌ヨ鐢ㄦ埛鏁版嵁
     *
     * @param userQueryDto 鍒嗛〉鍙傛暟
     * @return Result<List<User>> 鍝嶅簲缁撴灉
     */
    @Override
    public Result<List<User>> query(UserQueryDto userQueryDto) {
        UserQueryDto safeQueryDto = userQueryDto == null ? new UserQueryDto() : userQueryDto;
        List<User> users = userMapper.query(safeQueryDto);
        Integer count = userMapper.queryCount(safeQueryDto);
        return PageResult.success(users, count);
    }

    /**
     * 鐢ㄦ埛淇℃伅淇敼
     *
     * @param userUpdateDTO 淇敼淇℃伅鍏ュ弬
     * @return Result<String> 鍝嶅簲缁撴灉
     */
    @Override
    public Result<String> update(UserUpdateDTO userUpdateDTO) {
        if (userUpdateDTO == null) {
            return ApiResult.error("璇锋眰鍙傛暟涓嶈兘涓虹┖");
        }

        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("鐧诲綍宸插け鏁堬紝璇烽噸鏂扮櫥褰?);
        }
        User currentUser = userMapper.getByActive(User.builder().id(currentUserId).build());
        if (currentUser == null) {
            return ApiResult.error("鐢ㄦ埛涓嶅瓨鍦ㄦ垨宸插垹闄?);
        }

        if (userUpdateDTO.getUserAccount() != null && !StringUtils.hasText(userUpdateDTO.getUserAccount())) {
            return ApiResult.error("璐﹀彿涓嶈兘涓虹┖");
        }
        if (userUpdateDTO.getUserName() != null && !StringUtils.hasText(userUpdateDTO.getUserName())) {
            return ApiResult.error("鐢ㄦ埛鍚嶄笉鑳戒负绌?);
        }
        if (userUpdateDTO.getUserPwd() != null && !StringUtils.hasText(userUpdateDTO.getUserPwd())) {
            return ApiResult.error("瀵嗙爜涓嶈兘涓虹┖");
        }
        if (StringUtils.hasText(userUpdateDTO.getUserAccount())
                && existsOtherUserWithAccount(userUpdateDTO.getUserAccount().trim(), currentUserId)) {
            return ApiResult.error("璐﹀彿涓嶅彲鐢?);
        }

        User updateEntity = User.builder().id(currentUserId).build();
        BeanUtils.copyProperties(userUpdateDTO, updateEntity);
        normalizeUserMutableFields(updateEntity);
        userMapper.update(updateEntity);
        return ApiResult.success();
    }

    /**
     * 鎵归噺鍒犻櫎鐢ㄦ埛淇℃伅
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> batchDelete(List<Integer> ids) {
        List<Integer> userIds = sanitizeIds(ids);
        if (userIds.isEmpty()) {
            return ApiResult.success();
        }

        deleteUserInteractions(userIds);
        deleteUserOrders(userIds);
        deleteUserAddresses(userIds);
        deleteUserEvaluations(userIds);

        List<Integer> productIds = queryOwnedProductIds(userIds);
        if (!productIds.isEmpty()) {
            deleteProductInteractions(productIds);
            deleteProductOrders(productIds);
            productMapper.batchDelete(productIds);
            log.info("Cascade deleted products for removed users. userIds={}, productIds={}", userIds, productIds);
        }

        deleteUserContents(userIds);
        deleteUserMessages(userIds);
        deleteUserOperationLogs(userIds);
        userMapper.batchDelete(userIds);
        return ApiResult.success();
    }

    private List<Integer> queryOwnedProductIds(List<Integer> userIds) {
        Set<Integer> productIdSet = new LinkedHashSet<>();
        for (Integer userId : userIds) {
            List<Integer> ownedProductIds = productMapper.queryProductIds(userId);
            if (ownedProductIds == null || ownedProductIds.isEmpty()) {
                continue;
            }
            for (Integer productId : ownedProductIds) {
                if (productId != null) {
                    productIdSet.add(productId);
                }
            }
        }
        return new ArrayList<>(productIdSet);
    }

    private void deleteProductInteractions(List<Integer> productIds) {
        List<Interaction> interactionList = interactionMapper.queryByProductIds(productIds);
        if (interactionList == null || interactionList.isEmpty()) {
            return;
        }
        Set<Integer> interactionIds = new LinkedHashSet<>();
        for (Interaction interaction : interactionList) {
            if (interaction != null && interaction.getId() != null) {
                interactionIds.add(interaction.getId());
            }
        }
        if (!interactionIds.isEmpty()) {
            interactionMapper.batchDelete(new ArrayList<>(interactionIds));
        }
    }

    private void deleteProductOrders(List<Integer> productIds) {
        OrdersQueryDto ordersQueryDto = new OrdersQueryDto();
        ordersQueryDto.setProductIds(productIds);
        List<OrdersVO> ordersVOList = ordersMapper.queryByProductIds(ordersQueryDto);
        if (ordersVOList == null || ordersVOList.isEmpty()) {
            return;
        }
        Set<Integer> orderIds = new LinkedHashSet<>();
        for (OrdersVO ordersVO : ordersVOList) {
            if (ordersVO != null && ordersVO.getId() != null) {
                orderIds.add(ordersVO.getId());
            }
        }
        if (!orderIds.isEmpty()) {
            ordersMapper.batchDelete(new ArrayList<>(orderIds));
        }
    }

    private void deleteUserInteractions(List<Integer> userIds) {
        Set<Integer> interactionIds = new LinkedHashSet<>();
        for (Integer userId : userIds) {
            InteractionQueryDto interactionQueryDto = new InteractionQueryDto();
            interactionQueryDto.setUserId(userId);
            List<Interaction> interactionList = interactionMapper.query(interactionQueryDto);
            if (interactionList == null || interactionList.isEmpty()) {
                continue;
            }
            for (Interaction interaction : interactionList) {
                if (interaction != null && interaction.getId() != null) {
                    interactionIds.add(interaction.getId());
                }
            }
        }
        if (!interactionIds.isEmpty()) {
            List<Integer> deleteIds = new ArrayList<>(interactionIds);
            interactionMapper.batchDelete(deleteIds);
            log.info("Cascade deleted interactions created by removed users. userIds={}, interactionIds={}", userIds, deleteIds);
        }
    }

    private void deleteUserOrders(List<Integer> userIds) {
        Set<Integer> orderIds = new LinkedHashSet<>();
        for (Integer userId : userIds) {
            OrdersQueryDto ordersQueryDto = new OrdersQueryDto();
            ordersQueryDto.setUserId(userId);
            List<OrdersVO> ordersVOList = ordersMapper.query(ordersQueryDto);
            if (ordersVOList == null || ordersVOList.isEmpty()) {
                continue;
            }
            for (OrdersVO ordersVO : ordersVOList) {
                if (ordersVO != null && ordersVO.getId() != null) {
                    orderIds.add(ordersVO.getId());
                }
            }
        }
        if (!orderIds.isEmpty()) {
            List<Integer> deleteIds = new ArrayList<>(orderIds);
            ordersMapper.batchDelete(deleteIds);
            log.info("Cascade deleted orders created by removed users. userIds={}, orderIds={}", userIds, deleteIds);
        }
    }

    private void deleteUserAddresses(List<Integer> userIds) {
        Set<Integer> addressIds = new LinkedHashSet<>();
        for (Integer userId : userIds) {
            AddressQueryDto addressQueryDto = new AddressQueryDto();
            addressQueryDto.setUserId(userId);
            List<Address> addressList = addressMapper.query(addressQueryDto);
            if (addressList == null || addressList.isEmpty()) {
                continue;
            }
            for (Address address : addressList) {
                if (address != null && address.getId() != null) {
                    addressIds.add(address.getId());
                }
            }
        }
        if (!addressIds.isEmpty()) {
            List<Integer> deleteIds = new ArrayList<>(addressIds);
            addressMapper.batchDelete(deleteIds);
            log.info("Cascade deleted addresses for removed users. userIds={}, addressIds={}", userIds, deleteIds);
        }
    }

    private void deleteUserEvaluations(List<Integer> userIds) {
        Set<Integer> evaluationIds = new LinkedHashSet<>();
        for (Integer userId : userIds) {
            evaluationsUpvoteMapper.batchDeleteByUserId(userId);
            List<Integer> currentIds = evaluationsMapper.selectIdsByCommenterId(userId);
            if (currentIds == null || currentIds.isEmpty()) {
                continue;
            }
            for (Integer id : currentIds) {
                if (id != null) {
                    evaluationIds.add(id);
                }
            }
        }
        if (evaluationIds.isEmpty()) {
            return;
        }
        List<Integer> deleteIds = new ArrayList<>(evaluationIds);
        List<Integer> childIds = evaluationsMapper.selectChildComments(deleteIds);
        if (childIds != null) {
            for (Integer childId : childIds) {
                if (childId != null) {
                    evaluationIds.add(childId);
                }
            }
        }
        List<Integer> finalDeleteIds = new ArrayList<>(evaluationIds);
        evaluationsUpvoteMapper.batchDeleteByEvaluationsIds(finalDeleteIds);
        evaluationsMapper.batchDelete(finalDeleteIds);
        log.info("Cascade deleted evaluations for removed users. userIds={}, evaluationIds={}", userIds, finalDeleteIds);
    }

    private void deleteUserContents(List<Integer> userIds) {
        Set<Integer> contentIds = new LinkedHashSet<>();
        for (Integer userId : userIds) {
            ContentQueryDto contentQueryDto = new ContentQueryDto();
            contentQueryDto.setUserId(userId);
            List<ContentVO> contentVOList = contentMapper.query(contentQueryDto);
            if (contentVOList == null || contentVOList.isEmpty()) {
                continue;
            }
            for (ContentVO contentVO : contentVOList) {
                if (contentVO != null && contentVO.getId() != null) {
                    contentIds.add(contentVO.getId());
                }
            }
        }
        if (!contentIds.isEmpty()) {
            List<Integer> deleteIds = new ArrayList<>(contentIds);
            contentMapper.batchDelete(deleteIds);
            log.info("Cascade deleted contents for removed users. userIds={}, contentIds={}", userIds, deleteIds);
        }
    }

    private void deleteUserMessages(List<Integer> userIds) {
        Set<Integer> messageIds = new LinkedHashSet<>();
        for (Integer userId : userIds) {
            MessageQueryDto messageQueryDto = new MessageQueryDto();
            messageQueryDto.setUserId(userId);
            List<MessageVO> messageVOList = messageMapper.query(messageQueryDto);
            if (messageVOList == null || messageVOList.isEmpty()) {
                continue;
            }
            for (MessageVO messageVO : messageVOList) {
                if (messageVO != null && messageVO.getId() != null) {
                    messageIds.add(messageVO.getId());
                }
            }
        }
        if (!messageIds.isEmpty()) {
            List<Integer> deleteIds = new ArrayList<>(messageIds);
            messageMapper.batchDelete(deleteIds);
            log.info("Cascade deleted messages for removed users. userIds={}, messageIds={}", userIds, deleteIds);
        }
    }

    private void deleteUserOperationLogs(List<Integer> userIds) {
        Set<Integer> operationLogIds = new LinkedHashSet<>();
        for (Integer userId : userIds) {
            OperationLogQueryDto operationLogQueryDto = new OperationLogQueryDto();
            operationLogQueryDto.setUserId(userId);
            List<OperationLogVO> operationLogVOList = operationLogMapper.query(operationLogQueryDto);
            if (operationLogVOList == null || operationLogVOList.isEmpty()) {
                continue;
            }
            for (OperationLogVO operationLogVO : operationLogVOList) {
                if (operationLogVO != null && operationLogVO.getId() != null) {
                    operationLogIds.add(operationLogVO.getId());
                }
            }
        }
        if (!operationLogIds.isEmpty()) {
            List<Integer> deleteIds = new ArrayList<>(operationLogIds);
            operationLogMapper.batchDelete(deleteIds);
            log.info("Cascade deleted operation logs for removed users. userIds={}, operationLogIds={}", userIds, deleteIds);
        }
    }

    /**
     * 鐢ㄦ埛淇℃伅淇敼瀵嗙爜
     *
     * @param map 淇敼淇℃伅鍏ュ弬
     * @return Result<String> 鍝嶅簲缁撴灉
     */
    @Override
    public Result<String> updatePwd(Map<String, String> map) {
        if (map == null) {
            return ApiResult.error("璇锋眰鍙傛暟涓嶈兘涓虹┖");
        }

        String oldPwd = map.get("oldPwd");
        String newPwd = map.get("newPwd");
        String againPwd = map.get("againPwd");
        if (!StringUtils.hasText(oldPwd)) {
            return ApiResult.error("鍘熷瀵嗙爜杈撳叆涓嶈兘涓虹┖");
        }
        if (!StringUtils.hasText(newPwd)) {
            return ApiResult.error("璇疯緭鍏ユ柊瀵嗙爜");
        }
        if (!StringUtils.hasText(againPwd)) {
            return ApiResult.error("璇疯ˉ鍏呯‘璁ゅ瘑鐮?);
        }
        if (!Objects.equals(newPwd, againPwd)) {
            return ApiResult.error("鍓嶅悗瀵嗙爜杈撳叆涓嶄竴鑷?);
        }

        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("鐧诲綍宸插け鏁堬紝璇烽噸鏂扮櫥褰?);
        }

        User user = userMapper.getByActive(User.builder().id(currentUserId).build());
        if (user == null) {
            return ApiResult.error("鐢ㄦ埛涓嶅瓨鍦ㄦ垨宸插垹闄?);
        }
        if (!Objects.equals(user.getUserPwd(), oldPwd)) {
            return ApiResult.error("鍘熷瀵嗙爜楠岃瘉澶辫触");
        }

        user.setUserPwd(newPwd);
        userMapper.update(user);
        return ApiResult.success();
    }

    /**
     * 閫氳繃ID鏌ヨ鐢ㄦ埛淇℃伅
     *
     * @param id 鐢ㄦ埛ID
     */
    @Override
    public Result<UserVO> getById(Integer id) {
        if (id == null) {
            return ApiResult.error("鐢ㄦ埛ID涓嶈兘涓虹┖");
        }

        User user = userMapper.getByActive(User.builder().id(id).build());
        if (user == null) {
            return ApiResult.error("鐢ㄦ埛涓嶅瓨鍦ㄦ垨宸插垹闄?);
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return ApiResult.success(userVO);
    }

    /**
     * 鍚庡彴鏂板鐢ㄦ埛
     *
     * @param userRegisterDTO 娉ㄥ唽鍏ュ弬
     * @return Result<String> 鍝嶅簲缁撴灉
     */
    @Override
    public Result<String> insert(UserRegisterDTO userRegisterDTO) {
        return register(userRegisterDTO);
    }

    /**
     * 鍚庡彴鐢ㄦ埛淇℃伅淇敼
     *
     * @param user 淇℃伅瀹炰綋
     * @return Result<String> 鍝嶅簲缁撴灉
     */
    @Override
    public Result<String> backUpdate(User user) {
        if (user == null || user.getId() == null) {
            return ApiResult.error("鐢ㄦ埛ID涓嶈兘涓虹┖");
        }

        User existingUser = userMapper.getByActive(User.builder().id(user.getId()).build());
        if (existingUser == null) {
            return ApiResult.error("鐢ㄦ埛涓嶅瓨鍦ㄦ垨宸插垹闄?);
        }

        if (user.getUserAccount() != null && !StringUtils.hasText(user.getUserAccount())) {
            return ApiResult.error("璐﹀彿涓嶈兘涓虹┖");
        }
        if (user.getUserName() != null && !StringUtils.hasText(user.getUserName())) {
            return ApiResult.error("鐢ㄦ埛鍚嶄笉鑳戒负绌?);
        }
        if (user.getUserPwd() != null && !StringUtils.hasText(user.getUserPwd())) {
            return ApiResult.error("瀵嗙爜涓嶈兘涓虹┖");
        }
        if (StringUtils.hasText(user.getUserAccount())
                && existsOtherUserWithAccount(user.getUserAccount().trim(), user.getId())) {
            return ApiResult.error("璐﹀彿涓嶅彲鐢?);
        }

        normalizeUserMutableFields(user);
        userMapper.update(user);
        return ApiResult.success();
    }

    private boolean existsOtherUserWithAccount(String userAccount, Integer excludeUserId) {
        if (!StringUtils.hasText(userAccount)) {
            return false;
        }
        User entity = userMapper.getByActive(User.builder().userAccount(userAccount.trim()).build());
        return entity != null && !Objects.equals(entity.getId(), excludeUserId);
    }

    private void normalizeUserMutableFields(User user) {
        if (user == null) {
            return;
        }
        if (StringUtils.hasText(user.getUserAccount())) {
            user.setUserAccount(user.getUserAccount().trim());
        }
        if (StringUtils.hasText(user.getUserName())) {
            user.setUserName(user.getUserName().trim());
        }
        if (user.getUserEmail() != null) {
            user.setUserEmail(normalizeOptionalText(user.getUserEmail()));
        }
        if (user.getUserAvatar() != null) {
            user.setUserAvatar(normalizeOptionalText(user.getUserAvatar()));
        }
    }

    private String normalizeOptionalText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
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
