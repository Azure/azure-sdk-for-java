// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink;

import org.apache.kafka.connect.data.Date;
import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.data.Time;
import org.apache.kafka.connect.data.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO[GA]: Double check logic here, copied over from V1
public class StructToJsonMap {

    public static Map<String, Object> toJsonMap(Struct struct) {
        if (struct == null) {
            return null;
        }
        Map<String, Object> jsonMap = new HashMap<String, Object>(0);
        List<Field> fields = struct.schema().fields();
        for (Field field : fields) {
            String fieldName = field.name();
            Schema.Type fieldType = field.schema().type();
            String schemaName = field.schema().name();
            switch (fieldType) {
                case STRING:
                    jsonMap.put(fieldName, struct.getString(fieldName));
                    break;
                case INT32:
                    if (Date.LOGICAL_NAME.equals(schemaName) || Time.LOGICAL_NAME.equals(schemaName)) {
                        jsonMap.put(fieldName, (java.util.Date) struct.get(fieldName));
                    } else {
                        jsonMap.put(fieldName, struct.getInt32(fieldName));
                    }
                    break;
                case INT16:
                    jsonMap.put(fieldName, struct.getInt16(fieldName));
                    break;
                case INT64:
                    if (Timestamp.LOGICAL_NAME.equals(schemaName)) {
                        jsonMap.put(fieldName, (java.util.Date) struct.get(fieldName));
                    } else {
                        jsonMap.put(fieldName, struct.getInt64(fieldName));
                    }
                    break;
                case FLOAT32:
                    jsonMap.put(fieldName, struct.getFloat32(fieldName));
                    break;
                case FLOAT64:
                    jsonMap.put(fieldName, struct.getFloat64(fieldName));
                    break;
                case BOOLEAN:
                    jsonMap.put(fieldName, struct.getBoolean(fieldName));
                    break;
                case ARRAY:
                    List<Object> fieldArray = struct.getArray(fieldName);
                    if (fieldArray != null && !fieldArray.isEmpty() && fieldArray.get(0) instanceof Struct) {
                        // If Array contains list of Structs
                        List<Object> jsonArray = new ArrayList<>();
                        fieldArray.forEach(item -> {
                            jsonArray.add(toJsonMap((Struct) item));
                        });
                        jsonMap.put(fieldName, jsonArray);
                    } else {
                        jsonMap.put(fieldName, fieldArray);
                    }
                    break;
                case STRUCT:
                    jsonMap.put(fieldName, toJsonMap(struct.getStruct(fieldName)));
                    break;
                case MAP:
                    jsonMap.put(fieldName, handleMap(struct.getMap(fieldName)));
                    break;
                default:
                    jsonMap.put(fieldName, struct.get(fieldName));
                    break;
            }
        }
        return jsonMap;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> handleMap(Map<String, Object> map) {
        if (map == null) {
            return null;
        }
        Map<String, Object> cacheMap = new HashMap<>();
        map.forEach((key, value) -> {
            if (value instanceof Map) {
                cacheMap.put(key, handleMap((Map<String, Object>) value));
            } else if (value instanceof Struct) {
                cacheMap.put(key, toJsonMap((Struct) value));
            } else if (value instanceof List) {
                List<Object> list = (List<Object>) value;
                List<Object> jsonArray = new ArrayList<>();
                list.forEach(item -> {
                    if (item instanceof Struct) {
                        jsonArray.add(toJsonMap((Struct) item));
                    } else if (item instanceof Map) {
                        jsonArray.add(handleMap((Map<String, Object>) item));
                    } else {
                        jsonArray.add(item);
                    }
                });
                cacheMap.put(key, jsonArray);
            } else {
                cacheMap.put(key, value);
            }
        });
        return cacheMap;
    }
}
