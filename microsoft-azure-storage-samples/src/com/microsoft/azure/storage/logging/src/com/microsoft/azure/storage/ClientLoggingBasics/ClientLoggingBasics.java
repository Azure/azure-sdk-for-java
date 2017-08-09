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
package com.microsoft.azure.storage.ClientLoggingBasics;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.UUID;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

/**
 * This sample illustrates basic usage of enabling client-side logging in the Java library.
 * This is a stand along sample with its own connection string.
 */
public class ClientLoggingBasics {

    /**
     * MODIFY THIS!
     *
     * Stores the storage connection string.
     */
    public static final String storageConnectionString = "DefaultEndpointsProtocol=https;"
            + "AccountName=[MY_ACCOUNT_NAME];"
            + "AccountKey=[MY_ACCOUNT_KEY]";

    /**
     * Executes the sample.
     *
     * @param args
     *            No input args are expected from users.
     * @throws URISyntaxException
     * @throws InvalidKeyException
     */
    public static void main(String[] args) throws InvalidKeyException, URISyntaxException, StorageException {
        // Logs will be written to standard out by default and can be redirected to a file
        System.out.println("The Azure storage client sample to enable logging has started.");

        // Setup the cloud storage account.
        CloudStorageAccount account = CloudStorageAccount.parse(storageConnectionString);

        // Create a blob service client
        CloudBlobClient blobClient = account.createCloudBlobClient();

        // Get a reference to a container
        // This operation will not be logged since it does not make a service request.
        // Append a random UUID to the end of the container name so that
        // this sample can be run more than once in quick succession.
        CloudBlobContainer container = blobClient.getContainerReference("basicloggingcontainer"
                + UUID.randomUUID().toString().replace("-", ""));

        try {
            EnableGlobalLogging(container);

            EnablePerRequestLogging(container);
        }
        catch (Throwable t) {
            printException(t);
            container.deleteIfExists();
        }

        System.out.println("The Azure storage client sample to enable logging has completed.");
    }

    public static void EnableGlobalLogging(CloudBlobContainer container) throws StorageException {
        System.out.println("Enable logging for all requests");

        // Enable logging for cases where an OperationContext instance is not used in an API call.
        OperationContext.setLoggingEnabledByDefault(true);

        // Create the container if it does not exists.
        // This operation contacts the Azure Storage Service and will be logged.
        // Creating a container is just an example of a request to log.
        container.createIfNotExists();
    }

    public static void EnablePerRequestLogging(CloudBlobContainer container) throws StorageException, URISyntaxException, IOException {
        System.out.println("Enable logging per selected requests");

        // Set logging to false by default and pass in an operation context to log only specific APIs
        OperationContext.setLoggingEnabledByDefault(false);

        // Upload a blob passing in the operation context
        // Get a reference to a blob in the container
        // This operation will not be logged since it does not make a service request.
        CloudBlockBlob blob = container.getBlockBlobReference("blob");

        OperationContext operationContext = new OperationContext();
        operationContext.setLoggingEnabled(true);

        // Upload text to the blob passing in the operation context so the request is logged.
        // This operation contacts the Azure Storage Service and will be logged
        // since it is passing in an operation context that enables logging.
        blob.uploadText("Hello, World", null, null, null, operationContext);

        // Delete the container if it exists.
        // This operation will not be logged since logging has been disabled
        // by default and no operation context is being passed in.
        container.deleteIfExists();
    }

    /**
     * Prints out the exception information.
     */
    public static void printException(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        t.printStackTrace(printWriter);
        System.out.println(String.format(
                "Got an exception while running the sample. Exception details:\n%s\n",
                stringWriter.toString()));
    }
}