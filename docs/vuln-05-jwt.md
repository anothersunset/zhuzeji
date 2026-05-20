# 靶点 5：JWT 弱密钥 / alg=none

## 一、漏洞原理
- 密钥 `secret`（可不到 1 秒跱出）
- 不锁 alg，接受 alg=none
- 不校验 iss/aud/exp

## 二、复现步骤（vulnerable）
1. `mvn spring-boot:run -Dspring-boot.run.profiles=vulnerable`
2. `python attack/vuln05_jwt.py`
3. 两种伪造都能访问 `/api/me`

## 三、加固方案
- `HardenedJwtUtil`：Base64 密钥启动硬校验 ≥ 32 字节
- 显式 `Jwts.SIG.HS256` + `requireIssuer("seclab")` + `requireAudience("seclab-api")`
- 15 分钟过期 + jti
- `HardenedJwtAuthFilter` 逻辑

## 四、验证加固
- `mvn test -Dtest=JwtAttackTest`

## 五、通过标准
- [ ] vulnerable：两种伪造都能过
- [ ] hardened：都返 401

## 六、进阶选做
- RS256/HS256 混淆、kid SQL 注入、JWKS SSRF
- jti 黑名单主动作废 token
