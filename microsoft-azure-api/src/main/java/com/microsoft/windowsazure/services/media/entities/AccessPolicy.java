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

package com.microsoft.windowsazure.services.media.entities;

import java.util.EnumSet;

import javax.ws.rs.core.MultivaluedMap;

import com.microsoft.windowsazure.services.media.implementation.content.AccessPolicyType;
import com.microsoft.windowsazure.services.media.models.AccessPolicyInfo;
import com.microsoft.windowsazure.services.media.models.AccessPolicyPermission;
import com.microsoft.windowsazure.services.media.models.ListResult;
import com.sun.jersey.api.client.GenericType;

/**
 * 
 *
 */
public class AccessPolicy {

    private AccessPolicy() {
    }

    public static EntityCreationOperation<AccessPolicyInfo> create(String name, double durationInMinutes,
            EnumSet<AccessPolicyPermission> permissions) {
        return new CreatorImpl(name, durationInMinutes, permissions);
    }

    private static class CreatorImpl extends EntityOperationSingleResultBase<AccessPolicyInfo> implements
            EntityCreationOperation<AccessPolicyInfo> {
        private final String policyName;
        private final double durationInMinutes;
        private final EnumSet<AccessPolicyPermission> permissions;

        public CreatorImpl(String policyName, double durationInMinutes, EnumSet<AccessPolicyPermission> permissions) {

            super("AccessPolicies", AccessPolicyInfo.class);

            this.policyName = policyName;
            this.durationInMinutes = durationInMinutes;
            this.permissions = permissions;
        }

        @Override
        public Object getRequestContents() {
            return new AccessPolicyType().setName(policyName).setDurationInMinutes(durationInMinutes)
                    .setPermissions(AccessPolicyPermission.bitsFromPermissions(permissions));
        }

    }

    public static EntityGetOperation<AccessPolicyInfo> get(String accessPolicyId) {
        return new DefaultGetterOperation<AccessPolicyInfo>("AccessPolicies", accessPolicyId, AccessPolicyInfo.class);
    }

    public static EntityListOperation<AccessPolicyInfo> list() {
        return new DefaultListOperation<AccessPolicyInfo>("AccessPolicies",
                new GenericType<ListResult<AccessPolicyInfo>>() {
                });
    }

    public static EntityListOperation<AccessPolicyInfo> list(MultivaluedMap<String, String> queryParameters) {
        return new DefaultListOperation<AccessPolicyInfo>("AccessPolicies",
                new GenericType<ListResult<AccessPolicyInfo>>() {
                }, queryParameters);
    }

    public static EntityDeleteOperation delete(String accessPolicyId) {
        return new DefaultDeleteOperation("AccessPolicies", accessPolicyId);
    }
}
