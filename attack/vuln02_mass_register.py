"""靶点 2：批量注册。
vulnerable：~200 个账号在几秒内凨入。
hardened：同 IP 每小时 5 次封顶。
"""
import asyncio, httpx, time, uuid

N = 200

async def register(client, _):
    name = f"bot_{uuid.uuid4().hex[:10]}"
    r = await client.post("/auth/register",
        json={"username": name, "password": "P@ssw0rd!"})
    return r.status_code

async def main():
    async with httpx.AsyncClient(base_url="http://localhost:8080",
                                  timeout=10) as c:
        t0 = time.time()
        results = await asyncio.gather(*[register(c, i) for i in range(N)])
        dt = time.time() - t0
    ok = sum(1 for s in results if s == 200)
    print(f"\u7528\u65f6 {dt:.2f}s \u6ce8\u518c\u6210\u529f {ok}/{N} \u4e2a\u8d26\u53f7 ({ok/dt:.1f} acc/s)")

asyncio.run(main())
