package com.azure.cosmos.implementation.Json;

public final class ObjectProperty {

    private final IJsonNavigatorNode nameNode;
    private final IJsonNavigatorNode valueNode;
    /// <summary>
    /// Initializes a new instance of the ObjectProperty class.
    /// </summary>
    /// <param name="nameNode">The IJsonNavigatorNode to the node that holds the object property name.</param>
    /// <param name="valueNode">The IJsonNavigatorNode to the node that holds the object property value.</param>
    public ObjectProperty(IJsonNavigatorNode nameNode, IJsonNavigatorNode valueNode)
    {
        this.nameNode = nameNode;
        this.valueNode = valueNode;
    }
    /// <summary>
    /// The node that holds the object property name.
    /// </summary>
    public final IJsonNavigatorNode getNameNode(){
        return this.nameNode;
    }

    /// <summary>
    /// The node that holds the object property value.
    /// </summary>
    public final IJsonNavigatorNode getValueNode(){
        return this.valueNode;
    }
}
