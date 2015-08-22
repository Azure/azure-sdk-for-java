/**
 * Copyright Microsoft Corporation
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.utility;

import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.models.*;
import com.microsoft.windowsazure.core.OperationStatus;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.models.AccountType;
import com.microsoft.azure.management.storage.models.StorageAccount;
import com.microsoft.azure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import java.util.List;
import java.util.HashMap;

public class StorageHelper {
    /**
     * Create a storage account based on the input parameters.  Call blocks until 
     * either the storage account is created or an error occurs.  Default values 
     * for the storage account create parameters are provided.
     *
     * @param storageManagementClient       the storage management client object on which operations are performed
     * @param context                       information necessary for creating the storage account
     * @return StorageAccount               the storage account created, null if operation failed     
     * @throws Exception                    in the advent of a problem, the exception is thrown
     */
    public static StorageAccount createStorageAccount(
            StorageManagementClient storageManagementClient, ResourceContext context)
            throws Exception {
        //create storage account
        StorageAccountCreateParameters stoInput = new StorageAccountCreateParameters(AccountType.STANDARDGRS,
                context.getLocation());
        return createStorageAccount(storageManagementClient, context, stoInput);
    }

    /**
     * Create a storage account based on the input parameters.  Call blocks until 
     * either the storage account is created or an error occurs.
     *
     * @param storageManagementClient       the storage management client object on which operations are performed
     * @param context                       information necessary for creating the storage account
     * @param stoInput                      storage account specific parameters
     * @return StorageAccount               the storage account created, null if operation failed     
     * @throws Exception                    in the advent of a problem, the exception is thrown
     */
    public static StorageAccount createStorageAccount(
            StorageManagementClient storageManagementClient, ResourceContext context,
            StorageAccountCreateParameters stoInput) throws Exception {
        String storageAccountName = context.getStorageAccountName();
        StorageAccount storageAccount = storageManagementClient.getStorageAccountsOperations()
                .create(context.getResourceGroupName(), storageAccountName, stoInput)
                .getStorageAccount();

        //wait for the creation of storage account
        boolean created = false;
        while(!created) {
            waitSeconds(3);
            List<StorageAccount> storageAccountList = storageManagementClient.getStorageAccountsOperations()
                    .listByResourceGroup(context.getResourceGroupName()).getStorageAccounts();
            for (StorageAccount account : storageAccountList) {
                if (account.getName().equalsIgnoreCase(storageAccountName)) {
                    created = true;
                    break;
                }
            }

            //follow overwrite from .net tests
            storageAccount.setName(storageAccountName);
        }

        context.setStorageAccount(storageAccount);
        return storageAccount;
    }

    protected static void waitSeconds(double seconds) throws InterruptedException{
        if (!ManagementConfiguration.isPlayback()) {
            Thread.sleep((long)seconds * 100);
        }
    }

    /**
     * Retrieve a storage account by name.
     *
     * @param storageManagementClient       the storage management client object on which operations are performed
     * @param context                       information necessary for creating the storage account
     * @return StorageAccount               the storage account requested, null if not found
     * @throws Exception                    in the advent of a problem, the exception is thrown
     */
    public static StorageAccount getStorageAccount(
            StorageManagementClient storageManagementClient,
            ResourceContext context) throws Exception {
        StorageAccount result = null;
        String storageAccountName = context.getStorageAccountName();

        List<StorageAccount> storageAccountList = storageManagementClient.getStorageAccountsOperations()
                    .listByResourceGroup(context.getResourceGroupName()).getStorageAccounts();
        for (StorageAccount account : storageAccountList) {
            if (account.getName().equalsIgnoreCase(storageAccountName)) {
                result = account;
                break;
            }
        }
                    
        return result;
    }
    
    /**
     * Delete a storage account based on the input parameters.  Call blocks until
     * until the storage account is removed.
     *
     * @param storageManagementClient       the storage management client object on which operations are performed
     * @param context                       information necessary for creating the storage account
     * @return StorageAccount               the storage account created, null if operation failed
     * @throws Exception                    in the advent of a problem, the exception is thrown
     */
    public static boolean deleteStorageAccount(
            StorageManagementClient storageManagementClient, ResourceContext context) throws Exception {
            
        String storageAccountName = context.getStorageAccountName();
        boolean result = false;
        
        ExecutorService service = null;
        try {
                    Future <OperationResponse> future = null;
            service = Executors.newFixedThreadPool(1);            
            future = storageManagementClient.getStorageAccountsOperations()
                                .deleteAsync(context.getResourceGroupName(),
                                        storageAccountName);
            OperationResponse response = future.get();
            
            if(response.getStatusCode() / 100 == 2) {
                result = true;
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            service.shutdown();
        }
        
        return result;
    }

    /**
     * Get list of storage accounts.  If resourceGroup specified, only those in the
     * resource group are returned, otherwise all of them in the subscription.
     *
     * @param storageManagementClient       the storage management client object on which operations are performed
     * @param resourceGroup                 the name of the resource group, can be null
     * @return StorageAccount               the storage account created, null if operation failed
     * @throws Exception                    in the advent of a problem, the exception is thrown
     */
    public static List<StorageAccount> listStorageAccounts(
            StorageManagementClient storageManagementClient, String resourceGroup) throws Exception {

        List<StorageAccount> accounts = null;

        ExecutorService service = null;
        try {
            Future <StorageAccountListResponse> future = null;
            service = Executors.newFixedThreadPool(1);
            if(resourceGroup == null) {
                future = storageManagementClient.getStorageAccountsOperations().listAsync();
            } else {
                future = storageManagementClient.getStorageAccountsOperations().listByResourceGroupAsync(resourceGroup);
            }
            StorageAccountListResponse response = future.get();

            if(response != null) {
                accounts = response.getStorageAccounts();
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            service.shutdown();
        }

        return accounts;
    }

    /**
     * Regenerate storage account key based on the input parameters.  Call blocks until
     * the key is regenerated.
     *
     * @param storageManagementClient       the storage management client object on which operations are performed
     * @param context                       information necessary for creating the storage account
     * @param keyName                       storage account specific parameters
     * @return StorageAccount               the storage account created, null if operation failed
     * @throws Exception                    in the advent of a problem, the exception is thrown
     */
    public static StorageAccountRegenerateKeyResponse regenerateStorageAccountKey(
            StorageManagementClient storageManagementClient, ResourceContext context, KeyName keyName) throws Exception {

        String storageAccountName = context.getStorageAccountName();
        StorageAccountRegenerateKeyResponse response = null;

        ExecutorService service = null;
        try {
            Future <StorageAccountRegenerateKeyResponse> future = null;
            service = Executors.newFixedThreadPool(1);
            future = storageManagementClient.getStorageAccountsOperations()
                    .regenerateKeyAsync(context.getResourceGroupName(),
                            storageAccountName, keyName);
            response = future.get();
        } catch (Exception ex) {
            throw ex;
        } finally {
            service.shutdown();
        }

        return response;
    }

    /**
     * Update the tags for the storage account.
     *
     * @param storageManagementClient       the storage management client object on which operations are performed
     * @param context                       information necessary for creating the storage account
     * @param tags                          hash map of tags to apply to storage account, or null to clear
     * @return StorageAccountUpdateResponse     the update response
     * @throws Exception                    in the advent of a problem, the exception is thrown
     */
    public static StorageAccountUpdateResponse updateAccountTags(
            StorageManagementClient storageManagementClient, ResourceContext context,
            HashMap<String, String> tags) throws Exception {
        String storageAccountName = context.getStorageAccountName();
        StorageAccountUpdateResponse response = null;
        StorageAccountUpdateParameters parameters = new StorageAccountUpdateParameters();

        if(tags != null) {
            parameters.setTags(tags);
        }

        ExecutorService service = null;
        try {
            Future <StorageAccountUpdateResponse> future = null;
            service = Executors.newFixedThreadPool(1);
            future = storageManagementClient.getStorageAccountsOperations()
                    .updateAsync(context.getResourceGroupName(),
                            storageAccountName, parameters);
            response = future.get();
        } catch (Exception ex) {
            throw ex;
        } finally {
            service.shutdown();
        }

        return response;
    }
}
