package com.azure.digitaltwins.core.helpers;

import com.azure.digitaltwins.core.DigitalTwinsClient;
import com.azure.digitaltwins.core.implementation.models.ErrorResponseException;

import java.net.HttpURLConnection;
import java.util.Random;
import java.util.function.Function;

public class UniqueIdHelper {
    private static final Random random = new Random();

    public static String getUniqueModelId(String baseName, DigitalTwinsClient client) {
        return getUniqueId(baseName, (modelId -> client.getModel(modelId).getId()));
    }

    public static String getUniqueDigitalTwinId(String baseName, DigitalTwinsClient client) {
        return getUniqueId(baseName, (dtId -> client.getDigitalTwin(dtId)));
    }


    private static String getUniqueId(String baseName, Function<String, String> getResource) {
        int maxAttempts = 10;
        int maxVal = 10000;
        String id = baseName + random.nextInt(maxVal);

        for (int attemptsMade = 0 ; attemptsMade < maxAttempts ; attemptsMade ++ ) {
            try {
                getResource.apply(id);
            }
            catch (ErrorResponseException ex){
                if (ex.getResponse().getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                    return id;
                }
            }

            id = baseName + random.nextInt(maxVal);
        }

        throw new IllegalStateException("Unique id could not be found with base name" + baseName);
    }
}
