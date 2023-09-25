// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.core.util.Context;
import com.azure.communication.callautomation.models.DialogStateResult;
import com.azure.communication.callautomation.models.StartDialogOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;

/**
 * CallDialog.
 */
public final class CallDialog {

    /**
     * Instance of CallDialogAsync.
     */
    private final CallDialogAsync callDialogAsync;

    /**
     * Constructor.
     *
     * @param callDialogAsync callDialogAsync
     */
    CallDialog(CallDialogAsync callDialogAsync) {
        this.callDialogAsync = callDialogAsync;
    }

    /**
     * Start Dialog
     *
     * @param options A {@link StartDialogOptions} object containing different options for startDialog.
     * @param operationContext operationContext (pass null if not applicable)
     * @param context The context to associate with this operation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for successful startDialog request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<DialogStateResult> startDialogWithResponse(StartDialogOptions options, String operationContext, Context context) {
        return callDialogAsync.startDialogWithResponseInternal(options, operationContext, context).block();
    }

    /**
     * Stop Dialog
     *
     * @param dialogId The dialog id.
     * @param context The context to associate with this operation.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for successful stopDialog request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopDialogWithResponse(String dialogId, Context context) {
        return callDialogAsync.stopDialogWithResponseInternal(dialogId, context).block();
    }
}
