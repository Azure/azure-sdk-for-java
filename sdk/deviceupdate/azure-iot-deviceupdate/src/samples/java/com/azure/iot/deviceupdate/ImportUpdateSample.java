// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.iot.deviceupdate;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ImportUpdateSample {
    public static void main(String[] args)  throws IOException {
        DeviceUpdateClient client = new DeviceUpdateClientBuilder()
            .endpoint(Configuration.getGlobalConfiguration().get("AZURE_ACCOUNT_ENDPOINT"))
            .instanceId(Configuration.getGlobalConfiguration().get("AZURE_INSTANCE_ID"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .buildClient();

        try {
            String payloadFile = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_PAYLOAD_FILE");
            String payloadUrl = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_PAYLOAD_URL");
            String manifestFile = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_MANIFEST_FILE");
            String manifestUrl = Configuration.getGlobalConfiguration().get("DEVICEUPDATE_MANIFEST_URL");

            String content = String.format("[{\"importManifest\": {\"url\": \"%s\", \"sizeInBytes\": %s, \"hashes\": { \"sha256\": \"%s\" }}, "
                    + "\"files\": [{\"fileName\": \"%s\", \"url\": \"%s\" }]"
                    + "}]",
                manifestUrl, getFileSize(manifestFile), getFileHash(manifestFile),
                getFileName(payloadFile), payloadUrl);

            // BEGIN: com.azure.iot.deviceupdate.DeviceUpdateClient.ImportUpdate
            SyncPoller<BinaryData, BinaryData> response = client.beginImportUpdate(BinaryData.fromString(content), null);
            response.waitForCompletion();
            // END: com.azure.iot.deviceupdate.DeviceUpdateClient.ImportUpdate

            System.out.println(response.getFinalResult().toString());

        } catch (HttpResponseException e) {
            System.out.println("import failed");
        }
    }

    private static long getFileSize(String payloadLocalFile) {
        File file = new File(payloadLocalFile);
        return file.length();
    }

    private static String getFileName(String payloadLocalFile) {
        File file = new File(payloadLocalFile);
        return file.getName();
    }

    private static String getFileHash(String payloadLocalFile) throws IOException {
        try {
            MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
            String payload = readAllTextFromFile(payloadLocalFile);
            byte[] result = mDigest.digest(payload.getBytes());

            return Base64.getEncoder().encodeToString(result);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static String readAllTextFromFile(String filePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(filePath)));
        return content;
    }
}
