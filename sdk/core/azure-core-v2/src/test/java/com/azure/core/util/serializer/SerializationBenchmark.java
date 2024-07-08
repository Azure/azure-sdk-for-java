// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.v2.annotation.JsonFlatten;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Fork(3)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 5, time = 10)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
public class SerializationBenchmark {
    private JacksonAdapter serializer;
    private ObjectMapper mapper;
    private OuterModel simpleModel;
    private OuterModel additionalPropertiesModel;
    private OuterModel jsonAnyModel;
    private OuterModelFlatten flattenModel;

    @Setup
    public void setup() {
        this.serializer = new JacksonAdapter();
        this.mapper = new ObjectMapper();
        this.simpleModel = new OuterModel("foo", "bar", "baz", Test.PLAIN);
        this.additionalPropertiesModel = new OuterModel("foo", "bar", "baz", Test.ADDITIONAL_PROPERTIES);
        this.jsonAnyModel = new OuterModel("foo", "bar", "baz", Test.JSON_ANY);
        this.flattenModel = new OuterModelFlatten("foo", "bar", "baz");
    }

    enum Test {
        PLAIN, ADDITIONAL_PROPERTIES, JSON_ANY
    }

    class InnerModel {
        InnerModel(String foo, String bar, String baz) {
            this.foo = foo;
            this.bar = bar;
            this.baz = baz;
        }

        @JsonProperty
        private String foo;

        @JsonProperty
        private String bar;

        @JsonProperty
        private String baz;
    }

    class InnerModelAdditionalProperties {
        InnerModelAdditionalProperties(String foo, String bar, String baz) {
            this.additionalProperties = new HashMap<>();
            additionalProperties.put("foo", foo);
            additionalProperties.put("bar", bar);
            additionalProperties.put("baz", baz);
        }

        @JsonProperty
        private Map<String, String> additionalProperties;
    }

    class InnerModelJsonAny {
        InnerModelJsonAny(String foo, String bar, String baz) {
            this.any = new HashMap<>();
            any.put("foo", foo);
            any.put("bar", bar);
            any.put("baz", baz);
        }

        @JsonAnyGetter
        public Map<String, String> getAny() {
            return any;
        }

        @JsonAnySetter
        private Map<String, String> any;
    }

    class MiddleModel {
        MiddleModel(String foo, String bar, String baz, Test test) {
            if (test == Test.PLAIN) {
                this.inner = new InnerModel(foo, bar, baz);
            } else if (test == Test.ADDITIONAL_PROPERTIES) {
                this.inner = new InnerModelAdditionalProperties(foo, bar, baz);
            } else if (test == Test.JSON_ANY) {
                this.inner = new InnerModelJsonAny(foo, bar, baz);
            }
        }

        @JsonProperty()
        private Object inner;
    }

    class OuterModel {
        OuterModel(String foo, String bar, String baz, Test test) {
            this.middle = new MiddleModel(foo, bar, baz, test);
        }

        @JsonProperty()
        private MiddleModel middle;
    }

    @JsonFlatten
    class OuterModelFlatten {
        OuterModelFlatten(String foo, String bar, String baz) {
            this.foo = foo;
            this.bar = bar;
            this.baz = baz;
        }

        @JsonProperty("middle.inner.foo")
        private String foo;

        @JsonProperty("middle.inner.bar")
        private String bar;

        @JsonProperty("middle.inner.baz")
        private String baz;
    }

    @Benchmark
    public void simpleModelObjectMapperSerializeJson(Blackhole blackhole) throws IOException {
        blackhole.consume(this.mapper.writeValueAsString(this.simpleModel));
    }

    @Benchmark
    public void simpleModelJacksonAdapterSerializeJson(Blackhole blackhole) throws IOException {
        blackhole.consume(this.serializer.serialize(this.simpleModel, SerializerEncoding.JSON));
    }

    @Benchmark
    public void additionalPropertiesJacksonAdapterSerializeJson(Blackhole blackhole) throws IOException {
        blackhole.consume(this.serializer.serialize(this.additionalPropertiesModel, SerializerEncoding.JSON));
    }

    @Benchmark
    public void flattenJacksonAdapterSerializeJson(Blackhole blackhole) throws IOException {
        blackhole.consume(this.serializer.serialize(this.flattenModel, SerializerEncoding.JSON));
    }

    @Benchmark
    public void jsonAnyJacksonAdapterSerializeJson(Blackhole blackhole) throws IOException {
        blackhole.consume(this.serializer.serialize(this.jsonAnyModel, SerializerEncoding.JSON));
    }

    @Benchmark
    public void simpleModelJacksonAdapterSerializeXml(Blackhole blackhole) throws IOException {
        blackhole.consume(this.serializer.serialize(this.simpleModel, SerializerEncoding.XML));
    }

    public static void main(String... args) throws IOException, RunnerException {
        Main.main(args);
    }
}
