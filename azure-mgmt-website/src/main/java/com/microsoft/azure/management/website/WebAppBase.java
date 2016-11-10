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
 * An immutable client-side representation of an Azure Web App.
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
     * @return management information availability state for the web app.
     *
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
     * @return the mapping from host names and the host name bindings
     */
    Map<String, HostNameBinding> getHostNameBindings();

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
     * Start the web app or deployment slot.
     */
    void start();

    /**
     * Stop the web app or deployment slot.
     */
    void stop();

    /**
     * Restart the web app or deployment slot.
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
            DefinitionStages.WithCreate<FluentT> {
    }

    /**
     * Grouping of all the site definition stages.
     */
    interface DefinitionStages {
        /**
         * A web app definition allowing app service plan to be set.
         */
        interface WithAppServicePlan<FluentT> {
            WithCreate<FluentT> withNewFreeAppServicePlan();
            WithHostNameBinding<FluentT> withNewAppServicePlan(String name, AppServicePricingTier pricingTier);
            WithHostNameBinding<FluentT> withExistingAppServicePlan(String appServicePlanName);
        }

        interface WithHostNameBinding<FluentT> extends WithCreate<FluentT> {
            HostNameBinding.DefinitionStages.Blank<WithHostNameSslBinding<FluentT>> defineNewHostNameBinding(String hostname);
            WithHostNameSslBinding<FluentT> withManagedHostNameBindings(Domain domain, String... hostnames);
            WithHostNameSslBinding<FluentT> withVerifiedHostNameBinding(String domain, String... hostnames);
        }

        interface WithHostNameSslBinding<FluentT> extends WithHostNameBinding<FluentT> {
            HostNameSslBinding.DefinitionStages.Blank<WithHostNameSslBinding<FluentT>> defineNewSSLBindingForHostName(String hostname);
        }

        interface WithSiteEnabled<FluentT> {
            WithCreate<FluentT> withAppDisabledOnCreation();
        }

        interface WithScmSiteAlsoStopped<FluentT> {
            WithCreate<FluentT> withScmSiteAlsoStopped(boolean scmSiteAlsoStopped);
        }

        interface WithClientAffinityEnabled<FluentT> {
            WithCreate<FluentT> withClientAffinityEnabled(boolean enabled);
        }

        interface WithClientCertEnabled<FluentT> {
            WithCreate<FluentT> withClientCertEnabled(boolean enabled);
        }

        interface WithNetFrameworkVersion<FluentT> {
            WithCreate withNetFrameworkVersion(NetFrameworkVersion version);
        }

        interface WithPhpVersion<FluentT> {
            WithCreate<FluentT> withPhpVersion(PhpVersion version);
        }

        interface WithJavaVersion<FluentT> {
            WithCreate<FluentT> withJavaVersion(JavaVersion version);
        }

        interface WithWebContainer<FluentT> {
            WithCreate<FluentT> withWebContainer(WebContainer webContainer);
        }

        interface WithPythonVersion<FluentT> {
            WithCreate<FluentT> withPythonVersion(PythonVersion version);
        }

        interface WithPlatformArchitecture<FluentT> {
            WithCreate<FluentT> withPlatformArchitecture(PlatformArchitecture platform);
        }

        interface WithWebSockets<FluentT> {
            WithCreate<FluentT> withWebSocketsEnabled(boolean enabled);
        }

        interface WithAlwaysOn<FluentT> {
            WithCreate withWebAppAlwaysOn(boolean alwaysOn);
        }

        interface WithManagedPipelineMode<FluentT> {
            WithCreate<FluentT> withManagedPipelineMode(ManagedPipelineMode managedPipelineMode);
        }

        interface WithAutoSwap<FluentT> {
            WithCreate<FluentT> withAutoSwapSlotName(String slotName);
        }

        interface WithRemoteDebugging<FluentT> {
            WithCreate<FluentT> withRemoteDebuggingEnabled(RemoteVisualStudioVersion remoteVisualStudioVersion);
            WithCreate<FluentT> withReoteDebuggingDisabled();
        }

        interface WithDefaultDocuments<FluentT> {
            WithCreate<FluentT> withDefaultDocument(String document);
            WithCreate<FluentT> withDefaultDocuments(List<String> documents);
            WithCreate<FluentT> withoutDefaultDocument(String document);
        }

        /**
         * A site definition with sufficient inputs to create a new
         * website in the cloud, but exposing additional optional inputs to
         * specify.
         */
        interface WithCreate<FluentT> extends
                Creatable<FluentT>,
                WithSiteEnabled<FluentT>,
                WithScmSiteAlsoStopped<FluentT>,
                WithClientAffinityEnabled<FluentT>,
                WithClientCertEnabled<FluentT>,
                WithNetFrameworkVersion<FluentT>,
                WithPhpVersion<FluentT>,
                WithJavaVersion<FluentT>,
                WithWebContainer<FluentT>,
                WithPythonVersion<FluentT>,
                WithPlatformArchitecture<FluentT>,
                WithWebSockets<FluentT>,
                WithAlwaysOn<FluentT>,
                WithManagedPipelineMode<FluentT>,
                WithAutoSwap<FluentT>,
                WithRemoteDebugging<FluentT>,
                WithDefaultDocuments<FluentT> {
        }
    }

    /**
     * Grouping of all the site update stages.
     */
    interface UpdateStages {
        /**
         * A site definition allowing server farm to be set.
         */
        interface WithAppServicePlan {
            Update withNewFreeAppServicePlan();
            Update withNewAppServicePlan(String name, AppServicePricingTier pricingTier);
            Update withExistingAppServicePlan(String appServicePlanName);
        }

        interface WithHostNameBinding {
            HostNameBinding.UpdateDefinitionStages.Blank<Update> defineNewHostNameBinding(String hostname);
            Update withManagedHostNameBindings(Domain domain, String... hostnames);
            Update withVerifiedHostNameBinding(String domain, String... hostnames);
        }

        interface WithHostNameSslBinding {
            HostNameSslBinding.UpdateDefinitionStages.Blank<Update> defineNewSSLBindingForHostName(String hostname);
        }
    }

    /**
     * The template for a site update operation, containing all the settings that can be modified.
     */
    interface Update<FluentT extends WebAppBase<FluentT>> extends
            Appliable<FluentT>,
            UpdateStages.WithAppServicePlan,
            UpdateStages.WithHostNameBinding,
            UpdateStages.WithHostNameSslBinding {
    }
}