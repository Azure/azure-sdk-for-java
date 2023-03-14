package com.azure.cosmos.implementation.Json;

import java.nio.ByteBuffer;
import java.util.UUID;

public interface IJsonNavigator
{
    /// <summary>
    /// Gets the <see ref="JsonSerializationFormat"/> for the IJsonNavigator.
    /// </summary>
    JsonSerializationFormat getSerializationFormat();

    /// <summary>
    /// Gets the root node.
    /// </summary>
    /// <returns>The root node.</returns>
    IJsonNavigatorNode getRootNode();

    /// <summary>
    /// Gets the <see ref="JsonNodeType"/> type for a particular node
    /// </summary>
    /// <param name="node">The node you want to know the type of</param>
    /// <returns><see ref="JsonNodeType"/> for the node</returns>
    JsonNodeType getNodeType(IJsonNavigatorNode node);

    /// <summary>
    /// Gets the numeric value for a node
    /// </summary>
    /// <param name="numberNode">The node you want the number value from.</param>
    /// <returns>A double that represents the number value in the node.</returns>
    long getNumber64Value(IJsonNavigatorNode numberNode);

    /// <summary>
    /// Tries to get the buffered string value from a node.
    /// </summary>
    /// <param name="stringNode">The node to get the buffered string from.</param>
    /// <param name="value">The buffered string value if possible</param>
    /// <returns><code>true</code> if the JsonNavigator successfully got the buffered string value; <code>false</code> if the JsonNavigator failed to get the buffered string value.</returns>
    boolean tryGetBufferedStringValue(IJsonNavigatorNode stringNode, Utf8Memory value);

    /// <summary>
    /// Gets a string value from a node.
    /// </summary>
    /// <param name="stringNode">The node to get the string value from.</param>
    /// <returns>The string value from the node.</returns>
    String getStringValue(IJsonNavigatorNode stringNode);

    /// <summary>
    /// Gets the numeric value for a node as a signed byte.
    /// </summary>
    /// <param name="numberNode">The node you want the number value from.</param>
    /// <returns>A byte value that represents the number value in the node.</returns>
    byte getInt8Value(IJsonNavigatorNode numberNode);

    /// <summary>
    /// Gets the numeric value for a node as a 16-bit signed integer.
    /// </summary>
    /// <param name="numberNode">The node you want the number value from.</param>
    /// <returns>A short value that represents the number value in the node.</returns>
    short getInt16Value(IJsonNavigatorNode numberNode);

    /// <summary>
    /// Gets the numeric value for a node as a 32-bit signed integer.
    /// </summary>
    /// <param name="numberNode">The node you want the number value from.</param>
    /// <returns>An int value that represents the number value in the node.</returns>
    int getInt32Value(IJsonNavigatorNode numberNode);

    /// <summary>
    /// Gets the numeric value for a node as a 64-bit signed integer.
    /// </summary>
    /// <param name="numberNode">The node you want the number value from.</param>
    /// <returns>A long value that represents the number value in the node.</returns>
    long getInt64Value(IJsonNavigatorNode numberNode);

    /// <summary>
    /// Gets the numeric value for a node as a single precision number if the number is expressed as a floating point.
    /// </summary>
    /// <param name="numberNode">The node you want the number value from.</param>
    /// <returns>A double that represents the number value in the node.</returns>
    float getFloat32Value(IJsonNavigatorNode numberNode);

    /// <summary>
    /// Gets the numeric value for a node as double precision number if the number is expressed as a floating point.
    /// </summary>
    /// <param name="numberNode">The node you want the number value from.</param>
    /// <returns>A double that represents the number value in the node.</returns>
    double getFloat64Value(IJsonNavigatorNode numberNode);

    /// <summary>
    /// Gets the numeric value for a node as an unsigned 32 bit integer if the node is expressed as an uint32.
    /// </summary>
    /// <param name="numberNode">The node you want the number value from.</param>
    /// <returns>An unsigned integer that represents the number value in the node.</returns>
    long getUInt32Value(IJsonNavigatorNode numberNode);

    /// <summary>
    /// Gets the Guid value for a node.
    /// </summary>
    /// <param name="guidNode">The node you want the guid value from.</param>
    /// <returns>A guid read from the node.</returns>
    UUID getGuidValue(IJsonNavigatorNode guidNode);

    /// <summary>
    /// Gets a binary value for a given node from the input.
    /// </summary>
    /// <param name="binaryNode">The node to get the binary value from.</param>
    /// <returns>The binary value from the node</returns>
    ByteBuffer getBinaryValue(IJsonNavigatorNode binaryNode);

    /// <summary>
    /// Tries to get the buffered binary value from a node.
    /// </summary>
    /// <param name="binaryNode">The node to get the buffered binary from.</param>
    /// <param name="bufferedBinaryValue">The buffered binary value if possible</param>
    /// <returns><code>true</code> if the JsonNavigator successfully got the buffered binary value; <code>false</code> if the JsonNavigator failed to get the buffered binary value.</returns>
    boolean tryGetBufferedBinaryValue(
        IJsonNavigatorNode binaryNode,
        ByteBuffer bufferedBinaryValue);

    /// <summary>
    /// Gets the number of elements in an array node.
    /// </summary>
    /// <param name="arrayNode">The (array) node to get the count of.</param>
    /// <returns>The number of elements in the array node.</returns>
    int getArrayItemCount(IJsonNavigatorNode arrayNode);

    /// <summary>
    /// Gets the node at a particular index of an array node
    /// </summary>
    /// <param name="arrayNode">The (array) node to index from.</param>
    /// <param name="index">The offset into the array</param>
    /// <returns>The node at a particular index of an array node</returns>
    IJsonNavigatorNode getArrayItemAt(IJsonNavigatorNode arrayNode, int index);

    /// <summary>
    /// Gets the array item nodes of the array node.
    /// </summary>
    /// <param name="arrayNode">The array to get the items from.</param>
    /// <returns>The array item nodes of the array node</returns>
    Iterable<IJsonNavigatorNode> getArrayItems(IJsonNavigatorNode arrayNode);

    /// <summary>
    /// Gets the number of properties in an object node.
    /// </summary>
    /// <param name="objectNode">The node to get the property count from.</param>
    /// <returns>The number of properties in an object node.</returns>
    int getObjectPropertyCount(IJsonNavigatorNode objectNode);

    /// <summary>
    /// Tries to get a object property from an object with a particular property name.
    /// </summary>
    /// <param name="objectNode">The object node to get a property from.</param>
    /// <param name="propertyName">The name of the property to search for.</param>
    /// <param name="objectProperty">The <see ref="ObjectProperty"/> with the specified property name if it exists.</param>
    /// <returns><code>true</code> if the JsonNavigator successfully found the <see cref="ObjectProperty"/> with the specified property name; <code>false</code> otherwise.</returns>
    boolean tryGetObjectProperty(IJsonNavigatorNode objectNode, String propertyName, ObjectProperty objectProperty);

    /// <summary>
    /// Gets the <see ref="ObjectProperty"/> properties from an object node.
    /// </summary>
    /// <param name="objectNode">The object node to get the properties from.</param>
    /// <returns>The <see ref="ObjectProperty"/> properties from an object node.</returns>
    Iterable<ObjectProperty> getObjectProperties(IJsonNavigatorNode objectNode);

    /// <summary>
    /// Creates an <see ref="IJsonReader"/> that is able to read the supplied <see cref="IIJsonNavigatorNode"/>.
    /// </summary>
    /// <param name="IJsonNavigatorNode">The node to create a reader from..</param>
    /// <returns>The <see ref="IJsonReader"/> that is able to read the supplied <see cref="IIJsonNavigatorNode"/>.</returns>
    public IJsonReader createReader(IJsonNavigatorNode IJsonNavigatorNode);

    /// <summary>
    /// Writes a <see ref="IIJsonNavigatorNode"/> to a <see ref="IJsonWriter"/>.
    /// </summary>
    /// <param name="IJsonNavigatorNode">The <see ref="IIJsonNavigatorNode"/> to write.</param>
    /// <param name="jsonWriter">The <see ref="IJsonWriter"/> to write to.</param>
    void writeNode(IJsonNavigatorNode IJsonNavigatorNode, IJsonWriter jsonWriter);
}
