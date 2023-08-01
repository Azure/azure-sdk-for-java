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

package com.microsoft.windowsazure.services.media.models;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.xml.bind.JAXBElement;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.atom.ContentType;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.TaskType;

/**
 * Tests for the methods and factories of the Task entity.
 */
public class TaskEntityTest {
    static final String sampleTaskId = "nb:cid:UUID:1151b8bd-9ada-4e7f-9787-8dfa49968eab";

    private TaskType getTaskType(EntryType entryType) {
        for (Object child : entryType.getEntryChildren()) {
            if (child instanceof JAXBElement) {
                @SuppressWarnings("rawtypes")
                JAXBElement element = (JAXBElement) child;
                if (element.getDeclaredType() == ContentType.class) {
                    ContentType contentType = (ContentType) element.getValue();
                    for (Object grandChild : contentType.getContent()) {
                        if (grandChild instanceof JAXBElement) {
                            @SuppressWarnings("rawtypes")
                            JAXBElement contentElement = (JAXBElement) grandChild;
                            TaskType taskType = (TaskType) contentElement
                                    .getValue();
                            return taskType;
                        }
                    }
                    return null;
                }
            }
        }
        return null;
    }

    public TaskEntityTest() throws Exception {
    }

    @Test
    public void taskCreateReturnsDefaultCreatePayload() {
        String expectedMediaProcessorId = "expectedMediaProcessorId";
        String expectedTaskBody = "expectedTaskBody";

        TaskType taskType = getTaskType(Task.create(expectedMediaProcessorId,
                expectedTaskBody).getEntryType());

        assertNotNull(taskType);
        assertEquals(expectedMediaProcessorId, taskType.getMediaProcessorId());
        assertEquals(expectedTaskBody, taskType.getTaskBody());
    }

    @Test
    public void taskCreateCanSetTaskName() {
        String expectedName = "TaskCreateCanSetTaskName";

        String expectedMediaProcessorId = "expectedMediaProcessorId";
        String expectedTaskBody = "expectedTaskBody";

        TaskType taskType = getTaskType(Task
                .create(expectedMediaProcessorId, expectedTaskBody)
                .setName(expectedName).getEntryType());

        assertNotNull(taskType);
        assertEquals(expectedName, taskType.getName());
    }

    @Test
    public void taskCreateCanSetConfiguration() {
        String expectedConfiguration = "TaskCreateCanSetTaskCofniguration";

        String expectedMediaProcessorId = "expectedMediaProcessorId";
        String expectedTaskBody = "expectedTaskBody";

        TaskType taskType = getTaskType(Task
                .create(expectedMediaProcessorId, expectedTaskBody)
                .setConfiguration(expectedConfiguration).getEntryType());

        assertNotNull(taskType);
        assertEquals(expectedConfiguration, taskType.getConfiguration());
    }

    @Test
    public void taskCreateCanSetPriority() {
        Integer expectedPriority = 3;

        String expectedMediaProcessorId = "expectedMediaProcessorId";
        String expectedTaskBody = "expectedTaskBody";

        TaskType taskType = getTaskType(Task
                .create(expectedMediaProcessorId, expectedTaskBody)
                .setPriority(expectedPriority).getEntryType());

        assertNotNull(taskType);
        assertEquals(expectedPriority, taskType.getPriority());
    }

    @Test
    public void taskCreateCanSetTaskBody() {
        String expectedTaskBodyResult = "expectedTaskBodyResult";

        String expectedMediaProcessorId = "expectedMediaProcessorId";
        String expectedTaskBody = "expectedTaskBody";

        TaskType taskType = getTaskType(Task
                .create(expectedMediaProcessorId, expectedTaskBody)
                .setTaskBody(expectedTaskBodyResult).getEntryType());

        assertNotNull(taskType);
        assertEquals(expectedTaskBodyResult, taskType.getTaskBody());
    }

    @Test
    public void taskCreateCanSetEncryptionKeyId() {
        String expectedEncryptionKeyId = "expectedEncryptionKeyId";

        String expectedMediaProcessorId = "expectedMediaProcessorId";
        String expectedTaskBody = "expectedTaskBody";

        TaskType taskType = getTaskType(Task
                .create(expectedMediaProcessorId, expectedTaskBody)
                .setEncryptionKeyId(expectedEncryptionKeyId).getEntryType());

        assertNotNull(taskType);
        assertEquals(expectedEncryptionKeyId, taskType.getEncryptionKeyId());
    }

    @Test
    public void taskCreateCanSetEncryptionScheme() {
        String expectedEncryptionScheme = "expectedEncryptionScheme";

        String expectedMediaProcessorId = "expectedMediaProcessorId";
        String expectedTaskBody = "expectedTaskBody";

        TaskType taskType = getTaskType(Task
                .create(expectedMediaProcessorId, expectedTaskBody)
                .setEncryptionScheme(expectedEncryptionScheme).getEntryType());

        assertNotNull(taskType);
        assertEquals(expectedEncryptionScheme, taskType.getEncryptionScheme());
    }

    @Test
    public void taskCreateCanSetEncryptionVersion() {
        String expectedEncryptionVersion = "expectedEncryptionVersion";

        String expectedMediaProcessorId = "expectedMediaProcessorId";
        String expectedTaskBody = "expectedTaskBody";

        TaskType taskType = getTaskType(Task
                .create(expectedMediaProcessorId, expectedTaskBody)
                .setEncryptionVersion(expectedEncryptionVersion).getEntryType());

        assertNotNull(taskType);
        assertEquals(expectedEncryptionVersion, taskType.getEncryptionVersion());
    }

    @Test
    public void taskCreateCanSetInitializationVector() {
        String expectedInitializationVector = "expectedEncryptionKeyId";

        String expectedMediaProcessorId = "expectedMediaProcessorId";
        String expectedTaskBody = "expectedTaskBody";

        TaskType taskType = getTaskType(Task
                .create(expectedMediaProcessorId, expectedTaskBody)
                .setInitializationVector(expectedInitializationVector)
                .getEntryType());

        assertNotNull(taskType);
        assertEquals(expectedInitializationVector,
                taskType.getInitializationVector());
    }
}
