package com.azure.quantum.jobs;

import com.azure.quantum.jobs.models.JobDetails;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class JobsTest {

    @Test
    public void testQuantumJobClient() {
        QuantumJobClient client = new QuantumJobClient("<subId>",
            "<rg-name>", "<workspace-name>", "<region>");
        List<JobDetails> jobs = client.listAsync().collectList().block();
        assertTrue(jobs.size() > 0);
    }

}
