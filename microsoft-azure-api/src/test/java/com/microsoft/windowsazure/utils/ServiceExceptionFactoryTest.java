package com.microsoft.windowsazure.utils;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.utils.ServiceExceptionFactory;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;

public class ServiceExceptionFactoryTest {
    @Test
    public void serviceNameAndMessageAndCauseAppearInException() {
        // Arrange
        ClientResponse response = new ClientResponse(404, null, new ByteArrayInputStream(new byte[0]), null);
        UniformInterfaceException cause = new UniformInterfaceException(
                response);

        // Act
        ServiceException exception = ServiceExceptionFactory.process("testing", new ServiceException("this is a test", cause));

        // Assert
        assertNotNull(exception);
        assertEquals("testing", exception.getServiceName());
        assertEquals("this is a test", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void httpStatusCodeAndReasonPhraseAppearInException() {
        // Arrange
        ClientResponse response = new ClientResponse(404, null, new ByteArrayInputStream(new byte[0]), null);
        UniformInterfaceException cause = new UniformInterfaceException(
                response);

        // Act
        ServiceException exception = ServiceExceptionFactory.process("testing", new ServiceException("this is a test", cause));

        // Assert
        assertNotNull(exception);
        assertEquals(404, exception.getHttpStatusCode());
        assertEquals("Not Found", exception.getHttpReasonPhrase());
    }

    @Test
    public void informationWillPassUpIfServiceExceptionIsRootCauseOfClientHandlerExceptions() {
        // Arrange
        ClientResponse response = new ClientResponse(503, null, new ByteArrayInputStream(new byte[0]), null);
        UniformInterfaceException rootCause = new UniformInterfaceException(response);
        ServiceException originalDescription = ServiceExceptionFactory.process("underlying", new ServiceException(rootCause));
        ClientHandlerException wrappingException = new ClientHandlerException(originalDescription);

        // Act 
        ServiceException exception = ServiceExceptionFactory.process("actual", new ServiceException(wrappingException));

        // Assert
        assertEquals(503, exception.getHttpStatusCode());
        assertEquals("underlying", exception.getServiceName());
    }

}
