package cn.kmbeast.mapper;

import cn.kmbeast.pojo.dto.query.extend.EvaluationsQueryDto;
import cn.kmbeast.pojo.entity.Evaluations;
import cn.kmbeast.pojo.vo.CommentChildVO;
import cn.kmbeast.pojo.vo.CommentParentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论持久化接口
 */
public interface EvaluationsMapper {

    List<CommentParentVO> getParentComments(@Param(value = "contentId") Integer contentId,
                                            @Param(value = "userId") Integer userId,
                                            @Param(value = "contentType") String contentType);

    List<CommentChildVO> query(EvaluationsQueryDto evaluationsQueryDto);

    Integer queryCount(EvaluationsQueryDto evaluationsQueryDto);

    List<Integer> selectChildComments(@Param(value = "ids") List<Integer> ids);

    Evaluations selectById(@Param(value = "id") Integer id);

    List<Integer> selectIdsByCommenterId(@Param(value = "commenterId") Integer commenterId);

    void batchDelete(@Param(value = "ids") List<Integer> ids);

    Integer totalCount(Integer contentId, String contentType);

    void save(Evaluations evaluations);

}
