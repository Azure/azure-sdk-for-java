/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.protocol.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Specifies characteristics for a temporary 'auto pool'. The Batch service
 * will create this auto pool, run all the tasks for the job on it, and will
 * delete the pool once the job has completed.
 */
public class AutoPoolSpecification {
    /**
     * A prefix to be added to the unique identifier when a pool is
     * automatically created. The prefix can be up to 20 characters long.
     */
    private String autoPoolIdPrefix;

    /**
     * The minimum lifetime of created auto pools, and how multiple jobs on a
     * schedule are assigned to pools. Possible values include:
     * 'jobschedule', 'job', 'unmapped'.
     */
    @JsonProperty(required = true)
    private PoolLifetimeOption poolLifetimeOption;

    /**
     * Whether to keep an auto pool alive after its lifetime expires.
     */
    private Boolean keepAlive;

    /**
     * The pool specification for the auto pool.
     */
    private PoolSpecification pool;

    /**
     * Get the autoPoolIdPrefix value.
     *
     * @return the autoPoolIdPrefix value
     */
    public String autoPoolIdPrefix() {
        return this.autoPoolIdPrefix;
    }

    /**
     * Set the autoPoolIdPrefix value.
     *
     * @param autoPoolIdPrefix the autoPoolIdPrefix value to set
     * @return the AutoPoolSpecification object itself.
     */
    public AutoPoolSpecification withAutoPoolIdPrefix(String autoPoolIdPrefix) {
        this.autoPoolIdPrefix = autoPoolIdPrefix;
        return this;
    }

    /**
     * Get the poolLifetimeOption value.
     *
     * @return the poolLifetimeOption value
     */
    public PoolLifetimeOption poolLifetimeOption() {
        return this.poolLifetimeOption;
    }

    /**
     * Set the poolLifetimeOption value.
     *
     * @param poolLifetimeOption the poolLifetimeOption value to set
     * @return the AutoPoolSpecification object itself.
     */
    public AutoPoolSpecification withPoolLifetimeOption(PoolLifetimeOption poolLifetimeOption) {
        this.poolLifetimeOption = poolLifetimeOption;
        return this;
    }

    /**
     * Get the keepAlive value.
     *
     * @return the keepAlive value
     */
    public Boolean keepAlive() {
        return this.keepAlive;
    }

    /**
     * Set the keepAlive value.
     *
     * @param keepAlive the keepAlive value to set
     * @return the AutoPoolSpecification object itself.
     */
    public AutoPoolSpecification withKeepAlive(Boolean keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    /**
     * Get the pool value.
     *
     * @return the pool value
     */
    public PoolSpecification pool() {
        return this.pool;
    }

    /**
     * Set the pool value.
     *
     * @param pool the pool value to set
     * @return the AutoPoolSpecification object itself.
     */
    public AutoPoolSpecification withPool(PoolSpecification pool) {
        this.pool = pool;
        return this;
    }

}
