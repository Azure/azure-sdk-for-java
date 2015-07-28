/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.management.compute;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Callable;

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.junit.Assert;

import com.microsoft.windowsazure.Configuration;
import com.microsoft.windowsazure.MockIntegrationTestBase;
import com.microsoft.windowsazure.core.OperationResponse;
import com.microsoft.windowsazure.core.ServiceClient;
import com.microsoft.windowsazure.core.pipeline.apache.ApacheConfigurationProperties;
import com.microsoft.windowsazure.core.utils.KeyStoreType;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.management.ManagementClient;
import com.microsoft.windowsazure.management.ManagementService;
import com.microsoft.windowsazure.management.compute.models.MockCloudBlobClient;
import com.microsoft.windowsazure.management.compute.models.MockCloudBlobContainer;
import com.microsoft.windowsazure.management.compute.models.MockCloudPageBlob;
import com.microsoft.windowsazure.management.compute.models.MockListBlobItem;
import com.microsoft.windowsazure.management.configuration.ManagementConfiguration;
import com.microsoft.windowsazure.management.models.LocationAvailableServiceNames;
import com.microsoft.windowsazure.management.models.LocationsListResponse;
import com.microsoft.windowsazure.management.storage.StorageManagementClient;
import com.microsoft.windowsazure.management.storage.StorageManagementService;
import com.microsoft.windowsazure.management.storage.models.StorageAccountCreateParameters;
import com.microsoft.windowsazure.management.storage.models.StorageAccountGetKeysResponse;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;

public abstract class ComputeManagementIntegrationTestBase extends MockIntegrationTestBase{
    protected static String testVMPrefix = "aztst";
    protected static String testStoragePrefix = "aztst";
    protected static String testHostedServicePrefix = "azhst";
    protected static String storageAccountKey = "";
    protected static String vmLocation = null;
    protected static String blobhost = "";
    
    protected static ComputeManagementClient computeManagementClient;
    protected static StorageManagementClient storageManagementClient;
    protected static ManagementClient managementClient;

    protected static void createComputeManagementClient() throws Exception {
        Configuration config = createConfiguration();
        computeManagementClient = ComputeManagementService.create(config);
        addClient((ServiceClient<?>) computeManagementClient, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createComputeManagementClient();
                return null;
            }
        });
        addRegexRule("hostedservices/azhst[a-z]{10}");
    }
    
    protected static void createStorageManagementClient() throws Exception {
        Configuration config = createConfiguration();
        storageManagementClient = StorageManagementService.create(config);
        addClient((ServiceClient<?>) storageManagementClient, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createStorageManagementClient();
                return null;
            }
        });
        addRegexRule("storageservices/aztst[a-z]{10}");
    }
    
    protected static void createManagementClient() throws Exception {
        Configuration config = createConfiguration();
        config.setProperty(ApacheConfigurationProperties.PROPERTY_RETRY_HANDLER, new DefaultHttpRequestRetryHandler());

        managementClient = ManagementService.create(config);
        addClient((ServiceClient<?>) managementClient, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                createManagementClient();
                return null;
            }
        });
    }
   
    protected static Configuration createConfiguration() throws Exception {
        String baseUri = System.getenv(ManagementConfiguration.URI);
        if (IS_MOCKED) {
            return ManagementConfiguration.configure(
                    new URI(MOCK_URI),
                    MOCK_SUBSCRIPTION,
                    null,
                    null,
                    null
            );
        } else {
            return ManagementConfiguration.configure(
                    baseUri != null ? new URI(baseUri) : null,
                    System.getenv(ManagementConfiguration.SUBSCRIPTION_ID),
                    System.getenv(ManagementConfiguration.KEYSTORE_PATH),
                    System.getenv(ManagementConfiguration.KEYSTORE_PASSWORD),
                    KeyStoreType.fromString(System.getenv(ManagementConfiguration.KEYSTORE_TYPE))
            );
        }
    }
    
    protected static String randomString(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);
        for (int i=0; i<length; i++) {
            stringBuilder.append((char)('a' + random.nextInt(26)));
        }
        return stringBuilder.toString();
    }
    
    protected static void createStorageAccount(String storageAccountName, String storageContainer) throws Exception {
        //String storageAccountCreateName = testStoragePrefix + randomString(10);
        String storageAccountLabel = storageAccountName + "Label1";

        //Arrange
        StorageAccountCreateParameters createParameters = new StorageAccountCreateParameters();
        //required
        createParameters.setName(storageAccountName);
        //required
        createParameters.setLabel(storageAccountLabel);
        //required if no affinity group has set
        createParameters.setLocation(vmLocation);
        createParameters.setAccountType("Standard_LRS");

        //act
        OperationResponse operationResponse = storageManagementClient.getStorageAccountsOperations().create(createParameters);

        //Assert
        Assert.assertEquals(200, operationResponse.getStatusCode());

        //use container inside storage account, needed for os image storage.
        StorageAccountGetKeysResponse storageAccountGetKeysResponse = storageManagementClient.getStorageAccountsOperations().getKeys(storageAccountName);
        storageAccountKey = storageAccountGetKeysResponse.getPrimaryKey();
        
        createStorageContainer(storageAccountName, storageContainer);
    }
    
    protected static void createStorageContainer(String storageAccountName, String storageContainer) throws Exception {
        MockCloudBlobClient blobClient = createBlobClient(storageAccountName, storageAccountKey);
        MockCloudBlobContainer container = blobClient.getContainerReference(storageContainer);      
        container.createIfNotExists();

        //make sure it created and available, otherwise vm deployment will fail with storage/container still creating
        boolean found = false;
        while(found == false) {
            Iterable<MockCloudBlobContainer> listContainerResult = blobClient.listContainers(storageContainer);
            for (MockCloudBlobContainer item : listContainerResult) {
                blobhost =item.getUri().getHost();
                if (item.getName().contains(storageContainer) == true) {
                    blobhost =item.getUri().getHost();
                    found = true;
                }
            }

            if (found == false) {
                Thread.sleep(1000 * 30);
            } else if (!IS_MOCKED) {
                Thread.sleep(1000 * 60);
            }
        }
    }

    protected static MockCloudBlobClient createBlobClient(String storageAccountName, String storageAccountKey) throws InvalidKeyException, URISyntaxException {
        String storageconnectionstring = "DefaultEndpointsProtocol=http;AccountName="+ storageAccountName +";AccountKey=" + storageAccountKey;
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageconnectionstring);

        // Create the blob client
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
        return new MockCloudBlobClient(blobClient, IS_MOCKED);
    }

    protected static void uploadFileToBlob(String storageAccountName, String storageContainer, String fileName, String filePath) throws InvalidKeyException, URISyntaxException, StorageException, InterruptedException, IOException {
        MockCloudBlobClient blobClient = createBlobClient(storageAccountName, storageAccountKey);
        MockCloudBlobContainer container = blobClient.getContainerReference(storageContainer);

        MockCloudPageBlob pageblob = container.getPageBlobReference(fileName);

        File source = new File(filePath + fileName);
        pageblob.upload(new FileInputStream(source), source.length());

        //make sure it created and available, otherwise vm deployment will fail with storage/container still creating
        boolean found = false;
        while(found == false) {
            // Loop over blobs within the container and output the URI to each of them
            for (MockListBlobItem item : container.listBlobs()) {
                if (item.getUri().getPath().contains(fileName) == true) {
                    found = true;
                }
            }
  
            if (found == false) {
                Thread.sleep(1000 * 10);
            } else if (!IS_MOCKED) {
                Thread.sleep(1000 * 20);
            }
        }
    }

    protected static void getLocation() throws Exception {
        //has to be a location that support compute, storage, vm, some of the locations are not, need to find out the right one
        ArrayList<String> serviceName = new ArrayList<String>();
        serviceName.add(LocationAvailableServiceNames.COMPUTE);
        serviceName.add(LocationAvailableServiceNames.PERSISTENT_VMROLE);
        serviceName.add(LocationAvailableServiceNames.STORAGE);

        LocationsListResponse locationsListResponse = managementClient.getLocationsOperations().list();
        for (LocationsListResponse.Location location : locationsListResponse) {
            ArrayList<String> availableServicelist = location.getAvailableServices();
            String locationName = location.getName();            
            if (availableServicelist.containsAll(serviceName)== true) {  
                if (locationName.contains("West US") == true)
                {
                    vmLocation = locationName;
                }
                if (vmLocation==null)
                {
                    vmLocation = locationName;
                }
            }
        }
    }

    protected static void cleanBlob(String storageAccountName, String storageContainer) {
        // Create the blob client
        MockCloudBlobClient blobClient = null;
        try {
            blobClient = createBlobClient(storageAccountName, storageAccountKey);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (blobClient != null)
        {
            MockCloudBlobContainer container = null;
            try {
                container = blobClient.getContainerReference(storageContainer);
            } catch (URISyntaxException e) {
            } catch (StorageException e) {
            }
            
            try {
                container.breakLease(0);
            } catch (StorageException e) {
            }
            
            try {
                container.delete();
            } catch (StorageException e) {
            }
            
            try {
                while (container.exists())
                {
                    Thread.sleep(1000);
                }
            } catch (StorageException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected static void cleanStorageAccount(String storageAccountName) {
        OperationResponse operationResponse = null;
        try {
            operationResponse = storageManagementClient.getStorageAccountsOperations().delete(storageAccountName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ServiceException e) {
            e.printStackTrace();
        }

        if (operationResponse != null) {
            Assert.assertEquals(200, operationResponse.getStatusCode());
        }
    }
}