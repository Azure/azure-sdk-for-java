// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared.azurite;

import com.azure.core.util.Configuration;
import com.azure.storage.common.test.shared.TestAccount;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This class manages Azurite Lifecycle for a test class.
 * - Creates accounts pool, so that each test has own account, bump up pool size if you're running out of accounts
 * - Starts Azurite process
 *  - Tears down Azurite process after test class is run
 *  It requires Azurite V3. See instalation insturctions here https://github.com/Azure/Azurite.
 *  After installing Azuirte define env variable AZURE_AZURITE_LOCATION that points to azurite installation (e.g. C:\Users\kasobol.REDMOND\AppData\Roaming\npm)
 *  NodeJS installation is also required and node should be in the $PATH.
 */
public class AzuriteFixture implements Closeable {

    private static final String AZURITE_LOCATION_KEY = "AZURE_AZURITE_LOCATION";

    private final Path tempDirectory;
    private final Process process;
    private volatile int blobsPort;
    private volatile int queuesPort;
    private final CountDownLatch portReaderGate = new CountDownLatch(2);
    private final String accountName = UUID.randomUUID().toString();
    private final String accountKey = Base64.getEncoder()
        .encodeToString(UUID.randomUUID().toString().getBytes(StandardCharsets.UTF_8));

    public AzuriteFixture() {
        try {
            Configuration configuration = Configuration.getGlobalConfiguration();
            String azuriteLocation = configuration.get(AZURITE_LOCATION_KEY);
            File defaultPath = new File(configuration.get("APPDATA", ""), "npm");

            if (azuriteLocation == null || azuriteLocation.trim().isEmpty()) {
                if (defaultPath.isDirectory()) {
                    azuriteLocation = defaultPath.getAbsolutePath();
                } else {
                    throw new IllegalArgumentException(errorMessage(
                        String.format(
                            "%s environment variable is not set and %s doesn't exist",
                            AZURITE_LOCATION_KEY, defaultPath)));
                }
            }
            File azuriteScriptLocation = new File(azuriteLocation, "node_modules/azurite/dist/src/azurite.js");

            tempDirectory = Files.createTempDirectory("azurite-fixture");

            ProcessBuilder processBuilder = new ProcessBuilder(
                "node",
                azuriteScriptLocation.toString(),
                "-l", tempDirectory.toString(),
                "--blobPort", "0",
                "--queuePort", "0",
                "--skipApiVersionCheck")
                .redirectErrorStream(true);
            processBuilder.environment().put("AZURITE_ACCOUNTS", String.format("%s:%s", accountName, accountKey));

            process = processBuilder.start();

            Thread inputReader = new Thread(() -> {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("Azurite Blob service is successfully listening at")) {
                            blobsPort = parseAzuritePort(line);
                            portReaderGate.countDown();
                        }
                        if (line.contains("Azurite Queue service is successfully listening at")) {
                            queuesPort = parseAzuritePort(line);
                            portReaderGate.countDown();
                        }

                        // keep reading so the process doesn't hang unable to write to stdio.
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            inputReader.setDaemon(true);
            inputReader.start();

            portReaderGate.await(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TestAccount getTestAccount() {
        return new TestAccount(
            accountName,
            accountKey,
            getConnectionString(),
            String.format("http://127.0.0.1:%d/%s", blobsPort, accountName),
            null,
            null,
            String.format("http://127.0.0.1:%d/%s", queuesPort, accountName),
            null);
    }

    public String getConnectionString() {
        return String.format(
            "DefaultEndpointsProtocol=http;AccountName=%1$s;AccountKey=%2$s;"
                + "BlobEndpoint=http://127.0.0.1:%3$d/%1$s;"
                + "QueueEndpoint=http://127.0.0.1:%4$d/%1$s;",
            accountName, accountKey, blobsPort, queuesPort);
    }

    @Override
    public void close() throws IOException {
        if (process != null) {
            process.destroyForcibly();
        }

        Files.walk(tempDirectory)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
    }

    private int parseAzuritePort(String outputLine) {
        int indexFrom = outputLine.lastIndexOf(':') + 1;
        return Integer.parseInt(outputLine.substring(indexFrom));
    }

    private String errorMessage(String specificReason) {
        return String.format("\nCould not run Azurite based test due to: %s.\n"
                + "Make sure that:\n"
                + "- NodeJS is installed and available in $PATH (i.e. 'node' command can be run in terminal)\n"
                + "- Azurite V3 is installed via NPM (see https://github.com/Azure/Azurite for instructions)\n"
                + "- %s envorinment is set and pointing to location of directory that has 'azurite' command"
                + " (i.e. run 'where azurite' in Windows CMD)\n",
            specificReason, AZURITE_LOCATION_KEY);
    }
}
