package com.microsoft.azure.eventhubs.impl;

public class RequestResponseCloser implements Operation<Void> {
    private FaultTolerantObject<RequestResponseChannel> innerChannel = null;

    public RequestResponseCloser() {
    }

    // innerChannel is not available when this object is constructed, have to set later
    public void setInnerChannel(final FaultTolerantObject<RequestResponseChannel> innerChannel) {
        this.innerChannel = innerChannel;
    }

    @Override
    public void run(OperationResult<Void, Exception> closeOperationCallback) {
        final RequestResponseChannel channelToBeClosed = this.innerChannel.unsafeGetIfOpened();
        if (channelToBeClosed == null) {
            closeOperationCallback.onComplete(null);
        } else {
            channelToBeClosed.close(new OperationResult<Void, Exception>() {
                @Override
                public void onComplete(Void result) {
                    closeOperationCallback.onComplete(result);
                }

                @Override
                public void onError(Exception error) {
                    closeOperationCallback.onError(error);
                }
            });
        }
    }
}
