"""
GameHub FastAPI Local Server Launcher
"""
import uvicorn

if __name__ == "__main__":
    print("==================================================")
    print("  Starting GameHub FastAPI Backend on port 8000")
    print("  Documentation available at: http://127.0.0.1:8000/docs")
    print("  Android Emulator alias:     http://10.0.2.2:8000")
    print("==================================================")
    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=True)
