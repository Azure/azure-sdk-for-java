// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.jedis;


import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.messaging.eventhubs.models.Checkpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link  JedisRedisCheckpointStore}
 */
public class JedisRedisCheckpointStoreTests {
    JacksonAdapter jacksonAdapter = new JacksonAdapter();
    @Test
    public void testListCheckpoints() {

    }

}
