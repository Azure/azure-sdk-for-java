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
import com.microsoft.azure.management.storage.models.AccountType;
import com.microsoft.azure.management.storage.models.StorageAccount;
import com.microsoft.azure.management.storage.models.StorageAccountCreateParameters;

import java.util.List;

public class StorageHelper {
    public static StorageAccount createStorageAccount(
            StorageManagementClient storageManagementClient, ResourceContext context)
            throws Exception {
        //create storage account
        StorageAccountCreateParameters stoInput = new StorageAccountCreateParameters(AccountType.STANDARDGRS,
                context.getLocation());
        return createStorageAccount(storageManagementClient, context, stoInput);
    }

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
        Thread.sleep((long)seconds * 100);
    }
}
