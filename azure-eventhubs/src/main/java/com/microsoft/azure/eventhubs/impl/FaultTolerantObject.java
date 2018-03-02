/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.eventhubs.impl;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FaultTolerantObject<T extends IOObject> {

    final Operation<T> openTask;
    final Operation<Void> closeTask;
    final Queue<OperationResult<T, Exception>> openCallbacks;
    final Queue<OperationResult<Void, Exception>> closeCallbacks;

    T innerObject;
    boolean creatingNewInnerObject;
    boolean closingInnerObject;

    public FaultTolerantObject(
            final Operation<T> openAsync,
            final Operation<Void> closeAsync) {

        this.openTask = openAsync;
        this.closeTask = closeAsync;
        this.openCallbacks = new ConcurrentLinkedQueue<>();
        this.closeCallbacks = new ConcurrentLinkedQueue<>();
    }

    // should be invoked from reactor thread
    public T unsafeGetIfOpened() {

        if (innerObject != null && innerObject.getState() == IOObject.IOObjectState.OPENED)
            return innerObject;

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
                            && (innerObject == null || innerObject.getState() == IOObject.IOObjectState.CLOSED || innerObject.getState() == IOObject.IOObjectState.CLOSING)) {
                        creatingNewInnerObject = true;
                        openCallbacks.offer(openCallback);
                        openTask.run(new OperationResult<T, Exception>() {
                            @Override
                            public void onComplete(T result) {
                                creatingNewInnerObject = false;
                                innerObject = result;
                                for (OperationResult<T, Exception> callback : openCallbacks)
                                    callback.onComplete(result);

                                openCallbacks.clear();
                            }

                            @Override
                            public void onError(Exception error) {
                                creatingNewInnerObject = false;
                                for (OperationResult<T, Exception> callback : openCallbacks)
                                    callback.onError(error);

                                openCallbacks.clear();
                            }
                        });
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
                                for (OperationResult<Void, Exception> callback : closeCallbacks)
                                    callback.onComplete(result);

                                closeCallbacks.clear();
                            }

                            @Override
                            public void onError(Exception error) {
                                closingInnerObject = false;
                                for (OperationResult<Void, Exception> callback : closeCallbacks)
                                    callback.onError(error);

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
