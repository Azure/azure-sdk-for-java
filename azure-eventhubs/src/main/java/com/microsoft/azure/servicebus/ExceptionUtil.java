package com.microsoft.azure.servicebus;

import java.util.concurrent.*;
import org.apache.qpid.proton.amqp.transport.*;
import com.microsoft.azure.servicebus.amqp.*;

final class ExceptionUtil
{

	static Exception toException(ErrorCondition errorCondition)
	{
		if (errorCondition == null)
		{
			throw new IllegalArgumentException("'null' errorCondition cannot be translated to ServiceBusException");
		}
		
		if (errorCondition.getCondition() == ClientConstants.TimeoutError)
		{
			return new TimeoutException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.ServerBusyError)
		{
			return new ServerBusyException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.NotFound)
		{
			return new IllegalEntityException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.EntityDisabledError)
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
			return ServiceBusException.create(false, new AmqpException(errorCondition));
		}
		else if (errorCondition.getCondition() == ClientConstants.ArgumentError)
		{
			return new IllegalArgumentException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.ArgumentOutOfRangeError)
		{
			// TODO: Is there a need to translate this back to errorcode? if so, add errorCode to error msg ?
			return new IllegalArgumentException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.NotImplemented)
		{
			// TODO: ideally this should have been ToBeImplementedException
			return new UnsupportedOperationException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.NotAllowed)
		{
			return new UnsupportedOperationException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.PartitionNotOwnedError)
		{
			return ServiceBusException.create(false, errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.StoreLockLostError)
		{
			return ServiceBusException.create(false, errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.AmqpLinkDetachForced)
		{
			return ServiceBusException.create(false, new AmqpException(errorCondition));
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.ResourceLimitExceeded)
		{
			return ServiceBusException.create(false, new AmqpException(errorCondition));
		}
		
		return ServiceBusException.create(ClientConstants.DefaultIsTransient, errorCondition.getDescription());
	}
}
