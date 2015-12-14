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
		
		// enumerate all ExceptionTypes
		return null;
	}
}
