package com.customerdashboard.backend;

import java.util.Date;

/**
 * SurveyResponse represents a customer satisfaction survey response
 * Contains all survey data fields and validation logic
 */
public class SurveyResponse {
    private int surveyId;
    private String customerId;
    private Date surveyDate;
    private int foodQuality;
    private int serviceQuality;
    private int hygiene;
    private int valueForMoney;
    private String overallSatisfaction;
    private String comments;
    private int npsScore;
    
    // Constructors
    public SurveyResponse() {
        this.surveyDate = new Date();
        this.foodQuality = 3;
        this.serviceQuality = 3;
        this.hygiene = 3;
        this.valueForMoney = 3;
        this.overallSatisfaction = "Neutral";
        this.comments = "";
        this.npsScore = 5;
    }
    
    public SurveyResponse(String customerId, int foodQuality, int serviceQuality, 
                         int hygiene, int valueForMoney, String overallSatisfaction) {
        this();
        this.customerId = customerId;
        this.foodQuality = validateRating(foodQuality);
        this.serviceQuality = validateRating(serviceQuality);
        this.hygiene = validateRating(hygiene);
        this.valueForMoney = validateRating(valueForMoney);
        this.overallSatisfaction = validateOverallSatisfaction(overallSatisfaction);
    }
    
    // Getters and Setters
    public int getSurveyId() {
        return surveyId;
    }
    
    public void setSurveyId(int surveyId) {
        this.surveyId = surveyId;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    public Date getSurveyDate() {
        return surveyDate;
    }
    
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate != null ? surveyDate : new Date();
    }
    
    public int getFoodQuality() {
        return foodQuality;
    }
    
    public void setFoodQuality(int foodQuality) {
        this.foodQuality = validateRating(foodQuality);
    }
    
    public int getServiceQuality() {
        return serviceQuality;
    }
    
    public void setServiceQuality(int serviceQuality) {
        this.serviceQuality = validateRating(serviceQuality);
    }
    
    public int getHygiene() {
        return hygiene;
    }
    
    public void setHygiene(int hygiene) {
        this.hygiene = validateRating(hygiene);
    }
    
    public int getValueForMoney() {
        return valueForMoney;
    }
    
    public void setValueForMoney(int valueForMoney) {
        this.valueForMoney = validateRating(valueForMoney);
    }
    
    public String getOverallSatisfaction() {
        return overallSatisfaction;
    }
    
    public void setOverallSatisfaction(String overallSatisfaction) {
        this.overallSatisfaction = validateOverallSatisfaction(overallSatisfaction);
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments != null ? comments : "";
    }
    
    public int getNpsScore() {
        return npsScore;
    }
    
    public void setNpsScore(int npsScore) {
        this.npsScore = validateNpsScore(npsScore);
    }
    
    // Validation methods
    private int validateRating(int rating) {
        return Math.max(1, Math.min(5, rating));
    }
    
    private int validateNpsScore(int npsScore) {
        return Math.max(0, Math.min(10, npsScore));
    }
    
    private String validateOverallSatisfaction(String satisfaction) {
        if (satisfaction == null) return "Neutral";
        
        String[] validSatisfactionLevels = {
            "Highly Satisfied", "Satisfied", "Neutral", "Dissatisfied", "Highly Dissatisfied"
        };
        
        for (String level : validSatisfactionLevels) {
            if (level.equalsIgnoreCase(satisfaction)) {
                return level;
            }
        }
        
        return "Neutral"; // Default
    }
    
    // Utility methods
    public double getAverageAttributeRating() {
        return (foodQuality + serviceQuality + hygiene + valueForMoney) / 4.0;
    }
    
    public int getSatisfactionScore() {
        switch (overallSatisfaction) {
            case "Highly Satisfied": return 5;
            case "Satisfied": return 4;
            case "Neutral": return 3;
            case "Dissatisfied": return 2;
            case "Highly Dissatisfied": return 1;
            default: return 3;
        }
    }
    
    public boolean isPromoter() {
        return npsScore >= 9;
    }
    
    public boolean isDetractor() {
        return npsScore <= 6;
    }
    
    public boolean isPassive() {
        return npsScore == 7 || npsScore == 8;
    }
    
    public boolean isSatisfied() {
        return "Satisfied".equals(overallSatisfaction) || "Highly Satisfied".equals(overallSatisfaction);
    }
    
    public boolean hasComments() {
        return comments != null && !comments.trim().isEmpty();
    }
    
    // Validation
    public boolean isValid() {
        return customerId != null && !customerId.trim().isEmpty() &&
               surveyDate != null &&
               foodQuality >= 1 && foodQuality <= 5 &&
               serviceQuality >= 1 && serviceQuality <= 5 &&
               hygiene >= 1 && hygiene <= 5 &&
               valueForMoney >= 1 && valueForMoney <= 5 &&
               overallSatisfaction != null &&
               npsScore >= 0 && npsScore <= 10;
    }
    
    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (customerId == null || customerId.trim().isEmpty()) {
            errors.append("Customer ID is required. ");
        }
        
        if (surveyDate == null) {
            errors.append("Survey date is required. ");
        }
        
        if (foodQuality < 1 || foodQuality > 5) {
            errors.append("Food quality rating must be between 1 and 5. ");
        }
        
        if (serviceQuality < 1 || serviceQuality > 5) {
            errors.append("Service quality rating must be between 1 and 5. ");
        }
        
        if (hygiene < 1 || hygiene > 5) {
            errors.append("Hygiene rating must be between 1 and 5. ");
        }
        
        if (valueForMoney < 1 || valueForMoney > 5) {
            errors.append("Value for money rating must be between 1 and 5. ");
        }
        
        if (overallSatisfaction == null) {
            errors.append("Overall satisfaction is required. ");
        }
        
        if (npsScore < 0 || npsScore > 10) {
            errors.append("NPS score must be between 0 and 10. ");
        }
        
        return errors.toString().trim();
    }
    
    // toString method
    @Override
    public String toString() {
        return String.format(
            "SurveyResponse{surveyId=%d, customerId='%s', surveyDate=%s, " +
            "foodQuality=%d, serviceQuality=%d, hygiene=%d, valueForMoney=%d, " +
            "overallSatisfaction='%s', npsScore=%d, hasComments=%b}",
            surveyId, customerId, surveyDate, foodQuality, serviceQuality, 
            hygiene, valueForMoney, overallSatisfaction, npsScore, hasComments()
        );
    }
    
    // equals and hashCode
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        SurveyResponse that = (SurveyResponse) obj;
        
        if (surveyId != 0 && that.surveyId != 0) {
            return surveyId == that.surveyId;
        }
        
        return customerId != null ? customerId.equals(that.customerId) : that.customerId == null;
    }
    
    @Override
    public int hashCode() {
        if (surveyId != 0) {
            return surveyId;
        }
        return customerId != null ? customerId.hashCode() : 0;
    }
} 