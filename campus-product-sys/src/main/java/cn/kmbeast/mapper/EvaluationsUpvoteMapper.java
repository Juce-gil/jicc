package cn.kmbeast.mapper;

import cn.kmbeast.pojo.entity.EvaluationsUpvote;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论点赞持久化接口
 */
public interface EvaluationsUpvoteMapper {

    void save(EvaluationsUpvote evaluationsUpvote);

    void delete(EvaluationsUpvote evaluationsUpvote);

    void batchDeleteByEvaluationsIds(@Param(value = "evaluationsIds") List<Integer> evaluationsIds);

    void batchDeleteByUserId(@Param(value = "userId") Integer userId);

    int queryCount(EvaluationsUpvote evaluationsUpvote);

}
