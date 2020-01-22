// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.serializer.jsonwrapper.api;

import java.util.List;

public interface Node {

    /**
     * get value
     * @param name name of node to get
     * @return Node
     */
    Node get(String name);

    /**
     * Node to String
     * @return string
     */
    String asString();

    /**
     * Node to int
     * @return int
     */
    int asInt();

    /**
     * Node to double
     * @return double
     */
    double asDouble();

    /**
     * Node to boolean
     * @return boolean
     */
    boolean asBoolean();

    /**
     * Is the node a json array
     * @return boolean value
     */
    boolean isJsonArray();

    /**
     * Is the node an object
     * @return boolean value
     */
    boolean isJsonObject();

    /**
     * Is the node primitive type
     * @return boolean value
     */
    boolean isJsonPrimitive();

    /**
     * return the list of elements constructing a nested node
     * @return List of nodes
     */
    List<Node> getElements();
}
