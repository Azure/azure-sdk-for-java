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
 * The options to create affinity group.
 * 
 */
public class CreateAffinityGroupOptions {

    /** The descrption. */
    private String descrption;

    /** The name. */
    private final String name;

    /** The label. */
    private final String label;

    /** The location. */
    private final String location;

    /**
     * Instantiates a new creates the affinity group options.
     * 
     * @param name
     *            the name
     * @param label
     *            the label
     * @param location
     *            the location
     */
    public CreateAffinityGroupOptions(String name, String label, String location) {
        this.name = name;
        this.label = label;
        this.location = location;
    }

    /**
     * Sets the description.
     * 
     * @param description
     *            the description
     * @return the creates the affinity group options
     */
    public CreateAffinityGroupOptions setDescription(String description) {
        this.descrption = description;
        return this;
    }

    /**
     * Gets the description.
     * 
     * @return the description
     */
    public String getDescription() {
        return this.descrption;
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

    /**
     * Gets the location.
     * 
     * @return the location
     */
    public String getLocation() {
        return this.location;
    }

}
