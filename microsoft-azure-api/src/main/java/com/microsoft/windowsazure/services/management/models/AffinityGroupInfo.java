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
    private String name;
    private String label;
    private String description;
    private String location;

    public String getName() {
        return this.name;
    }

    public AffinityGroupInfo setName(String name) {
        this.name = name;
        return this;
    }

    public String getLabel() {
        return this.label;
    }

    public AffinityGroupInfo setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getDescription() {
        return this.description;
    }

    public AffinityGroupInfo setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getLocation() {
        return this.location;
    }

    public AffinityGroupInfo setLocation(String location) {
        this.location = location;
        return this;
    }
}
