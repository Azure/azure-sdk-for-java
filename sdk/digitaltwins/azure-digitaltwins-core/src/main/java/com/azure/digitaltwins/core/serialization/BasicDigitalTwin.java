

package com.azure.digitaltwins.core.serialization;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * An optional helper class for deserializing a digital twin.
 */
@Fluent
public class BasicDigitalTwin {

    @JsonProperty(value = "$dtId", required = true)
    private String id;

    @JsonProperty(value = "$etag", required = true)
    private String etag;

    @JsonProperty(value = "$metadata", required = true)
    private DigitalTwinMetadata metadata;

    private Map<String, Object> customProperties;

    /**
     * Gets the unique Id of the digital twin in a digital twins instance. This field is present on every digital twin.
     * @return The unique Id of the digital twin in a digital twins instance. This field is present on every digital twin.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique Id of the digital twin in a digital twins instance. This field is present on every digital twin.
     * @param id The unique Id of the digital twin in a digital twins instance. This field is present on every digital twin.
     * @return The BasicDigitalTwin object itself.
     */
    public BasicDigitalTwin setId(String id) {
        this.id =id;
        return this;
    }

    /**
     * Gets a string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     * @return A string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     */
    public String getEtag() {
        return etag;
    }

    /**
     * Sets a string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     * @param etag A string representing a weak ETag for the entity that this request performs an operation against, as per RFC7232.
     * @return The BasicDigitalTwin object itself.
     */
    public BasicDigitalTwin setEtag(String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * Gets the information about the model a digital twin conforms to. This field is present on every digital twin.
     * @return The information about the model a digital twin conforms to. This field is present on every digital twin.
     */
    public DigitalTwinMetadata getMetadata() {
        return metadata;
    }

    /**
     * Sets the information about the model a digital twin conforms to. This field is present on every digital twin.
     * @param metadata The information about the model a digital twin conforms to. This field is present on every digital twin.
     * @return The BasicDigitalTwin object itself.
     */
    public BasicDigitalTwin setMetadata(DigitalTwinMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    /**
     * Gets the additional properties of the digital twin. This field will contain any properties of the digital twin that are not already defined by the other strong types of this class.
     * @return The additional properties of the digital twin. This field will contain any properties of the digital twin that are not already defined by the other strong types of this class.
     */
    @JsonAnyGetter
    public Map<String, Object> getCustomProperties() {
        return customProperties;
    }

    /**
     * Sets the additional properties of the digital twin. This field will contain any properties of the digital twin that are not already defined by the other strong types of this class.
     */
    @JsonAnySetter
    public void setCustomProperties(String key, Object value) {
        customProperties.put(key, value);
    }
}
