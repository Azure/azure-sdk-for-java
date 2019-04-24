// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package com.microsoft.azure.servicebus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.microsoft.azure.servicebus.primitives.ServiceBusException;

public final class Utils {

    public static <T> T completeFuture(CompletableFuture<T> future) throws InterruptedException, ServiceBusException {
        try {
            return future.get();
        } catch (InterruptedException ie) {
            // Rare instance
            throw ie;
        } catch (ExecutionException ee) {
            Throwable cause = ee.getCause();
            if(cause instanceof RuntimeException)
            {
            	throw (RuntimeException)cause;
            }
            else if (cause instanceof Error)
            {
            	throw (Error)cause;
            }
            else if (cause instanceof ServiceBusException)
            {
                throw (ServiceBusException) cause;
            }
            else
            {
                throw new ServiceBusException(true, cause);
            }
        }
    }

    static void assertNonNull(String argumentName, Object argument) {
        if (argument == null)
            throw new IllegalArgumentException("Argument '" + argumentName + "' is null.");
    }
    
    static MessageBody fromSequence(List<Object> sequence)
    {
    	List<List<Object>> sequenceData = new ArrayList<>();
    	sequenceData.add(sequence);
    	return MessageBody.fromSequenceData(sequenceData);
    }
    
    static MessageBody fromBinay(byte[] binary)
    {
    	List<byte[]> binaryData = new ArrayList<>();
    	binaryData.add(binary);
    	return MessageBody.fromBinaryData(binaryData);
    }
    
    static byte[] getDataFromMessageBody(MessageBody messageBody)
    {
    	List<byte[]> binaryData = messageBody.getBinaryData();
		if(binaryData == null || binaryData.size() == 0)
		{
			return null;
		}
		else
		{
			return binaryData.get(0);
		}
    }
    
    static List<Object> getSequenceFromMessageBody(MessageBody messageBody)
    {
    	List<List<Object>> sequenceData = messageBody.getSequenceData();
		if(sequenceData == null || sequenceData.size() == 0)
		{
			return null;
		}
		else
		{
			return sequenceData.get(0);
		}
    }
}
