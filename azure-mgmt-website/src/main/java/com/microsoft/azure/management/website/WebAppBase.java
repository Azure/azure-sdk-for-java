/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website;

import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasName;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;
import com.microsoft.azure.management.website.implementation.SiteInner;
import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An immutable client-side representation of an Azure Web App or deployment slot.
 */
public interface WebAppBase<T extends WebAppBase<T>> extends
        HasName,
        Refreshable<T>,
        GroupableResource,
        Wrapper<SiteInner> {

    /**
     * @return state of the web app
     */
    String state();

    /**
     * @return hostnames associated with web app
     */
    Set<String> hostNames();

    /**
     * @return name of repository site
     */
    String repositorySiteName();

    /**
     * @return state indicating whether web app has exceeded its quota usage
     */
    UsageState usageState();

    /**
     * @return true if the site is enabled; otherwise, false
     */
    boolean enabled();

    /**
     * @return host names for the web app that are enabled
     */
    Set<String> enabledHostNames();

    /**
     * @return management information availability state for the web app
     */
    SiteAvailabilityState availabilityState();

    /**
     * @return list of SSL states used to manage the SSL bindings for site's hostnames
     */
    Map<String, HostNameSslState> hostNameSslStates();

    /**
     * @return The resource ID of the app service plan
     */
    String appServicePlanId();

    /**
     * @return Last time web app was modified in UTC
     */
    DateTime lastModifiedTime();

    /**
     * @return list of Azure Traffic manager host names associated with web
     * app
     */
    Set<String> trafficManagerHostNames();

    /**
     * @return whether web app is deployed as a premium app
     */
    boolean isPremiumApp();

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
     * @return list of IP addresses that this web app uses for
     * outbound connections. Those can be used when configuring firewall
     * rules for databases accessed by this web app.
     */
    Set<String> outboundIpAddresses();

    /**
     * @return size of a function container
     */
    int containerSize();

    /**
     * @return information about whether the web app is cloned from another
     */
    CloningInfo cloningInfo();

    /**
     * @return site is a default container
     */
    boolean isDefaultContainer();

    /**
     * @return default hostname of the web app
     */
    String defaultHostName();

    /**
     * @return the default documents
     */
    List<String> defaultDocuments();

    /**
     * @return the .NET Framework version
     */
    NetFrameworkVersion netFrameworkVersion();

    /**
     * @return the version of PHP
     */
    PhpVersion phpVersion();

    /**
     * @return the version of Python
     */
    PythonVersion pythonVersion();

    /**
     * @return the version of Node.JS
     */
    String nodeVersion();

    /**
     * @return if the remote eebugging is enabled
     */
    boolean remoteDebuggingEnabled();

    /**
     * @return the remote debugging version
     */
    RemoteVisualStudioVersion remoteDebuggingVersion();

    /**
     * @return if web socket is enabled
     */
    boolean webSocketsEnabled();

    /**
     * @return if the web app is always on
     */
    boolean alwaysOn();

    /**
     * @return Java version
     */
    JavaVersion javaVersion();

    /**
     * @return Java container
     */
    String javaContainer();

    /**
     * @return Java container version
     */
    String javaContainerVersion();

    /**
     * @return managed pipeline mode
     */
    ManagedPipelineMode managedPipelineMode();

    /**
     * @return the auto swap slot name
     */
    String autoSwapSlotName();

    /**
     * @return the mapping from host names and the host name bindings
     */
    Map<String, HostNameBinding> getHostNameBindings();

    /**
     * Starts the web app or deployment slot.
     */
    void start();

    /**
     * Stops the web app or deployment slot.
     */
    void stop();

    /**
     * Restarts the web app or deployment slot.
     */
    void restart();

    /**************************************************************
     * Fluent interfaces to provision a Web App or deployment slot
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition<FluentT> extends
            DefinitionStages.WithAppServicePlan<FluentT>,
            DefinitionStages.WithHostNameBinding<FluentT>,
            DefinitionStages.WithHostNameSslBinding<FluentT>,
            DefinitionStages.WithWebContainer<FluentT>,
            DefinitionStages.WithCreate<FluentT> {
    }

    /**
     * Grouping of all the site definition stages.
     */
    interface DefinitionStages {
        /**
         * A web app definition allowing app service plan to be set.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithAppServicePlan<FluentT> {
            /**
             * Creates a new free app service plan to use. No custom domains or SSL bindings are available in this plan.
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withNewFreeAppServicePlan();

            /**
             * Creates a new app service plan to use.
             * @param name the name of the app service plan
             * @param pricingTier the pricing tier to use
             * @return the next stage of web app definition
             */
            WithHostNameBinding<FluentT> withNewAppServicePlan(String name, AppServicePricingTier pricingTier);

            /**
             * Uses an existing app service plan for the web app.
             * @param appServicePlanName the name of the existing app service plan
             * @return the next stage of web app definition
             */
            WithHostNameBinding<FluentT> withExistingAppServicePlan(String appServicePlanName);
        }

        /**
         * A web app definition stage allowing host name binding to be specified.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithHostNameBinding<FluentT> extends WithCreate<FluentT> {
            /**
             * Starts the definition of a new host name binding.
             * @return the first stage of a hostname binding definition
             */
            HostNameBinding.DefinitionStages.Blank<WithHostNameSslBinding<FluentT>> defineHostnameBinding();

            /**
             * Defines a list of host names of an Azure managed domain. The DNS record type is
             * defaulted to be CNAME except for the root level domain ("@").
             * @param domain the Azure managed domain
             * @param hostnames the list of sub-domains
             * @return the next stage of web app definition
             */
            WithHostNameSslBinding<FluentT> withManagedHostnameBindings(AppServiceDomain domain, String... hostnames);

            /**
             * Defines a list of host names of an externally purchased domain. The hostnames
             * must be configured before hand to point to the web app.
             * @param domain the external domain name
             * @param hostnames the list of sub-domains
             * @return the next stage of web app definition
             */
            WithHostNameSslBinding<FluentT> withThirdPartyHostnameBinding(String domain, String... hostnames);
        }

        /**
         * A web app definition stage allowing SSL binding to be set.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithHostNameSslBinding<FluentT> extends WithHostNameBinding<FluentT> {
            /**
             * Starts a definition of an SSL binding.
             * @return the first stage of an SSL binding definition
             */
            HostNameSslBinding.DefinitionStages.Blank<WithHostNameSslBinding<FluentT>> defineSslBinding();
        }

        /**
         * A web app definition stage allowing disabling the web app upon creation.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithSiteEnabled<FluentT> {
            /**
             * Disables the web app upon creation.
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withAppDisabledOnCreation();
        }

        /**
         * A web app definition stage allowing setting if SCM site is also stopped when the web app is stopped.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithScmSiteAlsoStopped<FluentT> {
            /**
             * Specifies if SCM site is also stopped when the web app is stopped.
             * @param scmSiteAlsoStopped true if SCM site is also stopped
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withScmSiteAlsoStopped(boolean scmSiteAlsoStopped);
        }

        /**
         * A web app definition stage allowing setting if client affinity is enabled.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithClientAffinityEnabled<FluentT> {
            /**
             * Specifies if client affinity is enabled.
             * @param enabled true if client affinity is enabled
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withClientAffinityEnabled(boolean enabled);
        }

        /**
         * A web app definition stage allowing setting if client cert is enabled.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithClientCertEnabled<FluentT> {
            /**
             * Specifies if client cert is enabled.
             * @param enabled true if client cert is enabled
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withClientCertEnabled(boolean enabled);
        }

        /**
         * A web app definition stage allowing Java web container to be set. This is required
         * after specifying Java version.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithWebContainer<FluentT> {
            /**
             * Specifies the Java web container.
             * @param webContainer the Java web container
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withWebContainer(WebContainer webContainer);
        }

        /**
         * A web app definition stage allowing other configurations to be set. These configurations
         * can be cloned when creating or swapping with a deployment slot.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithSiteConfigs<FluentT> {
            /**
             * Specifies the .NET Framework version.
             * @param version the .NET Framework version
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withNetFrameworkVersion(NetFrameworkVersion version);

            /**
             * Specifies the PHP version.
             * @param version the PHP version
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withPhpVersion(PhpVersion version);

            /**
             * Specifies the Java version.
             * @param version the Java version
             * @return the next stage of web app definition
             */
            WithWebContainer<FluentT> withJavaVersion(JavaVersion version);

            /**
             * Specifies the Python version.
             * @param version the Python version
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withPythonVersion(PythonVersion version);

            /**
             * Specifies the platform architecture to use.
             * @param platform the platform architecture
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withPlatformArchitecture(PlatformArchitecture platform);

            /**
             * Specifies if web sockets are enabled.
             * @param enabled true if web sockets are enabled
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withWebSocketsEnabled(boolean enabled);

            /**
             * Specifies if the VM powering the web app is always powered on.
             * @param alwaysOn true if the web app is always powered on
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withWebAppAlwaysOn(boolean alwaysOn);

            /**
             * Specifies the managed pipeline mode.
             * @param managedPipelineMode managed pipeline mode
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withManagedPipelineMode(ManagedPipelineMode managedPipelineMode);

            /**
             * Specifies the slot name to auto-swap when a deployment is completed in this web app / deployment slot.
             * @param slotName the name of the slot, or 'production', to auto-swap
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withAutoSwapSlotName(String slotName);

            /**
             * Specifies the Visual Studio version for remote debugging.
             * @param remoteVisualStudioVersion the Visual Studio version for remote debugging
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withRemoteDebuggingEnabled(RemoteVisualStudioVersion remoteVisualStudioVersion);

            /**
             * Disables remote debugging.
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withRemoteDebuggingDisabled();

            /**
             * Adds a default document.
             * @param document default document
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withDefaultDocument(String document);

            /**
             * Adds a list of default documents.
             * @param documents list of default documents
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withDefaultDocuments(List<String> documents);

            /**
             * Removes a default document.
             * @param document default document to remove
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withoutDefaultDocument(String document);
        }

        /**
         * A site definition with sufficient inputs to create a new web app /
         * deployments slot in the cloud, but exposing additional optional
         * inputs to specify.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithCreate<FluentT> extends
                Creatable<FluentT>,
                WithSiteEnabled<FluentT>,
                WithScmSiteAlsoStopped<FluentT>,
                WithClientAffinityEnabled<FluentT>,
                WithClientCertEnabled<FluentT>,
                WithSiteConfigs<FluentT> {
        }
    }

    /**
     * Grouping of all the web app update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the web app update allowing app service plan to be set.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithAppServicePlan<FluentT> {
            /**
             * Creates a new free app service plan to use. No custom domains or SSL bindings are available in this plan.
             * @return the next stage of web app update
             */
            Update<FluentT> withNewFreeAppServicePlan();

            /**
             * Creates a new app service plan to use.
             * @param name the name of the app service plan
             * @param pricingTier the pricing tier to use
             * @return the next stage of web app update
             */
            Update<FluentT> withNewAppServicePlan(String name, AppServicePricingTier pricingTier);

            /**
             * Uses an existing app service plan for the web app.
             * @param appServicePlanName the name of the existing app service plan
             * @return the next stage of web app update
             */
            Update<FluentT> withExistingAppServicePlan(String appServicePlanName);
        }

        /**
         * The stage of the web app update allowing host name binding to be set.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithHostNameBinding<FluentT> {
            /**
             * Starts the definition of a new host name binding.
             * @return the first stage of a hostname binding update
             */
            HostNameBinding.UpdateDefinitionStages.Blank<Update<FluentT>> defineHostnameBinding();

            /**
             * Defines a list of host names of an Azure managed domain. The DNS record type is
             * defaulted to be CNAME except for the root level domain ("@").
             * @param domain the Azure managed domain
             * @param hostnames the list of sub-domains
             * @return the next stage of web app update
             */
            Update<FluentT> withManagedHostnameBindings(AppServiceDomain domain, String... hostnames);

            /**
             * Defines a list of host names of an externally purchased domain. The hostnames
             * must be configured before hand to point to the web app.
             * @param domain the external domain name
             * @param hostnames the list of sub-domains
             * @return the next stage of web app update
             */
            Update<FluentT> withThirdPartyHostnameBinding(String domain, String... hostnames);

            /**
             * Unbinds a hostname from the web app.
             * @param hostname the hostname to unbind
             * @return the next stage of web app update
             */
            Update<FluentT> withoutHostnameBinding(String hostname);
        }

        /**
         * The stage of the web app update allowing SSL binding to be set.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithHostNameSslBinding<FluentT> {
            /**
             * Starts a definition of an SSL binding.
             * @return the first stage of an SSL binding definition
             */
            HostNameSslBinding.UpdateDefinitionStages.Blank<Update<FluentT>> defineSslBinding();

            /**
             * Removes an SSL binding for a specific hostname.
             * @param hostname the hostname to remove SSL certificate from
             * @return the next stage of web app update
             */
            Update<FluentT> withoutSslBinding(String hostname);
        }

        /**
         * The stage of the web app update allowing disabling the web app upon creation.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithSiteEnabled<FluentT> {
            /**
             * Disables the web app upon creation.
             * @return the next stage of web app update
             */
            Update<FluentT> withAppDisabledOnCreation();
        }

        /**
         * The stage of the web app update allowing setting if SCM site is also stopped when the web app is stopped.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithScmSiteAlsoStopped<FluentT> {
            /**
             * Specifies if SCM site is also stopped when the web app is stopped.
             * @param scmSiteAlsoStopped true if SCM site is also stopped
             * @return the next stage of web app update
             */
            Update<FluentT> withScmSiteAlsoStopped(boolean scmSiteAlsoStopped);
        }

        /**
         * The stage of the web app update allowing setting if client affinity is enabled.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithClientAffinityEnabled<FluentT> {
            /**
             * Specifies if client affinity is enabled.
             * @param enabled true if client affinity is enabled
             * @return the next stage of web app update
             */
            Update<FluentT> withClientAffinityEnabled(boolean enabled);
        }

        /**
         * The stage of the web app update allowing setting if client cert is enabled.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithClientCertEnabled<FluentT> {
            /**
             * Specifies if client cert is enabled.
             * @param enabled true if client cert is enabled
             * @return the next stage of web app update
             */
            Update<FluentT> withClientCertEnabled(boolean enabled);
        }

        /**
         * The stage of the web app update allowing other configurations to be set. These configurations
         * can be cloned when creating or swapping with a deployment slot.
         * @param <FluentT> the type of the resource, either a web app or a deployment slot
         */
        interface WithSiteConfigs<FluentT> {
            /**
             * Specifies the .NET Framework version.
             * @param version the .NET Framework version
             * @return the next stage of web app update
             */
            Update<FluentT> withNetFrameworkVersion(NetFrameworkVersion version);

            /**
             * Specifies the PHP version.
             * @param version the PHP version
             * @return the next stage of web app update
             */
            Update<FluentT> withPhpVersion(PhpVersion version);

            /**
             * Specifies the Java version.
             * @param version the Java version
             * @return the next stage of web app update
             */
            Update<FluentT> withJavaVersion(JavaVersion version);

            /**
             * Specifies the Java web container.
             * @param webContainer the Java web container
             * @return the next stage of web app update
             */
            Update<FluentT> withWebContainer(WebContainer webContainer);

            /**
             * Specifies the Python version.
             * @param version the Python version
             * @return the next stage of web app update
             */
            Update<FluentT> withPythonVersion(PythonVersion version);

            /**
             * Specifies the platform architecture to use.
             * @param platform the platform architecture
             * @return the next stage of web app update
             */
            Update<FluentT> withPlatformArchitecture(PlatformArchitecture platform);

            /**
             * Specifies if web sockets are enabled.
             * @param enabled true if web sockets are enabled
             * @return the next stage of web app update
             */
            Update<FluentT> withWebSocketsEnabled(boolean enabled);

            /**
             * Specifies if the VM powering the web app is always powered on.
             * @param alwaysOn true if the web app is always powered on
             * @return the next stage of web app update
             */
            Update<FluentT> withWebAppAlwaysOn(boolean alwaysOn);

            /**
             * Specifies the managed pipeline mode.
             * @param managedPipelineMode managed pipeline mode
             * @return the next stage of web app update
             */
            Update<FluentT> withManagedPipelineMode(ManagedPipelineMode managedPipelineMode);

            /**
             * Specifies the slot name to auto-swap when a deployment is completed in this web app / deployment slot.
             * @param slotName the name of the slot, or 'production', to auto-swap
             * @return the next stage of web app update
             */
            Update<FluentT> withAutoSwapSlotName(String slotName);

            /**
             * Specifies the Visual Studio version for remote debugging.
             * @param remoteVisualStudioVersion the Visual Studio version for remote debugging
             * @return the next stage of web app update
             */
            Update<FluentT> withRemoteDebuggingEnabled(RemoteVisualStudioVersion remoteVisualStudioVersion);

            /**
             * Disables remote debugging.
             * @return the next stage of web app update
             */
            Update<FluentT> withRemoteDebuggingDisabled();

            /**
             * Adds a default document.
             * @param document default document
             * @return the next stage of web app update
             */
            Update<FluentT> withDefaultDocument(String document);

            /**
             * Adds a list of default documents.
             * @param documents list of default documents
             * @return the next stage of web app update
             */
            Update<FluentT> withDefaultDocuments(List<String> documents);

            /**
             * Removes a default document.
             * @param document default document to remove
             * @return the next stage of web app update
             */
            Update<FluentT> withoutDefaultDocument(String document);
        }
    }

    /**
     * The template for a web app update operation, containing all the settings that can be modified.
     */
    interface Update<FluentT> extends
            Appliable<FluentT>,
            UpdateStages.WithAppServicePlan<FluentT>,
            UpdateStages.WithHostNameBinding<FluentT>,
            UpdateStages.WithHostNameSslBinding<FluentT>,
            UpdateStages.WithClientAffinityEnabled<FluentT>,
            UpdateStages.WithClientCertEnabled<FluentT>,
            UpdateStages.WithScmSiteAlsoStopped<FluentT>,
            UpdateStages.WithSiteEnabled<FluentT>,
            UpdateStages.WithSiteConfigs<FluentT> {
    }
}