package com.domaindns.cf.mapper;

import com.domaindns.cf.model.DnsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DnsRecordMapper {
    int upsert(DnsRecord r);

    List<DnsRecord> listByZone(@Param("zoneId") Long zoneId, @Param("type") String type, @Param("name") String name);

    int countByZoneAndName(@Param("zoneId") Long zoneId, @Param("name") String name);
}
