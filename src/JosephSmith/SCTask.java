package JosephSmith;

public class SCTask {
    String taskNumber;
    String customerName;
    String taskNotes;

    public SCTask(String taskNumber, String customerName, String taskNotes){
        this.taskNumber = taskNumber;
        this.customerName = customerName;
        this.taskNotes = taskNotes;
    }

    public String getTaskNumber() {
        return taskNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getTaskNotes() {
        return taskNotes;
    }
}
