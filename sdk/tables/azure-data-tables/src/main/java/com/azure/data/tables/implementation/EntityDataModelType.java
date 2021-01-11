// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables.implementation;

import com.azure.core.util.Base64Util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Entity Data Model (EDM) types
 */
public enum EntityDataModelType {
    /**
     * <strong>Edm.Binary</strong> Represents fixed- or variable-length binary data
     */
    BINARY("Edm.Binary", Base64Util::decodeString, byte[].class, Byte[].class),

    /**
     * <strong>Edm.Boolean</strong> Represents the mathematical concept of binary-valued logic
     */
    BOOLEAN("Edm.Boolean", Boolean::parseBoolean),

    /**
     * <strong>Edm.DateTime</strong> Represents date and time with values ranging from 12:00:00 midnight, January 1,
     * 1753 A.D. through 11:59:59 P.M, December 9999 A.D.
     */
    DATE_TIME("Edm.DateTime", s ->
        DateTimeFormatter.ISO_DATE_TIME.parseBest(s, OffsetDateTime::from, LocalDateTime::from),
        OffsetDateTime.class, ZonedDateTime.class, Instant.class, LocalDateTime.class, LocalDate.class, Date.class),

    /**
     * <strong>Edm.Double</strong> Represents a floating point number with 15 digits precision that can represent values
     * with approximate range of +/- 2.23e -308 through +/- 1.79e +308
     */
    DOUBLE("Edm.Double", Double::parseDouble),

    /**
     * <strong>Edm.Guid</strong> Represents a 16-byte (128-bit) unique identifier value
     */
    GUID("Edm.Guid", UUID::fromString, UUID.class),

    /**
     * <strong>Edm.Int32</strong> Represents a signed 32-bit integer value
     */
    INT32("Edm.Int32", Integer::parseInt),

    /**
     * <strong>Edm.Int64</strong> Represents a signed 64-bit integer value
     */
    INT64("Edm.Int64", Long::parseLong, long.class, Long.class),

    /**
     * <strong>Edm.String</strong> Represents fixed- or variable-length character data
     */
    STRING("Edm.String", String::toString);

    private final String edmType;
    private final Function<String, Object> deserializer;
    private final Set<Class<?>> typesToAnnotate;

    EntityDataModelType(String edmType, Function<String, Object> deserializer, Class<?>... typesToAnnotate) {
        this.edmType = edmType;
        this.deserializer = deserializer;

        if (typesToAnnotate.length == 0) {
            this.typesToAnnotate = Collections.emptySet();
        } else if (typesToAnnotate.length == 1) {
            this.typesToAnnotate = Collections.singleton(typesToAnnotate[0]);
        } else {
            this.typesToAnnotate = Arrays.stream(typesToAnnotate).collect(Collectors.toCollection(HashSet::new));
        }
    }

    public static EntityDataModelType fromString(String edmType) {
        for (EntityDataModelType value : EntityDataModelType.values()) {
            if (value.edmType.equals(edmType)) {
                return value;
            }
        }
        return null;
    }

    public static EntityDataModelType forClass(Class<?> clazz) {
        for (EntityDataModelType value : EntityDataModelType.values()) {
            if (value.typesToAnnotate.contains(clazz)) {
                return value;
            }
        }
        return null;
    }

    public String getEdmType() {
        return edmType;
    }

    public Object deserialize(String value) {
        return deserializer.apply(value);
    }
}
