/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry;

import java.io.ByteArrayOutputStream;

public interface ByteEncoder extends Codec {
    /**
     * Return schema name for storing in registry store
     * @param object Schema object
     * Refer to Schema Registry documentation for information on schema grouping and naming.
     *
     * @return schema name
     * @throws SerializationException
     */
    public String getSchemaName(Object object) throws SerializationException;

    /**
     * Returns string representation of schema object to be stored in the service.
     *
     * @param object Schema object used to generate schema string
     * @return String representation of schema object parameter
     * @throws SerializationException
     */
    public String getSchemaString(Object object) throws SerializationException;

    /**
     * Converts object into stream containing the encoded representation of the object.
     * @param object Object to be encoded into byte stream
     *
     * TODO: Method does not currently require schema object to be passed since schemas can be derived from
     *              Avro objects. JSON implementation would be the same.
     */
    public ByteArrayOutputStream encode(Object object) throws SerializationException;
}
