// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.deidentification.testutils;

import java.time.Instant;

public class Utils {

    /**
     * Generates a job name by appending the current timestamp in milliseconds to the specified base name.
     *
     * <p>Example usage:</p>
     * <pre>
     * {@code
     * String jobName = generateJobName("job");
     * System.out.println(jobName); // Output: job-1625689000000
     * }
     * </pre>
     *
     * @param baseName the base name to which the timestamp will be appended
     * @return a new job name composed of the base name and the current timestamp in milliseconds
     */
    public static String generateJobName(String baseName) {
        long timestamp = Instant.now().toEpochMilli();
        return baseName + "-" + timestamp;
    }

}
