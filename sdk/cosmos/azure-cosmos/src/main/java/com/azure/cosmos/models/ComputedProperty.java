// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a computed property definition for a Cosmos DB container.
 *
 * Below is an example of how to use {@link ComputedProperty} in the context of creating a container.
 * <!-- src_embed com.azure.cosmos.computedProperty.createContainer -->
 * <pre>
 * List&lt;ComputedProperty&gt; computedProperties = new ArrayList&lt;&gt;&#40;
 *         Arrays.asList&#40;
 *                 new ComputedProperty&#40;&quot;lowerName&quot;, &quot;SELECT VALUE LOWER&#40;c.name&#41; FROM c&quot;&#41;
 *         &#41;
 * &#41;;
 * containerProperties.setComputedProperties&#40;computedProperties&#41;;
 * database.createContainer&#40;containerProperties&#41;.subscribe&#40;&#41;;
 * </pre>
 * <!-- end com.azure.cosmos.computedProperty.createContainer -->
 *
 * Below is an example of how to use {@link ComputedProperty} in the context of replacing a container.
 * <!-- src_embed com.azure.cosmos.computedProperty.replaceContainer -->
 * <pre>
 * CosmosContainerProperties containerProperties = getCollectionDefinition&#40;containerName&#41;;
 * List&lt;ComputedProperty&gt; computedProperties = new ArrayList&lt;&gt;&#40;
 *         Arrays.asList&#40;
 *                 new ComputedProperty&#40;&quot;upperName&quot;, &quot;SELECT VALUE UPPER&#40;c.name&#41; FROM c&quot;&#41;
 *         &#41;
 * &#41;;
 * containerProperties.setComputedProperties&#40;computedProperties&#41;;
 * container = database.getContainer&#40;containerName&#41;;
 * container.replace&#40;containerProperties&#41;.subscribe&#40;&#41;;
 * </pre>
 * <!-- end com.azure.cosmos.computedProperty.replaceContainer -->
 */
public final class ComputedProperty {
    private final String name;
    private final String query;

    /**
     * Instantiates a new Computed properties with name and query.
     * @param name the name of the computed property.
     * @param query the query used to evaluate the value for the computed property.
     */
    @JsonCreator
    public ComputedProperty(@JsonProperty("name") String name, @JsonProperty("query") String query) {
        this.name = name;
        this.query = query;
    }

    /**
     * Gets the name of the computed property.
     *
     * @return the name of the computed property.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the query used to evaluate the value for the computed property.
     *
     * @return the query for the computed property.
     */
    public String getQuery() {
        return query;
    }
}
