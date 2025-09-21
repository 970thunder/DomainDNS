package com.domaindns.admin.mapper;

import com.domaindns.admin.model.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    @Insert("INSERT INTO announcements (title, content, status, priority, published_at, created_by) " +
            "VALUES (#{title}, #{content}, #{status}, #{priority}, #{publishedAt}, #{createdBy})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Announcement announcement);

    @Update("UPDATE announcements SET title=#{title}, content=#{content}, status=#{status}, " +
            "priority=#{priority}, published_at=#{publishedAt}, updated_at=CURRENT_TIMESTAMP " +
            "WHERE id=#{id}")
    int update(Announcement announcement);

    @Delete("DELETE FROM announcements WHERE id=#{id}")
    int deleteById(Long id);

    @Select("SELECT * FROM announcements WHERE id=#{id}")
    Announcement findById(Long id);

    @Select("SELECT * FROM announcements ORDER BY priority DESC, published_at DESC, created_at DESC")
    List<Announcement> findAll();

    @Select("SELECT * FROM announcements WHERE status=#{status} ORDER BY priority DESC, published_at DESC, created_at DESC")
    List<Announcement> findByStatus(String status);

    @Select("SELECT * FROM announcements WHERE status='PUBLISHED' ORDER BY priority DESC, published_at DESC LIMIT #{limit}")
    List<Announcement> findPublished(int limit);

    @Select("SELECT COUNT(*) FROM announcements WHERE status='PUBLISHED'")
    int countPublished();

    @Update("UPDATE announcements SET status=#{status}, published_at=#{publishedAt} WHERE id=#{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status,
            @Param("publishedAt") java.time.LocalDateTime publishedAt);
}
