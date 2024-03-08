// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobContainerAccessPolicies;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccessPolicyTests extends BlobTestBase {
    //ContainerApiTests
    @Test
    public void setAccessPolicyMinAccess() {
        setAccessPolicySleep(cc, PublicAccessType.CONTAINER, null);
        assertEquals(PublicAccessType.CONTAINER, cc.getProperties().getBlobPublicAccess());
    }

    @Test
    public void setAccessPolicyMinIds() {
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime())
                .setExpiresOn(testResourceNamer.now().atZoneSameInstant(ZoneId.of("UTC")).toOffsetDateTime()
                    .plusDays(1))
                .setPermissions("r"));

        List<BlobSignedIdentifier> ids = Collections.singletonList(identifier);

        setAccessPolicySleep(cc, null, ids);

        assertEquals("0000", cc.getAccessPolicy().getIdentifiers().get(0).getId());
    }

    @Test
    public void setAccessPolicyError() {
        cc = primaryBlobServiceClient.getBlobContainerClient(generateContainerName());
        assertThrows(BlobStorageException.class, () -> cc.setAccessPolicy(null, null));
    }

    @Test
    public void getAccessPolicy() {
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy()
                .setStartsOn(testResourceNamer.now())
                .setExpiresOn(testResourceNamer.now().plusDays(1))
                .setPermissions("r"));
        List<BlobSignedIdentifier> ids = Collections.singletonList(identifier);
        setAccessPolicySleep(cc, PublicAccessType.BLOB, ids);
        Response<BlobContainerAccessPolicies> response = cc.getAccessPolicyWithResponse(null, null,
            null);

        assertResponseStatusCode(response, 200);
        assertEquals(PublicAccessType.BLOB, response.getValue().getBlobAccessType());
        assertTrue(validateBasicHeaders(response.getHeaders()));
        assertEquals(identifier.getAccessPolicy().getExpiresOn(),
            response.getValue().getIdentifiers().get(0).getAccessPolicy().getExpiresOn());
        assertEquals(identifier.getAccessPolicy().getStartsOn(),
            response.getValue().getIdentifiers().get(0).getAccessPolicy().getStartsOn());
        assertEquals(identifier.getAccessPolicy().getPermissions(), response.getValue().getIdentifiers().get(0).getAccessPolicy().getPermissions());
    }

    //ServiceApiTests
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void sasSanitization(boolean unsanitize) {
        String identifier = "id with spaces";
        String blobName = generateBlobName();
        setAccessPolicySleep(cc, null, Collections.singletonList(new BlobSignedIdentifier()
            .setId(identifier)
            .setAccessPolicy(new BlobAccessPolicy()
                .setPermissions("racwdl")
                .setExpiresOn(testResourceNamer.now().plusDays(1)))));
        cc.getBlobClient(blobName).upload(BinaryData.fromBytes("test".getBytes()));
        String sas = cc.generateSas(new BlobServiceSasSignatureValues(identifier));
        if (unsanitize) {
            sas = sas.replace("%20", " ");
        }

        //

        // when: "Endpoint with SAS built in, works as expected"
        String finalSas = sas;
        assertDoesNotThrow(() -> instrument(new BlobContainerClientBuilder()
            .endpoint(cc.getBlobContainerUrl() + "?" + finalSas))
            .buildClient()
            .getBlobClient(blobName)
            .downloadContent());

        String connectionString = "AccountName=" + BlobUrlParts.parse(cc.getAccountUrl()).getAccountName()
            + ";SharedAccessSignature=" + sas;
        assertDoesNotThrow(() -> instrument(new BlobContainerClientBuilder()
            .connectionString(connectionString)
            .containerName(cc.getBlobContainerName()))
            .buildClient()
            .getBlobClient(blobName)
            .downloadContent());
    }

    //sasClientTests
    @Test
    public void containerSasIdentifierAndPermissions() {
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy().setPermissions("racwdl")
                .setExpiresOn(testResourceNamer.now().plusDays(1)));
        setAccessPolicySleep(cc, null, Arrays.asList(identifier));

        // Check containerSASPermissions
        BlobContainerSasPermission permissions = new BlobContainerSasPermission()
            .setReadPermission(true)
            .setWritePermission(true)
            .setListPermission(true)
            .setCreatePermission(true)
            .setDeletePermission(true)
            .setAddPermission(true)
            .setListPermission(true);
        if (Constants.SAS_SERVICE_VERSION.compareTo("2019-12-12") >= 0) {
            permissions.setDeleteVersionPermission(true).setFilterPermission(true);
        }
        if (Constants.SAS_SERVICE_VERSION.compareTo("2020-06-12") >= 0) {
            permissions.setImmutabilityPolicyPermission(true);
        }

        OffsetDateTime expiryTime = testResourceNamer.now().plusDays(1);

        BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(identifier.getId());
        String sasWithId = cc.generateSas(sasValues);
        BlobContainerClient client1 = getContainerClient(sasWithId, cc.getBlobContainerUrl());
        assertDoesNotThrow(() -> client1.listBlobs().iterator().hasNext());

        sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);
        String sasWithPermissions = cc.generateSas(sasValues);
        BlobContainerClient client2 = getContainerClient(sasWithPermissions, cc.getBlobContainerUrl());
        assertDoesNotThrow(() -> client2.listBlobs().iterator().hasNext());
    }
}
