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
import com.microsoft.azure.servicebus.rules.CorrelationFilter;
import com.microsoft.azure.servicebus.rules.RuleDescription;
import com.microsoft.azure.servicebus.rules.SqlFilter;
import com.microsoft.azure.servicebus.rules.SqlRuleAction;

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
	
	public static Symbol getResponseErrorCondition(Message responseMessage)
	{
		return (Symbol)responseMessage.getApplicationProperties().getValue().get(ClientConstants.REQUEST_RESPONSE_ERROR_CONDITION);
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
	
	public static Map<String, Object> encodeRuleDescriptionToMap(RuleDescription ruleDescription)
	{
		HashMap<String, Object> descriptionMap = new HashMap<>();
		if(ruleDescription.getFilter() instanceof SqlFilter)
		{
			HashMap<String, Object> filterMap = new HashMap<>();
			filterMap.put(ClientConstants.REQUEST_RESPONSE_EXPRESSION, ((SqlFilter)ruleDescription.getFilter()).getSqlExpression());
			descriptionMap.put(ClientConstants.REQUEST_RESPONSE_SQLFILTER, filterMap);
		}
		else if(ruleDescription.getFilter() instanceof CorrelationFilter)
		{
			CorrelationFilter correlationFilter = (CorrelationFilter)ruleDescription.getFilter();
			HashMap<String, Object> filterMap = new HashMap<>();
			filterMap.put(ClientConstants.REQUEST_RESPONSE_CORRELATION_ID, correlationFilter.getCorrelationId());
			filterMap.put(ClientConstants.REQUEST_RESPONSE_MESSAGE_ID, correlationFilter.getMessageId());
			filterMap.put(ClientConstants.REQUEST_RESPONSE_TO, correlationFilter.getTo());
			filterMap.put(ClientConstants.REQUEST_RESPONSE_REPLY_TO, correlationFilter.getReplyTo());
			filterMap.put(ClientConstants.REQUEST_RESPONSE_LABEL, correlationFilter.getLabel());
			filterMap.put(ClientConstants.REQUEST_RESPONSE_SESSION_ID, correlationFilter.getSessionId());
			filterMap.put(ClientConstants.REQUEST_RESPONSE_REPLY_TO_SESSION_ID, correlationFilter.getReplyToSessionId());
			filterMap.put(ClientConstants.REQUEST_RESPONSE_CONTENT_TYPE, correlationFilter.getContentType());
			filterMap.put(ClientConstants.REQUEST_RESPONSE_CORRELATION_FILTER_PROPERTIES, correlationFilter.getProperties());		
			
			descriptionMap.put(ClientConstants.REQUEST_RESPONSE_CORRELATION_FILTER, filterMap);
		}
		else
		{
			throw new IllegalArgumentException("This API supports the addition of only SQLFilters and CorrelationFilters.");
		}
		
		if(ruleDescription.getAction() == null)
		{
			descriptionMap.put(ClientConstants.REQUEST_RESPONSE_SQLRULEACTION, null);
		}
		else if(ruleDescription.getAction() instanceof SqlRuleAction)
		{
			HashMap<String, Object> sqlActionMap = new HashMap<>();
			sqlActionMap.put(ClientConstants.REQUEST_RESPONSE_EXPRESSION, ((SqlRuleAction)ruleDescription.getAction()).getSqlExpression());
			descriptionMap.put(ClientConstants.REQUEST_RESPONSE_SQLRULEACTION, sqlActionMap);
		}
		else
		{
			throw new IllegalArgumentException("This API supports the addition of only filters with SqlRuleActions.");
		}
		
		return descriptionMap;
	}
}
