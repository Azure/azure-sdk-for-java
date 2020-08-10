// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.CookiePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.http.policy.UserAgentPolicy;

public class DigitalTwinsClient
{
    private AzureDigitalTwinsAPI _digitalTwinsApi;

    public DigitalTwinsClient(String host, TokenCredential tokenCredential)
    {
        AzureDigitalTwinsAPIBuilder builder = new AzureDigitalTwinsAPIBuilder();
        builder.host(host);
        HttpPipeline pipeline =
            new HttpPipelineBuilder()
                .policies(
                    new UserAgentPolicy(),
                    new RetryPolicy(),
                    new CookiePolicy(),
                    new BearerTokenAuthenticationPolicy(tokenCredential, String.format("%s/.default", "https://digitaltwins.azure.net"))                    )
                .build();

        // TODO: Implement credential scope for digital twins and make a method call to get it.

        builder.pipeline(pipeline);

        this._digitalTwinsApi = builder.buildClient();
    }
}
