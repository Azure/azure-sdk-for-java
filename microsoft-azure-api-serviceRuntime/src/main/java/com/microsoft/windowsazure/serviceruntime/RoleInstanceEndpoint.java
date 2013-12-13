/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.serviceruntime;

import java.net.InetSocketAddress;

/**
 * Represents an endpoint associated with a role instance.
 */
public final class RoleInstanceEndpoint {

    /**
     * Gets the RoleInstance object associated with this endpoint.
     */
    private RoleInstance roleInstance;

    /**
     * Gets the protocol associated with the endpoint
     */
    private final String protocol;

    /**
     * Represents a network endpoint as an IP address and a port number.
     */
    private final InetSocketAddress ipEndPoint;

    /**
     * Package accessible constructor
     * 
     * @param protocol
     * @param ipEndPoint
     */
    RoleInstanceEndpoint(String protocol, InetSocketAddress ipEndPoint) {
        this.protocol = protocol;
        this.ipEndPoint = ipEndPoint;
    }

    void setRoleInstance(RoleInstance roleInstance) {
        this.roleInstance = roleInstance;
    }

    /**
     * Returns the {@link RoleInstance} object associated with this endpoint.
     * 
     * @return A <code>RoleInstance</code> object that represents the role
     *         instance associated with this endpoint.
     */
    public RoleInstance getRoleInstance() {
        return roleInstance;
    }

    /**
     * Returns the protocol associated with the endpoint.
     * 
     * @return A <code>String</code> object that represents the protocol
     *         associated with the endpoint.
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Returns an <code>InetSocketAddress</code> object for this role instance
     * endpoint. The <code>InetSocketAddress</code> object provides the IP
     * address and port number for the endpoint.
     * 
     * @return A <code>java.net.InetSocketAddress</code> object that contains
     *         the IP address and port number for the endpoint.
     */
    public InetSocketAddress getIpEndPoint() {
        return ipEndPoint;
    }
}
