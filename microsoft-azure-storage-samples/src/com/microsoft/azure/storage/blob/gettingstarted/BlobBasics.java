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
package com.microsoft.azure.storage.blob.gettingstarted;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.UUID;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.azure.storage.util.Utility;

/**
 * This sample illustrates basic usage of the various Blob Primitives provided
 * in the Storage Client Library including CloudBlobContainer, CloudBlockBlob 
 * and CloudBlobClient.
 */
public class BlobBasics {

	/**
	 * Executes the sample.
	 * 
	 * @param args
	 *            No input args are expected from users.
	 * @throws URISyntaxException 
	 * @throws InvalidKeyException 
	 */
	public static void main(String[] args) throws InvalidKeyException, URISyntaxException {
		Utility.printSampleStartInfo("BlobBasics");

		// Setup the cloud storage account.
		CloudStorageAccount account = CloudStorageAccount.parse(Utility.storageConnectionString);

		// Create a blob service client
		CloudBlobClient blobClient = account.createCloudBlobClient();
		
		try {
			// Get a reference to a container
			// The container name must be lower case
			// Append a random UUID to the end of the container name so that
			// this sample can be run more than once in quick succession.
			CloudBlobContainer container = blobClient.getContainerReference("blobbasicscontainer"
					+ UUID.randomUUID().toString().replace("-", ""));

			// Create the container if it does not exist
			container.createIfNotExists();
			
			// Make the container public
			// Create a permissions object
			BlobContainerPermissions containerPermissions = new BlobContainerPermissions();

			// Include public access in the permissions object
			containerPermissions.setPublicAccess(BlobContainerPublicAccessType.CONTAINER);

			// Set the permissions on the container
			container.uploadPermissions(containerPermissions);
			
			// Upload 3 blobs
			// Get a reference to a blob in the container
			CloudBlockBlob blob1 = container.getBlockBlobReference("blobbasicsblob1");
			
			// Upload text to the blob
			blob1.uploadText("Hello, World1");
			
			// Get a reference to a blob in the container
			CloudBlockBlob blob2 = container.getBlockBlobReference("blobbasicsblob2");
						
			// Upload text to the blob
			blob2.uploadText("Hello, World2");
			
			// Get a reference to a blob in the container
			CloudBlockBlob blob3 = container.getBlockBlobReference("blobbasicsblob3");
									
			// Upload text to the blob
			blob3.uploadText("Hello, World3");
						
			// Download the blob
			// For each item in the container
			for (ListBlobItem blobItem : container.listBlobs()) {
			    // If the item is a blob, not a virtual directory
			    if (blobItem instanceof CloudBlockBlob) {
			        // Download the text
			    	CloudBlockBlob retrievedBlob = (CloudBlockBlob) blobItem;
			    	System.out.println(retrievedBlob.downloadText());
			    }
			}

			// List the blobs in a container, loop over them and 
			// output the URI of each of them
			for (ListBlobItem blobItem : container.listBlobs()) {
			    System.out.println(blobItem.getUri());
			}
			
			// Delete the blobs
			blob1.deleteIfExists();
			blob2.deleteIfExists();
			blob3.deleteIfExists();

			// Delete the container
			container.deleteIfExists();
		}
		catch (Throwable t) {
			Utility.printException(t);
		}
		
		Utility.printSampleCompleteInfo("BlobBasics");
	}
}
