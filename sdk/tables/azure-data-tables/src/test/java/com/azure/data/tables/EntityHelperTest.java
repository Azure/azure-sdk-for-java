// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.util.logging.ClientLogger;

public class EntityHelperTest {
    private final ClientLogger logger = new ClientLogger(EntityHelperTest.class);

    // Will not be supporting subclasses of TableEntity for the time being.
    /*@Test
    public void testConvertToSubclass() {
        byte[] bytes = new byte[]{1, 2, 3};
        boolean b = true;
        OffsetDateTime dateTime = OffsetDateTime.now();
        double d = 1.23D;
        UUID uuid = UUID.randomUUID();
        int i = 123;
        long l = 123L;
        String s = "Test";
        SampleEntity.Color color = SampleEntity.Color.GREEN;

        Map<String, Object> props = new HashMap<>();
        props.put("ByteField", bytes);
        props.put("BooleanField", b);
        props.put("DateTimeField", dateTime);
        props.put("DoubleField", d);
        props.put("UuidField", uuid);
        props.put("IntField", i);
        props.put("LongField", l);
        props.put("StringField", s);
        props.put("EnumField", color);

        TableEntity entity = new TableEntity("abc", "def");
        entity.setProperties(props);

        SampleEntity result = EntityHelper.convertToSubclass(entity, SampleEntity.class, logger);
        Assertions.assertEquals(bytes, result.getByteField());
        Assertions.assertEquals(b, result.getBooleanField());
        Assertions.assertEquals(dateTime, result.getDateTimeField());
        Assertions.assertEquals(d, result.getDoubleField());
        Assertions.assertEquals(uuid, result.getUuidField());
        Assertions.assertEquals(i, result.getIntField());
        Assertions.assertEquals(l, result.getLongField());
        Assertions.assertEquals(s, result.getStringField());
        Assertions.assertEquals(color, result.getEnumField());
    }

    @Test
    public void testSetPropertiesFromGetters() {
        byte[] bytes = new byte[]{1, 2, 3};
        boolean b = true;
        OffsetDateTime dateTime = OffsetDateTime.now();
        double d = 1.23D;
        UUID uuid = UUID.randomUUID();
        int i = 123;
        long l = 123L;
        String s = "Test";
        SampleEntity.Color color = SampleEntity.Color.GREEN;

        SampleEntity entity = new SampleEntity("abc", "def");
        entity.setByteField(bytes);
        entity.setBooleanField(b);
        entity.setDateTimeField(dateTime);
        entity.setDoubleField(d);
        entity.setUuidField(uuid);
        entity.setIntField(i);
        entity.setLongField(l);
        entity.setStringField(s);
        entity.setEnumField(color);

        EntityHelper.setPropertiesFromGetters(entity, logger);

        Assertions.assertEquals(entity.getProperties().get("ByteField"), bytes);
        Assertions.assertEquals(entity.getProperties().get("BooleanField"), b);
        Assertions.assertEquals(entity.getProperties().get("DateTimeField"), dateTime);
        Assertions.assertEquals(entity.getProperties().get("DoubleField"), d);
        Assertions.assertEquals(entity.getProperties().get("UuidField"), uuid);
        Assertions.assertEquals(entity.getProperties().get("IntField"), i);
        Assertions.assertEquals(entity.getProperties().get("LongField"), l);
        Assertions.assertEquals(entity.getProperties().get("StringField"), s);
        Assertions.assertEquals(entity.getProperties().get("EnumField"), color);
    }*/
}
