# 靶点 4：短信轰炸

## 一、漏洞原理
短信发送接口对同一手机号无频率限制、无日上限。

## 二、复现步骤（vulnerable）
1. `mvn spring-boot:run -Dspring-boot.run.profiles=vulnerable`
2. `python attack/vuln04_sms_bomb.py`
3. 100 条全部下发

## 三、加固方案
- `HardenedSmsService`：手机号维度 60s/1 次、小时 5 条、日 10 条；IP 维度小时 20 条
- 手机号正则 `^1[3-9]\d{9}$`

## 四、验证加固
- `mvn test -Dtest=SmsBombTest`

## 五、通过标准
- [ ] vulnerable：100 条全发
- [ ] hardened：同号 60s 只放 1 条

## 六、进阶选做
- Bucket 换 Redis
- 虚商号段黑名单 / 空号检测
