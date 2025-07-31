# Deployment Guide for Customer Survey Dashboard

## Quick Start - Local Development

1. **Start Local Server:**
   ```bash
   # Windows
   start-local-server.bat
   
   # Or manually
   cd frontend
   python -m http.server 8000
   ```

2. **Access Application:**
   - Open browser and go to: `http://localhost:8000/survey-dashboard.html`

## Static Hosting (Frontend Only)

### GitHub Pages (Free)

1. **Create GitHub Repository:**
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/yourusername/survey-dashboard.git
   git push -u origin main
   ```

2. **Enable GitHub Pages:**
   - Go to repository Settings → Pages
   - Source: Deploy from a branch
   - Branch: main, folder: /frontend
   - Your site will be available at: `https://yourusername.github.io/survey-dashboard/`

### Netlify (Free)

1. **Deploy via Netlify:**
   - Go to [netlify.com](https://netlify.com)
   - Drag and drop the `frontend` folder
   - Your site will be available immediately

2. **Custom Domain (Optional):**
   - Add your custom domain in Netlify settings

### Vercel (Free)

1. **Deploy via Vercel:**
   ```bash
   npm install -g vercel
   cd frontend
   vercel
   ```

## Full Stack Hosting (Frontend + Backend)

### Heroku (Recommended)

1. **Install Heroku CLI:**
   ```bash
   # Download from: https://devcenter.heroku.com/articles/heroku-cli
   ```

2. **Login to Heroku:**
   ```bash
   heroku login
   ```

3. **Create Heroku App:**
   ```bash
   heroku create your-survey-dashboard
   ```

4. **Deploy:**
   ```bash
   git add .
   git commit -m "Deploy to Heroku"
   git push heroku main
   ```

5. **Open App:**
   ```bash
   heroku open
   ```

### Railway

1. **Connect GitHub Repository:**
   - Go to [railway.app](https://railway.app)
   - Connect your GitHub account
   - Select your repository

2. **Deploy:**
   - Railway will automatically detect Java and deploy
   - Your app will be available at the provided URL

### Render

1. **Create Account:**
   - Go to [render.com](https://render.com)
   - Sign up with GitHub

2. **New Web Service:**
   - Connect your repository
   - Build Command: `mvn clean package`
   - Start Command: `java -jar target/your-app.jar`

## Environment Variables

For production, set these environment variables:

```bash
# Database Configuration
DATABASE_URL=your_database_url
DB_USERNAME=your_username
DB_PASSWORD=your_password

# Application Settings
PORT=8080
NODE_ENV=production
```

## Database Setup

### PostgreSQL (Recommended for Production)

1. **Heroku Postgres:**
   ```bash
   heroku addons:create heroku-postgresql:hobby-dev
   ```

2. **Railway Postgres:**
   - Add PostgreSQL service in Railway dashboard
   - Connect to your web service

3. **Update Database Configuration:**
   - Update your Java application to use the production database URL

## SSL/HTTPS

Most hosting platforms provide SSL certificates automatically:
- **GitHub Pages:** HTTPS by default
- **Netlify:** HTTPS by default
- **Heroku:** HTTPS by default
- **Railway:** HTTPS by default

## Custom Domain Setup

1. **Purchase Domain** (e.g., from Namecheap, GoDaddy)
2. **Configure DNS:**
   - Add CNAME record pointing to your hosting provider
3. **Update Hosting Settings:**
   - Add custom domain in your hosting platform

## Monitoring and Analytics

1. **Google Analytics:**
   - Add tracking code to your HTML files
2. **Error Monitoring:**
   - Consider Sentry for error tracking
3. **Performance Monitoring:**
   - Use hosting platform's built-in monitoring

## Backup Strategy

1. **Database Backups:**
   - Enable automatic backups in your hosting platform
2. **Code Backups:**
   - Use Git for version control
   - Regular commits and pushes

## Troubleshooting

### Common Issues:

1. **Port Issues:**
   - Ensure your app uses `process.env.PORT` (Heroku/Railway)
   - Default to 8080 for local development

2. **Database Connection:**
   - Check environment variables
   - Verify database credentials

3. **Static Files:**
   - Ensure all CSS/JS files are in the correct location
   - Check file paths in HTML

### Support Resources:

- **Heroku:** [Dev Center](https://devcenter.heroku.com/)
- **Railway:** [Documentation](https://docs.railway.app/)
- **Netlify:** [Documentation](https://docs.netlify.com/)
- **GitHub Pages:** [Documentation](https://pages.github.com/)

## Cost Comparison

| Platform | Free Tier | Paid Plans | Best For |
|----------|-----------|------------|----------|
| GitHub Pages | ✅ | ❌ | Static sites |
| Netlify | ✅ | $19/month | Static sites, JAMstack |
| Vercel | ✅ | $20/month | React/Vue/Next.js |
| Heroku | ❌ | $7/month | Full-stack apps |
| Railway | ✅ | $5/month | Full-stack apps |
| Render | ✅ | $7/month | Full-stack apps |

## Recommendation

**For your survey dashboard:**

1. **Development:** Use local server (`start-local-server.bat`)
2. **Frontend Only:** GitHub Pages (free, easy)
3. **Full Stack:** Railway (free tier available, easy deployment)
4. **Production:** Heroku or Railway with custom domain 