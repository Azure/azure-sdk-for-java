package com.microsoft.windowsazure.services.queue.client;

/**
 * RESERVED FOR INTERNAL USE. Holds the Constants used for the Queue Service.
 */
final class QueueConstants {
    /**
     * Default client side timeout, in milliseconds, for queue service.
     */
    public static final int DEFAULT_QUEUE_CLIENT_TIMEOUT_IN_MS = 30 * 1000;

    /**
     * XML element for a queue.
     */
    public static final String QUEUE_ELEMENT = "Queue";

    /**
     * XML element for queues.
     */
    public static final String QUEUES_ELEMENT = "Queues";

    /**
     * XML element for QueueMessagesList.
     */
    public static final String QUEUE_MESSAGES_LIST_ELEMENT = "QueueMessagesList";

    /**
     * XML element for QueueMessage.
     */
    public static final String QUEUE_MESSAGE_ELEMENT = "QueueMessage";

    /**
     * XML element for MessageId.
     */
    public static final String MESSAGE_ID_ELEMENT = "MessageId";

    /**
     * XML element for InsertionTime.
     */
    public static final String INSERTION_TIME_ELEMENT = "InsertionTime";

    /**
     * XML element for ExpirationTime.
     */
    public static final String EXPIRATION_TIME_ELEMENT = "ExpirationTime";

    /**
     * XML element for PopReceipt.
     */
    public static final String POP_RECEIPT_ELEMENT = "PopReceipt";

    /**
     * XML element for TimeNextVisible.
     */
    public static final String TIME_NEXT_VISIBLE_ELEMENT = "TimeNextVisible";

    /**
     * XML element for DequeueCount.
     */
    public static final String DEQUEUE_COUNT_ELEMENT = "DequeueCount";

    /**
     * XML element for MessageText.
     */
    public static final String MESSAGE_TEXT_ELEMENT = "MessageText";

    /**
     * The URI path component to access the messages in a queue.
     */
    public static final String MESSAGES = "messages";

    /**
     * The header that specifies the approximate message count of a queue.
     */
    public static final String APPROXIMATE_MESSAGES_COUNT =
            com.microsoft.windowsazure.services.core.storage.Constants.PREFIX_FOR_STORAGE_HEADER
                    + "approximate-messages-count";

    /**
     * Private Default Ctor.
     */
    private QueueConstants() {
        // No op
    }
}
