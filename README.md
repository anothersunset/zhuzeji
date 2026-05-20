# zhuzeji — Spring Boot 注册场景安全靶场

> **仅限本机学习使用。本项目所有攻击脚本只能针对 `localhost` 上启动的本身实例，不得用于任何第三方服务。**

一个可“一键切换 vulnerable / hardened”的 Spring Boot 认证模块，覆盖 8 类常见注册/登录漏洞与加固方案，配套 Python 攻击脚本与 JUnit 测试。

## 技术栈

- **Spring Boot 3.3.x** / Java 17
- **JJWT 0.12.6** （JWT 发发与验证，显式锁定 alg）
- **Bucket4j 8.10** （限流）
- **H2 内存数据库**（零外部依赖，启动即用）
- **RestAssured** + **JUnit 5**（攻击测试断言）
- **httpx** （Python 攻击脚本）

## 快速开始

```bash
# 1. 克隆 & 进入项目
git clone https://github.com/anothersunset/zhuzeji.git
cd zhuzeji

# 2. 启动易受攻击版
mvn spring-boot:run -Dspring-boot.run.profiles=vulnerable

# 3. 另开一个终端，跑第一个攻击脚本
cd attack
pip install -r requirements.txt
python vuln01_enum.py

# 4. 切到加固版重跑同一脚本验证防御
mvn spring-boot:run -Dspring-boot.run.profiles=hardened
python vuln01_enum.py
```

## 8 大靶点

| # | 漏洞 | 攻击脚本 | 加固方案 | 报告 |
|---|---|---|---|---|
| 1 | 用户名枚举 | `attack/vuln01_enum.py` | 统一错误文案 + 假哈希恒时间 | [docs/vuln-01-username-enum.md](docs/vuln-01-username-enum.md) |
| 2 | 批量注册无防护 | `attack/vuln02_mass_register.py` | Bucket4j IP 维度限频 + 验证码 | [docs/vuln-02-mass-register.md](docs/vuln-02-mass-register.md) |
| 3 | 验证码可绕过 | `attack/vuln03_captcha_bypass.py` | 后端一次性 + 绑定 session + TTL | [docs/vuln-03-captcha-bypass.md](docs/vuln-03-captcha-bypass.md) |
| 4 | 短信轰炸 | `attack/vuln04_sms_bomb.py` | 手机号/IP 多层滑动窗口限流 | [docs/vuln-04-sms-bomb.md](docs/vuln-04-sms-bomb.md) |
| 5 | JWT 弱密钥/alg=none | `attack/vuln05_jwt.py` | 随机密钥 + 显式锁 HS256 + 完整校验链 | [docs/vuln-05-jwt.md](docs/vuln-05-jwt.md) |
| 6 | OAuth state 缺失 | `attack/vuln06_oauth_csrf.py` | state + nonce + PKCE | [docs/vuln-06-oauth-state.md](docs/vuln-06-oauth-state.md) |
| 7 | 重置 Token 不安全 | `attack/vuln07_reset_predict.py` | SecureRandom 32B + 存哈希 + 单次使用 | [docs/vuln-07-reset-token.md](docs/vuln-07-reset-token.md) |
| 8 | 登录接口时序攻击 | `attack/vuln08_timing.py` | 假哈希兑底恒时间比对 | [docs/vuln-08-timing.md](docs/vuln-08-timing.md) |

## 项目结构

```
zhuzeji/
├── README.md
├── pom.xml
├── src/main/java/com/example/seclab/
│   ├── SecLabApplication.java
│   ├── common/         # User、Repository、JwtUtil、SecurityConfig 等共享骨架
│   ├── auth/           # 靶点 1 用户名枚举 + 靶点 2 批量注册
│   ├── ratelimit/      # Bucket4j 限流滤器
│   ├── captcha/        # 靶点 3 验证码绕过
│   ├── sms/            # 靶点 4 短信轰炸
│   ├── jwt/            # 靶点 5 JWT 弱配置
│   ├── oauth/          # 靶点 6 OAuth state
│   ├── reset/          # 靶点 7 重置 Token
│   └── timing/         # 靶点 8 时序攻击
├── src/main/resources/
│   ├── application.yml
│   ├── application-vulnerable.yml
│   └── application-hardened.yml
├── src/test/java/com/example/seclab/attack/  # 8 个攻击测试
├── attack/                                     # 8 个 Python 攻击脚本
└── docs/                                       # 8 篇漏洞分析报告模板
```

## 推荐学习路径

1. 先跑通 `mvn clean compile` 确认环境没问题
2. 靶点 1、2、5 〔认证三件套〕 → 完成后你的 JWT/限流基础可以在 GraphRAG 项目里复用
3. 靶点 3、4 〔验证码与频控〕 → 作为限流进阶
4. 靶点 6、7 〔OAuth 与重置流程〕 → 联邦身份场景
5. 靶点 8 〔时序攻击〕 → 高阶微观安全

每个靶点流程：

1. 在 vulnerable profile 下跑攻击脚本 → 应当成功
2. 切到 hardened profile 重跑 → 应当失败
3. 在 `docs/vuln-0X-*.md` 填上你的观察笔记
4. 跑对应的 `*AttackTest`（`hardened` profile 应该全部通过）

## License & 免责

仅供学习研究。使用者需自行承担使用后果。严禁用于任何未经授权的系统。
