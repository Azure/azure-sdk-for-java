/**
 * Copyright 2012 Microsoft Corporation
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

import static org.junit.Assert.*;

import java.net.URLEncoder;

import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.implementation.atom.ContentType;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.atom.LinkType;
import com.microsoft.windowsazure.services.media.implementation.content.Constants;
import com.microsoft.windowsazure.services.media.implementation.content.JobType;
import com.microsoft.windowsazure.services.media.implementation.content.TaskType;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityGetOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityListOperation;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityUpdateOperation;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Tests for the methods and factories of the Task entity.
 */
public class TaskEntityTest {
    static final String sampleTaskId = "nb:cid:UUID:1151b8bd-9ada-4e7f-9787-8dfa49968eab";
    private final String expectedUri = String.format("Tasks('%s')", URLEncoder.encode(sampleTaskId, "UTF-8"));

    public TaskEntityTest() throws Exception {
    }

    @Test
    public void TaskCreateReturnsDefaultCreatePayload() {
        String expectedMediaProcessorId = "expectedMediaProcessorId";
        String expectedTaskBody = "expectedTaskBody";
        TaskType payload = Task.create(expectedMediaProcessorId, expectedTaskBody);

        assertNotNull(payload);
        assertNull(payload.getId());
        assertNull(payload.getState());
        assertNull(payload.getCreated());
        assertNull(payload.getLastModified());
        assertNull(payload.getAlternateId());
        assertNull(payload.getName());
        assertNull(payload.getOptions());
    }

    @Test
    public void TaskCreateCanSetTaskName() {
        String name = "TaskCreateCanSetTaskName";

        Task.Creator creator = Task.create().setName("TaskCreateCanSetTaskName");

        TaskType payload = (TaskType) creator.getRequestContents();

        assertNotNull(payload);
        assertNull(payload.getId());
        assertNull(payload.getState());
        assertNull(payload.getCreated());
        assertNull(payload.getLastModified());
        assertNull(payload.getAlternateId());
        assertEquals(name, payload.getName());
        assertNull(payload.getOptions());
    }

    @Test
    public void TaskGetReturnsExpectedUri() throws Exception {
        String expectedUri = String.format("Tasks('%s')", URLEncoder.encode(sampleTaskId, "UTF-8"));

        EntityGetOperation<TaskInfo> getter = Task.get(sampleTaskId);

        assertEquals(expectedUri, getter.getUri());
    }

    @Test
    public void TaskListReturnsExpectedUri() {
        EntityListOperation<TaskInfo> lister = Task.list();

        assertEquals("Tasks", lister.getUri());
        assertNotNull(lister.getQueryParameters());
        assertEquals(0, lister.getQueryParameters().size());
    }

    @Test
    public void TaskListCanTakeQueryParameters() {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("$top", "10");
        queryParams.add("$skip", "2");

        EntityListOperation<TaskInfo> lister = Task.list(queryParams);

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals(2, lister.getQueryParameters().size());
    }

    @Test
    public void TaskListCanTakeQueryParametersChained() {
        EntityListOperation<TaskInfo> lister = Task.list().setTop(10).setSkip(2).set("filter", "something");

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals("something", lister.getQueryParameters().getFirst("filter"));
        assertEquals(3, lister.getQueryParameters().size());
    }

    @Test
    public void TaskUpdateReturnsExpectedUri() throws Exception {
        EntityUpdateOperation updater = Task.update(sampleTaskId);
        assertEquals(expectedUri, updater.getUri());
    }

    @Test
    public void TaskUpdateCanSetNameAndAltId() throws Exception {

        String expectedName = "newTaskName";
        String expectedAltId = "newAltId";

        EntityUpdateOperation updater = Task.update(sampleTaskId).setName(expectedName).setAlternateId(expectedAltId);

        TaskType payload = (TaskType) updater.getRequestContents();

        assertEquals(expectedName, payload.getName());
        assertEquals(expectedAltId, payload.getAlternateId());
    }

    @Test
    public void TaskDeleteReturnsExpectedUri() throws Exception {
        EntityDeleteOperation deleter = Task.delete(sampleTaskId);

        assertEquals(expectedUri, deleter.getUri());
    }

    private static final String expectedOutputTask = "Job(someJobId)/OutputTasks";
    private static final String expectedInputTask = "Job(someJobId)/InputTasks";

    @Test
    public void listForLinkReturnsExpectedUri() throws Exception {
        JobInfo fakeJob = createJob();

        EntityListOperation<TaskInfo> lister = Task.list(fakeJob.getInputTasksLink());

        assertEquals(lister.getUri(), expectedInputTask);
    }

    private JobInfo createJob() {
        EntryType fakeJobEntry = new EntryType();
        addEntryLink(fakeJobEntry, Constants.ODATA_DATA_NS + "/related/OutputMediaTasks", expectedOutputTask,
                "application/atom+xml;type=feed", "OutputTasks");
        addEntryLink(fakeJobEntry, Constants.ODATA_DATA_NS + "/related/InputMediaTasks", expectedInputTask,
                "application/atom+xml;type=feed", "InputTasks");

        JobType payload = new JobType().setId("SomeId").setName("FakeJob");
        addEntryContent(fakeJobEntry, payload);

        return new JobInfo(fakeJobEntry, payload);
    }

    private void addEntryLink(EntryType entry, String rel, String href, String type, String title) {
        LinkType link = new LinkType();
        link.setRel(rel);
        link.setHref(href);
        link.setType(type);
        link.setTitle(title);

        JAXBElement<LinkType> linkElement = new JAXBElement<LinkType>(new QName("link", Constants.ATOM_NS),
                LinkType.class, link);
        entry.getEntryChildren().add(linkElement);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private ContentType addEntryContent(EntryType entry, Object content) {
        ContentType contentWrapper = new ContentType();
        contentWrapper.getContent().add(
                new JAXBElement(Constants.ODATA_PROPERTIES_ELEMENT_NAME, content.getClass(), content));

        entry.getEntryChildren().add(
                new JAXBElement<ContentType>(Constants.ATOM_CONTENT_ELEMENT_NAME, ContentType.class, contentWrapper));
        return contentWrapper;
    }
}
