// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.inner.SiteInner;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasName;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import java.io.File;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure Web App or deployment slot. */
@Fluent
public interface WebAppBase extends HasName, GroupableResource<AppServiceManager, SiteInner> {

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

    /** @return The resource ID of the app service plan */
    String appServicePlanId();

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
    boolean hostNamesDisabled();

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

    /** @return the default documents */
    List<String> defaultDocuments();

    /** @return the .NET Framework version */
    NetFrameworkVersion netFrameworkVersion();

    /** @return the version of PHP */
    PhpVersion phpVersion();

    /** @return the version of Python */
    PythonVersion pythonVersion();

    /** @return the version of Node.JS */
    String nodeVersion();

    /** @return if the remote eebugging is enabled */
    boolean remoteDebuggingEnabled();

    /** @return the remote debugging version */
    RemoteVisualStudioVersion remoteDebuggingVersion();

    /** @return if web socket is enabled */
    boolean webSocketsEnabled();

    /** @return if the web app is always on */
    boolean alwaysOn();

    /** @return Java version */
    JavaVersion javaVersion();

    /** @return Java container */
    String javaContainer();

    /** @return Java container version */
    String javaContainerVersion();

    /** @return managed pipeline mode */
    ManagedPipelineMode managedPipelineMode();

    /** @return the auto swap slot name */
    String autoSwapSlotName();

    /** @return true if the web app is configured to accept only HTTPS requests. HTTP requests will be redirected. */
    boolean httpsOnly();

    /** @return the state of FTP / FTPS service */
    FtpsState ftpsState();

    /** @return the virtual applications and their virtual directories in this web app */
    List<VirtualApplication> virtualApplications();

    /** @return whether to allow clients to connect over http2.0 */
    boolean http20Enabled();

    /** @return the minimum version of TLS required for SSL requests. */
    SupportedTlsVersions minTlsVersion();

    /** @return whether local MySQL is enabled */
    boolean localMySqlEnabled();

    /** @return the SCM configuration for the web app */
    ScmType scmType();

    /** @return the root directory for the web app */
    String documentRoot();

    /**
     * @return the System Assigned (Local) Managed Service Identity specific Active Directory tenant ID assigned to the
     *     web app.
     */
    String systemAssignedManagedServiceIdentityTenantId();

    /**
     * @return the System Assigned (Local) Managed Service Identity specific Active Directory service principal ID
     *     assigned to the web app.
     */
    String systemAssignedManagedServiceIdentityPrincipalId();

    /** @return The ids of the user assigned identities */
    Set<String> userAssignedManagedServiceIdentityIds();

    /** @return the app settings defined on the web app */
    Map<String, AppSetting> getAppSettings();

    /** @return the app settings defined on the web app */
    Mono<Map<String, AppSetting>> getAppSettingsAsync();

    /** @return the connection strings defined on the web app */
    Map<String, ConnectionString> getConnectionStrings();

    /** @return the connection strings defined on the web app */
    Mono<Map<String, ConnectionString>> getConnectionStringsAsync();

    /** @return the authentication configuration defined on the web app */
    WebAppAuthentication getAuthenticationConfig();

    /** @return the authentication configuration defined on the web app */
    Mono<WebAppAuthentication> getAuthenticationConfigAsync();

    /** @return the operating system the web app is running on */
    OperatingSystem operatingSystem();

    /** @return the architecture of the platform, either 32 bit (x86) or 64 bit (x64). */
    PlatformArchitecture platformArchitecture();

    /** @return the Linux app framework and version if this is a Linux web app. */
    String linuxFxVersion();

    /** @return the diagnostic logs configuration */
    WebAppDiagnosticLogs diagnosticLogsConfig();

    /** @return the mapping from host names and the host name bindings */
    Map<String, HostnameBinding> getHostnameBindings();

    /** @return the mapping from host names and the host name bindings */
    Mono<Map<String, HostnameBinding>> getHostnameBindingsAsync();

    /** @return the URL and credentials for publishing through FTP or Git */
    PublishingProfile getPublishingProfile();

    /** @return the URL and credentials for publishing through FTP or Git */
    Mono<PublishingProfile> getPublishingProfileAsync();

    /** @return the source control information for the web app */
    WebAppSourceControl getSourceControl();

    /** @return the source control information for the web app */
    Mono<WebAppSourceControl> getSourceControlAsync();

    /**
     * First step specifying the parameters to make a web deployment (MS Deploy) to the web app.
     *
     * @return a stage to create web deployment
     */
    WebDeployment.DefinitionStages.WithPackageUri deploy();

    /** @return the last lines of docker logs for a Linux web app */
    byte[] getContainerLogs();

    /** @return the last lines of docker logs for a Linux web app */
    Mono<byte[]> getContainerLogsAsync();

    /** @return the zipped archive of docker logs for a Linux web app */
    byte[] getContainerLogsZip();

    /** @return the zipped archive of docker logs for a Linux web app */
    Mono<byte[]> getContainerLogsZipAsync();

    /** @return a open stream to the application logs */
    InputStream streamApplicationLogs();

    /** @return a open stream to the HTTP logs */
    InputStream streamHttpLogs();

    /** @return a open stream to the trace logs */
    InputStream streamTraceLogs();

    /** @return a open stream to the deployment logs */
    InputStream streamDeploymentLogs();

    /** @return a open stream to all logs */
    InputStream streamAllLogs();

    /** @return an Observable streaming application logs */
    Flux<String> streamApplicationLogsAsync();

    /** @return an Observable streaming HTTP logs */
    Flux<String> streamHttpLogsAsync();

    /** @return an Observable streaming trace logs */
    Flux<String> streamTraceLogsAsync();

    /** @return an Observable streaming deployment logs */
    Flux<String> streamDeploymentLogsAsync();

    /** @return an Observable streaming all logs */
    Flux<String> streamAllLogsAsync();

    /**
     * Verifies the ownership of the domain for a certificate order by verifying a hostname of the domain is bound to
     * this web app.
     *
     * @param certificateOrderName the name of the certificate order
     * @param domainVerificationToken the domain verification token for the certificate order
     */
    void verifyDomainOwnership(String certificateOrderName, String domainVerificationToken);

    /**
     * Verifies the ownership of the domain for a certificate order by verifying a hostname of the domain is bound to
     * this web app.
     *
     * @param certificateOrderName the name of the certificate order
     * @param domainVerificationToken the domain verification token for the certificate order
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> verifyDomainOwnershipAsync(String certificateOrderName, String domainVerificationToken);

    /** Starts the web app or deployment slot. */
    void start();

    /**
     * Starts the web app or deployment slot.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> startAsync();

    /** Stops the web app or deployment slot. */
    void stop();

    /**
     * Stops the web app or deployment slot.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> stopAsync();

    /** Restarts the web app or deployment slot. */
    void restart();

    /**
     * Restarts the web app or deployment slot.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> restartAsync();

    /**
     * Swaps the app running in the current web app / slot with the app running in the specified slot.
     *
     * @param slotName the target slot to swap with. Use 'production' for the production slot.
     */
    void swap(String slotName);

    /**
     * Swaps the app running in the current web app / slot with the app running in the specified slot.
     *
     * @param slotName the target slot to swap with. Use 'production' for the production slot.
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> swapAsync(String slotName);

    /**
     * Apply the slot (or sticky) configurations from the specified slot to the current one. This is useful for "Swap
     * with Preview".
     *
     * @param slotName the target slot to apply configurations from
     */
    void applySlotConfigurations(String slotName);

    /**
     * Apply the slot (or sticky) configurations from the specified slot to the current one. This is useful for "Swap
     * with Preview".
     *
     * @param slotName the target slot to apply configurations from
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> applySlotConfigurationsAsync(String slotName);

    /** Reset the slot to its original configurations. */
    void resetSlotConfigurations();

    /**
     * Reset the slot to its original configurations.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> resetSlotConfigurationsAsync();

    /**
     * Deploys a ZIP file onto the Azure specialized Java SE image on this web app.
     *
     * @param zipFile the ZIP file to upload
     */
    void zipDeploy(File zipFile);

    /**
     * Deploys a ZIP file onto the Azure specialized Java SE image on this web app.
     *
     * @param zipFile the ZIP file to upload
     * @return a completable of the operation
     */
    Mono<Void> zipDeployAsync(File zipFile);

    /**
     * Deploys a ZIP file onto the Azure specialized Java SE image on this web app.
     *
     * @param zipFile the ZIP file to upload
     */
    void zipDeploy(InputStream zipFile);

    /**
     * Deploys a ZIP file onto the Azure specialized Java SE image on this web app.
     *
     * @param zipFile the ZIP file to upload
     * @return a completable of the operation
     */
    Mono<Void> zipDeployAsync(InputStream zipFile);

    /**************************************************************
     * Fluent interfaces to provision a Web App or deployment slot.
     **************************************************************/

    /**
     * The entirety of the web app base definition.
     *
     * @param <FluentT> the type of the resource
     */
    interface Definition<FluentT>
        extends DefinitionStages.WithWebContainer<FluentT>,
            DefinitionStages.WithCreate<FluentT>,
            DefinitionStages.WithSystemAssignedIdentityBasedAccessOrCreate<FluentT>,
            DefinitionStages.WithUserAssignedManagedServiceIdentityBasedAccessOrCreate<FluentT> {
    }

    /** Grouping of all the site definition stages. */
    interface DefinitionStages {
        /**
         * A web app definition stage allowing host name binding to be specified.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithHostNameBinding<FluentT> {
            /**
             * Starts the definition of a new host name binding.
             *
             * @return the first stage of a hostname binding definition
             */
            HostnameBinding.DefinitionStages.Blank<WithCreate<FluentT>> defineHostnameBinding();

            /**
             * Defines a list of host names of an Azure managed domain. The DNS record type is defaulted to be CNAME
             * except for the root level domain ("@").
             *
             * @param domain the Azure managed domain
             * @param hostnames the list of sub-domains
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withManagedHostnameBindings(AppServiceDomain domain, String... hostnames);

            /**
             * Defines a list of host names of an externally purchased domain. The hostnames must be configured before
             * hand to point to the web app.
             *
             * @param domain the external domain name
             * @param hostnames the list of sub-domains
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withThirdPartyHostnameBinding(String domain, String... hostnames);
        }

        /**
         * A web app definition stage allowing SSL binding to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithHostNameSslBinding<FluentT> {
            /**
             * Starts a definition of an SSL binding.
             *
             * @return the first stage of an SSL binding definition
             */
            HostnameSslBinding.DefinitionStages.Blank<WithCreate<FluentT>> defineSslBinding();
        }

        /**
         * A web app definition stage allowing disabling the web app upon creation.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithSiteEnabled<FluentT> {
            /**
             * Disables the web app upon creation.
             *
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withAppDisabledOnCreation();
        }

        /**
         * A web app definition stage allowing setting if SCM site is also stopped when the web app is stopped.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithScmSiteAlsoStopped<FluentT> {
            /**
             * Specifies if SCM site is also stopped when the web app is stopped.
             *
             * @param scmSiteAlsoStopped true if SCM site is also stopped
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withScmSiteAlsoStopped(boolean scmSiteAlsoStopped);
        }

        /**
         * A web app definition stage allowing setting if client affinity is enabled.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithClientAffinityEnabled<FluentT> {
            /**
             * Specifies if client affinity is enabled.
             *
             * @param enabled true if client affinity is enabled
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withClientAffinityEnabled(boolean enabled);
        }

        /**
         * A web app definition stage allowing setting if client cert is enabled.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithClientCertEnabled<FluentT> {
            /**
             * Specifies if client cert is enabled.
             *
             * @param enabled true if client cert is enabled
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withClientCertEnabled(boolean enabled);
        }

        /**
         * A web app definition stage allowing Java web container to be set. This is required after specifying Java
         * version.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithWebContainer<FluentT> {
            /**
             * Specifies the Java web container.
             *
             * @param webContainer the Java web container
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withWebContainer(WebContainer webContainer);
        }

        /**
         * A web app definition stage allowing other configurations to be set. These configurations can be cloned when
         * creating or swapping with a deployment slot.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithSiteConfigs<FluentT> {
            /**
             * Specifies the .NET Framework version.
             *
             * @param version the .NET Framework version
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withNetFrameworkVersion(NetFrameworkVersion version);

            /**
             * Specifies the PHP version.
             *
             * @param version the PHP version
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withPhpVersion(PhpVersion version);

            /**
             * Turn off PHP support.
             *
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withoutPhp();

            /**
             * Specifies the Java version.
             *
             * @param version the Java version
             * @return the next stage of the definition
             */
            WithWebContainer<FluentT> withJavaVersion(JavaVersion version);

            /**
             * Specifies the Python version.
             *
             * @param version the Python version
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withPythonVersion(PythonVersion version);

            /**
             * Specifies the platform architecture to use.
             *
             * @param platform the platform architecture
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withPlatformArchitecture(PlatformArchitecture platform);

            /**
             * Specifies if web sockets are enabled.
             *
             * @param enabled true if web sockets are enabled
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withWebSocketsEnabled(boolean enabled);

            /**
             * Specifies if the VM powering the web app is always powered on.
             *
             * @param alwaysOn true if the web app is always powered on
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withWebAppAlwaysOn(boolean alwaysOn);

            /**
             * Specifies the managed pipeline mode.
             *
             * @param managedPipelineMode managed pipeline mode
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withManagedPipelineMode(ManagedPipelineMode managedPipelineMode);

            /**
             * Specifies the slot name to auto-swap when a deployment is completed in this web app / deployment slot.
             *
             * @param slotName the name of the slot, or 'production', to auto-swap
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withAutoSwapSlotName(String slotName);

            /**
             * Specifies the Visual Studio version for remote debugging.
             *
             * @param remoteVisualStudioVersion the Visual Studio version for remote debugging
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withRemoteDebuggingEnabled(RemoteVisualStudioVersion remoteVisualStudioVersion);

            /**
             * Disables remote debugging.
             *
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withRemoteDebuggingDisabled();

            /**
             * Adds a default document.
             *
             * @param document default document
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withDefaultDocument(String document);

            /**
             * Adds a list of default documents.
             *
             * @param documents list of default documents
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withDefaultDocuments(List<String> documents);

            /**
             * Removes a default document.
             *
             * @param document default document to remove
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withoutDefaultDocument(String document);

            /**
             * Sets whether the web app only accepts HTTPS traffic.
             *
             * @param httpsOnly true if the web app only accepts HTTPS traffic
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withHttpsOnly(boolean httpsOnly);

            /**
             * Sets whether the web app accepts HTTP 2.0 traffic.
             *
             * @param http20Enabled true if the web app accepts HTTP 2.0 traffic
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withHttp20Enabled(boolean http20Enabled);

            /**
             * Sets whether the web app supports certain type of FTP(S).
             *
             * @param ftpsState the FTP(S) configuration
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withFtpsState(FtpsState ftpsState);

            /**
             * Sets the virtual applications in the web app.
             *
             * @param virtualApplications the list of virtual applications in the web app
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withVirtualApplications(List<VirtualApplication> virtualApplications);

            /**
             * Sets the minimum version of TLS required for SSL requests.
             *
             * @param minTlsVersion the minimum version of TLS required for SSL requests.
             * @return the next stage of web app definition
             */
            WithCreate<FluentT> withMinTlsVersion(SupportedTlsVersions minTlsVersion);
        }

        /**
         * A web app definition stage allowing app settings to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithAppSettings<FluentT> {
            /**
             * Adds an app setting to the web app.
             *
             * @param key the key for the app setting
             * @param value the value for the app setting
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withAppSetting(String key, String value);

            /**
             * Specifies the app settings for the web app as a {@link Map}.
             *
             * @param settings a {@link Map} of app settings
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withAppSettings(Map<String, String> settings);

            /**
             * Adds an app setting to the web app. This app setting will be swapped as well after a deployment slot
             * swap.
             *
             * @param key the key for the app setting
             * @param value the value for the app setting
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withStickyAppSetting(String key, String value);

            /**
             * Specifies the app settings for the web app as a {@link Map}. These app settings will be swapped as well
             * after a deployment slot swap.
             *
             * @param settings a {@link Map} of app settings
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withStickyAppSettings(Map<String, String> settings);
        }

        /**
         * A web app definition stage allowing connection strings to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithConnectionString<FluentT> {
            /**
             * Adds a connection string to the web app.
             *
             * @param name the name of the connection string
             * @param value the connection string value
             * @param type the connection string type
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withConnectionString(String name, String value, ConnectionStringType type);

            /**
             * Adds a connection string to the web app. This connection string will be swapped as well after a
             * deployment slot swap.
             *
             * @param name the name of the connection string
             * @param value the connection string value
             * @param type the connection string type
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withStickyConnectionString(String name, String value, ConnectionStringType type);
        }

        /**
         * A web app definition stage allowing source control to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithSourceControl<FluentT> {
            /**
             * Starts the definition of a new source control.
             *
             * @return the first stage of a source control definition
             */
            WebAppSourceControl.DefinitionStages.Blank<WithCreate<FluentT>> defineSourceControl();

            /**
             * Specifies the source control to be a local Git repository on the web app.
             *
             * @return the next stage of the definition
             */
            WithCreate<FluentT> withLocalGitSourceControl();
        }

        /**
         * A web app definition stage allowing authentication to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithAuthentication<FluentT> {
            /**
             * Specifies the definition of a new authentication configuration.
             *
             * @return the first stage of an authentication definition
             */
            WebAppAuthentication.DefinitionStages.Blank<WithCreate<FluentT>> defineAuthentication();
        }

        /**
         * A web app definition stage allowing diagnostic logging to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithDiagnosticLogging<FluentT> {
            /**
             * Specifies the definition of a new diagnostic logs configuration.
             *
             * @return the first stage of an diagnostic logs definition
             */
            WebAppDiagnosticLogs.DefinitionStages.Blank<WithCreate<FluentT>> defineDiagnosticLogsConfiguration();

            /**
             * Specifies the configuration for container logging for Linux web apps.
             *
             * @param quotaInMB the limit that restricts file system usage by app diagnostics logs. Value can range from
             *     25 MB and 100 MB.
             * @param retentionDays maximum days of logs that will be available
             * @return the next stage of the web app definition
             */
            WithCreate<FluentT> withContainerLoggingEnabled(int quotaInMB, int retentionDays);

            /**
             * Specifies the configuration for container logging for Linux web apps. Logs will be stored on the file
             * system for up to 35 MB.
             *
             * @return the next stage of the web app definition
             */
            WithCreate<FluentT> withContainerLoggingEnabled();

            /**
             * Disable the container logging for Linux web apps.
             *
             * @return the next stage of the web app definition
             */
            WithCreate<FluentT> withContainerLoggingDisabled();
        }

        /**
         * A web app definition stage allowing System Assigned Managed Service Identity to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithManagedServiceIdentity<FluentT> {
            /**
             * Specifies that System Assigned Managed Service Identity needs to be enabled in the web app.
             *
             * @return the next stage of the web app definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate<FluentT> withSystemAssignedManagedServiceIdentity();

            /**
             * Specifies that User Assigned Managed Service Identity needs to be enabled in the web app.
             *
             * @return the next stage of the web app definition
             */
            WithUserAssignedManagedServiceIdentityBasedAccessOrCreate<FluentT> withUserAssignedManagedServiceIdentity();

            /**
             * Specifies that System Assigned (Local) Managed Service Identity needs to be disabled.
             *
             * @return the next stage of the update
             */
            Update<FluentT> withoutSystemAssignedManagedServiceIdentity();
        }

        /**
         * The stage of the System Assigned (Local) Managed Service Identity enabled web app allowing to set access role
         * for the identity.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithSystemAssignedIdentityBasedAccessOrCreate<FluentT> extends WithCreate<FluentT> {
            /**
             * Specifies that web app's system assigned (local) identity should have the given access (described by the
             * role) on an ARM resource identified by the resource ID. Applications running on the web app will have the
             * same permission (role) on the ARM resource.
             *
             * @param resourceId the ARM identifier of the resource
             * @param role access role to assigned to the web app's local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate<FluentT> withSystemAssignedIdentityBasedAccessTo(
                String resourceId, BuiltInRole role);

            /**
             * Specifies that web app's system assigned (local) identity should have the given access (described by the
             * role) on the resource group that web app resides. Applications running on the web app will have the same
             * permission (role) on the resource group.
             *
             * @param role access role to assigned to the web app's local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate<FluentT>
                withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole role);

            /**
             * Specifies that web app's system assigned (local) identity should have the access (described by the role
             * definition) on an ARM resource identified by the resource ID. Applications running on the web app will
             * have the same permission (role) on the ARM resource.
             *
             * @param resourceId scope of the access represented in ARM resource ID format
             * @param roleDefinitionId access role definition to assigned to the web app's local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate<FluentT> withSystemAssignedIdentityBasedAccessTo(
                String resourceId, String roleDefinitionId);

            /**
             * Specifies that web app's system assigned (local) identity should have the access (described by the role
             * definition) on the resource group that web app resides. Applications running on the web app will have the
             * same permission (role) on the resource group.
             *
             * @param roleDefinitionId access role definition to assigned to the web app's local identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate<FluentT>
                withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(String roleDefinitionId);
        }

        /** The stage of the web app update allowing to add User Assigned (External) Managed Service Identities. */
        interface WithUserAssignedManagedServiceIdentityBasedAccessOrCreate<FluentT> extends WithCreate<FluentT> {
            /**
             * Specifies the definition of a not-yet-created user assigned identity to be associated with the web app.
             *
             * @param creatableIdentity a creatable identity definition
             * @return the next stage of the definition.
             */
            WithUserAssignedManagedServiceIdentityBasedAccessOrCreate<FluentT>
                withNewUserAssignedManagedServiceIdentity(Creatable<Identity> creatableIdentity);

            /**
             * Specifies an existing user assigned identity to be associated with the web app.
             *
             * @param identity the identity
             * @return the next stage of the definition.
             */
            WithUserAssignedManagedServiceIdentityBasedAccessOrCreate<FluentT>
                withExistingUserAssignedManagedServiceIdentity(Identity identity);

            /**
             * Specifies that an user assigned identity associated with the web app should be removed.
             *
             * @param identityId ARM resource id of the identity
             * @return the next stage of the virtual machine update
             */
            Update<FluentT> withoutUserAssignedManagedServiceIdentity(String identityId);
        }

        /**
         * A site definition with sufficient inputs to create a new web app / deployments slot in the cloud, but
         * exposing additional optional inputs to specify.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithCreate<FluentT>
            extends Creatable<FluentT>,
                GroupableResource.DefinitionWithTags<WithCreate<FluentT>>,
                WithClientAffinityEnabled<FluentT>,
                WithClientCertEnabled<FluentT>,
                WithScmSiteAlsoStopped<FluentT>,
                WithSiteConfigs<FluentT>,
                WithAppSettings<FluentT>,
                WithConnectionString<FluentT>,
                WithSourceControl<FluentT>,
                WithHostNameBinding<FluentT>,
                WithHostNameSslBinding<FluentT>,
                WithAuthentication<FluentT>,
                WithDiagnosticLogging<FluentT>,
                WithManagedServiceIdentity<FluentT> {
        }
    }

    /** Grouping of all the web app update stages. */
    interface UpdateStages {
        /**
         * The stage of the web app update allowing host name binding to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithHostNameBinding<FluentT> {
            /**
             * Starts the definition of a new host name binding.
             *
             * @return the first stage of a hostname binding update
             */
            HostnameBinding.UpdateDefinitionStages.Blank<Update<FluentT>> defineHostnameBinding();

            /**
             * Defines a list of host names of an Azure managed domain. The DNS record type is defaulted to be CNAME
             * except for the root level domain ("@").
             *
             * @param domain the Azure managed domain
             * @param hostnames the list of sub-domains
             * @return the next stage of web app update
             */
            Update<FluentT> withManagedHostnameBindings(AppServiceDomain domain, String... hostnames);

            /**
             * Defines a list of host names of an externally purchased domain. The hostnames must be configured before
             * hand to point to the web app.
             *
             * @param domain the external domain name
             * @param hostnames the list of sub-domains
             * @return the next stage of web app update
             */
            Update<FluentT> withThirdPartyHostnameBinding(String domain, String... hostnames);

            /**
             * Unbinds a hostname from the web app.
             *
             * @param hostname the hostname to unbind
             * @return the next stage of web app update
             */
            Update<FluentT> withoutHostnameBinding(String hostname);
        }

        /**
         * The stage of the web app update allowing SSL binding to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithHostNameSslBinding<FluentT> {
            /**
             * Starts a definition of an SSL binding.
             *
             * @return the first stage of an SSL binding definition
             */
            HostnameSslBinding.UpdateDefinitionStages.Blank<Update<FluentT>> defineSslBinding();

            /**
             * Removes an SSL binding for a specific hostname.
             *
             * @param hostname the hostname to remove SSL certificate from
             * @return the next stage of web app update
             */
            Update<FluentT> withoutSslBinding(String hostname);
        }

        /**
         * The stage of the web app update allowing disabling the web app upon creation.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithSiteEnabled<FluentT> {
            /**
             * Disables the web app upon creation.
             *
             * @return the next stage of web app update
             */
            Update<FluentT> withAppDisabledOnCreation();
        }

        /**
         * The stage of the web app update allowing setting if SCM site is also stopped when the web app is stopped.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithScmSiteAlsoStopped<FluentT> {
            /**
             * Specifies if SCM site is also stopped when the web app is stopped.
             *
             * @param scmSiteAlsoStopped true if SCM site is also stopped
             * @return the next stage of web app update
             */
            Update<FluentT> withScmSiteAlsoStopped(boolean scmSiteAlsoStopped);
        }

        /**
         * The stage of the web app update allowing setting if client affinity is enabled.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithClientAffinityEnabled<FluentT> {
            /**
             * Specifies if client affinity is enabled.
             *
             * @param enabled true if client affinity is enabled
             * @return the next stage of web app update
             */
            Update<FluentT> withClientAffinityEnabled(boolean enabled);
        }

        /**
         * The stage of the web app update allowing setting if client cert is enabled.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithClientCertEnabled<FluentT> {
            /**
             * Specifies if client cert is enabled.
             *
             * @param enabled true if client cert is enabled
             * @return the next stage of web app update
             */
            Update<FluentT> withClientCertEnabled(boolean enabled);
        }

        /**
         * The stage of the web app update allowing Java web container to be set. This is required after specifying Java
         * version.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithWebContainer<FluentT> {
            /**
             * Specifies the Java web container.
             *
             * @param webContainer the Java web container
             * @return the next stage of the web app update
             */
            Update<FluentT> withWebContainer(WebContainer webContainer);
        }

        /**
         * The stage of the web app update allowing other configurations to be set. These configurations can be cloned
         * when creating or swapping with a deployment slot.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithSiteConfigs<FluentT> {
            /**
             * Specifies the .NET Framework version.
             *
             * @param version the .NET Framework version
             * @return the next stage of web app update
             */
            Update<FluentT> withNetFrameworkVersion(NetFrameworkVersion version);

            /**
             * Specifies the PHP version.
             *
             * @param version the PHP version
             * @return the next stage of web app update
             */
            Update<FluentT> withPhpVersion(PhpVersion version);

            /**
             * Specifies the Java version.
             *
             * @param version the Java version
             * @return the next stage of web app update
             */
            WithWebContainer<FluentT> withJavaVersion(JavaVersion version);

            /**
             * Turn off Java support.
             *
             * @return the next stage of web app update
             */
            Update<FluentT> withoutJava();

            /**
             * Specifies the Python version.
             *
             * @param version the Python version
             * @return the next stage of web app update
             */
            Update<FluentT> withPythonVersion(PythonVersion version);

            /**
             * Turn off Python support.
             *
             * @return the next stage of web app update
             */
            Update<FluentT> withoutPython();

            /**
             * Specifies the platform architecture to use.
             *
             * @param platform the platform architecture
             * @return the next stage of web app update
             */
            Update<FluentT> withPlatformArchitecture(PlatformArchitecture platform);

            /**
             * Specifies if web sockets are enabled.
             *
             * @param enabled true if web sockets are enabled
             * @return the next stage of web app update
             */
            Update<FluentT> withWebSocketsEnabled(boolean enabled);

            /**
             * Specifies if the VM powering the web app is always powered on.
             *
             * @param alwaysOn true if the web app is always powered on
             * @return the next stage of web app update
             */
            Update<FluentT> withWebAppAlwaysOn(boolean alwaysOn);

            /**
             * Specifies the managed pipeline mode.
             *
             * @param managedPipelineMode managed pipeline mode
             * @return the next stage of web app update
             */
            Update<FluentT> withManagedPipelineMode(ManagedPipelineMode managedPipelineMode);

            /**
             * Specifies the slot name to auto-swap when a deployment is completed in this web app / deployment slot.
             *
             * @param slotName the name of the slot, or 'production', to auto-swap
             * @return the next stage of web app update
             */
            Update<FluentT> withAutoSwapSlotName(String slotName);

            /**
             * Specifies the Visual Studio version for remote debugging.
             *
             * @param remoteVisualStudioVersion the Visual Studio version for remote debugging
             * @return the next stage of web app update
             */
            Update<FluentT> withRemoteDebuggingEnabled(RemoteVisualStudioVersion remoteVisualStudioVersion);

            /**
             * Disables remote debugging.
             *
             * @return the next stage of web app update
             */
            Update<FluentT> withRemoteDebuggingDisabled();

            /**
             * Adds a default document.
             *
             * @param document default document
             * @return the next stage of web app update
             */
            Update<FluentT> withDefaultDocument(String document);

            /**
             * Adds a list of default documents.
             *
             * @param documents list of default documents
             * @return the next stage of web app update
             */
            Update<FluentT> withDefaultDocuments(List<String> documents);

            /**
             * Removes a default document.
             *
             * @param document default document to remove
             * @return the next stage of web app update
             */
            Update<FluentT> withoutDefaultDocument(String document);

            /**
             * Sets whether the web app only accepts HTTPS traffic.
             *
             * @param httpsOnly true if the web app only accepts HTTPS traffic
             * @return the next stage of web app update
             */
            Update<FluentT> withHttpsOnly(boolean httpsOnly);

            /**
             * Sets whether the web app accepts HTTP 2.0 traffic.
             *
             * @param http20Enabled true if the web app accepts HTTP 2.0 traffic
             * @return the next stage of web app update
             */
            Update<FluentT> withHttp20Enabled(boolean http20Enabled);

            /**
             * Sets whether the web app supports certain type of FTP(S).
             *
             * @param ftpsState the FTP(S) configuration
             * @return the next stage of web app update
             */
            Update<FluentT> withFtpsState(FtpsState ftpsState);

            /**
             * Sets the virtual applications in the web app.
             *
             * @param virtualApplications the list of virtual applications in the web app
             * @return the next stage of web app update
             */
            Update<FluentT> withVirtualApplications(List<VirtualApplication> virtualApplications);

            /**
             * Sets the minimum version of TLS required for SSL requests.
             *
             * @param minTlsVersion the minimum version of TLS required for SSL requests.
             * @return the next stage of web app definition
             */
            Update<FluentT> withMinTlsVersion(SupportedTlsVersions minTlsVersion);
        }

        /**
         * A web app update stage allowing app settings to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithAppSettings<FluentT> {
            /**
             * Adds an app setting to the web app.
             *
             * @param key the key for the app setting
             * @param value the value for the app setting
             * @return the next stage of the web app update
             */
            Update<FluentT> withAppSetting(String key, String value);

            /**
             * Specifies the app settings for the web app as a {@link Map}.
             *
             * @param settings a {@link Map} of app settings
             * @return the next stage of the web app update
             */
            Update<FluentT> withAppSettings(Map<String, String> settings);

            /**
             * Adds an app setting to the web app. This app setting will stay at the slot during a swap.
             *
             * @param key the key for the app setting
             * @param value the value for the app setting
             * @return the next stage of the web app update
             */
            Update<FluentT> withStickyAppSetting(String key, String value);

            /**
             * Specifies the app settings for the web app as a {@link Map}. These app settings will stay at the slot
             * during a swap.
             *
             * @param settings a {@link Map} of app settings
             * @return the next stage of the web app update
             */
            Update<FluentT> withStickyAppSettings(Map<String, String> settings);

            /**
             * Removes an app setting from the web app.
             *
             * @param key the key of the app setting to remove
             * @return the next stage of the web app update
             */
            Update<FluentT> withoutAppSetting(String key);

            /**
             * Changes the stickiness of an app setting.
             *
             * @param key the key of the app setting to change stickiness
             * @param sticky true if the app setting sticks to the slot during a swap
             * @return the next stage of the web app update
             */
            Update<FluentT> withAppSettingStickiness(String key, boolean sticky);
        }

        /**
         * A web app update stage allowing connection strings to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithConnectionString<FluentT> {
            /**
             * Adds a connection string to the web app.
             *
             * @param name the name of the connection string
             * @param value the connection string value
             * @param type the connection string type
             * @return the next stage of the web app update
             */
            Update<FluentT> withConnectionString(String name, String value, ConnectionStringType type);

            /**
             * Adds a connection string to the web app. This connection string will stay at the slot during a swap.
             *
             * @param name the name of the connection string
             * @param value the connection string value
             * @param type the connection string type
             * @return the next stage of the web app update
             */
            Update<FluentT> withStickyConnectionString(String name, String value, ConnectionStringType type);

            /**
             * Removes a connection string from the web app.
             *
             * @param name the name of the connection string
             * @return the next stage of the web app update
             */
            Update<FluentT> withoutConnectionString(String name);

            /**
             * Changes the stickiness of a connection string.
             *
             * @param name the name of the connection string
             * @param sticky true if the connection string sticks to the slot during a swap
             * @return the next stage of the web app update
             */
            Update<FluentT> withConnectionStringStickiness(String name, boolean sticky);
        }

        /**
         * A web app update stage allowing source control to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithSourceControl<FluentT> {
            /**
             * Starts the definition of a new source control.
             *
             * @return the first stage of a source control definition
             */
            WebAppSourceControl.UpdateDefinitionStages.Blank<Update<FluentT>> defineSourceControl();

            /**
             * Removes source control for deployment from the web app.
             *
             * @return the next stage of the web app update
             */
            Update<FluentT> withoutSourceControl();

            /**
             * Specifies the source control to be a local Git repository on the web app.
             *
             * @return the next stage of the web app update
             */
            Update<FluentT> withLocalGitSourceControl();
        }

        /**
         * A web app definition stage allowing authentication to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithAuthentication<FluentT> {
            /**
             * Specifies the definition of a new authentication configuration.
             *
             * @return the first stage of an authentication definition
             */
            WebAppAuthentication.UpdateDefinitionStages.Blank<Update<FluentT>> defineAuthentication();

            /**
             * Turns off the authentication on the web app.
             *
             * @return the next stage of the web app update
             */
            Update<FluentT> withoutAuthentication();
        }

        /**
         * A web app definition stage allowing diagnostic logging to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithDiagnosticLogging<FluentT> {
            /**
             * Specifies the update of an existing diagnostic logs configuration.
             *
             * @return the first stage of an diagnostic logs update
             */
            WebAppDiagnosticLogs.UpdateStages.Blank<Update<FluentT>> updateDiagnosticLogsConfiguration();

            /**
             * Specifies the configuration for container logging for Linux web apps.
             *
             * @param quotaInMB the limit that restricts file system usage by app diagnostics logs. Value can range from
             *     25 MB and 100 MB.
             * @param retentionDays maximum days of logs that will be available
             * @return the next stage of the web app update
             */
            Update<FluentT> withContainerLoggingEnabled(int quotaInMB, int retentionDays);

            /**
             * Specifies the configuration for container logging for Linux web apps. Logs will be stored on the file
             * system for up to 35 MB.
             *
             * @return the next stage of the web app update
             */
            Update<FluentT> withContainerLoggingEnabled();

            /**
             * Disable the container logging for Linux web apps.
             *
             * @return the next stage of the web app update
             */
            Update<FluentT> withContainerLoggingDisabled();
        }

        /**
         * A web app definition stage allowing System Assigned Managed Service Identity to be set.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithManagedServiceIdentity<FluentT> {
            /**
             * Specifies that System Assigned Managed Service Identity needs to be enabled in the web app.
             *
             * @return the next stage of the web app definition
             */
            Update<FluentT> withSystemAssignedManagedServiceIdentity();

            /**
             * Specifies that User Assigned Managed Service Identity needs to be enabled in the web app.
             *
             * @return the next stage of the web app definition
             */
            Update<FluentT> withUserAssignedManagedServiceIdentity();
        }

        /**
         * The stage of the System Assigned (Local) Managed Service Identity enabled web app allowing to set access role
         * for the identity.
         *
         * @param <FluentT> the type of the resource
         */
        interface WithSystemAssignedIdentityBasedAccess<FluentT> {
            /**
             * Specifies that web app's system assigned (local) identity should have the given access (described by the
             * role) on an ARM resource identified by the resource ID. Applications running on the web app will have the
             * same permission (role) on the ARM resource.
             *
             * @param resourceId the ARM identifier of the resource
             * @param role access role to assigned to the web app's local identity
             * @return the next stage of the update
             */
            Update<FluentT> withSystemAssignedIdentityBasedAccessTo(String resourceId, BuiltInRole role);

            /**
             * Specifies that web app's system assigned (local) identity should have the given access (described by the
             * role) on the resource group that web app resides. Applications running on the web app will have the same
             * permission (role) on the resource group.
             *
             * @param role access role to assigned to the web app's local identity
             * @return the next stage of the update
             */
            Update<FluentT> withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole role);

            /**
             * Specifies that web app's system assigned (local) identity should have the access (described by the role
             * definition) on an ARM resource identified by the resource ID. Applications running on the web app will
             * have the same permission (role) on the ARM resource.
             *
             * @param resourceId scope of the access represented in ARM resource ID format
             * @param roleDefinitionId access role definition to assigned to the web app's local identity
             * @return the next stage of the update
             */
            Update<FluentT> withSystemAssignedIdentityBasedAccessTo(String resourceId, String roleDefinitionId);

            /**
             * Specifies that web app's system assigned (local) identity should have the access (described by the role
             * definition) on the resource group that web app resides. Applications running on the web app will have the
             * same permission (role) on the resource group.
             *
             * @param roleDefinitionId access role definition to assigned to the web app's local identity
             * @return the next stage of the update
             */
            Update<FluentT> withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(String roleDefinitionId);
        }

        /** The stage of the web app update allowing to add User Assigned (External) Managed Service Identities. */
        interface WithUserAssignedManagedServiceIdentityBasedAccess<FluentT> {
            /**
             * Specifies the definition of a not-yet-created user assigned identity to be associated with the web app.
             *
             * @param creatableIdentity a creatable identity definition
             * @return the next stage of the definition.
             */
            Update<FluentT> withNewUserAssignedManagedServiceIdentity(Creatable<Identity> creatableIdentity);

            /**
             * Specifies an existing user assigned identity to be associated with the web app.
             *
             * @param identity the identity
             * @return the next stage of the definition.
             */
            Update<FluentT> withExistingUserAssignedManagedServiceIdentity(Identity identity);
        }
    }

    /**
     * The template for a site update operation, containing all the settings that can be modified.
     *
     * @param <FluentT> the type of the resource
     */
    interface Update<FluentT>
        extends Appliable<FluentT>,
            GroupableResource.UpdateWithTags<Update<FluentT>>,
            UpdateStages.WithClientAffinityEnabled<FluentT>,
            UpdateStages.WithClientCertEnabled<FluentT>,
            UpdateStages.WithScmSiteAlsoStopped<FluentT>,
            UpdateStages.WithSiteConfigs<FluentT>,
            UpdateStages.WithAppSettings<FluentT>,
            UpdateStages.WithConnectionString<FluentT>,
            UpdateStages.WithSourceControl<FluentT>,
            UpdateStages.WithHostNameBinding<FluentT>,
            UpdateStages.WithHostNameSslBinding<FluentT>,
            UpdateStages.WithAuthentication<FluentT>,
            UpdateStages.WithDiagnosticLogging<FluentT>,
            UpdateStages.WithManagedServiceIdentity<FluentT>,
            UpdateStages.WithSystemAssignedIdentityBasedAccess<FluentT>,
            UpdateStages.WithUserAssignedManagedServiceIdentityBasedAccess<FluentT> {
    }
}
