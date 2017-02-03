package com.microsoft.azure.servicebus.primitives;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;

import com.microsoft.azure.servicebus.amqp.AmqpErrorCode;

public class RequestResponseUtils {
	public static Message createRequestMessage(String operation, Map propertyBag, Duration timeout)
	{
		Message requestMessage = Message.Factory.create();
		requestMessage.setBody(new AmqpValue(propertyBag));
		HashMap applicationPropertiesMap = new HashMap();
		applicationPropertiesMap.put(ClientConstants.REQUEST_RESPONSE_OPERATION_NAME, operation);
		applicationPropertiesMap.put(ClientConstants.REQUEST_RESPONSE_TIMEOUT, timeout.toMillis());		
		requestMessage.setApplicationProperties(new ApplicationProperties(applicationPropertiesMap));
		return requestMessage;
	}
	
	// Pass one second less to the server so client doesn't time out before server times out
	public static Duration adjustServerTimeout(Duration clientTimeout)
	{
		return clientTimeout.minusSeconds(1);
	}
	
	public static int getResponseStatusCode(Message responseMessage)
	{
		int statusCode = ClientConstants.REQUEST_RESPONSE_UNDEFINED_STATUS_CODE;
		Object codeObject = responseMessage.getApplicationProperties().getValue().get(ClientConstants.REQUEST_RESPONSE_STATUS_CODE);
		if(codeObject != null)
		{
			statusCode = (int)codeObject;
		}
		
		return statusCode;
	}
	
	public static Map getResponseBody(Message responseMessage)
	{
		return (Map)((AmqpValue)responseMessage.getBody()).getValue();				
	}
	
	public static Exception genereateExceptionFromResponse(Message responseMessage)
	{
		Symbol errorCondition = (Symbol)responseMessage.getApplicationProperties().getValue().get(ClientConstants.REQUEST_RESPONSE_ERROR_CONDITION);
		Object statusDescription = responseMessage.getApplicationProperties().getValue().get(ClientConstants.REQUEST_RESPONSE_STATUS_DESCRIPTION);
		return generateExceptionFromError(errorCondition, statusDescription == null ? errorCondition.toString() : (String) statusDescription);
	}
	
	public static Exception generateExceptionFromError(Symbol errorCondition, String exceptionMessage)
	{	
		return ExceptionUtil.toException(new ErrorCondition(errorCondition, exceptionMessage));		
	}
}
