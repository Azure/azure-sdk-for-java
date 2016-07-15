/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.batch.interceptor;

import com.microsoft.azure.batch.BatchClientBehavior;

public class BatchClientParallelOptions extends BatchClientBehavior {

    private int maxDegreeOfParallelism;

    /// <summary>
    /// Gets or sets the maximum number of concurrent tasks enabled by this <see cref="BatchClientParallelOptions"/> instance.
    /// The default value is 1.
    /// </summary>
    public int maxDegreeOfParallelism() {
        return this.maxDegreeOfParallelism;
    }

    public BatchClientParallelOptions withMaxDegreeOfParallelism(int maxDegreeOfParallelism) {
        if (maxDegreeOfParallelism > 0) {
            this.maxDegreeOfParallelism = maxDegreeOfParallelism;
        }
        else {
            throw new IllegalArgumentException("maxDegreeOfParallelism");
        }
        return this;
    }

    public BatchClientParallelOptions() {
        this.maxDegreeOfParallelism = 1;
    }

    public BatchClientParallelOptions(int maxDegreeOfParallelism) {
        this.maxDegreeOfParallelism = maxDegreeOfParallelism;
    }

}
