@echo off
echo Starting local web server...
echo.
echo Your application will be available at: http://localhost:8000
echo Press Ctrl+C to stop the server
echo.

cd frontend
python -m http.server 8000 