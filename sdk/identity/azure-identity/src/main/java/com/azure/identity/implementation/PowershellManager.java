// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.CredentialUnavailableException;
import com.sun.jna.Platform;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class PowershellManager {
    private static final ClientLogger LOGGER = new ClientLogger(PowershellManager.class);
    private static final String DEFAULT_WINDOWS_POWERSHELL_PATH = "pwsh.exe";
    private static final String LEGACY_WINDOWS_POWERSHELL_PATH = "powershell.exe";
    private static final String DEFAULT_NIX_POWERSHELL_PATH = "pwsh";
    private final String powershellPath;

    public PowershellManager(boolean useLegacyPowerShell) {
        if (Platform.isWindows()) {
            this.powershellPath = useLegacyPowerShell ? LEGACY_WINDOWS_POWERSHELL_PATH : DEFAULT_WINDOWS_POWERSHELL_PATH;
        } else {
            this.powershellPath = DEFAULT_NIX_POWERSHELL_PATH;
        }
    }

    public Mono<String> runCommand(String input) {
        return Mono.fromCallable(() -> {
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
                throw LOGGER.logExceptionAsError(new CredentialUnavailableException("PowerShell command failure.", e));
            }
        });
    }

    String[] getCommandLine(String input) {
        return Platform.isWindows()
            ? new String[]{powershellPath, "-NoProfile", "-Command", input}
            : new String[]{"/bin/bash", "-c", String.format("%s -NoProfile -Command '%s'", powershellPath, input)};
    }
}
