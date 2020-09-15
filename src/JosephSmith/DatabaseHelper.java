package JosephSmith;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseHelper {
    Connection connection = null;

    //Constructor
    public DatabaseHelper(){}

    //Connect to SQLExpress database
    public void connect(){
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(localSQLServer, localUser, localPass);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    //Close database connection
    public void closeConnection(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    //Convert the log entry to a string for filtering
    public String logEntryToString(LogEntry entry){
        return entry.date + " " + entry.requestNumber + " " + entry.serviceTag + " " + entry.model + " " + entry.machineIssue + " " + entry.partNeeded;
    }

    /*
    Get cell value by value
     */
    public String getCellValue(String returnColumn, String tableName, String sortColumn, String sortValue) {
        //Increment index for database use
        String value = null;

        try(Statement statement = connection.createStatement()) {
            //Get result set

            ResultSet tempValue = statement.executeQuery
                    ("SELECT " + returnColumn + " FROM " + tableName + " WHERE " + sortColumn + "= '" + sortValue + "';");
            //Move cursor to first position
            tempValue.next();

            //Get cell value as string
            value = tempValue.getString(returnColumn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return value;
    }

    /*
    Get machines with multiple issues
    */
    public ArrayList<WarrantyMachine> getDuplicateMachines(String serviceTag) {

        ArrayList<WarrantyMachine> duplicateMachines = new ArrayList<>();

        try(Statement statement = connection.createStatement()) {
            //Get result set
            ResultSet tempValue = statement.executeQuery
                    ("SELECT * FROM WarrantyMachines WHERE Service_Tag = '" + serviceTag + "';");

            //Loop through rows retuned by result set
            while (tempValue.next()) {
                //Get cell value as string
                String machineIssue = tempValue.getString("Machine_issue");
                String troubleshootingSteps = tempValue.getString("Troubleshooting_Steps");
                String partNeeded = tempValue.getString("Part_Needed");
                String serialNumber = tempValue.getString("Serial_Number");

                //Create WarrantyMachine object
                WarrantyMachine warrantyMachine = new WarrantyMachine(serviceTag, machineIssue, troubleshootingSteps, partNeeded, serialNumber);

                //Add warranty machine to list
                duplicateMachines.add(warrantyMachine);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
        return duplicateMachines;
    }

    /*
    Get machines with multiple issues
    */
    public ObservableList<WarrantyMachine> getWarrantyMachines() {

        ArrayList<WarrantyMachine> warrantyMachines = new ArrayList<>();

        try(Statement statement = connection.createStatement()) {
            //Get result set
            ResultSet tempValue = statement.executeQuery
                    ("SELECT * FROM WarrantyMachines;");

            //Loop through rows retuned by result set
            while (tempValue.next()) {

                WarrantyMachine warrantyMachine = new WarrantyMachine(tempValue.getString("Service_Tag"),
                                                                    tempValue.getString("Machine_Issue"),
                                                                    tempValue.getString("Troubleshooting_Steps"),
                                                                    tempValue.getString("Part_Needed"),
                                                                    tempValue.getString("Serial_Number"));
                //Add warranty machine to list
                warrantyMachines.add(warrantyMachine);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return FXCollections.observableArrayList(warrantyMachines);
    }

    /*
    Get log machines
    */
    public ObservableList<LogEntry> getLogMachines() {

        ArrayList<LogEntry> logMachines = new ArrayList<>();

        try(Statement statement = connection.createStatement()) {
            //Get result set
            ResultSet tempValue = statement.executeQuery
                    ("SELECT * FROM MachineLog;");

            //Loop through rows retuned by result set
            while (tempValue.next()) {

                LogEntry logMachine = new LogEntry (tempValue.getString("Date"),
                                                tempValue.getString("Request_Number"),
                                                tempValue.getString("Service_Tag"),
                                                tempValue.getString("Model"),
                                                tempValue.getString("Machine_Issue"),
                                                tempValue.getString("Part_Needed"));

                //Add warranty machine to list
                logMachines.add(logMachine);
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }

        return FXCollections.observableArrayList(logMachines);
    }

    /*
    Get number of rows that contain a specific value
     */
    public int getValueCount(String tableName, String column, String value){
        int count = 0;
        try (Statement statement = connection.createStatement()){
            //Get Result set
            ResultSet countSet = statement.
                    executeQuery("SELECT COUNT(*) AS count FROM " + tableName + " WHERE " + column +  "= '" + value + "';");
            //Move cursor to first position
            countSet.next();

            //Get count value
            count = countSet.getInt("count");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /*
    Add new row to table
     */
    public void addNewRowToWarrantyMachines(WarrantyMachine machine) {


        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO WarrantyMachines (Service_Tag, Machine_Issue, Troubleshooting_Steps, Part_Needed) " +
                    "VALUES ('" + machine.serviceTag + "' , '" + machine.machineIssue + "' , '"
                    + machine.troubleshootingSteps + "' , '" + machine.partNeeded + "' );");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addNewRowToAlertLog(String serviceTag, String alertMessage ){

        //Create date format and get current date
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/YYYY");
        Date currentDate = new Date();
        String formattedDate = formatter.format(currentDate);

        try(Statement statement = connection.createStatement()){
            statement.executeUpdate("INSERT INTO AlertLog (Date, Service_Tag, Alert_Message) " +
                    "VALUES ('" + formattedDate + "' , '" + serviceTag + "' , '" + alertMessage + "');");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void addNewRowToMachineLog(String requestNumber, String serviceTag , String model, String machineIssue, String partNeeded ){

        //Create date format and get current date
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/YYYY");
        Date currentDate = new Date();
        String formattedDate = formatter.format(currentDate);

        try(Statement statement = connection.createStatement()){
            statement.executeUpdate("INSERT INTO MachineLog (Date, Request_Number, Service_Tag, Model, Machine_Issue, Part_Needed) " +
                    "VALUES ('" + formattedDate + "' , '" + requestNumber + "' , '" + serviceTag + "' , '" + model +
                    "' , '" + machineIssue + "' , '" + partNeeded + "' ); ");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void addNewRowToIssueDescriptions( String machineIssue, String troubleshootingSteps, String partNeeded ){
        if (machineIssue != null && troubleshootingSteps != null && partNeeded != null) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO IssueDescriptions (Machine_Issue, Troubleshooting_Steps, Part_Needed) VALUES " +
                        "('" + machineIssue + "' , '" + troubleshootingSteps + "' , '" + partNeeded + "');");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    remove rows from warranty machine table by list of machines
     */
    public void removeRowsFromWarrantyMachines(ObservableList<WarrantyMachine> list){

        //reIndexTable("WarrantyMachines");

        for (WarrantyMachine machine : list) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM WarrantyMachines WHERE Service_Tag ='" + machine.serviceTag + "';");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPartDescription(String model, String part){

        //Format model to match table columns
       model = model.replace(" ", "_");
       model = model.replace("-", "_");

       String value = null;

        try (Statement statement = connection.createStatement()){
            //Get Result set
            ResultSet queryResult = statement.
                    executeQuery("SELECT " + model + " FROM PartDescriptions WHERE Part_Needed = '" + part + "';");
            //Move cursor to first position
            queryResult.next();

            value = queryResult.getString(model);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return value;
    }

    /*
    Create lists from tables
     */
    public ArrayList<String> createListFromColumn(String column, String tableName){

        //Create array list to return values
        ArrayList<String> columnList = new ArrayList<>();

        //Create statement and retrieve result set
        Statement statement = null;
        try {
           statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ResultSet resultSet = null;
        try {

            //Prevent executeQuery nullPointerException
            assert statement != null;
            resultSet = statement.executeQuery("SELECT " + column + " FROM " + tableName + " ORDER BY " + column + " ASC ;");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Loop through result set and add to arraylist
        while (true) {
            try {

                //Prevent next() nullPointerException
                assert resultSet != null;
                if (!resultSet.next()) break;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //Convert database information to string value
            try {
                columnList.add(resultSet.getString(column));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return columnList;
    }

    public ArrayList<String> createUniqueValueList(String column, String tableName){

        //Create array list to return values
        ArrayList<String> valueList = new ArrayList<>();

        //Create statement and retrieve result set
        Statement statement = null;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        ResultSet resultSet = null;
        try {

            //Prevent executeQuery nullPointerException
            assert statement != null;
            resultSet = statement.executeQuery("SELECT DISTINCT " + column + " FROM " + tableName + ";");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //Loop through result set and add to arraylist
        while (true) {
            try {

                //Prevent next() nullPointerException
                assert resultSet != null;
                if (!resultSet.next()) break;
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //Convert database information to string value
            try {
                valueList.add(resultSet.getString(column));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return valueList;
    }
}

