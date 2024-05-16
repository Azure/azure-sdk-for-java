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

package com.microsoft.windowsazure.services.media.implementation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.models.Job;
import com.microsoft.windowsazure.services.media.models.Task;

public class MediaBatchOperationsTest {

    @Test
    public void createMediaBatchOperationSuccess() throws JAXBException,
            ParserConfigurationException {
        // Arrange
        URI serviceUri = UriBuilder.fromPath("http://www.contoso.com/media")
                .build();

        // Act
        MediaBatchOperations mediaBatchOperations = new MediaBatchOperations(
                serviceUri);

        // Assert
        assertNotNull(mediaBatchOperations);

    }

    @Test(expected = IllegalArgumentException.class)
    public void createMediaBatchOperationFailedWithNullUri()
            throws JAXBException, ParserConfigurationException {
        // Arrange
        URI serviceUri = null;

        // Act
        @SuppressWarnings("unused")
        MediaBatchOperations mediaBatchOperations = new MediaBatchOperations(
                serviceUri);

        // Assert
        assertTrue(false);

    }

    @Test
    public void addCreateJobOperationToMediaBatchOperationsSuccess()
            throws JAXBException, ParserConfigurationException {
        // Arrange
        URI serviceUri = UriBuilder.fromPath("http://www.contoso.com/media")
                .build();
        Job.CreateBatchOperation createJobOperation = new Job.CreateBatchOperation(
                serviceUri);

        // Act
        MediaBatchOperations mediaBatchOperations = new MediaBatchOperations(
                serviceUri);
        mediaBatchOperations.addOperation(createJobOperation);

        // Assert
        assertNotNull(mediaBatchOperations);
        assertEquals(1, mediaBatchOperations.getOperations().size());

    }

    @Test
    public void addCreateTaskOperationToMediaBatchOperationsSuccess()
            throws JAXBException, ParserConfigurationException {
        // Arrange
        URI serviceUri = UriBuilder.fromPath("http://www.contoso.com/media")
                .build();
        String mediaProcessorId = "testMediaProcessorId";
        String taskBody = "testTaskBody";

        Task.CreateBatchOperation taskCreateBatchOperation = new Task.CreateBatchOperation(
                mediaProcessorId, taskBody);

        // Act
        MediaBatchOperations mediaBatchOperations = new MediaBatchOperations(
                serviceUri);
        mediaBatchOperations.addOperation(taskCreateBatchOperation);

        // Assert
        assertNotNull(mediaBatchOperations);
        assertEquals(1, mediaBatchOperations.getOperations().size());
    }

    @Test
    public void getMimeMultipartSuccess() throws JAXBException,
            ParserConfigurationException, MessagingException, IOException {
        // Arrange
        String mediaProcessorId = "testMediaProcessorId";
        String taskBody = "testTaskBody";
        URI serviceUri = UriBuilder.fromPath("http://www.contoso.com/media")
                .build();
        Task.CreateBatchOperation taskCreateBatchOperation = new Task.CreateBatchOperation(
                mediaProcessorId, taskBody);
        Job.CreateBatchOperation jobCreateBatchOperation = new Job.CreateBatchOperation(
                serviceUri);

        // Act
        MediaBatchOperations mediaBatchOperations = new MediaBatchOperations(
                serviceUri);
        mediaBatchOperations.addOperation(jobCreateBatchOperation);
        mediaBatchOperations.addOperation(taskCreateBatchOperation);
        MimeMultipart mimeMultipart = mediaBatchOperations.getMimeMultipart();

        // Assert
        assertNotNull(mediaBatchOperations);
        assertEquals(2, mediaBatchOperations.getOperations().size());
        assertNotNull(mimeMultipart);
    }

}
