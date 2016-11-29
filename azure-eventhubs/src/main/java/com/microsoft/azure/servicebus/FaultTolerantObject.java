/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.microsoft.azure.servicebus.amqp.DispatchHandler;
import com.microsoft.azure.servicebus.amqp.IIOObject;
import com.microsoft.azure.servicebus.amqp.IOperation;
import com.microsoft.azure.servicebus.amqp.IOperationResult;
import com.microsoft.azure.servicebus.amqp.ReactorDispatcher;

public class FaultTolerantObject<T extends IIOObject> {
    
    final IOperation<T> openTask;
    final IOperation<Void> closeTask;
    final Queue<IOperationResult<T, Exception>> openCallbacks;
    final Queue<IOperationResult<Void, Exception>> closeCallbacks;

    T innerObject;
    
    public FaultTolerantObject(
        final IOperation<T> openAsync,
        final IOperation<Void> closeAsync) {
        
        this.openTask = openAsync;
        this.closeTask = closeAsync;
        this.openCallbacks = new ConcurrentLinkedQueue<>();
        this.closeCallbacks = new ConcurrentLinkedQueue<>();
    }
    
    // should be invoked from reactor thread
    public T unsafeGetIfOpened() {

        if (innerObject != null && innerObject.getState() == IIOObject.IOObjectState.OPENED)
            return innerObject;
        
        return null;
    }
    
    public void runOnOpenedObject(
        final ReactorDispatcher dispatcher,
        final IOperationResult<T, Exception> openCallback) {
        
        try {
            dispatcher.invoke(new DispatchHandler() {
                @Override
                public void onEvent() {
                    if (innerObject == null || innerObject.getState() == IIOObject.IOObjectState.CLOSED) {
                        openCallbacks.offer(openCallback);
                        openTask.run(new IOperationResult<T, Exception>() {
                            @Override
                            public void onComplete(T result) {
                                innerObject = result;
                                for (IOperationResult<T, Exception> callback: openCallbacks)
                                    callback.onComplete(result);
                                
                                openCallbacks.clear();
                            }
                            @Override
                            public void onError(Exception error) {
                                for (IOperationResult<T, Exception> callback: openCallbacks)
                                    callback.onError(error);
                                
                                openCallbacks.clear();
                            }
                        });
                    }
                    else if (innerObject.getState() == IIOObject.IOObjectState.OPENED) {
                        openCallback.onComplete(innerObject);
                    }
                    else {
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
            final IOperationResult<Void, Exception> closeCallback) {
        
        try {
            dispatcher.invoke(new DispatchHandler() {
                @Override
                public void onEvent() {
                    if (innerObject == null || innerObject.getState() == IIOObject.IOObjectState.CLOSED) {
                        closeCallback.onComplete(null);
                    } 
                    else if (innerObject.getState() == IIOObject.IOObjectState.OPENING) {
                        closeCallbacks.offer(closeCallback);
                    } 
                    else if (innerObject.getState() == IIOObject.IOObjectState.OPENED) {
                        closeCallbacks.offer(closeCallback);
                        closeTask.run(new IOperationResult<Void, Exception>() {
                            @Override
                            public void onComplete(Void result) {
                                for (IOperationResult<Void, Exception> callback: closeCallbacks)
                                    callback.onComplete(result);
                                
                                closeCallbacks.clear();
                            }

                            @Override
                            public void onError(Exception error) {
                                for (IOperationResult<Void, Exception> callback: closeCallbacks)
                                    callback.onError(error);
                                
                                closeCallbacks.clear();
                            }
                        });
                    }
                }
            });
        } catch (IOException ioException) {
            closeCallback.onError(ioException);
        }
    }
}
