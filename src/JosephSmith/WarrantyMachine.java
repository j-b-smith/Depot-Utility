package JosephSmith;

public class WarrantyMachine {

    String serviceTag;
    String machineIssue;
    String troubleshootingSteps;
    String partNeeded;
    String serialNumber;

    public WarrantyMachine(String serviceTag, String machineIssue, String troubleshootingSteps, String partNeeded){
        this.serviceTag = serviceTag.toUpperCase();
        this.machineIssue = machineIssue;
        this.troubleshootingSteps = troubleshootingSteps;
        this.partNeeded = partNeeded;

    }

    public WarrantyMachine(String serviceTag, String machineIssue, String troubleshootingSteps, String partNeeded, String serialNumber){
        this.serviceTag = serviceTag.toUpperCase();
        this.machineIssue = machineIssue;
        this.troubleshootingSteps = troubleshootingSteps;
        this.partNeeded = partNeeded;
        this.serialNumber = serialNumber;


    }


    public String toString() {
        if (this.serialNumber != null) {
            return this.serviceTag + " | " + this.machineIssue + " | " + this.troubleshootingSteps + " | " + this.partNeeded + " | " + this.serialNumber;
        } else
            return this.serviceTag + " | " + this.machineIssue + " | " + this.troubleshootingSteps + " | " + this.partNeeded;
    }

    public String getServiceTag() {
        return serviceTag;
    }

    public String getMachineIssue() {
        return machineIssue;
    }

    public String getTroubleshootingSteps() {
        return troubleshootingSteps;
    }

    public String getPartNeeded() {
        return partNeeded;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
}


