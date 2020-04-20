package com.newcode.community.community.dao;

import com.newcode.community.community.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMapper {

    User selectById(int id);
    User selectByEmail(String email);
    User selectByName(String username);
    int insertUser(User user);
    int updateStatus( @Param("id") int id,@Param("status") int status);
    int updateHeader(@Param("id") int id,@Param("headerUrl") String headerUrl);
    int updatePassword(@Param("id") int id,@Param("password") String password);
}
