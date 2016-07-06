/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.website.implementation.SiteConfigInner;
import com.microsoft.azure.management.website.implementation.SiteInner;
import org.joda.time.DateTime;

import java.util.List;

/**
 * An immutable client-side representation of an Azure website.
 */
public interface Site extends
        GroupableResource,
        Refreshable<Site>,
        Updatable<Site.Update>,
        Wrapper<SiteInner> {

    /**
     * @return name of web app
     */
    String siteName();

    /**
     * @return state of the web app
     */
    String state();

    /**
     * @return hostnames associated with web app
     */
    List<String> hostNames();

    /**
     * @return name of repository site
     */
    String repositorySiteName();

    /**
     * @return state indicating whether web app has exceeded its quota usage. Possible
     * values include: 'Normal', 'Exceeded'.
     */
    UsageState usageState();

    /**
     * @return true if the site is enabled; otherwise, false. Setting this  value to
     * false disables the site (takes the site off line).
     */
    boolean enabled();

    /**
     * @return hostnames for the web app that are enabled. Hostnames need to be
     * assigned and enabled. If some hostnames are assigned but not enabled
     * the app is not served on those hostnames.
     */
    List<String> enabledHostNames();

    /**
     * @return management information availability state for the web app. Possible
     * values are Normal or Limited.
     * Normal means that the site is running correctly and that
     * management information for the site is available.
     * Limited means that only partial management information for
     * the site is available and that detailed site information is
     * unavailable. Possible values include: 'Normal', 'Limited',
     * 'DisasterRecoveryMode'.
     */
    SiteAvailabilityState availabilityState();

    /**
     * @return list of SSL states used to manage the SSL bindings for site's hostnames
     */
    List<HostNameSslState> hostNameSslStates();

    /**
     * @return The serverFarmId property
     */
    String serverFarmId();

    /**
     * @return Last time web app was modified in UTC
     */
    DateTime lastModifiedTimeUtc();

    /**
     * @return Configuration of web app
     */
    SiteConfigInner siteConfig();

    /**
     * @return list of Azure Traffic manager host names associated with web
     * app
     */
    List<String> trafficManagerHostNames();

    /**
     * @return whether web app is deployed as a premium app
     */
    boolean premiumAppDeployed();

    /**
     * @return whether to stop SCM (KUDU) site when the web app is
     * stopped. Default is false.
     */
    boolean scmSiteAlsoStopped();

    /**
     * @return which slot this app will swap into
     */
    String targetSwapSlot();

    /**
     * @return specification for the hosting environment (App Service Environment) to
     * use for the web app
     */
    HostingEnvironmentProfile hostingEnvironmentProfile();

    /**
     * @return the micro-service name
     */
    String microService();

    /**
     * @return name of gateway app associated with web app
     */
    String gatewaySiteName();

    /**
     * @return if the client affinity is enabled when load balancing http
     * request for multiple instances of the web app
     */
    boolean clientAffinityEnabled();

    /**
     * @return if the client certificate is enabled for the web app
     */
    boolean clientCertEnabled();

    /**
     * @return if the public hostnames are disabled the web app.
     * If set to true the app is only accessible via API
     * Management process.
     */
    boolean hostNamesDisabled();

    /**
     * @return list of comma separated IP addresses that this web app uses for
     * outbound connections. Those can be used when configuring firewall
     * rules for databases accessed by this web app.
     */
    String outboundIpAddresses();

    /**
     * @return size of a function container
     */
    int containerSize();

    /**
     * @return maximum number of workers
     * This only applies to function container.
     */
    int maxNumberOfWorkers();

    /**
     * @return information about whether the web app is cloned from another
     */
    CloningInfo cloningInfo();

    /**
     * @return resource group web app belongs to
     */
    String resourceGroup();

    /**
     * @return site is a default container
     */
    boolean isDefaultContainer();

    /**
     * @return default hostname of the web app
     */
    String defaultHostName();

    /**************************************************************
     * Fluent interfaces to provision a StorageAccount
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithAppServicePlan,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the site definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the site definition.
         */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * A site definition allowing resource group to be set.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithAppServicePlan> {
        }

        /**
         * A site definition allowing server farm to be set.
         */
        interface WithAppServicePlan {
            WithCreate withNewAppServicePlan();
            WithCreate withNewAppServicePlan(String name, AppServicePricingTier pricingTier);
            WithCreate withExistingAppServicePlan(String appServicePlanName);
        }

        /**
         * A site definition with sufficient inputs to create a new
         * website in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate extends
                Creatable<Site> {
        }
    }

    /**
     * Grouping of all the site update stages.
     */
    interface UpdateStages {

    }

    /**
     * The template for a site update operation, containing all the settings that can be modified.
     */
    interface Update {
    }
}

