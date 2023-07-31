// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.json;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Interface for JsonNavigators.
 */
interface IJsonNavigator {

    /**
     * Gets the {@link JsonSerializationFormat} for the {@link IJsonNavigator}.
     *
     * @return The {@link JsonSerializationFormat} for the {@link IJsonNavigator}.
     */
    JsonSerializationFormat getSerializationFormat();

    /**
     * Gets the root node.
     *
     * @return The root node.
     */
    IJsonNavigatorNode getRootNode();

    /**
     * Gets the {@link JsonNodeType} type for a particular node.
     *
     * @param node The node you want to know the type of.
     * @return The {@link JsonNodeType} for the node.
     */
    JsonNodeType getNodeType(IJsonNavigatorNode node);

    /**
     * Gets the numeric value for a node.
     *
     * @param numberNode The node you want the number value from.
     * @return A double that represents the number value in the node.
     */
    Number64 getNumber64Value(IJsonNavigatorNode numberNode);

    /**
     * Tries to get the buffered string value from a node.
     *
     * @param stringNode The node to get the buffered string from.
     * @param value      The buffered string value if possible.
     * @return {@code true} if the JsonNavigator successfully got the buffered string value; {@code false} if the JsonNavigator failed to get the buffered string value.
     */
    boolean tryGetBufferedStringValue(IJsonNavigatorNode stringNode, Utf8Memory value);

    /**
     * Gets a string value from a node.
     *
     * @param stringNode The node to get the string value from.
     * @return The string value from the node.
     */
    UtfAnyString getStringValue(IJsonNavigatorNode stringNode);

    /**
     * Gets the numeric value for a node as a signed byte.
     *
     * @param numberNode The node you want the number value from.
     * @return A {@code byte} value that represents the number value in the node.
     */
    byte getInt8Value(IJsonNavigatorNode numberNode);

    /**
     * Gets the numeric value for a node as a 16-bit signed integer.
     *
     * @param numberNode The node you want the number value from.
     * @return A {@code short} value that represents the number value in the node.
     */
    short getInt16Value(IJsonNavigatorNode numberNode);

    /**
     * Gets the numeric value for a node as a 32-bit signed integer.
     *
     * @param numberNode The node you want the number value from.
     * @return An {@code int} value that represents the number value in the node.
     */
    int getInt32Value(IJsonNavigatorNode numberNode);

    /**
     * Gets the numeric value for a node as a single precision number if the number is expressed as a floating point.
     *
     * @param numberNode The node you want the number value from.
     * @return A {@code float} that represents the number value in the node.
     */
    float getFloat32Value(IJsonNavigatorNode numberNode);

    /**
     * Gets the numeric value for a node as double precision number if the number is expressed as a floating point.
     *
     * @param numberNode The node you want the number value from.
     * @return A {@code double} that represents the number value in the node.
     */
    double getFloat64Value(IJsonNavigatorNode numberNode);

    /**
     * Gets the GUID value for a node.
     *
     * @param guidNode The node you want the GUID value from.
     * @return A GUID read from the node.
     */
    UUID getGuidValue(IJsonNavigatorNode guidNode); //todo: should all GUID references be renamed to UUID?

    /**
     * Gets a binary value for a given node from the input.
     *
     * @param binaryNode The node to get the binary value from.
     * @return The binary value from the node.
     */
    ByteBuffer getBinaryValue(IJsonNavigatorNode binaryNode);

    /**
     * Tries to get the buffered binary value from a node.
     *
     * @param binaryNode          The node to get the buffered binary from.
     * @param bufferedBinaryValue The buffered binary value if possible.
     * @return {@code true} if the JsonNavigator successfully got the buffered binary value; {@code false} if the JsonNavigator failed to get the buffered binary value.
     */
    boolean tryGetBufferedBinaryValue(IJsonNavigatorNode binaryNode, ByteBuffer bufferedBinaryValue);

    /**
     * Gets the number of elements in an array node.
     *
     * @param arrayNode The (array) node to get the count of.
     * @return The number of elements in the array node.
     */
    int getArrayItemCount(IJsonNavigatorNode arrayNode);

    /**
     * Gets the node at a particular index of an array node.
     *
     * @param arrayNode The (array) node to index from.
     * @param index     The offset into the array.
     * @return The node at a particular index of an array node.
     */
    IJsonNavigatorNode getArrayItemAt(IJsonNavigatorNode arrayNode, int index);

    /**
     * Gets the array item nodes of the array node.
     *
     * @param arrayNode The array to get the items from.
     * @return The array item nodes of the array node.
     */
    Iterable<IJsonNavigatorNode> getArrayItems(IJsonNavigatorNode arrayNode);

    /**
     * Gets the number of properties in an object node.
     *
     * @param objectNode The node to get the property count from.
     * @return The number of properties in an object node.
     */
    int getObjectPropertyCount(IJsonNavigatorNode objectNode);

    /**
     * Tries to get an object property from an object with a particular property name.
     *
     * @param objectNode    The object node to get a property from.
     * @param propertyName  The name of the property to search for.
     * @param objectProperty The {@link ObjectProperty} with the specified property name if it exists.
     * @return {@code true} if the JsonNavigator successfully found the {@link ObjectProperty} with the specified property name; {@code false} otherwise.
     */
    boolean tryGetObjectProperty(IJsonNavigatorNode objectNode, String propertyName, ObjectProperty objectProperty);

    /**
     * Gets the {@link ObjectProperty} properties from an object node.
     *
     * @param objectNode The object node to get the properties from.
     * @return The {@link ObjectProperty} properties from an object node.
     */
    Iterable<ObjectProperty> getObjectProperties(IJsonNavigatorNode objectNode);

    /**
     * Creates an {@link IJsonReader} that is able to read the supplied {@link IJsonNavigatorNode}.
     *
     * @param jsonNavigatorNode The node to create a reader from.
     * @return The {@link IJsonReader} that is able to read the supplied {@link IJsonNavigatorNode}.
     */
    IJsonReader createReader(IJsonNavigatorNode jsonNavigatorNode);

    /**
     * Writes a {@link IJsonNavigatorNode} to a {@link IJsonWriter}.
     *
     * @param jsonNavigatorNode The {@link IJsonNavigatorNode} to write.
     * @param jsonWriter        The {@link IJsonWriter} to write to.
     */
    void writeNode(IJsonNavigatorNode jsonNavigatorNode, IJsonWriter jsonWriter);
}
