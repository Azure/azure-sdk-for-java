package com.microsoft.azure.servicebus.rules;

import java.util.Map;

public class CorrelationFilter extends Filter {
	private String correlationId;
	private String messageId;
	private String to;
	private String replyTo;
	private String label;
	private String sessionId;
	private String replyToSessionId;
	private String contentType;
	private Map<String, Object> properties;
	
	public CorrelationFilter()
	{		
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getReplyTo() {
		return replyTo;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getReplyToSessionId() {
		return replyToSessionId;
	}

	public void setReplyToSessionId(String replyToSessionId) {
		this.replyToSessionId = replyToSessionId;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}	
}
