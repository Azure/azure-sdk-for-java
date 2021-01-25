package com.azure.quantum.jobs;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.quantum.jobs.models.JobDetails;
import com.azure.identity.DefaultAzureCredentialBuilder;
import jdk.nashorn.internal.parser.Token;
import com.azure.core.management.AzureEnvironment;

import java.util.ArrayList;
import java.util.List;

public class QuantumJobClient {

    private JobsAsyncClient jobsAsyncClient;

    public QuantumJobClient(String subscriptionId,
                            String resourceGroupName,
                            String workspaceName,
                            String location) {

        AzureEnvironment env = AzureEnvironment.AZURE;
        AzureProfile profile = new AzureProfile(env);
        TokenCredential tokenCredential = new DefaultAzureCredentialBuilder()
            .authorityHost(profile.getEnvironment().getActiveDirectoryEndpoint())
            .build();

//        TokenCredential tokenCredential = new AzureCliCredentialBuilder()
//            .build();

        List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new BearerTokenAuthenticationPolicy(tokenCredential, "https://quantum.microsoft.com"));

        HttpPipeline httpPipeline =
            new HttpPipelineBuilder()
                //.httpClient(httpClient) //potentially add this for playback?
                .policies(policies.toArray(new HttpPipelinePolicy[0]))
                .build();

        QuantumClientBuilder clientBuilder = new QuantumClientBuilder()
            .subscriptionId(subscriptionId)
            .resourceGroupName(resourceGroupName)
            .workspaceName(workspaceName)
            .host(String.format("https://%s.quantum.azure.com", location))
            .pipeline(httpPipeline)
            .environment(env);
        jobsAsyncClient = clientBuilder.buildJobsAsyncClient();

    }

    public PagedFlux<JobDetails> listAsync() {
        return jobsAsyncClient.list();
    }
}
