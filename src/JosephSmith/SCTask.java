package JosephSmith;

public class SCTask {
    String taskNumber;
    String workNotes;
    String trackingNumber;

    public SCTask(String taskNumber, String trackingNumber, String workNotes){
        this.taskNumber = taskNumber;
        this.workNotes = workNotes;
        this.trackingNumber = trackingNumber;
    }

    public String getTaskNumber() {
        return taskNumber;
    }


    public String getWorkNotes() {
        return workNotes;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}
