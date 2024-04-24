// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.sink.idStrategy;

import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.IdStrategy;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.ProvidedInConfig;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.ProvidedInKeyStrategy;
import com.azure.cosmos.kafka.connect.implementation.sink.idstrategy.ProvidedInValueStrategy;
import org.apache.kafka.connect.data.Schema;
import org.apache.kafka.connect.data.SchemaBuilder;
import org.apache.kafka.connect.data.Struct;
import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.sink.SinkRecord;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

public class ProvidedInStrategyTest {
    protected static final int TIMEOUT = 60000;

    @DataProvider(name = "idStrategyParameterProvider")
    public static Object[][] idStrategyParameterProvider() {
        return new Object[][]{
            { new ProvidedInValueStrategy() },
            { new ProvidedInKeyStrategy() },
        };
    }

    private void returnOnKeyOrValue(
        Schema schema,
        Object ret,
        IdStrategy idStrategy,
        SinkRecord sinkRecord) {
        if (idStrategy.getClass() == ProvidedInKeyStrategy.class) {
            when(sinkRecord.keySchema()).thenReturn(schema);
            when(sinkRecord.key()).thenReturn(ret);
        } else {
            when(sinkRecord.valueSchema()).thenReturn(schema);
            when(sinkRecord.value()).thenReturn(ret);
        }
    }

    @Test(groups = { "unit" }, dataProvider = "idStrategyParameterProvider", expectedExceptions = ConnectException.class, timeOut = TIMEOUT)
    public void valueNotStructOrMapShouldFail(IdStrategy idStrategy) {
        idStrategy.configure(new HashMap<>());
        SinkRecord sinkRecord = Mockito.mock(SinkRecord.class);
        returnOnKeyOrValue(Schema.STRING_SCHEMA, "a string", idStrategy, sinkRecord);
        idStrategy.generateId(sinkRecord);
    }

    @Test(groups = { "unit" }, dataProvider = "idStrategyParameterProvider", expectedExceptions = ConnectException.class, timeOut = TIMEOUT)
    public void noIdInValueShouldFail(IdStrategy idStrategy) {
        idStrategy.configure(new HashMap<>());
        SinkRecord sinkRecord = Mockito.mock(SinkRecord.class);
        returnOnKeyOrValue(null, new HashMap<>(), idStrategy, sinkRecord);
        idStrategy.generateId(sinkRecord);
    }

    @Test(groups = { "unit" }, dataProvider = "idStrategyParameterProvider", timeOut = TIMEOUT)
    public void stringIdOnMapShouldReturn(IdStrategy idStrategy) {
        idStrategy.configure(new HashMap<>());
        SinkRecord sinkRecord = Mockito.mock(SinkRecord.class);
        returnOnKeyOrValue(
            null,
            new HashMap<String, Object>(){{ put("id", "1234567"); }},
            idStrategy,
            sinkRecord);
        assertThat("1234567").isEqualTo(idStrategy.generateId(sinkRecord));
    }

    @Test(groups = { "unit" }, dataProvider = "idStrategyParameterProvider", timeOut = TIMEOUT)
    public void nonStringIdOnMapShouldReturn(IdStrategy idStrategy) {
        idStrategy.configure(new HashMap<>());
        SinkRecord sinkRecord = Mockito.mock(SinkRecord.class);
        returnOnKeyOrValue(
            null,
            new HashMap<String, Object>(){{ put("id", 1234567); }},
            idStrategy,
            sinkRecord);
        assertThat("1234567").isEqualTo(idStrategy.generateId(sinkRecord));
    }

    @Test(groups = { "unit" }, dataProvider = "idStrategyParameterProvider", timeOut = TIMEOUT)
    public void stringIdOnStructShouldReturn(IdStrategy idStrategy) {
        idStrategy.configure(new HashMap<>());
        Schema schema = SchemaBuilder.struct()
            .field("id", Schema.STRING_SCHEMA)
            .build();

        SinkRecord sinkRecord = Mockito.mock(SinkRecord.class);
        Struct struct = new Struct(schema).put("id", "1234567");
        returnOnKeyOrValue(struct.schema(), struct, idStrategy, sinkRecord);

        assertThat("1234567").isEqualTo(idStrategy.generateId(sinkRecord));
    }

    @Test(groups = { "unit" }, dataProvider = "idStrategyParameterProvider", timeOut = TIMEOUT)
    public void structIdOnStructShouldReturn(IdStrategy idStrategy) {
        idStrategy.configure(new HashMap<>());
        Schema idSchema = SchemaBuilder.struct()
            .field("name", Schema.STRING_SCHEMA)
            .build();
        Schema schema = SchemaBuilder.struct()
            .field("id", idSchema)
            .build();
        Struct struct = new Struct(schema)
            .put("id", new Struct(idSchema).put("name", "cosmos kramer"));
        SinkRecord sinkRecord = Mockito.mock(SinkRecord.class);
        returnOnKeyOrValue(struct.schema(), struct, idStrategy, sinkRecord);

        assertThat("{\"name\":\"cosmos kramer\"}").isEqualTo(idStrategy.generateId(sinkRecord));
    }

    @Test(groups = { "unit" }, dataProvider = "idStrategyParameterProvider", timeOut = TIMEOUT)
    public void jsonPathOnStruct(IdStrategy idStrategy) {
        idStrategy.configure(new HashMap<String, Object>(){{ put(ProvidedInConfig.JSON_PATH_CONFIG, "$.id.name"); }});

        Schema idSchema = SchemaBuilder.struct()
            .field("name", Schema.STRING_SCHEMA)
            .build();
        Schema schema = SchemaBuilder.struct()
            .field("id", idSchema)
            .build();
        Struct struct = new Struct(schema)
            .put("id", new Struct(idSchema).put("name", "franz kafka"));

        SinkRecord sinkRecord = Mockito.mock(SinkRecord.class);
        returnOnKeyOrValue(struct.schema(), struct, idStrategy, sinkRecord);
        assertThat("franz kafka").isEqualTo(idStrategy.generateId(sinkRecord));
    }

    @Test(groups = { "unit" }, dataProvider = "idStrategyParameterProvider", timeOut = TIMEOUT)
    public void jsonPathOnMap(IdStrategy idStrategy) {
        idStrategy.configure(new HashMap<String, Object>(){{ put(ProvidedInConfig.JSON_PATH_CONFIG, "$.id.name"); }});

        SinkRecord sinkRecord = Mockito.mock(SinkRecord.class);
        returnOnKeyOrValue(
            null,
            new HashMap<String, Object>(){{ put("id", new HashMap<String, Object>(){{ put("name", "franz kafka"); }}); }},
            idStrategy,
            sinkRecord);
        assertThat("franz kafka").isEqualTo(idStrategy.generateId(sinkRecord));
    }

    @Test(groups = { "unit" }, dataProvider = "idStrategyParameterProvider", expectedExceptions = ConnectException.class, timeOut = TIMEOUT)
    public void invalidJsonPathThrows(IdStrategy idStrategy) {
        idStrategy.configure(new HashMap<String, Object>(){{ put(ProvidedInConfig.JSON_PATH_CONFIG, "invalid.path"); }});
        SinkRecord sinkRecord = Mockito.mock(SinkRecord.class);
        returnOnKeyOrValue(
            null,
            new HashMap<String, Object>(){{ put("id", new HashMap<String, Object>(){{ put("name", "franz kafka"); }}); }},
            idStrategy,
            sinkRecord);

        idStrategy.generateId(sinkRecord);
    }

    @Test(groups = { "unit" }, dataProvider = "idStrategyParameterProvider", expectedExceptions = ConnectException.class, timeOut = TIMEOUT)
    public void jsonPathNotExistThrows(IdStrategy idStrategy) {
        idStrategy.configure(new HashMap<String, Object>(){{ put(ProvidedInConfig.JSON_PATH_CONFIG, "$.id.not.exist"); }});
        SinkRecord sinkRecord = Mockito.mock(SinkRecord.class);
        returnOnKeyOrValue(
            null,
            new HashMap<String, Object>(){{ put("id", new HashMap<String, Object>(){{ put("name", "franz kafka"); }}); }},
            idStrategy,
            sinkRecord);

        idStrategy.generateId(sinkRecord);
    }

    @Test(groups = { "unit" }, dataProvider = "idStrategyParameterProvider", timeOut = TIMEOUT)
    public void complexJsonPath(IdStrategy idStrategy) {
        Map<String, Object> map1 = new LinkedHashMap<>();
        map1.put("id", 0);
        map1.put("name", "cosmos kramer");
        map1.put("occupation", "unknown");
        Map<String, Object> map2 = new LinkedHashMap<>();
        map2.put("id", 1);
        map2.put("name", "franz kafka");
        map2.put("occupation", "writer");

        SinkRecord sinkRecord = Mockito.mock(SinkRecord.class);
        returnOnKeyOrValue(
            null,
            new HashMap<String, Object>() {{ put("id", Arrays.asList(map1, map2)); }},
            idStrategy,
            sinkRecord);

        idStrategy.configure(new HashMap<String, Object>(){{ put(ProvidedInConfig.JSON_PATH_CONFIG, "$.id[0].name"); }});
        assertThat("cosmos kramer").isEqualTo(idStrategy.generateId(sinkRecord));

        idStrategy.configure(new HashMap<String, Object>(){{ put(ProvidedInConfig.JSON_PATH_CONFIG, "$.id[1].name"); }});
        assertThat("franz kafka").isEqualTo(idStrategy.generateId(sinkRecord));

        idStrategy.configure(new HashMap<String, Object>(){{ put(ProvidedInConfig.JSON_PATH_CONFIG, "$.id[*].id"); }});
        assertThat("[0,1]").isEqualTo(idStrategy.generateId(sinkRecord));

        idStrategy.configure(new HashMap<String, Object>(){{ put(ProvidedInConfig.JSON_PATH_CONFIG, "$.id"); }});
        assertThat("[{\"id\":0,\"name\":\"cosmos kramer\",\"occupation\":\"unknown\"},{\"id\":1,\"name\":\"franz kafka\",\"occupation\":\"writer\"}]").isEqualTo(idStrategy.generateId(sinkRecord));
    }

    @Test(groups = { "unit" }, dataProvider = "idStrategyParameterProvider", timeOut = TIMEOUT)
    public void generatedIdSanitized(IdStrategy idStrategy) {
        idStrategy.configure(new HashMap<>());
        SinkRecord sinkRecord = Mockito.mock(SinkRecord.class);
        returnOnKeyOrValue(
            null,
            new HashMap<String, Object>() {{put("id", "#my/special\\id?");}},
            idStrategy,
            sinkRecord);

        assertThat("_my_special_id_").isEqualTo(idStrategy.generateId(sinkRecord));
    }
}
