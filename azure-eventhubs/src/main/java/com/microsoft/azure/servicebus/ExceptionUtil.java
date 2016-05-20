/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

import com.microsoft.azure.servicebus.amqp.AmqpErrorCode;
import com.microsoft.azure.servicebus.amqp.AmqpException;

final class ExceptionUtil
{
	static Exception toException(ErrorCondition errorCondition)
	{
		if (errorCondition == null)
		{
			throw new IllegalArgumentException("'null' errorCondition cannot be translated to ServiceBusException");
		}

		if (errorCondition.getCondition() == ClientConstants.TIMEOUT_ERROR)
		{
			return new ServiceBusException(ClientConstants.DEFAULT_IS_TRANSIENT, new TimeoutException(errorCondition.getDescription()));
		}
		else if (errorCondition.getCondition() == ClientConstants.SERVER_BUSY_ERROR)
		{
			return new ServerBusyException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.NotFound)
		{
			return new IllegalEntityException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.ENTITY_DISABLED_ERROR)
		{
			return new IllegalEntityException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.Stolen)
		{
			return new ReceiverDisconnectedException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.UnauthorizedAccess)
		{
			return new AuthorizationFailedException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.PayloadSizeExceeded)
		{
			return new PayloadSizeExceededException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.InternalError)
		{
			return new ServiceBusException(true, new AmqpException(errorCondition));
		}
		else if (errorCondition.getCondition() == ClientConstants.ARGUMENT_ERROR)
		{
			return new ServiceBusException(false, errorCondition.getDescription(), new AmqpException(errorCondition));
		}
		else if (errorCondition.getCondition() == ClientConstants.ARGUMENT_OUT_OF_RANGE_ERROR)
		{
			return new ServiceBusException(false, errorCondition.getDescription(), new AmqpException(errorCondition));
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.NotImplemented)
		{
			return new UnsupportedOperationException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.NotAllowed)
		{
			return new UnsupportedOperationException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.PARTITION_NOT_OWNED_ERROR)
		{
			return new ServiceBusException(false, errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.STORE_LOCK_LOST_ERROR)
		{
			return new ServiceBusException(false, errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.AmqpLinkDetachForced)
		{
			return new ServiceBusException(false, new AmqpException(errorCondition));
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.ResourceLimitExceeded)
		{
			return new ServiceBusException(false, new AmqpException(errorCondition));
		}

		return new ServiceBusException(ClientConstants.DEFAULT_IS_TRANSIENT, errorCondition.getDescription());
	}

	static <T> void completeExceptionally(CompletableFuture<T> future, Exception exception, IErrorContextProvider contextProvider)
	{
		if (exception != null && exception instanceof ServiceBusException)
		{
			ErrorContext errorContext = contextProvider.getContext();
			((ServiceBusException) exception).setContext(errorContext);
		}

		future.completeExceptionally(exception);
	}

	// not a specific message related error
	static boolean isGeneralSendError(Symbol amqpError)
	{
		return (amqpError == ClientConstants.SERVER_BUSY_ERROR 
				|| amqpError == ClientConstants.TIMEOUT_ERROR 
				|| amqpError == AmqpErrorCode.ResourceLimitExceeded);
	}

	static String getTrackingIDAndTimeToLog()
	{
		return String.format(Locale.US, "TrackingId: %s, at: %s", UUID.randomUUID().toString(), ZonedDateTime.now()); 
	}
}
