// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FaultTolerantObject<T extends IOObject> {

    private final Operation<T> openTask;
    private final Operation<Void> closeTask;
    private final Queue<OperationResult<T, Exception>> openCallbacks;
    private final Queue<OperationResult<Void, Exception>> closeCallbacks;

    private T innerObject;
    private boolean creatingNewInnerObject;
    private boolean closingInnerObject;

    public FaultTolerantObject(
            final Operation<T> openAsync,
            final Operation<Void> closeAsync) {

        this.openTask = openAsync;
        this.closeTask = closeAsync;
        this.openCallbacks = new ConcurrentLinkedQueue<>();
        this.closeCallbacks = new ConcurrentLinkedQueue<>();
    }

    // should be invoked from reactor thread
    T unsafeGetIfOpened() {

        if (innerObject != null && innerObject.getState() == IOObject.IOObjectState.OPENED) {
            return innerObject;
        }
        return null;
    }

    public void runOnOpenedObject(
            final ReactorDispatcher dispatcher,
            final OperationResult<T, Exception> openCallback) {

        try {
            dispatcher.invoke(new DispatchHandler() {
                @Override
                public void onEvent() {
                    if (!creatingNewInnerObject
                            && (innerObject == null || innerObject.getState() == IOObject.IOObjectState.CLOSED
                        || innerObject.getState() == IOObject.IOObjectState.CLOSING)) {
                        creatingNewInnerObject = true;

                        try {
                            openCallbacks.offer(openCallback);
                            openTask.run(new OperationResult<T, Exception>() {
                                @Override
                                public void onComplete(T result) {
                                    innerObject = result;
                                    for (OperationResult<T, Exception> callback : openCallbacks) {
                                        callback.onComplete(result);
                                    }

                                    openCallbacks.clear();
                                }

                                @Override
                                public void onError(Exception error) {
                                    for (OperationResult<T, Exception> callback : openCallbacks) {
                                        callback.onError(error);
                                    }

                                    openCallbacks.clear();
                                }
                            });
                        } finally {
                            creatingNewInnerObject = false;
                        }
                    } else if (innerObject != null && innerObject.getState() == IOObject.IOObjectState.OPENED) {
                        openCallback.onComplete(innerObject);
                    } else {
                        openCallbacks.offer(openCallback);
                    }
                }
            });
        } catch (IOException ioException) {
            openCallback.onError(ioException);
        }
    }

    public void close(
            final ReactorDispatcher dispatcher,
            final OperationResult<Void, Exception> closeCallback) {

        try {
            dispatcher.invoke(new DispatchHandler() {
                @Override
                public void onEvent() {
                    if (innerObject == null || innerObject.getState() == IOObject.IOObjectState.CLOSED) {
                        closeCallback.onComplete(null);
                    } else if (!closingInnerObject && (innerObject.getState() == IOObject.IOObjectState.OPENED || innerObject.getState() == IOObject.IOObjectState.OPENING)) {
                        closingInnerObject = true;
                        closeCallbacks.offer(closeCallback);
                        closeTask.run(new OperationResult<Void, Exception>() {
                            @Override
                            public void onComplete(Void result) {
                                closingInnerObject = false;
                                for (OperationResult<Void, Exception> callback : closeCallbacks) {
                                    callback.onComplete(result);
                                }
                                closeCallbacks.clear();
                            }

                            @Override
                            public void onError(Exception error) {
                                closingInnerObject = false;
                                for (OperationResult<Void, Exception> callback : closeCallbacks) {
                                    callback.onError(error);
                                }
                                closeCallbacks.clear();
                            }
                        });
                    } else {
                        closeCallbacks.offer(closeCallback);
                    }
                }
            });
        } catch (IOException ioException) {
            closeCallback.onError(ioException);
        }
    }
}
