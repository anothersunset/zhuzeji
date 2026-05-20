# 靶点 8：登录接口时序攻击

## 一、漏洞原理
返回体一致但处理路径代价不一致（不存在用户快返，存在用户跑 BCrypt）。

## 二、复现步骤（vulnerable）
1. `mvn spring-boot:run -Dspring-boot.run.profiles=vulnerable`
2. 在 H2 插入 alice 用户
3. `python attack/vuln08_timing.py`
4. 观察存在/不存在的中位数相差一个数量级

## 三、加固方案
- `TimingHardenedLogin`：`DUMMY_HASH` 兑底，无论是否存在都跑一次 BCrypt

## 四、验证加固
- `mvn test -Dtest=TimingAttackTest`

## 五、通过标准
- [ ] vulnerable：数量级差距
- [ ] hardened：中位数比 < 1.5

## 六、进阶选做
- 响应中加随机 sleep 增加噪声
- 考虑其他可被时序的点：邮箱查重、邀请码检查
- 论文：Brumley & Boneh, 2003 *Remote Timing Attacks Are Practical*
