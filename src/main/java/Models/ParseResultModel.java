package Models;

import Interfaces.IParseResultModel;

import java.util.HashMap;

public class ParseResultModel implements IParseResultModel {


    private String Command;
    private TargetModel Target;
    private String naming;
    private String receiver;
    private int seqNum;
    private HashMap<String, Boolean> NeedClarification;
    private ClarificationModel clarificationModel;


    public ParseResultModel(String command, TargetModel target,
                            String naming, String receiver, int seqNum, ClarificationModel clarificationModel) {
        this.Command = command.toLowerCase();
        this.Target = target;
        this.naming = naming;
        this.receiver = receiver;
        this.seqNum = seqNum;
        this.clarificationModel = clarificationModel;

    }

    public int getSeqNum() {
        return seqNum;
    }

    public String getCommand() {
        return Command;
    }

    public void setCommand(String command) {
        Command = command;
    }

    public TargetModel getTarget() {
        return Target;
    }

    public void setTarget(TargetModel target) {
        Target = target;
    }

    public String getNaming() {
        return naming;
    }

    public void setNaming(String naming) {
        this.naming = naming;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setSeqNum(int seqNum) {
        this.seqNum = seqNum;
    }

    public HashMap<String, Boolean> getNeedClarification() {
        return NeedClarification;
    }

    public void setNeedClarification(HashMap<String, Boolean> needClarification) {
        NeedClarification = needClarification;
    }

    public ClarificationModel getClarificationModel() {
        return clarificationModel;
    }

    public void setClarificationModel(ClarificationModel clarificationModel) {
        this.clarificationModel = clarificationModel;
    }

}
