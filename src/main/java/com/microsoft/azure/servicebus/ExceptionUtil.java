package com.microsoft.azure.servicebus;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;

final class ExceptionUtil {

	static ServiceBusException toException(ErrorCondition errorCondition) {

		if (errorCondition == null) {
			throw new IllegalArgumentException("'null' errorCondition cannot be translated to ServiceBusException");
		}
		
		if (errorCondition.getCondition() == AmqpErrorCode.NotFound) {
			return new EntityNotFoundException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.Stolen) {
			return new ReceiverDisconnectedException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.UnauthorizedAccess) {
			return new AuthorizationFailedException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == ClientConstants.ServerBusyError) {
			return new ServerBusyException(errorCondition.getDescription());
		}
		else if (errorCondition.getCondition() == AmqpErrorCode.PayloadSizeExceeded) {
			return new PayloadSizeExceededException(errorCondition.getDescription());
		}
		
		// TODO: enumerate all ExceptionTypes
		return null;
	}
}
