# 泽优城迹 CityTrace

泽优城迹是一个城市生活发现与地点分享平台。用户可以发现附近地点、发布城市动态、关注其他用户、参与互动，并领取地点提供的普通优惠券和限时秒杀券。

## 核心能力

- 手机验证码登录与 Redis token 会话
- 地点分类、详情缓存和 Redis GEO 附近搜索
- 城市动态发布、点赞排行与关注 Feed
- 用户关注、取关和共同关注
- Redis Bitmap 签到统计
- 优惠券查询与 Caffeine、Redis、MySQL 多级缓存
- Redis Lua 秒杀资格校验、Redisson 分布式锁与 Kafka 异步订单消息
- Redis Lua 滑动窗口接口限流

## 技术栈

Java 8、Spring Boot、Spring MVC、MyBatis-Plus、MySQL、Redis、Lettuce、Redisson、Caffeine、Kafka、Lua、Maven。

## 本地运行

1. 创建名为 `citytrace` 的 MySQL 数据库，并执行 `src/main/resources/db/citytrace.sql`。
2. 将 `config/application-local.example.yaml` 复制为 `config/application-local.yaml`，填写本机 MySQL、Redis 口令；该本地配置已被 Git 忽略，不会上传。其他连接参数可在 `src/main/resources/application.yaml` 中调整。
3. 执行 `mvn spring-boot:run`，服务默认监听 `8081` 端口。
4. 进入 `nginx-1.18.0` 目录执行 `.\nginx.exe`，然后访问 `http://localhost:8080`。

前端静态资源位于 `nginx-1.18.0/html/citytrace`。动态图片会由后端保存到其中的 `imgs/blogs` 目录，并由 Nginx 直接提供访问。

## 代码结构

```text
src/main/java/com/citytrace/
├─ controller/   REST API
├─ service/      业务接口与实现
├─ mapper/       MyBatis-Plus 数据访问层
├─ entity/       领域实体
├─ config/       Web、MyBatis、Redis 与 Kafka 配置
├─ consumer/     异步消息消费者
├─ limiter/      AOP + Redis Lua 限流
└─ utils/        缓存、登录状态、分布式 ID 等通用能力

nginx-1.18.0/
├─ conf/          Nginx 配置与后端反向代理
└─ html/citytrace 前端页面、脚本、样式与图片资源
```

> 当前 Kafka 秒杀消费者监听注解处于注释状态，启用异步订单链路前应先统一消息序列化方式并完成端到端验证。
