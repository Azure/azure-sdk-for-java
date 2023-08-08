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

    /** @return state of the web app */
    String state();

    /** @return hostnames associated with web app */
    Set<String> hostnames();

    /** @return name of repository site */
    String repositorySiteName();

    /** @return state indicating whether web app has exceeded its quota usage */
    UsageState usageState();

    /** @return true if the site is enabled; otherwise, false */
    boolean enabled();

    /** @return host names for the web app that are enabled */
    Set<String> enabledHostNames();

    /** @return management information availability state for the web app */
    SiteAvailabilityState availabilityState();

    /** @return list of SSL states used to manage the SSL bindings for site's hostnames */
    Map<String, HostnameSslState> hostnameSslStates();

    /** @return Last time web app was modified in UTC */
    OffsetDateTime lastModifiedTime();

    /** @return list of Azure Traffic manager host names associated with web app */
    Set<String> trafficManagerHostNames();

    /** @return whether to stop SCM (KUDU) site when the web app is stopped. Default is false. */
    boolean scmSiteAlsoStopped();

    /** @return which slot this app will swap into */
    String targetSwapSlot();

    /**
     * @return if the client affinity is enabled when load balancing http request for multiple instances of the web app
     */
    boolean clientAffinityEnabled();

    /** @return if the client certificate is enabled for the web app */
    boolean clientCertEnabled();

    /**
     * @return if the public hostnames are disabled the web app. If set to true the app is only accessible via API
     *     Management process.
     */
    boolean hostnamesDisabled();

    /**
     * @return list of IP addresses that this web app uses for outbound connections. Those can be used when configuring
     *     firewall rules for databases accessed by this web app.
     */
    Set<String> outboundIPAddresses();

    /** @return size of a function container */
    int containerSize();

    /** @return information about whether the web app is cloned from another */
    CloningInfo cloningInfo();

    /** @return site is a default container */
    boolean isDefaultContainer();

    /** @return default hostname of the web app */
    String defaultHostname();

    /** @return true if the web app is configured to accept only HTTPS requests. HTTP requests will be redirected. */
    boolean httpsOnly();

    /** @return The resource ID of the app service plan */
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

    /** @return the operating system the web app is running on */
    OperatingSystem operatingSystem();
}
