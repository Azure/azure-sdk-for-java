/**
 * Copyright 2011 Microsoft Corporation
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
package com.microsoft.windowsazure.services.core.storage.utils.pipeline;

import com.microsoft.windowsazure.services.core.storage.models.AccessCondition;
import com.microsoft.windowsazure.services.core.storage.models.AccessConditionHeaderType;
import com.microsoft.windowsazure.services.core.utils.pipeline.PipelineHelpers;
import com.sun.jersey.api.client.WebResource.Builder;

public class StoragePipelineHelpers extends PipelineHelpers {
    public static Builder addOptionalAccessContitionHeader(Builder builder, AccessCondition accessCondition) {
        if (accessCondition != null) {
            if (accessCondition.getHeader() != AccessConditionHeaderType.NONE) {
                builder = addOptionalHeader(builder, accessCondition.getHeader().toString(), accessCondition.getValue());
            }
        }
        return builder;
    }

    public static Builder addOptionalSourceAccessContitionHeader(Builder builder, AccessCondition accessCondition) {
        if (accessCondition != null) {
            if (accessCondition.getHeader() != AccessConditionHeaderType.NONE) {
                String headerName;
                switch (accessCondition.getHeader()) {
                    case IF_MATCH:
                        headerName = "x-ms-source-if-match";
                    case IF_UNMODIFIED_SINCE:
                        headerName = "x-ms-source-if-unmodified-since";
                    case IF_MODIFIED_SINCE:
                        headerName = "x-ms-source-if-modified-since";
                    case IF_NONE_MATCH:
                        headerName = "x-ms-source-if-none-match";
                    default:
                        headerName = "";
                }
                builder = addOptionalHeader(builder, headerName, accessCondition.getValue());
            }
        }
        return builder;
    }
}
