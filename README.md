📊 Customer Satisfaction Dashboard — Rio 45
A simple yet powerful dashboard to analyze customer satisfaction data using HTML, CSS, JavaScript, and Chart.js for the frontend, with Java Servlets for the backend.

🚀 Project Overview
This project visualizes customer survey results, including metrics like food quality, service, hygiene, and overall satisfaction. It supports Excel file uploads and displays real-time charts with key metrics and trends.

✨ Features

📈 Interactive Charts: Pie, Bar, Line, and Radar charts for data visualization.
📊 Key Metrics: Net Promoter Score (NPS), satisfaction rate, and total responses.
📤 Excel Uploads: Supports .xlsx and .xls files for survey data.
📄 Customer Comments: Displays the latest customer feedback.
🧠 AI-Style Suggestions: Data-driven performance improvement tips.
📱 Responsive Design: Optimized for both mobile and desktop devices.


🛠️ Tech Stack



Layer
Technology



Frontend
HTML, CSS, JavaScript


Charts
Chart.js


Backend
Java Servlets


Excel Parser
Apache POI


Database
H2 Embedded DB


Tools
Maven, Tomcat



📁 Folder Structure
CustomerDashboard/
├── backend/          # Java servlets and processing logic
├── frontend/         # HTML, JavaScript, and CSS files
├── data/             # Sample data files
├── lib/              # Maven configuration and dependencies
└── scripts/          # Deployment helper scripts


🚀 Quick Start
🔁 Clone the Repository
git clone https://github.com/Satheshwaran26/Rio-45-Creating-Customer-Satisfaction-Dashboard.git
cd Rio-45-Creating-Customer-Satisfaction-Dashboard

🖥️ Run Frontend (No Setup Required)

Open frontend/survey-dashboard.html in your browser.
Test features using the sample data in data/sample_survey_data.csv.

⚙️ Full Setup (Frontend + Backend)
🔧 Prerequisites

Java 11 or higher
Maven
Apache Tomcat (or Jetty)
Python or Node.js (optional, for serving static files)

▶️ Run Backend
cd lib
mvn clean compile
mvn jetty:run


Access the dashboard at: http://localhost:8080/customer-dashboard/survey-dashboard.html

📂 Run Frontend (Standalone)
cd frontend
# Using Python
python -m http.server 8000

# Using Node.js
npx http-server -p 8000


Visit: http://localhost:8000/survey-dashboard.html


📊 Excel Format
The Excel file should follow this structure:



Column
Example



Food Quality
4


Service Quality
5


Hygiene
4


Value for Money
3


Overall Satisfaction
Satisfied


NPS Score
8


Comments
Great service!



Use data/sample_survey_data.csv for testing.


📋 Dashboard Summary

Metrics Cards: Shows satisfaction rate, total responses, and NPS.
Visual Charts: Interactive Pie, Bar, Line, and Radar charts.
Live Filtering: Real-time data sorting and filtering.
Excel Uploads: Upload and analyze survey data.
AI Suggestions: Performance improvement tips based on data trends.


🔧 Customization

Styles: Edit frontend/survey-styles.css for custom designs.
Fields: Add new fields in backend/SurveyResponse.java.
Logic: Extend functionality in backend/ExcelProcessor.java.
Charts: Update chart configurations in frontend/survey-dashboard.js.


🤝 Contributing

Fork the repository.
Create a new branch (git checkout -b feature/your-feature).
Commit your changes (git commit -m "Add your feature").
Push to the branch (git push origin feature/your-feature).
Open a Pull Request.


📄 License
This project is for educational and demonstration purposes only.