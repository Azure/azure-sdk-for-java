/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.utils;

import com.microsoft.windowsazure.core.ServiceTimeoutException;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.exception.ServiceExceptionFactory;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.net.SocketTimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

public class ServiceExceptionFactoryTest {
    @Test
    public void serviceNameAndMessageAndCauseAppearInException() {
        // Arrange
        ClientResponse response = new ClientResponse(404, null,
                new ByteArrayInputStream(new byte[0]), null);
        UniformInterfaceException cause = new UniformInterfaceException(
                response);

        // Act
        ServiceException exception = ServiceExceptionFactory.process("testing",
                new ServiceException("this is a test", cause));

        // Assert
        assertNotNull(exception);
        assertEquals("testing", exception.getServiceName());
        assertEquals("this is a test", exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    public void httpStatusCodeAndReasonPhraseAppearInException() {
        // Arrange
        ClientResponse response = new ClientResponse(404, null,
                new ByteArrayInputStream(new byte[0]), null);
        UniformInterfaceException cause = new UniformInterfaceException(
                response);

        // Act
        ServiceException exception = ServiceExceptionFactory.process("testing",
                new ServiceException("this is a test", cause));

        // Assert
        assertNotNull(exception);
        assertEquals(404, exception.getHttpStatusCode());
        assertEquals("Not Found", exception.getHttpReasonPhrase());
    }

    @Test
    public void informationWillPassUpIfServiceExceptionIsRootCauseOfClientHandlerExceptions() {
        // Arrange
        ClientResponse response = new ClientResponse(503, null,
                new ByteArrayInputStream(new byte[0]), null);
        UniformInterfaceException rootCause = new UniformInterfaceException(
                response);
        ServiceException originalDescription = ServiceExceptionFactory.process(
                "underlying", new ServiceException(rootCause));
        ClientHandlerException wrappingException = new ClientHandlerException(
                originalDescription);

        // Act
        ServiceException exception = ServiceExceptionFactory.process("actual",
                new ServiceException(wrappingException));

        // Assert
        assertEquals(503, exception.getHttpStatusCode());
        assertEquals("underlying", exception.getServiceName());
    }

    @Test
    public void socketTimeoutWillPassUpIfInsideClientHandlerException() {
        String expectedMessage = "connect timeout";
        SocketTimeoutException rootCause = new SocketTimeoutException(
                expectedMessage);
        ClientHandlerException wrappingException = new ClientHandlerException(
                rootCause);

        ServiceException exception = ServiceExceptionFactory.process("testing",
                new ServiceException(wrappingException));

        assertSame(ServiceTimeoutException.class, exception.getClass());
        assertEquals(expectedMessage, exception.getMessage());
        assertEquals("testing", exception.getServiceName());
    }
}
