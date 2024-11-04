// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import reactor.util.annotation.Nullable;

public final class IntervalMetricsKey {

    private final String ikey;

    private final String host;

    private final String causeFieldName;

    private final Object causeValue;

    static IntervalMetricsKey create(String ikey, String host, @Nullable String causeFieldName,
        @Nullable Object causeValue) {
        return new IntervalMetricsKey(ikey, host, causeFieldName, causeValue);
    }

    private IntervalMetricsKey(String ikey, String host, @Nullable String causeFieldName, @Nullable Object causeValue) {
        if (ikey == null) {
            throw new NullPointerException("Null ikey");
        }
        this.ikey = ikey;
        if (host == null) {
            throw new NullPointerException("Null host");
        }
        this.host = host;
        this.causeFieldName = causeFieldName;
        this.causeValue = causeValue;
    }

    public String getIkey() {
        return ikey;
    }

    public String getHost() {
        return host;
    }

    @Nullable
    public String getCauseFieldName() {
        return causeFieldName;
    }

    @Nullable
    public Object getCauseValue() {
        return causeValue;
    }

    @Override
    public String toString() {
        return "IntervalMetricsKey{" + "ikey=" + ikey + ", " + "host=" + host + ", " + "causeFieldName="
            + causeFieldName + ", " + "causeValue=" + causeValue + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o instanceof IntervalMetricsKey) {
            IntervalMetricsKey that = (IntervalMetricsKey) o;
            return this.ikey.equals(that.getIkey())
                && this.host.equals(that.getHost())
                && (this.causeFieldName == null
                    ? that.getCauseFieldName() == null
                    : this.causeFieldName.equals(that.getCauseFieldName()))
                && (this.causeValue == null
                    ? that.getCauseValue() == null
                    : this.causeValue.equals(that.getCauseValue()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h$ = 1;
        h$ *= 1000003;
        h$ ^= ikey.hashCode();
        h$ *= 1000003;
        h$ ^= host.hashCode();
        h$ *= 1000003;
        h$ ^= (causeFieldName == null) ? 0 : causeFieldName.hashCode();
        h$ *= 1000003;
        h$ ^= (causeValue == null) ? 0 : causeValue.hashCode();
        return h$;
    }
}
