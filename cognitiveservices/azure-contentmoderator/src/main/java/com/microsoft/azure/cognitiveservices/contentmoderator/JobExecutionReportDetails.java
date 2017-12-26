/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Job Execution Report Values.
 */
public class JobExecutionReportDetails {
    /**
     * Time details.
     */
    @JsonProperty(value = "Ts")
    private String ts;

    /**
     * Message details.
     */
    @JsonProperty(value = "Msg")
    private String msg;

    /**
     * Get the ts value.
     *
     * @return the ts value
     */
    public String ts() {
        return this.ts;
    }

    /**
     * Set the ts value.
     *
     * @param ts the ts value to set
     * @return the JobExecutionReportDetails object itself.
     */
    public JobExecutionReportDetails withTs(String ts) {
        this.ts = ts;
        return this;
    }

    /**
     * Get the msg value.
     *
     * @return the msg value
     */
    public String msg() {
        return this.msg;
    }

    /**
     * Set the msg value.
     *
     * @param msg the msg value to set
     * @return the JobExecutionReportDetails object itself.
     */
    public JobExecutionReportDetails withMsg(String msg) {
        this.msg = msg;
        return this;
    }

}
