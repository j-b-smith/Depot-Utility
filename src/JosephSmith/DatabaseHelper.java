package JosephSmith;

import javafx.collections.ObservableList;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DatabaseHelper {
    Connection connection = null;

    String test = "This is a version control test";

    //Constructor
    public DatabaseHelper(){}

    //Connect to SQLite database
    public void connect(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:C:\\Users\\jsmit\\Documents\\dellAutomate.sqlite");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public LogEntry getLogRow(int index){

        return new LogEntry(getCellValue("Date", "LogSheet", index),
                (getCellValue("Request_Number", "LogSheet", index)),
                (getCellValue("Service_Tag", "LogSheet", index)),
                (getCellValue("Model", "LogSheet", index)),
                (getCellValue("Machine_Issue", "LogSheet", index)),
                (getCellValue("Part_Needed", "LogSheet", index)));
    }

    public String logEntryToString(LogEntry entry){
        return entry.date + " " + entry.requestNumber + " " + entry.serviceTag + " " + entry.model + " " + entry.machineIssue + " " + entry.partNeeded;
    }

    public int getRowCount(String tableName){
        int count = 0;
        try (Statement statement = connection.createStatement()){
            //Get Result set
            ResultSet countSet = statement.
                    executeQuery("SELECT COUNT(*) AS count FROM " + tableName + ";");
            //Move cursor to first position
            countSet.next();

            //Get count value
            count = countSet.getInt("count");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }


    public String getCellValue(String columnName, String tableName, int index) {
        //Increment index for database use
        index +=1;
        String value = null;

        try(Statement statement = connection.createStatement()) {
            //Get result set

            ResultSet tempValue = statement.executeQuery
                    ("SELECT " + columnName + " FROM " + tableName + " WHERE rowid = " + index + ";");
            //Move cursor to first position
            tempValue.next();

            //Get cell value as string
            value = tempValue.getString(columnName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return value;
    }
    /*
    Get number of rows that contain a specific value
     */
    public int getValueCount(String tableName, String column, String value){
        int count = 0;
        try (Statement statement = connection.createStatement()){
            //Get Result set
            ResultSet countSet = statement.
                    executeQuery("SELECT COUNT(*) AS count FROM " + tableName + " WHERE " + column +  "= \"" + value + "\";");
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
    Add and Delete tables
     */
    public void createNewTable(String tableName, String columnName){

        try (Statement statement = connection.createStatement()){
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS "  + tableName + " (" + columnName +  " text NOT NULL);");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void clearTable(String tableName){

        try(Statement statement = connection.createStatement()){
            statement.executeUpdate("DELETE FROM " + tableName + ";");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    /*
    Add new column
     */

    public void addNewColumn(String tableName, String columnName){

        try(Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE " + tableName + " ADD " + columnName + " VARCHAR;");
        } catch (SQLException e){
            e.printStackTrace();
        }

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

    public void addNewRowToWarrantyMachinesBattery(WarrantyMachine machine) {

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO WarrantyMachines (Service_Tag, Machine_Issue, Troubleshooting_Steps, Part_Needed, Battery_Serial_Number) " +
                    "VALUES ('" + machine.serviceTag + "' , '" + machine.machineIssue + "' , '"
                    + machine.troubleshootingSteps + "' , '" + machine.partNeeded + "' , '" + machine.batterySerialNumber + "');");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addNewRowToAlertSheet(String serviceTag, String alertMessage ){

        //Create date format and get current date
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/YYYY");
        Date currentDate = new Date();
        String formattedDate = formatter.format(currentDate);

        try(Statement statement = connection.createStatement()){
            statement.executeUpdate("INSERT INTO AlertSheet (Date, Service_Tag, Alert_Message) " +
                    "VALUES ('" + formattedDate + "' , '" + serviceTag + "' , '" + alertMessage + "');");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void addNewRowToLogSheet(String requestNumber, String serviceTag , String model, String machineIssue, String partNeeded ){

        //Create date format and get current date
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/YYYY");
        Date currentDate = new Date();
        String formattedDate = formatter.format(currentDate);

        try(Statement statement = connection.createStatement()){
            statement.executeUpdate("INSERT INTO LogSheet (Date, Request_Number, Service_Tag, Model, Machine_Issue, Part_Needed) " +
                    "VALUES ('" + formattedDate + "' , '" + requestNumber + "' , '" + serviceTag + "' , '" + model +
                    "' , '" + machineIssue + "' , '" + partNeeded + "' ); ");
        } catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void addNewRowToDescriptionSheet( String machineIssue, String troubleshootingSteps, String partNeeded ){
        if (machineIssue != null && troubleshootingSteps != null && partNeeded != null) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("INSERT INTO DescriptionSheet (Machine_Issue, Troubleshooting_Steps, Part_Needed) VALUES " +
                        "('" + machineIssue + "' , '" + troubleshootingSteps + "' , '" + partNeeded + "');");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    /*
    remove row from warranty machine table by list of machines
     */
    public void removeRowsFromWarrantyMachines(ObservableList<WarrantyMachine> list){

        for (WarrantyMachine machine : list) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM WarrantyMachines WHERE (Service_Tag ='" + machine.serviceTag + "' AND Machine_Issue ='"
                        + machine.machineIssue + "' AND Part_Needed ='" + machine.partNeeded + "');");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
            resultSet = statement.executeQuery("SELECT " + column + " FROM " + tableName + ";");
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

