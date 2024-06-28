package com.azure.health.deidentification.testutils;
import com.azure.health.deidentification.DeidentificationClient;
import com.azure.health.deidentification.models.DeidentificationJob;
import com.azure.health.deidentification.models.DeidentificationResult;
import com.azure.health.deidentification.models.JobStatus;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

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

    /**
     * Polls the status of a deidentification job asynchronously until it matches the specified status.
     * <p>
     * This method periodically checks the status of a deidentification job by its name. It continues to poll
     * at a specified interval until the job's status matches the desired status. If the job encounters an error,
     * the error message is printed to the console.
     *
     * @param client The {@link DeidentificationClient} used to fetch job status.
     * @param jobName The name of the job to poll.
     * @param statusToWait The {@link JobStatus} to wait for.
     * @param intervalInMS The interval, in milliseconds, between each status check.
     * @return A {@link CompletableFuture} that completes with the {@link DeidentificationJob} once its status
     *         matches the specified status. If the thread is interrupted while waiting, it completes exceptionally
     *         with a {@link RuntimeException}.
     */

    public static CompletableFuture<DeidentificationJob> pollJobStatus(DeidentificationClient client, String jobName, JobStatus statusToWait, long intervalInMS) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                DeidentificationJob jobOutput = client.getJob(jobName);
                while (!jobOutput.getStatus().equals(statusToWait)) {
                    TimeUnit.MILLISECONDS.sleep(intervalInMS);
                    jobOutput = client.getJob(jobName);
                    System.out.println("Job status: " + jobOutput.getStatus());
                    if (jobOutput.getError() != null) {
                        System.out.println("Job error: " + jobOutput.getError().getMessage());
                    }
                }
                return jobOutput;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
