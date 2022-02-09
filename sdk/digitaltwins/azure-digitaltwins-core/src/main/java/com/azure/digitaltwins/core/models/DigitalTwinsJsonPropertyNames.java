// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.models;

/**
 * String constants for use in JSON de/serialization for digital twins, digital twin relationships,
 * and digital twin components.
 */
public class DigitalTwinsJsonPropertyNames {
    /**
     * The JSON property name for the Id field on a digital twin.
     */
    public static final String DIGITAL_TWIN_ID = "$dtId";

    /**
     * The JSON property name for the ETag field on a digital twin.
     */
    public static final String DIGITAL_TWIN_ETAG = "$etag";

    /**
     * The JSON property name for the metadata field on a digital twin and for the metadata field on a component.
     */
    public static final String DIGITAL_TWIN_METADATA = "$metadata";

    /**
     * The JSON property name for the model field on a digital twin metadata.
     */
    public static final String METADATA_MODEL = "$model";

    /**
     * The JSON property name for the Id field on a relationship.
     */
    public static final String RELATIONSHIP_ID = "$relationshipId";

    /**
     * The JSON property name for the source Id field on a relationship.
     */
    public static final String RELATIONSHIP_SOURCE_ID = "$sourceId";

    /**
     * The JSON property name for the target Id field on a relationship.
     */
    public static final String RELATIONSHIP_TARGET_ID = "$targetId";

    /**
     * The JSON property name for the name field on a relationship.
     */
    public static final String RELATIONSHIP_NAME = "$relationshipName";

    /**
     * The JSON property name for the lastUpdateTime field on a digital twin component's property metadata.
     */
    public static final String METADATA_PROPERTY_LAST_UPDATE_TIME = "lastUpdateTime";
}
