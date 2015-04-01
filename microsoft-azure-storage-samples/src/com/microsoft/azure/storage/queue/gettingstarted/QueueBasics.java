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
package com.microsoft.azure.storage.queue.gettingstarted;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.EnumSet;
import java.util.UUID;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.queue.CloudQueue;
import com.microsoft.azure.storage.queue.CloudQueueClient;
import com.microsoft.azure.storage.queue.CloudQueueMessage;
import com.microsoft.azure.storage.queue.MessageUpdateFields;
import com.microsoft.azure.storage.util.Utility;

/**
 * This sample illustrates basic usage of the various Queue Primitives provided
 * in the Storage Client Library including CloudQueue, CloudQueueMessage 
 * and CloudQueueClient.
 */
public class QueueBasics {
	
	/**
	 * Executes the sample.
	 * 
	 * @param args
	 *            No input args are expected from users.
	 * @throws URISyntaxException
	 * @throws InvalidKeyException
	 */
	public static void main(String[] args) throws InvalidKeyException, URISyntaxException {
		Utility.printSampleStartInfo("QueueBasics");

		// Setup the cloud storage account.
		CloudStorageAccount account = CloudStorageAccount.parse(Utility.storageConnectionString);

		// Create a queue service client
		CloudQueueClient queueClient = account.createCloudQueueClient();
		
		try {
			// Retrieve a reference to a queue
			// Append a random UUID to the end of the queue name so that this
			// sample can be run more than once in quick succession.
			CloudQueue queue = queueClient.getQueueReference("queuebasics"
					+ UUID.randomUUID().toString().replace("-", ""));

			// Create the queue if it doesn't already exist
			queue.createIfNotExists();
			
			// Create messages and add it to the queue
			CloudQueueMessage message1 = new CloudQueueMessage("Hello, World1");
			queue.addMessage(message1);
			
			CloudQueueMessage message2 = new CloudQueueMessage("Hello, World2");
			queue.addMessage(message2);
			
			CloudQueueMessage message3 = new CloudQueueMessage("Hello, World3");
			queue.addMessage(message3);
			
			CloudQueueMessage message4 = new CloudQueueMessage("Hello, World4");
			queue.addMessage(message4);
			
			CloudQueueMessage message5 = new CloudQueueMessage("Hello, World5");
			queue.addMessage(message5);
			
			// Peek at the next message
			CloudQueueMessage peekedMessage = queue.peekMessage();
			System.out.println(String.format("Peeked Message : %s", peekedMessage.getMessageContentAsString()));
			
			// Modify the message content and set it to be visible immediately
			// Retrieve the first visible message in the queue
			CloudQueueMessage updateMessage = queue.retrieveMessage();
			updateMessage.setMessageContent("Updated contents.");
			EnumSet<MessageUpdateFields> updateFields = 
			    EnumSet.of(MessageUpdateFields.CONTENT, MessageUpdateFields.VISIBILITY);
			queue.updateMessage(updateMessage, 0, updateFields, null, null);
			
			// Retrieve 3 messages from the queue with a visibility timeout of 1 second
			queue.retrieveMessages(3, 1, null, null);
			
			// Sleep for 2 seconds so the messages become visible and can be processed/deleted
			Thread.sleep(2000);
			
			// Retrieve the messages in the queue with a visibility timeout of 30 seconds and delete them
			CloudQueueMessage retrievedMessage;
			while((retrievedMessage = queue.retrieveMessage(30, null /*options*/, null /*opContext*/)) != null) {
				// Process the message in less than 30 seconds, and then delete the message.
				System.out.println(retrievedMessage.getMessageContentAsString());
				queue.deleteMessage(retrievedMessage);
			}
			
			// Delete a queue
			queue.deleteIfExists();
			
		}catch (Throwable t) {
			Utility.printException(t);
		}

		Utility.printSampleCompleteInfo("QueueBasics");
	}
}
