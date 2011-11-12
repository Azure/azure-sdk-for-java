package com.microsoft.windowsazure.services.queue;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.ServiceException;
import com.microsoft.windowsazure.configuration.Configuration;
import com.microsoft.windowsazure.services.queue.models.CreateQueueOptions;
import com.microsoft.windowsazure.services.queue.models.GetQueueMetadataResult;
import com.microsoft.windowsazure.services.queue.models.ServiceProperties;

public class QueueServiceIntegrationTest extends IntegrationTestBase {
    //private static final String testContainersPrefix = "sdktest-";
    private static final String createableQueuesPrefix = "csdktest-";
    private static final String CREATABLE_QUEUE_1 = createableQueuesPrefix + "1";
    private static final String CREATABLE_QUEUE_2 = createableQueuesPrefix + "2";

    @BeforeClass
    public static void setup() throws Exception {
    }

    @AfterClass
    public static void cleanup() throws Exception {
        Configuration config = createConfiguration();
        QueueServiceContract service = config.create(QueueServiceContract.class);

        try {
            service.deleteQueue(CREATABLE_QUEUE_1);
        }
        catch (ServiceException e) {
            // Queue might not exist
        }
        try {
            service.deleteQueue(CREATABLE_QUEUE_2);
        }
        catch (ServiceException e) {
            // Queue might not exist
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
}
