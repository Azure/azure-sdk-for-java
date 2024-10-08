// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.automation.models;

import com.azure.resourcemanager.automation.fluent.models.GraphicalRunbookContentInner;

/**
 * An immutable client-side representation of GraphicalRunbookContent.
 */
public interface GraphicalRunbookContent {
    /**
     * Gets the rawContent property: Raw graphical Runbook content.
     * 
     * @return the rawContent value.
     */
    RawGraphicalRunbookContent rawContent();

    /**
     * Gets the graphRunbookJson property: Graphical Runbook content as JSON.
     * 
     * @return the graphRunbookJson value.
     */
    String graphRunbookJson();

    /**
     * Gets the inner com.azure.resourcemanager.automation.fluent.models.GraphicalRunbookContentInner object.
     * 
     * @return the inner object.
     */
    GraphicalRunbookContentInner innerModel();
}
