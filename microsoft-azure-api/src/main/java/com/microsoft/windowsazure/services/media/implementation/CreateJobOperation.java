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
import com.microsoft.windowsazure.services.media.models.JobInfo;

public class CreateJobOperation extends Operation {

    private final URI assetServiceURI;
    private JobInfo jobInfo;

    public CreateJobOperation(URI assetServiceURI) {
        this.verb = "POST";
        this.assetServiceURI = assetServiceURI;
    }

    public CreateJobOperation setJob(List<String> inputMediaAssets, List<String> outputMediaAssets, JobType jobType) {
        for (String inputMediaAsset : inputMediaAssets) {
            addLink("InputMediaAssets",
                    String.format("%s/Assets('%s')", assetServiceURI.toString(), inputMediaAsset.toString()),
                    "application/atom+xml;type=feed",
                    "http://schemas.microsoft.com/ado/2007/08/dataservices/related/InputMediaAssets");
        }

        for (String outputMediaAsset : outputMediaAssets) {
            addLink("OutputMediaAssets",
                    String.format("%s/Assets('%s'", assetServiceURI.toString(), outputMediaAsset.toString()),
                    "application/atom+xml;type=feed",
                    "http://schemas.microsoft.com/ado/2007/08/dataservices/related/InputMediaAssets");
        }
        addContentObject(jobType);
        return this;
    }

    public void setJobInfo(JobInfo jobInfo) {
        this.jobInfo = jobInfo;
    }

    public JobInfo getJobInfo() {
        return this.jobInfo;
    }

}
