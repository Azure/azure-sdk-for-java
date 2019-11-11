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

import java.net.URI;

import javax.mail.internet.MimeMultipart;

import org.junit.Test;

import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.entityoperations.EntityListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityProxyData;

/**
 * Tests for the methods and factories of the Job entity.
 */
public class JobEntityTest {

    private EntityProxyData createProxyData() {
        return new EntityProxyData() {
            @Override
            public URI getServiceUri() {
                return URI.create("http://contoso.com");
            }
        };
    }

    public JobEntityTest() throws Exception {
    }

    @Test
    public void JobCreateReturnsDefaultCreatePayload() throws ServiceException {
        Job.Creator jobCreator = Job.create();
        jobCreator.setProxyData(createProxyData());
        MimeMultipart payload = (MimeMultipart) jobCreator.getRequestContents();
        assertNotNull(payload);
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
        EntityListOperation<JobInfo> lister = Job.list().setTop(10).setSkip(2);

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals(2, lister.getQueryParameters().size());
    }

    @Test
    public void JobListCanTakeQueryParametersChained() {
        EntityListOperation<JobInfo> lister = Job.list().setTop(10).setSkip(2)
                .set("filter", "something");

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals("something", lister.getQueryParameters()
                .getFirst("filter"));
        assertEquals(3, lister.getQueryParameters().size());
    }

}
