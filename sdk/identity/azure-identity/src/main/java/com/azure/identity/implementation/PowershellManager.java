package com.azure.identity.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.CredentialUnavailableException;
import com.sun.jna.Platform;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class PowershellManager {

    private static final ClientLogger logger = new ClientLogger(PowershellManager.class);
    private Process process;
    private PrintWriter commandWriter;
    private boolean closed = false;
    private static final String DEFAULT_WINDOWS_PS_EXECUTABLE = "pwsh.exe";
    private static final String LEGACY_WINDOWS_PS_EXECUTABLE = "poweshell.exe";
    private static final String DEFAULT_LINUX_PS_EXECUTABLE = "pwsh";
    private static final String LEGACY_LINUX_PS_EXECUTABLE = "powershell";
    private int waitPause = 5000;
    private long maxWait = 10000L;
    private final boolean legacyPowershell;


    public PowershellManager(boolean legacyPowershell) {
        this.legacyPowershell = legacyPowershell;
    }

    public Mono<PowershellManager> initSession() {

        String powerShellExecutablePath = legacyPowershell ?
            (Platform.isWindows() ? LEGACY_WINDOWS_PS_EXECUTABLE : LEGACY_LINUX_PS_EXECUTABLE)
            : (Platform.isWindows() ? DEFAULT_WINDOWS_PS_EXECUTABLE : DEFAULT_LINUX_PS_EXECUTABLE);

        ProcessBuilder pb;
        if (Platform.isWindows()) {
            pb = new ProcessBuilder(new String[]{"cmd.exe", "/c", "chcp", "65001", ">", "NUL", "&",
                powerShellExecutablePath, "-ExecutionPolicy", "Bypass", "-NoExit", "-NoProfile", "-Command", "-"});
        } else {
            pb = new ProcessBuilder(new String[]{powerShellExecutablePath, "-nologo", "-noexit", "-Command", "-"});
        }

        pb.redirectErrorStream(true);


        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            try {
                this.process = pb.start();
                this.commandWriter = new PrintWriter(
                    new OutputStreamWriter(new BufferedOutputStream(process.getOutputStream())), true);
                if (this.process.waitFor(5L, TimeUnit.SECONDS) && !this.process.isAlive()) {
                    throw new CredentialUnavailableException("Unable to execute PowerShell. Please make sure that"
                        + " it is installed in your system.");
                }
            } catch (InterruptedException | IOException e) {
                throw new CredentialUnavailableException("Unable to execute PowerShell. Please make sure"
                    + " that it is installed in your system", e);
            }
            return this;
        }));
    }


    public Mono<String> executeCommand(String command) {
        String commandOutput = "";
        boolean isError = false;
        boolean timeout = false;

        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        this.waitPause = waitPause;
        StringBuilder powerShellOutput = new StringBuilder();
        commandWriter.println(command);
        return canRead(reader)
                .flatMap(b -> {
                    if (b) {
                        return readData(reader, powerShellOutput)
                                .flatMap(ignored -> {
                                    return Mono.just(powerShellOutput.toString());
                                });
                    } else {
                        return Mono.error(new RuntimeException("Error reading data from reader"));
                    }
                });
    }

    private Mono<Boolean> readData(BufferedReader reader, StringBuilder powerShellOutput) {
        return Mono.defer(() -> {
            String line;
            try {
                if (null != (line = reader.readLine())) {
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
                return Mono.error(e);
            }
        }).repeatWhenEmpty((Flux<Long> longFlux) -> longFlux.concatMap(ignored -> Flux.just(true)));
    }

    private Mono<Boolean> canRead(BufferedReader reader) {
        return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
            while (true) {
                try {
                    if (!reader.ready()) {
                        Thread.sleep((long) this.waitPause);
                        return false;
                    }
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException("Powershell reader not ready for reading");
                }
                return true;
            }
        }));
    }

    public Mono<Boolean> close() {
        if (!this.closed) {
            return Mono.fromFuture(CompletableFuture.supplyAsync(() -> {
                this.commandWriter.println("exit");
                try {
                    this.process.waitFor(maxWait, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    logger.logExceptionAsError(new RuntimeException("PowerShell process encountered unexpcted"
                            + " error when closing.", e));
                } finally {
                    this.commandWriter.close();

                    try {
                        if (process.isAlive()) {
                            process.getInputStream().close();
                        }
                    } catch (IOException ex) {
                        logger.logExceptionAsError(new RuntimeException("PowerShell stream encountered unexpcted"
                                + " error when closing.", ex));
                    }
                    this.closed = true;
                }
                return this.closed;
            }));
        } else {
            return Mono.just(true);
        }
    }
}
