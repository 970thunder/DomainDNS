package com.domaindns.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDomainMapper {
    int insert(@Param("userId") Long userId, @Param("zoneId") Long zoneId, @Param("dnsRecordId") Long dnsRecordId,
            @Param("prefix") String prefix, @Param("fullDomain") String fullDomain, @Param("remark") String remark);
}
