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

/**
 * Represents a local storage resource reserved for a service.
 */
public final class LocalResource {

    /**
     * maximum size in megabytes allocated for the local storage resource, as
     * defined in the service
     */
    private int maximumSizeInMegabytes;

    /**
     * name of the local store as declared in the service definition file.
     */
    private String name;

    /**
     * full directory path to the local storage resource.
     */
    private String rootPath;

    /**
     * Package accessible constructor
     * 
     * @param maximumSizeInMegabytes
     * @param name
     * @param rootPath
     */
    LocalResource(int maximumSizeInMegabytes, String name, String rootPath) {
        this.maximumSizeInMegabytes = maximumSizeInMegabytes;
        this.name = name;
        this.rootPath = rootPath;
    }

    /**
     * Returns the maximum size, in megabytes, allocated for the local storage
     * resource, as defined in the service.
     * 
     * @return The maximum size, in megabytes, allocated for the local storage
     *         resource.
     */
    public int getMaximumSizeInMegabytes() {
        return maximumSizeInMegabytes;
    }

    /**
     * Returns the name of the local store as declared in the service definition
     * file.
     * 
     * @return A <code>String</code> object that represents the name of the
     *         local store.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the full directory path to the local storage resource.
     * 
     * @return A <code>String</code> object that represents the path to the
     *         local storage resource.
     */
    public String getRootPath() {
        return rootPath;
    }

    /**
     * Returns the name, maximum size, and full directory path of the local
     * resource.
     * 
     * @return A <code>String</code> object that contains the local resource
     *         name, maximum size, and directory path.
     */
    @Override
    public String toString() {
        return name + " max:" + maximumSizeInMegabytes + "MB path:" + rootPath;
    }
}
