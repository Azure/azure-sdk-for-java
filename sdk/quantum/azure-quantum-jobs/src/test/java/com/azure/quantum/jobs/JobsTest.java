package com.azure.quantum.jobs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.quantum.jobs.models.JobDetails;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static com.azure.quantum.jobs.TestUtils.getBuilder;


public class JobsTest {

    @Test
    public void testQuantumJobClient() {

        JobsAsyncClient client = getBuilder().buildJobsAsyncClient();
        List<JobDetails> jobs = client.list().collectList().block();
        assertTrue(jobs.size() > 0);
    }

}
