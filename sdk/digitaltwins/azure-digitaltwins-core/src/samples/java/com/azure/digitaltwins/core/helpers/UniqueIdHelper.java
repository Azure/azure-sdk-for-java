package com.azure.digitaltwins.core.helpers;

import com.azure.digitaltwins.core.DigitalTwinsAsyncClient;
import com.azure.digitaltwins.core.DigitalTwinsClient;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;

import java.net.HttpURLConnection;
import java.util.Objects;
import java.util.function.Function;

public class UniqueIdHelper {
    public static String getUniqueModelId(String baseName, DigitalTwinsAsyncClient client, Function<Integer, String> randomIntegerStringGenerator) {
        return getUniqueId(baseName, (modelId -> Objects.requireNonNull(client.getModel(modelId).block()).getModelId()), randomIntegerStringGenerator);
    }

    public static String getUniqueModelId(String baseName, DigitalTwinsClient client, Function<Integer, String> randomIntegerStringGenerator) {
        return getUniqueId(baseName, (modelId -> client.getModel(modelId).getModelId()), randomIntegerStringGenerator);
    }

    public static String getUniqueDigitalTwinId(String baseName, DigitalTwinsAsyncClient client, Function<Integer, String> randomIntegerStringGenerator) {
        return getUniqueId(baseName, (digitalTwinId -> client.getDigitalTwin(digitalTwinId, String.class).block()), randomIntegerStringGenerator);
    }

    public static String getUniqueDigitalTwinId(String baseName, DigitalTwinsClient client, Function<Integer, String> randomIntegerStringGenerator) {
        return getUniqueId(baseName, (digitalTwinId -> client.getDigitalTwin(digitalTwinId, String.class)), randomIntegerStringGenerator);
    }

    // Taking randomIntegerStringGenerator as a parameter here because e2e tests use a special function for recording and replaying "random" numbers
    // and samples just use random numbers.
    private static String getUniqueId(String baseName, Function<String, String> getResource, Function<Integer, String> randomIntegerStringGenerator) {
        int maxAttempts = 10;
        int maxRandomDigits = 8; // Not to be confused with max random value. This value determines the length of the string of random integers

        for (int attemptsMade = 0 ; attemptsMade < maxAttempts ; attemptsMade++) {
            String id = baseName + randomIntegerStringGenerator.apply(maxRandomDigits);
            try {
                getResource.apply(id);
            } catch (ErrorResponseException ex) {
                if (ex.getResponse().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    return id;
                } else {
                    // This request should not retried if it encounters a 401 error, for instance
                    throw new IllegalStateException("Encountered unexpected error while searching for unique id", ex);
                }
            }
        }

        throw new IllegalStateException("Unique id could not be found with base name" + baseName);
    }
}
