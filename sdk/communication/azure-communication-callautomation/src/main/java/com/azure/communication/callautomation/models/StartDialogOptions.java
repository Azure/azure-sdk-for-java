package com.azure.communication.callautomation.models;

import java.util.Map;

/**
 * The options for starting a dialog.
 */
public class StartDialogOptions {

    /*
     * Bot identifier.
     */
    private String botId;

    /*
     * The dialog ID.
     */
    private String dialogId;

    /*
     * Determines the type of the dialog.
     */
    private DialogInputType dialogInputType;

    /*
     * Dialog context.
     */
    private Map<String, Object> dialogContext;

    /*
     * The value to identify context of the operation.
     */
    private String operationContext;

    /**
     * Constructor
     *
     * @param botId bot identifier
     * @param dialogInputType type of dialog
     * @param dialogContext context of the dialog
     */
    public StartDialogOptions(String botId, DialogInputType dialogInputType, Map<String, Object> dialogContext) {
        this.botId = botId;
        this.dialogInputType = dialogInputType;
        this.dialogContext = dialogContext;
    }

    /**
     * Constructor that includes dialogId
     *
     * @param botId bot identifier
     * @param dialogId id of the dialog
     * @param dialogInputType type of dialog
     * @param dialogContext context of the dialog
     */
    public StartDialogOptions(String botId, String dialogId, DialogInputType dialogInputType, Map<String, Object> dialogContext) {
        this.botId = botId;
        this.dialogId = dialogId;
        this.dialogInputType = dialogInputType;
        this.dialogContext = dialogContext;
    }

    /**
     * Get the botAppId property: Bot identifier.
     *
     * @return the botAppId value.
     */
    public String getBotId() {
        return botId;
    }

    /**
     * Get the dialogId property: The dialog ID.
     *
     * @return the dialogId value.
     */
    public String getDialogId() {
        return dialogId;
    }

    /**
     * Get the dialogOptions property: Defines options for dialog.
     *
     * @return the dialogOptions value.
     */
    public DialogInputType getDialogInputType() {
        return dialogInputType;
    }

    /**
     * Get the dialogContext property: Dialog context.
     *
     * @return the dialogContext value.
     */
    public Map<String, Object> getDialogContext() {
        return dialogContext;
    }

    /**
     * Get the operationContext property: The value to identify context of the operation.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return operationContext;
    }

    /**
     * Set the operationContext property: The value to identify context of the operation.
     *
     * @param operationContext context of the operation
     */
    public void setOperationContext(String operationContext) {
        this.operationContext = operationContext;
    }
}
