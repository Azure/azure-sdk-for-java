// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FaultTolerantObject<T extends IOObject> {

    private final Operation<T> openTask;
    private final Operation<Void> closeTask;
    private final Queue<OperationResult<T, Exception>> openCallbacks;
    private final Queue<OperationResult<Void, Exception>> closeCallbacks;

    private static final Logger TRACE_LOGGER = LoggerFactory.getLogger(FaultTolerantObject.class);
    private final String instanceName = StringUtil.getRandomString("FTO");
    private final String clientId;
    private final String sessionName;

    private T innerObject;
    private final Object creatingSynchronizer = new Object();
    private volatile boolean creatingNewInnerObject;
    private final Object closingSynchronizer = new Object();
    private volatile boolean closingInnerObject;

    public FaultTolerantObject(
            final Operation<T> openAsync,
            final Operation<Void> closeAsync,
            final String clientId,
            final String sessionName) {

        this.openTask = openAsync;
        this.closeTask = closeAsync;
        this.openCallbacks = new ConcurrentLinkedQueue<>();
        this.closeCallbacks = new ConcurrentLinkedQueue<>();
        this.clientId = clientId;
        this.sessionName = sessionName;
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
                    boolean shouldCreateNewInnerObject = false;
                    synchronized (FaultTolerantObject.this.creatingSynchronizer) {
                        if (!FaultTolerantObject.this.creatingNewInnerObject
                            && (FaultTolerantObject.this.innerObject == null ||
                                FaultTolerantObject.this.innerObject.getState() == IOObject.IOObjectState.CLOSED ||
                                FaultTolerantObject.this.innerObject.getState() == IOObject.IOObjectState.CLOSING)) {
                                    if (TRACE_LOGGER.isInfoEnabled()) {
                                        TRACE_LOGGER.info(String.format(Locale.US,
                                            "FaultTolerantObject[%s] client[%s] session[%s] decided to create cNIO[%s] innerObject[%s] iOstate[%s]",
                                            FaultTolerantObject.this.instanceName, FaultTolerantObject.this.clientId, FaultTolerantObject.this.sessionName,
                                            FaultTolerantObject.this.creatingNewInnerObject ? "T" : "F",
                                            FaultTolerantObject.this.innerObject != null ? FaultTolerantObject.this.innerObject.getId() : "null",
                                            FaultTolerantObject.this.innerObject != null ? FaultTolerantObject.this.innerObject.getState().toString() : "--"));
                                    }
                                    shouldCreateNewInnerObject = true;
                                    FaultTolerantObject.this.creatingNewInnerObject = true;
                        }
                    }
                    if (shouldCreateNewInnerObject) {
                        try {
                            FaultTolerantObject.this.openCallbacks.offer(openCallback);
                            FaultTolerantObject.this.openTask.run(new OperationResult<T, Exception>() {
                                @Override
                                public void onComplete(T result) {
                                    FaultTolerantObject.this.innerObject = result;
                                    for (OperationResult<T, Exception> callback : FaultTolerantObject.this.openCallbacks) {
                                        callback.onComplete(result);
                                    }

                                    FaultTolerantObject.this.openCallbacks.clear();
                                    synchronized (FaultTolerantObject.this.creatingSynchronizer) {
                                        FaultTolerantObject.this.creatingNewInnerObject = false;
                                    }

                                    if (TRACE_LOGGER.isInfoEnabled()) {
                                        TRACE_LOGGER.info(String.format(Locale.US, "FaultTolerantObject[%s] client[%s] session[%s] inner object rrc[%s] creation complete",
                                            FaultTolerantObject.this.instanceName, FaultTolerantObject.this.clientId, FaultTolerantObject.this.sessionName,
                                            result.getId()));
                                    }
                                }

                                @Override
                                public void onError(Exception error) {
                                    for (OperationResult<T, Exception> callback : FaultTolerantObject.this.openCallbacks) {
                                        callback.onError(error);
                                    }

                                    FaultTolerantObject.this.openCallbacks.clear();
                                    synchronized (FaultTolerantObject.this.creatingSynchronizer) {
                                        FaultTolerantObject.this.creatingNewInnerObject = false;
                                    }
                                }
                            });
                        } catch (RuntimeException re) {
                            // Originally this was a finally clause, but when scheduling the creation succeeded, that was resetting
                            // creatingNewInnerObject while inner object creation was still in progress, causing a race that could
                            // result in multiple simultaneous creation attempts. Changed to reset the flag when scheduling fails,
                            // while still allowing the exception to bubble up. If the scheduling is successful, then the
                            // OperationResult above performs the reset when the creation succeeds or fails.
                            synchronized (FaultTolerantObject.this.creatingSynchronizer) {
                                FaultTolerantObject.this.creatingNewInnerObject = false;
                            }
                            throw re;
                        }
                    } else if (FaultTolerantObject.this.innerObject != null && FaultTolerantObject.this.innerObject.getState() == IOObject.IOObjectState.OPENED) {
                        openCallback.onComplete(innerObject);
                    } else {
                        FaultTolerantObject.this.openCallbacks.offer(openCallback);
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
                    if (FaultTolerantObject.this.innerObject == null ||
                        FaultTolerantObject.this.innerObject.getState() == IOObject.IOObjectState.CLOSED) {
                        closeCallback.onComplete(null);
                    } else {
                        boolean shouldClose = false;
                        synchronized (FaultTolerantObject.this.closingSynchronizer) {
                            if (!FaultTolerantObject.this.closingInnerObject &&
                                (FaultTolerantObject.this.innerObject.getState() == IOObject.IOObjectState.OPENED ||
                                FaultTolerantObject.this.innerObject.getState() == IOObject.IOObjectState.OPENING)) {
                                     shouldClose = true;
                                     FaultTolerantObject.this.closingInnerObject = true;
                            }
                        }
                        if (shouldClose) {
                            FaultTolerantObject.this.closeCallbacks.offer(closeCallback);
                            FaultTolerantObject.this.closeTask.run(new OperationResult<Void, Exception>() {
                                @Override
                                public void onComplete(Void result) {
                                    synchronized (FaultTolerantObject.this.closingSynchronizer) {
                                        FaultTolerantObject.this.closingInnerObject = false;
                                    }
                                    for (OperationResult<Void, Exception> callback : FaultTolerantObject.this.closeCallbacks) {
                                        callback.onComplete(result);
                                    }
                                    FaultTolerantObject.this.closeCallbacks.clear();
                                }

                                @Override
                                public void onError(Exception error) {
                                    synchronized (FaultTolerantObject.this.closingSynchronizer) {
                                        FaultTolerantObject.this.closingInnerObject = false;
                                    }
                                    for (OperationResult<Void, Exception> callback : FaultTolerantObject.this.closeCallbacks) {
                                        callback.onError(error);
                                    }
                                    FaultTolerantObject.this.closeCallbacks.clear();
                                }
                            });
                        } else {
                            FaultTolerantObject.this.closeCallbacks.offer(closeCallback);
                        }
                    }
                }
            });
        } catch (IOException ioException) {
            closeCallback.onError(ioException);
        }
    }
}
