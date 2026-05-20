# 靶点 3：验证码可绕过

## 一、漏洞原理
仅前端校验、验证码可重放、与 session/IP 无绑定。

## 二、复现步骤（vulnerable）
1. `mvn spring-boot:run -Dspring-boot.run.profiles=vulnerable`
2. `python attack/vuln03_captcha_bypass.py`
3. 同一验证码能复用 20 次；伪造 `captchaPassed=true` 可直接注册

## 三、加固方案
- `CaptchaService.verifyOnce`：单次性、绑定 session、带 TTL
- `AtomicBoolean used` 原子置位防并发重放
- 不再信任前端字段

## 四、验证加固
- `mvn test -Dtest=CaptchaBypassTest`

## 五、通过标准
- [ ] vulnerable：能复用 + 能绕过
- [ ] hardened：不能复用、不能跨 session、不能过期后用

## 六、进阶选做
- ConcurrentHashMap 换 Redis + Lua 原子脚本
- 接入 hCaptcha / Cloudflare Turnstile
