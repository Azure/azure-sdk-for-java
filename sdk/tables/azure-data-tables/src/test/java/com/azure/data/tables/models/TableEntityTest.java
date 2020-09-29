// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.tables.models;

import com.azure.data.tables.implementation.TablesConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TableEntityTest {

    @Test
    public void testConvertToSubclass() {
        byte[] bytes = new byte[]{1,2,3};
        boolean b = true;
        OffsetDateTime dateTime = OffsetDateTime.now();
        double d = 1.23D;
        UUID uuid = UUID.randomUUID();
        int i = 123;
        long l = 123L;
        String s = "Test";

        Map<String, Object> props = new HashMap<>();
        props.put(TablesConstants.PARTITION_KEY, "abc");
        props.put(TablesConstants.ROW_KEY, "def");
        props.put("ByteField", bytes);
        props.put("BooleanField", b);
        props.put("DateTimeField", dateTime);
        props.put("DoubleField", d);
        props.put("UuidField", uuid);
        props.put("IntField", i);
        props.put("LongField", l);
        props.put("StringField", s);

        TableEntity entity = new TableEntity(props);
        SampleEntity result = entity.convertToSubclass(SampleEntity.class);
        Assertions.assertEquals(bytes, result.getByteField());
        Assertions.assertEquals(b, result.getBooleanField());
        Assertions.assertEquals(dateTime, result.getDateTimeField());
        Assertions.assertEquals(d, result.getDoubleField());
        Assertions.assertEquals(uuid, result.getUuidField());
        Assertions.assertEquals(i, result.getIntField());
        Assertions.assertEquals(l, result.getLongField());
        Assertions.assertEquals(s, result.getStringField());
    }

    @Test
    public void testSetPropertiesFromGetters() {
        byte[] bytes = new byte[]{1,2,3};
        boolean b = true;
        OffsetDateTime dateTime = OffsetDateTime.now();
        double d = 1.23D;
        UUID uuid = UUID.randomUUID();
        int i = 123;
        long l = 123L;
        String s = "Test";

        SampleEntity entity = new SampleEntity("abc", "def");
        entity.setByteField(bytes);
        entity.setBooleanField(b);
        entity.setDateTimeField(dateTime);
        entity.setDoubleField(d);
        entity.setUuidField(uuid);
        entity.setIntField(i);
        entity.setLongField(l);
        entity.setStringField(s);

        entity.setPropertiesFromGetters();

        Assertions.assertEquals(entity.getProperties().get("ByteField"), bytes);
        Assertions.assertEquals(entity.getProperties().get("BooleanField"), b);
        Assertions.assertEquals(entity.getProperties().get("DateTimeField"), dateTime);
        Assertions.assertEquals(entity.getProperties().get("DoubleField"), d);
        Assertions.assertEquals(entity.getProperties().get("UuidField"), uuid);
        Assertions.assertEquals(entity.getProperties().get("IntField"), i);
        Assertions.assertEquals(entity.getProperties().get("LongField"), l);
        Assertions.assertEquals(entity.getProperties().get("StringField"), s);
    }
}
