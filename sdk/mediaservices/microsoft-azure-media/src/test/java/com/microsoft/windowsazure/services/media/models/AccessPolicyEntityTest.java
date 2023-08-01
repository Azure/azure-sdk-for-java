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

import static org.junit.Assert.assertEquals;

import java.net.URLEncoder;
import java.util.EnumSet;

import org.junit.Test;

import com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityListOperation;
import com.microsoft.windowsazure.services.media.implementation.content.AccessPolicyType;

/**
 * Tests for access policy entity
 * 
 */
public class AccessPolicyEntityTest {
    private static final String examplePolicyId = "nb:pid:UUID:c577052a-6c0a-45b0-bf15-3ff3a2a41802";
    private final String expectedUri;

    public AccessPolicyEntityTest() throws Exception {
        expectedUri = String.format("AccessPolicies('%s')",
                URLEncoder.encode(examplePolicyId, "UTF-8"));
    }

    @Test
    public void createAccessPolicyProvidesExpectedPayload() throws Exception {
        String name = "some Access Policy";
        double duration = 10;
        EnumSet<AccessPolicyPermission> permissions = EnumSet.of(
                AccessPolicyPermission.READ, AccessPolicyPermission.LIST);

        EntityCreateOperation<AccessPolicyInfo> creator = AccessPolicy.create(
                name, duration, permissions);

        AccessPolicyType payload = (AccessPolicyType) creator
                .getRequestContents();

        assertEquals(name, payload.getName());
        assertEquals(duration, payload.getDurationInMinutes(), 0.0);
        assertEquals(AccessPolicyPermission.bitsFromPermissions(permissions),
                payload.getPermissions().intValue());
    }

    @Test
    public void getReturnsExpectedUri() throws Exception {
        EntityGetOperation<AccessPolicyInfo> getter = AccessPolicy
                .get(examplePolicyId);

        assertEquals(expectedUri, getter.getUri());
    }

    @Test
    public void listReturnsExpectedUri() throws Exception {
        EntityListOperation<AccessPolicyInfo> lister = AccessPolicy.list();

        assertEquals("AccessPolicies", lister.getUri());
    }

    @Test
    public void listWithQueryParametersReturnsThem() throws Exception {
        EntityListOperation<AccessPolicyInfo> lister = AccessPolicy.list()
                .setTop(10).setSkip(2);

        assertEquals("10", lister.getQueryParameters().getFirst("$top"));
        assertEquals("2", lister.getQueryParameters().getFirst("$skip"));
        assertEquals(2, lister.getQueryParameters().size());
    }

    @Test
    public void deleteReturnsExpectedUri() throws Exception {
        assertEquals(expectedUri, AccessPolicy.delete(examplePolicyId).getUri());
    }
}
