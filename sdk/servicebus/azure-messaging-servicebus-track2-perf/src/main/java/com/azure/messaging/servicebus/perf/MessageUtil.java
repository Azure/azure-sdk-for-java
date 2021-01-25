// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.perf;

/**
 * A utility class for message.
 */
public class MessageUtil {

    /**
     * Generate the message content with given {@code messageSize} value.
     *
     * @param messageSize of the message to be generated.
     * @return the generated message.
     */
    public static String generateMessageContent(int messageSize) {
        char[] chars = new char[messageSize];
        while (messageSize > 0) {
            chars[--messageSize] = 'S'; // using a fixed character 'S'.
        }
        return new String(chars);
    }
}
