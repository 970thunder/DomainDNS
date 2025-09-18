package com.domaindns.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDomainMapper {
    int insert(@Param("userId") Long userId, @Param("zoneId") Long zoneId, @Param("dnsRecordId") Long dnsRecordId,
            @Param("prefix") String prefix, @Param("fullDomain") String fullDomain, @Param("remark") String remark);

    int countByUserAndDomain(@Param("userId") Long userId, @Param("fullDomain") String fullDomain);

    java.util.List<com.domaindns.user.model.UserDomain> listByUser(@Param("userId") Long userId,
            @Param("offset") Integer offset, @Param("size") Integer size);

    int countByUser(@Param("userId") Long userId);

    com.domaindns.user.model.UserDomain findByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);

    int updateDnsRecordId(@Param("id") Long id, @Param("dnsRecordId") Long dnsRecordId);

    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
}
