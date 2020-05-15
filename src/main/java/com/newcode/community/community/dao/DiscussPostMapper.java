package com.newcode.community.community.dao;

import com.newcode.community.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,@Param("offset") int offset,
                                         @Param("limit")int limit,@Param("orderMode") int orderMode);

    //当只有一个参数，且这个参数会被用到<if>里的时候，必须使用@Param来定义别名
    int selectDiscussPostRows(@Param("userId") int userId);

    int insertDiscussPost(DiscussPost discussPost);

    DiscussPost selectDiscussPostById(int id);

    int updateCommentCount(@Param("id") int id,@Param("commentCount") int commentCount);

    //更新置顶
    int updateType(@Param("id") int id,@Param("type") int type);

    //更新状态，是加精还是删除
    int updateStatus(@Param("id") int id,@Param("status") int status);

    int updateScore(@Param("id") int id,@Param("score") double score);

}
