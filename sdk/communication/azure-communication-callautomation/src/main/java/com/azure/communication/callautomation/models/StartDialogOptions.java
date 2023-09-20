package com.azure.communication.callautomation.models;

import java.util.Map;

public class StartDialogOptions {

    private String botId;
    private String dialogId;
    private DialogInputType dialogInputType;
    private Map<String, Object> dialogContext;
    private String operationContext;

    public StartDialogOptions(String botId, DialogInputType dialogInputType, Map<String, Object> dialogContext) {
        this.botId = botId;
        this.dialogInputType = dialogInputType;
        this.dialogContext = dialogContext;
    }

    public StartDialogOptions(String botId, String dialogId, DialogInputType dialogInputType, Map<String, Object> dialogContext) {
        this.botId = botId;
        this.dialogId = dialogId;
        this.dialogInputType = dialogInputType;
        this.dialogContext = dialogContext;
    }

    public String getBotId() {
        return botId;
    }

    public String getDialogId() {
        return dialogId;
    }

    public DialogInputType getDialogInputType() {
        return dialogInputType;
    }

    public Map<String, Object> getDialogContext() {
        return dialogContext;
    }

    public String getOperationContext() {
        return operationContext;
    }

    public void setOperationContext(String operationContext) {
        this.operationContext = operationContext;
    }
}
