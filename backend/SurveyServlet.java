package com.customerdashboard.backend;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * SurveyServlet handles customer satisfaction survey operations
 * Provides REST API endpoints for survey data management and analytics
 */
@WebServlet("/api/survey/*")
@MultipartConfig(maxFileSize = 16177215) // 16MB max file size
public class SurveyServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private DatabaseManager dbManager;
    private Gson gson;
    private ExcelProcessor excelProcessor;

    @Override
    public void init() throws ServletException {
        super.init();
        dbManager = new DatabaseManager();
        gson = new Gson();
        excelProcessor = new ExcelProcessor();
    }

    /**
     * GET method to retrieve survey data and analytics
     * Endpoints:
     * - /api/survey/data - Get all survey responses
     * - /api/survey/analytics - Get analytics summary
     * - /api/survey/metrics - Get key metrics
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/data")) {
                // Get survey data with optional filters
                getSurveyData(request, response, out);
            } else if ("/analytics".equals(pathInfo)) {
                // Get analytics summary
                getSurveyAnalytics(request, response, out);
            } else if ("/metrics".equals(pathInfo)) {
                // Get key metrics
                getSurveyMetrics(request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonObject error = new JsonObject();
                error.addProperty("error", "Endpoint not found");
                out.print(gson.toJson(error));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Internal server error: " + e.getMessage());
            out.print(gson.toJson(error));
        } finally {
            out.close();
        }
    }

    /**
     * POST method to upload survey data or create new responses
     * Endpoints:
     * - /api/survey/upload - Upload Excel file with survey data
     * - /api/survey/response - Create new survey response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");
        
        String pathInfo = request.getPathInfo();
        PrintWriter out = response.getWriter();

        try {
            if ("/upload".equals(pathInfo)) {
                // Handle Excel file upload
                handleFileUpload(request, response, out);
            } else if ("/response".equals(pathInfo)) {
                // Create new survey response
                createSurveyResponse(request, response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                JsonObject error = new JsonObject();
                error.addProperty("error", "Endpoint not found");
                out.print(gson.toJson(error));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Internal server error: " + e.getMessage());
            out.print(gson.toJson(error));
        } finally {
            out.close();
        }
    }

    /**
     * OPTIONS method for CORS support
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Get survey data with optional filtering
     */
    private void getSurveyData(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        String dateFilter = request.getParameter("dateFilter");
        String ratingFilter = request.getParameter("ratingFilter");
        int limit = getIntParameter(request, "limit", 1000);
        int offset = getIntParameter(request, "offset", 0);

        try (Connection conn = dbManager.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT survey_id, customer_id, survey_date, food_quality, service_quality, " +
                "hygiene, value_for_money, overall_satisfaction, comments, nps_score " +
                "FROM survey_responses WHERE 1=1"
            );
            
            List<String> parameters = new ArrayList<>();
            
            // Apply date filter
            if (dateFilter != null && !dateFilter.equals("all")) {
                switch (dateFilter) {
                    case "last-30":
                        sql.append(" AND survey_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)");
                        break;
                    case "last-90":
                        sql.append(" AND survey_date >= DATE_SUB(NOW(), INTERVAL 90 DAY)");
                        break;
                    case "last-year":
                        sql.append(" AND survey_date >= DATE_SUB(NOW(), INTERVAL 1 YEAR)");
                        break;
                }
            }
            
            // Apply rating filter
            if (ratingFilter != null && !ratingFilter.equals("all")) {
                sql.append(" AND overall_satisfaction = ?");
                parameters.add(ratingFilter.replace("-", " "));
            }
            
            sql.append(" ORDER BY survey_date DESC LIMIT ? OFFSET ?");
            parameters.add(String.valueOf(limit));
            parameters.add(String.valueOf(offset));
            
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            for (String param : parameters) {
                if (param.equals(String.valueOf(limit)) || param.equals(String.valueOf(offset))) {
                    stmt.setInt(paramIndex++, Integer.parseInt(param));
                } else {
                    stmt.setString(paramIndex++, param);
                }
            }
            
            ResultSet rs = stmt.executeQuery();
            JsonArray surveyArray = new JsonArray();
            
            while (rs.next()) {
                JsonObject survey = createSurveyJson(rs);
                surveyArray.add(survey);
            }
            
            out.print(gson.toJson(surveyArray));
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Database error: " + e.getMessage());
            out.print(gson.toJson(error));
        }
    }

    /**
     * Get survey analytics summary
     */
    private void getSurveyAnalytics(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        try (Connection conn = dbManager.getConnection()) {
            JsonObject analytics = new JsonObject();
            
            // Overall metrics
            String metricsSQL = """
                SELECT 
                    COUNT(*) as total_responses,
                    AVG(CASE overall_satisfaction 
                        WHEN 'Highly Satisfied' THEN 5
                        WHEN 'Satisfied' THEN 4
                        WHEN 'Neutral' THEN 3
                        WHEN 'Dissatisfied' THEN 2
                        WHEN 'Highly Dissatisfied' THEN 1
                        ELSE 3 END) as avg_satisfaction,
                    AVG(food_quality) as avg_food_quality,
                    AVG(service_quality) as avg_service_quality,
                    AVG(hygiene) as avg_hygiene,
                    AVG(value_for_money) as avg_value_for_money,
                    AVG(nps_score) as avg_nps
                FROM survey_responses
            """;
            
            PreparedStatement metricsStmt = conn.prepareStatement(metricsSQL);
            ResultSet metricsRs = metricsStmt.executeQuery();
            
            if (metricsRs.next()) {
                analytics.addProperty("totalResponses", metricsRs.getInt("total_responses"));
                analytics.addProperty("avgSatisfaction", metricsRs.getDouble("avg_satisfaction"));
                analytics.addProperty("avgFoodQuality", metricsRs.getDouble("avg_food_quality"));
                analytics.addProperty("avgServiceQuality", metricsRs.getDouble("avg_service_quality"));
                analytics.addProperty("avgHygiene", metricsRs.getDouble("avg_hygiene"));
                analytics.addProperty("avgValueForMoney", metricsRs.getDouble("avg_value_for_money"));
                analytics.addProperty("avgNPS", metricsRs.getDouble("avg_nps"));
            }
            
            // Satisfaction distribution
            String distributionSQL = """
                SELECT overall_satisfaction, COUNT(*) as count 
                FROM survey_responses 
                GROUP BY overall_satisfaction
            """;
            
            PreparedStatement distStmt = conn.prepareStatement(distributionSQL);
            ResultSet distRs = distStmt.executeQuery();
            
            JsonObject distribution = new JsonObject();
            while (distRs.next()) {
                distribution.addProperty(distRs.getString("overall_satisfaction"), distRs.getInt("count"));
            }
            analytics.add("satisfactionDistribution", distribution);
            
            // Monthly trends (last 12 months)
            String trendsSQL = """
                SELECT 
                    DATE_FORMAT(survey_date, '%Y-%m') as month,
                    AVG(CASE overall_satisfaction 
                        WHEN 'Highly Satisfied' THEN 5
                        WHEN 'Satisfied' THEN 4
                        WHEN 'Neutral' THEN 3
                        WHEN 'Dissatisfied' THEN 2
                        WHEN 'Highly Dissatisfied' THEN 1
                        ELSE 3 END) as avg_satisfaction,
                    COUNT(*) as response_count
                FROM survey_responses 
                WHERE survey_date >= DATE_SUB(NOW(), INTERVAL 12 MONTH)
                GROUP BY DATE_FORMAT(survey_date, '%Y-%m')
                ORDER BY month
            """;
            
            PreparedStatement trendsStmt = conn.prepareStatement(trendsSQL);
            ResultSet trendsRs = trendsStmt.executeQuery();
            
            JsonArray trends = new JsonArray();
            while (trendsRs.next()) {
                JsonObject monthData = new JsonObject();
                monthData.addProperty("month", trendsRs.getString("month"));
                monthData.addProperty("avgSatisfaction", trendsRs.getDouble("avg_satisfaction"));
                monthData.addProperty("responseCount", trendsRs.getInt("response_count"));
                trends.add(monthData);
            }
            analytics.add("monthlyTrends", trends);
            
            out.print(gson.toJson(analytics));
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Database error: " + e.getMessage());
            out.print(gson.toJson(error));
        }
    }

    /**
     * Get key survey metrics
     */
    private void getSurveyMetrics(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        try (Connection conn = dbManager.getConnection()) {
            JsonObject metrics = new JsonObject();
            
            // Calculate satisfaction rate
            String satisfactionSQL = """
                SELECT 
                    COUNT(*) as total,
                    SUM(CASE WHEN overall_satisfaction IN ('Satisfied', 'Highly Satisfied') THEN 1 ELSE 0 END) as satisfied
                FROM survey_responses
            """;
            
            PreparedStatement satisfactionStmt = conn.prepareStatement(satisfactionSQL);
            ResultSet satisfactionRs = satisfactionStmt.executeQuery();
            
            if (satisfactionRs.next()) {
                int total = satisfactionRs.getInt("total");
                int satisfied = satisfactionRs.getInt("satisfied");
                double satisfactionRate = total > 0 ? (double) satisfied / total * 100 : 0;
                
                metrics.addProperty("totalResponses", total);
                metrics.addProperty("satisfactionRate", satisfactionRate);
            }
            
            // Calculate NPS
            String npsSQL = """
                SELECT 
                    SUM(CASE WHEN nps_score >= 9 THEN 1 ELSE 0 END) as promoters,
                    SUM(CASE WHEN nps_score <= 6 THEN 1 ELSE 0 END) as detractors,
                    COUNT(*) as total_nps_responses
                FROM survey_responses 
                WHERE nps_score IS NOT NULL
            """;
            
            PreparedStatement npsStmt = conn.prepareStatement(npsSQL);
            ResultSet npsRs = npsStmt.executeQuery();
            
            if (npsRs.next()) {
                int promoters = npsRs.getInt("promoters");
                int detractors = npsRs.getInt("detractors");
                int totalNPS = npsRs.getInt("total_nps_responses");
                
                if (totalNPS > 0) {
                    double npsScore = (double) (promoters - detractors) / totalNPS * 100;
                    metrics.addProperty("npsScore", npsScore);
                }
            }
            
            out.print(gson.toJson(metrics));
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Database error: " + e.getMessage());
            out.print(gson.toJson(error));
        }
    }

    /**
     * Handle Excel file upload
     */
    private void handleFileUpload(HttpServletRequest request, HttpServletResponse response, PrintWriter out) 
            throws IOException, ServletException {
        
        try {
            Part filePart = request.getPart("file");
            if (filePart == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                JsonObject error = new JsonObject();
                error.addProperty("error", "No file uploaded");
                out.print(gson.toJson(error));
                return;
            }
            
            // Process Excel file
            List<SurveyResponse> surveyResponses = excelProcessor.processExcelFile(filePart.getInputStream());
            
            // Save to database
            int savedCount = saveSurveyResponses(surveyResponses);
            
            JsonObject success = new JsonObject();
            success.addProperty("message", "File processed successfully");
            success.addProperty("recordsProcessed", surveyResponses.size());
            success.addProperty("recordsSaved", savedCount);
            out.print(gson.toJson(success));
            response.setStatus(HttpServletResponse.SC_OK);
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("error", "File processing error: " + e.getMessage());
            out.print(gson.toJson(error));
        }
    }

    /**
     * Create new survey response
     */
    private void createSurveyResponse(HttpServletRequest request, HttpServletResponse response, PrintWriter out) 
            throws IOException {
        
        // Read JSON from request body
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            sb.append(line);
        }
        
        try {
            JsonObject surveyData = gson.fromJson(sb.toString(), JsonObject.class);
            
            try (Connection conn = dbManager.getConnection()) {
                String sql = """
                    INSERT INTO survey_responses 
                    (customer_id, survey_date, food_quality, service_quality, hygiene, 
                     value_for_money, overall_satisfaction, comments, nps_score) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
                
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, surveyData.get("customerId").getAsString());
                stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                stmt.setInt(3, surveyData.get("foodQuality").getAsInt());
                stmt.setInt(4, surveyData.get("serviceQuality").getAsInt());
                stmt.setInt(5, surveyData.get("hygiene").getAsInt());
                stmt.setInt(6, surveyData.get("valueForMoney").getAsInt());
                stmt.setString(7, surveyData.get("overallSatisfaction").getAsString());
                stmt.setString(8, surveyData.has("comments") ? surveyData.get("comments").getAsString() : "");
                stmt.setInt(9, surveyData.has("npsScore") ? surveyData.get("npsScore").getAsInt() : 5);
                
                int rowsInserted = stmt.executeUpdate();
                
                if (rowsInserted > 0) {
                    JsonObject success = new JsonObject();
                    success.addProperty("message", "Survey response created successfully");
                    out.print(gson.toJson(success));
                    response.setStatus(HttpServletResponse.SC_CREATED);
                }
                
            } catch (SQLException e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                JsonObject error = new JsonObject();
                error.addProperty("error", "Database error: " + e.getMessage());
                out.print(gson.toJson(error));
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("error", "Invalid JSON data: " + e.getMessage());
            out.print(gson.toJson(error));
        }
    }

    /**
     * Save survey responses to database
     */
    private int saveSurveyResponses(List<SurveyResponse> responses) throws SQLException {
        int savedCount = 0;
        
        try (Connection conn = dbManager.getConnection()) {
            String sql = """
                INSERT INTO survey_responses 
                (customer_id, survey_date, food_quality, service_quality, hygiene, 
                 value_for_money, overall_satisfaction, comments, nps_score) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            for (SurveyResponse response : responses) {
                stmt.setString(1, response.getCustomerId());
                stmt.setTimestamp(2, new Timestamp(response.getSurveyDate().getTime()));
                stmt.setInt(3, response.getFoodQuality());
                stmt.setInt(4, response.getServiceQuality());
                stmt.setInt(5, response.getHygiene());
                stmt.setInt(6, response.getValueForMoney());
                stmt.setString(7, response.getOverallSatisfaction());
                stmt.setString(8, response.getComments());
                stmt.setInt(9, response.getNpsScore());
                
                try {
                    stmt.executeUpdate();
                    savedCount++;
                } catch (SQLException e) {
                    // Log error but continue processing other records
                    System.err.println("Error saving survey response: " + e.getMessage());
                }
            }
        }
        
        return savedCount;
    }

    /**
     * Helper method to create survey JSON object from ResultSet
     */
    private JsonObject createSurveyJson(ResultSet rs) throws SQLException {
        JsonObject survey = new JsonObject();
        survey.addProperty("surveyId", rs.getInt("survey_id"));
        survey.addProperty("customerId", rs.getString("customer_id"));
        survey.addProperty("surveyDate", rs.getTimestamp("survey_date").toString());
        survey.addProperty("foodQuality", rs.getInt("food_quality"));
        survey.addProperty("serviceQuality", rs.getInt("service_quality"));
        survey.addProperty("hygiene", rs.getInt("hygiene"));
        survey.addProperty("valueForMoney", rs.getInt("value_for_money"));
        survey.addProperty("overallSatisfaction", rs.getString("overall_satisfaction"));
        survey.addProperty("comments", rs.getString("comments"));
        survey.addProperty("npsScore", rs.getInt("nps_score"));
        return survey;
    }

    /**
     * Helper method to get integer parameter with default value
     */
    private int getIntParameter(HttpServletRequest request, String paramName, int defaultValue) {
        String paramValue = request.getParameter(paramName);
        if (paramValue != null) {
            try {
                return Integer.parseInt(paramValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    @Override
    public void destroy() {
        if (dbManager != null) {
            dbManager.closeConnection();
        }
        super.destroy();
    }
} 