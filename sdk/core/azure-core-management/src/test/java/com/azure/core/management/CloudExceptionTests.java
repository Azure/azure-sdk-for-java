// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.management.serializer.AzureJacksonAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class CloudExceptionTests {

    @Test
    public void deserialization() throws IOException {
        // actually a test on CloudErrorDeserializer

        final String errorBody = "{\"error\":{\"code\":\"ResourceGroupNotFound\",\"message\":\"Resource group 'rg-not-exist' could not be found.\"}}";

        AzureJacksonAdapter serializerAdapter = new AzureJacksonAdapter();
        CloudError cloudError = serializerAdapter.deserialize(errorBody, CloudError.class, SerializerEncoding.JSON);
        Assertions.assertEquals("ResourceGroupNotFound", cloudError.getCode());
    }
}
