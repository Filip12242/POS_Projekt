import pathlib
import markdown

ROOT = pathlib.Path(__file__).resolve().parent.parent
SRC = ROOT / "DOKUMENTATION.md"
HTML_OUT = ROOT / "DOKUMENTATION.html"

CSS = """
@page {
    margin: 22mm 18mm;
}
body {
    font-family: 'Segoe UI', Calibri, Arial, sans-serif;
    color: #24292e;
    line-height: 1.55;
    font-size: 11pt;
}
h1 {
    font-size: 22pt;
    color: #0b3d63;
    border-bottom: 3px solid #0b3d63;
    padding-bottom: 6px;
    margin-top: 0;
}
h2 {
    font-size: 15pt;
    color: #0b3d63;
    border-bottom: 1px solid #d0d7de;
    padding-bottom: 4px;
    margin-top: 28px;
}
h3 {
    font-size: 12.5pt;
    color: #1b5e8a;
    margin-top: 20px;
}
p, li {
    font-size: 10.5pt;
}
code {
    font-family: 'Consolas', 'Courier New', monospace;
    background: #f3f4f6;
    border-radius: 3px;
    padding: 1px 4px;
    font-size: 9.5pt;
}
pre {
    background: #f6f8fa;
    border: 1px solid #d0d7de;
    border-left: 4px solid #0b3d63;
    border-radius: 4px;
    padding: 10px 14px;
    overflow-x: auto;
    font-size: 9pt;
}
pre code {
    background: none;
    padding: 0;
}
ul {
    padding-left: 22px;
}
li {
    margin-bottom: 4px;
}
strong {
    color: #0b3d63;
}
hr {
    border: none;
    border-top: 1px solid #d0d7de;
    margin: 26px 0;
}
"""

HTML_TEMPLATE = """<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="utf-8">
<title>Lagerverwaltung - Dokumentation</title>
<style>{css}</style>
</head>
<body>
{body}
</body>
</html>
"""


def main():
    text = SRC.read_text(encoding="utf-8")
    body = markdown.markdown(text, extensions=["fenced_code", "tables", "sane_lists"])
    html = HTML_TEMPLATE.format(css=CSS, body=body)
    HTML_OUT.write_text(html, encoding="utf-8")
    print(f"HTML geschrieben nach {HTML_OUT}")


if __name__ == "__main__":
    main()
