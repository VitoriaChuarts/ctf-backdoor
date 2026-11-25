import os
import sqlite3
from http.server import BaseHTTPRequestHandler, HTTPServer
from urllib.parse import parse_qs

FLAG = os.environ.get("FLAG", "CTF{flag_nao_configurada}")

db = sqlite3.connect(":memory:", check_same_thread=False)
db.execute("CREATE TABLE users (username TEXT, password TEXT)")
db.execute("INSERT INTO users VALUES ('admin', ?)", (os.urandom(16).hex(),))

PAGE = """<!doctype html>
<html lang="pt-br"><head><meta charset="utf-8"><title>Cofre do Admin</title>
<style>
body{font-family:sans-serif;background:#12121a;color:#eee;display:grid;place-items:center;height:100vh;margin:0}
form{display:grid;gap:10px;width:280px}
input,button{padding:10px;border-radius:6px;border:0}
button{background:#ff4fa3;color:#fff;font-weight:bold;cursor:pointer}
</style></head>
<body><form method="post"><h2>Cofre do Admin</h2>
<input name="username" placeholder="usuario">
<input name="password" type="password" placeholder="senha">
<button>Entrar</button><p>{{MSG}}</p></form></body></html>"""


class Handler(BaseHTTPRequestHandler):

    def _send(self, status, msg):
        body = PAGE.replace("{{MSG}}", msg).encode()
        self.send_response(status)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.end_headers()
        self.wfile.write(body)

    def do_GET(self):
        self._send(200, "")

    def do_POST(self):
        size = min(int(self.headers.get("Content-Length", 0)), 1024)
        data = parse_qs(self.rfile.read(size).decode(errors="ignore"))
        user = data.get("username", [""])[0]
        pwd = data.get("password", [""])[0]

        # VULNERAVEL DE PROPOSITO: o texto digitado entra direto na consulta SQL
        query = f"SELECT username FROM users WHERE username = '{user}' AND password = '{pwd}'"
        try:
            row = db.execute(query).fetchone()
        except sqlite3.Error:
            row = None

        if row:
            self._send(200, f"Bem-vindo, {row[0]}! Sua flag: {FLAG}")
        else:
            self._send(401, "Usuario ou senha incorretos")

    def log_message(self, *args):
        pass


HTTPServer(("0.0.0.0", 8000), Handler).serve_forever()
