package com.domaindns.cf.mapper;

import com.domaindns.cf.model.Zone;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ZoneMapper {
    int upsert(Zone z);

    List<Zone> list(@Param("enabled") Integer enabled, @Param("name") String name,
            @Param("cfAccountId") Long cfAccountId);

    int setEnabled(@Param("id") Long id, @Param("enabled") Integer enabled);

    Zone findById(@Param("id") Long id);

    Zone findByCfZoneId(@Param("zoneId") String zoneId);

    Zone findByName(@Param("name") String name);

    int count(@Param("enabled") Integer enabled);
}