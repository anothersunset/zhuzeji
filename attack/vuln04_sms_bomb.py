"""靶点 4：短信轰炸。只能针对你自己所有且负责的号码。"""
import asyncio, httpx, time

TARGET = "13800138000"  # 你自己控制的轰炸测试号
N = 100

async def bomb(c, _):
    r = await c.post("/sms/send", json={"phone": TARGET})
    return r.status_code

async def main():
    async with httpx.AsyncClient(base_url="http://localhost:8080") as c:
        t0 = time.time()
        results = await asyncio.gather(*[bomb(c, i) for i in range(N)])
        dt = time.time() - t0
    ok = sum(1 for s in results if s == 200)
    print(f"\u7528\u65f6 {dt:.2f}s\uff0c{ok}/{N} \u6761\u77ed\u4fe1\u53d1\u9001\u6210\u529f")

asyncio.run(main())
