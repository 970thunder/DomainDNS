package com.domaindns.admin.mapper;

import com.domaindns.admin.model.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AuditMapper {
    List<AuditLog> list(@Param("action") String action, @Param("offset") Integer offset, @Param("size") Integer size);

    int count(@Param("action") String action);
}
