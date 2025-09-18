// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.fluentcore;

import com.azure.core.http.HttpPipeline;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.serializer.SerializerAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class AzureServiceClientTests {

    private class AzureServiceClientImpl extends AzureServiceClient {

        AzureServiceClientImpl() {
            this(null, null, null);
        }

        protected AzureServiceClientImpl(HttpPipeline httpPipeline, SerializerAdapter serializerAdapter,
            AzureEnvironment environment) {
            super(httpPipeline, serializerAdapter, environment);
        }

        @Override
        public Duration getDefaultPollInterval() {
            return Duration.ZERO;
        }
    }

    @Test
    public void testMergeContext() {
        AzureServiceClient client = new AzureServiceClientImpl();

        // verify client context
        Context clientContext = client.getContext();
        Assertions.assertEquals(2, clientContext.getValues().size());
        Assertions.assertNotNull(clientContext.getData("Sdk-Name"));
        Assertions.assertTrue(clientContext.getData("Sdk-Version").isPresent());

        // merge with Context.NONE
        Context methodContext = Context.NONE;
        Context mergedContext = client.mergeContext(methodContext);
        Assertions.assertEquals(2, mergedContext.getValues().size());

        // merge with context
        methodContext = new Context("caller-method", "Method1");
        mergedContext = client.mergeContext(methodContext);
        Assertions.assertEquals(3, mergedContext.getValues().size());
        Assertions.assertTrue(mergedContext.getData("caller-method").isPresent());
        // clientContext is not changed
        Assertions.assertEquals(2, client.getContext().getValues().size());

        // merge with value override
        methodContext = new Context("Sdk-Name", "Sdk1");
        mergedContext = client.mergeContext(methodContext);
        Assertions.assertEquals(2, mergedContext.getValues().size());
        Assertions.assertEquals("Sdk1", mergedContext.getData("Sdk-Name").get());
        // clientContext is not changed
        Assertions.assertNotEquals("Sdk1", client.getContext().getData("Sdk-Name").get());

        // into is Context.NONE
        methodContext = new Context("caller-method", "Method1");
        mergedContext = CoreUtils.mergeContexts(Context.NONE, methodContext);
        Assertions.assertEquals(1, mergedContext.getValues().size());
        Assertions.assertTrue(mergedContext.getData("caller-method").isPresent());

        // both is Context.NONE
        mergedContext = CoreUtils.mergeContexts(Context.NONE, Context.NONE);
        Assertions.assertEquals(0, mergedContext.getValues().size());
    }
}
