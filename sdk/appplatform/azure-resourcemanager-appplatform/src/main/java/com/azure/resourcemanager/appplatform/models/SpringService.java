// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.appplatform.AppPlatformManager;
import com.azure.resourcemanager.appplatform.fluent.models.ServiceResourceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import reactor.core.publisher.Mono;

import java.util.List;

/** An immutable client-side representation of an Azure Spring Service. */
@Fluent
public interface SpringService extends GroupableResource<AppPlatformManager, ServiceResourceInner>,
    Refreshable<SpringService>, Updatable<SpringService.Update> {

    /**
     * Gets the Sku of the service.
     *
     * @return Sku of the service
     */
    Sku sku();

    /**
     * Gets the entry point of the spring app.
     *
     * @return the entry point of the spring app
     */
    SpringApps apps();

    /**
     * Gets the entry point of the spring service certificate.
     *
     * @return the entry point of the spring service certificate
     */
    SpringServiceCertificates certificates();

    /**
     * Gets the Monitoring Setting properties of the service.
     *
     * @return Monitoring Setting properties of the service
     */
    MonitoringSettingProperties getMonitoringSetting();

    /**
     * Gets the Monitoring Setting properties of the service.
     *
     * @return Monitoring Setting properties of the service
     */
    Mono<MonitoringSettingProperties> getMonitoringSettingAsync();

    /**
     * Gets the server properties of the service.
     *
     * @return server properties of the service
     */
    ConfigServerProperties getServerProperties();

    /**
     * Gets the server properties of the service.
     *
     * @return server properties of the service */
    Mono<ConfigServerProperties> getServerPropertiesAsync();

    /**
     * Lists test keys for the service.
     * @return all test keys
     */
    TestKeys listTestKeys();

    /**
     * Lists test keys for the service.
     * @return all test keys
     */
    Mono<TestKeys> listTestKeysAsync();

    /**
     * Regenerates a test key for the Service.
     * @param keyType the type of the regenerated key
     * @return all test keys
     */
    TestKeys regenerateTestKeys(TestKeyType keyType);

    /**
     * Regenerates a test key for the Service.
     * @param keyType the type of the regenerated key
     * @return all test keys
     */
    Mono<TestKeys> regenerateTestKeysAsync(TestKeyType keyType);

    /**
     * Disables the test endpoint for the service.
     */
    void disableTestEndpoint();

    /**
     * Disables the test endpoint for the service.
     * @return null
     */
    Mono<Void> disableTestEndpointAsync();

    /**
     * Enables the test endpoint for the service.
     * @return all test keys
     */
    TestKeys enableTestEndpoint();

    /**
     * Enables the test endpoint for the service.
     * @return all test keys
     */
    Mono<TestKeys> enableTestEndpointAsync();

    /**
     * Gets default Configuration Service for Enterprise Tier.
     *
     * @return default Configuration Service for Enterprise Tier
     */
    SpringConfigurationService getDefaultConfigurationService();

    /**
     * Gets default Service Registry for Enterprise Tier.
     *
     * @return default Service Registry for Enterprise Tier
     */
    SpringServiceRegistry getDefaultServiceRegistry();

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition extends DefinitionStages.Blank, DefinitionStages.WithGroup, DefinitionStages.WithCreate,
        DefinitionStages.WithEnterpriseTierCreate {
    }

    /** Grouping of all the spring service definition stages. */
    interface DefinitionStages {
        /** The first stage of the spring service definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of a spring service definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * The stage of a spring service definition allowing to specify sku.
         * All sku supported could be gotten from {@link SpringServices#listSkus()}.
         */
        interface WithSku {
            /**
             * Specifies the sku of the spring service.
             * @param skuName the sku name
             * @return the next stage of spring service definition
             */
            WithCreate withSku(String skuName);

            /**
             * Specifies the sku of the spring service.
             * @param skuName the sku name
             * @return the next stage of spring service definition
             */
            WithCreate withSku(SkuName skuName);

            /**
             * Specifies the sku of the spring service.
             * @param skuName the sku name
             * @param capacity the capacity for the spring service
             * @return the next stage of spring service definition
             */
            WithCreate withSku(String skuName, int capacity);

            /**
             * Specifies the sku of the spring service.
             * @param sku the entire sku object
             * @return the next stage of spring service definition
             */
            WithCreate withSku(Sku sku);

            /**
             * Specifies the sku of the spring service to be enterprise tier.
             * @return the next stage of enterprise tier spring service definition
             */
            WithEnterpriseTierCreate withEnterpriseTierSku();
        }

        /** The stage of a spring service definition allowing to specify tracing with app insight. */
        interface WithTracing {
            /**
             * Specifies tracing property with app insight
             * @param appInsightInstrumentationKey the app insight instrumentation key
             * @return the next stage of spring service definition
             */
            WithCreate withTracing(String appInsightInstrumentationKey);
        }

        /** The stage of a spring service definition allowing to specify the server configuration. */
        interface WithConfiguration {
            /**
             * Specifies the git repository for the spring service.
             * @param uri the uri of the git repository
             * @return the next stage of spring service definition
             */
            WithCreate withGitUri(String uri);

            /**
             * Specifies the git repository for the spring service.
             * @param uri the uri of the git repository
             * @param username the username of the private git repository
             * @param password the password of the private git repository
             * @return the next stage of spring service definition
             */
            WithCreate withGitUriAndCredential(String uri, String username, String password);

            /**
             * Specifies the git repository for the spring service.
             * @param gitConfig the configuration of the git repository
             * @return the next stage of spring service definition
             */
            WithCreate withGitConfig(ConfigServerGitProperty gitConfig);
        }

        /**
         * (Enterprise Tier Only)
         * The stage of a spring service definition allowing to specify the enterprise tier configuration service
         */
        interface WithConfigurationService {
            /**
             * Specifies the default git repository for the spring service.
             * @param uri the uri of the git repository
             * @param branch branch of the git repository
             * @param filePatterns patterns for configuration files to be selected from the git repository
             * @return the next stage of spring service definition
             */
            WithEnterpriseTierCreate withDefaultGitRepository(String uri, String branch, List<String> filePatterns);

            /**
             * Specifies additional git repository for the spring service.
             * New repository configurations will override the old with the same name.
             * @param name the name of the git repository
             * @param uri the uri of the git repository
             * @param branch branch of the git repository
             * @param filePatterns patterns for configuration files to be selected from the git repository
             * @return the next stage of spring service definition
             */
            WithEnterpriseTierCreate withGitRepository(String name, String uri, String branch,
                List<String> filePatterns);

            /**
             * Specifies complete git repository configuration for the spring service.
             * New repository configurations will override the old with the same name.
             * @param gitConfig git repository configuration
             * @return the next stage of spring service definition
             */
            WithEnterpriseTierCreate withGitRepositoryConfig(ConfigurationServiceGitProperty gitConfig);
        }

        /** The stage of a spring service definition allowing to specify the certificate. */
        interface WithCertificate {
            /**
             * Specifies a certificate in key vault with latest version binding to the spring service.
             * @param name the certificate name
             * @param keyVaultUri the uri for key vault that contains certificate
             * @param certNameInKeyVault the certificate name in the key vault
             * @return the next stage of spring service definition
             */
            WithCreate withCertificate(String name, String keyVaultUri, String certNameInKeyVault);

            /**
             * Specifies a certificate in key vault with specific version binding to the spring service.
             * @param name the certificate name
             * @param keyVaultUri the uri for key vault that contains certificate
             * @param certNameInKeyVault the certificate name in the key vault
             * @param certVersion the certificate version in the key vault
             * @return the next stage of spring service definition
             */
            WithCreate withCertificate(String name, String keyVaultUri, String certNameInKeyVault, String certVersion);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource of enterprise tier to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface WithEnterpriseTierCreate extends Creatable<SpringService>, Resource.DefinitionWithTags<WithCreate>,
            WithSku, WithTracing, WithConfigurationService, WithCertificate {
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends Creatable<SpringService>, Resource.DefinitionWithTags<WithCreate>, WithSku,
            WithTracing, WithConfiguration, WithCertificate {
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update extends Appliable<SpringService>, Resource.UpdateWithTags<Update>, UpdateStages.WithSku,
        UpdateStages.WithTracing, UpdateStages.WithConfiguration, UpdateStages.WithCertificate {
    }

    /** Grouping of spring service update stages. */
    interface UpdateStages {
        /**
         * The stage of a spring service update allowing to specify sku.
         * All sku supported could be gotten from {@link SpringServices#listSkus()}.
         */
        interface WithSku {
            /**
             * Specifies the sku of the spring service.
             * @param skuName the sku name
             * @return the next stage of spring service update
             */
            Update withSku(String skuName);

            /**
             * Specifies the sku of the spring service.
             * @param skuName the sku name
             * @param capacity the capacity for the spring service
             * @return the next stage of spring service update
             */
            Update withSku(String skuName, int capacity);

            /**
             * Specifies the sku of the spring service.
             * @param sku the entire sku object
             * @return the next stage of spring service update
             */
            Update withSku(Sku sku);
        }

        /** The stage of a spring service update allowing to specify tracing with app insight. */
        interface WithTracing {
            /**
             * Specifies tracing property with app insight.
             * @param appInsightInstrumentationKey the app insight instrumentation key
             * @return the next stage of spring service update
             */
            Update withTracing(String appInsightInstrumentationKey);

            /**
             * Removes tracing property.
             * @return the next stage of spring service update
             */
            Update withoutTracing();
        }

        /** The stage of a spring service update allowing to specify the server configuration. */
        interface WithConfiguration {
            /**
             * (Basic/Standard Tier Only)
             * Specifies the git repository for the spring service.
             * @param uri the uri of the git repository
             * @return the next stage of spring service update
             */
            Update withGitUri(String uri);

            /**
             * (Basic/Standard Tier Only)
             * Specifies the git repository for the spring service.
             * @param uri the uri of the git repository
             * @param username the username of the private git repository
             * @param password the password of the private git repository
             * @return the next stage of spring service update
             */
            Update withGitUriAndCredential(String uri, String username, String password);

            /**
             * (Basic/Standard Tier Only)
             * Specifies the git repository for the spring service.
             * @param gitConfig the configuration of the git repository
             * @return the next stage of spring service update
             */
            Update withGitConfig(ConfigServerGitProperty gitConfig);

            /**
             * (Basic/Standard Tier Only)
             * Removes the git configuration.
             * @return the next stage of spring service update
             */
            Update withoutGitConfig();

            /**
             * (Enterprise Tier Only)
             * Specifies the default git repository for the spring service.
             * @param uri the uri of the git repository
             * @param branch branch of the git repository
             * @param filePatterns patterns for configuration files to be selected from the git repository
             * @return the next stage of spring service update
             */
            Update withDefaultGitRepository(String uri, String branch, List<String> filePatterns);

            /**
             * (Enterprise Tier Only)
             * Specifies additional git repository for the spring service.
             * New repository configurations will override the old with the same name.
             * @param name the name of the git repository
             * @param uri the uri of the git repository
             * @param branch branch of the git repository
             * @param filePatterns patterns for configuration files to be selected from the git repository
             * @return the next stage of spring service update
             */
            Update withGitRepository(String name, String uri, String branch, List<String> filePatterns);

            /**
             * (Enterprise Tier Only)
             * Specifies complete git repository configuration for the spring service.
             * New repository configurations will override the old with the same name.
             * @param gitConfig git repository configuration
             * @return the next stage of spring service update
             */
            Update withGitRepositoryConfig(ConfigurationServiceGitProperty gitConfig);

            /**
             * (Enterprise Tier Only)
             * Removes git repository with specified name.
             * @param name name of the git repository to remove
             * @return the next stage of spring service update
             */
            Update withoutGitRepository(String name);

            /**
             * (Enterprise Tier Only)
             * Removes the git repository configuration.
             * @return the next stage of spring service update
             */
            Update withoutGitRepositories();
        }

        /** The stage of a spring service update allowing to specify the certificate. */
        interface WithCertificate {
            /**
             * Specifies a certificate in key vault with latest version binding to the spring service.
             * @param name the certificate name
             * @param keyVaultUri the uri for key vault that contains certificate
             * @param certNameInKeyVault the certificate name in the key vault
             * @return the next stage of spring service update
             */
            Update withCertificate(String name, String keyVaultUri, String certNameInKeyVault);

            /**
             * Specifies a certificate in key vault with specific version binding to the spring service.
             * @param name the certificate name
             * @param keyVaultUri the uri for key vault that contains certificate
             * @param certNameInKeyVault the certificate name in the key vault
             * @param certVersion the certificate version in the key vault
             * @return the next stage of spring service update
             */
            Update withCertificate(String name, String keyVaultUri, String certNameInKeyVault, String certVersion);

            /**
             * Removes a certificate binding to the spring service.
             * @param name the certificate name
             * @return the next stage of spring service update
             */
            Update withoutCertificate(String name);
        }
    }
}
