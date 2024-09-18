// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.communication.identity.implementation;

import com.azure.communication.identity.implementation.converters.IdentityErrorConverter;
import com.azure.communication.identity.implementation.models.CommunicationError;
import com.azure.communication.identity.implementation.models.CommunicationErrorResponse;
import com.azure.communication.identity.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.identity.models.IdentityError;
import com.azure.communication.identity.models.IdentityErrorResponseException;
import com.azure.core.http.HttpResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.*;

public class IdentityErrorConverterUnitTests {

    private HttpResponse httpResponse;
    private CommunicationErrorResponseException communicationResponseException;

    @BeforeEach
    public void setUp() {
        httpResponse = Mockito.mock(HttpResponse.class);
    }

    @AfterEach
    public void destroy() {
        httpResponse = null;
        communicationResponseException = null;
    }

    @Test
    public void translateExceptionWithAllPropertiesSet() {
        // Arrange
        communicationResponseException = setUpCommunicationResponseExceptionWithAllProperties();

        // Action
        IdentityErrorResponseException identityResponseException = IdentityErrorConverter.translateException(communicationResponseException);

        // Assert
        assertIdentityResponseExceptionMandates(identityResponseException);
        assertIdentityErrorMandates(identityResponseException.getValue());
        assertNotNull(identityResponseException.getValue().getTarget());
        assertFalse(identityResponseException.getValue().getDetails().isEmpty());
    }

    @Test
    public void translateExceptionWithoutAllPropertiesSet() {
        // Arrange
        communicationResponseException = setUpCommunicationResponseExceptionWithoutAllProperties();

        // Action
        IdentityErrorResponseException identityResponseException = IdentityErrorConverter.translateException(communicationResponseException);

        // Assert
        assertIdentityResponseExceptionMandates(identityResponseException);
        assertIdentityErrorMandates(identityResponseException.getValue());
        assertNull(identityResponseException.getValue().getTarget());
        assertTrue(identityResponseException.getValue().getDetails().isEmpty());
    }

    @Test
    public void translateExceptionWithoutErrorResponse() {
        // Arrange
        communicationResponseException = new CommunicationErrorResponseException("Exception Message", httpResponse);

        // Action
        IdentityErrorResponseException identityResponseException = IdentityErrorConverter.translateException(communicationResponseException);

        // Assert
        assertIdentityResponseExceptionMandates(identityResponseException);
        assertNull(identityResponseException.getValue());
    }

    private void assertIdentityResponseExceptionMandates(IdentityErrorResponseException identityResponseException) {
        assertNotNull(identityResponseException);
        assertEquals(communicationResponseException.getMessage(), identityResponseException.getMessage());
        assertEquals(communicationResponseException.getResponse(), identityResponseException.getResponse());
    }

    private void assertIdentityErrorMandates(IdentityError identityError) {
        assertEquals(communicationResponseException.getValue().getError().getCode(), identityError.getCode());
        assertEquals(communicationResponseException.getValue().getError().getMessage(), identityError.getMessage());
    }

    private CommunicationErrorResponseException setUpCommunicationResponseExceptionWithoutAllProperties() {
        CommunicationError communicationError = new CommunicationError().setCode("Error Code").setMessage("Error Message");
        CommunicationErrorResponse errorResponse = new CommunicationErrorResponse().setError(communicationError);
        return new CommunicationErrorResponseException("Exception Message", httpResponse, errorResponse);
    }

    private CommunicationErrorResponseException setUpCommunicationResponseExceptionWithAllProperties() {
        String value = "{\"code\":\"Error Code\",\"message\":\"Error Message\",\"target\":\"Error Target\",\"details\":[{\"code\":\"New Error Code\",\"message\":\"New Error Message\"}]}";
        CommunicationError communicationError;
        try {
            communicationError = new ObjectMapper().readValue(value, CommunicationError.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        CommunicationErrorResponse errorResponse = new CommunicationErrorResponse().setError(communicationError);
        return new CommunicationErrorResponseException("Exception Message", httpResponse, errorResponse);
    }
}
