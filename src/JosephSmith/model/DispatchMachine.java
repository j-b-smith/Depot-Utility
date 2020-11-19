package JosephSmith.model;

public class DispatchMachine {

    private String serviceTag;
    private String machineIssue;
    private String troubleshootingSteps;
    private String partNeeded;
    private String serialNumber;
    private String partCode;

    /**
     * @param serviceTag set the service tag
     * @param machineIssue set the machine issue
     * @param troubleshootingSteps set the troubleshooting steps
     * @param partNeeded set the part needed
     * @param serialNumber set the serial number
     * @param partCode set the part code
     */
    public DispatchMachine(String serviceTag, String machineIssue, String troubleshootingSteps, String partNeeded, String serialNumber, String partCode){
        this.serviceTag = serviceTag.toUpperCase();
        this.machineIssue = machineIssue;
        this.troubleshootingSteps = troubleshootingSteps;
        this.partNeeded = partNeeded;
        this.serialNumber = serialNumber;
        this.partCode = partCode;
    }

    /**
     * @return the Dispatch machines service tag
     */
    public String getServiceTag() {
        return serviceTag;
    }

    /**
     * @param serviceTag the service tag to set
     */
    public void setServiceTag(String serviceTag) {
        this.serviceTag = serviceTag;
    }

    /**
     * @return the Dispatch machines machine issue
     */
    public String getMachineIssue() {
        return machineIssue;
    }

    /**
     * @param machineIssue the machine issue to set
     */
    public void setMachineIssue(String machineIssue) {
        this.machineIssue = machineIssue;
    }

    /**
     * @return the Dispatch machines troubleshooting steps
     */
    public String getTroubleshootingSteps() {
        return troubleshootingSteps;
    }

    /**
     * @param troubleshootingSteps the troubleshooting steps to set
     */
    public void setTroubleshootingSteps(String troubleshootingSteps) {
        this.troubleshootingSteps = troubleshootingSteps;
    }

    /**
     * @return the Dispatch machines part needed
     */
    public String getPartNeeded() {
        return partNeeded;
    }

    /**
     * @param partNeeded the part needed to set
     */
    public void setPartNeeded(String partNeeded) {
        this.partNeeded = partNeeded;
    }

    /**
     * @return the Dispatch machines serial number
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    /**
     * @param serialNumber the seria number to set
     */
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    /**
     * @return the Dispatch machines part code
     */
    public String getPartCode() {
        return partCode;
    }

    /**
     * @param partCode the part code to set
     */
    public void setPartCode(String partCode) {
        this.partCode = partCode;
    }
}


