package com.microsoft.azure.utils;

import com.microsoft.azure.ServiceException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;

public class ServiceExceptionFactory {
	public static ServiceException create(String serviceName, String message, UniformInterfaceException cause){
		ServiceException exception = new ServiceException(message, cause);
		populate(exception, serviceName, cause);
		return exception;
	}

	public static ServiceException create(String serviceName, UniformInterfaceException cause) {
		ServiceException exception = new ServiceException(cause);
		populate(exception, serviceName, cause);
		return exception;
	}

	public static ServiceException create(String serviceName, String message, ClientHandlerException cause) {
		ServiceException exception = new ServiceException(message, cause);
		for (Throwable scan = cause; scan != null; scan = scan.getCause()) {
			if (ServiceException.class.isAssignableFrom(scan.getClass())) {
				populate(exception, serviceName, (ServiceException)scan);
				break;
			}
			else if (UniformInterfaceException.class.isAssignableFrom(scan.getClass())) {
				populate(exception, serviceName, (UniformInterfaceException)scan);
				break;
			}
		}
		return exception;
	}

	public static ServiceException create(String serviceName, ClientHandlerException cause) {
		ServiceException exception = new ServiceException(cause);
		for (Throwable scan = cause; scan != null; scan = scan.getCause()) {
			if (ServiceException.class.isAssignableFrom(scan.getClass())) {
				populate(exception, serviceName, (ServiceException)scan);
				break;
			}
			else if (UniformInterfaceException.class.isAssignableFrom(scan.getClass())) {
				populate(exception, serviceName, (UniformInterfaceException)scan);
				break;
			}
		}
		return exception;
	}

	static void populate(ServiceException exception, String serviceName,
			UniformInterfaceException cause) {
		exception.setServiceName(serviceName);

		if (cause != null) {
			ClientResponse response = cause.getResponse();
			if (response != null) {
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
			}
		}
	}
	
	static void populate(ServiceException exception, String serviceName,
			ServiceException cause) {
		exception.setServiceName(cause.getServiceName());
		exception.setHttpStatusCode(cause.getHttpStatusCode());
		exception.setHttpReasonPhrase(cause.getHttpReasonPhrase());
		exception.setErrorCode(cause.getErrorCode());
		exception.setErrorMessage(cause.getErrorMessage());
		exception.setErrorValues(cause.getErrorValues());
	}

}
