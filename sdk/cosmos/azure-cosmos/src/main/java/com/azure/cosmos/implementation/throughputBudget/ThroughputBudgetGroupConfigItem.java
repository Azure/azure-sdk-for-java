package com.azure.cosmos.implementation.throughputBudget;

import com.azure.cosmos.implementation.Constants;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.ZonedDateTime;

@JsonSerialize(using = ThroughputBudgetGroupConfigItem.ThroughputBudgetGroupConfigItemJsonSerializer.class)
public class ThroughputBudgetGroupConfigItem extends ThroughputBudgetControlContainerItem {

    private static final ZonedDateTime UNIX_START_TIME = ZonedDateTime.parse("1970-01-01T00:00:00.0Z[UTC]");
    private static final String PROPERTY_NAME_GROUP = "group";
    private static final String PROPERTY_NAME_THROUGHPUTLIMIT = "throughputLimit";
    private static final String PROPERTY_NAME_THROUGHPUTLIMITTHRESHOLD = "throughputLimitThreshold";
    private static final String PROPERTY_NAME_USEBYDEFAULT = "useByDefault";

    private final String throughputLimit;
    private final String throughputLimitThreshold;
    private final boolean useByDefault;

    public ThroughputBudgetGroupConfigItem(
        String id,
        String group,
        String throughputLimit,
        String throughputLimitThreshold,
        boolean useByDefault) {
        super(id, group);
        this.throughputLimit = throughputLimit;
        this.throughputLimitThreshold = throughputLimitThreshold;
        this.useByDefault = useByDefault;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null || getClass() != other.getClass()) {
            return false;
        }

        ThroughputBudgetGroupConfigItem that = (ThroughputBudgetGroupConfigItem) other;

        return this.getId().equals(that.getId())
            && this.getGroup().equals(that.getGroup())
            && this.throughputLimit == that.throughputLimit
            && this.throughputLimitThreshold == that.throughputLimitThreshold
            && this.useByDefault == that.useByDefault;
    }

    @Override
    public int hashCode() {
        int throughputLimitHashCode = throughputLimit == null ? 0 : throughputLimit.hashCode();
        int throughputLimitThresholdHashCode = throughputLimitThreshold == null ? 0 : throughputLimitThreshold.hashCode();
        return (397 * this.getId().hashCode())
            ^ this.getGroup().hashCode()
            ^ String.valueOf(this.useByDefault).hashCode()
            ^ throughputLimitHashCode
            ^ throughputLimitThresholdHashCode;
    }

    static final class ThroughputBudgetGroupConfigItemJsonSerializer extends StdSerializer<ThroughputBudgetGroupConfigItem> {
        // this value should be incremented if changes are made to the ServiceItemLease class members
        private static final long serialVersionUID = 1L;

        protected ThroughputBudgetGroupConfigItemJsonSerializer() { this(null); }

        protected ThroughputBudgetGroupConfigItemJsonSerializer(Class<ThroughputBudgetGroupConfigItem> t) {
            super(t);
        }

        @Override
        public void serialize(ThroughputBudgetGroupConfigItem item, JsonGenerator writer, SerializerProvider serializerProvider) {
            try {
                writer.writeStartObject();
                writer.writeStringField(Constants.Properties.ID, item.getId());
                writer.writeStringField(Constants.Properties.E_TAG, item.getEtag());
                writer.writeStringField(PROPERTY_NAME_GROUP, item.getGroup());
                writer.writeStringField(PROPERTY_NAME_THROUGHPUTLIMIT, String.valueOf(item.throughputLimit));
                writer.writeStringField(PROPERTY_NAME_THROUGHPUTLIMITTHRESHOLD, String.valueOf(item.throughputLimitThreshold));
                writer.writeStringField(PROPERTY_NAME_USEBYDEFAULT, String.valueOf(item.useByDefault));
                writer.writeEndObject();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
