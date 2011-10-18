package com.microsoft.azure;
import java.util.Map;


public class ServiceException extends Exception {
	int httpStatusCode;
	String httpReasonPhrase;
	
	String errorCode;
	String errorMessage;
	Map<String, String> errorValues;

	public ServiceException() {
		
	}
	
	public int getHttpStatusCode() {
		return httpStatusCode;
	}
	public void setHttpStatusCode(int httpStatusCode) {
		this.httpStatusCode = httpStatusCode;
	}
	public String getHttpReasonPhrase() {
		return httpReasonPhrase;
	}
	public void setHttpReasonPhrase(String httpReasonPhrase) {
		this.httpReasonPhrase = httpReasonPhrase;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public Map<String, String> getErrorValues() {
		return errorValues;
	}
	public void setErrorValues(Map<String, String> errorValues) {
		this.errorValues = errorValues;
	}
}
