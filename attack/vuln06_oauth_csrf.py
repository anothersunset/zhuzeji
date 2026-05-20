"""靶点 6：OAuth state 缺失。
hardened 下本脚本应该拿不到 bound=true（返回 400）。
"""
import httpx

with httpx.Client(base_url="http://localhost:8080") as c:
    eve_code = "EVE_OBTAINED_CODE"
    r = c.get(f"/oauth/callback?code={eve_code}")
    print(r.status_code, r.text)
