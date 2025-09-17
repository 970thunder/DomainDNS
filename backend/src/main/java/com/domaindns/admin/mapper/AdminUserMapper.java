package com.domaindns.admin.mapper;

import com.domaindns.admin.model.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminUserMapper {
    List<AdminUser> list(@Param("status") Integer status, @Param("role") String role, @Param("offset") Integer offset,
            @Param("size") Integer size);

    int count(@Param("status") Integer status, @Param("role") String role);

    int adjustPoints(@Param("id") Long id, @Param("delta") Integer delta);
}
