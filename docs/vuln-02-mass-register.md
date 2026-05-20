# 靶点 2：批量注册无防护

## 一、漏洞原理
注册接口无频控、无验证码、无设备指纹，黑产可在几分钟内灰注几万小号。

## 二、复现步骤（vulnerable）
1. `mvn spring-boot:run -Dspring-boot.run.profiles=vulnerable`
2. `python attack/vuln02_mass_register.py`
3. 期望 200 个账号在 < 5 秒内全部成功

## 三、加固方案
- `RegisterRateLimitFilter`：Bucket4j 按 IP 限 5/小时
- 另需验证码（靶点 3加固后接入）
- 可补充：User-Agent + Accept-Language + 屏幕分辨率 hash 作设备指纹

## 四、验证加固
- `mvn spring-boot:run -Dspring-boot.run.profiles=hardened`
- 重跑脚本：期望成功 ≤ 5，其余 429
- `mvn test -Dtest=MassRegisterTest`

## 五、通过标准
- [ ] vulnerable：全部成功
- [ ] hardened：限流生效
- [ ] `MassRegisterTest` 通过

## 六、进阶选做
- 内存 Bucket 换为 Redis 后端 Bucket4j
- 引入风险评分模型：密码模板、用户名模式（`bot_xxx`）
