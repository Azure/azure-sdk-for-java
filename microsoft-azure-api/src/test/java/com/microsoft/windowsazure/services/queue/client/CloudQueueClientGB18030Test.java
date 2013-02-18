/**
 * Copyright 2011 Microsoft Corporation
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

    public static final String GB18030CharSet = "啊齄丂狛狜隣郎隣兀﨩ˊ▇█〞〡￤℡㈱‐ー﹡﹢﹫、〓ⅰⅹ⒈€㈠㈩ⅠⅫ！￣ぁんァヶΑ︴АЯаяāɡㄅㄩ─╋︵﹄︻︱︳︴ⅰⅹɑɡ〇〾⿻⺁䜣€㐀㒣㕴㕵㙉㙊䵯䵰䶴䶵";

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
