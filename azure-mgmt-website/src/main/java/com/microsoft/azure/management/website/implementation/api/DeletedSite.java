/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

import org.joda.time.DateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;
import com.microsoft.azure.Resource;

/**
 * Reports deleted site including the timestamp of operation.
 */
@JsonFlatten
public class DeletedSite extends Resource {
    /**
     * Time when the site was deleted.
     */
    @JsonProperty(value = "properties.deletedTimestamp")
    private DateTime deletedTimestamp;

    /**
     * Name of web app.
     */
    @JsonProperty(value = "properties.name")
    private String deletedSiteName;

    /**
     * State of the web app.
     */
    @JsonProperty(value = "properties.state", access = JsonProperty.Access.WRITE_ONLY)
    private String state;

    /**
     * Hostnames associated with web app.
     */
    @JsonProperty(value = "properties.hostNames", access = JsonProperty.Access.WRITE_ONLY)
    private List<String> hostNames;

    /**
     * Name of repository site.
     */
    @JsonProperty(value = "properties.repositorySiteName", access = JsonProperty.Access.WRITE_ONLY)
    private String repositorySiteName;

    /**
     * State indicating whether web app has exceeded its quota usage. Possible
     * values include: 'Normal', 'Exceeded'.
     */
    @JsonProperty(value = "properties.usageState", access = JsonProperty.Access.WRITE_ONLY)
    private UsageState usageState;

    /**
     * True if the site is enabled; otherwise, false. Setting this  value to
     * false disables the site (takes the site off line).
     */
    @JsonProperty(value = "properties.enabled")
    private Boolean enabled;

    /**
     * Hostnames for the web app that are enabled. Hostnames need to be
     * assigned and enabled. If some hostnames are assigned but not enabled
     * the app is not served on those hostnames.
     */
    @JsonProperty(value = "properties.enabledHostNames", access = JsonProperty.Access.WRITE_ONLY)
    private List<String> enabledHostNames;

    /**
     * Management information availability state for the web app. Possible
     * values are Normal or Limited.
     * Normal means that the site is running correctly and that
     * management information for the site is available.
     * Limited means that only partial management information for
     * the site is available and that detailed site information is
     * unavailable. Possible values include: 'Normal', 'Limited',
     * 'DisasterRecoveryMode'.
     */
    @JsonProperty(value = "properties.availabilityState", access = JsonProperty.Access.WRITE_ONLY)
    private SiteAvailabilityState availabilityState;

    /**
     * Hostname SSL states are  used to manage the SSL bindings for site's
     * hostnames.
     */
    @JsonProperty(value = "properties.hostNameSslStates")
    private List<HostNameSslState> hostNameSslStates;

    /**
     * The serverFarmId property.
     */
    @JsonProperty(value = "properties.serverFarmId")
    private String serverFarmId;

    /**
     * Last time web app was modified in UTC.
     */
    @JsonProperty(value = "properties.lastModifiedTimeUtc", access = JsonProperty.Access.WRITE_ONLY)
    private DateTime lastModifiedTimeUtc;

    /**
     * Configuration of web app.
     */
    @JsonProperty(value = "properties.siteConfig")
    private SiteConfigInner siteConfig;

    /**
     * Read-only list of Azure Traffic manager hostnames associated with web
     * app.
     */
    @JsonProperty(value = "properties.trafficManagerHostNames", access = JsonProperty.Access.WRITE_ONLY)
    private List<String> trafficManagerHostNames;

    /**
     * If set indicates whether web app is deployed as a premium app.
     */
    @JsonProperty(value = "properties.premiumAppDeployed", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean premiumAppDeployed;

    /**
     * If set indicates whether to stop SCM (KUDU) site when the web app is
     * stopped. Default is false.
     */
    @JsonProperty(value = "properties.scmSiteAlsoStopped")
    private Boolean scmSiteAlsoStopped;

    /**
     * Read-only property that specifies which slot this app will swap into.
     */
    @JsonProperty(value = "properties.targetSwapSlot", access = JsonProperty.Access.WRITE_ONLY)
    private String targetSwapSlot;

    /**
     * Specification for the hosting environment (App Service Environment) to
     * use for the web app.
     */
    @JsonProperty(value = "properties.hostingEnvironmentProfile")
    private HostingEnvironmentProfile hostingEnvironmentProfile;

    /**
     */
    @JsonProperty(value = "properties.microService")
    private String microService;

    /**
     * Name of gateway app associated with web app.
     */
    @JsonProperty(value = "properties.gatewaySiteName")
    private String gatewaySiteName;

    /**
     * Specifies if the client affinity is enabled when load balancing http
     * request for multiple instances of the web app.
     */
    @JsonProperty(value = "properties.clientAffinityEnabled")
    private Boolean clientAffinityEnabled;

    /**
     * Specifies if the client certificate is enabled for the web app.
     */
    @JsonProperty(value = "properties.clientCertEnabled")
    private Boolean clientCertEnabled;

    /**
     * Specifies if the public hostnames are disabled the web app.
     * If set to true the app is only accessible via API
     * Management process.
     */
    @JsonProperty(value = "properties.hostNamesDisabled")
    private Boolean hostNamesDisabled;

    /**
     * List of comma separated IP addresses that this web app uses for
     * outbound connections. Those can be used when configuring firewall
     * rules for databases accessed by this web app.
     */
    @JsonProperty(value = "properties.outboundIpAddresses", access = JsonProperty.Access.WRITE_ONLY)
    private String outboundIpAddresses;

    /**
     * Size of a function container.
     */
    @JsonProperty(value = "properties.containerSize")
    private Integer containerSize;

    /**
     * Maximum number of workers
     * This only applies to function container.
     */
    @JsonProperty(value = "properties.maxNumberOfWorkers")
    private Integer maxNumberOfWorkers;

    /**
     * This is only valid for web app creation. If specified, web app is
     * cloned from
     * a source web app.
     */
    @JsonProperty(value = "properties.cloningInfo")
    private CloningInfo cloningInfo;

    /**
     * Resource group web app belongs to.
     */
    @JsonProperty(value = "properties.resourceGroup", access = JsonProperty.Access.WRITE_ONLY)
    private String resourceGroup;

    /**
     * Site is a default container.
     */
    @JsonProperty(value = "properties.isDefaultContainer", access = JsonProperty.Access.WRITE_ONLY)
    private Boolean isDefaultContainer;

    /**
     * Default hostname of the web app.
     */
    @JsonProperty(value = "properties.defaultHostName", access = JsonProperty.Access.WRITE_ONLY)
    private String defaultHostName;

    /**
     * Get the deletedTimestamp value.
     *
     * @return the deletedTimestamp value
     */
    public DateTime deletedTimestamp() {
        return this.deletedTimestamp;
    }

    /**
     * Set the deletedTimestamp value.
     *
     * @param deletedTimestamp the deletedTimestamp value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withDeletedTimestamp(DateTime deletedTimestamp) {
        this.deletedTimestamp = deletedTimestamp;
        return this;
    }

    /**
     * Get the deletedSiteName value.
     *
     * @return the deletedSiteName value
     */
    public String deletedSiteName() {
        return this.deletedSiteName;
    }

    /**
     * Set the deletedSiteName value.
     *
     * @param deletedSiteName the deletedSiteName value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withDeletedSiteName(String deletedSiteName) {
        this.deletedSiteName = deletedSiteName;
        return this;
    }

    /**
     * Get the state value.
     *
     * @return the state value
     */
    public String state() {
        return this.state;
    }

    /**
     * Get the hostNames value.
     *
     * @return the hostNames value
     */
    public List<String> hostNames() {
        return this.hostNames;
    }

    /**
     * Get the repositorySiteName value.
     *
     * @return the repositorySiteName value
     */
    public String repositorySiteName() {
        return this.repositorySiteName;
    }

    /**
     * Get the usageState value.
     *
     * @return the usageState value
     */
    public UsageState usageState() {
        return this.usageState;
    }

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean enabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled the enabled value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the enabledHostNames value.
     *
     * @return the enabledHostNames value
     */
    public List<String> enabledHostNames() {
        return this.enabledHostNames;
    }

    /**
     * Get the availabilityState value.
     *
     * @return the availabilityState value
     */
    public SiteAvailabilityState availabilityState() {
        return this.availabilityState;
    }

    /**
     * Get the hostNameSslStates value.
     *
     * @return the hostNameSslStates value
     */
    public List<HostNameSslState> hostNameSslStates() {
        return this.hostNameSslStates;
    }

    /**
     * Set the hostNameSslStates value.
     *
     * @param hostNameSslStates the hostNameSslStates value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withHostNameSslStates(List<HostNameSslState> hostNameSslStates) {
        this.hostNameSslStates = hostNameSslStates;
        return this;
    }

    /**
     * Get the serverFarmId value.
     *
     * @return the serverFarmId value
     */
    public String serverFarmId() {
        return this.serverFarmId;
    }

    /**
     * Set the serverFarmId value.
     *
     * @param serverFarmId the serverFarmId value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withServerFarmId(String serverFarmId) {
        this.serverFarmId = serverFarmId;
        return this;
    }

    /**
     * Get the lastModifiedTimeUtc value.
     *
     * @return the lastModifiedTimeUtc value
     */
    public DateTime lastModifiedTimeUtc() {
        return this.lastModifiedTimeUtc;
    }

    /**
     * Get the siteConfig value.
     *
     * @return the siteConfig value
     */
    public SiteConfigInner siteConfig() {
        return this.siteConfig;
    }

    /**
     * Set the siteConfig value.
     *
     * @param siteConfig the siteConfig value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withSiteConfig(SiteConfigInner siteConfig) {
        this.siteConfig = siteConfig;
        return this;
    }

    /**
     * Get the trafficManagerHostNames value.
     *
     * @return the trafficManagerHostNames value
     */
    public List<String> trafficManagerHostNames() {
        return this.trafficManagerHostNames;
    }

    /**
     * Get the premiumAppDeployed value.
     *
     * @return the premiumAppDeployed value
     */
    public Boolean premiumAppDeployed() {
        return this.premiumAppDeployed;
    }

    /**
     * Get the scmSiteAlsoStopped value.
     *
     * @return the scmSiteAlsoStopped value
     */
    public Boolean scmSiteAlsoStopped() {
        return this.scmSiteAlsoStopped;
    }

    /**
     * Set the scmSiteAlsoStopped value.
     *
     * @param scmSiteAlsoStopped the scmSiteAlsoStopped value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withScmSiteAlsoStopped(Boolean scmSiteAlsoStopped) {
        this.scmSiteAlsoStopped = scmSiteAlsoStopped;
        return this;
    }

    /**
     * Get the targetSwapSlot value.
     *
     * @return the targetSwapSlot value
     */
    public String targetSwapSlot() {
        return this.targetSwapSlot;
    }

    /**
     * Get the hostingEnvironmentProfile value.
     *
     * @return the hostingEnvironmentProfile value
     */
    public HostingEnvironmentProfile hostingEnvironmentProfile() {
        return this.hostingEnvironmentProfile;
    }

    /**
     * Set the hostingEnvironmentProfile value.
     *
     * @param hostingEnvironmentProfile the hostingEnvironmentProfile value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withHostingEnvironmentProfile(HostingEnvironmentProfile hostingEnvironmentProfile) {
        this.hostingEnvironmentProfile = hostingEnvironmentProfile;
        return this;
    }

    /**
     * Get the microService value.
     *
     * @return the microService value
     */
    public String microService() {
        return this.microService;
    }

    /**
     * Set the microService value.
     *
     * @param microService the microService value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withMicroService(String microService) {
        this.microService = microService;
        return this;
    }

    /**
     * Get the gatewaySiteName value.
     *
     * @return the gatewaySiteName value
     */
    public String gatewaySiteName() {
        return this.gatewaySiteName;
    }

    /**
     * Set the gatewaySiteName value.
     *
     * @param gatewaySiteName the gatewaySiteName value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withGatewaySiteName(String gatewaySiteName) {
        this.gatewaySiteName = gatewaySiteName;
        return this;
    }

    /**
     * Get the clientAffinityEnabled value.
     *
     * @return the clientAffinityEnabled value
     */
    public Boolean clientAffinityEnabled() {
        return this.clientAffinityEnabled;
    }

    /**
     * Set the clientAffinityEnabled value.
     *
     * @param clientAffinityEnabled the clientAffinityEnabled value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withClientAffinityEnabled(Boolean clientAffinityEnabled) {
        this.clientAffinityEnabled = clientAffinityEnabled;
        return this;
    }

    /**
     * Get the clientCertEnabled value.
     *
     * @return the clientCertEnabled value
     */
    public Boolean clientCertEnabled() {
        return this.clientCertEnabled;
    }

    /**
     * Set the clientCertEnabled value.
     *
     * @param clientCertEnabled the clientCertEnabled value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withClientCertEnabled(Boolean clientCertEnabled) {
        this.clientCertEnabled = clientCertEnabled;
        return this;
    }

    /**
     * Get the hostNamesDisabled value.
     *
     * @return the hostNamesDisabled value
     */
    public Boolean hostNamesDisabled() {
        return this.hostNamesDisabled;
    }

    /**
     * Set the hostNamesDisabled value.
     *
     * @param hostNamesDisabled the hostNamesDisabled value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withHostNamesDisabled(Boolean hostNamesDisabled) {
        this.hostNamesDisabled = hostNamesDisabled;
        return this;
    }

    /**
     * Get the outboundIpAddresses value.
     *
     * @return the outboundIpAddresses value
     */
    public String outboundIpAddresses() {
        return this.outboundIpAddresses;
    }

    /**
     * Get the containerSize value.
     *
     * @return the containerSize value
     */
    public Integer containerSize() {
        return this.containerSize;
    }

    /**
     * Set the containerSize value.
     *
     * @param containerSize the containerSize value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withContainerSize(Integer containerSize) {
        this.containerSize = containerSize;
        return this;
    }

    /**
     * Get the maxNumberOfWorkers value.
     *
     * @return the maxNumberOfWorkers value
     */
    public Integer maxNumberOfWorkers() {
        return this.maxNumberOfWorkers;
    }

    /**
     * Set the maxNumberOfWorkers value.
     *
     * @param maxNumberOfWorkers the maxNumberOfWorkers value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withMaxNumberOfWorkers(Integer maxNumberOfWorkers) {
        this.maxNumberOfWorkers = maxNumberOfWorkers;
        return this;
    }

    /**
     * Get the cloningInfo value.
     *
     * @return the cloningInfo value
     */
    public CloningInfo cloningInfo() {
        return this.cloningInfo;
    }

    /**
     * Set the cloningInfo value.
     *
     * @param cloningInfo the cloningInfo value to set
     * @return the DeletedSite object itself.
     */
    public DeletedSite withCloningInfo(CloningInfo cloningInfo) {
        this.cloningInfo = cloningInfo;
        return this;
    }

    /**
     * Get the resourceGroup value.
     *
     * @return the resourceGroup value
     */
    public String resourceGroup() {
        return this.resourceGroup;
    }

    /**
     * Get the isDefaultContainer value.
     *
     * @return the isDefaultContainer value
     */
    public Boolean isDefaultContainer() {
        return this.isDefaultContainer;
    }

    /**
     * Get the defaultHostName value.
     *
     * @return the defaultHostName value
     */
    public String defaultHostName() {
        return this.defaultHostName;
    }

}
