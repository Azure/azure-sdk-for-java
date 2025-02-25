// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.test.utils;

import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.implementation.util.IdentityUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Util to run Azure CLI command line and get result output string.
 */
public final class CliRunner {
    private static final ClientLogger LOGGER = new ClientLogger(CliRunner.class);

    private CliRunner() {
    }

    /**
     * Run Azure CLI command and get output result.
     * @param azCommand the Azure CLI command to run in command line
     * @return command line output if successful
     * @throws IOException if IOException occurs
     * @throws InterruptedException if InterruptedException occurs
     */
    public static String run(String azCommand) throws IOException, InterruptedException {
        final Pattern windowsProcessErrorMessage = Pattern.compile("'azd?' is not recognized");
        final Pattern shProcessErrorMessage = Pattern.compile("azd?:.*not found");
        String starter;
        String switcher;
        if (IdentityUtil.isWindowsPlatform()) {
            starter = "cmd.exe";
            switcher = "/c";
        } else {
            starter = "/bin/sh";
            switcher = "-c";
        }

        ProcessBuilder builder = new ProcessBuilder(starter, switcher, azCommand);
        // Redirects stdin to dev null, helps to avoid messages sent in by the cmd process to upgrade etc.
        builder.redirectInput(ProcessBuilder.Redirect.from(IdentityUtil.NULL_FILE));

        builder.redirectErrorStream(true);
        Process process = builder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader
            = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while (true) {
                line = reader.readLine();
                if (line == null) {
                    break;
                }

                if (windowsProcessErrorMessage.matcher(line).find() || shProcessErrorMessage.matcher(line).find()) {
                    throw LOGGER.logExceptionAsError(
                        new RuntimeException("AzureCliCredential authentication unavailable. Azure CLI not installed."
                            + "To mitigate this issue, please refer to the troubleshooting guidelines here at "
                            + "https://aka.ms/azsdk/java/identity/azclicredential/troubleshoot"));
                }
                output.append(line);
            }
        }
        String processOutput = output.toString();

        // wait(at most) 10 seconds for the process to complete
        boolean finished = process.waitFor(10, TimeUnit.SECONDS);

        if (!finished) {
            throw LOGGER
                .logExceptionAsError(new RuntimeException("Process did not complete within the expected time."));
        }
        if (process.exitValue() != 0) {
            if (processOutput.length() > 0) {
                if (processOutput.contains("az login") || processOutput.contains("az account set")) {
                    throw LOGGER.logExceptionAsError(
                        new RuntimeException("AzureCliCredential authentication unavailable. Azure CLI not installed."
                            + "To mitigate this issue, please refer to the troubleshooting guidelines here at "
                            + "https://aka.ms/azsdk/java/identity/azclicredential/troubleshoot"));
                }
                throw LOGGER.logExceptionAsError(new ClientAuthenticationException(azCommand + " failed", null));
            } else {
                throw LOGGER
                    .logExceptionAsError(new ClientAuthenticationException("Failed to invoke Azure CLI ", null));
            }
        }

        LOGGER.verbose(azCommand + " => A response was received from Azure CLI, deserializing the" + " response.");

        return processOutput;
    }
}
