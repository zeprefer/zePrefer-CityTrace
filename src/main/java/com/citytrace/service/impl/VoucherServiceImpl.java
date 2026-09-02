package com.citytrace.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.citytrace.dto.Result;
import com.citytrace.entity.SeckillVoucher;
import com.citytrace.entity.Voucher;
import com.citytrace.mapper.VoucherMapper;
import com.citytrace.service.ISeckillVoucherService;
import com.citytrace.service.IVoucherService;
import com.citytrace.service.cache.VoucherListCacheService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.citytrace.utils.RedisConstants.SECKILL_STOCK_KEY;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @since 2021-12-22
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private VoucherListCacheService voucherListCacheService;

    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息，可按配置启用 L1 Caffeine + L2 Redis 多级缓存
        List<Voucher> vouchers = voucherListCacheService.getVoucherByShopId(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);
    }
}
