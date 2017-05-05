package com.microsoft.azure.servicebus.amqp;

import com.microsoft.azure.servicebus.FaultTolerantObject;

public class RequestResponseCloser implements IOperation<Void> {
	private FaultTolerantObject<RequestResponseChannel> innerChannel = null;
	
	public RequestResponseCloser() {
	}

	// innerChannel is not available when this object is constructed, have to set later
	public void setInnerChannel(final FaultTolerantObject<RequestResponseChannel> innerChannel)	{
		this.innerChannel = innerChannel;
	}
	
    @Override
    public void run(IOperationResult<Void, Exception> closeOperationCallback) {
        final RequestResponseChannel channelToBeClosed = this.innerChannel.unsafeGetIfOpened();
        if (channelToBeClosed == null) {
            closeOperationCallback.onComplete(null);
        }
        else {
            channelToBeClosed.close(new IOperationResult<Void, Exception>() {
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
