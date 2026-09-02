package com.citytrace.service.cache;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.citytrace.config.VoucherCacheProperties;
import com.citytrace.entity.Voucher;
import com.citytrace.mapper.VoucherMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.citytrace.utils.RedisConstants.CACHE_VOUCHER_LIST_KEY;

@Slf4j
@Service
public class VoucherListCacheService {

    @Resource
    private VoucherMapper voucherMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private VoucherCacheProperties cacheProperties;

    // Caffeine 本地缓存：key 为 shopId，value 为优惠券列表
    private LoadingCache<Long, List<Voucher>> voucherListCache;

    // 项目启动后初始化 Caffeine 缓存
    @PostConstruct
    public void initCache() {
        // 本地缓存刷新、过期策略由 VoucherCacheProperties 提供
        /*
            refreshAfterWrite(x, TimeUnit.MILLISECONDS)
            含义：写入后多久触发“刷新”。
            到了这个时间后，下一次访问该 key 时，会触发重新加载最新值（注意是异步线程 refresh，当前线程会先返回旧值）。
            重点：它不是立刻删除缓存，而是让缓存“该更新了" 提高数据一致性

            expireAfterWrite(y, TimeUnit.MILLISECONDS)
            含义：写入后多久“过期失效”。
            超过这个时间，缓存项会被当作不存在，下次访问会走加载逻辑（通常同步加载新值）。
            重点：这是硬过期时间，过期后旧值不再可用。
        */
        voucherListCache = Caffeine.newBuilder()
                .maximumSize(1000)  // 缓存最大容量值
                .refreshAfterWrite(cacheProperties.getRefreshAfterWrite().toMillis(), TimeUnit.MILLISECONDS)
                .expireAfterWrite(cacheProperties.getExpireAfterWrite().toMillis(), TimeUnit.MILLISECONDS)
                .build(new CacheLoader<Long, List<Voucher>>() {
                    @Override
                    public List<Voucher> load(Long shopId) {
                        return loadFromRedisThenDb(shopId);
                    }
                });
    }


    // 通过商户ID获取商户的秒杀优惠券列表
    public List<Voucher> getVoucherByShopId(Long shopId) {
        String mode = cacheProperties.getMode();
        switch (mode.toLowerCase(Locale.ROOT)) {
            case "mysql":
                return loadFromDb(shopId);
            case "redis":
                return loadFromRedisThenDb(shopId);
            case "caffeine":
            default:
                // 直接从本地缓存中获取,若本地缓存中不存在会执行load函数,加载最新数据再返回
                return voucherListCache.get(shopId);
        }
    }


    // 本地缓存的load逻辑
    private List<Voucher> loadFromRedisThenDb(Long shopId) {
        String key = CACHE_VOUCHER_LIST_KEY + shopId;

        // 1) L2: 读Redis
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toList(json, Voucher.class);
        }

        // 2) Redis中不存在，读 MySQL
        List<Voucher> vouchers = loadFromDb(shopId);

        if(CollectionUtil.isNotEmpty(vouchers)) {
            // 3) MySQL中数据不为空才写回 Redis, 避免写入空数据
            long ttlMillis = cacheProperties.getRedisTtl().toMillis();
            stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(vouchers), ttlMillis, TimeUnit.MILLISECONDS);
        }

        return vouchers;
    }

    private List<Voucher> loadFromDb(Long shopId) {
        List<Voucher> vouchers = voucherMapper.queryVoucherOfShop(shopId);
        return vouchers == null ? Collections.emptyList() : vouchers;
    }
}
