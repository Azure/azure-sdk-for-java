package com.microsoft.windowsazure.services.queue;

import static org.junit.Assert.*;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.ServiceException;
import com.microsoft.windowsazure.configuration.Configuration;
import com.microsoft.windowsazure.services.queue.models.CreateQueueOptions;
import com.microsoft.windowsazure.services.queue.models.GetQueueMetadataResult;
import com.microsoft.windowsazure.services.queue.models.ListMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.ListMessagesResult;
import com.microsoft.windowsazure.services.queue.models.ListMessagesResult.Entry;
import com.microsoft.windowsazure.services.queue.models.ListQueuesOptions;
import com.microsoft.windowsazure.services.queue.models.ListQueuesResult;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesOptions;
import com.microsoft.windowsazure.services.queue.models.PeekMessagesResult;
import com.microsoft.windowsazure.services.queue.models.ServiceProperties;

public class QueueServiceIntegrationTest extends IntegrationTestBase {
    private static final String testQueuesPrefix = "sdktest-";
    private static final String createableQueuesPrefix = "csdktest-";
    private static String TEST_QUEUE_FOR_MESSAGES;
    private static String TEST_QUEUE_FOR_MESSAGES_2;
    private static String TEST_QUEUE_FOR_MESSAGES_3;
    private static String TEST_QUEUE_FOR_MESSAGES_4;
    private static String TEST_QUEUE_FOR_MESSAGES_5;
    private static String CREATABLE_QUEUE_1;
    private static String CREATABLE_QUEUE_2;
    private static String CREATABLE_QUEUE_3;
    private static String[] creatableQueues;
    private static String[] testQueues;

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

        CREATABLE_QUEUE_1 = creatableQueues[0];
        CREATABLE_QUEUE_2 = creatableQueues[1];
        CREATABLE_QUEUE_3 = creatableQueues[2];

        // Create all test containers and their content
        Configuration config = createConfiguration();
        QueueServiceContract service = config.create(QueueServiceContract.class);
        for (int i = 0; i < testQueues.length; i++) {
            try {
                service.createQueue(testQueues[i]);
            }
            catch (ServiceException e) {
                // Ignore exception as the containers might not exists
            }
        }
    }

    @AfterClass
    public static void cleanup() throws Exception {
        Configuration config = createConfiguration();
        QueueServiceContract service = config.create(QueueServiceContract.class);

        for (int i = 0; i < testQueues.length; i++) {
            try {
                service.deleteQueue(testQueues[i]);
            }
            catch (ServiceException e) {
                // Ignore exception as the containers might not exists
            }
        }

        for (int i = 0; i < creatableQueues.length; i++) {
            try {
                service.deleteQueue(creatableQueues[i]);
            }
            catch (ServiceException e) {
                // Ignore exception as the containers might not exists
            }
        }
    }

    @Test
    public void getServicePropertiesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueServiceContract service = config.create(QueueServiceContract.class);

        // Act
        ServiceProperties props = service.getServiceProperties();

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
        QueueServiceContract service = config.create(QueueServiceContract.class);

        // Act
        ServiceProperties props = service.getServiceProperties();

        props.getLogging().setRead(true);
        service.setServiceProperties(props);

        props = service.getServiceProperties();

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
        QueueServiceContract service = config.create(QueueServiceContract.class);

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
        QueueServiceContract service = config.create(QueueServiceContract.class);

        // Act
        service.createQueue(CREATABLE_QUEUE_2, new CreateQueueOptions().addMetadata("foo", "bar").addMetadata("test", "blah"));
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
        QueueServiceContract service = config.create(QueueServiceContract.class);

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
        QueueServiceContract service = config.create(QueueServiceContract.class);

        // Act
        ListQueuesResult result = service.listQueues(new ListQueuesOptions().setMaxResults(3).setPrefix(testQueuesPrefix));
        ListQueuesResult result2 = service.listQueues(new ListQueuesOptions().setMarker(result.getNextMarker()).setPrefix(testQueuesPrefix)
                .setIncludeMetadata(true));

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
        QueueServiceContract service = config.create(QueueServiceContract.class);

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
        QueueServiceContract service = config.create(QueueServiceContract.class);

        // Act
        service.createMessage(TEST_QUEUE_FOR_MESSAGES, "message1");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES, "message2");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES, "message3");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES, "message4");

        // Assert
    }

    @Test
    public void listMessagesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueServiceContract service = config.create(QueueServiceContract.class);
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
        assertEquals(1, result.getEntries().size());

        Entry entry = result.getEntries().get(0);

        assertNotNull(entry.getId());
        assertNotNull(entry.getText());
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
        QueueServiceContract service = config.create(QueueServiceContract.class);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2010, 01, 01);
        Date year2010 = calendar.getTime();

        // Act
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_3, "message1");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_3, "message2");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_3, "message3");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_3, "message4");
        ListMessagesResult result = service.listMessages(TEST_QUEUE_FOR_MESSAGES_3, new ListMessagesOptions().setNumberOfMessages(4)
                .setVisibilityTimeoutInSeconds(20));

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getEntries().size());
        for (int i = 0; i < 4; i++) {
            Entry entry = result.getEntries().get(i);

            assertNotNull(entry.getId());
            assertNotNull(entry.getText());
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
        QueueServiceContract service = config.create(QueueServiceContract.class);
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
        assertEquals(1, result.getEntries().size());

        com.microsoft.windowsazure.services.queue.models.PeekMessagesResult.Entry entry = result.getEntries().get(0);

        assertNotNull(entry.getId());
        assertNotNull(entry.getText());
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
        QueueServiceContract service = config.create(QueueServiceContract.class);
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(2010, 01, 01);
        Date year2010 = calendar.getTime();

        // Act
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_5, "message1");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_5, "message2");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_5, "message3");
        service.createMessage(TEST_QUEUE_FOR_MESSAGES_5, "message4");
        PeekMessagesResult result = service.peekMessages(TEST_QUEUE_FOR_MESSAGES_5, new PeekMessagesOptions().setNumberOfMessages(4));

        // Assert
        assertNotNull(result);
        assertEquals(4, result.getEntries().size());
        for (int i = 0; i < 4; i++) {
            com.microsoft.windowsazure.services.queue.models.PeekMessagesResult.Entry entry = result.getEntries().get(i);

            assertNotNull(entry.getId());
            assertNotNull(entry.getText());
            assertEquals(0, entry.getDequeueCount());

            assertNotNull(entry.getExpirationDate());
            assertTrue(year2010.before(entry.getExpirationDate()));

            assertNotNull(entry.getInsertionDate());
            assertTrue(year2010.before(entry.getInsertionDate()));
        }
    }
}
