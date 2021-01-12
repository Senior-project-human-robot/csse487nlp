package Models;

public class ClarificationModel {
    private boolean needCommand;
    private boolean needTarget;
    private boolean needReference;

    public ClarificationModel(boolean needCommand, boolean needTarget, boolean needReference) {
        this.needCommand = needCommand;
        this.needTarget = needTarget;
        this.needReference = needReference;
    }
    public boolean isNeedCommand() {
        return needCommand;
    }

    public void setNeedCommand(boolean needCommand) {
        this.needCommand = needCommand;
    }

    public boolean isNeedTarget() {
        return needTarget;
    }

    public void setNeedTarget(boolean needTarget) {
        this.needTarget = needTarget;
    }

    public boolean isNeedReference() {
        return needReference;
    }

    public void setNeedReference(boolean needReference) {
        this.needReference = needReference;
    }

}
