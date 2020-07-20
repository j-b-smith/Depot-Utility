package JosephSmith;

public class LogEntry {
    String date;
    String requestNumber;
    String serviceTag;
    String model;
    String machineIssue;
    String partNeeded;

    public LogEntry(String date, String requestNumber, String serviceTag, String model, String machineIssue, String partNeeded ){
        this.date = date;
        this.requestNumber = requestNumber;
        this.serviceTag = serviceTag;
        this.model = model;
        this.machineIssue = machineIssue;
        this.partNeeded = partNeeded;
    }

    //Accessor methods
    public String getDate(){
        return date;
    }
    public String getRequestNumber(){
        return requestNumber;
    }
    public String getModel(){
        return model;
    }
    public String getServiceTag(){
        return serviceTag;
    }
    public String getMachineIssue(){
        return machineIssue;
    }
    public String getPartNeeded(){
        return partNeeded;
    }

    //Mutator methods
    public void setDate(String date){
        this.date = date;
    }
    public void setRequestNumber(String requestNumber){
        this.requestNumber = requestNumber;
    }
    public void setModel(String model){
        this.model = model;
    }
    public void setServiceTag(String serviceTag){
        this.serviceTag = serviceTag;
    }
    public void setMachineIssue(String machineIssue){
        this.machineIssue = machineIssue;
    }
    public void setPartNeeded(String partNeeded){
        this.partNeeded = partNeeded;
    }
}
