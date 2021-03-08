// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.models;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

/**
 * An instance of PageViewPerf represents: a page view with no performance data, a page view with performance data, or
 * just the performance data of an earlier page request.
 */
@Fluent
public final class PageViewPerfData extends MonitorDomain {
    /*
     * Identifier of a page view instance. Used for correlation between page
     * view and other telemetry items.
     */
    @JsonProperty(value = "id", required = true)
    private String id;

    /*
     * Event name. Keep it low cardinality to allow proper grouping and useful
     * metrics.
     */
    @JsonProperty(value = "name", required = true)
    private String name;

    /*
     * Request URL with all query string parameters
     */
    @JsonProperty(value = "url")
    private String url;

    /*
     * Request duration in format: DD.HH:MM:SS.MMMMMM. For a page view
     * (PageViewData), this is the duration. For a page view with performance
     * information (PageViewPerfData), this is the page load time. Must be less
     * than 1000 days.
     */
    @JsonProperty(value = "duration")
    private String duration;

    /*
     * Performance total in TimeSpan 'G' (general long) format:
     * d:hh:mm:ss.fffffff
     */
    @JsonProperty(value = "perfTotal")
    private String perfTotal;

    /*
     * Network connection time in TimeSpan 'G' (general long) format:
     * d:hh:mm:ss.fffffff
     */
    @JsonProperty(value = "networkConnect")
    private String networkConnect;

    /*
     * Sent request time in TimeSpan 'G' (general long) format:
     * d:hh:mm:ss.fffffff
     */
    @JsonProperty(value = "sentRequest")
    private String sentRequest;

    /*
     * Received response time in TimeSpan 'G' (general long) format:
     * d:hh:mm:ss.fffffff
     */
    @JsonProperty(value = "receivedResponse")
    private String receivedResponse;

    /*
     * DOM processing time in TimeSpan 'G' (general long) format:
     * d:hh:mm:ss.fffffff
     */
    @JsonProperty(value = "domProcessing")
    private String domProcessing;

    /*
     * Collection of custom properties.
     */
    @JsonProperty(value = "properties")
    private Map<String, String> properties;

    /*
     * Collection of custom measurements.
     */
    @JsonProperty(value = "measurements")
    private Map<String, Double> measurements;

    /**
     * Get the id property: Identifier of a page view instance. Used for correlation between page view and other
     * telemetry items.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: Identifier of a page view instance. Used for correlation between page view and other
     * telemetry items.
     *
     * @param id the id value to set.
     * @return the PageViewPerfData object itself.
     */
    public PageViewPerfData setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the name property: Event name. Keep it low cardinality to allow proper grouping and useful metrics.
     *
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: Event name. Keep it low cardinality to allow proper grouping and useful metrics.
     *
     * @param name the name value to set.
     * @return the PageViewPerfData object itself.
     */
    public PageViewPerfData setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the url property: Request URL with all query string parameters.
     *
     * @return the url value.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Set the url property: Request URL with all query string parameters.
     *
     * @param url the url value to set.
     * @return the PageViewPerfData object itself.
     */
    public PageViewPerfData setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Get the duration property: Request duration in format: DD.HH:MM:SS.MMMMMM. For a page view (PageViewData), this
     * is the duration. For a page view with performance information (PageViewPerfData), this is the page load time.
     * Must be less than 1000 days.
     *
     * @return the duration value.
     */
    public String getDuration() {
        return this.duration;
    }

    /**
     * Set the duration property: Request duration in format: DD.HH:MM:SS.MMMMMM. For a page view (PageViewData), this
     * is the duration. For a page view with performance information (PageViewPerfData), this is the page load time.
     * Must be less than 1000 days.
     *
     * @param duration the duration value to set.
     * @return the PageViewPerfData object itself.
     */
    public PageViewPerfData setDuration(String duration) {
        this.duration = duration;
        return this;
    }

    /**
     * Get the perfTotal property: Performance total in TimeSpan 'G' (general long) format: d:hh:mm:ss.fffffff.
     *
     * @return the perfTotal value.
     */
    public String getPerfTotal() {
        return this.perfTotal;
    }

    /**
     * Set the perfTotal property: Performance total in TimeSpan 'G' (general long) format: d:hh:mm:ss.fffffff.
     *
     * @param perfTotal the perfTotal value to set.
     * @return the PageViewPerfData object itself.
     */
    public PageViewPerfData setPerfTotal(String perfTotal) {
        this.perfTotal = perfTotal;
        return this;
    }

    /**
     * Get the networkConnect property: Network connection time in TimeSpan 'G' (general long) format:
     * d:hh:mm:ss.fffffff.
     *
     * @return the networkConnect value.
     */
    public String getNetworkConnect() {
        return this.networkConnect;
    }

    /**
     * Set the networkConnect property: Network connection time in TimeSpan 'G' (general long) format:
     * d:hh:mm:ss.fffffff.
     *
     * @param networkConnect the networkConnect value to set.
     * @return the PageViewPerfData object itself.
     */
    public PageViewPerfData setNetworkConnect(String networkConnect) {
        this.networkConnect = networkConnect;
        return this;
    }

    /**
     * Get the sentRequest property: Sent request time in TimeSpan 'G' (general long) format: d:hh:mm:ss.fffffff.
     *
     * @return the sentRequest value.
     */
    public String getSentRequest() {
        return this.sentRequest;
    }

    /**
     * Set the sentRequest property: Sent request time in TimeSpan 'G' (general long) format: d:hh:mm:ss.fffffff.
     *
     * @param sentRequest the sentRequest value to set.
     * @return the PageViewPerfData object itself.
     */
    public PageViewPerfData setSentRequest(String sentRequest) {
        this.sentRequest = sentRequest;
        return this;
    }

    /**
     * Get the receivedResponse property: Received response time in TimeSpan 'G' (general long) format:
     * d:hh:mm:ss.fffffff.
     *
     * @return the receivedResponse value.
     */
    public String getReceivedResponse() {
        return this.receivedResponse;
    }

    /**
     * Set the receivedResponse property: Received response time in TimeSpan 'G' (general long) format:
     * d:hh:mm:ss.fffffff.
     *
     * @param receivedResponse the receivedResponse value to set.
     * @return the PageViewPerfData object itself.
     */
    public PageViewPerfData setReceivedResponse(String receivedResponse) {
        this.receivedResponse = receivedResponse;
        return this;
    }

    /**
     * Get the domProcessing property: DOM processing time in TimeSpan 'G' (general long) format: d:hh:mm:ss.fffffff.
     *
     * @return the domProcessing value.
     */
    public String getDomProcessing() {
        return this.domProcessing;
    }

    /**
     * Set the domProcessing property: DOM processing time in TimeSpan 'G' (general long) format: d:hh:mm:ss.fffffff.
     *
     * @param domProcessing the domProcessing value to set.
     * @return the PageViewPerfData object itself.
     */
    public PageViewPerfData setDomProcessing(String domProcessing) {
        this.domProcessing = domProcessing;
        return this;
    }

    /**
     * Get the properties property: Collection of custom properties.
     *
     * @return the properties value.
     */
    public Map<String, String> getProperties() {
        return this.properties;
    }

    /**
     * Set the properties property: Collection of custom properties.
     *
     * @param properties the properties value to set.
     * @return the PageViewPerfData object itself.
     */
    public PageViewPerfData setProperties(Map<String, String> properties) {
        this.properties = properties;
        return this;
    }

    /**
     * Get the measurements property: Collection of custom measurements.
     *
     * @return the measurements value.
     */
    public Map<String, Double> getMeasurements() {
        return this.measurements;
    }

    /**
     * Set the measurements property: Collection of custom measurements.
     *
     * @param measurements the measurements value to set.
     * @return the PageViewPerfData object itself.
     */
    public PageViewPerfData setMeasurements(Map<String, Double> measurements) {
        this.measurements = measurements;
        return this;
    }
}
