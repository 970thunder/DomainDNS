package com.domaindns.auth.mapper;

import com.domaindns.auth.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);

    User findByEmail(@Param("email") String email);

    User findById(@Param("id") Long id);

    int insert(User user);

    int countByRole(@Param("role") String role);

    int updatePassword(@Param("id") Long id, @Param("passwordHash") String passwordHash);

    int updateInviteCode(@Param("id") Long id, @Param("inviteCode") String inviteCode);

    int updateInviterId(@Param("id") Long id, @Param("inviterId") Long inviterId);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updatePoints(@Param("id") Long id, @Param("points") Integer points);
}
