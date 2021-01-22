package com.azure.quantum;

import com.azure.quantum.models.JobDetails;
import com.azure.core.http.rest.Response;
import org.junit.jupiter.api.Test;
import java.util.List;

import static com.azure.quantum.TestUtils.getClient;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class JobsTest {

    @Test
    public void listAsyncTest() {
        Jobs jobs = new Jobs(getClient());
        List<JobDetails> results = jobs.listAsync().collectSortedList().block();

        assertEquals(0, results.size());
        assertEquals("id", results.get(0).getId());
    }

    @Test
    public void createWithResponseAsyncTest() {
        Jobs jobs = new Jobs(getClient());
        JobDetails details = new JobDetails();
        details.setId("id").setContainerUri("containerURI").setName("name");
        Response<JobDetails> response = jobs.createWithResponseAsync("jobId", details).block();

        assertEquals(200, response.getStatusCode());
        assertEquals(details, response.getValue());
    }

    @Test
    public void getWithResponseAsyncTest() {
        Jobs jobs = new Jobs(getClient());
        Response<JobDetails> response = jobs.getWithResponseAsync("jobId").block();

        assertEquals(200, response.getStatusCode());
        assertEquals("name", response.getValue().getName());
    }

    @Test
    public void cancelWithResponseAsyncTest() {
        Jobs jobs = new Jobs(getClient());
        Response<Void> response = jobs.cancelWithResponseAsync("jobId").block();

        assertEquals(200, response.getStatusCode());
    }
}
