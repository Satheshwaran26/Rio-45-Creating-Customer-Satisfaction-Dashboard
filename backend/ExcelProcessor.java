package com.customerdashboard.backend;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * ExcelProcessor handles Excel file processing for survey data
 * Supports both .xlsx and .xls formats
 */
public class ExcelProcessor {
    
    /**
     * Process Excel file and extract survey responses
     */
    public List<SurveyResponse> processExcelFile(InputStream inputStream) throws Exception {
        List<SurveyResponse> responses = new ArrayList<>();
        
        try {
            // Try to create workbook (handles both .xlsx and .xls)
            Workbook workbook = null;
            try {
                workbook = new XSSFWorkbook(inputStream);
            } catch (Exception e) {
                // If XLSX fails, try XLS
                workbook = new HSSFWorkbook(inputStream);
            }
            
            Sheet sheet = workbook.getSheetAt(0); // Get first sheet
            
            // Find header row and map columns
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new Exception("Header row not found");
            }
            
            ColumnMapping columnMapping = mapColumns(headerRow);
            
            // Process data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                try {
                    SurveyResponse response = processRow(row, columnMapping);
                    if (response != null) {
                        responses.add(response);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing row " + (i + 1) + ": " + e.getMessage());
                    // Continue processing other rows
                }
            }
            
            workbook.close();
            
        } catch (Exception e) {
            throw new Exception("Error processing Excel file: " + e.getMessage(), e);
        }
        
        return responses;
    }
    
    /**
     * Map Excel columns to survey fields
     */
    private ColumnMapping mapColumns(Row headerRow) {
        ColumnMapping mapping = new ColumnMapping();
        
        for (int i = 0; i < headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) continue;
            
            String columnName = cell.getStringCellValue().toLowerCase().trim();
            
            // Map column names to indices
            if (columnName.contains("date") || columnName.contains("timestamp")) {
                mapping.dateColumn = i;
            } else if (columnName.contains("customer") && columnName.contains("id")) {
                mapping.customerIdColumn = i;
            } else if (columnName.contains("food") && columnName.contains("quality")) {
                mapping.foodQualityColumn = i;
            } else if (columnName.contains("service") && columnName.contains("quality")) {
                mapping.serviceQualityColumn = i;
            } else if (columnName.contains("hygiene")) {
                mapping.hygieneColumn = i;
            } else if (columnName.contains("value") && columnName.contains("money")) {
                mapping.valueForMoneyColumn = i;
            } else if (columnName.contains("overall") && columnName.contains("satisfaction")) {
                mapping.overallSatisfactionColumn = i;
            } else if (columnName.contains("comment")) {
                mapping.commentsColumn = i;
            } else if (columnName.contains("nps")) {
                mapping.npsColumn = i;
            }
        }
        
        return mapping;
    }
    
    /**
     * Process individual row and create SurveyResponse
     */
    private SurveyResponse processRow(Row row, ColumnMapping mapping) throws Exception {
        SurveyResponse response = new SurveyResponse();
        
        // Extract data from cells
        response.setSurveyDate(getDateValue(row, mapping.dateColumn));
        response.setCustomerId(getStringValue(row, mapping.customerIdColumn));
        response.setFoodQuality(getRatingValue(row, mapping.foodQualityColumn));
        response.setServiceQuality(getRatingValue(row, mapping.serviceQualityColumn));
        response.setHygiene(getRatingValue(row, mapping.hygieneColumn));
        response.setValueForMoney(getRatingValue(row, mapping.valueForMoneyColumn));
        response.setOverallSatisfaction(normalizeOverallSatisfaction(getStringValue(row, mapping.overallSatisfactionColumn)));
        response.setComments(getStringValue(row, mapping.commentsColumn));
        response.setNpsScore(getNpsValue(row, mapping.npsColumn));
        
        // Validate required fields
        if (response.getCustomerId() == null || response.getCustomerId().trim().isEmpty()) {
            throw new Exception("Customer ID is required");
        }
        
        return response;
    }
    
    /**
     * Get date value from cell
     */
    private Date getDateValue(Row row, int columnIndex) {
        if (columnIndex == -1) return new Date(); // Default to current date
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return new Date();
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    // Excel date serial number
                    return DateUtil.getJavaDate(cell.getNumericCellValue());
                }
            } else if (cell.getCellType() == CellType.STRING) {
                // Try to parse string date
                String dateStr = cell.getStringCellValue();
                // Add date parsing logic here if needed
                return new Date(); // Fallback to current date
            }
        } catch (Exception e) {
            System.err.println("Error parsing date: " + e.getMessage());
        }
        
        return new Date();
    }
    
    /**
     * Get string value from cell
     */
    private String getStringValue(Row row, int columnIndex) {
        if (columnIndex == -1) return "";
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return "";
        
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue();
                case NUMERIC:
                    return String.valueOf((long) cell.getNumericCellValue());
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    return cell.getCellFormula();
                default:
                    return "";
            }
        } catch (Exception e) {
            System.err.println("Error getting string value: " + e.getMessage());
            return "";
        }
    }
    
    /**
     * Get rating value (1-5) from cell
     */
    private int getRatingValue(Row row, int columnIndex) {
        if (columnIndex == -1) return 3; // Default neutral rating
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return 3;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                int rating = (int) cell.getNumericCellValue();
                return Math.max(1, Math.min(5, rating)); // Ensure 1-5 range
            } else if (cell.getCellType() == CellType.STRING) {
                String ratingStr = cell.getStringCellValue().trim();
                try {
                    int rating = Integer.parseInt(ratingStr);
                    return Math.max(1, Math.min(5, rating));
                } catch (NumberFormatException e) {
                    // Try to parse text ratings
                    return parseTextRating(ratingStr);
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing rating: " + e.getMessage());
        }
        
        return 3; // Default neutral rating
    }
    
    /**
     * Get NPS score (0-10) from cell
     */
    private int getNpsValue(Row row, int columnIndex) {
        if (columnIndex == -1) return 5; // Default neutral NPS
        
        Cell cell = row.getCell(columnIndex);
        if (cell == null) return 5;
        
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                int nps = (int) cell.getNumericCellValue();
                return Math.max(0, Math.min(10, nps)); // Ensure 0-10 range
            } else if (cell.getCellType() == CellType.STRING) {
                String npsStr = cell.getStringCellValue().trim();
                try {
                    int nps = Integer.parseInt(npsStr);
                    return Math.max(0, Math.min(10, nps));
                } catch (NumberFormatException e) {
                    return 5; // Default neutral
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing NPS: " + e.getMessage());
        }
        
        return 5; // Default neutral NPS
    }
    
    /**
     * Parse text-based ratings
     */
    private int parseTextRating(String text) {
        text = text.toLowerCase().trim();
        
        if (text.contains("excellent") || text.contains("outstanding") || text.equals("5")) return 5;
        if (text.contains("good") || text.contains("very good") || text.equals("4")) return 4;
        if (text.contains("average") || text.contains("okay") || text.contains("fair") || text.equals("3")) return 3;
        if (text.contains("poor") || text.contains("below average") || text.equals("2")) return 2;
        if (text.contains("terrible") || text.contains("very poor") || text.equals("1")) return 1;
        
        return 3; // Default neutral
    }
    
    /**
     * Normalize overall satisfaction values
     */
    private String normalizeOverallSatisfaction(String satisfaction) {
        if (satisfaction == null || satisfaction.trim().isEmpty()) {
            return "Neutral";
        }
        
        satisfaction = satisfaction.toLowerCase().trim();
        
        if (satisfaction.contains("highly satisfied") || satisfaction.contains("excellent") || satisfaction.contains("very satisfied")) {
            return "Highly Satisfied";
        } else if (satisfaction.contains("satisfied") || satisfaction.contains("good")) {
            return "Satisfied";
        } else if (satisfaction.contains("neutral") || satisfaction.contains("average") || satisfaction.contains("okay")) {
            return "Neutral";
        } else if (satisfaction.contains("dissatisfied") || satisfaction.contains("poor")) {
            return "Dissatisfied";
        } else if (satisfaction.contains("highly dissatisfied") || satisfaction.contains("terrible") || satisfaction.contains("very poor")) {
            return "Highly Dissatisfied";
        }
        
        return "Neutral";
    }
    
    /**
     * Inner class to map column indices
     */
    private static class ColumnMapping {
        int dateColumn = -1;
        int customerIdColumn = -1;
        int foodQualityColumn = -1;
        int serviceQualityColumn = -1;
        int hygieneColumn = -1;
        int valueForMoneyColumn = -1;
        int overallSatisfactionColumn = -1;
        int commentsColumn = -1;
        int npsColumn = -1;
    }
} 