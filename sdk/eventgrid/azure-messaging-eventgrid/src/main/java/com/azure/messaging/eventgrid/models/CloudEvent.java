package com.azure.messaging.eventgrid.models;

import com.azure.core.annotation.Fluent;
import com.azure.messaging.eventgrid.EventSchema;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

/** The CloudEvent model. */
@Fluent
public final class CloudEvent implements EventSchema {
    /*
     * An identifier for the event. The combination of id and source must be
     * unique for each distinct event.
     */
    @JsonProperty(value = "id", required = true)
    private String id;

    /*
     * Identifies the context in which an event happened. The combination of id
     * and source must be unique for each distinct event.
     */
    @JsonProperty(value = "source", required = true)
    private String source;

    /*
     * Event data specific to the event type.
     */
    @JsonProperty(value = "data")
    private Object data;

    /*
     * Type of event related to the originating occurrence.
     */
    @JsonProperty(value = "type", required = true)
    private String type;

    /*
     * The time (in UTC) the event was generated, in RFC3339 format.
     */
    @JsonProperty(value = "time")
    private OffsetDateTime time;

    /*
     * The version of the CloudEvents specification which the event uses.
     */
    @JsonProperty(value = "specversion", required = true)
    private String specversion;

    /*
     * Identifies the schema that data adheres to.
     */
    @JsonProperty(value = "dataschema")
    private String dataschema;

    /*
     * Content type of data value.
     */
    @JsonProperty(value = "datacontenttype")
    private String datacontenttype;

    /*
     * This describes the subject of the event in the context of the event
     * producer (identified by source).
     */
    @JsonProperty(value = "subject")
    private String subject;

    /**
     * Get the id property: An identifier for the event. The combination of id and source must be unique for each
     * distinct event.
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: An identifier for the event. The combination of id and source must be unique for each
     * distinct event.
     * @param id the id value to set.
     *
     * @return the CloudEvent object itself.
     */
    public CloudEvent setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the source property: Identifies the context in which an event happened. The combination of id and source must
     * be unique for each distinct event.
     * @return the source value.
     */
    public String getSource() {
        return this.source;
    }

    /**
     * Set the source property: Identifies the context in which an event happened. The combination of id and source must
     * be unique for each distinct event.
     * @param source the source value to set.
     *
     * @return the CloudEvent object itself.
     */
    public CloudEvent setSource(String source) {
        this.source = source;
        return this;
    }

    @Override
    public boolean isCloudEvent() {
        return true;
    }

    @Override
    public boolean isEventGridEvent() {
        return false;
    }

    @Override
    public boolean isCustomEvent() {
        return false;
    }

    /**
     * Get the data property: Event data specific to the event type.
     * @return the data value.
     */
    @Override
    public Object getData() {
        return this.data;
    }

    /**
     * Set the data property: Event data specific to the event type.
     * @param data the data value to set.
     *
     * @return the CloudEvent object itself.
     */
    @Override
    public CloudEvent setData(Object data) {
        this.data = data;
        return this;
    }

    /**
     * Get the type property: Type of event related to the originating occurrence.
     * @return the type value.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Set the type property: Type of event related to the originating occurrence.
     * @param type the type value to set.
     *
     * @return the CloudEvent object itself.
     */
    public CloudEvent setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get the time property: The time (in UTC) the event was generated, in RFC3339 format.
     * @return the time value.
     */
    public OffsetDateTime getTime() {
        return this.time;
    }

    /**
     * Set the time property: The time (in UTC) the event was generated, in RFC3339 format.
     * @param time the time value to set.
     *
     * @return the CloudEvent object itself.
     */
    public CloudEvent setTime(OffsetDateTime time) {
        this.time = time;
        return this;
    }

    /**
     * Get the specversion property: The version of the CloudEvents specification which the event uses.
     * @return the specversion value.
     */
    public String getSpecversion() {
        return this.specversion;
    }

    /**
     * Set the specversion property: The version of the CloudEvents specification which the event uses.
     * @param specversion the specversion value to set.
     *
     * @return the CloudEvent object itself.
     */
    public CloudEvent setSpecversion(String specversion) {
        this.specversion = specversion;
        return this;
    }

    /**
     * Get the dataschema property: Identifies the schema that data adheres to.
     * @return the dataschema value.
     */
    public String getDataschema() {
        return this.dataschema;
    }

    /**
     * Set the dataschema property: Identifies the schema that data adheres to.
     * @param dataschema the dataschema value to set.
     *
     * @return the CloudEvent object itself.
     */
    public CloudEvent setDataschema(String dataschema) {
        this.dataschema = dataschema;
        return this;
    }

    /**
     * Get the datacontenttype property: Content type of data value.
     * @return the datacontenttype value.
     */
    public String getDatacontenttype() {
        return this.datacontenttype;
    }

    /**
     * Set the datacontenttype property: Content type of data value.
     * @param datacontenttype the datacontenttype value to set.
     *
     * @return the CloudEvent object itself.
     */
    public CloudEvent setDatacontenttype(String datacontenttype) {
        this.datacontenttype = datacontenttype;
        return this;
    }

    /**
     * Get the subject property: This describes the subject of the event in the context of the event producer
     * (identified by source).
     * @return the subject value.
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Set the subject property: This describes the subject of the event in the context of the event producer
     * (identified by source).
     * @param subject the subject value to set.
     *
     * @return the CloudEvent object itself.
     */
    public CloudEvent setSubject(String subject) {
        this.subject = subject;
        return this;
    }
}
