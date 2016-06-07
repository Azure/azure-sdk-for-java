/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation.api;

import java.util.List;
import com.microsoft.azure.SubResource;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * Load balancer Probe.
 */
@JsonFlatten
public class Probe extends SubResource {
    /**
     * Gets Load balancer rules that use this probe.
     */
    @JsonProperty(value = "properties.loadBalancingRules")
    private List<SubResource> loadBalancingRules;

    /**
     * Gets or sets the protocol of the end point. Possible values are http pr
     * Tcp. If Tcp is specified, a received ACK is required for the probe to
     * be successful. If http is specified,a 200 OK response from the
     * specifies URI is required for the probe to be successful. Possible
     * values include: 'Http', 'Tcp'.
     */
    @JsonProperty(value = "properties.protocol", required = true)
    private String protocol;

    /**
     * Gets or sets Port for communicating the probe. Possible values range
     * from 1 to 65535, inclusive.
     */
    @JsonProperty(value = "properties.port", required = true)
    private int port;

    /**
     * Gets or sets the interval, in seconds, for how frequently to probe the
     * endpoint for health status. Typically, the interval is slightly less
     * than half the allocated timeout period (in seconds) which allows two
     * full probes before taking the instance out of rotation. The default
     * value is 15, the minimum value is 5.
     */
    @JsonProperty(value = "properties.intervalInSeconds")
    private Integer intervalInSeconds;

    /**
     * Gets or sets the number of probes where if no response, will result in
     * stopping further traffic from being delivered to the endpoint. This
     * values allows endponints to be taken out of rotation faster or slower
     * than the typical times used in Azure.
     */
    @JsonProperty(value = "properties.numberOfProbes")
    private Integer numberOfProbes;

    /**
     * Gets or sets the URI used for requesting health status from the VM.
     * Path is required if a protocol is set to http. Otherwise, it is not
     * allowed. There is no default value.
     */
    @JsonProperty(value = "properties.requestPath")
    private String requestPath;

    /**
     * Gets or sets Provisioning state of the PublicIP resource
     * Updating/Deleting/Failed.
     */
    @JsonProperty(value = "properties.provisioningState")
    private String provisioningState;

    /**
     * Gets name of the resource that is unique within a resource group. This
     * name can be used to access the resource.
     */
    private String name;

    /**
     * A unique read-only string that changes whenever the resource is updated.
     */
    private String etag;

    /**
     * Get the loadBalancingRules value.
     *
     * @return the loadBalancingRules value
     */
    public List<SubResource> loadBalancingRules() {
        return this.loadBalancingRules;
    }

    /**
     * Set the loadBalancingRules value.
     *
     * @param loadBalancingRules the loadBalancingRules value to set
     * @return the Probe object itself.
     */
    public Probe withLoadBalancingRules(List<SubResource> loadBalancingRules) {
        this.loadBalancingRules = loadBalancingRules;
        return this;
    }

    /**
     * Get the protocol value.
     *
     * @return the protocol value
     */
    public String protocol() {
        return this.protocol;
    }

    /**
     * Set the protocol value.
     *
     * @param protocol the protocol value to set
     * @return the Probe object itself.
     */
    public Probe withProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Get the port value.
     *
     * @return the port value
     */
    public int port() {
        return this.port;
    }

    /**
     * Set the port value.
     *
     * @param port the port value to set
     * @return the Probe object itself.
     */
    public Probe withPort(int port) {
        this.port = port;
        return this;
    }

    /**
     * Get the intervalInSeconds value.
     *
     * @return the intervalInSeconds value
     */
    public Integer intervalInSeconds() {
        return this.intervalInSeconds;
    }

    /**
     * Set the intervalInSeconds value.
     *
     * @param intervalInSeconds the intervalInSeconds value to set
     * @return the Probe object itself.
     */
    public Probe withIntervalInSeconds(Integer intervalInSeconds) {
        this.intervalInSeconds = intervalInSeconds;
        return this;
    }

    /**
     * Get the numberOfProbes value.
     *
     * @return the numberOfProbes value
     */
    public Integer numberOfProbes() {
        return this.numberOfProbes;
    }

    /**
     * Set the numberOfProbes value.
     *
     * @param numberOfProbes the numberOfProbes value to set
     * @return the Probe object itself.
     */
    public Probe withNumberOfProbes(Integer numberOfProbes) {
        this.numberOfProbes = numberOfProbes;
        return this;
    }

    /**
     * Get the requestPath value.
     *
     * @return the requestPath value
     */
    public String requestPath() {
        return this.requestPath;
    }

    /**
     * Set the requestPath value.
     *
     * @param requestPath the requestPath value to set
     * @return the Probe object itself.
     */
    public Probe withRequestPath(String requestPath) {
        this.requestPath = requestPath;
        return this;
    }

    /**
     * Get the provisioningState value.
     *
     * @return the provisioningState value
     */
    public String provisioningState() {
        return this.provisioningState;
    }

    /**
     * Set the provisioningState value.
     *
     * @param provisioningState the provisioningState value to set
     * @return the Probe object itself.
     */
    public Probe withProvisioningState(String provisioningState) {
        this.provisioningState = provisioningState;
        return this;
    }

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Set the name value.
     *
     * @param name the name value to set
     * @return the Probe object itself.
     */
    public Probe withName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the etag value.
     *
     * @return the etag value
     */
    public String etag() {
        return this.etag;
    }

    /**
     * Set the etag value.
     *
     * @param etag the etag value to set
     * @return the Probe object itself.
     */
    public Probe withEtag(String etag) {
        this.etag = etag;
        return this;
    }

}
