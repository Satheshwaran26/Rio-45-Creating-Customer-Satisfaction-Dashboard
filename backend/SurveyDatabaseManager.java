package com.customerdashboard.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * SurveyDatabaseManager extends DatabaseManager to include survey-specific tables
 */
public class SurveyDatabaseManager extends DatabaseManager {
    
    @Override
    protected void createTables() throws SQLException {
        // Create base tables from parent class
        super.createTables();
        
        // Create survey-specific tables
        createSurveyTables();
    }
    
    /**
     * Create survey-specific tables
     */
    private void createSurveyTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create survey_responses table
            String createSurveyResponsesTable = """
                CREATE TABLE IF NOT EXISTS survey_responses (
                    survey_id INT AUTO_INCREMENT PRIMARY KEY,
                    customer_id VARCHAR(50) NOT NULL,
                    survey_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    food_quality INT CHECK (food_quality >= 1 AND food_quality <= 5),
                    service_quality INT CHECK (service_quality >= 1 AND service_quality <= 5),
                    hygiene INT CHECK (hygiene >= 1 AND hygiene <= 5),
                    value_for_money INT CHECK (value_for_money >= 1 AND value_for_money <= 5),
                    overall_satisfaction VARCHAR(50) NOT NULL,
                    comments TEXT,
                    nps_score INT CHECK (nps_score >= 0 AND nps_score <= 10),
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE CASCADE
                )
            """;
            stmt.execute(createSurveyResponsesTable);
            
            // Create survey_analytics table for cached analytics
            String createSurveyAnalyticsTable = """
                CREATE TABLE IF NOT EXISTS survey_analytics (
                    analytics_id INT AUTO_INCREMENT PRIMARY KEY,
                    calculation_date DATE NOT NULL,
                    total_responses INT DEFAULT 0,
                    avg_satisfaction_score DECIMAL(3,2),
                    satisfaction_rate DECIMAL(5,2),
                    nps_score DECIMAL(5,2),
                    avg_food_quality DECIMAL(3,2),
                    avg_service_quality DECIMAL(3,2),
                    avg_hygiene DECIMAL(3,2),
                    avg_value_for_money DECIMAL(3,2),
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_date (calculation_date)
                )
            """;
            stmt.execute(createSurveyAnalyticsTable);
            
            // Create survey_insights table for AI-generated insights
            String createSurveyInsightsTable = """
                CREATE TABLE IF NOT EXISTS survey_insights (
                    insight_id INT AUTO_INCREMENT PRIMARY KEY,
                    insight_type ENUM('positive', 'negative', 'recommendation') NOT NULL,
                    insight_text TEXT NOT NULL,
                    confidence_score DECIMAL(3,2),
                    based_on_responses INT,
                    date_generated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    is_active BOOLEAN DEFAULT TRUE
                )
            """;
            stmt.execute(createSurveyInsightsTable);
            
            System.out.println("Survey tables created successfully");
        }
    }
    
    @Override
    protected void insertSampleData() throws SQLException {
        // Insert base sample data
        super.insertSampleData();
        
        // Insert survey sample data
        insertSurveySampleData();
    }
    
    /**
     * Insert sample survey data
     */
    private void insertSurveySampleData() throws SQLException {
        try (Connection conn = getConnection()) {
            
            // Check if survey data already exists
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM survey_responses");
            var rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                return; // Data already exists
            }
            
            // Insert sample survey responses
            String insertSurvey = """
                INSERT INTO survey_responses 
                (customer_id, survey_date, food_quality, service_quality, hygiene, 
                 value_for_money, overall_satisfaction, comments, nps_score) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
            PreparedStatement surveyStmt = conn.prepareStatement(insertSurvey);
            
            // Sample survey data with varied responses
            Object[][] sampleSurveys = {
                {"CUST001", "2024-01-15 10:30:00", 5, 4, 5, 4, "Highly Satisfied", "Excellent food quality and great atmosphere!", 9},
                {"CUST001", "2023-12-20 14:20:00", 4, 5, 4, 5, "Satisfied", "Very good service and reasonable prices", 8},
                {"CUST002", "2024-01-10 12:15:00", 3, 3, 4, 3, "Neutral", "Average experience overall", 6},
                {"CUST002", "2023-11-15 18:45:00", 2, 3, 2, 2, "Dissatisfied", "Food quality could be much better", 4},
                {"CUST003", "2024-01-05 19:30:00", 5, 5, 5, 4, "Highly Satisfied", "Outstanding service and delicious food!", 10},
                {"CUST003", "2023-12-01 13:10:00", 4, 4, 5, 4, "Satisfied", "Clean environment and good food", 7},
                
                // Recent surveys for trending analysis
                {"CUST001", "2024-01-20 11:00:00", 4, 4, 4, 4, "Satisfied", "Consistent quality", 7},
                {"CUST002", "2024-01-18 15:30:00", 5, 4, 5, 3, "Satisfied", "Great food but expensive", 8},
                {"CUST003", "2024-01-16 12:45:00", 3, 4, 3, 4, "Neutral", "Good service, average food", 6},
                
                // Negative feedback samples
                {"CUST001", "2023-10-15 20:00:00", 1, 2, 2, 2, "Highly Dissatisfied", "Poor hygiene standards and terrible food", 2},
                {"CUST002", "2023-09-20 17:30:00", 2, 2, 3, 2, "Dissatisfied", "Long waiting time and cold food", 3},
                
                // High satisfaction samples
                {"CUST003", "2024-01-12 14:15:00", 5, 5, 5, 5, "Highly Satisfied", "Perfect experience! Will definitely return", 10},
                {"CUST001", "2023-12-25 19:45:00", 5, 4, 5, 4, "Highly Satisfied", "Excellent Christmas dinner!", 9},
                {"CUST002", "2023-11-28 16:20:00", 4, 5, 4, 5, "Satisfied", "Great Thanksgiving service", 8},
                
                // More diverse data for analytics
                {"CUST003", "2023-11-10 13:30:00", 3, 3, 3, 3, "Neutral", "Average across all categories", 5},
                {"CUST001", "2023-10-05 18:15:00", 4, 3, 4, 4, "Satisfied", "Good food and clean place", 7},
                {"CUST002", "2023-09-15 12:00:00", 2, 3, 2, 3, "Dissatisfied", "Food was cold and not fresh", 3},
                {"CUST003", "2023-08-20 19:20:00", 4, 5, 5, 4, "Satisfied", "Excellent customer service", 8},
                {"CUST001", "2023-07-25 14:40:00", 3, 2, 3, 3, "Neutral", "Average food and slow service", 5},
                {"CUST002", "2023-06-30 16:50:00", 5, 4, 5, 5, "Highly Satisfied", "Outstanding food quality and value!", 10}
            };
            
            for (Object[] survey : sampleSurveys) {
                surveyStmt.setString(1, (String) survey[0]);
                surveyStmt.setTimestamp(2, java.sql.Timestamp.valueOf((String) survey[1]));
                surveyStmt.setInt(3, (Integer) survey[2]);
                surveyStmt.setInt(4, (Integer) survey[3]);
                surveyStmt.setInt(5, (Integer) survey[4]);
                surveyStmt.setInt(6, (Integer) survey[5]);
                surveyStmt.setString(7, (String) survey[6]);
                surveyStmt.setString(8, (String) survey[7]);
                surveyStmt.setInt(9, (Integer) survey[8]);
                surveyStmt.executeUpdate();
            }
            
            // Insert some sample insights
            insertSampleInsights(conn);
            
            System.out.println("Sample survey data inserted successfully");
        }
    }
    
    /**
     * Insert sample insights
     */
    private void insertSampleInsights(Connection conn) throws SQLException {
        String insertInsight = """
            INSERT INTO survey_insights (insight_type, insight_text, confidence_score, based_on_responses) 
            VALUES (?, ?, ?, ?)
        """;
        
        PreparedStatement insightStmt = conn.prepareStatement(insertInsight);
        
        // Positive insights
        insightStmt.setString(1, "positive");
        insightStmt.setString(2, "Food Quality consistently rates above 4.0 - customers love our culinary offerings");
        insightStmt.setDouble(3, 0.85);
        insightStmt.setInt(4, 15);
        insightStmt.executeUpdate();
        
        insightStmt.setString(1, "positive");
        insightStmt.setString(2, "Hygiene standards are highly appreciated by customers with average rating of 4.2");
        insightStmt.setDouble(3, 0.90);
        insightStmt.setInt(4, 18);
        insightStmt.executeUpdate();
        
        // Negative insights
        insightStmt.setString(1, "negative");
        insightStmt.setString(2, "Service Quality shows room for improvement with some customers experiencing delays");
        insightStmt.setDouble(3, 0.75);
        insightStmt.setInt(4, 12);
        insightStmt.executeUpdate();
        
        insightStmt.setString(1, "negative");
        insightStmt.setString(2, "Value for Money perception varies - some customers find pricing higher than expected");
        insightStmt.setDouble(3, 0.70);
        insightStmt.setInt(4, 10);
        insightStmt.executeUpdate();
        
        // Recommendations
        insightStmt.setString(1, "recommendation");
        insightStmt.setString(2, "Implement service training program to improve response times and customer interaction");
        insightStmt.setDouble(3, 0.80);
        insightStmt.setInt(4, 20);
        insightStmt.executeUpdate();
        
        insightStmt.setString(1, "recommendation");
        insightStmt.setString(2, "Consider reviewing pricing strategy or enhancing portion sizes to improve value perception");
        insightStmt.setDouble(3, 0.75);
        insightStmt.setInt(4, 16);
        insightStmt.executeUpdate();
        
        insightStmt.setString(1, "recommendation");
        insightStmt.setString(2, "Leverage high food quality ratings in marketing to attract more customers");
        insightStmt.setDouble(3, 0.85);
        insightStmt.setInt(4, 18);
        insightStmt.executeUpdate();
    }
    
    /**
     * Calculate and cache daily analytics
     */
    public void calculateDailyAnalytics() throws SQLException {
        try (Connection conn = getConnection()) {
            String analyticsSQL = """
                INSERT INTO survey_analytics 
                (calculation_date, total_responses, avg_satisfaction_score, satisfaction_rate, 
                 nps_score, avg_food_quality, avg_service_quality, avg_hygiene, avg_value_for_money)
                SELECT 
                    CURDATE() as calculation_date,
                    COUNT(*) as total_responses,
                    AVG(CASE overall_satisfaction 
                        WHEN 'Highly Satisfied' THEN 5
                        WHEN 'Satisfied' THEN 4
                        WHEN 'Neutral' THEN 3
                        WHEN 'Dissatisfied' THEN 2
                        WHEN 'Highly Dissatisfied' THEN 1
                        ELSE 3 END) as avg_satisfaction_score,
                    (SUM(CASE WHEN overall_satisfaction IN ('Satisfied', 'Highly Satisfied') THEN 1 ELSE 0 END) * 100.0 / COUNT(*)) as satisfaction_rate,
                    (SUM(CASE WHEN nps_score >= 9 THEN 1 ELSE 0 END) - SUM(CASE WHEN nps_score <= 6 THEN 1 ELSE 0 END)) * 100.0 / COUNT(*) as nps_score,
                    AVG(food_quality) as avg_food_quality,
                    AVG(service_quality) as avg_service_quality,
                    AVG(hygiene) as avg_hygiene,
                    AVG(value_for_money) as avg_value_for_money
                FROM survey_responses
                ON DUPLICATE KEY UPDATE
                    total_responses = VALUES(total_responses),
                    avg_satisfaction_score = VALUES(avg_satisfaction_score),
                    satisfaction_rate = VALUES(satisfaction_rate),
                    nps_score = VALUES(nps_score),
                    avg_food_quality = VALUES(avg_food_quality),
                    avg_service_quality = VALUES(avg_service_quality),
                    avg_hygiene = VALUES(avg_hygiene),
                    avg_value_for_money = VALUES(avg_value_for_money)
            """;
            
            PreparedStatement stmt = conn.prepareStatement(analyticsSQL);
            stmt.executeUpdate();
        }
    }
} 