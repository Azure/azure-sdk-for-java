/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;
import com.microsoft.azure.management.storage.StorageAccount;
import rx.Completable;
import rx.Observable;

import java.util.Map;

/**
 * An immutable client-side representation of an Azure Function App.
 */
@Fluent(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
@Beta
public interface FunctionApp extends
    WebAppBase,
    Refreshable<FunctionApp>,
    Updatable<FunctionApp.Update> {

    /**
     * @return the storage account associated with the function app
     */
    StorageAccount storageAccount();

    /**
     * @return the master key for the function app
     */
    String getMasterKey();

    /**
     * @return the master key for the function app
     */
    Observable<String> getMasterKeyAsync();

    /**
     * Retrieve the function key for a specific function.
     * @param functionName the name of the function
     * @return the function key
     */
    Map<String, String> listFunctionKeys(String functionName);

    /**
     * Retrieve the function key for a specific function.
     * @param functionName the name of the function
     * @return the function key
     */
    Observable<Map<String, String>> listFunctionKeysAsync(String functionName);

    /**
     * Adds a key to a function in this function app.
     * @param functionName the name of the function
     * @param keyName the name of the key to add
     * @param keyValue optional. If not provided, a value will be generated.
     * @return the added function key
     */
    NameValuePair addFunctionKey(String functionName, String keyName, String keyValue);

    /**
     * Adds a key to a function in this function app.
     * @param functionName the name of the function
     * @param keyName the name of the key to add
     * @param keyValue optional. If not provided, a value will be generated.
     * @return the added function key
     */
    Observable<NameValuePair> addFunctionKeyAsync(String functionName, String keyName, String keyValue);

    /**
     * Removes a key to a function in this function app.
     * @param functionName the name of the function
     * @param keyName the name of the key to remove
     */
    void removeFunctionKey(String functionName, String keyName);

    /**
     * Removes a key to a function in this function app.
     * @param functionName the name of the function
     * @param keyName the name of the key to remove
     * @return the completable of the operation
     */
    Completable removeFunctionKeyAsync(String functionName, String keyName);

    /**
     * Syncs the triggers on the function app.
     */
    void syncTriggers();

    /**
     * Syncs the triggers on the function app.
     * @return a completable for the operation
     */
    Completable syncTriggersAsync();

    /**************************************************************
     * Fluent interfaces to provision a Function App
     **************************************************************/

    /**
     * Container interface for all the definitions that need to be implemented.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.ExistingAppServicePlanWithGroup,
        DefinitionStages.WithStorageAccount,
        DefinitionStages.WithCreate {
    }

    /**
     * Grouping of all the function app definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of the function app definition.
         */
        interface Blank extends DefinitionWithRegion<NewAppServicePlanWithGroup> {
            /**
             * Uses an existing app service plan for the function app.
             * @param appServicePlan the existing app service plan
             * @return the next stage of the definition
             */
            ExistingAppServicePlanWithGroup withExistingAppServicePlan(AppServicePlan appServicePlan);
        }

        /**
         * A function app definition allowing resource group to be specified when an existing app service plan is used.
         */
        interface ExistingAppServicePlanWithGroup extends GroupableResource.DefinitionStages.WithGroup<WithCreate> {
        }

        /**
         * A function app definition allowing resource group to be specified when a new app service plan is to be created.
         */
        interface NewAppServicePlanWithGroup {
            /**
             * Associates the resource with an existing resource group.
             * @param groupName the name of an existing resource group to put this resource in.
             * @return the next stage of the definition
             */
            WithCreate withExistingResourceGroup(String groupName);

            /**
             * Associates the resource with an existing resource group.
             * @param group an existing resource group to put the resource in
             * @return the next stage of the definition
             */
            WithCreate withExistingResourceGroup(ResourceGroup group);

            /**
             * Creates a new resource group to put the resource in.
             * <p>
             * The group will be created in the same location as the resource.
             * @param name the name of the new group
             * @return the next stage of the definition
             */
            WithCreate withNewResourceGroup(String name);

            /**
             * Creates a new resource group to put the resource in.
             * <p>
             * The group will be created in the same location as the resource.
             * The group's name is automatically derived from the resource's name.
             * @return the next stage of the definition
             */
            WithCreate withNewResourceGroup();

            /**
             * Creates a new resource group to put the resource in, based on the definition specified.
             * @param groupDefinition a creatable definition for a new resource group
             * @return the next stage of the definition
             */
            WithCreate withNewResourceGroup(Creatable<ResourceGroup> groupDefinition);
        }

        /**
         * A function app definition allowing app service plan to be set.
         */
        interface WithNewAppServicePlan {
            /**
             * Creates a new consumption plan to use.
             * @return the next stage of the definition
             */
            WithCreate withNewConsumptionPlan();

            /**
             * Creates a new free app service plan. This will fail if there are 10 or more
             * free plans in the current subscription.
             *
             * @return the next stage of the definition
             */
            WithCreate withNewFreeAppServicePlan();

            /**
             * Creates a new shared app service plan.
             *
             * @return the next stage of the definition
             */
            WithCreate withNewSharedAppServicePlan();

            /**
             * Creates a new app service plan to use.
             *
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the definition
             */
            WithCreate withNewAppServicePlan(PricingTier pricingTier);

            /**
             * Creates a new app service plan to use.
             *
             * @param appServicePlanCreatable the new app service plan creatable
             * @return the next stage of the definition
             */
            WithCreate withNewAppServicePlan(Creatable<AppServicePlan> appServicePlanCreatable);
        }

        /**
         * A function app definition allowing storage account to be specified.
         * A storage account is required for storing function execution runtime,
         * triggers, and logs.
         */
        interface WithStorageAccount {
            /**
             * Creates a new storage account to use for the function app.
             * @param name the name of the storage account
             * @param sku the sku of the storage account
             * @return the next stage of the definition
             */
            WithCreate withNewStorageAccount(String name, com.microsoft.azure.management.storage.SkuName sku);

            /**
             * Specifies the storage account to use for the function app.
             * @param storageAccount the storage account to use
             * @return the next stage of the definition
             */
            WithCreate withExistingStorageAccount(StorageAccount storageAccount);
        }

        /**
         * A function app definition allowing runtime version to be specified.
         */
        interface WithRuntimeVersion {
            /**
             * Specifies the runtime version for the function app.
             * @param version the version of the Azure Functions runtime
             * @return the next stage of the definition
             */
            WithCreate withRuntimeVersion(String version);

            /**
             * Uses the latest runtime version for the function app.
             * @return the next stage of the definition
             */
            WithCreate withLatestRuntimeVersion();
        }

        /**
         * A function app definition allowing daily usage quota to be specified.
         */
        interface WithDailyUsageQuota {
            /**
             * Specifies the daily usage data cap.
             * @param quota the daily usage quota
             * @return the next stage of the definition
             */
            WithCreate withDailyUsageQuota(int quota);

            /**
             * Specifies the daily usage data cap.
             * @return the next stage of the definition
             */
            WithCreate withoutDailyUsageQuota();
        }

        /**
         * A function app definition with sufficient inputs to create a new
         * function app in the cloud, but exposing additional optional
         * inputs to specify.
         */
        interface WithCreate extends
            Creatable<FunctionApp>,
            DefinitionStages.WithNewAppServicePlan,
            DefinitionStages.WithStorageAccount,
            DefinitionStages.WithRuntimeVersion,
            DefinitionStages.WithDailyUsageQuota,
            WebAppBase.DefinitionStages.WithCreate<FunctionApp> {
        }
    }

    /**
     * Grouping of all the function app update stages.
     */
    interface UpdateStages {
        /**
         * A function app update allowing app service plan to be set.
         */
        interface WithAppServicePlan {
            /**
             * Creates a new consumption plan to use.
             * @return the next stage of the function app update
             */
            Update withNewConsumptionPlan();

            /**
             * Creates a new free app service plan. This will fail if there are 10 or more
             * free plans in the current subscription.
             *
             * @return the next stage of the function app update
             */
            Update withNewFreeAppServicePlan();

            /**
             * Creates a new shared app service plan.
             *
             * @return the next stage of the function app update
             */
            Update withNewSharedAppServicePlan();

            /**
             * Creates a new app service plan to use.
             *
             * @param pricingTier the sku of the app service plan
             * @return the next stage of the function app update
             */
            Update withNewAppServicePlan(PricingTier pricingTier);

            /**
             * Creates a new app service plan to use.
             *
             * @param appServicePlanCreatable the new app service plan creatable
             * @return the next stage of the function app update
             */
            Update withNewAppServicePlan(Creatable<AppServicePlan> appServicePlanCreatable);

            /**
             * Uses an existing app service plan for the function app.
             * @param appServicePlan the existing app service plan
             * @return the next stage of the function app update
             */
            Update withExistingAppServicePlan(AppServicePlan appServicePlan);
        }

        /**
         * A function app update allowing runtime version to be specified.
         */
        interface WithRuntimeVersion {
            /**
             * Specifies the runtime version for the function app.
             * @param version the version of the Azure Functions runtime
             * @return the next stage of the function app update
             */
            Update withRuntimeVersion(String version);

            /**
             * Uses the latest runtime version for the function app.
             * @return the next stage of the function app update
             */
            Update withLatestRuntimeVersion();
        }

        /**
         * A function app definition allowing storage account to be specified.
         * A storage account is required for storing function execution runtime,
         * triggers, and logs.
         */
        interface WithStorageAccount {
            /**
             * Creates a new storage account to use for the function app.
             * @param name the name of the storage account
             * @param sku the sku of the storage account
             * @return the next stage of the function app update
             */
            Update withNewStorageAccount(String name, com.microsoft.azure.management.storage.SkuName sku);

            /**
             * Specifies the storage account to use for the function app.
             * @param storageAccount the storage account to use
             * @return the next stage of the function app update
             */
            Update withExistingStorageAccount(StorageAccount storageAccount);
        }

        /**
         * A function app definition allowing daily usage quota to be specified.
         */
        interface WithDailyUsageQuota {
            /**
             * Specifies the daily usage data cap.
             * @param quota the daily usage quota
             * @return the next stage of the function app update
             */
            Update withDailyUsageQuota(int quota);

            /**
             * Specifies the daily usage data cap.
             * @return the next stage of the function app update
             */
            Update withoutDailyUsageQuota();
        }
    }

    /**
     * The template for a function app update operation, containing all the settings that can be modified.
     */
    interface Update extends
        WebAppBase.Update<FunctionApp>,
        UpdateStages.WithAppServicePlan,
        UpdateStages.WithRuntimeVersion,
        UpdateStages.WithStorageAccount,
        UpdateStages.WithDailyUsageQuota {
    }
}