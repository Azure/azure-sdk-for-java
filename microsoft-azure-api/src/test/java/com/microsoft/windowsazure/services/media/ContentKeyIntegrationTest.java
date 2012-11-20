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

package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.junit.Test;

import com.microsoft.windowsazure.services.core.ExponentialRetryPolicy;
import com.microsoft.windowsazure.services.core.RetryPolicyFilter;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.ContentKey;
import com.microsoft.windowsazure.services.media.models.ContentKeyInfo;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class ContentKeyIntegrationTest extends IntegrationTestBase {
    private void verifyInfosEqual(String message, ContentKeyInfo expected, ContentKeyInfo actual) {
        verifyPolicyProperties(message, expected.getName(), expected.getDurationInMinutes(), expected.getPermissions(),
                actual);
    }

    private void verifyPolicyProperties(String message, String testName, double duration,
            ContentKeyPermission permission, ContentKeyInfo policy) {
        verifyPolicyProperties(message, testName, duration, EnumSet.of(permission), policy);
    }

    private void verifyPolicyProperties(String message, String testName, double duration,
            EnumSet<ContentKeyPermission> permissions, ContentKeyInfo policy) {
        assertNotNull(message, policy);
        assertEquals(message + " Name", testName, policy.getName());
        assertEquals(message + " DurationInMinutes", duration, policy.getDurationInMinutes(), 0.00001);
        for (ContentKeyPermission permission : permissions) {
            if (permission != ContentKeyPermission.NONE) {
                assertTrue(message + "permissions should contain " + permission,
                        policy.getPermissions().contains(permission));
            }
        }
        assertEquals(message + " Permissions", permissions, policy.getPermissions());

        assertNotNull(message + " Id", policy.getId());
        assertNotNull(message + " Created", policy.getCreated());
        assertNotNull(message + " LastModified", policy.getLastModified());
        assertEquals(message + " Created & LastModified", policy.getCreated(), policy.getLastModified());
    }

    @Test
    public void canCreateContentKey() throws Exception {
        String testName = testPolicyPrefix + "CanCreate";
        double duration = 5;

        ContentKeyInfo policy = service.create(ContentKey.create(testName, duration,
                EnumSet.of(ContentKeyPermission.WRITE)));

        verifyPolicyProperties("policy", testName, duration, ContentKeyPermission.WRITE, policy);
    }

    @Test
    public void canCreateContentKeyWithReadPermissions() throws Exception {
        String testName = testPolicyPrefix + "CanCreateRead";
        double duration = 5;

        ContentKeyInfo policy = service.create(ContentKey.create(testName, duration,
                EnumSet.of(ContentKeyPermission.READ)));

        verifyPolicyProperties("policy", testName, duration, ContentKeyPermission.READ, policy);
    }

    // TODO: Null name or duration?

    @Test
    public void canGetSinglePolicyById() throws Exception {
        String expectedName = testPolicyPrefix + "GetOne";
        double duration = 1;
        ContentKeyInfo policyToGet = service.create(ContentKey.create(expectedName, duration,
                EnumSet.of(ContentKeyPermission.WRITE)));

        ContentKeyInfo retrievedPolicy = service.get(ContentKey.get(policyToGet.getId()));

        assertEquals(policyToGet.getId(), retrievedPolicy.getId());
        verifyPolicyProperties("retrievedPolicy", expectedName, duration, ContentKeyPermission.WRITE, retrievedPolicy);
    }

    @Test
    public void canGetSinglePolicyByInvalidId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(500));
        service.get(ContentKey.get(invalidId));
    }

    @Test
    public void canGetSinglePolicyByNonexistId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(ContentKey.get(validButNonexistContentKeyId));
    }

    @Test
    public void canRetrieveListOfAccessPolicies() throws Exception {
        String[] policyNames = new String[] { testPolicyPrefix + "ListOne", testPolicyPrefix + "ListTwo" };
        double duration = 3;
        EnumSet<ContentKeyPermission> permissions = EnumSet.of(ContentKeyPermission.WRITE, ContentKeyPermission.LIST);

        List<ContentKeyInfo> expectedAccessPolicies = new ArrayList<ContentKeyInfo>();
        for (int i = 0; i < policyNames.length; i++) {
            ContentKeyInfo policy = service.create(ContentKey.create(policyNames[i], duration, permissions));
            expectedAccessPolicies.add(policy);
        }

        List<ContentKeyInfo> actualAccessPolicies = service.list(ContentKey.list());

        verifyListResultContains("listAccessPolicies", expectedAccessPolicies, actualAccessPolicies,
                new ComponentDelegate() {
                    @Override
                    public void verifyEquals(String message, Object expected, Object actual) {
                        verifyInfosEqual(message, (ContentKeyInfo) expected, (ContentKeyInfo) actual);
                    }
                });
    }

    @Test
    public void canUseQueryParametersWhenListingAccessPolicies() throws Exception {
        String[] policyNames = new String[] { testPolicyPrefix + "ListThree", testPolicyPrefix + "ListFour",
                testPolicyPrefix + "ListFive", testPolicyPrefix + "ListSix", testPolicyPrefix + "ListSeven" };

        double duration = 3;
        EnumSet<ContentKeyPermission> permissions = EnumSet.of(ContentKeyPermission.WRITE, ContentKeyPermission.LIST);

        List<ContentKeyInfo> expectedAccessPolicies = new ArrayList<ContentKeyInfo>();
        for (int i = 0; i < policyNames.length; i++) {
            ContentKeyInfo policy = service.create(ContentKey.create(policyNames[i], duration, permissions));
            expectedAccessPolicies.add(policy);
        }

        MultivaluedMap<String, String> options = new MultivaluedMapImpl();
        options.add("$top", "2");

        List<ContentKeyInfo> actualAccessPolicies = service.list(ContentKey.list(options));

        assertEquals(2, actualAccessPolicies.size());
    }

    // Note: Access Policy cannot be updated.

    @Test
    public void canDeleteContentKeyById() throws Exception {
        String policyName = testPolicyPrefix + "ToDelete";
        double duration = 1;
        ContentKeyInfo policyToDelete = service.create(ContentKey.create(policyName, duration,
                EnumSet.of(ContentKeyPermission.WRITE)));
        List<ContentKeyInfo> listPoliciesResult = service.list(ContentKey.list());
        int policyCountBaseline = listPoliciesResult.size();

        service.delete(ContentKey.delete(policyToDelete.getId()));

        listPoliciesResult = service.list(ContentKey.list());
        assertEquals("listPoliciesResult.size", policyCountBaseline - 1, listPoliciesResult.size());

        for (ContentKeyInfo policy : service.list(ContentKey.list())) {
            assertFalse(policyToDelete.getId().equals(policy.getId()));
        }

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(ContentKey.get(policyToDelete.getId()));
    }

    @Test
    public void canDeleteContentKeyByInvalidId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(500));
        service.delete(ContentKey.delete(invalidId));
    }

    @Test
    public void canDeleteContentKeyByNonexistId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.delete(ContentKey.delete(validButNonexistContentKeyId));
    }

    @Test
    public void canRetryContentKeyCreation() throws Exception {
        String name = testPolicyPrefix + "canRetryContentKeyCreationPolicy";
        double duration = 1;
        EnumSet<ContentKeyPermission> write = EnumSet.of(ContentKeyPermission.WRITE);
        service.create(ContentKey.create(name + "1", duration, write));

        ExponentialRetryPolicy forceRetryPolicy = new ExponentialRetryPolicy(1, 1, new int[] { 201 });
        MediaContract forceRetryService = service.withFilter(new RetryPolicyFilter(forceRetryPolicy));

        forceRetryService.create(ContentKey.create(name + "2", duration, write));
    }
}
