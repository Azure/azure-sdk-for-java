/**
 * Copyright Microsoft Corporation
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

package com.microsoft.windowsazure.services.media.models;

import static org.junit.Assert.*;

import java.net.URLEncoder;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;

/**
 * Tests for the methods and factories of the Operation entity.
 */
public class OperationEntityTest {
    static final String sampleOperationId = "nb:cid:UUID:1151b8bd-9ada-4e7f-9787-8dfa49968eab";
    private final String expectedUri = String.format("Operations('%s')", URLEncoder.encode(sampleOperationId, "UTF-8"));

    public OperationEntityTest() throws Exception {
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void operationGetReturnsExpectedUri() throws Exception {
        EntityGetOperation<OperationInfo> getter = Operation.get(sampleOperationId);

        assertEquals(expectedUri, getter.getUri());
    }
}
