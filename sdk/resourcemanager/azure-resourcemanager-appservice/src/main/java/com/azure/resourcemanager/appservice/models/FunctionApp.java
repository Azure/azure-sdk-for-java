// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingPrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsListingPrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.collection.SupportsUpdatingPrivateEndpointConnection;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.resourcemanager.storage.models.StorageAccountSkuType;
import java.util.Map;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure Function App. */
@Fluent
public interface FunctionApp extends FunctionAppBasic, WebAppBase, Updatable<FunctionApp.Update>,
    SupportsListingPrivateLinkResource,
    SupportsListingPrivateEndpointConnection,
    SupportsUpdatingPrivateEndpointConnection {

    /** @return the entry point to deployment slot management API under the function app */
    FunctionDeploymentSlots deploymentSlots();

    /** @return the storage account associated with the function app */
    StorageAccount storageAccount();

    /** @return the master key for the function app */
    String getMasterKey();

    /** @return the master key for the function app */
    Mono<String> getMasterKeyAsync();

    /**
     * List function information elements.
     *
     * @return list of function information elements
     */
    PagedIterable<FunctionEnvelope> listFunctions();

    /**
     * Retrieve the function key for a specific function.
     *
     * @param functionName the name of the function
     * @return the function key
     */
    Map<String, String> listFunctionKeys(String functionName);

    /**
     * Retrieve the function key for a specific function.
     *
     * @param functionName the name of the function
     * @return the function key
     */
    Mono<Map<String, String>> listFunctionKeysAsync(String functionName);

    /**
     * Adds a key to a function in this function app.
     *
     * @param functionName the name of the function
     * @param keyName the name of the key to add
     * @param keyValue optional. If not provided, a value will be generated.
     * @return the added function key
     */
    NameValuePair addFunctionKey(String functionName, String keyName, String keyValue);

    /**
     * Adds a key to a function in this function app.
     *
     * @param functionName the name of the function
     * @param keyName the name of the key to add
     * @param keyValue optional. If not provided, a value will be generated.
     * @return the added function key
     */
    Mono<NameValuePair> addFunctionKeyAsync(String functionName, String keyName, String keyValue);

    /**
     * Removes a key to a function in this function app.
     *
     * @param functionName the name of the function
     * @param keyName the name of the key to remove
     */
    void removeFunctionKey(String functionName, String keyName);

    /**
     * Removes a key to a function in this function app.
     *
     * @param functionName the name of the function
     * @param keyName the name of the key to remove
     * @return the completable of the operation
     */
    Mono<Void> removeFunctionKeyAsync(String functionName, String keyName);

    /**
     * Triggers a function.
     *
     * @param functionName the name of the function
     * @param payload the payload to be serialized to JSON
     */
    void triggerFunction(String functionName, Object payload);

    /**
     * Triggers a function.
     *
     * @param functionName the name of the function
     * @param payload the payload to be serialized to JSON
     * @return the completable of the operation
     */
    Mono<Void> triggerFunctionAsync(String functionName, Object payload);

    /** Syncs the triggers on the function app. */
    void syncTriggers();

    /**
     * Syncs the triggers on the function app.
     *
     * @return a completable for the operation
     */
    Mono<Void> syncTriggersAsync();

    /**************************************************************
     * Fluent interfaces to provision a Function App
     **************************************************************/

    /** Container interface for all the definitions that need to be implemented. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.ExistingAppServicePlanWithGroup,
            DefinitionStages.WithStorageAccount,
            DefinitionStages.WithDockerContainerImage,
            DefinitionStages.WithCredentials,
            DefinitionStages.WithCreate {
    }

    /** Grouping of all the function app definition stages. */
    interface DefinitionStages {
        /** The first stage of the function app definition. */
        interface Blank extends DefinitionWithRegion<NewAppServicePlanWithGroup> {
            /**
             * Uses an existing Windows app service plan for the function app.
             *
             * @param appServicePlan the existing app service plan
             * @return the next stage of the definition
             */
            ExistingAppServicePlanWithGroup withExistingAppServicePlan(AppServicePlan appServicePlan);

            /**
             * Uses an existing Linux app service plan for the function app.
             *
             * @param appServicePlan the existing app service plan
             * @return the next stage of the definition
             */
            ExistingLinuxPlanWithGroup withExistingLinuxAppServicePlan(AppServicePlan appServicePlan);
        }

        /**
         * A function app definition allowing resource group to be specified when an existing app service plan is used.
         */
        interface ExistingAppServicePlanWithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * A function app definition allowing resource group to be specified when a new app service plan is to be
         * created.
         */
        interface NewAppServicePlanWithGroup {
            /**
             * Associates the resource with an existing resource group.
             *
             * @param groupName the name of an existing resource group to put this resource in.
             * @return the next stage of the definition
             */
            WithCreate withExistingResourceGroup(String groupName);

            /**
             * Associates the resource with an existing resource group.
             *
             * @param group an existing resource group to put the resource in
             * @return the next stage of the definition
             */
            WithCreate withExistingResourceGroup(ResourceGroup group);

            /**
             * Creates a new resource group to put the resource in.
             *
             * <p>The group will be created in the same location as the resource.
             *
             * @param name the name of the new group
             * @return the next stage of the definition
             */
            WithCreate withNewResourceGroup(String name);

            /**
             * Creates a new resource group to put the resource in.
             *
             * <p>The group will be created in the same location as the resource. The group's name is automatically
             * derived from the resource's name.
             *
             * @return the next stage of the definition
             */
            WithCreate withNewResourceGroup();

            /**
             * Creates a new resource group to put the resource in, based on the definition specified.
             *
             * @param groupDefinition a creatable definition for a new resource group
             * @return the next stage of the definition
             */
            WithCreate withNewResourceGroup(Creatable<ResourceGroup> groupDefinition);
        }

        /** A function app definition allowing app service plan to be set. */
        interface WithNewAppServicePlan {
            /**
             * Creates a new Windows consumption plan to use.
             *
             * @return the next stage of the definition
             */
            WithCreate withNewConsumptionPlan();

            /**
             * Creates a new Windows consumption plan to use.
             *
             * @param appServicePlanName the name of the new consumption plan
             * @return the next stage of the definition
             */
            WithCreate withNewConsumptionPlan(String appServicePlanName);

            /**
             * Creates a new Windows free app service plan. This will fail if there are 10 or more free plans in the
             * current subscription.
             *
             * @return the next stage of the definition
             */
            WithCreate withNewFreeAppServicePlan();

            /**
             * Creates a new Windows shared app service plan.
             *
             * @return the next stage of the definition
             */
            WithCreate withNewSharedAppServicePlan();

            /**
             * Creates a new Windows app service plan to use.
             *
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the definition
             */
            WithCreate withNewAppServicePlan(PricingTier pricingTier);

            /**
             * Creates a new Windows app service plan to use.
             *
             * @param appServicePlanName the name of the new app service plan
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the definition
             */
            WithCreate withNewAppServicePlan(String appServicePlanName, PricingTier pricingTier);

            /**
             * Creates a new Windows app service plan to use.
             *
             * @param appServicePlanCreatable the new app service plan creatable
             * @return the next stage of the definition
             */
            WithCreate withNewAppServicePlan(Creatable<AppServicePlan> appServicePlanCreatable);

            /**
             * Creates a new Linux consumption plan to use.
             *
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewLinuxConsumptionPlan();

            /**
             * Creates a new Linux consumption plan to use.
             *
             * @param appServicePlanName the name of the new consumption plan
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewLinuxConsumptionPlan(String appServicePlanName);

            /**
             * Creates a new Linux app service plan to use.
             *
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewLinuxAppServicePlan(PricingTier pricingTier);

            /**
             * Creates a new Linux app service plan to use.
             *
             * @param appServicePlanName the name of the new app service plan
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewLinuxAppServicePlan(String appServicePlanName, PricingTier pricingTier);

            /**
             * Creates a new Linux app service plan to use.
             *
             * @param appServicePlanCreatable the new app service plan creatable
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewLinuxAppServicePlan(Creatable<AppServicePlan> appServicePlanCreatable);
        }

        /**
         * A function app definition allowing storage account to be specified. A storage account is required for storing
         * function execution runtime, triggers, and logs.
         */
        interface WithStorageAccount {
            /**
             * Creates a new storage account to use for the function app.
             *
             * @param name the name of the storage account
             * @param sku the sku of the storage account
             * @return the next stage of the definition
             */
            WithCreate withNewStorageAccount(String name, StorageAccountSkuType sku);

            /**
             * Creates a new storage account to use for the function app.
             *
             * @param storageAccount a creatable definition for a new storage account
             * @return the next stage of the definition
             */
            WithCreate withNewStorageAccount(Creatable<StorageAccount> storageAccount);

            /**
             * Specifies the storage account to use for the function app.
             *
             * @param storageAccount the storage account to use
             * @return the next stage of the definition
             */
            WithCreate withExistingStorageAccount(StorageAccount storageAccount);
        }

        /** A function app definition allowing runtime version to be specified. */
        interface WithRuntimeVersion {
            /**
             * Specifies the runtime for the function app.
             *
             * @param runtime the Azure Functions runtime
             * @return the next stage of the definition
             */
            WithCreate withRuntime(String runtime);

            /**
             * Specifies the runtime version for the function app.
             *
             * @param version the version of the Azure Functions runtime
             * @return the next stage of the definition
             */
            WithCreate withRuntimeVersion(String version);

            /**
             * Uses the latest runtime version for the function app.
             *
             * @return the next stage of the definition
             */
            WithCreate withLatestRuntimeVersion();
        }

        /** A function app definition allowing daily usage quota to be specified. */
        interface WithDailyUsageQuota {
            /**
             * Specifies the daily usage data cap.
             *
             * @param quota the daily usage quota
             * @return the next stage of the definition
             */
            WithCreate withDailyUsageQuota(int quota);

            /**
             * Specifies the daily usage data cap.
             *
             * @return the next stage of the definition
             */
            WithCreate withoutDailyUsageQuota();
        }

        /**
         * A function app definition with sufficient inputs to create a new function app in the cloud, but exposing
         * additional optional inputs to specify.
         */
        interface WithCreate
            extends Creatable<FunctionApp>,
                DefinitionStages.WithNewAppServicePlan,
                DefinitionStages.WithStorageAccount,
                DefinitionStages.WithRuntimeVersion,
                DefinitionStages.WithDailyUsageQuota,
                WebAppBase.DefinitionStages.WithCreate<FunctionApp> {
        }

        /**
         * A function app definition allowing resource group to be specified when an existing app service plan is used.
         */
        interface ExistingLinuxPlanWithGroup {
            /**
             * Associates the resource with an existing resource group.
             *
             * @param groupName the name of an existing resource group to put this resource in.
             * @return the next stage of the definition
             */
            WithDockerContainerImage withExistingResourceGroup(String groupName);

            /**
             * Associates the resource with an existing resource group.
             *
             * @param group an existing resource group to put the resource in
             * @return the next stage of the definition
             */
            WithDockerContainerImage withExistingResourceGroup(ResourceGroup group);

            /**
             * Creates a new resource group to put the resource in.
             *
             * <p>The group will be created in the same location as the resource.
             *
             * @param name the name of the new group
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewResourceGroup(String name);

            /**
             * Creates a new resource group to put the resource in.
             *
             * <p>The group will be created in the same location as the resource. The group's name is automatically
             * derived from the resource's name.
             *
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewResourceGroup();

            /**
             * Creates a new resource group to put the resource in, based on the definition specified.
             *
             * @param groupDefinition a creatable definition for a new resource group
             * @return the next stage of the definition
             */
            WithDockerContainerImage withNewResourceGroup(Creatable<ResourceGroup> groupDefinition);
        }

        /** A function app definition allowing docker image source to be specified. */
        interface WithDockerContainerImage {
            /**
             * Specifies the docker container image to be a built in one.
             *
             * @param runtimeStack the runtime stack installed on the image
             * @return the next stage of the definition
             */
            WithCreate withBuiltInImage(FunctionRuntimeStack runtimeStack);

            /**
             * Specifies the docker container image to be one from Docker Hub.
             *
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @return the next stage of the definition
             */
            WithCreate withPublicDockerHubImage(String imageAndTag);

            /**
             * Specifies the docker container image to be one from Docker Hub.
             *
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @return the next stage of the definition
             */
            WithCredentials withPrivateDockerHubImage(String imageAndTag);

            /**
             * Specifies the docker container image to be one from a private registry.
             *
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @param serverUrl the URL to the private registry server
             * @return the next stage of the definition
             */
            WithCredentials withPrivateRegistryImage(String imageAndTag, String serverUrl);
        }

        /** A function app definition allowing docker registry credentials to be set. */
        interface WithCredentials {
            /**
             * Specifies the username and password for Docker Hub.
             *
             * @param username the username for Docker Hub
             * @param password the password for Docker Hub
             * @return the next stage of the web app update
             */
            WithCreate withCredentials(String username, String password);
        }
    }

    /** Grouping of all the function app update stages. */
    interface UpdateStages {
        /** A function app update allowing app service plan to be set. */
        interface WithAppServicePlan {
            /**
             * Creates a new Windows consumption plan to use.
             *
             * @return the next stage of the function app update
             */
            Update withNewConsumptionPlan();

            /**
             * Creates a new Windows consumption plan to use.
             *
             * @param appServicePlanName the name of the new consumption plan
             * @return the next stage of the function app update
             */
            Update withNewConsumptionPlan(String appServicePlanName);

            /**
             * Creates a new Windows free app service plan. This will fail if there are 10 or more free plans in the
             * current subscription.
             *
             * @return the next stage of the function app update
             */
            Update withNewFreeAppServicePlan();

            /**
             * Creates a new Windows shared app service plan.
             *
             * @return the next stage of the function app update
             */
            Update withNewSharedAppServicePlan();

            /**
             * Creates a new Windows app service plan to use.
             *
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the function app update
             */
            Update withNewAppServicePlan(PricingTier pricingTier);

            /**
             * Creates a new Windows app service plan to use.
             *
             * @param appServicePlanName the name of the new app service plan
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the function app update
             */
            Update withNewAppServicePlan(String appServicePlanName, PricingTier pricingTier);

            /**
             * Creates a new Windows app service plan to use.
             *
             * @param appServicePlanCreatable the new app service plan creatable
             * @return the next stage of the function app update
             */
            Update withNewAppServicePlan(Creatable<AppServicePlan> appServicePlanCreatable);

            /**
             * Uses an existing Windows app service plan for the function app.
             *
             * @param appServicePlan the existing app service plan
             * @return the next stage of the function app update
             */
            Update withExistingAppServicePlan(AppServicePlan appServicePlan);

            /**
             * Uses an existing Linux app service plan for the function app.
             *
             * @param appServicePlan the existing app service plan
             * @return the next stage of the definition
             */
            Update withExistingLinuxAppServicePlan(AppServicePlan appServicePlan);

            /**
             * Creates a new Linux consumption plan to use.
             *
             * @return the next stage of the definition
             */
            Update withNewLinuxConsumptionPlan();

            /**
             * Creates a new Linux consumption plan to use.
             *
             * @param appServicePlanName the name of the new consumption plan
             * @return the next stage of the definition
             */
            Update withNewLinuxConsumptionPlan(String appServicePlanName);

            /**
             * Creates a new Linux app service plan to use.
             *
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the definition
             */
            Update withNewLinuxAppServicePlan(PricingTier pricingTier);

            /**
             * Creates a new Linux app service plan to use.
             *
             * @param appServicePlanName the name of the new app service plan
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the definition
             */
            Update withNewLinuxAppServicePlan(String appServicePlanName, PricingTier pricingTier);

            /**
             * Creates a new Linux app service plan to use.
             *
             * @param appServicePlanCreatable the new app service plan creatable
             * @return the next stage of the definition
             */
            Update withNewLinuxAppServicePlan(Creatable<AppServicePlan> appServicePlanCreatable);
        }

        /** A function app update allowing runtime version to be specified. */
        interface WithRuntimeVersion {
            /**
             * Specifies the runtime for the function app.
             *
             * @param runtime the Azure Functions runtime
             * @return the next stage of the definition
             */
            Update withRuntime(String runtime);

            /**
             * Specifies the runtime version for the function app.
             *
             * @param version the version of the Azure Functions runtime
             * @return the next stage of the function app update
             */
            Update withRuntimeVersion(String version);

            /**
             * Uses the latest runtime version for the function app.
             *
             * @return the next stage of the function app update
             */
            Update withLatestRuntimeVersion();
        }

        /**
         * A function app definition allowing storage account to be specified. A storage account is required for storing
         * function execution runtime, triggers, and logs.
         */
        interface WithStorageAccount {
            /**
             * Creates a new storage account to use for the function app.
             *
             * @param name the name of the storage account
             * @param sku the sku of the storage account
             * @return the next stage of the function app update
             */
            Update withNewStorageAccount(String name, StorageAccountSkuType sku);

            /**
             * Specifies the storage account to use for the function app.
             *
             * @param storageAccount the storage account to use
             * @return the next stage of the function app update
             */
            Update withExistingStorageAccount(StorageAccount storageAccount);
        }

        /** A function app definition allowing daily usage quota to be specified. */
        interface WithDailyUsageQuota {
            /**
             * Specifies the daily usage data cap.
             *
             * @param quota the daily usage quota
             * @return the next stage of the function app update
             */
            Update withDailyUsageQuota(int quota);

            /**
             * Specifies the daily usage data cap.
             *
             * @return the next stage of the function app update
             */
            Update withoutDailyUsageQuota();
        }

        /** A function app update allowing docker image source to be specified. */
        interface WithDockerContainerImage {
            /**
             * Specifies the docker container image to be a built in one.
             *
             * @param runtimeStack the runtime stack installed on the image
             * @return the next stage of the web app update
             */
            Update withBuiltInImage(FunctionRuntimeStack runtimeStack);

            /**
             * Specifies the docker container image to be one from Docker Hub.
             *
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @return the next stage of the web app update
             */
            Update withPublicDockerHubImage(String imageAndTag);

            /**
             * Specifies the docker container image to be one from Docker Hub.
             *
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @return the next stage of the web app update
             */
            WithCredentials withPrivateDockerHubImage(String imageAndTag);

            /**
             * Specifies the docker container image to be one from a private registry.
             *
             * @param imageAndTag image and optional tag (eg 'image:tag')
             * @param serverUrl the URL to the private registry server
             * @return the next stage of the web app update
             */
            WithCredentials withPrivateRegistryImage(String imageAndTag, String serverUrl);
        }

        /** A function app update allowing docker hub credentials to be set. */
        interface WithCredentials {
            /**
             * Specifies the username and password for Docker Hub.
             *
             * @param username the username for Docker Hub
             * @param password the password for Docker Hub
             * @return the next stage of the web app update
             */
            Update withCredentials(String username, String password);
        }
    }

    /** The template for a function app update operation, containing all the settings that can be modified. */
    interface Update
        extends WebAppBase.Update<FunctionApp>,
            UpdateStages.WithAppServicePlan,
            UpdateStages.WithRuntimeVersion,
            UpdateStages.WithStorageAccount,
            UpdateStages.WithDailyUsageQuota,
            UpdateStages.WithDockerContainerImage,
            UpdateStages.WithCredentials {
    }
}
