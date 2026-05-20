# 靶点 7：密码重置 Token 不安全

## 一、漏洞原理
Token 用 Base36(递增) 生成可猜、不过期、可重用。

## 二、复现步骤（vulnerable）
1. `mvn spring-boot:run -Dspring-boot.run.profiles=vulnerable`
2. 预先在 H2 里给 victim 插入 email 为 `victim@test.io`
3. `python attack/vuln07_reset_predict.py`
4. 期望能猜出下一个 token

## 三、加固方案
- `HardenedResetService`：SecureRandom 32 字节 + SHA-256 存哈希
- 15 分钟 TTL + AtomicBoolean 单次使用
- 邮箱枚举防护：无论是否存在都返同一文案

## 四、验证加固
- `mvn test -Dtest=ResetTokenTest`

## 五、通过标准
- [ ] vulnerable：可预测
- [ ] hardened：不可预测、不可重用、会过期

## 六、进阶选做
- 重置后作废该用户所有会话（jti 黑名单）
- 同 IP / 同邮箱 / 全局重置频率限制
