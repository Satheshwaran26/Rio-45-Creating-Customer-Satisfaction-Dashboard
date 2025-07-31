package com.customerdashboard.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

/**
 * DatabaseManager handles all database connections and operations
 * Provides connection pooling and database initialization
 */
public class DatabaseManager {
    private static final String DB_PROPERTIES_FILE = "/db.properties";
    private String dbUrl;
    private String dbUsername;
    private String dbPassword;
    private String dbDriver;
    
    // Connection pool variables
    private Connection connection;
    
    /**
     * Constructor - loads database configuration
     */
    public DatabaseManager() {
        loadDatabaseConfig();
        initializeDatabase();
    }
    
    /**
     * Load database configuration from properties file
     */
    private void loadDatabaseConfig() {
        Properties props = new Properties();
        
        try (InputStream input = getClass().getResourceAsStream(DB_PROPERTIES_FILE)) {
            if (input != null) {
                props.load(input);
                
                dbUrl = props.getProperty("db.url", "jdbc:h2:./data/customerdb;AUTO_SERVER=TRUE");
                dbUsername = props.getProperty("db.username", "sa");
                dbPassword = props.getProperty("db.password", "");
                dbDriver = props.getProperty("db.driver", "org.h2.Driver");
            } else {
                // Use default H2 database configuration
                setDefaultConfig();
            }
        } catch (IOException e) {
            System.err.println("Error loading database configuration: " + e.getMessage());
            setDefaultConfig();
        }
    }
    
    /**
     * Set default database configuration
     */
    private void setDefaultConfig() {
        dbUrl = "jdbc:h2:./data/customerdb;AUTO_SERVER=TRUE";
        dbUsername = "sa";
        dbPassword = "";
        dbDriver = "org.h2.Driver";
    }
    
    /**
     * Initialize database connection and create tables if they don't exist
     */
    private void initializeDatabase() {
        try {
            // Load database driver
            Class.forName(dbDriver);
            
            // Create tables if they don't exist
            createTables();
            
            // Insert sample data if tables are empty
            insertSampleData();
            
            System.out.println("Database initialized successfully");
            
        } catch (ClassNotFoundException e) {
            System.err.println("Database driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }
    
    /**
     * Get database connection
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        }
        return connection;
    }
    
    /**
     * Create database tables
     */
    private void createTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create customers table
            String createCustomersTable = """
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,
                    email VARCHAR(100) UNIQUE NOT NULL,
                    phone VARCHAR(20),
                    address TEXT,
                    total_orders INT DEFAULT 0,
                    account_balance DECIMAL(10,2) DEFAULT 0.00,
                    reward_points INT DEFAULT 0,
                    last_login TIMESTAMP,
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.execute(createCustomersTable);
            
            // Create orders table
            String createOrdersTable = """
                CREATE TABLE IF NOT EXISTS orders (
                    order_id VARCHAR(50) PRIMARY KEY,
                    customer_id VARCHAR(50),
                    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    status VARCHAR(20) DEFAULT 'Pending',
                    total_amount DECIMAL(10,2) NOT NULL,
                    shipping_address TEXT,
                    FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
                )
            """;
            stmt.execute(createOrdersTable);
            
            // Create order_items table
            String createOrderItemsTable = """
                CREATE TABLE IF NOT EXISTS order_items (
                    item_id INT AUTO_INCREMENT PRIMARY KEY,
                    order_id VARCHAR(50),
                    product_name VARCHAR(100),
                    quantity INT,
                    unit_price DECIMAL(10,2),
                    total_price DECIMAL(10,2),
                    FOREIGN KEY (order_id) REFERENCES orders(order_id)
                )
            """;
            stmt.execute(createOrderItemsTable);
            
            // Create products table (for future use)
            String createProductsTable = """
                CREATE TABLE IF NOT EXISTS products (
                    product_id VARCHAR(50) PRIMARY KEY,
                    product_name VARCHAR(100) NOT NULL,
                    description TEXT,
                    price DECIMAL(10,2) NOT NULL,
                    stock_quantity INT DEFAULT 0,
                    category VARCHAR(50),
                    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """;
            stmt.execute(createProductsTable);
            
            System.out.println("Database tables created successfully");
        }
    }
    
    /**
     * Insert sample data for testing
     */
    private void insertSampleData() throws SQLException {
        try (Connection conn = getConnection()) {
            
            // Check if data already exists
            PreparedStatement checkStmt = conn.prepareStatement("SELECT COUNT(*) FROM customers");
            var rs = checkStmt.executeQuery();
            rs.next();
            if (rs.getInt(1) > 0) {
                return; // Data already exists
            }
            
            // Insert sample customers
            String insertCustomer = """
                INSERT INTO customers (customer_id, name, email, phone, address, 
                                     total_orders, account_balance, reward_points, last_login) 
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
            """;
            
            PreparedStatement customerStmt = conn.prepareStatement(insertCustomer);
            
            // Customer 1
            customerStmt.setString(1, "CUST001");
            customerStmt.setString(2, "John Doe");
            customerStmt.setString(3, "john.doe@email.com");
            customerStmt.setString(4, "+1 (555) 123-4567");
            customerStmt.setString(5, "123 Main Street, Anytown, ST 12345");
            customerStmt.setInt(6, 15);
            customerStmt.setDouble(7, 247.50);
            customerStmt.setInt(8, 1250);
            customerStmt.executeUpdate();
            
            // Customer 2
            customerStmt.setString(1, "CUST002");
            customerStmt.setString(2, "Jane Smith");
            customerStmt.setString(3, "jane.smith@email.com");
            customerStmt.setString(4, "+1 (555) 987-6543");
            customerStmt.setString(5, "456 Oak Avenue, Springfield, ST 67890");
            customerStmt.setInt(6, 8);
            customerStmt.setDouble(7, 156.75);
            customerStmt.setInt(8, 875);
            customerStmt.executeUpdate();
            
            // Customer 3
            customerStmt.setString(1, "CUST003");
            customerStmt.setString(2, "Mike Johnson");
            customerStmt.setString(3, "mike.johnson@email.com");
            customerStmt.setString(4, "+1 (555) 456-7890");
            customerStmt.setString(5, "789 Pine Road, Riverside, ST 54321");
            customerStmt.setInt(6, 22);
            customerStmt.setDouble(7, 389.25);
            customerStmt.setInt(8, 1875);
            customerStmt.executeUpdate();
            
            // Insert sample orders
            String insertOrder = """
                INSERT INTO orders (order_id, customer_id, order_date, status, total_amount, shipping_address) 
                VALUES (?, ?, ?, ?, ?, ?)
            """;
            
            PreparedStatement orderStmt = conn.prepareStatement(insertOrder);
            
            // Orders for John Doe
            orderStmt.setString(1, "ORD-001");
            orderStmt.setString(2, "CUST001");
            orderStmt.setTimestamp(3, java.sql.Timestamp.valueOf("2024-01-15 10:30:00"));
            orderStmt.setString(4, "Completed");
            orderStmt.setDouble(5, 89.99);
            orderStmt.setString(6, "123 Main Street, Anytown, ST 12345");
            orderStmt.executeUpdate();
            
            orderStmt.setString(1, "ORD-002");
            orderStmt.setString(2, "CUST001");
            orderStmt.setTimestamp(3, java.sql.Timestamp.valueOf("2024-01-10 14:20:00"));
            orderStmt.setString(4, "Pending");
            orderStmt.setDouble(5, 156.50);
            orderStmt.setString(6, "123 Main Street, Anytown, ST 12345");
            orderStmt.executeUpdate();
            
            orderStmt.setString(1, "ORD-003");
            orderStmt.setString(2, "CUST001");
            orderStmt.setTimestamp(3, java.sql.Timestamp.valueOf("2024-01-05 09:15:00"));
            orderStmt.setString(4, "Completed");
            orderStmt.setDouble(5, 45.75);
            orderStmt.setString(6, "123 Main Street, Anytown, ST 12345");
            orderStmt.executeUpdate();
            
            orderStmt.setString(1, "ORD-004");
            orderStmt.setString(2, "CUST001");
            orderStmt.setTimestamp(3, java.sql.Timestamp.valueOf("2023-12-28 16:45:00"));
            orderStmt.setString(4, "Cancelled");
            orderStmt.setDouble(5, 120.00);
            orderStmt.setString(6, "123 Main Street, Anytown, ST 12345");
            orderStmt.executeUpdate();
            
            orderStmt.setString(1, "ORD-005");
            orderStmt.setString(2, "CUST001");
            orderStmt.setTimestamp(3, java.sql.Timestamp.valueOf("2023-12-20 11:30:00"));
            orderStmt.setString(4, "Completed");
            orderStmt.setDouble(5, 78.25);
            orderStmt.setString(6, "123 Main Street, Anytown, ST 12345");
            orderStmt.executeUpdate();
            
            // Orders for Jane Smith
            orderStmt.setString(1, "ORD-006");
            orderStmt.setString(2, "CUST002");
            orderStmt.setTimestamp(3, java.sql.Timestamp.valueOf("2024-01-12 13:20:00"));
            orderStmt.setString(4, "Completed");
            orderStmt.setDouble(5, 65.30);
            orderStmt.setString(6, "456 Oak Avenue, Springfield, ST 67890");
            orderStmt.executeUpdate();
            
            System.out.println("Sample data inserted successfully");
        }
    }
    
    /**
     * Close database connection
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }
    
    /**
     * Execute a custom SQL query (for utility purposes)
     */
    public boolean executeQuery(String sql) {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            return true;
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            return false;
        }
    }
} 