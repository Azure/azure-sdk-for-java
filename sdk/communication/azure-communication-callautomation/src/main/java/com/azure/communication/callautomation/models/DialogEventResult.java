// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.callautomation.models.events.DialogCompleted;
import com.azure.communication.callautomation.models.events.DialogConsent;
import com.azure.communication.callautomation.models.events.DialogFailed;
import com.azure.communication.callautomation.models.events.DialogHangup;
import com.azure.communication.callautomation.models.events.DialogLanguageChange;
import com.azure.communication.callautomation.models.events.DialogSensitivityUpdate;
import com.azure.communication.callautomation.models.events.DialogStarted;
import com.azure.communication.callautomation.models.events.DialogTransfer;

/**
 * The result of a dialog event.
 */
public final class DialogEventResult {
    private final boolean isSuccess;
    private final DialogCompleted dialogCompletedSuccessResult;
    private final DialogConsent dialogConsentSuccessResult;
    private final DialogFailed failureResult;
    private final DialogHangup dialogHangupSuccessResult;
    private final DialogStarted dialogStartedSuccessResult;
    private final DialogTransfer dialogTransferSuccessResult;
    private final DialogSensitivityUpdate dialogSensitivityUpdateResult;
    private final DialogLanguageChange dialogLanguageChangeResult;

    /**
     * Initializes a new instance of DialogEventResult.
     *
     * @param isSuccess the success status of the dialog operation.
     * @param dialogCompletedSuccessResult the dialog completed succeeded event.
     * @param dialogConsentSuccessResult the dialog consent succeeded event.
     * @param failureResult the dialog failed event.
     * @param dialogHangupSuccessResult the dialog hangup succeeded event.
     * @param dialogStartedSuccessResult the dialog started succeeded event.
     * @param dialogTransferSuccessResult the dialog transfer succeeded event.
     * @param dialogSensitivityUpdateResult the dialog sensitivity update event.
     * @param dialogLanguageChangeResult the dialog language change event.
     */
    DialogEventResult(boolean isSuccess, DialogCompleted dialogCompletedSuccessResult, DialogConsent dialogConsentSuccessResult, DialogFailed failureResult, DialogHangup dialogHangupSuccessResult, DialogStarted dialogStartedSuccessResult, DialogTransfer dialogTransferSuccessResult, DialogSensitivityUpdate dialogSensitivityUpdateResult, DialogLanguageChange dialogLanguageChangeResult) {
        this.isSuccess = isSuccess;
        this.dialogCompletedSuccessResult = dialogCompletedSuccessResult;
        this.dialogConsentSuccessResult = dialogConsentSuccessResult;
        this.failureResult = failureResult;
        this.dialogHangupSuccessResult = dialogHangupSuccessResult;
        this.dialogStartedSuccessResult = dialogStartedSuccessResult;
        this.dialogTransferSuccessResult = dialogTransferSuccessResult;
        this.dialogSensitivityUpdateResult = dialogSensitivityUpdateResult;
        this.dialogLanguageChangeResult = dialogLanguageChangeResult;
    }

    /**
     * Gets the success status of the dialog operation.
     *
     * @return the success status of the dialog operation.
     */
    public boolean isSuccess() {
        return isSuccess;
    }

    /**
     * Gets the dialog completed succeeded event.
     *
     * @return the dialog completed succeeded event.
     */
    public DialogCompleted getDialogCompletedSuccessResult() {
        return dialogCompletedSuccessResult;
    }

    /**
     * Gets the dialog consent succeeded event.
     *
     * @return the dialog consent succeeded event.
     */
    public DialogConsent getDialogConsentSuccessResult() {
        return dialogConsentSuccessResult;
    }

    /**
     * Gets the dialog failed event.
     *
     * @return the dialog failed event.
     */
    public DialogFailed getFailureResult() {
        return failureResult;
    }

    /**
     * Gets the dialog hangup succeeded event.
     *
     * @return the dialog hangup succeeded event.
     */
    public DialogHangup getDialogHangupSuccessResult() {
        return dialogHangupSuccessResult;
    }

    /**
     * Gets the dialog started succeeded event.
     *
     * @return the dialog started succeeded event.
     */
    public DialogStarted getDialogStartedSuccessResult() {
        return dialogStartedSuccessResult;
    }

    /**
     * Gets the dialog transfer succeeded event.
     *
     * @return the dialog transfer succeeded event.
     */
    public DialogTransfer getDialogTransferSuccessResult() {
        return dialogTransferSuccessResult;
    }

    /**
     * Gets the dialog sensitivity update event.
     *
     * @return the dialog sensitivity update event.
     */
    public DialogSensitivityUpdate getDialogSensitivityUpdateResult() {
        return dialogSensitivityUpdateResult;
    }

    /**
     * Gets the dialog language change event.
     *
     * @return the dialog language change event.
     */
    public DialogLanguageChange getDialogLanguageChangeResult() {
        return dialogLanguageChangeResult;
    }
}
