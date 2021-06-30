// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.CredentialUnavailableException;
import com.sun.jna.Platform;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class PowershellManager {

    private static final ClientLogger LOGGER = new ClientLogger(PowershellManager.class);
    public static final Pattern PS_RESPONSE_PATTERN = Pattern.compile("\\s+$");
    private Process process;
    private PrintWriter commandWriter;
    private boolean closed;
    private int waitPause = 1000;
    private long maxWait = 10000L;
    private final String powershellPath;
    private ExecutorService executorService;


    public PowershellManager(String powershellPath) {
        this.powershellPath = powershellPath;
    }

    public PowershellManager(String powershellPath, ExecutorService executorService) {
        this.powershellPath = powershellPath;
        this.executorService = executorService;
    }

    public Mono<PowershellManager> initSession() {

        ProcessBuilder pb;
        if (Platform.isWindows()) {
            pb = new ProcessBuilder(new String[]{"cmd.exe", "/c", "chcp", "65001", ">", "NUL", "&",
                powershellPath, "-ExecutionPolicy", "Bypass", "-NoExit", "-NoProfile", "-Command", "-"});
        } else {
            pb = new ProcessBuilder(new String[]{powershellPath, "-nologo", "-noexit", "-Command", "-"});
        }

        pb.redirectErrorStream(true);


        Supplier<PowershellManager> supplier = () -> {
            try {
                this.process = pb.start();
                this.commandWriter = new PrintWriter(
                    new OutputStreamWriter(new BufferedOutputStream(process.getOutputStream()), StandardCharsets.UTF_8),
                    true);
                if (this.process.waitFor(4L, TimeUnit.SECONDS) && !this.process.isAlive()) {
                    throw LOGGER.logExceptionAsError(new CredentialUnavailableException("Unable to execute PowerShell."
                        + " Please make sure that it is installed in your system."));
                }
                this.closed = false;
            } catch (InterruptedException | IOException e) {
                throw LOGGER.logExceptionAsError(new CredentialUnavailableException("Unable to execute PowerShell. "
                    + "Please make sure that it is installed in your system", e));
            }
            return this;
        };
        return executorService != null ? Mono.fromFuture(CompletableFuture.supplyAsync(supplier, executorService))
            : Mono.fromFuture(CompletableFuture.supplyAsync(supplier));
    }


    public Mono<String> runCommand(String command) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(),
            StandardCharsets.UTF_8));
        StringBuilder powerShellOutput = new StringBuilder();
        commandWriter.println(command);
        return canRead(reader)
                .flatMap(b -> {
                    if (b) {
                        return readData(reader, powerShellOutput)
                                .flatMap(ignored -> Mono.just(PS_RESPONSE_PATTERN.matcher(powerShellOutput.toString())
                                        .replaceAll("")));
                    } else {
                        return Mono.error(new CredentialUnavailableException("Error reading data from reader"));
                    }
                });
    }

    private Mono<Boolean> readData(BufferedReader reader, StringBuilder powerShellOutput) {
        return Mono.defer(() -> {
            String line;
            try {
                line = reader.readLine();
                if (line != null) {
                    powerShellOutput.append(line).append("\r\n");
                    return canRead(reader).flatMap(b -> {
                        if (!this.closed && b) {
                            return Mono.empty();
                        }
                        return Mono.just(true);
                    });
                } else {
                    return Mono.just(true);
                }
            } catch (IOException e) {
                return Mono.error(LOGGER.logExceptionAsError(
                    new CredentialUnavailableException("Powershell reader not ready for reading", e)));
            }
        }).repeatWhenEmpty((Flux<Long> longFlux) -> longFlux.concatMap(ignored -> Flux.just(true)));
    }

    private Mono<Boolean> canRead(BufferedReader reader) {
        Supplier<Boolean> supplier = () -> {
            int pause = 62;
            int maxPause = Platform.isMac() ? this.waitPause : 500;
            while (true) {
                try {
                    if (!reader.ready()) {
                        if (pause > maxPause) {
                            return false;
                        }
                        pause *= 2;
                        Thread.sleep((long) pause);
                    } else {
                        break;
                    }

                } catch (IOException | InterruptedException e) {
                    throw LOGGER.logExceptionAsError(
                        new CredentialUnavailableException("Powershell reader not ready for reading", e));
                }
            }
            return true;
        };
        return executorService != null ? Mono.fromFuture(CompletableFuture.supplyAsync(supplier, executorService))
            : Mono.fromFuture(CompletableFuture.supplyAsync(supplier));
    }

    public Mono<Boolean> close() {
        if (!this.closed && this.process != null) {
            Supplier<Boolean> supplier = () -> {
                this.commandWriter.println("exit");
                try {
                    this.process.waitFor(maxWait, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    LOGGER.logExceptionAsError(new RuntimeException("PowerShell process encountered unexpcted"
                        + " error when closing.", e));
                } finally {
                    this.commandWriter.close();

                    try {
                        if (process.isAlive()) {
                            process.getInputStream().close();
                        }
                    } catch (IOException ex) {
                        LOGGER.logExceptionAsError(new RuntimeException("PowerShell stream encountered unexpcted"
                            + " error when closing.", ex));
                    }
                    this.closed = true;
                }
                return this.closed;
            };
            return executorService != null ? Mono.fromFuture(CompletableFuture.supplyAsync(supplier, executorService))
                : Mono.fromFuture(CompletableFuture.supplyAsync(supplier));
        } else {
            return Mono.just(true);
        }
    }
}
