package com.domaindns.admin.mapper;

import com.domaindns.admin.model.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    List<PaymentOrder> list(@Param("status") String status, @Param("userId") Long userId,
            @Param("offset") Integer offset, @Param("size") Integer size);

    int count(@Param("status") String status, @Param("userId") Long userId);
}
