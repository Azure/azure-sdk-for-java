// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.deviceregistry.softwareupdate;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;
import com.azure.iot.deviceregistry.softwareupdate.models.FileImportMetadata;
import com.azure.iot.deviceregistry.softwareupdate.models.ImportManifestMetadata;
import com.azure.iot.deviceregistry.softwareupdate.models.ImportUpdateInputItem;
import com.azure.iot.deviceregistry.softwareupdate.models.ImportUpdateRequest;
import com.azure.iot.deviceregistry.softwareupdate.models.Update;
import com.azure.iot.deviceregistry.softwareupdate.models.UpdateOperation;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SoftwareUpdateClientTest extends SoftwareUpdateTestBase {
    private static final String PROVIDER = "Contoso";
    private static final String NAME = "Toaster";
    private static final String VERSION = "1.0";
    private static final String MANIFEST_VERSION = "4.0";
    private static final long MANIFEST_SIZE = 712L;
    private static final String MANIFEST_SHA256 = "PHuSWFOX73yLXeaIrSo9gtsiGGKOKY6fw5n6/6rFFh4=";
    private static final String PAYLOAD_FILE_NAME = "README.md";

    @Test
    public void importGetListAndDeleteUpdate() {
        assertThrows(ResourceNotFoundException.class,
            () -> softwareUpdateClient.getUpdateWithResponse(PROVIDER, NAME, VERSION, new RequestOptions()),
            "The update used by this test already exists. Delete it before recording the test.");

        ImportUpdateRequest request = createImportRequest();
        boolean importSucceeded = false;
        try {
            SyncPoller<UpdateOperation, Void> importPoller = softwareUpdateClient.beginImportUpdate(request);
            PollResponse<UpdateOperation> importResponse = importPoller.waitForCompletion();
            assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, importResponse.getStatus());
            importSucceeded = true;

            Update importedUpdate = softwareUpdateClient.getUpdate(PROVIDER, NAME, VERSION);
            assertUpdate(importedUpdate);

            Response<BinaryData> getResponse
                = softwareUpdateClient.getUpdateWithResponse(PROVIDER, NAME, VERSION, new RequestOptions());
            assertEquals(200, getResponse.getStatusCode());
            assertUpdate(getResponse.getValue().toObject(Update.class));
            assertTrue(softwareUpdateClient.listFiles(PROVIDER, NAME, VERSION, new RequestOptions())
                .stream()
                .findFirst()
                .isPresent());
        } finally {
            if (importSucceeded) {
                PollResponse<UpdateOperation> deleteResponse
                    = softwareUpdateClient.beginDeleteUpdate(PROVIDER, NAME, VERSION).waitForCompletion();
                assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, deleteResponse.getStatus());
            }
        }

        assertThrows(ResourceNotFoundException.class,
            () -> softwareUpdateClient.getUpdateWithResponse(PROVIDER, NAME, VERSION, new RequestOptions()));
    }

    @Test
    public void listUpdatesAndProviders() {
        assertNotNull(softwareUpdateClient.listUpdates(new RequestOptions()).iterableByPage().iterator().next());
        assertNotNull(softwareUpdateClient.listProviders(new RequestOptions()).iterableByPage().iterator().next());
    }

    private ImportUpdateRequest createImportRequest() {
        String manifestUrl = getTestMode() == TestMode.PLAYBACK
            ? PLAYBACK_MANIFEST_URL
            : getRequiredConfiguration("DEVICE_REGISTRY_SOFTWARE_UPDATE_MANIFEST_URL");
        String fileUrl = getTestMode() == TestMode.PLAYBACK
            ? PLAYBACK_PAYLOAD_URL
            : getRequiredConfiguration("DEVICE_REGISTRY_SOFTWARE_UPDATE_PAYLOAD_URL");

        Map<String, String> hashes = new HashMap<>();
        hashes.put("sha256", MANIFEST_SHA256);
        ImportManifestMetadata manifest = new ImportManifestMetadata(manifestUrl, MANIFEST_SIZE, hashes);
        ImportUpdateInputItem input = new ImportUpdateInputItem(manifest)
            .setFiles(Collections.singletonList(new FileImportMetadata(PAYLOAD_FILE_NAME, fileUrl)))
            .setFriendlyName("Java SDK lifecycle test");
        return new ImportUpdateRequest(Collections.singletonList(input)).setEnableScan(false);
    }

    private static void assertUpdate(Update update) {
        assertNotNull(update);
        assertNotNull(update.getUpdateId());
        assertEquals(PROVIDER, update.getUpdateId().getProvider());
        assertEquals(NAME, update.getUpdateId().getName());
        assertEquals(VERSION, update.getUpdateId().getVersion());
        assertNotNull(update.getCompatibility());
        assertEquals(MANIFEST_VERSION, update.getManifestVersion());
        assertNotNull(update.getImportedDateTime());
        assertNotNull(update.getCreatedDateTime());
    }
}
