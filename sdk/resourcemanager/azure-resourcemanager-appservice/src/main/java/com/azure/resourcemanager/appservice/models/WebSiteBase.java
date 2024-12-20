// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

/**
 * An immutable client-side representation of an Azure Web App or Function App.
 */
public interface WebSiteBase extends Resource, HasResourceGroup, HasInnerModel<SiteInner> {

    /**
     * Gets state of the web app.
     *
     * @return state of the web app
     */
    String state();

    /**
     * Gets hostnames associated with web app.
     *
     * @return hostnames associated with web app
     */
    Set<String> hostnames();

    /**
     * Gets name of repository site.
     *
     * @return name of repository site
     */
    String repositorySiteName();

    /**
     * Gets state indicating whether web app has exceeded its quota usage.
     *
     * @return state indicating whether web app has exceeded its quota usage
     */
    UsageState usageState();

    /**
     * Check whether the site is enabled.
     *
     * @return true if the site is enabled; otherwise, false
     */
    boolean enabled();

    /**
     * Gets host names for the web app that are enabled.
     *
     * @return host names for the web app that are enabled
     */
    Set<String> enabledHostNames();

    /**
     * Gets management information availability state for the web app.
     *
     * @return management information availability state for the web app
     */
    SiteAvailabilityState availabilityState();

    /**
     * Gets list of SSL states used to manage the SSL bindings for site's hostnames.
     *
     * @return list of SSL states used to manage the SSL bindings for site's hostnames
     */
    Map<String, HostnameSslState> hostnameSslStates();

    /**
     * Gets last time web app was modified in UTC.
     *
     * @return Last time web app was modified in UTC
     */
    OffsetDateTime lastModifiedTime();

    /**
     * Gets list of Azure Traffic manager host names associated with web app.
     *
     * @return list of Azure Traffic manager host names associated with web app
     */
    Set<String> trafficManagerHostNames();

    /**
     * Check whether to stop SCM (KUDU) site when the web app is stopped.
     *
     * @return whether to stop SCM (KUDU) site when the web app is stopped. Default is false.
     */
    boolean scmSiteAlsoStopped();

    /**
     * Gets which slot this app will swap into.
     *
     * @return which slot this app will swap into
     */
    String targetSwapSlot();

    /**
     * Check whether the client affinity is enabled when load balancing http request for multiple instances of the web app.
     *
     * @return if the client affinity is enabled when load balancing http request for multiple instances of the web app
     */
    boolean clientAffinityEnabled();

    /**
     * Check whether the client certificate is enabled for the web app.
     *
     * @return if the client certificate is enabled for the web app
     */
    boolean clientCertEnabled();

    /**
     * Check whether the public hostnames are disabled for the web app.
     *
     * @return if the public hostnames are disabled the web app. If set to true the app is only accessible via API
     *     Management process.
     */
    boolean hostnamesDisabled();

    /**
     * Gets list of IP addresses that this web app uses for outbound connections.
     *
     * @return list of IP addresses that this web app uses for outbound connections. Those can be used when configuring
     *     firewall rules for databases accessed by this web app.
     */
    Set<String> outboundIPAddresses();

    /**
     * Gets size of a function container.
     *
     * @return size of a function container
     */
    int containerSize();

    /**
     * Gets information about whether the web app is cloned from another.
     *
     * @return information about whether the web app is cloned from another
     */
    CloningInfo cloningInfo();

    /**
     * Check whether site is a default container.
     *
     * @return site is a default container
     */
    boolean isDefaultContainer();

    /**
     * Gets default hostname of the web app.
     *
     * @return default hostname of the web app
     */
    String defaultHostname();

    /**
     * Check whether the web app is configured to accept only HTTPS requests.
     *
     * @return true if the web app is configured to accept only HTTPS requests. HTTP requests will be redirected.
     */
    boolean httpsOnly();

    /**
     * Gets the resource ID of the app service plan.
     *
     * @return The resource ID of the app service plan
     */
    String appServicePlanId();

    /**
     * Get the identity property: Managed service identity.
     *
     * @return the identity value.
     */
    ManagedServiceIdentity identity();

    /**
     * Get the hyperV property: Hyper-V sandbox.
     *
     * @return the hyperV value.
     */
    boolean hyperV();

    /**
     * Get the hostingEnvironmentProfile property: App Service Environment to use for the app.
     *
     * @return the hostingEnvironmentProfile value.
     */
    HostingEnvironmentProfile hostingEnvironmentProfile();

    /**
     * Get the clientCertExclusionPaths property: client certificate authentication comma-separated exclusion paths.
     *
     * @return the clientCertExclusionPaths value.
     */
    Set<String> clientCertExclusionPaths();

    /**
     * Get the possibleOutboundIpAddresses property: List of IP addresses that the app uses for outbound connections
     * (e.g. database access). Includes VIPs from all tenants except dataComponent. Read-only.
     *
     * @return the possibleOutboundIpAddresses value.
     */
    Set<String> possibleOutboundIpAddresses();

    /**
     * Get the dailyMemoryTimeQuota property: Maximum allowed daily memory-time quota (applicable on dynamic apps only).
     *
     * @return the dailyMemoryTimeQuota value.
     */
    int dailyMemoryTimeQuota();

    /**
     * Get the suspendedTill property: App suspended till in case memory-time quota is exceeded.
     *
     * @return the suspendedTill value.
     */
    OffsetDateTime suspendedTill();

    /**
     * Get the maxNumberOfWorkers property: Maximum number of workers. This only applies to Functions container.
     *
     * @return the maxNumberOfWorkers value.
     */
    int maxNumberOfWorkers();

    /**
     * Get the slotSwapStatus property: Status of the last deployment slot swap operation.
     *
     * @return the slotSwapStatus value.
     */
    SlotSwapStatus slotSwapStatus();

    /**
     * Get the redundancyMode property: Site redundancy mode.
     *
     * @return the redundancyMode value.
     */
    RedundancyMode redundancyMode();

    /**
     * Gets the operating system the web app is running on.
     *
     * @return the operating system the web app is running on
     */
    OperatingSystem operatingSystem();
}
