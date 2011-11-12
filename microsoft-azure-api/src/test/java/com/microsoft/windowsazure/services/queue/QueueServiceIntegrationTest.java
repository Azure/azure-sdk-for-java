package com.microsoft.windowsazure.services.queue;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.windowsazure.configuration.Configuration;
import com.microsoft.windowsazure.services.queue.models.QueueServiceProperties;

public class QueueServiceIntegrationTest extends IntegrationTestBase {

    @BeforeClass
    public static void setup() throws Exception {
    }

    @AfterClass
    public static void cleanup() throws Exception {
    }

    @Test
    public void getServiceProppertiesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueServiceContract service = config.create(QueueServiceContract.class);

        // Act
        QueueServiceProperties props = service.getServiceProperties();

        // Assert
        assertNotNull(props);
        assertNotNull(props.getLogging());
        assertNotNull(props.getLogging().getRetentionPolicy());
        assertNotNull(props.getLogging().getVersion());
        assertNotNull(props.getMetrics().getRetentionPolicy());
        assertNotNull(props.getMetrics().getVersion());
    }

    @Test
    public void setServiceProppertiesWorks() throws Exception {
        // Arrange
        Configuration config = createConfiguration();
        QueueServiceContract service = config.create(QueueServiceContract.class);

        // Act
        QueueServiceProperties props = service.getServiceProperties();

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
}
