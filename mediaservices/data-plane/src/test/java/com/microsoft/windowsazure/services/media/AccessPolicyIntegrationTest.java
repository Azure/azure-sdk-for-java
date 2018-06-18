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

package com.microsoft.windowsazure.services.media;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.Test;

import com.microsoft.windowsazure.core.pipeline.jersey.ExponentialRetryPolicy;
import com.microsoft.windowsazure.core.pipeline.jersey.RetryPolicyFilter;
import com.microsoft.windowsazure.exception.ServiceException;
import com.microsoft.windowsazure.services.media.models.AccessPolicy;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;

public class AccessPolicyIntegrationTest extends IntegrationTestBase {
    private void verifyInfosEqual(String message, AccessPolicyInfo expected,
            AccessPolicyInfo actual) {
        verifyPolicyProperties(message, expected.getName(),
                expected.getDurationInMinutes(), expected.getPermissions(),
                actual);
    }

    private void verifyPolicyProperties(String message, String testName,
            double duration, AccessPolicyPermission permission,
            AccessPolicyInfo policy) {
        verifyPolicyProperties(message, testName, duration,
                EnumSet.of(permission), policy);
    }

    private void verifyPolicyProperties(String message, String testName,
            double duration, EnumSet<AccessPolicyPermission> permissions,
            AccessPolicyInfo policy) {
        assertNotNull(message, policy);
        assertEquals(message + " Name", testName, policy.getName());
        assertEquals(message + " DurationInMinutes", duration,
                policy.getDurationInMinutes(), 0.00001);
        for (AccessPolicyPermission permission : permissions) {
            if (permission != AccessPolicyPermission.NONE) {
                assertTrue(
                        message + "permissions should contain " + permission,
                        policy.getPermissions().contains(permission));
            }
        }
        assertEquals(message + " Permissions", permissions,
                policy.getPermissions());

        assertNotNull(message + " Id", policy.getId());
        assertNotNull(message + " Created", policy.getCreated());
        assertNotNull(message + " LastModified", policy.getLastModified());
        assertEquals(message + " Created & LastModified", policy.getCreated(),
                policy.getLastModified());
    }

    @Test
    public void canCreateAccessPolicy() throws Exception {
        String testName = testPolicyPrefix + "CanCreate";
        double duration = 5;

        AccessPolicyInfo policy = service.create(AccessPolicy.create(testName,
                duration, EnumSet.of(AccessPolicyPermission.WRITE)));

        verifyPolicyProperties("policy", testName, duration,
                AccessPolicyPermission.WRITE, policy);
    }

    @Test
    public void canCreateAccessPolicyWithReadPermissions() throws Exception {
        String testName = testPolicyPrefix + "CanCreateRead";
        double duration = 5;

        AccessPolicyInfo policy = service.create(AccessPolicy.create(testName,
                duration, EnumSet.of(AccessPolicyPermission.READ)));

        verifyPolicyProperties("policy", testName, duration,
                AccessPolicyPermission.READ, policy);
    }

    @Test
    public void canGetSinglePolicyById() throws Exception {
        String expectedName = testPolicyPrefix + "GetOne";
        double duration = 1;
        AccessPolicyInfo policyToGet = service.create(AccessPolicy.create(
                expectedName, duration,
                EnumSet.of(AccessPolicyPermission.WRITE)));

        AccessPolicyInfo retrievedPolicy = service.get(AccessPolicy
                .get(policyToGet.getId()));

        assertEquals(policyToGet.getId(), retrievedPolicy.getId());
        verifyPolicyProperties("retrievedPolicy", expectedName, duration,
                AccessPolicyPermission.WRITE, retrievedPolicy);
    }

    @Test
    public void canGetSinglePolicyByInvalidId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.get(AccessPolicy.get(invalidId));
    }

    @Test
    public void canGetSinglePolicyByNonexistId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(AccessPolicy.get(validButNonexistAccessPolicyId));
    }

    @Test
    public void canRetrieveListOfAccessPolicies() throws Exception {
        String[] policyNames = new String[] { testPolicyPrefix + "ListOne",
                testPolicyPrefix + "ListTwo" };
        double duration = 3;
        EnumSet<AccessPolicyPermission> permissions = EnumSet.of(
                AccessPolicyPermission.WRITE, AccessPolicyPermission.LIST);

        List<AccessPolicyInfo> expectedAccessPolicies = new ArrayList<AccessPolicyInfo>();
        for (int i = 0; i < policyNames.length; i++) {
            AccessPolicyInfo policy = service.create(AccessPolicy.create(
                    policyNames[i], duration, permissions));
            expectedAccessPolicies.add(policy);
        }

        List<AccessPolicyInfo> actualAccessPolicies = service.list(AccessPolicy
                .list());

        verifyListResultContains("listAccessPolicies", expectedAccessPolicies,
                actualAccessPolicies, new ComponentDelegate() {
                    @Override
                    public void verifyEquals(String message, Object expected,
                            Object actual) {
                        verifyInfosEqual(message, (AccessPolicyInfo) expected,
                                (AccessPolicyInfo) actual);
                    }
                });
    }

    @Test
    public void canUseQueryParametersWhenListingAccessPolicies()
            throws Exception {
        String[] policyNames = new String[] { testPolicyPrefix + "ListThree",
                testPolicyPrefix + "ListFour", testPolicyPrefix + "ListFive",
                testPolicyPrefix + "ListSix", testPolicyPrefix + "ListSeven" };

        double duration = 3;
        EnumSet<AccessPolicyPermission> permissions = EnumSet.of(
                AccessPolicyPermission.WRITE, AccessPolicyPermission.LIST);

        List<AccessPolicyInfo> expectedAccessPolicies = new ArrayList<AccessPolicyInfo>();
        for (int i = 0; i < policyNames.length; i++) {
            AccessPolicyInfo policy = service.create(AccessPolicy.create(
                    policyNames[i], duration, permissions));
            expectedAccessPolicies.add(policy);
        }

        List<AccessPolicyInfo> actualAccessPolicies = service.list(AccessPolicy
                .list().setTop(2));

        assertEquals(2, actualAccessPolicies.size());
    }

    // Note: Access Policy cannot be updated.

    @Test
    public void canDeleteAccessPolicyById() throws Exception {
        String policyName = testPolicyPrefix + "ToDelete";
        double duration = 1;
        AccessPolicyInfo policyToDelete = service
                .create(AccessPolicy.create(policyName, duration,
                        EnumSet.of(AccessPolicyPermission.WRITE)));
        List<AccessPolicyInfo> listPoliciesResult = service.list(AccessPolicy
                .list());
        int policyCountBaseline = listPoliciesResult.size();

        service.delete(AccessPolicy.delete(policyToDelete.getId()));

        listPoliciesResult = service.list(AccessPolicy.list());
        assertEquals("listPoliciesResult.size", policyCountBaseline - 1,
                listPoliciesResult.size());

        for (AccessPolicyInfo policy : service.list(AccessPolicy.list())) {
            assertFalse(policyToDelete.getId().equals(policy.getId()));
        }

        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.get(AccessPolicy.get(policyToDelete.getId()));
    }

    @Test
    public void canDeleteAccessPolicyByInvalidId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(400));
        service.delete(AccessPolicy.delete(invalidId));
    }

    @Test
    public void canDeleteAccessPolicyByNonexistId() throws Exception {
        expectedException.expect(ServiceException.class);
        expectedException.expect(new ServiceExceptionMatcher(404));
        service.delete(AccessPolicy.delete(validButNonexistAccessPolicyId));
    }

    @Test
    public void canRetryAccessPolicyCreation() throws Exception {
        String name = testPolicyPrefix + "canRetryAccessPolicyCreationPolicy";
        double duration = 1;
        EnumSet<AccessPolicyPermission> write = EnumSet
                .of(AccessPolicyPermission.WRITE);
        service.create(AccessPolicy.create(name + "1", duration, write));

        ExponentialRetryPolicy forceRetryPolicy = new ExponentialRetryPolicy(1,
                1, new int[] { 201 });
        MediaContract forceRetryService = service
                .withFilter(new RetryPolicyFilter(forceRetryPolicy));

        forceRetryService.create(AccessPolicy.create(name + "2", duration,
                write));
    }
}
