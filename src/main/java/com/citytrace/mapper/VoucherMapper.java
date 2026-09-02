package com.citytrace.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.citytrace.entity.Voucher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @since 2021-12-22
 */
public interface VoucherMapper extends BaseMapper<Voucher> {

    // 查询秒杀优惠券,where type = 1
    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}
