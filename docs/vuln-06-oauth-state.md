# 靶点 6：OAuth state 缺失

## 一、漏洞原理
OAuth 回调不校 state，可被 CSRF 绑号接管。

## 二、复现步骤（vulnerable）
1. `mvn spring-boot:run -Dspring-boot.run.profiles=vulnerable`
2. `python attack/vuln06_oauth_csrf.py`
3. 期望 bound=true

## 三、加固方案
- `HardenedOAuthController`：`SecureRandom` 生成 state + nonce + PKCE verifier
- 回调时 `MessageDigest.isEqual` 恒时间比较 state
- state/verifier/nonce 均仅一次使用后从 session 移除

## 四、验证加固
- `mvn test -Dtest=OAuthStateTest`

## 五、通过标准
- [ ] vulnerable：伪造 callback 成功
- [ ] hardened：缺失/不匹配 state 都 400

## 六、进阶选做
- 对照 Spring Authorization Server 官方 OIDC 实现
- 三方账号重置本地密码加二重验证
