import base64
import json
import pathlib
import subprocess
import sys
import time
import urllib.parse

import requests
import websocket

ROOT = pathlib.Path(__file__).resolve().parent.parent
EDGE = r"C:\Program Files (x86)\Microsoft\Edge\Application\msedge.exe"
DEBUG_PORT = 9311


def main():
    html_path = ROOT / "DOKUMENTATION.html"
    pdf_path = ROOT / "DOKUMENTATION.pdf"
    url = "file:///" + urllib.parse.quote(str(html_path).replace("\\", "/"))

    proc = subprocess.Popen([
        EDGE,
        "--headless=new",
        "--disable-gpu",
        "--no-sandbox",
        f"--remote-debugging-port={DEBUG_PORT}",
        "--remote-allow-origins=*",
        "--user-data-dir=" + str(ROOT / "tools" / ".edge-profile"),
    ])
    try:
        for _ in range(50):
            try:
                tabs = requests.get(f"http://127.0.0.1:{DEBUG_PORT}/json").json()
                if tabs:
                    break
            except requests.exceptions.ConnectionError:
                pass
            time.sleep(0.2)
        else:
            raise RuntimeError("Edge DevTools endpoint kam nicht hoch")

        ws_url = tabs[0]["webSocketDebuggerUrl"]
        ws = websocket.create_connection(ws_url)

        def send(msg_id, method, params=None):
            ws.send(json.dumps({"id": msg_id, "method": method, "params": params or {}}))
            while True:
                resp = json.loads(ws.recv())
                if resp.get("id") == msg_id:
                    return resp

        send(1, "Page.enable")
        send(2, "Page.navigate", {"url": url})
        time.sleep(1.5)

        result = send(3, "Page.printToPDF", {
            "printBackground": True,
            "displayHeaderFooter": False,
            "preferCSSPageSize": True,
        })

        pdf_bytes = base64.b64decode(result["result"]["data"])
        pdf_path.write_bytes(pdf_bytes)
        ws.close()
        print(f"PDF geschrieben: {pdf_path} ({len(pdf_bytes)} bytes)")
    finally:
        proc.terminate()
        try:
            proc.wait(timeout=5)
        except subprocess.TimeoutExpired:
            proc.kill()


if __name__ == "__main__":
    main()
