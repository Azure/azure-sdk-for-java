/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An array of face groups based on face similarity.
 */
public class GroupResponseInner {
    /**
     * A partition of the original faces based on face similarity. Groups are
     * ranked by number of faces.
     */
    @JsonProperty(value = "groups", required = true)
    private List<List<String>> groups;

    /**
     * Face ids array of faces that cannot find any similar faces from original
     * faces.
     */
    @JsonProperty(value = "messyGroup")
    private List<String> messyGroup;

    /**
     * Get the groups value.
     *
     * @return the groups value
     */
    public List<List<String>> groups() {
        return this.groups;
    }

    /**
     * Set the groups value.
     *
     * @param groups the groups value to set
     * @return the GroupResponseInner object itself.
     */
    public GroupResponseInner withGroups(List<List<String>> groups) {
        this.groups = groups;
        return this;
    }

    /**
     * Get the messyGroup value.
     *
     * @return the messyGroup value
     */
    public List<String> messyGroup() {
        return this.messyGroup;
    }

    /**
     * Set the messyGroup value.
     *
     * @param messyGroup the messyGroup value to set
     * @return the GroupResponseInner object itself.
     */
    public GroupResponseInner withMessyGroup(List<String> messyGroup) {
        this.messyGroup = messyGroup;
        return this;
    }

}
