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

package com.microsoft.windowsazure.services.management.models;

/**
 * The base result class for all the result of service management operation.
 * 
 */
public class UpdateAffinityGroupOptions {

    /** The description. */
    private String description;

    /** The name. */
    private final String name;

    /** The label. */
    private final String label;

    /**
     * Instantiates a new update affinity group options.
     * 
     * @param name
     *            the name
     * @param label
     *            the label
     */
    public UpdateAffinityGroupOptions(String name, String label) {
        this.name = name;
        this.label = label;
    }

    /**
     * Sets the description.
     * 
     * @param description
     *            the description
     * @return the update affinity group options
     */
    public UpdateAffinityGroupOptions setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the label.
     * 
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

}
