// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.exception;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Immutable;
import com.azure.core.annotation.JsonFlatten;
import com.azure.core.management.Resource;
import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class ManagementExceptionTests {

    @Test
    public void testDeserialization() throws IOException {
        final String errorBody = "{\"error\":{\"code\":\"ResourceGroupNotFound\",\"message\":\"Resource group 'rg-not-exist' could not be found.\"}}";

        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        ManagementError managementError = serializerAdapter.deserialize(errorBody, ManagementError.class, SerializerEncoding.JSON);
        Assertions.assertEquals("ResourceGroupNotFound", managementError.getCode());
    }

    @Test
    public void testSubclassDeserialization() throws IOException {
        final String errorBody = "{\"error\":{\"code\":\"WepAppError\",\"message\":\"Web app error.\",\"innererror\":\"Deployment error.\",\"details\":[{\"innererror\":\"Inner error.\"}]}}";

        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        WebError webError = serializerAdapter.deserialize(errorBody, WebError.class, SerializerEncoding.JSON);
        Assertions.assertEquals("WepAppError", webError.getCode());

        // The response is actually not a valid management error response. But still accommodate.
        final String errorBodyWithoutErrorProperty = "{\"code\":\"ResourceGroupNotFound\",\"message\":\"Resource group 'rg-not-exist' could not be found.\"}";

        ManagementError managementError = serializerAdapter.deserialize(errorBodyWithoutErrorProperty, ManagementError.class, SerializerEncoding.JSON);
        Assertions.assertEquals("ResourceGroupNotFound", managementError.getCode());
    }

    @Test
    public void testDeserializationInResource() throws IOException {
        final String virtualMachineJson = "{\"properties\":{\"instanceView\":{\"patchStatus\":{\"availablePatchSummary\":{\"error\":{}}}}}}";

        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        VirtualMachine virtualMachine = serializerAdapter.deserialize(virtualMachineJson, VirtualMachine.class, SerializerEncoding.JSON);

        Assertions.assertNotNull(virtualMachine.instanceView.patchStatus.availablePatchSummary.error);
    }

    @Immutable
    private static class WebError extends ManagementError {
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

    @JsonFlatten
    @Fluent
    private static final class VirtualMachine extends Resource {
        @JsonProperty(value = "properties.instanceView", access = JsonProperty.Access.WRITE_ONLY)
        private VirtualMachineInstanceView instanceView;
    }

    @Fluent
    public static final class VirtualMachineInstanceView {
        @JsonProperty(value = "patchStatus")
        private VirtualMachinePatchStatus patchStatus;
    }

    @Fluent
    public static final class VirtualMachinePatchStatus {
        @JsonProperty(value = "availablePatchSummary")
        private AvailablePatchSummary availablePatchSummary;
    }

    @Immutable
    public static final class AvailablePatchSummary {
        @JsonProperty(value = "error", access = JsonProperty.Access.WRITE_ONLY)
        private ManagementError error;
    }
}
