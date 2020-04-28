package com.newcode.community.community.dao;

import com.newcode.community.community.entity.Comment;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentMapper {

    List<Comment> selectCommentsByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId,
                                         @Param("offset") int offset, @Param("limit") int limit);
    int selectCountByEntity(@Param("entityType") int entityType,@Param("entityId") int entityId);

    int insertComment(Comment comment);
}
