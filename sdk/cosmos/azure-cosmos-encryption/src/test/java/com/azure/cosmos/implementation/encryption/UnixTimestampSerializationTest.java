package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.guava27.Strings;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class UnixTimestampSerializationTest {
    public static class TestDoc {
        @JsonProperty("time")
        @JsonSerialize(using = UnixTimestampSerializer.class)
        @JsonDeserialize(using = UnixTimestampDeserializer.class)
        public Instant time;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestDoc testDoc = (TestDoc) o;
            return Objects.equals(time, testDoc.time);
        }

        @Override
        public int hashCode() {
            return Objects.hash(time);
        }
    }

    @Test(groups = "unit")
    public void serialization() throws Exception {
        TestDoc testDoc = new TestDoc();
        int epochSeconds = 1587157090;
        testDoc.time = Instant.ofEpochSecond(epochSeconds);

        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(testDoc);
        assertThat(json).isEqualTo(Strings.lenientFormat("{\"time\":%s}", epochSeconds));

        TestDoc deserializedTestDoc = objectMapper.readValue(json, TestDoc.class);
        assertThat(deserializedTestDoc).isEqualTo(testDoc);
    }
}
