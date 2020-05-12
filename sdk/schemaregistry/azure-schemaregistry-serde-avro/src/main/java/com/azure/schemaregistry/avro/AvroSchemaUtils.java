/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License.
 */

package com.azure.schemaregistry.avro;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class AvroSchemaUtils {
  private static final Map<String, Schema> primitiveSchemas;

  static {
    Schema.Parser parser = new Schema.Parser();
    primitiveSchemas = new HashMap<>();
    primitiveSchemas.put("Null", createPrimitiveSchema(parser, "null"));
    primitiveSchemas.put("Boolean", createPrimitiveSchema(parser, "boolean"));
    primitiveSchemas.put("Integer", createPrimitiveSchema(parser, "int"));
    primitiveSchemas.put("Long", createPrimitiveSchema(parser, "long"));
    primitiveSchemas.put("Float", createPrimitiveSchema(parser, "float"));
    primitiveSchemas.put("Double", createPrimitiveSchema(parser, "double"));
    primitiveSchemas.put("String", createPrimitiveSchema(parser, "string"));
    primitiveSchemas.put("Bytes", createPrimitiveSchema(parser, "bytes"));
  }

  private static Schema createPrimitiveSchema(Schema.Parser parser, String type) {
    String schemaString = String.format("{\"type\" : \"%s\"}", type);
    return parser.parse(schemaString);
  }

  public static Map<String, Schema> getPrimitiveSchemas() {
    return Collections.unmodifiableMap(primitiveSchemas);
  }

  public static Schema getSchema(Object object) {
    if (object == null) {
      return primitiveSchemas.get("Null");
    } else if (object instanceof Boolean) {
      return primitiveSchemas.get("Boolean");
    } else if (object instanceof Integer) {
      return primitiveSchemas.get("Integer");
    } else if (object instanceof Long) {
      return primitiveSchemas.get("Long");
    } else if (object instanceof Float) {
      return primitiveSchemas.get("Float");
    } else if (object instanceof Double) {
      return primitiveSchemas.get("Double");
    } else if (object instanceof CharSequence) {
      return primitiveSchemas.get("String");
    } else if (object instanceof byte[]) {
      return primitiveSchemas.get("Bytes");
    } else if (object instanceof GenericContainer) {
      return ((GenericContainer) object).getSchema();
    } else {
      throw new IllegalArgumentException(
          "Unsupported Avro type. Supported types are null, Boolean, Integer, Long, "
              + "Float, Double, String, byte[] and IndexedRecord");
    }
  }
}
