# 靶点 1：用户名枚举

## 一、漏洞原理
登录接口区分返回“用户不存在”与“密码错误”，使攻击者可以根据响应体/状态码枚举出有效用户名。

## 二、复现步骤（vulnerable）
1. 启动：`mvn spring-boot:run -Dspring-boot.run.profiles=vulnerable`
2. 跑脚本：`python attack/vuln01_enum.py`
3. 观察哪些用户名被标为 `[+]`

## 三、加固方案
- 统一错误文案 `用户名或密码错误`、统一状态码 401
- 用户不存在时仍跑一次 BCrypt（`DUMMY_HASH`），消除时序差
- 参考：`HardenedAuthController.java`

## 四、验证加固
- `mvn spring-boot:run -Dspring-boot.run.profiles=hardened`
- 重跑 `python attack/vuln01_enum.py`，期望所有响应一样
- `mvn test -Dtest=UsernameEnumerationTest` 应该通过

## 五、通过标准
- [ ] vulnerable 可枚举
- [ ] hardened 不可区分
- [ ] `UsernameEnumerationTest` 通过

## 六、进阶选做
- 加上响应时间方差检测脚本，验证恒时间路径有效
- 检查 `/auth/register` “用户名已存在”是否同样被枚举
