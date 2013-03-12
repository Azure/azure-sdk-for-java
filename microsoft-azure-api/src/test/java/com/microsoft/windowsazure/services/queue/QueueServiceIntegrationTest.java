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
package com.microsoft.windowsazure.services.queue;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.queue.models.CreateQueueOptions;
import com.microsoft.windowsazure.services.queue.models.GetQueueMetadataResult;
import com.microsoft.windowsazure.services.queue.models.ListMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.ListMessagesResult;
import com.microsoft.windowsazure.services.queue.models.ListMessagesResult.QueueMessage;
import com.microsoft.windowsazure.services.queue.models.ListQueuesOptions;
import com.microsoft.windowsazure.services.queue.models.ListQueuesResult;
import com.microsoft.windowsazure.services.queue.models.ListQueuesResult.Queue;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesResult;
import com.microsoft.windowsazure.services.queue.models.ServiceProperties;
import com.microsoft.windowsazure.services.queue.models.UpdateMessageResult;

public class QueueServiceIntegrationTest extends IntegrationTestBase {
    private static final String testQueuesPrefix = "sdktest-";
    private static final String createableQueuesPrefix = "csdktest-";
    private static String TEST_QUEUE_FOR_MESSAGES;
    private static String TEST_QUEUE_FOR_MESSAGES_2;
    private static String TEST_QUEUE_FOR_MESSAGES_3;
    private static String TEST_QUEUE_FOR_MESSAGES_4;
    private static String TEST_QUEUE_FOR_MESSAGES_5;
    private static String TEST_QUEUE_FOR_MESSAGES_6;
    private static String TEST_QUEUE_FOR_MESSAGES_7;
    private static String TEST_QUEUE_FOR_MESSAGES_8;
    private static String CREATABLE_QUEUE_1;
    private static String CREATABLE_QUEUE_2;
    private static String CREATABLE_QUEUE_3;
    private static String[] creatableQueues;
    private static String[] testQueues;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @BeforeClass
    public static void setup() throws Exception {
        // Setup container names array (list of container names used by
        // integration tests)
        testQueues = new String[10];
        for (int i = 0; i < testQueues.length; i++) {
            testQueues[i] = String.format("%s%d", testQueuesPrefix, i + 1);
        }

        creatableQueues = new String[10];
        for (int i = 0; i < creatableQueues.length; i++) {
            creatableQueues[i] = String.format("%s%d", createableQueuesPrefix, i + 1);
        }

        TEST_QUEUE_FOR_MESSAGES = testQueues[0];
        TEST_QUEUE_FOR_MESSAGES_2 = testQueues[1];
        TEST_QUEUE_FOR_MESSAGES_3 = testQueues[2];
        TEST_QUEUE_FOR_MESSAGES_4 = testQueues[3];
        TEST_QUEUE_FOR_MESSAGES_5 = testQueues[4];
        TEST_QUEUE_FOR_MESSAGES_6 = testQueues[5];
        TEST_QUEUE_FOR_MESSAGES_7 = testQueues[6];
        TEST_QUEUE_FOR_MESSAGES_8 = testQueues[7];

        CREATABLE_QUEUE_1 = creatableQueues[0];
        CREATABLE_QUEUE_2 = creatableQueues[1];
        CREATABLE_QUEUE_3 = creatableQueues[2];

        // Create all test containers and their content
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);

        createQueues(service, testQueuesPrefix, testQueues);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);

        deleteQueues(service, testQueuesPrefix, testQueues);
        deleteQueues(service, createableQueuesPrefix, creatableQueues);
    }

    private static void createQueues(QueueContract service, String prefix, String[] list) throws Exception {
        Set<String> containers = listQueues(service, prefix);
        for (String item : list) {
            if (!containers.contains(item)) {
                service.createQueue(item);
            }
        }
    }

    private static void deleteQueues(QueueContract service, String prefix, String[] list) throws Exception {
        Set<String> containers = listQueues(service, prefix);
        for (String item : list) {
            if (containers.contains(item)) {
                service.deleteQueue(item);
            }
        }
    }

    private static Set<String> listQueues(QueueContract service, String prefix) throws Exception {
        HashSet<String> result = new HashSet<String>();
        ListQueuesResult list = service.listQueues(new ListQueuesOptions().setPrefix(prefix));
        for (Queue item : list.getQueues()) {
            result.add(item.getName());
        }
        return result;
    }

    @Test
    public void getServicePropertiesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);

        // Don't run this test with emulator, as v1.6 doesn't support this method
        if (isRunningWithEmulator(config)) {
            return;
        }

        // Act
        ServiceProperties props = service.getServiceProperties().getValue();

        // Assert
        assertNotNull(props);
        assertNotNull(props.getLogging());
        assertNotNull(props.getLogging().getRetentionPolicy());
        assertNotNull(props.getLogging().getVersion());
        assertNotNull(props.getMetrics().getRetentionPolicy());
        assertNotNull(props.getMetrics().getVersion());
    }

    @Test
    public void setServicePropertiesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);

        // Don't run this test with emulator, as v1.6 doesn't support this method
        if (isRunningWithEmulator(config)) {
            return;
        }

        // Act
        ServiceProperties props = service.getServiceProperties().getValue();

        props.getLogging().setRead(true);
        service.setServiceProperties(props);

        props = service.getServiceProperties().getValue();

        // Assert
        assertNotNull(props);
        assertNotNull(props.getLogging());
        assertNotNull(props.getLogging().getRetentionPolicy());
        assertNotNull(props.getLogging().getVersion());
        assertTrue(props.getLogging().isRead());
        assertNotNull(props.getMetrics().getRetentionPolicy());
        assertNotNull(props.getMetrics().getVersion());
    }

    @Test
    public void createQueueWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);

        // Act
        service.createQueue(CREATABLE_QUEUE_1);
        GetQueueMetadataResult result = service.getQueueMetadata(CREATABLE_QUEUE_1);
        service.deleteQueue(CREATABLE_QUEUE_1);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getApproximateMessageCount());
        assertNotNull(result.getMetadata());
        assertEquals(0, result.getMetadata().size());
    }

    @Test
    public void createQueueWithOptionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);

        // Act
        service.createQueue(CREATABLE_QUEUE_2,
                new CreateQueueOptions().addMetadata("foo", "bar").addMetadata("test", "blah"));
        GetQueueMetadataResult result = service.getQueueMetadata(CREATABLE_QUEUE_2);
        service.deleteQueue(CREATABLE_QUEUE_2);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getApproximateMessageCount());
        assertNotNull(result.getMetadata());
        assertEquals(2, result.getMetadata().size());
        assertEquals("bar", result.getMetadata().get("foo"));
        assertEquals("blah", result.getMetadata().get("test"));
    }

    @Test
    public void listQueuesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);

        // Act
        ListQueuesResult result = service.listQueues();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getQueues());
        assertNotNull(result.getAccountName());
        assertNull(result.getMarker());
        assertEquals(0, result.getMaxResults());
        assertTrue(testQueues.length <= result.getQueues().size());
    }

    @Test
    public void listQueuesWithOptionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);

        // Act
        ListQueuesResult result = service.listQueues(new ListQueuesOptions().setMaxResults(3).setPrefix(
                testQueuesPrefix));
        ListQueuesResult result2 = service.listQueues(new ListQueuesOptions().setMarker(result.getNextMarker())
                .setPrefix(testQueuesPrefix).setIncludeMetadata(true));

        // Assert
        assertNotNull(result);
        assertNotNull(result.getQueues());
        assertEquals(3, result.getQueues().size());
        assertEquals(3, result.getMaxResults());
        assertNotNull(result.getAccountName());
        assertNull(result.getMarker());
        assertNotNull(result.getQueues().get(0));
        assertNotNull(result.getQueues().get(0).getMetadata());
        assertNotNull(result.getQueues().get(0).getName());
        assertNotNull(result.getQueues().get(0).getUrl());

        assertNotNull(result2);
        assertNotNull(result2.getQueues());
        assertTrue(testQueues.length - 3 <= result2.getQueues().size());
        assertEquals(0, result2.getMaxResults());
        assertNotNull(result2.getAccountName());
        assertEquals(result.getNextMarker(), result2.getMarker());
        assertNotNull(result2.getQueues().get(0));
        assertNotNull(result2.getQueues().get(0).getMetadata());
        assertNotNull(result2.getQueues().get(0).getName());
        assertNotNull(result2.getQueues().get(0).getUrl());
    }

    @Test
    public void setQueueMetadataWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);

        // Act
        service.createQueue(CREATABLE_QUEUE_3);

        HashMap<String, String> metadata = new HashMap<String, String>();
        metadata.put("foo", "bar");
        metadata.put("test", "blah");
        service.setQueueMetadata(CREATABLE_QUEUE_3, metadata);

        GetQueueMetadataResult result = service.getQueueMetadata(CREATABLE_QUEUE_3);

        service.deleteQueue(CREATABLE_QUEUE_3);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getApproximateMessageCount());
        assertNotNull(result.getMetadata());
        assertEquals(2, result.getMetadata().size());
        assertEquals("bar", result.getMetadata().get("foo"));
        assertEquals("blah", result.getMetadata().get("test"));
    }

    @Test
    public void createMessageWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);

        // Act
        service.createMessage(TEST_QUEUE_FOR_MESSAGES, "message1");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES, "message2");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES, "message3");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES, "message4");

        // Assert
    }

    @Test
    public void createNullMessageException() throws Exception {
        // Arrange 
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);

        // Act
        expectedException.expect(NullPointerException.class);
        service.createMessage(TEST_QUEUE_FOR_MESSAGES, null);
    }

    @Test
    public void listMessagesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2010, 01, 01);
        Date year2010 = calendar.getTime();

        // Act
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_2, "message1");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_2, "message2");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_2, "message3");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_2, "message4");
        ListMessagesResult result = service.listMessages(TEST_QUEUE_FOR_MESSAGES_2);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getQueueMessages().size());

        QueueMessage entry = result.getQueueMessages().get(0);

        assertNotNull(entry.getMessageId());
        assertNotNull(entry.getMessageText());
        assertNotNull(entry.getPopReceipt());
        assertEquals(1, entry.getDequeueCount());

        assertNotNull(entry.getExpirationDate());
        assertTrue(year2010.before(entry.getExpirationDate()));

        assertNotNull(entry.getInsertionDate());
        assertTrue(year2010.before(entry.getInsertionDate()));

        assertNotNull(entry.getTimeNextVisible());
        assertTrue(year2010.before(entry.getTimeNextVisible()));
    }

    @Test
    public void listMessagesWithOptionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2010, 01, 01);
        Date year2010 = calendar.getTime();

        // Act
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_3, "message1");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_3, "message2");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_3, "message3");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_3, "message4");
        ListMessagesResult result = service.listMessages(TEST_QUEUE_FOR_MESSAGES_3, new ListMessagesOptions()
                .setNumberOfMessages(4).setVisibilityTimeoutInSeconds(20));

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getQueueMessages().size());
        for (int i = 0; i < 4; i++) {
            QueueMessage entry = result.getQueueMessages().get(i);

            assertNotNull(entry.getMessageId());
            assertNotNull(entry.getMessageText());
            assertNotNull(entry.getPopReceipt());
            assertEquals(1, entry.getDequeueCount());

            assertNotNull(entry.getExpirationDate());
            assertTrue(year2010.before(entry.getExpirationDate()));

            assertNotNull(entry.getInsertionDate());
            assertTrue(year2010.before(entry.getInsertionDate()));

            assertNotNull(entry.getTimeNextVisible());
            assertTrue(year2010.before(entry.getTimeNextVisible()));
        }
    }

    @Test
    public void peekMessagesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2010, 01, 01);
        Date year2010 = calendar.getTime();

        // Act
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_4, "message1");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_4, "message2");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_4, "message3");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_4, "message4");
        PeekMessagesResult result = service.peekMessages(TEST_QUEUE_FOR_MESSAGES_4);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getQueueMessages().size());

        com.microsoft.windowsazure.services.queue.models.PeekMessagesResult.QueueMessage entry = result
                .getQueueMessages().get(0);

        assertNotNull(entry.getMessageId());
        assertNotNull(entry.getMessageText());
        assertEquals(0, entry.getDequeueCount());

        assertNotNull(entry.getExpirationDate());
        assertTrue(year2010.before(entry.getExpirationDate()));

        assertNotNull(entry.getInsertionDate());
        assertTrue(year2010.before(entry.getInsertionDate()));
    }

    @Test
    public void peekMessagesWithOptionsWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2010, 01, 01);
        Date year2010 = calendar.getTime();

        // Act
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_5, "message1");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_5, "message2");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_5, "message3");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_5, "message4");
        PeekMessagesResult result = service.peekMessages(TEST_QUEUE_FOR_MESSAGES_5,
                new PeekMessagesOptions().setNumberOfMessages(4));

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getQueueMessages().size());
        for (int i = 0; i < 4; i++) {
            com.microsoft.windowsazure.services.queue.models.PeekMessagesResult.QueueMessage entry = result
                    .getQueueMessages().get(i);

            assertNotNull(entry.getMessageId());
            assertNotNull(entry.getMessageText());
            assertEquals(0, entry.getDequeueCount());

            assertNotNull(entry.getExpirationDate());
            assertTrue(year2010.before(entry.getExpirationDate()));

            assertNotNull(entry.getInsertionDate());
            assertTrue(year2010.before(entry.getInsertionDate()));
        }
    }

    @Test
    public void clearMessagesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);

        // Act
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_6, "message1");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_6, "message2");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_6, "message3");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_6, "message4");
        service.clearMessages(TEST_QUEUE_FOR_MESSAGES_6);

        PeekMessagesResult result = service.peekMessages(TEST_QUEUE_FOR_MESSAGES_6);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getQueueMessages().size());
    }

    @Test
    public void deleteMessageWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);

        // Act
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_7, "message1");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_7, "message2");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_7, "message3");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_7, "message4");

        ListMessagesResult result = service.listMessages(TEST_QUEUE_FOR_MESSAGES_7);
        service.deleteMessage(TEST_QUEUE_FOR_MESSAGES_7, result.getQueueMessages().get(0).getMessageId(), result
                .getQueueMessages().get(0).getPopReceipt());
        ListMessagesResult result2 = service.listMessages(TEST_QUEUE_FOR_MESSAGES_7,
                new ListMessagesOptions().setNumberOfMessages(32));

        // Assert
        assertNotNull(result2);
        assertEquals(3, result2.getQueueMessages().size());
    }

    @Test
    public void updateNullMessageException() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);
        String messageId = "messageId";

        String popReceipt = "popReceipt";
        String messageText = null;
        int visibilityTimeoutInSeconds = 10;

        // Act
        expectedException.expect(NullPointerException.class);
        service.updateMessage(TEST_QUEUE_FOR_MESSAGES_8, messageId, popReceipt, messageText, visibilityTimeoutInSeconds);

    }

    @Test
    public void updateMessageWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueContract service = QueueService.create(config);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2010, 01, 01);
        Date year2010 = calendar.getTime();

        // Act
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_8, "message1");

        ListMessagesResult listResult1 = service.listMessages(TEST_QUEUE_FOR_MESSAGES_8);
        UpdateMessageResult updateResult = service.updateMessage(TEST_QUEUE_FOR_MESSAGES_8, listResult1
                .getQueueMessages().get(0).getMessageId(), listResult1.getQueueMessages().get(0).getPopReceipt(),
                "new text", 0);
        ListMessagesResult listResult2 = service.listMessages(TEST_QUEUE_FOR_MESSAGES_8);

        // Assert
        assertNotNull(updateResult);
        assertNotNull(updateResult.getPopReceipt());
        assertNotNull(updateResult.getTimeNextVisible());
        assertTrue(year2010.before(updateResult.getTimeNextVisible()));

        assertNotNull(listResult2);
        QueueMessage entry = listResult2.getQueueMessages().get(0);

        assertEquals(listResult1.getQueueMessages().get(0).getMessageId(), entry.getMessageId());
        assertEquals("new text", entry.getMessageText());
        assertNotNull(entry.getPopReceipt());
        assertEquals(2, entry.getDequeueCount());

        assertNotNull(entry.getExpirationDate());
        assertTrue(year2010.before(entry.getExpirationDate()));

        assertNotNull(entry.getInsertionDate());
        assertTrue(year2010.before(entry.getInsertionDate()));

        assertNotNull(entry.getTimeNextVisible());
        assertTrue(year2010.before(entry.getTimeNextVisible()));

    }
}
