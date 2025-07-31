# Customer Satisfaction Survey Dashboard

A comprehensive analytics dashboard for customer satisfaction survey data with Excel import capabilities, real-time visualizations, and AI-powered insights.

## 🎯 Overview

This dashboard analyzes customer satisfaction survey data across multiple attributes including:
- **Food Quality** (1-5 rating scale)
- **Service Quality** (1-5 rating scale) 
- **Hygiene** (1-5 rating scale)
- **Value for Money** (1-5 rating scale)
- **Overall Satisfaction** (Highly Satisfied, Satisfied, Neutral, Dissatisfied, Highly Dissatisfied)
- **Comments** (Optional feedback)
- **NPS Score** (0-10 Net Promoter Score)

## 📊 Dashboard Features

### Key Metrics
- **Overall Satisfaction Score** - Average satisfaction rating
- **Total Responses** - Number of survey participants
- **Satisfaction Rate** - Percentage of satisfied customers
- **Net Promoter Score** - Customer loyalty metric

### Interactive Visualizations
- **Satisfaction Distribution** - Pie chart showing satisfaction levels
- **Attribute Ratings** - Bar chart comparing different service aspects
- **Satisfaction Trends** - Line chart showing performance over time
- **Performance Radar** - Comprehensive attribute comparison

### Advanced Analytics
- **Performance Table** - Detailed attribute analysis with priority indicators
- **Recent Feedback** - Latest customer comments with satisfaction ratings
- **AI-Powered Insights** - Automated analysis and recommendations

### Data Management
- **Excel Import** - Upload .xlsx/.xls files with survey data
- **Real-time Filtering** - Filter by date range and satisfaction levels
- **Data Validation** - Automatic data cleaning and validation

## 🚀 Quick Start

### Option 1: Frontend Demo (Immediate)
1. Open `CustomerDashboard/frontend/survey-dashboard.html` in your browser
2. The dashboard loads with 150+ sample survey responses
3. Explore all features with realistic data

### Option 2: Full Application Setup

#### Prerequisites
- Java 11+ and Maven (for backend)
- Modern web browser
- Optional: Python or Node.js (for local server)

#### Setup Steps

1. **Navigate to project directory:**
   ```bash
   cd CustomerDashboard
   ```

2. **Option A: Simple HTTP Server**
   ```bash
   # Using Python
   cd frontend
   python -m http.server 8000
   
   # Using Node.js
   cd frontend
   npx http-server -p 8000
   
   # Then open: http://localhost:8000/survey-dashboard.html
   ```

3. **Option B: Full Backend Setup**
   ```bash
   # Install Maven dependencies and run
   cd lib
   mvn clean compile
   mvn jetty:run
   
   # Open: http://localhost:8080/customer-dashboard/survey-dashboard.html
   ```

## 📋 Excel File Format

The dashboard accepts Excel files (.xlsx/.xls) with the following columns:

| Column Name | Type | Description | Example |
|-------------|------|-------------|---------|
| Date/Timestamp | Date | Survey completion date | 2024-01-15 |
| Customer ID | Text | Unique customer identifier | CUST001 |
| Food Quality | Number | Rating 1-5 | 4 |
| Service Quality | Number | Rating 1-5 | 5 |
| Hygiene | Number | Rating 1-5 | 4 |
| Value for Money | Number | Rating 1-5 | 3 |
| Overall Satisfaction | Text | Satisfaction level | Satisfied |
| Comments | Text | Customer feedback | "Great service!" |
| NPS Score | Number | Score 0-10 | 8 |

### Sample Data
A sample CSV file with 100 survey responses is provided at:
`CustomerDashboard/data/sample_survey_data.csv`

You can open this in Excel and save as .xlsx to test the upload functionality.

## 🎨 Dashboard Sections

### 1. Key Metrics Cards
- **Overall Satisfaction**: Weighted average of all responses
- **Total Responses**: Complete survey count
- **Satisfaction Rate**: Percentage of satisfied/highly satisfied customers
- **Net Promoter Score**: Industry-standard loyalty metric

### 2. Visual Analytics
- **Satisfaction Pie Chart**: Distribution of satisfaction levels
- **Attribute Bar Chart**: Average ratings by category
- **Trends Line Chart**: Performance over time
- **Radar Chart**: Multi-dimensional attribute view

### 3. Performance Analysis
- **Filterable Data Table**: Sortable performance metrics
- **Priority Indicators**: High/Medium/Low improvement priorities
- **Trend Arrows**: Performance direction indicators

### 4. Customer Feedback
- **Recent Comments**: Latest customer feedback with ratings
- **Sentiment Indicators**: Color-coded satisfaction levels
- **Chronological Display**: Most recent feedback first

### 5. AI Insights
- **Top Performing Areas**: Strengths to maintain
- **Improvement Areas**: Focus areas for enhancement
- **Actionable Recommendations**: Specific improvement suggestions

## 🔧 Customization

### Adding New Attributes
1. Update Excel column mapping in `ExcelProcessor.java`
2. Add new fields to `SurveyResponse.java`
3. Update database schema in `SurveyDatabaseManager.java`
4. Modify frontend charts in `survey-dashboard.js`

### Custom Satisfaction Levels
Edit the satisfaction levels in:
- Frontend: `survey-dashboard.js` (satisfactionLevels array)
- Backend: `SurveyResponse.java` (validateOverallSatisfaction method)

### Styling Customization
Modify `survey-styles.css` to change:
- Color schemes
- Chart layouts
- Responsive breakpoints
- Animation effects

## 📊 Analytics Features

### Real-time Filtering
- **Date Range**: All Time, Last 30/90 Days, Last Year
- **Satisfaction Level**: Filter by specific satisfaction ratings
- **Dynamic Updates**: Charts and metrics update automatically

### Performance Prioritization
The system automatically calculates priority levels:
- **High Priority**: Average rating < 3.5 (Red indicator)
- **Medium Priority**: Average rating 3.5-4.0 (Orange indicator)  
- **Low Priority**: Average rating > 4.0 (Green indicator)

### Trend Analysis
- **Monthly Aggregation**: Data grouped by month for trend analysis
- **Moving Averages**: Smooth trend lines for better insights
- **Comparative Analysis**: Current vs. historical performance

## 🤖 AI-Powered Insights

The dashboard generates intelligent insights including:

### Automated Analysis
- **Performance Identification**: Automatically identifies top and weak areas
- **Trend Detection**: Recognizes improving or declining metrics
- **Correlation Analysis**: Links between different attributes

### Recommendations Engine
- **Specific Actions**: Tailored improvement suggestions
- **Priority Ranking**: Most impactful recommendations first
- **Implementation Guidance**: Practical steps for improvement

## 🛠️ Technical Architecture

### Frontend Stack
- **HTML5**: Semantic markup and structure
- **CSS3**: Modern styling with gradients and animations
- **JavaScript ES6+**: Interactive functionality and data processing
- **Chart.js**: Professional data visualizations
- **SheetJS**: Excel file processing

### Backend Stack (Optional)
- **Java Servlets**: RESTful API endpoints
- **H2 Database**: Embedded database for data storage
- **Apache POI**: Excel file processing
- **Gson**: JSON data processing
- **Maven**: Dependency management

### Key Libraries
- **Chart.js 3.x**: Interactive charts and graphs
- **XLSX.js**: Client-side Excel file reading
- **Apache POI**: Server-side Excel processing
- **H2 Database**: Lightweight embedded database

## 📱 Responsive Design

The dashboard is fully responsive and works on:
- **Desktop**: Full feature set with large visualizations
- **Tablet**: Optimized layout with touch-friendly controls
- **Mobile**: Streamlined interface with stacked components

### Browser Compatibility
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## 🔒 Data Security

### Client-Side Processing
- Excel files are processed locally in the browser
- No data is transmitted to external servers
- Complete privacy and security

### Data Validation
- Automatic data type validation
- Range checking for ratings (1-5, 0-10)
- Duplicate detection and handling
- Error reporting and correction guidance

## 🚀 Performance Optimization

### Efficient Data Processing
- **Lazy Loading**: Charts rendered only when visible
- **Data Caching**: Processed data cached for quick access
- **Optimized Queries**: Database queries optimized for performance

### Responsive Loading
- **Progressive Enhancement**: Core functionality loads first
- **Asynchronous Processing**: Non-blocking file processing
- **Memory Management**: Efficient data structure usage

## 📈 Advanced Features

### Export Capabilities
- **Chart Export**: Save visualizations as images
- **Data Export**: Download filtered data as CSV
- **Report Generation**: Comprehensive PDF reports

### Integration Options
- **API Endpoints**: RESTful APIs for external integration
- **Webhook Support**: Real-time data synchronization
- **Database Connectivity**: Multiple database backend support

## 🔧 Troubleshooting

### Common Issues

1. **Excel Upload Fails**
   - Check file format (.xlsx or .xls)
   - Verify column headers match expected format
   - Ensure data is in correct ranges (1-5 for ratings)

2. **Charts Not Displaying**
   - Check browser console for JavaScript errors
   - Verify Chart.js library is loaded
   - Ensure data is properly formatted

3. **Performance Issues**
   - Reduce data set size (< 10,000 records)
   - Clear browser cache
   - Use modern browser version

### Debug Mode
Enable debug logging by adding to browser console:
```javascript
window.surveyDashboard.debugMode = true;
```

## 🤝 Contributing

To extend the dashboard:

1. **Frontend Enhancements**: Modify JavaScript and CSS files
2. **Backend Extensions**: Add new servlet endpoints
3. **Database Changes**: Update schema in DatabaseManager
4. **New Analytics**: Add custom insight generation

## 📄 License

This project is provided for educational and demonstration purposes.

## 🆘 Support

For questions or issues:
1. Check this documentation
2. Review browser console for errors
3. Verify sample data format
4. Test with provided sample CSV file

---

**Happy Analyzing! 📊✨**

Transform your customer feedback into actionable insights with this comprehensive survey analytics dashboard. 