package com.microsoft.windowsazure;

import java.util.HashMap;
import java.util.Map;

public class ServiceException extends Exception {

    private static final long serialVersionUID = -4942076377009150131L;

    int httpStatusCode;
    String httpReasonPhrase;
    String serviceName;

    String errorCode;
    String errorMessage;
    Map<String, String> errorValues;

    public ServiceException() {
        init();
    }

    public ServiceException(String message) {
        super(message);
        init();
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
        init();
    }

    public ServiceException(Throwable cause) {
        super(cause);
        init();
    }

    private void init() {
        errorValues = new HashMap<String, String>();
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

    public String getErrorValue(String name) {
        return errorValues.get(name);
    }

    public void setErrorValue(String name, String value) {
        this.errorValues.put(name, value);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

}
