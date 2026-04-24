start "FastAPI Backend" cmd /k "call .venv\Scripts\activate && uvicorn main:app --reload --port 8000"
start "Localtunnel" cmd /k "lt --port 8000 --subdomain evoluthion-notes"
