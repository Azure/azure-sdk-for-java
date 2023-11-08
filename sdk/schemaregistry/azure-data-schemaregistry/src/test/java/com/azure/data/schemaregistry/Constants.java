// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

/**
 * Shared constants.
 */
class Constants {
    static final int RESOURCE_LENGTH = 16;

    // When we regenerate recordings, make sure that the schema group matches what we are persisting.
    static final String PLAYBACK_TEST_GROUP = "azsdk_java_group";

    static final String PLAYBACK_ENDPOINT = "https://foo.servicebus.windows.net";

    static final String SCHEMA_REGISTRY_AVRO_FULLY_QUALIFIED_NAMESPACE = "SCHEMA_REGISTRY_AVRO_FULLY_QUALIFIED_NAMESPACE";

    static final String SCHEMA_REGISTRY_CUSTOM_FULLY_QUALIFIED_NAMESPACE = "SCHEMA_REGISTRY_CUSTOM_FULLY_QUALIFIED_NAMESPACE";

    static final String SCHEMA_REGISTRY_JSON_FULLY_QUALIFIED_NAMESPACE = "SCHEMA_REGISTRY_JSON_FULLY_QUALIFIED_NAMESPACE";

    static final String SCHEMA_REGISTRY_GROUP = "SCHEMA_REGISTRY_GROUP";
}
