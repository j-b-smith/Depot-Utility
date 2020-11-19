package JosephSmith.Database;

import JosephSmith.model.DispatchMachine;
import JosephSmith.model.LogEntry;
import JosephSmith.model.Token;
import JosephSmith.view.HomePageController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.time.ZoneId;
import java.util.Properties;

/** Class to handle all database interactions */
public class Database {

    //Store connection
    private static Connection connection = null;

    /*
    Handle create and close connection
     */

    /**
     * Connect to the database
     */
    private static void connect(){

        //Create properties and load file
        Properties properties = new Properties();
        FileInputStream config = null;
        try {
            config = new FileInputStream("src/JosephSmith/Resources/config.properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            assert config != null;
            properties.load(config);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Try to connect to database
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(properties.getProperty("localDBServer"),
                                                     properties.getProperty("localDBUsername"),
                                                     properties.getProperty("localDBPassword"));
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            HomePageController.databaseAlert.setTitle("Database Error");
            HomePageController.databaseAlert.setHeaderText("Database Connection Failed");
            HomePageController.databaseAlert.setContentText("Failed to establish connection with the database");
            HomePageController.databaseAlert.showAndWait();
        }
    }

    /**
     * Disconnect from the database
     */
    private static void disconnect(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /*
    Handle Machine Log operations
     */

    /**
     * Return a list of log entries containing search criteria sorted by date in descending order
     * @param searchCriteria the string to search for
     * @return the list of log entries
     */
    public static ObservableList<LogEntry> searchMachineLog(String searchCriteria){

        //Connect to the database
        connect();

        //Store the log entries from the query
        ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();

        try (Statement statement = connection.createStatement()){

            //Get result set from query
            ResultSet logMachines = statement.executeQuery("SELECT * " +
                                                               "FROM MachineLog " +
                                                               "WHERE LOWER(Date) LIKE '%" + searchCriteria + "%' " +
                                                               "OR LOWER(Request_Number) LIKE '%" + searchCriteria + "%' " +
                                                               "OR LOWER(Service_Tag) LIKE '%" + searchCriteria + "%' " +
                                                               "OR LOWER(Model) LIKE '%" + searchCriteria + "%' " +
                                                               "OR LOWER(Machine_Issue) LIKE '%" + searchCriteria + "%' " +
                                                               "OR LOWER(Part_Needed) LIKE '%" + searchCriteria + "%' " +
                                                               "ORDER BY Date DESC;");

            //Iterate through result set
            while (logMachines.next()){

                //Create log entry from row adn add to list
                logEntries.add(new LogEntry (logMachines.getString("Date"),
                                                logMachines.getString("Request_Number"),
                                                logMachines.getString("Service_Tag"),
                                                logMachines.getString("Model"),
                                                logMachines.getString("Machine_Issue"),
                                                logMachines.getString("Part_Needed")));

            }

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        //Disconnect from the database
        disconnect();

        //Return the result of the search
        return logEntries;
    }

    /**
     * Get all the log entries from the MachineLog table
     * @return an observable list of all the log entries
     */
    public static ObservableList<LogEntry> getLogMachines() {

        //Connect to the database
        connect();

        //Store the log entries returned from the query
        ObservableList<LogEntry> logEntries = FXCollections.observableArrayList();

        //Create a new statement
        try(Statement statement = connection.createStatement()) {

            //Get the result set
            ResultSet logMachines = statement.executeQuery
                    ("SELECT * FROM MachineLog ORDER BY Date DESC;");

            //Loop through the result set
            while (logMachines.next()) {

                //Instantiate log entry
                LogEntry logEntry = new LogEntry (logMachines.getString("Date"),
                        logMachines.getString("Request_Number"),
                        logMachines.getString("Service_Tag"),
                        logMachines.getString("Model"),
                        logMachines.getString("Machine_Issue"),
                        logMachines.getString("Part_Needed"));

                //Add log entry to the list
                logEntries.add(logEntry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Close the database connection
        disconnect();

        //Return the logEntries
        return logEntries;
    }

    /**
     * Add new log entry to the database
     * @param logEntry the log entry to add
     */
    public static void addMachineToLog(LogEntry logEntry){

        //Connect to the database
        connect();

        try(Statement statement = connection.createStatement()){

            //Insert query
            String insertQuery = "INSERT INTO MachineLog " +
                                 "(Date, Request_Number, Service_Tag, Model, Machine_Issue, Part_Needed) " +
                                 "VALUES " +
                                 "('" + logEntry.getDate() + "' , '" + logEntry.getRequestNumber() +
                                 "' , '" + logEntry.getServiceTag() + "' , '" + logEntry.getModel() +
                                 "' , '" + logEntry.getMachineIssue() + "' , '" + logEntry.getPartNeeded() + "' ); ";

            //Execute query
            statement.executeUpdate(insertQuery);
        } catch (SQLException e){
            e.printStackTrace();
        }

        //Disconnect from the database
        disconnect();
    }

    /*
    Handle Dispatch Machine operations
     */

    /**
     * Create dispatch machine from issue description table
     * @param serviceTag the service tag of the machine
     * @param serialNumber the serial number of the machine
     * @param machineIssue the issue of the machine
     * @return the dispatch machine
     */
    public static DispatchMachine createDispatchMachine(String serviceTag, String serialNumber, String machineIssue){

        //Store dispatch machine
        DispatchMachine dispatchMachine = null;

        //Connect to the database
        connect();

        try(Statement statement = connection.createStatement()){

            //Machine Issue Query
            String machineIssueQuery = "SELECT * FROM IssueDescriptions WHERE Machine_Issue = '" + machineIssue + "';";

            //Get result set
            ResultSet queryResult = statement.executeQuery(machineIssueQuery);

            //Iterate result set and create dispatch machine
            while(queryResult.next()){
                dispatchMachine = new DispatchMachine(serviceTag,
                                                    machineIssue,
                                                    queryResult.getString("Troubleshooting_Steps"),
                                                    queryResult.getString("Part_Needed"),
                                                    serialNumber,
                                                    queryResult.getString("Part_Code").trim());
            }

        } catch (SQLException e){
            e.printStackTrace();
        }

        //Return dispatch machine
        return dispatchMachine;
    }

    /**
     * Add a list of WarrantyMachines to the WarrantyMachine table
     * @param dispatchMachines the list of WarrantyMachines to add
     */
    public static void addDispatchMachines(ObservableList<DispatchMachine> dispatchMachines) {
        clearTable("DispatchMachines");
        connect();
        for (DispatchMachine machine : dispatchMachines) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO DispatchMachines (Service_Tag, Machine_Issue, Troubleshooting_Steps, Part_Needed, Serial_Number) " +
                        "VALUES ('" + machine.getServiceTag() + "' , '" + machine.getMachineIssue() + "' , '"
                        + machine.getTroubleshootingSteps() + "' , '" + machine.getPartNeeded() + "' , '" + machine.getSerialNumber() + "' );");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        disconnect();
    }

    /*
    Create lists from tables
     */

    /**
     * Return a list of machine issues based on part needed
     * @param part the part needed
     * @return the list of machine issues
     */
    public static ObservableList<String> getMachineIssues(String part){
        connect();

        ObservableList<String> machineIssues = FXCollections.observableArrayList();

        try (Statement statement = connection.createStatement()){

            //Query
            String uniqueValueQuery = "SELECT * FROM IssueDescriptions WHERE Part_Needed = '" + part + "' ORDER BY Machine_Issue ASC;";

            //Get result set
            ResultSet queryResult = statement.executeQuery(uniqueValueQuery);

            while (queryResult.next()){
                machineIssues.add(queryResult.getString("Machine_Issue"));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        disconnect();
        return machineIssues;
    }

    /**
     * Return a list of unique values from the database
     * @param column the column to retrieve from
     * @param tableName the table to retrieve from
     * @return the list of values
     */
    public static ObservableList<String> createUniqueValueList(String column, String tableName){

        //Connect to the database
        connect();

        //Store the values returned from the query
        ObservableList<String> valueList = FXCollections.observableArrayList();

        try (Statement statement = connection.createStatement()){

            //Query
            String uniqueValueQuery = "SELECT DISTINCT " + column + " FROM " + tableName + ";";

            //Get result set
            ResultSet queryResult = statement.executeQuery(uniqueValueQuery);

            //Iterate result set and add vlaue to list
            while (queryResult.next()){
                valueList.add(queryResult.getString(column));
            }
        } catch (SQLException e){
            e.printStackTrace();
        }

        //Disconnect from the database
        disconnect();

        //Return the values
        return valueList;
    }

    /*
    Handle Dell API database operations
     */

    /**
     * Get the bearer token from the database
     * @param apiName the api name of the requested token
     * @return the token
     */
    public static Token getToken(String apiName){
        connect();

        Token token = null;

        try (Statement statement = connection.createStatement()){
            //Get Result set
            ResultSet queryResult = statement.
                    executeQuery("SELECT * FROM BearerToken WHERE API_Name = '" + apiName + "';");
            //Move cursor to first position
            while(queryResult.next()) {
                token = new Token(queryResult.getString("token"),
                        queryResult.getTimestamp("expiration").toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
        return token;
    }

    /**
     * Update the bearer token in the database
     * @param token the updated token
     * @param apiName the name of the api to update the token for
     */
    public static void updateToken(Token token, String apiName){
        connect();

        try (Statement statement = connection.createStatement()) {
            String query = "Update BearerToken SET token = '" + token.getBearerToken() + "', expiration = GETDATE() WHERE API_Name = '" + apiName + "'; " +
                    "UPDATE BearerToken SET expiration = DATEADD(ss, 3600, expiration) WHERE API_Name = '" + apiName + "';";
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
     }

    /*
    Misc. operations
     */

    /**
     * Get the number of occurrences for a cell value
     * @param tableName the table to be queried
     * @param column the column to be queried
     * @param value the value to search for
     * @return the number of occurrences of the value
     */
    public static int getValueCount(String tableName, String column, String value){

        //Connect to the database
        connect();

        //Store the count
        int count = 0;

        try (Statement statement = connection.createStatement()){

            //Get Result set
            ResultSet countSet = statement.
                    executeQuery("SELECT COUNT(*) AS count FROM " + tableName + " WHERE " + column +  "= '" + value + "';");
            //Move cursor to first position
            countSet.next();

            //Set count value
            count = countSet.getInt("count");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Disconnect from the database
        disconnect();

        //Return the count value
        return count;
    }

    /**
     * Clear all entries from a table
     * @param tableName the table to clear
     */
    public static void clearTable(String tableName){
        connect();
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM " + tableName + ";");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
    }

    /**
     * Add new machine issue to the database
     * @param machineIssue the machine issue to add
     * @param troubleshootingSteps the troubleshooting steps for the issue
     * @param partNeeded the part needed for the issue
     */
    public static void addNewIssue(String machineIssue, String troubleshootingSteps, String partNeeded){

        connect();

        //Store part code
        String partCode = null;

        try (Statement statement = connection.createStatement()){
            //Query string
            String queryString = "SELECT Part_Code FROM IssueDescriptions WHERE Part_Needed = '" + partNeeded + "';";
            //Get the results of the query
            ResultSet resultSet = statement.executeQuery(queryString);

            //Get the part code
            while (resultSet.next()){
                partCode = resultSet.getString("Part_Code");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


        try (Statement statement = connection.createStatement()) {
            //Query string
            String queryString = "INSERT INTO IssueDescriptions (Machine_Issue, Troubleshooting_Steps, Part_Needed, Part_Code) VALUES " +
                    "('" + machineIssue + "' , '" + troubleshootingSteps + "' , '" + partNeeded + "','" + partCode + "');";
            //Execute query
            statement.executeUpdate(queryString);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        disconnect();
    }
}

