package cn.kmbeast.service.impl;

import cn.kmbeast.aop.Protector;
import cn.kmbeast.context.LocalThreadHolder;
import cn.kmbeast.mapper.ContentMapper;
import cn.kmbeast.pojo.api.ApiResult;
import cn.kmbeast.pojo.api.Result;
import cn.kmbeast.pojo.dto.query.extend.ContentQueryDto;
import cn.kmbeast.pojo.entity.Content;
import cn.kmbeast.pojo.vo.ContentVO;
import cn.kmbeast.service.ContentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Content service implementation.
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Resource
    private ContentMapper contentMapper;

    @Override
    public Result<String> save(Content content) {
        if (content == null) {
            return ApiResult.error("content payload cannot be empty");
        }
        if (!StringUtils.hasText(content.getName())) {
            return ApiResult.error("title cannot be empty");
        }
        if (!StringUtils.hasText(content.getCover())) {
            return ApiResult.error("cover cannot be empty");
        }
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("login expired, please login again");
        }
        content.setUserId(currentUserId);
        content.setCreateTime(LocalDateTime.now());
        contentMapper.save(content);
        return ApiResult.success("content created successfully");
    }

    @Override
    public Result<String> update(Content content) {
        if (content == null || content.getId() == null) {
            return ApiResult.error("content id cannot be empty");
        }
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("login expired, please login again");
        }
        if (!isAdmin(LocalThreadHolder.getRoleId()) && !isContentOwner(content.getId(), currentUserId)) {
            return ApiResult.error("you can only update your own content");
        }
        if (!StringUtils.hasText(content.getName())) {
            return ApiResult.error("title cannot be empty");
        }
        if (!StringUtils.hasText(content.getCover())) {
            return ApiResult.error("cover cannot be empty");
        }
        contentMapper.update(content);
        return ApiResult.success("content updated successfully");
    }

    @Override
    public Result<String> batchDelete(List<Integer> ids) {
        Integer currentUserId = LocalThreadHolder.getUserId();
        if (currentUserId == null) {
            return ApiResult.error("login expired, please login again");
        }
        List<Integer> deleteIds = sanitizeIds(ids);
        if (deleteIds.isEmpty()) {
            return ApiResult.success("no content needs to be deleted");
        }
        if (!isAdmin(LocalThreadHolder.getRoleId())) {
            deleteIds = filterOwnedContentIds(deleteIds, currentUserId);
            if (deleteIds.isEmpty()) {
                return ApiResult.error("you can only delete your own content");
            }
        }
        contentMapper.batchDelete(deleteIds);
        return ApiResult.success("content deleted successfully");
    }

    @Override
    public Result<List<ContentVO>> query(ContentQueryDto contentQueryDto) {
        ContentQueryDto safeQuery = contentQueryDto == null ? new ContentQueryDto() : contentQueryDto;
        int totalCount = contentMapper.queryCount(safeQuery);
        List<ContentVO> contentVOS = contentMapper.query(safeQuery);
        return ApiResult.success(contentVOS, totalCount);
    }

    private boolean isContentOwner(Integer contentId, Integer userId) {
        if (contentId == null || userId == null) {
            return false;
        }
        ContentQueryDto queryDto = new ContentQueryDto();
        queryDto.setId(contentId);
        queryDto.setUserId(userId);
        List<ContentVO> list = contentMapper.query(queryDto);
        return list != null && !list.isEmpty();
    }

    private List<Integer> filterOwnedContentIds(List<Integer> ids, Integer userId) {
        List<Integer> result = new ArrayList<>();
        for (Integer id : ids) {
            if (isContentOwner(id, userId)) {
                result.add(id);
            }
        }
        return result;
    }

    private List<Integer> sanitizeIds(List<Integer> ids) {
        Set<Integer> set = new LinkedHashSet<>();
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        for (Integer id : ids) {
            if (id != null) {
                set.add(id);
            }
        }
        return new ArrayList<>(set);
    }

    private boolean isAdmin(Integer roleId) {
        return Objects.equals(roleId, Protector.ROLE_ADMIN);
    }
}
