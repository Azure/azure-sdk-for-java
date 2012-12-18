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

import java.io.IOException;
import java.io.InputStream;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMultipart;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.implementation.ODataAtomUnmarshaller;
import com.microsoft.windowsazure.services.media.implementation.entities.EntityListOperation;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Tests for the methods and factories of the Job entity.
 */
public class JobEntityTest {

    private JobInfo getJobInfo(MimeMultipart payload) throws MessagingException, IOException, JAXBException,
            ServiceException {
        for (int i = 0; i < payload.getCount(); i++) {
            BodyPart bodyPart = payload.getBodyPart(i);
            JobInfo jobInfo = parseBodyPart(bodyPart);
            if (jobInfo != null) {
                return jobInfo;
            }
        }

        return null;
    }

    private JobInfo parseBodyPart(BodyPart bodyPart) throws IOException, MessagingException, JAXBException,
            ServiceException {
        JobInfo jobInfo = null;
        ODataAtomUnmarshaller oDataAtomUnmarshaller = new ODataAtomUnmarshaller();
        InputStream inputStream = bodyPart.getInputStream();
        jobInfo = oDataAtomUnmarshaller.unmarshalEntry(inputStream, JobInfo.class);
        return jobInfo;
    }

    public JobEntityTest() throws Exception {
    }

    @Test
    public void JobCreateReturnsDefaultCreatePayload() throws ServiceException {
        MimeMultipart payload = (MimeMultipart) Job.create().getRequestContents();
        assertNotNull(payload);
    }

    @Test
    public void JobCreateCanSetJobName() throws ServiceException, MessagingException, IOException, JAXBException {
        String expectedName = "JobCreateCanSetJobName";

        Job.Creator creator = Job.create().setName(expectedName);

        MimeMultipart payload = (MimeMultipart) creator.getRequestContents();

        JobInfo jobInfo = getJobInfo(payload);

        assertEquals(expectedName, jobInfo.getName());
    }

    @Test
    public void JobListReturnsExpectedUri() {
        EntityListOperation<JobInfo> lister = Job.list();

        assertEquals("Jobs", lister.getUri());
        assertNotNull(lister.getQueryParameters());
        assertEquals(0, lister.getQueryParameters().size());
    }

    @Test
    public void JobListCanTakeQueryParameters() {
        MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
        queryParams.add("$top", "10");
        queryParams.add("$skip", "2");

        EntityListOperation<JobInfo> lister = Job.list(queryParams);

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals(2, lister.getQueryParameters().size());
    }

    @Test
    public void JobListCanTakeQueryParametersChained() {
        EntityListOperation<JobInfo> lister = Job.list().setTop(10).setSkip(2).set("filter", "something");

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals("something", lister.getQueryParameters().getFirst("filter"));
        assertEquals(3, lister.getQueryParameters().size());
    }

}
