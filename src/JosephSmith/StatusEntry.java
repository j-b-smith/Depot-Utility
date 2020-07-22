package JosephSmith;

public class StatusEntry{
    String serviceTag;
    String status;

    public StatusEntry(String serviceTag, String status){
        this.serviceTag = serviceTag;
        this.status = status;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public String getStatus() {
        return status;
    }
}
