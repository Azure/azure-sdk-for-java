package com.azure.search.data.customization.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

public class DateWrapper {

    @JsonProperty(value = "field1")
    public OffsetDateTime field1;
    @JsonProperty(value = "field2")
    public List<OffsetDateTime> field2;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DateWrapper)) return false;
        DateWrapper that = (DateWrapper) o;
        return Objects.equals(field1, that.field1) &&
            Objects.equals(field2, that.field2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(field1, field2);
    }

    public DateWrapper field1(OffsetDateTime testDate) {
        field1 = testDate;
        return this;
    }

    public DateWrapper field2(List<OffsetDateTime> dates) {
        field2 = dates;
        return this;
    }
}
