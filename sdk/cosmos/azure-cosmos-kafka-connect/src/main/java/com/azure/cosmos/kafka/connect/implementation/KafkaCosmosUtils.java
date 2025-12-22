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
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

public class KafkaCosmosUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaCosmosUtils.class);
    private static final Set<String> ALLOWED_CLASSES = new HashSet<>();
    static {
        ALLOWED_CLASSES.add(CosmosClientMetadataCachesSnapshot.class.getName());
        ALLOWED_CLASSES.add(byte[].class.getName());
    }

    public static CosmosClientMetadataCachesSnapshot getCosmosClientMetadataFromString(String metadataCacheString) {
        if (StringUtils.isNotEmpty(metadataCacheString)) {
            byte[] inputByteArray = Base64.getDecoder().decode(metadataCacheString);
            try (ObjectInputStream objectInputStream =
                     new ObjectInputStream(new ByteArrayInputStream(inputByteArray)) {
                    @Override
                    protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                        // Whitelist only allowed classes to prevent rce from arbitrary classes
                        if (!ALLOWED_CLASSES.contains(desc.getName())) {
                            LOGGER.error(desc.getName());
                            throw new InvalidClassException("Unauthorized deserialization attempt", desc.getName());
                        }
                        return super.resolveClass(desc);
                    }
            }) {
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
