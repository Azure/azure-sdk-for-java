package com.microsoft.azure.servicebus.primitives;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.azure.servicebus.rules.*;
import org.apache.qpid.proton.amqp.DescribedType;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.amqp.transport.ErrorCondition;
import org.apache.qpid.proton.message.Message;

public class RequestResponseUtils {
	public static Message createRequestMessageFromPropertyBag(String operation, Map propertyBag, Duration timeout)
	{
		return createRequestMessageFromValueBody(operation, propertyBag, timeout);
	}
	
	public static Message createRequestMessageFromValueBody(String operation, Object valueBody, Duration timeout)
    {
        Message requestMessage = Message.Factory.create();
        requestMessage.setBody(new AmqpValue(valueBody));
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
		if(codeObject == null)
		{
		    codeObject = responseMessage.getApplicationProperties().getValue().get(ClientConstants.REQUEST_RESPONSE_LEGACY_STATUS_CODE);
		}
		if(codeObject != null)
		{
			statusCode = (int)codeObject;
		}
		
		return statusCode;
	}
	
	public static Symbol getResponseErrorCondition(Message responseMessage)
	{
		Symbol errorCondition = (Symbol)responseMessage.getApplicationProperties().getValue().get(ClientConstants.REQUEST_RESPONSE_ERROR_CONDITION);
		if(errorCondition == null)
		{
		    errorCondition = (Symbol)responseMessage.getApplicationProperties().getValue().get(ClientConstants.REQUEST_RESPONSE_LEGACY_ERROR_CONDITION);
		}
		return errorCondition;
	}
	
	public static String getResponseStatusDescription(Message responseMessage)
    {
        String statusDescription = (String)responseMessage.getApplicationProperties().getValue().get(ClientConstants.REQUEST_RESPONSE_STATUS_DESCRIPTION);
        if(statusDescription == null)
        {
            statusDescription = (String)responseMessage.getApplicationProperties().getValue().get(ClientConstants.REQUEST_RESPONSE_LEGACY_STATUS_DESCRIPTION);
        }
        return statusDescription;
    }
	
	public static Map getResponseBody(Message responseMessage)
	{
		return (Map)((AmqpValue)responseMessage.getBody()).getValue();				
	}
	
	public static Exception genereateExceptionFromResponse(Message responseMessage)
	{
		Symbol errorCondition = getResponseErrorCondition(responseMessage);
		Object statusDescription = getResponseStatusDescription(responseMessage);
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

		descriptionMap.put(ClientConstants.REQUEST_RESPONSE_RULENAME, ruleDescription.getName());
		
		return descriptionMap;
	}

	static RuleDescription decodeRuleDescriptionMap(DescribedType ruleDescribedType)
	{
		if (ruleDescribedType == null) {
			return null;
		}
		if (!(ruleDescribedType.getDescriptor()).equals(ClientConstants.RULE_DESCRIPTION_DESCRIPTOR)) {
			return null;
		}

		RuleDescription ruleDescription = new RuleDescription();
		if (ruleDescribedType.getDescribed() instanceof ArrayList) {
			ArrayList<Object> describedRule = (ArrayList<Object>) ruleDescribedType.getDescribed();
			int count = describedRule.size();
			if (count-- > 0) {
				ruleDescription.setFilter(decodeFilter(describedRule.get(0)));
			}

			if (count-- > 0) {
				ruleDescription.setAction(decodeRuleAction(describedRule.get(1)));
			}

			if (count > 0) {
				ruleDescription.setName((String) describedRule.get(2));
			}
		}

		return ruleDescription;
	}

	private static Filter decodeFilter(Object describedFilterObject) {
		if (describedFilterObject != null && describedFilterObject instanceof DescribedType) {
			DescribedType describedFilter = (DescribedType) describedFilterObject;
			if (describedFilter.getDescriptor().equals(ClientConstants.SQL_FILTER_DESCRIPTOR)) {
				ArrayList<Object> describedSqlFilter = (ArrayList<Object>)describedFilter.getDescribed();
				if (describedSqlFilter.size() > 0) {
					return new SqlFilter((String)describedSqlFilter.get(0));
				}
			}
			else if (describedFilter.getDescriptor().equals(ClientConstants.CORRELATION_FILTER_DESCRIPTOR)) {
				CorrelationFilter correlationFilter = new CorrelationFilter();
				ArrayList<Object> describedCorrelationFilter = (ArrayList<Object>)describedFilter.getDescribed();
				int countCorrelationFilter = describedCorrelationFilter.size();
				if (countCorrelationFilter-- > 0) {
					correlationFilter.setCorrelationId((String) (describedCorrelationFilter.get(0)));
				}
				if (countCorrelationFilter-- > 0) {
					correlationFilter.setMessageId((String) (describedCorrelationFilter.get(1)));
				}
				if (countCorrelationFilter-- > 0) {
					correlationFilter.setTo((String) (describedCorrelationFilter.get(2)));
				}
				if (countCorrelationFilter-- > 0) {
					correlationFilter.setReplyTo((String) (describedCorrelationFilter.get(3)));
				}
				if (countCorrelationFilter-- > 0) {
					correlationFilter.setLabel((String) (describedCorrelationFilter.get(4)));
				}
				if (countCorrelationFilter-- > 0) {
					correlationFilter.setSessionId((String) (describedCorrelationFilter.get(5)));
				}
				if (countCorrelationFilter-- > 0) {
					correlationFilter.setReplyToSessionId((String) (describedCorrelationFilter.get(6)));
				}
				if (countCorrelationFilter-- > 0) {
					correlationFilter.setContentType((String) (describedCorrelationFilter.get(7)));
				}
				if (countCorrelationFilter > 0) {
					Object properties = describedCorrelationFilter.get(8);
					if (properties != null && properties instanceof Map) {
						correlationFilter.setProperties((Map)properties);
					}
				}

				return correlationFilter;
			}
			else if (describedFilter.getDescriptor().equals(ClientConstants.TRUE_FILTER_DESCRIPTOR)) {
				return new TrueFilter();
			}
			else if (describedFilter.getDescriptor().equals(ClientConstants.FALSE_FILTER_DESCRIPTOR)) {
				return new FalseFilter();
			}
			else {
				throw new UnsupportedOperationException("This client doesn't support filter with descriptor: " + describedFilter.getDescriptor());
			}
		}

		return null;
	}

	private static RuleAction decodeRuleAction(Object describedActionObject) {
		if (describedActionObject != null && describedActionObject instanceof DescribedType) {
			DescribedType describedAction = (DescribedType) describedActionObject;
			if (describedAction.getDescriptor().equals(ClientConstants.EMPTY_RULE_ACTION_DESCRIPTOR)) {
				return null;
			} else if (describedAction.getDescriptor().equals(ClientConstants.SQL_RULE_ACTION_DESCRIPTOR)) {
				ArrayList<Object> describedSqlAction = (ArrayList<Object>) describedAction.getDescribed();
				if (describedSqlAction.size() > 0) {
					return new SqlRuleAction((String) describedSqlAction.get(0));
				}
			}
		}

		return null;
	}
}
