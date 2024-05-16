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
package com.microsoft.windowsazure.services.blob.models;


/**
 * A wrapper class for the response returned from a Blob Service REST API Get
 * Container ACL operation. This is returned by calls to implementations of
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#getContainerACL(String)} and
 * {@link com.microsoft.windowsazure.services.blob.BlobContract#getContainerACL(String, BlobServiceOptions)}.
 * <p>
 * See the <a
 * href="http://msdn.microsoft.com/en-us/library/windowsazure/dd179469.aspx">Get
 * Container ACL</a> documentation on MSDN for details of the underlying Blob
 * Service REST API operation.
 */
public class GetContainerACLResult {
    private ContainerACL containerACL;

    /**
     * Gets the container's public access level and container-level access
     * policies from the headers and body returned in the response.
     * 
     * @return A {@link ContainerACL} instance representing the public access
     *         level and container-level access policies returned by the
     *         request.
     */
    public ContainerACL getContainerACL() {
        return containerACL;
    }

    /**
     * Reserved for internal use. Sets the container's public access level and
     * container-level access policies from the headers and body returned in the
     * response.
     * <p>
     * This method is invoked by the API to set the value from the Blob Service
     * REST API operation response returned by the server.
     * 
     * @param containerACL
     *            A {@link ContainerACL} instance representing the public access
     *            level and container-level access policies returned by the
     *            request.
     */
    public void setValue(ContainerACL containerACL) {
        this.containerACL = containerACL;
    }
}
