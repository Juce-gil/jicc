package cn.kmbeast.service.impl;

import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.mapper.EvaluationsMapper;
import cn.kmbeast.mapper.EvaluationsUpvoteMapper;
import cn.kmbeast.mapper.UserMapper;
import cn.kmbeast.pojo.api.ApiResult;
import cn.kmbeast.pojo.api.PageResult;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.EvaluationsQueryDto;
import cn.kmbeast.pojo.em.RoleEnum;
import cn.kmbeast.pojo.entity.Evaluations;
import cn.kmbeast.pojo.entity.EvaluationsUpvote;
import cn.kmbeast.pojo.entity.User;
import cn.kmbeast.pojo.vo.CommentChildVO;
import cn.kmbeast.pojo.vo.CommentParentVO;
import cn.kmbeast.pojo.vo.EvaluationsVO;
import cn.kmbeast.service.EvaluationsService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 评论服务实现类
 */
@Service
public class EvaluationsServiceImpl implements EvaluationsService {

    @Resource
    private EvaluationsMapper evaluationsMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private EvaluationsUpvoteMapper evaluationsUpvoteMapper;

    @Override
    public Result<Object> insert(Evaluations evaluations) {
        if (evaluations == null) {
            return ApiResult.error("请求参数不能为空");
        }
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("登录已失效，请重新登录");
        }
        if (evaluations.getContentId() == null) {
            return ApiResult.error("内容ID不能为空");
        }
        if (!StringUtils.hasText(evaluations.getContentType())) {
            return ApiResult.error("内容类型不能为空");
        }
        if (!StringUtils.hasText(evaluations.getContent())) {
            return ApiResult.error("评论内容不能为空");
        }
        User user = userMapper.getByActive(User.builder().id(currentUserId).build());
        if (user == null) {
            return ApiResult.error("当前用户不存在或已删除");
        }
        if (Boolean.TRUE.equals(user.getIsWord())) {
            return ApiResult.error("账户已被禁言");
        }
        evaluations.setCommenterId(currentUserId);
        evaluations.setContentType(evaluations.getContentType().trim());
        evaluations.setContent(evaluations.getContent().trim());
        evaluations.setCreateTime(LocalDateTime.now());
        evaluationsMapper.save(evaluations);
        return ApiResult.success("评论成功");
    }

    @Override
    public Result<Object> list(Integer contentId, String contentType) {
        if (contentId == null || !StringUtils.hasText(contentType)) {
            return ApiResult.success(new EvaluationsVO(0, new ArrayList<>()));
        }
        List<CommentParentVO> parentComments = evaluationsMapper.getParentComments(
                contentId,
                LocalThreadHolder.getUserId(),
                contentType.trim()
        );
        Integer count = evaluationsMapper.totalCount(contentId, contentType.trim());
        return ApiResult.success(new EvaluationsVO(count == null ? 0 : count,
                parentComments == null ? new ArrayList<>() : parentComments));
    }

    @Override
    public Result<Object> query(EvaluationsQueryDto evaluationsQueryDto) {
        EvaluationsQueryDto safeQueryDto = evaluationsQueryDto == null ? new EvaluationsQueryDto() : evaluationsQueryDto;
        if (StringUtils.hasText(safeQueryDto.getContentType())) {
            safeQueryDto.setContentType(safeQueryDto.getContentType().trim());
        }
        if (StringUtils.hasText(safeQueryDto.getContent())) {
            safeQueryDto.setContent(safeQueryDto.getContent().trim());
        }
        List<CommentChildVO> list = evaluationsMapper.query(safeQueryDto);
        Integer totalPage = evaluationsMapper.queryCount(safeQueryDto);
        return PageResult.success(list == null ? new ArrayList<>() : list, totalPage == null ? 0 : totalPage);
    }

    @Override
    public Result<Object> batchDelete(List<Integer> ids) {
        List<Integer> deleteIds = sanitizeIds(ids);
        if (deleteIds.isEmpty()) {
            return ApiResult.success("没有需要删除的评论");
        }
        deleteEvaluationsWithChildren(deleteIds);
        return ApiResult.success("评论删除成功");
    }

    @Override
    public Result<String> delete(Integer id) {
        if (id == null) {
            return ApiResult.error("评论ID不能为空");
        }
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("登录已失效，请重新登录");
        }
        User currentUser = userMapper.getByActive(User.builder().id(currentUserId).build());
        if (currentUser == null) {
            return ApiResult.error("当前用户不存在或已删除");
        }
        Evaluations savedEvaluations = evaluationsMapper.selectById(id);
        if (savedEvaluations == null) {
            return ApiResult.error("评论不存在或已删除");
        }
        boolean isAdmin = Objects.equals(currentUser.getUserRole(), RoleEnum.ADMIN.getRole());
        if (!isAdmin && !Objects.equals(savedEvaluations.getCommenterId(), currentUserId)) {
            return ApiResult.error("无权删除该评论");
        }
        deleteEvaluationsWithChildren(Collections.singletonList(id));
        return ApiResult.success("评论删除成功");
    }

    @Override
    public Result<Object> upvoteOperation(Evaluations evaluations) {
        if (evaluations == null || evaluations.getId() == null) {
            return ApiResult.error("评论ID不能为空");
        }
        Integer userId = LocalThreadHolder.getUserId();
        if (userId == null) {
            return ApiResult.error("登录已失效，请重新登录");
        }
        if (evaluationsMapper.selectById(evaluations.getId()) == null) {
            return ApiResult.error("评论不存在或已删除");
        }
        Integer evaluationsId = evaluations.getId();
        EvaluationsUpvote query = new EvaluationsUpvote();
        query.setUserId(userId);
        query.setEvaluationsId(evaluationsId);
        boolean hasUpvoted = evaluationsUpvoteMapper.queryCount(query) > 0;
        if (hasUpvoted) {
            evaluationsUpvoteMapper.delete(query);
        } else {
            evaluationsUpvoteMapper.save(query);
        }
        EvaluationsUpvote countQuery = new EvaluationsUpvote();
        countQuery.setEvaluationsId(evaluationsId);
        int total = evaluationsUpvoteMapper.queryCount(countQuery);
        Map<String, Object> rep = new HashMap<>();
        rep.put("count", total);
        rep.put("haveUpvote", !hasUpvoted);
        return ApiResult.success(rep);
    }

    private void deleteEvaluationsWithChildren(List<Integer> ids) {
        List<Integer> deleteIds = sanitizeIds(ids);
        if (deleteIds.isEmpty()) {
            return;
        }
        Set<Integer> allDeleteIds = new LinkedHashSet<>(deleteIds);
        List<Integer> childIds = evaluationsMapper.selectChildComments(deleteIds);
        if (childIds != null) {
            for (Integer childId : childIds) {
                if (childId != null) {
                    allDeleteIds.add(childId);
                }
            }
        }
        List<Integer> finalDeleteIds = new ArrayList<>(allDeleteIds);
        if (!finalDeleteIds.isEmpty()) {
            evaluationsUpvoteMapper.batchDeleteByEvaluationsIds(finalDeleteIds);
            evaluationsMapper.batchDelete(finalDeleteIds);
        }
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
