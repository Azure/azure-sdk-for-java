package com.azure.messaging.eventgrid.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

@Fluent
public class CustomSchema {

    @JsonProperty(value = "id", required = true)
    private String id;

    @JsonProperty(value = "sentData", required = true)
    private ContosoItemSentEventData sentData;

    @JsonProperty(value = "data")
    private Object data;

    @JsonIgnore
    private Map<String, Object> additionalProperties;

    /**
     * Get the event's unique ID.
     * @return the ID associated with this event.
     */
    public String getId() {
        return id;
    }

    /**
     * Set the unique ID of the event.
     * @param id the ID to set.
     *
     * @return the event object itself.
     */
    public CustomSchema setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the event's unique sent data.
     * @return the sent data associated with this event.
     */
    public ContosoItemSentEventData getSentData() {
        return sentData;
    }

    /**
     * Set the sent data of the event.
     * @param sentData the item sent data to set.
     *
     * @return the event object itself.
     */
    public CustomSchema setSentData(ContosoItemSentEventData sentData) {
        this.sentData = sentData;
        return this;
    }

    /**
     * Get the event's associated data.
     * @return the data associated with this event.
     */
    public Object getData() {
        return data;
    }

    /**
     * Set the data of the event.
     * @param data the data to set.
     *
     * @return the event object itself.
     */
    public CustomSchema setData(Object data) {
        this.data = data;
        return this;
    }

    /**
     * Get the additionalProperties property: Properties of an event published to an Event Grid topic using the
     * CloudEvent 1.0 Schema.
     * @return the additionalProperties value.
     */
    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    /**
     * Set the additionalProperties property: Properties of an event published to an Event Grid topic using the
     * CloudEvent 1.0 Schema.
     * @param additionalProperties the additionalProperties value to set.
     *
     * @return the event object itself.
     */
    public CustomSchema setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    @JsonAnySetter
    void setAdditionalProperties(String key, Object value) {
        if (additionalProperties == null) {
            additionalProperties = new HashMap<>();
        }
        additionalProperties.put(key, value);
    }

}
