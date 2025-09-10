package com.domaindns.auth.mapper;

import com.domaindns.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);

    User findByEmail(@Param("email") String email);

    int insert(User user);

    int countByRole(@Param("role") String role);

    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);
}
