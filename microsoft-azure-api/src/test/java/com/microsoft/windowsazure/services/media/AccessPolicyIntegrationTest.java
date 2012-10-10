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

import java.util.EnumSet;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.CreateAccessPolicyOptions;

public class AccessPolicyIntegrationTest extends IntegrationTestBase {
    private MediaContract service;

    private final String invalidId = "notAValidId";
    private final String validButNonexistId = "nb:pid:UUID:bce3863e-830b-49f5-9199-7cfaff52935f";

    private static final String testPrefix = "testPolicy";

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @BeforeClass
    public static void setup() throws Exception {
        cleanupEnvironment();
    }

    @AfterClass
    public static void cleanup() throws Exception {
        cleanupEnvironment();
    }

    private static void cleanupEnvironment() {
        config = createConfig();
        MediaContract service = MediaService.create(config);
        try {
            List<AccessPolicyInfo> policies = service.listAccessPolicies();
            for (AccessPolicyInfo policy : policies) {
                if (policy.getName().startsWith(testPrefix)) {
                    service.deleteAccessPolicy(policy.getId());
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void verifyPolicyProperties(String message, String testName, double duration,
            AccessPolicyPermission permission, AccessPolicyInfo policy) {
        verifyPolicyProperties(message, testName, duration, EnumSet.of(permission), policy);
    }

    private void verifyPolicyProperties(String message, String testName, double duration,
            EnumSet<AccessPolicyPermission> permissions, AccessPolicyInfo policy) {
        assertNotNull(message, policy);
        assertEquals(message + " Name", testName, policy.getName());
        assertEquals(message + " DurationInMinutes", duration, policy.getDurationInMinutes(), 0.00001);
        for (AccessPolicyPermission permission : permissions) {
            if (permission != AccessPolicyPermission.NONE) {
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

    @Before
    public void setupInstance() throws Exception {
        service = MediaService.create(config);
    }

    @Test
    public void canCreateAccessPolicy() throws Exception {
        String testName = testPrefix + "CanCreate";
        double duration = 5;

        AccessPolicyInfo policy = service.createAccessPolicy(testName, duration);

        verifyPolicyProperties("policy", testName, duration, AccessPolicyPermission.WRITE, policy);
    }

    @Test
    public void canCreateAccessPolicyOptions() throws Exception {
        String testName = testPrefix + "CanCreateOptions";
        double duration = 5;
        AccessPolicyPermission permission = AccessPolicyPermission.WRITE;
        CreateAccessPolicyOptions options = new CreateAccessPolicyOptions().addPermissions(permission);

        AccessPolicyInfo policy = service.createAccessPolicy(testName, duration, options);

        verifyPolicyProperties("policy", testName, duration, permission, policy);
    }

    // TODO: Null name or duration?

    @Test
    public void canGetSinglePolicyById() throws Exception {
        String expectedName = testPrefix + "GetOne";
        double duration = 1;
        AccessPolicyInfo policyToGet = service.createAccessPolicy(expectedName, duration);

        AccessPolicyInfo retrievedPolicy = service.getAccessPolicy(policyToGet.getId());

        assertEquals(policyToGet.getId(), retrievedPolicy.getId());
        verifyPolicyProperties("retrievedPolicy", expectedName, duration, AccessPolicyPermission.WRITE, retrievedPolicy);
    }

    @Test
    public void canGetSinglePolicyByInvalidId() throws Exception {
        expected.expect(ServiceException.class);
        expected.expect(new ServiceExceptionMatcher(500));
        service.getAccessPolicy(invalidId);
    }

    @Test
    public void canGetSinglePolicyByNonexistId() throws Exception {
        expected.expect(ServiceException.class);
        expected.expect(new ServiceExceptionMatcher(404));
        service.getAccessPolicy(validButNonexistId);
    }

    @Test
    public void canRetrieveListOfAccessPolicies() throws Exception {
        String[] policyNames = new String[] { testPrefix + "ListOne", testPrefix + "ListTwo" };
        double duration = 3;
        EnumSet<AccessPolicyPermission> permissions = EnumSet.of(AccessPolicyPermission.WRITE,
                AccessPolicyPermission.LIST);

        for (String name : policyNames) {
            service.createAccessPolicy(name, duration, new CreateAccessPolicyOptions().addPermissions(permissions));
        }

        List<AccessPolicyInfo> policies = service.listAccessPolicies();

        assertNotNull(policies);
        assertTrue(policies.size() >= 2);

        AccessPolicyInfo[] orderedPolicies = new AccessPolicyInfo[policyNames.length];

        for (AccessPolicyInfo policy : policies) {
            for (int i = 0; i < policyNames.length; i++) {
                if (policy.getName().equals(policyNames[i])) {
                    orderedPolicies[i] = policy;
                }
            }
        }

        for (int i = 0; i < policyNames.length; i++) {
            verifyPolicyProperties("orderedPolicies " + i, policyNames[i], duration, permissions, orderedPolicies[i]);
        }
    }

    // Note: Access Policy cannot be updated.

    @Test
    public void canDeleteAccessPolicyById() throws Exception {
        String policyName = testPrefix + "ToDelete";
        double duration = 1;
        AccessPolicyInfo policyToDelete = service.createAccessPolicy(policyName, duration);
        List<AccessPolicyInfo> listPoliciesResult = service.listAccessPolicies();
        int policyCountBaseline = listPoliciesResult.size();

        service.deleteAccessPolicy(policyToDelete.getId());

        listPoliciesResult = service.listAccessPolicies();
        assertEquals("listPoliciesResult.size", policyCountBaseline - 1, listPoliciesResult.size());

        for (AccessPolicyInfo policy : service.listAccessPolicies()) {
            assertFalse(policyToDelete.getId().equals(policy.getId()));
        }

        expected.expect(ServiceException.class);
        expected.expect(new ServiceExceptionMatcher(404));
        service.getAccessPolicy(policyToDelete.getId());
    }

    @Test
    public void canDeleteAccessPolicyByInvalidId() throws Exception {
        expected.expect(ServiceException.class);
        expected.expect(new ServiceExceptionMatcher(500));
        service.deleteAccessPolicy(invalidId);
    }

    @Test
    public void canDeleteAccessPolicyByNonexistId() throws Exception {
        expected.expect(ServiceException.class);
        expected.expect(new ServiceExceptionMatcher(404));
        service.deleteAccessPolicy(validButNonexistId);
    }
}
