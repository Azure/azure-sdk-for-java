// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.json;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

/**
 * Class to hold the property name and property value for an object property.
 */
class ObjectProperty {

    /**
     * Initializes a new instance of the ObjectProperty class.
     *
     * @param nameNode The IJsonNavigatorNode to the node that holds the object property name.
     * @param valueNode The IJsonNavigatorNode to the node that holds the object property value.
     */
    public ObjectProperty(IJsonNavigatorNode nameNode, IJsonNavigatorNode valueNode) {

        checkNotNull(nameNode, "Argument 'nameNode' must not be null.");
        checkNotNull(valueNode, "Argument 'valueNode' must not be null.");

        this.nameNode = nameNode;
        this.valueNode = valueNode;
    }

    /**
     * The node that holds the object property name.
     *
     * @return The IJsonNavigatorNode that holds the object property name.
     */
    public IJsonNavigatorNode getNameNode() {
        return nameNode;
    }

    /**
     * The node that holds the object property value.
     *
     * @return The IJsonNavigatorNode that holds the object property value.
     */
    public IJsonNavigatorNode getValueNode() {
        return valueNode;
    }

    private final IJsonNavigatorNode nameNode;
    private final IJsonNavigatorNode valueNode;
}
