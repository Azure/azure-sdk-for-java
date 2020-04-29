// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.exception;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class CloudErrorTests {

    @Test
    public void testDeserialization() throws IOException {
        final String sampleErrorResponse = "{\"error\":{\"code\":\"PolicyViolation\",\"message\":\"Policy violation.\",\"target\":\"\",\"additionalInfo\":[],\"details\":[{\"code\":\"PolicyViolation\",\"target\":\"\",\"message\":\"\",\"additionalInfo\":[{\"type\":\"PolicyViolation\",\"info\":{\"policySetDefinitionDisplayName\":\"Secure the environment\",\"policySetDefinitionId\":\"\\/subscriptions\\/00000-00000-0000-000\\/providers\\/Microsoft.Authorization\\/policySetDefinitions\\/TestPolicySet\",\"policyDefinitionDisplayName\":\"Allowed locations\",\"policyDefinitionId\":\"\\/subscriptions\\/00000-00000-0000-000\\/providers\\/Microsoft.Authorization\\/policyDefinitions\\/TestPolicy1\",\"policyAssignmentDisplayName\":\"Allow Central US and WEU only\",\"policyAsssignmentId\":\"\\/subscriptions\\/00000-00000-0000-000\\/providers\\/Microsoft.Authorization\\/policyAssignments\\/TestAssignment1\"}},{\"type\":\"SomeOtherViolation\",\"info\":{\"innerException\":\"innerException Details\"}}]}]}}";

        SerializerAdapter serializerAdapter = new JacksonAdapter();

        ErrorResponse<CloudError> errorResponse = serializerAdapter.deserialize(
            sampleErrorResponse,
            new TypeReference<ErrorResponse<CloudError>>() {
            }.getType(), SerializerEncoding.JSON);
        CloudError cloudError = errorResponse.getError();
        Assertions.assertEquals("PolicyViolation", cloudError.getCode());
        Assertions.assertEquals(1, cloudError.getDetails().size());
        Assertions.assertEquals(2, cloudError.getDetails().get(0).getAdditionalInfo().size());
        Assertions.assertEquals("SomeOtherViolation", cloudError.getDetails().get(0).getAdditionalInfo().get(1).getType());
        Assertions.assertEquals("Policy violation.", cloudError.toString());
    }

    @Test
    public void testSubclassDeserialization() throws IOException {
        final String sampleErrorResponse = "{\"error\":{\"code\":\"WepAppError\",\"message\":\"Web app error.\",\"innererror\":\"Deployment error.\",\"details\":[{\"innererror\":\"Inner error.\"}]}}";

        SerializerAdapter serializerAdapter = new JacksonAdapter();

        ErrorResponse<WebError> errorResponse = serializerAdapter.deserialize(
            sampleErrorResponse,
            new TypeReference<ErrorResponse<WebError>>() {
            }.getType(), SerializerEncoding.JSON);
        WebError webError = errorResponse.getError();
        Assertions.assertEquals("WepAppError", webError.getCode());
        Assertions.assertEquals("Deployment error.", webError.getInnererror());
        Assertions.assertEquals(1, webError.getDetails().size());
        Assertions.assertEquals("Inner error.", webError.getDetails().get(0).getInnererror());

        Object errorResponseObj = serializerAdapter.deserialize(sampleErrorResponse, Object.class, SerializerEncoding.JSON);
        WebError webError2 = errorFromObject(serializerAdapter, errorResponseObj);
        Assertions.assertEquals("WepAppError", webError2.getCode());
    }

    private static WebError errorFromObject(SerializerAdapter serializerAdapter, Object errorResponseObj) throws IOException {
        String json = serializerAdapter.serialize(errorResponseObj, SerializerEncoding.JSON);
        ErrorResponse<WebError> errorResponse =  serializerAdapter.deserialize(json,
            new TypeReference<ErrorResponse<WebError>>() {
            }.getType(), SerializerEncoding.JSON);
        return errorResponse.getError();
    }

    @Immutable
    private static class WebError extends CloudError {
        @JsonProperty(value = "innererror", access = JsonProperty.Access.WRITE_ONLY)
        private String innererror;

        @JsonProperty(value = "details", access = JsonProperty.Access.WRITE_ONLY)
        private List<WebError> details;

        public String getInnererror() {
            return this.innererror;
        }

        public List<WebError> getDetails() {
            return details;
        }
    }

    private static class ErrorResponse<T> {
        @JsonProperty("error")
        private T error;

        public T getError() {
            return error;
        }
    }
}
