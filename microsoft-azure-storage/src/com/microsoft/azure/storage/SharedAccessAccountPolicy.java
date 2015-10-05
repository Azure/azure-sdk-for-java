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
package com.microsoft.azure.storage;

import java.util.EnumSet;

/**
 * Represents a shared access policy, which specifies the start time, expiry time,
 * and permissions for a shared access signature.
 */
public final class SharedAccessAccountPolicy extends SharedAccessPolicy {
    /**
     *  The permissions for a shared access signature associated with this shared access policy.
     */
    private EnumSet<SharedAccessAccountPermissions> permissions;

    /**
     * The services (blob, file, queue, table) for a shared access signature associated with this shared access policy.
     */
    private EnumSet<SharedAccessAccountService> services;

    /**
     * The resource type for a shared access signature associated with this shared access policy.
     */
    private EnumSet<SharedAccessAccountResourceType> resourceTypes;
    
    /**
     * The allowed IP addresses for a shared access signature associated with this shared access policy.
     */
    private IPRange range;
    
    /**
     * The allowed protocols for a shared access signature associated with this shared access policy.
     */
    private SharedAccessProtocols protocols;
    
    /**
     * Gets the permissions for a shared access signature associated with this shared access policy.
     * 
     * @return the permissions
     */
    public EnumSet<SharedAccessAccountPermissions> getPermissions() {
        return this.permissions;
    }

    /**
     * Gets the services (blob, file, queue, table) for a shared access signature associated with
     * this shared access policy.
     * 
     * @return the services
     */
    public EnumSet<SharedAccessAccountService> getServices() {
        return this.services;
    }

    /**
     * Gets the resource type for a shared access signature associated with this shared access policy.
     * 
     * @return the resourceTypes
     */
    public EnumSet<SharedAccessAccountResourceType> getResourceTypes() {
        return this.resourceTypes;
    }

    /**
     * Gets the allowed IP addresses for a shared access signature associated with this shared access policy.
     * 
     * @return A {@link IPRange} object containing the range of IP addresses.
     */
    public IPRange getRange() {
        return range;
    }

    /**
     * Gets the allowed protocols for a shared access signature associated with this shared access policy.
     * 
     * @return A {@link SharedAccessProtocols} representing the chosen Internet protocols.
     */
    public SharedAccessProtocols getProtocols() {
        return protocols;
    }
    
    /**
     * Sets the permissions for a shared access signature associated with this shared access policy.
     * 
     * @param permissions
     *            the permissions to set
     */
    public void setPermissions(EnumSet<SharedAccessAccountPermissions> permissions) {
        this.permissions = permissions;
    }

    /**
     * Sets the services (blob, file, queue, table) for a shared access signature associated with
     * this shared access policy.
     * 
     * @param services
     *            the services to set
     */
    public void setServices(EnumSet<SharedAccessAccountService> services) {
        this.services = services;
    }

    /**
     * Sets the resource type for a shared access signature associated with this shared access policy.
     * 
     * @param resourceTypes
     *            the resourceTypes to set
     */
    public void setResourceTypes(EnumSet<SharedAccessAccountResourceType> resourceTypes) {
        this.resourceTypes = resourceTypes;
    }

    /**
     * Sets the allowed IP addresses for a shared access signature associated with this shared access policy.
     * 
     * @param range
     *        A {@link IPRange} object containing the range of IP addresses.
     */
    public void setRange(IPRange range) {
        this.range = range;
    }

    /**
     * Sets the allowed protocols for a shared access signature associated with this shared access policy.
     * 
     * @param protocols
     *        A {@link SharedAccessProtocols} representing the chosen Internet protocols.
     */
    public void setProtocols(SharedAccessProtocols protocols) {
        this.protocols = protocols;
    }

    /**
     * Converts this shared access policy's permissions to a <code>String</code>.
     * 
     * @return A <code>String</code> which represents the shared access permissions.
     */
    public String permissionsToString() {
        return SharedAccessAccountPermissions.permissionsToString(this.getPermissions());
    }

    /**
     * Converts this shared access policy's resource types to a <code>String</code>.
     * 
     * @return A <code>String</code> which represents the shared access permissions.
     */
    public String resourceTypesToString() {
        return SharedAccessAccountResourceType.resourceTypesToString(this.getResourceTypes());
    }

    /**
     * Converts this shared access policy's services to a <code>String</code>.
     * 
     * @return A <code>String</code> which represents the shared access permissions.
     */
    public String servicesToString() {
        return SharedAccessAccountService.servicesToString(this.getServices());
    }

    /**
     * Sets shared access permissions using the specified permissions <code>String</code>.
     * 
     * @param value
     *        A <code>String</code> which represents the shared access permissions.
     **/
    public void setPermissionsFromString(final String value) {
        this.setPermissions(SharedAccessAccountPermissions.permissionsFromString(value));
    }
    
    /**
     * Sets shared access resource types using the specified resource types <code>String</code>.
     * 
     * @param value
     *        A <code>String</code> which represents the shared access resource types.
     **/
    public void setResourceTypeFromString(final String value) {
        this.setResourceTypes(SharedAccessAccountResourceType.resourceTypesFromString(value));
    }

    /**
     * Sets shared access services using the specified services <code>String</code>.
     * 
     * @param value
     *        A <code>String</code> which represents the shared access services.
     **/
    public void setServiceFromString(final String value) {
        this.setServices(SharedAccessAccountService.servicesFromString(value));
    }
}