package com.microsoft.windowsazure.services.core.storage;

import java.util.EnumSet;

/**
 * Represents the logging properties for the analytics service.
 * 
 * Copyright (c)2011 Microsoft. All rights reserved.
 */
public final class LoggingProperties {

    /**
     * The analytics version to use.
     */
    private String version = "1.0";

    /**
     * A EnumSet of <code>LoggingOperationTypes</code> that represent which storage operations should be logged.
     */
    private EnumSet<LoggingOperations> logOperationTypes = EnumSet.noneOf(LoggingOperations.class);

    /**
     * The Retention policy for the logging data.
     */
    private Integer retentionIntervalInDays;

    /**
     * @return the logOperationTypes
     */
    public EnumSet<LoggingOperations> getLogOperationTypes() {
        return this.logOperationTypes;
    }

    /**
     * @return the retentionIntervalInDays
     */
    public Integer getRetentionIntervalInDays() {
        return this.retentionIntervalInDays;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * @param logOperationTypes
     *            the logOperationTypes to set
     */
    public void setLogOperationTypes(final EnumSet<LoggingOperations> logOperationTypes) {
        this.logOperationTypes = logOperationTypes;
    }

    /**
     * @param retentionIntervalInDays
     *            the retentionIntervalInDays to set
     */
    public void setRetentionIntervalInDays(final Integer retentionIntervalInDays) {
        this.retentionIntervalInDays = retentionIntervalInDays;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(final String version) {
        this.version = version;
    }
}
