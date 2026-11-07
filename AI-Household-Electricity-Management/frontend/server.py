import http.server
import socketserver
import os
import sys

PORT = 3000
FRONTEND_DIR = os.path.dirname(os.path.abspath(__file__))

class NoCacheHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=FRONTEND_DIR, **kwargs)

    def end_headers(self):
        self.send_header('Cache-Control', 'no-store, no-cache, must-revalidate, max-age=0')
        self.send_header('Pragma', 'no-cache')
        self.send_header('Expires', '0')
        self.send_header('Access-Control-Allow-Origin', '*')
        super().end_headers()

    def do_GET(self):
        clean_path = self.path.split('?')[0].rstrip('/')
        if clean_path in ('', '/index.html'):
            self.send_response(302)
            self.send_header('Location', '/pages/login.html')
            self.end_headers()
            return
        super().do_GET()

def run_server():
    socketserver.TCPServer.allow_reuse_address = True
    try:
        with socketserver.TCPServer(("", PORT), NoCacheHTTPRequestHandler) as httpd:
            print(f"===========================================================")
            print(f" AI Electricity Management Web Frontend Server running on port {PORT}")
            print(f" URL: http://localhost:{PORT}")
            print(f" Mode: Zero-Cache / Auto-Redirect to Login")
            print(f" Press Ctrl+C to stop the server")
            print(f"===========================================================")
            httpd.serve_forever()
    except OSError as e:
        print(f"Error starting server on port {PORT}: {e}")
        print(f"If port {PORT} is already in use, please close the previous terminal and retry.")
        sys.exit(1)

if __name__ == '__main__':
    run_server()
