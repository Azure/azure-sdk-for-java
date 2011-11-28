package com.microsoft.windowsazure.services.core.utils;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;

public class ServiceExceptionFactory {

    public static ServiceException process(String serviceName, ServiceException exception) {
        Throwable cause = exception.getCause();

        for (Throwable scan = cause; scan != null; scan = scan.getCause()) {
            if (ServiceException.class.isAssignableFrom(scan.getClass())) {
                return populate(exception, serviceName, (ServiceException) scan);
            }
            else if (UniformInterfaceException.class.isAssignableFrom(scan.getClass())) {
                return populate(exception, serviceName, (UniformInterfaceException) scan);
            }
        }

        exception.setServiceName(serviceName);

        return exception;
    }

    static ServiceException populate(ServiceException exception, String serviceName, UniformInterfaceException cause) {
        exception.setServiceName(serviceName);

        if (cause != null) {
            ClientResponse response = cause.getResponse();
            if (response != null) {
                // Set status
                Status status = response.getClientResponseStatus();
                if (status == null) {
                    status = Status.fromStatusCode(response.getStatus());
                }
                if (status == null) {
                    exception.setHttpStatusCode(response.getStatus());
                }
                else {
                    exception.setHttpStatusCode(status.getStatusCode());
                    exception.setHttpReasonPhrase(status.getReasonPhrase());
                }

                // Set raw response body
                if (response.hasEntity()) {
                    try {
                        String body = response.getEntity(String.class);
                        exception.setRawResponseBody(body);
                    }
                    catch (Exception e) {
                        // Skip exceptions as getting the response body as a string is a best effort thing
                    }
                }
            }
        }
        return exception;
    }

    static ServiceException populate(ServiceException exception, String serviceName, ServiceException cause) {
        exception.setServiceName(cause.getServiceName());
        exception.setHttpStatusCode(cause.getHttpStatusCode());
        exception.setHttpReasonPhrase(cause.getHttpReasonPhrase());
        exception.setErrorCode(cause.getErrorCode());
        exception.setErrorMessage(cause.getErrorMessage());
        exception.setRawResponseBody(cause.getRawResponseBody());
        exception.setErrorValues(cause.getErrorValues());
        return exception;
    }

}
