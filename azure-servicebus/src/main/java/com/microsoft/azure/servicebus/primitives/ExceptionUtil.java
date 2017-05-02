/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
package com.microsoft.azure.servicebus.primitives;

import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

import com.microsoft.azure.servicebus.amqp.AmqpErrorCode;
import com.microsoft.azure.servicebus.amqp.AmqpException;

public final class ExceptionUtil
{
	static Exception toException(ErrorCondition errorCondition)
	{
		if (errorCondition == null)
		{
			throw new IllegalArgumentException("'null' errorCondition cannot be translated to ServiceBusException");
		}
		if (errorCondition.getCondition() == ClientConstants.TIMEOUT_ERROR)
		{
			return new TimeoutException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.SERVER_BUSY_ERROR)
		{
			return new ServerBusyException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.NotFound)
		{
			return new MessagingEntityNotFoundException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.ENTITY_DISABLED_ERROR)
		{
			return new MessagingEntityDisabledException(errorCondition.getDescription());
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
			return new QuotaExceededException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.MESSAGE_LOCK_LOST_ERROR)
		{
			return new MessageLockLostException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.SESSION_LOCK_LOST_ERROR)
		{
			return new SessionLockLostException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.SESSIONS_CANNOT_BE_LOCKED_ERROR)
		{
			return new SessionCannotBeLockedException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.MESSAGE_NOT_FOUND_ERROR)
		{
			return new MessageNotFoundException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.ENTITY_ALREADY_EXISTS_ERROR)
		{
			return new MessagingEntityAlreadyExistsException(errorCondition.getDescription());
		}

		return new ServiceBusException(ClientConstants.DEFAULT_IS_TRANSIENT, errorCondition.getDescription());
	}	

	static <T> void completeExceptionally(CompletableFuture<T> future, Exception exception, IErrorContextProvider contextProvider, boolean completeAsynchronously)
	{
		if (exception != null && exception instanceof ServiceBusException)
		{
			ErrorContext errorContext = contextProvider.getContext();
			((ServiceBusException) exception).setContext(errorContext);
		}

		if(completeAsynchronously)
		{
			AsyncUtil.completeFutureExceptionally(future, exception);
		}
		else
		{
			future.completeExceptionally(exception);
		}		
	}

	// not a specific message related error
	static boolean isGeneralError(Symbol amqpError)
	{
		return (amqpError == ClientConstants.SERVER_BUSY_ERROR 
				|| amqpError == ClientConstants.TIMEOUT_ERROR 
				|| amqpError == AmqpErrorCode.ResourceLimitExceeded);
	}

	static String getTrackingIDAndTimeToLog()
	{
		return String.format(Locale.US, "TrackingId: %s, at: %s", UUID.randomUUID().toString(), ZonedDateTime.now()); 
	}
	
	static String toStackTraceString(final Throwable exception, final String customErrorMessage)
	{
		final StringBuilder builder = new StringBuilder();
		
		if (!StringUtil.isNullOrEmpty(customErrorMessage))
		{
			builder.append(customErrorMessage);
			builder.append(System.lineSeparator());
		}
		
		builder.append(exception.getMessage());
		if (exception.getStackTrace() != null)
			for (StackTraceElement ste: exception.getStackTrace())
			{
				builder.append(System.lineSeparator());
				builder.append(ste.toString());
			}

		Throwable innerException = exception.getCause();
		if (innerException != null)
		{
			builder.append("Cause: " + innerException.getMessage());
			if (innerException.getStackTrace() != null)
				for (StackTraceElement ste: innerException.getStackTrace())
				{
					builder.append(System.lineSeparator());
					builder.append(ste.toString());
				}
		}
		
		return builder.toString();
	}

    public static Throwable extractAsyncCompletionCause(Throwable completionEx)
    {
    	if(completionEx instanceof CompletionException || completionEx instanceof ExecutionException)
    	{
    		return completionEx.getCause();
    	}
    	else
    	{
    		return completionEx;
    	}
    }
}
