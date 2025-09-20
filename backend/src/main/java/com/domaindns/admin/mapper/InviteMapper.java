package com.domaindns.admin.mapper;

import com.domaindns.admin.model.InviteCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface InviteMapper {
        List<InviteCode> list(@Param("keyword") String keyword, @Param("ownerUserId") Long ownerUserId,
                        @Param("offset") Integer offset,
                        @Param("size") Integer size);

        int count(@Param("keyword") String keyword, @Param("ownerUserId") Long ownerUserId);

        int insert(@Param("code") String code, @Param("ownerUserId") Long ownerUserId,
                        @Param("maxUses") Integer maxUses,
                        @Param("expiredAt") LocalDateTime expiredAt);

        com.domaindns.admin.model.InviteCode findByOwnerUserId(@Param("ownerUserId") Long ownerUserId);

        int updateByOwnerUserId(@Param("ownerUserId") Long ownerUserId, @Param("code") String code,
                        @Param("maxUses") Integer maxUses, @Param("expiredAt") LocalDateTime expiredAt);

        int deleteByOwnerUserId(@Param("ownerUserId") Long ownerUserId);

        InviteCode findByCode(@Param("code") String code);

        int incrementUsedCount(@Param("code") String code);
}
