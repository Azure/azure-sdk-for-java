// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.implementation.CosmosClientMetadataCachesSnapshot;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;

public class KafkaCosmosUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaCosmosUtils.class);

    public static CosmosClientMetadataCachesSnapshot getCosmosClientMetadataFromString(String metadataCacheString) {
        if (StringUtils.isNotEmpty(metadataCacheString)) {
            byte[] inputByteArray = Base64.getDecoder().decode(metadataCacheString);
            try (ObjectInputStream objectInputStream =
                     new ObjectInputStream(new ByteArrayInputStream(inputByteArray))) {

                return (CosmosClientMetadataCachesSnapshot) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.warn("Failed to deserialize cosmos client metadata cache snapshot");
                return null;
            }
        }

        return null;
    }

    public static String convertClientMetadataCacheSnapshotToString(CosmosAsyncClient client) {
        if (client == null) {
            return null;
        }

        CosmosClientMetadataCachesSnapshot clientMetadataCachesSnapshot = new CosmosClientMetadataCachesSnapshot();
        clientMetadataCachesSnapshot.serialize(client);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
            outputStream.writeObject(clientMetadataCachesSnapshot);
            return Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            LOGGER.warn("Failed to serialize cosmos client metadata cache snapshot", e);
            return null;
        }
    }
}
