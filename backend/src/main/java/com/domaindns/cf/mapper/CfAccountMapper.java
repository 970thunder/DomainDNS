package com.domaindns.cf.mapper;

import com.domaindns.cf.model.CfAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CfAccountMapper {
    int insert(CfAccount a);

    int update(CfAccount a);

    int delete(@Param("id") Long id);

    CfAccount findById(@Param("id") Long id);

    List<CfAccount> findAll(@Param("enabled") Integer enabled);

    CfAccount findByEmailAndType(@Param("email") String email, @Param("apiType") String apiType);
}
