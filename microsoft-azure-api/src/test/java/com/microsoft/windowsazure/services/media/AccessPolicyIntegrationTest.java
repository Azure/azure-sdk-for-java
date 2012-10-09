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
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.microsoft.windowsazure.services.core.Configuration;
import com.microsoft.windowsazure.services.core.ServiceException;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.CreateAccessPolicyOptions;

public class AccessPolicyIntegrationTest extends IntegrationTestBase {
    private static MediaContract service;

    private static final String testPrefix = "testPolicy";

    @Rule
    public ExpectedException expected = ExpectedException.none();

    @BeforeClass
    public static void setup() throws Exception {
        service = MediaService.create(createConfig());
    }

    private static Configuration createConfig() {
        Configuration config = Configuration.getInstance();
        overrideWithEnv(config, MediaConfiguration.URI);
        overrideWithEnv(config, MediaConfiguration.OAUTH_URI);
        overrideWithEnv(config, MediaConfiguration.OAUTH_CLIENT_ID);
        overrideWithEnv(config, MediaConfiguration.OAUTH_CLIENT_SECRET);
        overrideWithEnv(config, MediaConfiguration.OAUTH_SCOPE);
        return config;
    }

    @AfterClass
    public static void cleanup() throws Exception {
        for (AccessPolicyInfo policy : service.listAccessPolicies()) {
            if (policy.getName().startsWith(testPrefix)) {
                service.deleteAccessPolicy(policy.getId());
            }
        }
    }

    @Test
    public void canCreateAccessPolicy() throws Exception {
        AccessPolicyInfo policy = service.createAccessPolicy(testPrefix + "CanCreate", 5,
                new CreateAccessPolicyOptions().addPermissions(EnumSet.of(AccessPolicyPermission.WRITE)));

        assertTrue(policy.getPermissions().contains(AccessPolicyPermission.WRITE));
    }

    @Test
    public void canGetSinglePolicyById() throws Exception {
        String expectedName = testPrefix + "GetOne";
        AccessPolicyInfo policyToGet = service.createAccessPolicy(expectedName, 1);

        AccessPolicyInfo retrievedPolicy = service.getAccessPolicy(policyToGet.getId());

        assertEquals(expectedName, retrievedPolicy.getName());
        assertEquals(policyToGet.getId(), retrievedPolicy.getId());
    }

    @Test
    public void canRetrieveListOfAccessPolicies() throws Exception {
        String[] policyNames = new String[] { testPrefix + "ListOne", testPrefix + "ListTwo" };
        for (String name : policyNames) {
            service.createAccessPolicy(name, 3, new CreateAccessPolicyOptions().addPermissions(EnumSet.of(
                    AccessPolicyPermission.WRITE, AccessPolicyPermission.LIST)));
        }

        List<AccessPolicyInfo> policies = service.listAccessPolicies();

        assertNotNull(policies);
        assertTrue(policies.size() >= 2);

        AccessPolicyInfo policy1 = null;
        AccessPolicyInfo policy2 = null;

        for (AccessPolicyInfo policy : policies) {
            if (policy.getName().equals(policyNames[0])) {
                policy1 = policy;
            }
            if (policy.getName().equals(policyNames[1])) {
                policy2 = policy;
            }
        }

        assertNotNull(policy1);
        assertNotNull(policy2);

        assertTrue(policy1.getPermissions().containsAll(
                EnumSet.of(AccessPolicyPermission.WRITE, AccessPolicyPermission.LIST)));
    }

    @Test
    public void getWithBadIdThrowsServiceException() throws Exception {
        expected.expect(ServiceException.class);
        AccessPolicyInfo policy = service.getAccessPolicy("notAValidId");
    }

    @Test
    public void getWithValidButNonExistentPolicyIdThrows404ServiceException() throws Exception {
        expected.expect(new ServiceExceptionMatcher(404));
        service.getAccessPolicy("nb:pid:UUID:bce3863e-830b-49f5-9199-7cfaff52935f");
    }

    @Test
    public void canDeleteAccessPolicyById() throws Exception {
        AccessPolicyInfo policyToDelete = service.createAccessPolicy(testPrefix + "ToDelete", 1);

        service.deleteAccessPolicy(policyToDelete.getId());

        for (AccessPolicyInfo policy : service.listAccessPolicies()) {
            assertFalse(policyToDelete.getId().equals(policy.getId()));
        }
    }
}
