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
package com.microsoft.windowsazure.services.core.utils.pipeline;

import static org.junit.Assert.*;

import org.junit.Test;

import com.microsoft.windowsazure.services.blob.models.AccessCondition;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;

public class PipelineHelpersTest {
    @Test
    public void addOptionalSourceAccessConditionHeaderSuccess() throws Exception {
        // Arrange
        Client client = Client.create();
        WebResource webResource = client.resource("http://www.microsoft.com");
        Builder builder = webResource.header("ms-version", "1.0");
        AccessCondition accessCondition = AccessCondition.ifMatch("thisIsAETag");

        // Act
        Builder resultBuilder = PipelineHelpers.addOptionalSourceAccessConditionHeader(builder, accessCondition);

        // Assert
        assertNotNull(resultBuilder);

    }
}
