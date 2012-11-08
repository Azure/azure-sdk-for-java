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

package com.microsoft.windowsazure.services.media.implementation;

import java.net.URI;
import java.util.List;

import com.microsoft.windowsazure.services.media.implementation.content.JobType;

public class CreateJobOperation extends Operation {

    private final URI serviceURI;

    public CreateJobOperation(URI serviceURI) {
        this.verb = "POST";
        this.serviceURI = serviceURI;
    }

    public CreateJobOperation setJob(List<String> inputMediaAssets, List<String> outputMediaAssets, JobType jobType) {
        for (String inputMediaAsset : inputMediaAssets) {
            addLink("InputMediaAssets",
                    String.format("%s/Assets('%s')", serviceURI.toString(), inputMediaAsset.toString()),
                    "application/atom+xml;type=feed",
                    "http://schemas.microsoft.com/ado/2007/08/dataservices/related/InputMediaAssets");
        }

        for (String outputMediaAsset : outputMediaAssets) {
            addLink("OutputMediaAssets",
                    String.format("%s/Assets('%s'", serviceURI.toString(), outputMediaAsset.toString()),
                    "application/atom+xml;type=feed",
                    "http://schemas.microsoft.com/ado/2007/08/dataservices/related/InputMediaAssets");
        }
        addContentObject(jobType);
        return this;
    }

}
