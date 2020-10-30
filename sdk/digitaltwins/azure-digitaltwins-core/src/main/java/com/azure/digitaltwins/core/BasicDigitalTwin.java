// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.annotation.Fluent;
import com.azure.digitaltwins.core.models.DigitalTwinsJsonPropertyNames;
import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

/**
 * An optional, helper class for deserializing a digital twin.
 * Only properties with non-null values are included.
 * <p>
 * Note that this class uses {@link JsonProperty} from the Jackson serialization library. Because of this, this type
 * will only work if the default json serializer is used by the digital twins client or if the custom json
 * serializer uses Jackson as well. In order to use a different json library, a new BasicDigitalTwin class must
 * be constructed and have its json properties tagged by the annotation used by that json library.
 */
@Fluent
@JsonInclude(Include.NON_NULL)
public final class BasicDigitalTwin {

    @JsonProperty(value = DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ID, required = true)
    private String id;

    @JsonProperty(value = DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_ETAG, required = true)
    private String etag;

    @JsonProperty(value = DigitalTwinsJsonPropertyNames.DIGITAL_TWIN_METADATA, required = true)
    private BasicDigitalTwinMetadata metadata;

    @JsonIgnore
    private final Map<String, Object> contents = new HashMap<>();

    /**
     * Construct a basic digital twin.
     * @param digitalTwinId The Id of the digital twin. The Id is unique within the service and case sensitive.
     */
    public BasicDigitalTwin(String digitalTwinId) {
        this.id = digitalTwinId;
    }

    // Empty constructor for json deserialization purposes
    private BasicDigitalTwin() {
    }

    /**
     * Gets the unique Id of the digital twin in a digital twins instance. This field is present on every digital twin.
     * @return The unique Id of the digital twin in a digital twins instance. This field is present on every digital twin.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets a string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     * @return A string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     */
    public String getETag() {
        return etag;
    }

    /**
     * Sets a string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     * @param etag A string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     * @return The BasicDigitalTwin object itself.
     */
    public BasicDigitalTwin setETag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * Gets the information about the model a digital twin conforms to. This field is present on every digital twin.
     * @return The information about the model a digital twin conforms to. This field is present on every digital twin.
     */
    public BasicDigitalTwinMetadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the information about the model a digital twin conforms to. This field is present on every digital twin.
     * @param metadata The information about the model a digital twin conforms to. This field is present on every digital twin.
     * @return The BasicDigitalTwin object itself.
     */
    public BasicDigitalTwin setMetadata(BasicDigitalTwinMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the additional custom contents of the digital twin. This field will contain any contents of the digital twin that are not already defined by the other strong types of this class.
     * @return The additional contents of the digital twin. This field will contain any contents of the digital twin that are not already defined by the other strong types of this class.
     */
    @JsonAnyGetter
    public Map<String, Object> getContents() {
        return contents;
    }

    /**
     * Adds an additional custom property to the digital twin contents. This field will contain any contents of the
     * digital twin that are not already defined by the other strong types of this class.
     * @param key The key of the additional property to be added to the digital twin.
     * @param value The value of the additional property to be added to the digital twin.
     * @return The BasicDigitalTwin object itself.
     */
    @JsonAnySetter
    public BasicDigitalTwin addToContents(String key, Object value) {
        this.contents.put(key, value);
        return this;
    }
}
