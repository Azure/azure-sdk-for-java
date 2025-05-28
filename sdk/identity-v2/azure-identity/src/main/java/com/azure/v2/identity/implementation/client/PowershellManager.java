// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.client;

import com.azure.v2.identity.exceptions.CredentialUnavailableException;
import com.sun.jna.Platform;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class PowershellManager {
    private static final ClientLogger LOGGER = new ClientLogger(PowershellManager.class);
    private static final String DEFAULT_WINDOWS_POWERSHELL_PATH = "pwsh.exe";
    private static final String LEGACY_WINDOWS_POWERSHELL_PATH = "powershell.exe";
    private static final String DEFAULT_NIX_POWERSHELL_PATH = "pwsh";
    private final String powershellPath;

    public PowershellManager(boolean useLegacyPowerShell) {
        if (Platform.isWindows()) {
            this.powershellPath
                = useLegacyPowerShell ? LEGACY_WINDOWS_POWERSHELL_PATH : DEFAULT_WINDOWS_POWERSHELL_PATH;
        } else {
            this.powershellPath = DEFAULT_NIX_POWERSHELL_PATH;
        }
    }

    public String runCommand(String input) {
        try {
            String[] command = getCommandLine(input);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            process.waitFor(10000L, TimeUnit.MILLISECONDS);
            // Read output
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }
            }
            return output.toString();
        } catch (IOException | InterruptedException e) {
            throw LOGGER.throwableAtError().log("PowerShell command failure.", e, CredentialUnavailableException::new);
        }
    }

    String[] getCommandLine(String input) {
        String base64Input = java.util.Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_16LE));

        return Platform.isWindows()
            ? new String[] { powershellPath, "-NoProfile", "-EncodedCommand", base64Input }
            : new String[] {
                "/bin/bash",
                "-c",
                String.format("%s -NoProfile -EncodedCommand '%s'", powershellPath, base64Input) };
    }
}
