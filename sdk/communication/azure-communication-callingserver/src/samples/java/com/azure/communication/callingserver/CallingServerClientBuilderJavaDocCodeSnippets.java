// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;

public class CallingServerClientBuilderJavaDocCodeSnippets {
    public void createCallingServerClientWithPipeline() {

        String connectionString = getConnectionString();
        // BEGIN: com.azure.communication.callingserver.CallingServerClientBuilder.pipeline.instantiation
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(/* add policies */)
            .build();

        CallingServerClient callingServerClient = new CallingServerClientBuilder()
            .pipeline(pipeline)
            .connectionString(connectionString)
            .buildClient();

        CallingServerAsyncClient callingServerAsyncClient = new CallingServerClientBuilder()
            .pipeline(pipeline)
            .connectionString(connectionString)
            .buildAsyncClient();
        // END: com.azure.communication.callingserver.CallingServerClientBuilder.pipeline.instantiation

    }

    private String getConnectionString() {
        return "endpoint=https://<resource-name>.communication.azure.com/;accesskey=<access-key>";
    }

}
