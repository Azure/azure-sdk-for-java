// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.CallDialogsImpl;
import com.azure.communication.callautomation.implementation.accesshelpers.DialogStateResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.models.DialogOptions;
import com.azure.communication.callautomation.implementation.models.StartDialogRequestInternal;
import com.azure.communication.callautomation.models.DialogStateResult;
import com.azure.communication.callautomation.models.StartDialogOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * CallDialogAsync.
 */
public final class CallDialogAsync {

    private final CallDialogsImpl dialogsInternal;
    private final String callConnectionId;
    private final ClientLogger logger;

    /**
     * Constructor.
     *
     * @param callConnectionId callConnectionId the id of the call connection
     * @param callDialogInternal Internal implementation of CallDialog
     */
    CallDialogAsync(String callConnectionId, CallDialogsImpl callDialogInternal) {
        this.callConnectionId = callConnectionId;
        this.dialogsInternal = callDialogInternal;
        this.logger = new ClientLogger(CallDialogAsync.class);
    }

    /**
     * Start Dialog
     *
     * @param startDialogOptions startDialog options
     * @return Response for successful startDialog request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<DialogStateResult> startDialog(StartDialogOptions startDialogOptions) {
        return startDialogWithResponse(startDialogOptions)
            .flatMap(response -> Mono.just(response.getValue()));
    }

    /**
     * Start Dialog
     *
     * @param startDialogOptions startDialog options
     * @return Response for successful startDialog request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<DialogStateResult>> startDialogWithResponse(StartDialogOptions startDialogOptions) {
        return withContext(context -> startDialogWithResponseInternal(startDialogOptions, context));
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    Mono<Response<DialogStateResult>> startDialogWithResponseInternal(StartDialogOptions startDialogOptions, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            DialogOptions dialogOptions = new DialogOptions();
            dialogOptions.setDialogContext(startDialogOptions.getDialogContext());
            dialogOptions.setBotAppId(startDialogOptions.getBotId());

            StartDialogRequestInternal requestInternal = new StartDialogRequestInternal()
                .setDialogOptions(dialogOptions)
                .setOperationContext(startDialogOptions.getOperationContext());

            return dialogsInternal.startDialogWithResponseAsync(callConnectionId, startDialogOptions.getDialogId(), requestInternal, context).
                map(response -> new SimpleResponse<>(response, DialogStateResponseConstructorProxy.create(response.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Stop Dialog
     *
     * @param dialogId The dialog id.
     * @return Response for successful stopDialog request.
     */
    public Mono<Void> stopDialog(String dialogId) {
        return stopDialogWithResponse(dialogId).then();
    }

    /**
     * Sop Dialog
     *
     * @param dialogId The dialog id.
     * @return Response for successful stopDialog request.
     */
    public Mono<Response<Void>> stopDialogWithResponse(String dialogId) {
        return withContext(context -> stopDialogWithResponseInternal(dialogId, context));
    }

    Mono<Response<Void>> stopDialogWithResponseInternal(String dialogId, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            return dialogsInternal.stopDialogWithResponseAsync(callConnectionId, dialogId, context);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }
}
