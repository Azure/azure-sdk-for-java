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
package com.microsoft.windowsazure.services.queue.client;

import java.net.URISyntaxException;
import java.util.EnumSet;

import junit.framework.Assert;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.storage.StorageException;

public class CloudQueueClientGB18030Test extends QueueTestBase {

    // GB18030CharSet is "啊齄丂狛狜隣郎隣兀﨩ˊ▇█〞〡￤℡㈱‐ー﹡﹢﹫、〓ⅰⅹ⒈€㈠㈩ⅠⅫ！￣ぁんァヶΑ︴АЯаяāɡㄅㄩ─╋︵﹄︻︱︳︴ⅰⅹɑɡ〇〾⿻⺁䜣€㐀㒣㕴㕵㙉㙊䵯䵰䶴䶵".
    public static final String GB18030CharSet = new String(new char[] { 0x554A, 0x9F44, 0x4E02, 0x72DB, 0x72DC, 0xF9F1,
            0xF92C, 0xF9F1, 0xFA0C, 0xFA29, 0x02CA, 0x2587, 0x2588, 0x301E, 0x3021, 0xFFE4, 0x2121, 0x3231, 0x2010,
            0x30FC, 0xFE61, 0xFE62, 0xFE6B, 0x3001, 0x3013, 0x2170, 0x2179, 0x2488, 0x20AC, 0x3220, 0x3229, 0x2160,
            0x216B, 0xFF01, 0xFFE3, 0x3041, 0x3093, 0x30A1, 0x30F6, 0x0391, 0xFE34, 0x0410, 0x042F, 0x0430, 0x044F,
            0x0101, 0x0261, 0x3105, 0x3129, 0x2500, 0x254B, 0xFE35, 0xFE44, 0xFE3B, 0xFE31, 0xFE33, 0xFE34, 0x2170,
            0x2179, 0x0251, 0xE7C7, 0x0261, 0x3007, 0x303E, 0x2FFB, 0x2E81, 0xE843, 0x4723, 0xE864, 0x20AC, 0x3400,
            0x34A3, 0x3574, 0x3575, 0x3649, 0x364A, 0x4D6F, 0x4D70, 0x4DB4, 0x4DB5 });

    @Test
    public void GB18030TestForSingleMessage() throws URISyntaxException, StorageException {
        String messageContent = GB18030CharSet;
        CloudQueueMessage cqm = new CloudQueueMessage(messageContent);
        queue.addMessage(cqm);

        CloudQueueMessage messageFromPeekMessage = queue.peekMessage();
        Assert.assertEquals(messageContent, messageFromPeekMessage.getMessageContentAsString());

        CloudQueueMessage messageFromRetrieveMessage = queue.retrieveMessage();
        Assert.assertEquals(messageContent, messageFromRetrieveMessage.getMessageContentAsString());

        String messageContentUpdated = messageContent + " updated";
        messageFromRetrieveMessage.setMessageContent(messageContentUpdated);
        queue.updateMessage(messageFromRetrieveMessage, 0);

        messageFromPeekMessage = queue.peekMessage(null, null);
        Assert.assertEquals(messageContent, messageFromPeekMessage.getMessageContentAsString());

        messageContentUpdated = messageContent + " updated again";
        messageFromRetrieveMessage.setMessageContent(messageContentUpdated);
        queue.updateMessage(messageFromRetrieveMessage, 0,
                EnumSet.of(MessageUpdateFields.VISIBILITY, MessageUpdateFields.CONTENT), null, null);

        messageFromRetrieveMessage = queue.retrieveMessage(5, null, null);
        Assert.assertEquals(messageContentUpdated, messageFromRetrieveMessage.getMessageContentAsString());

        queue.deleteMessage(messageFromRetrieveMessage);
    }

    @Test
    public void GB18030TestForMultipleMessages() throws URISyntaxException, StorageException {
        int messageLength = 2;
        String[] messageContents = new String[messageLength];
        for (int i = 0; i < messageLength; i++) {
            messageContents[i] = GB18030CharSet + i;
            queue.addMessage(new CloudQueueMessage(messageContents[i]), 600, 0, null, null);
        }

        Iterable<CloudQueueMessage> messagesFromPeekMessages = queue.peekMessages(messageLength);
        int count = 0;
        for (CloudQueueMessage message : messagesFromPeekMessages) {
            Assert.assertEquals(messageContents[count], message.getMessageContentAsString());
            count++;
        }

        Iterable<CloudQueueMessage> messagesFromRetrieveMessages = queue.retrieveMessages(messageLength);
        count = 0;
        for (CloudQueueMessage message : messagesFromRetrieveMessages) {
            Assert.assertEquals(messageContents[count], message.getMessageContentAsString());
            message.setMessageContent(message.getMessageContentAsString() + " updated");
            queue.updateMessage(message, 0, EnumSet.of(MessageUpdateFields.VISIBILITY, MessageUpdateFields.CONTENT),
                    null, null);
            count++;
        }

        messagesFromPeekMessages = queue.peekMessages(messageLength, null, null);
        count = 0;
        for (CloudQueueMessage message : messagesFromPeekMessages) {
            Assert.assertEquals(messageContents[count] + " updated", message.getMessageContentAsString());
            count++;
        }

        messagesFromRetrieveMessages = queue.retrieveMessages(messageLength, 5, null, null);
        count = 0;
        for (CloudQueueMessage message : messagesFromRetrieveMessages) {
            Assert.assertEquals(messageContents[count] + " updated", message.getMessageContentAsString());
            queue.deleteMessage(message, null, null);
            count++;
        }
    }
}
