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

package com.microsoft.azure.storage.queue;

/**
 * RESERVED FOR INTERNAL USE. Holds the Constants used for the Queue Service.
 */
final class QueueConstants {
    /**
     * The header that specifies the approximate message count of a queue.
     */
    public static final String APPROXIMATE_MESSAGES_COUNT = com.microsoft.azure.storage.Constants.PREFIX_FOR_STORAGE_HEADER
            + "approximate-messages-count";

    /**
     * Default visibility time out for the retrieve message operation.
     */
    public static final int DEFAULT_VISIBILITY_MESSAGE_TIMEOUT_IN_SECONDS = 30;

    /**
     * XML element for DequeueCount.
     */
    public static final String DEQUEUE_COUNT_ELEMENT = "DequeueCount";

    /**
     * XML element for ExpirationTime.
     */
    public static final String EXPIRATION_TIME_ELEMENT = "ExpirationTime";

    /**
     * XML element for InsertionTime.
     */
    public static final String INSERTION_TIME_ELEMENT = "InsertionTime";

    /**
     * The maximum message size in bytes.
     */
    public static final long MAX_MESSAGE_SIZE = 64 * com.microsoft.azure.storage.Constants.KB;

    /**
     * The maximum number of messages that can be peeked or retrieved at a time.
     */
    public static final int MAX_NUMBER_OF_MESSAGES_TO_PEEK = 32;

    /**
     * The maximum amount of time a message is kept inside the queue, in seconds.
     */
    public static final int MAX_TIME_TO_LIVE_IN_SECONDS = 7 * 24 * 60 * 60;

    /**
     * XML element for MessageId.
     */
    public static final String MESSAGE_ID_ELEMENT = "MessageId";

    /**
     * XML element for MessageText.
     */
    public static final String MESSAGE_TEXT_ELEMENT = "MessageText";

    /**
     * The URI path component to access the messages in a queue.
     */
    public static final String MESSAGES = "messages";

    /**
     * XML element for PopReceipt.
     */
    public static final String POP_RECEIPT_ELEMENT = "PopReceipt";

    /**
     * XML element for a queue.
     */
    public static final String QUEUE_ELEMENT = "Queue";

    /**
     * XML element for QueueMessage.
     */
    public static final String QUEUE_MESSAGE_ELEMENT = "QueueMessage";

    /**
     * XML element for QueueMessagesList.
     */
    public static final String QUEUE_MESSAGES_LIST_ELEMENT = "QueueMessagesList";

    /**
     * XML element for queues.
     */
    public static final String QUEUES_ELEMENT = "Queues";

    /**
     * XML element for TimeNextVisible.
     */
    public static final String TIME_NEXT_VISIBLE_ELEMENT = "TimeNextVisible";

    /**
     * Private Default Constructor.
     */
    private QueueConstants() {
        // No op
    }
}
