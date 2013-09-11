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
 * Type containing data about affinity group.
 * 
 */
public class AffinityGroupInfo {

    /** The name. */
    private String name;

    /** The label. */
    private String label;

    /** The description. */
    private String description;

    /** The location. */
    private String location;

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the name
     * @return the affinity group info
     */
    public AffinityGroupInfo setName(String name) {
        this.name = name;
        return this;
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
     * Sets the label.
     * 
     * @param label
     *            the label
     * @return the affinity group info
     */
    public AffinityGroupInfo setLabel(String label) {
        this.label = label;
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
     * Sets the description.
     * 
     * @param description
     *            the description
     * @return the affinity group info
     */
    public AffinityGroupInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets the location.
     * 
     * @return the location
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Sets the location.
     * 
     * @param location
     *            the location
     * @return the affinity group info
     */
    public AffinityGroupInfo setLocation(String location) {
        this.location = location;
        return this;
    }

}
