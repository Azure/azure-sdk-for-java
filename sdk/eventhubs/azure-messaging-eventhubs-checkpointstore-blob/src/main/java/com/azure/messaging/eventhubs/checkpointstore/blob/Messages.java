// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.util.CoreUtils;
import java.util.Map;

/**
 * I18n messages loaded from the messages.properties file located within the same package.
 */
public enum Messages {
    ;
    private static final String PATH = "eventhubs-checkpointstore-blob-messages.properties";
    private static final Map<String, String> PROPERTIES = CoreUtils.getProperties(PATH);

    /**
     * No metadata available for blob.
     */
    public static final String NO_METADATA_AVAILABLE_FOR_BLOB = getMessage("NO_METADATA_AVAILABLE_FOR_BLOB");

    /**
     * Claim error.
     */
    public static final String CLAIM_ERROR = getMessage("CLAIM_ERROR");

    /**
     * Found blob for partition.
     */
    public static final String FOUND_BLOB_FOR_PARTITION = getMessage("FOUND_BLOB_FOR_PARTITION");

    /**
     * Blob owner info.
     */
    public static final String BLOB_OWNER_INFO = getMessage("BLOB_OWNER_INFO");

    /**
     * Checkpoint info.
     */
    public static final String CHECKPOINT_INFO = getMessage("CHECKPOINT_INFO");

    /**
     * @param key the key of the message to retrieve
     * @return the message matching the given key
     */
    public static String getMessage(String key) {
        return PROPERTIES.getOrDefault(key, key);
    }

}

