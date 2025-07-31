@echo off
echo ========================================
echo GitHub Pages Deployment Script
echo ========================================
echo.

echo 1. Initializing Git repository...
git init

echo 2. Adding files to Git...
git add .

echo 3. Making initial commit...
git commit -m "Initial commit for GitHub Pages"

echo 4. Adding remote origin...
echo Please enter your GitHub repository URL:
set /p repo_url=
git remote add origin %repo_url%

echo 5. Pushing to GitHub...
git push -u origin main

echo.
echo ========================================
echo Deployment Complete!
echo ========================================
echo.
echo Next steps:
echo 1. Go to your GitHub repository
echo 2. Settings ^> Pages
echo 3. Source: Deploy from a branch
echo 4. Branch: main, folder: /frontend
echo 5. Save
echo.
echo Your site will be available at:
echo https://yourusername.github.io/repository-name/
echo.
pause 