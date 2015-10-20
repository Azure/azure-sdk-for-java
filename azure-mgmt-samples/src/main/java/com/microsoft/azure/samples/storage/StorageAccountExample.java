/**
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * <p>
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.azure.samples.storage;

import com.microsoft.azure.management.storage.StorageManagementClient;
import com.microsoft.azure.management.storage.StorageManagementService;
import com.microsoft.azure.management.storage.models.AccountType;
import com.microsoft.azure.management.storage.models.KeyName;
import com.microsoft.azure.management.storage.models.StorageAccount;
import com.microsoft.azure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.azure.management.storage.models.StorageAccountKeys;
import com.microsoft.azure.management.storage.models.StorageAccountRegenerateKeyResponse;
import com.microsoft.azure.management.storage.models.StorageAccountUpdateResponse;
import com.microsoft.azure.utility.AuthHelper;
import com.microsoft.azure.utility.ResourceContext;
import com.microsoft.azure.utility.StorageHelper;
import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

public class StorageAccountExample {
    /**
     * Examples for managing storage accounts.
     * 
     * The following code allows for the performance of various actions from the 
     * command line on storage accounts.
     *
     * For single storage account the following commands are supported along with
     * the expected command line arguments:
     * 
     *      create <resource group> <account name> <type> <location>
     *          - creates the storage account
     *      delete <resource group> <account name>
     *          - deletes the storage account
     *      retrieve <resource group> <account name>
     *          - shows information about the storage account
     *      regenerateKey <resource group> <account name> <primary/secondary>
     *          - regenerates the specified key
     *      listAccounts [resource group]
     *          - if specified, list all storage accounts in the resource group,
     *            otherwise, list all for subscription.
     *      updateTags <resource group> <account name> [<key> <value>]...
     *          - updates the tags on a storage account.  up to fifteern key/value pairs can
     *            be specified.  if none are specified, tags are removed.
     *
     * To use the sample please set following environment variable or simply replace the getenv call
     * with actual value:
     *
     management.uri=https://management.core.windows.net/
     arm.url=https://management.azure.com/
     arm.aad.url=https://login.windows.net/
     arm.clientid=[your service principal client id]
     arm.clientkey=[your service principal client key]
     arm.tenant=[your service principal tenant]
     management.subscription.id=[your subscription id (GUID)]
     *
     * @param args arguments supplied at the command line (see above for commands and expected args
     * @throws Exception all of the exceptions!!
     */
    public static void main(String[] args) throws Exception {
        Configuration config = createConfiguration();
        StorageManagementClient storageManagementClient = null;

        // When using the Java command line, a run time error similar to the following
        // was occurring:
        //          Exception in thread "main" java.lang.RuntimeException: 
        //          Service or property not registered:  
        //          com.microsoft.azure.management.storage.StorageManagementClient 
        //          interface com.microsoft.azure.management.storage.StorageManagementClient
        // To get around this, explicitly setting the ContextClassLoader works.
        //
        // This "hack" is not needed in IntelliJ.

        // Get current context class loader
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader(); 
        // Change context classloader to class context loader   
        Thread.currentThread().setContextClassLoader(ManagementConfiguration.class.getClassLoader()); 
        try { 
            storageManagementClient = StorageManagementService.create(config);
        } catch (Exception e) {
            // handle exceptions  
        } finally {
            // Reset back class loader
            Thread.currentThread().setContextClassLoader(contextLoader); 
        }        
        
        if(args.length == 0) {
            displayUsageAndExit(null);
        }
        
        // assume guilty until proven innocent
        int result = 1;

        if(args[0].equals("create")) {
            if(args.length != 5) {
                result = -1;
            } else {
                result = createStorageAccount(storageManagementClient, args[1], args[2], args[3], args[4]);
            }
        } else if(args[0].equals("delete")) {
            if(args.length != 3) {
                result = -1;
            } else {
                result = deleteStorageAccount(storageManagementClient, args[1], args[2]);
            }
        } else if(args[0].equals("retrieve")) {
            if(args.length != 3) {
                result = -1;
            } else {
                result = retrieveStorageAccount(storageManagementClient, args[1], args[2]);
            }
        } else if(args[0].equals("regenerateKey")) {
            if (args.length != 4) {
                result = -1;
            } else {
                KeyName keyName = null;
                if (args[3].toLowerCase().equals("primary")) {
                    keyName = KeyName.Key1;
                } else if (args[3].toLowerCase().equals("secondary")) {
                    keyName = KeyName.Key2;
                }
                if (keyName == null) {
                    result = -1;
                } else {
                    result = regenerateStorageAccountKey(storageManagementClient, args[1], args[2], keyName);
                }
            }
        } else if(args[0].equals("listAccounts")) {
            if (args.length > 2) {
                result = -1;
            } else {
                result = listAccounts(storageManagementClient, (args.length == 1 ? null : args[1]));
            }
        } else if(args[0].equals("updateTags")) {
            if((args.length < 3) || ((args.length - 3) % 2 != 0)) {
                result = -1;
            } else {
                HashMap<String, String> tags = null;
                if(args.length > 3) {
                    tags = new HashMap<String, String>();
                    for(int i = 3; i < args.length; i += 2) {
                        tags.put(args[i], args[i + 1]);
                    }
                }
                result = updateAccountTags(storageManagementClient, args[1], args[2], tags);
            }
        } else {
            displayUsageAndExit(null);
        }
        
        if(result == -1) {
            // an invalid number of arguments were given for a command.
            displayUsageAndExit(args[0]);
        }
        
        System.exit(result);
    }
    
    private static void displayUsageAndExit(String command) {
        if(command == null) {
            System.out.println("Invalid command line.");
        } else {
            System.out.println("Invalid number of arguments for command: " + command);
        }

        System.out.println("For single storage account the following commands are supported along with");
        System.out.println("the expected command line arguments:");System.out.println("");
        System.out.println("     create <resource group> <account name> <type> <location>");
        System.out.println("         - creates the storage account");
        System.out.println("     delete <resource group> <account name>");
        System.out.println("         - deletes the storage account");
        System.out.println("     retrieve <resource group> <account name>");
        System.out.println("         - shows information about the storage account");
        System.out.println("     regenerateKey <resource group> <account name> <primary/secondary>");
        System.out.println("         - regenerates the specified key");
        System.out.println("     listAccounts [resource group]");
        System.out.println("         - if specified, list all storage accounts in the resource group,");
        System.out.println("           otherwise, list all for subscription.");
        System.out.println("     updateTags <resource group> <account name> [<key> <value>]...");
        System.out.println("         - updates the tags on a storage account.  up to fifteern key/value pairs can");
        System.out.println("           be specified.  if none are specified, tags are removed.");
        System.out.println("");
        System.out.println("To use the sample please set following environment variable or simply replace the getenv call");
        System.out.println("with actual value:");
        System.out.println("     management.uri=https://management.core.windows.net/");
        System.out.println("     arm.url=https://management.azure.com/");
        System.out.println("     arm.aad.url=https://login.windows.net/");
        System.out.println("     arm.clientid=[your service principal client id]");
        System.out.println("     arm.clientkey=[your service principal client key]");
        System.out.println("     arm.tenant=[your service principal tenant]");
        System.out.println("     management.subscription.id =[your subscription id(GUID)]");

        System.exit(1);
    }
    
    private static AccountType accountTypeStrToVal(String accountType) {
        if(accountType.toLowerCase().equals("standard_grs")) {
            return AccountType.StandardGRS;
        } else if(accountType.toLowerCase().equals("standard_ragrs")) {
            return AccountType.StandardRAGRS;
        } else if(accountType.toLowerCase().equals("standard_lrs")) {
            return AccountType.StandardLRS;
        } else if(accountType.toLowerCase().equals("standard_zrs")) {
            return AccountType.StandardZRS;
        } else if(accountType.toLowerCase().equals("premium_lrs")) {
            return AccountType.PremiumLRS;
        }
        
        return null;
    }
    
    /**
     * Create a storage account.
     *
     * @param client                storage management client
     * @param resourceGroup         name of the resource group
     * @param accountName           name of the storage account
     * @param accountType           type of storage account
     * @param location              location of the storage account
     * @return int                  zero on success, one on failure, minus one for invalid arg
     * @throws Exception            throw all exceptions
     */
    private static int createStorageAccount(StorageManagementClient client,
                                            String resourceGroup, String accountName, 
                                            String accountType, String location) 
                                            throws Exception {
        AccountType accountTypeVal = accountTypeStrToVal(accountType);
        if(accountTypeVal == null) {
            // unknown storage type
            return -1;
        }
        
        ResourceContext context = new ResourceContext(location, resourceGroup,
                                            getPropertyElseEnvironment(ManagementConfiguration.SUBSCRIPTION_ID),
                                            false);
        context.setStorageAccountName(accountName);
        StorageAccountCreateParameters stoInput = new StorageAccountCreateParameters(accountTypeVal,
                                                                                     location);
                                                                                     
        StorageAccount account = StorageHelper.createStorageAccount(client, context, stoInput);
        if(account == null) {
            System.out.println("Creation of storage account failed: " + accountName);
        } else {
            System.out.println("Creation of storage account succeeded: " + accountName);
        }
        
        return (account == null ? 1 : 0);
    } 
    
    /**
     * Delete a storage account.
     *
     * @param client                storage management client
     * @param resourceGroup         name of the resource group
     * @param accountName           name of the storage account
     * @return int                  zero on success, one on failure
     * @throws Exception            throw all exceptions
     */
    private static int deleteStorageAccount(StorageManagementClient client,
                                            String resourceGroup, String accountName) 
                                            throws Exception {
        ResourceContext context = new ResourceContext(null, resourceGroup,
                                            getPropertyElseEnvironment(ManagementConfiguration.SUBSCRIPTION_ID),
                                            false);
        boolean result = StorageHelper.deleteStorageAccount(client, context);
        context.setStorageAccountName(accountName);
        if(result) {
            System.out.println("Storage account deleted: " + accountName);
        } else {
            System.out.println("Unable to delete storage account: " + accountName);
        }
               
        return (result ? 0 : 1);                                            
    } 

    /**
     * Retrieve a storage account and print it out.
     *
     * @param client                storage management client
     * @param resourceGroup         name of the resource group
     * @param accountName           name of the storage account
     * @return int                  zero on success, one on failure
     * @throws Exception            throw all exceptions
     */
    private static int retrieveStorageAccount(StorageManagementClient client,
                                            String resourceGroup, String accountName) 
                                            throws Exception {
        ResourceContext context = new ResourceContext(null, resourceGroup, 
                                            getPropertyElseEnvironment(ManagementConfiguration.SUBSCRIPTION_ID),
                                            false);
        context.setStorageAccountName(accountName);
        StorageAccount account = StorageHelper.getStorageAccount(client, context);
        if(account != null) {
            System.out.println("Account name:  " + accountName);
            System.out.println("Account type:  " + account.getAccountType().toString());
            System.out.println("Creation time: " + account.getCreationTime().toString()); 
        } else {
            System.out.println("Unable to retrieve storage account: " + accountName);
        }
               
        return (account == null ? 1 : 0);
    }

    /**
     * Regenerate the specified key on a storage account and print it out.
     *
     * @param client                storage management client
     * @param resourceGroup         name of the resource group
     * @param accountName           name of the storage account
     * @param keyName               the key to regenerate
     * @return int                  zero on success, one on failure
     * @throws Exception            throw all exceptions
     */
    private static int regenerateStorageAccountKey(StorageManagementClient client,
                                                   String resourceGroup, String accountName,
                                                   KeyName keyName)
            throws Exception {
        ResourceContext context = new ResourceContext(null, resourceGroup,
                getPropertyElseEnvironment(ManagementConfiguration.SUBSCRIPTION_ID),
                false);
        context.setStorageAccountName(accountName);
        StorageAccountRegenerateKeyResponse response = StorageHelper.regenerateStorageAccountKey(client, context,
                keyName);
        if(response != null) {
            System.out.println("Key " + (keyName == KeyName.Key1 ? "Primary" : "Secondary") + " regenerated.");
            StorageAccountKeys keys = response.getStorageAccountKeys();
            System.out.println("Key Value: " + (keyName == KeyName.Key1 ? keys.getKey1() : keys.getKey2()));
        }

        return (response == null ? 1 : 0);
    }

    /**
     * List storage accounts.  If resourceGroup not specified, list all for subscription.
     *
     * @param client                storage management client
     * @param resourceGroup         resourceGroup (or null)
     * @return int                  zero on success, one on failure
     * @throws Exception            throw all exceptions
     */
    private static int listAccounts(StorageManagementClient client, String resourceGroup) throws Exception {
        List<StorageAccount> accounts = null;
        accounts = StorageHelper.listStorageAccounts(client, resourceGroup);
        if(accounts != null) {
            System.out.println("Storage accounts for " +
                                (resourceGroup == null ? "subscription " + getPropertyElseEnvironment(ManagementConfiguration.SUBSCRIPTION_ID) :
                                                         "resource group " + resourceGroup) + ": ");
            for (StorageAccount account : accounts) {
                System.out.println("  " + account.getName());
            }
        }
        return (accounts == null ? 1 : 0);
    }

    /**
     * Update (replace) the tags for a storage account.  Specifying no tags
     *
     * @param client                storage management client
     * @param resourceGroup         name of the resource group
     * @param accountName           name of the storage account
     * @param tags                  a hash map of key value pairs to replace the tags (or null to empty them)
     * @return int                  zero on success, one on failure
     * @throws Exception            throw all exceptions
     */
    private static int updateAccountTags(StorageManagementClient client,
                                         String resourceGroup, String accountName,
                                         HashMap<String, String> tags) 
            throws Exception {
        ResourceContext context = new ResourceContext(null, resourceGroup,
                getPropertyElseEnvironment(ManagementConfiguration.SUBSCRIPTION_ID),
                false);
        context.setStorageAccountName(accountName);
        StorageAccountUpdateResponse response = StorageHelper.updateAccountTags(client, context, tags);
        if(response != null) {
            if(tags != null) {
                if (response.getStorageAccount().getTags().equals(tags)) {
                    System.out.println("Updating of tags for storage account " + accountName + "succeeded.");
                } else {
                    System.out.println("Updating of tags for storage account " + accountName + "failed.");
                }
            } else {
                if(response.getStorageAccount().getTags().isEmpty()) {
                    System.out.println("Updating of tags for storage account " + accountName + "succeeded.");
                } else {
                    System.out.println("Updating of tags for storage account " + accountName + "failed.");
                }
            }
        }

        return (response == null ? 1 : 0);
    }

    /**
     * Create configuration builds the management configuration needed for creating the clients.
     * The config contains the baseURI which is the base of the ARM REST service, the subscription id as the context for
     * the ResourceManagementService and the AAD token required for the HTTP Authorization header.
     *
     * @return Configuration the generated configuration
     * @throws Exception all of the exceptions!!
     */
    public static Configuration createConfiguration() throws Exception {
        // set up variables, and try both getPropertyElseEnvironment and System.getProperty.
        // this allows the use of defining the values on the command line using -D
        // default to the property first, else environment
        String baseUri = getPropertyElseEnvironment("arm.url");
        String aadUri = getPropertyElseEnvironment("arm.aad.url");
        String tenantId = getPropertyElseEnvironment("arm.tenant");
        String clientId = getPropertyElseEnvironment("arm.clientid");
        String clientKey = getPropertyElseEnvironment("arm.clientkey");
        String managementUri = getPropertyElseEnvironment(ManagementConfiguration.URI);
        String subscriptionId = getPropertyElseEnvironment(ManagementConfiguration.SUBSCRIPTION_ID);
        
        if((baseUri == null) || (aadUri == null) || (tenantId == null) ||
                (clientId == null) || (clientKey == null) || 
                (managementUri == null) || (subscriptionId == null)) {
            System.out.println("The following properties or environment variables must be set:");
            System.out.println("management.uri=https://management.core.windows.net/");
            System.out.println("arm.url=https://management.azure.com/");
            System.out.println("arm.aad.url=https://login.windows.net/");
            System.out.println("arm.clientid=[your service principal client id]");
            System.out.println("arm.clientkey=[your service principal client key]");
            System.out.println("arm.tenant=[your service principal tenant]");
            System.out.println("management.subscription.id=[your subscription id (GUID)]");
            System.exit(1);
        }
    
        return ManagementConfiguration.configure(
                null,
                baseUri != null ? new URI(baseUri) : null,
                subscriptionId,
                AuthHelper.getAccessTokenFromServicePrincipalCredentials(
                        managementUri, aadUri,
                        tenantId, clientId,
                        clientKey).getAccessToken());
    }
    
    /**
     * Helper method to check for a key.  Properties take precedence over environment
     * variables.
     *
     * @param key the key to look up
     * @return String value of the key (else null if not found)
     */
    private static String getPropertyElseEnvironment(String key) {
        if(System.getProperty(key) != null) {
            return System.getProperty(key);
        }
        return null;
    }
}
