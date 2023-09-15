package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.AddParticipantResult;
import com.azure.communication.callautomation.models.DialogStateResult;
import com.azure.communication.callautomation.models.StartDialogOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;

public final class CallDialog {

    private final CallDialogAsync callDialogAsync;

    public CallDialog(CallDialogAsync callDialogAsync) {
        this.callDialogAsync = callDialogAsync;
    }

    /**
     * Start Dialog
     *
     * @param options A {@link StartDialogOptions} object containing different options for startDialog.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DialogStateResult> startDialog(StartDialogOptions options) {
       return callDialogAsync.startDialog(options).block();
    }

    /**
     * Stop Dialog
     *
     * @param dialogId A {@link StartDialogOptions} object containing different options for startDialog.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void stopDialog(String dialogId) {
        callDialogAsync.stopDialog(dialogId).block();
    }
}
