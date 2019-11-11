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

import java.util.EnumSet;

import com.microsoft.windowsazure.services.media.entityoperations.DefaultDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.DefaultListOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityCreateOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityDeleteOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityGetOperation;
import com.microsoft.windowsazure.services.media.entityoperations.EntityOperationSingleResultBase;
import com.microsoft.windowsazure.services.media.implementation.content.AccessPolicyType;
import com.sun.jersey.api.client.GenericType;

/**
 * Class for creating operations to manipulate Access Policy entities.
 * 
 */
public final class AccessPolicy {

    private static final String ENTITY_SET = "AccessPolicies";

    private AccessPolicy() {
    }

    /**
     * Creates an operation to create a new access policy
     * 
     * @param name
     *            name of the access policy
     * @param durationInMinutes
     *            how long the access policy will be in force
     * @param permissions
     *            permissions allowed by this access policy
     * @return The operation
     */
    public static EntityCreateOperation<AccessPolicyInfo> create(String name,
            double durationInMinutes,
            EnumSet<AccessPolicyPermission> permissions) {
        return new Creator(name, durationInMinutes, permissions);
    }

    private static class Creator extends
            EntityOperationSingleResultBase<AccessPolicyInfo> implements
            EntityCreateOperation<AccessPolicyInfo> {
        private final String policyName;
        private final double durationInMinutes;
        private final EnumSet<AccessPolicyPermission> permissions;

        public Creator(String policyName, double durationInMinutes,
                EnumSet<AccessPolicyPermission> permissions) {

            super(ENTITY_SET, AccessPolicyInfo.class);

            this.policyName = policyName;
            this.durationInMinutes = durationInMinutes;
            this.permissions = permissions;
        }

        @Override
        public Object getRequestContents() {
            return new AccessPolicyType()
                    .setName(policyName)
                    .setDurationInMinutes(durationInMinutes)
                    .setPermissions(
                            AccessPolicyPermission
                                    .bitsFromPermissions(permissions));
        }

    }

    /**
     * Create an operation that will retrieve the given access policy
     * 
     * @param accessPolicyId
     *            id of access policy to retrieve
     * @return the operation
     */
    public static EntityGetOperation<AccessPolicyInfo> get(String accessPolicyId) {
        return new DefaultGetOperation<AccessPolicyInfo>(ENTITY_SET,
                accessPolicyId, AccessPolicyInfo.class);
    }

    /**
     * Create an operation that will retrieve the access policy at the given
     * link
     * 
     * @param link
     *            the link
     * @return the operation
     */
    public static EntityGetOperation<AccessPolicyInfo> get(
            LinkInfo<AccessPolicyInfo> link) {
        return new DefaultGetOperation<AccessPolicyInfo>(link.getHref(),
                AccessPolicyInfo.class);
    }

    /**
     * Create an operation that will retrieve all access policies
     * 
     * @return the operation
     */
    public static DefaultListOperation<AccessPolicyInfo> list() {
        return new DefaultListOperation<AccessPolicyInfo>(ENTITY_SET,
                new GenericType<ListResult<AccessPolicyInfo>>() {
                });
    }

    /**
     * Create an operation to delete the given access policy
     * 
     * @param accessPolicyId
     *            id of access policy to delete
     * @return the delete operation
     */
    public static EntityDeleteOperation delete(String accessPolicyId) {
        return new DefaultDeleteOperation(ENTITY_SET, accessPolicyId);
    }
}
