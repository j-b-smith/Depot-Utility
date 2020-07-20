package JosephSmith;

public class WarrantyMachine {

    String serviceTag;
    String machineIssue;
    String troubleshootingSteps;
    String partNeeded;
    String batterySerialNumber;

    public WarrantyMachine(String serviceTag, String machineIssue, String troubleshootingSteps, String partNeeded){
        this.serviceTag = serviceTag.toUpperCase();
        this.machineIssue = machineIssue;
        this.troubleshootingSteps = troubleshootingSteps;
        this.partNeeded = partNeeded;
    }

    public WarrantyMachine(String serviceTag, String machineIssue, String troubleshootingSteps, String partNeeded, String batterySerialNumber){
        this.serviceTag = serviceTag;
        this.machineIssue = machineIssue;
        this.troubleshootingSteps = troubleshootingSteps;
        this.partNeeded = partNeeded;
        this.batterySerialNumber = batterySerialNumber;
    }

    public String toString() {
        if (this.batterySerialNumber != null) {
            return this.serviceTag + " | " + this.machineIssue + " | " + this.troubleshootingSteps + " | " + this.partNeeded + " | " + this.batterySerialNumber;
        } else
            return this.serviceTag + " | " + this.machineIssue + " | " + this.troubleshootingSteps + " | " + this.partNeeded;
    }
}
