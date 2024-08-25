// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.models.BlobAccessPolicy;
import com.azure.storage.blob.models.BlobSignedIdentifier;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccessPolicyAsyncTests extends BlobTestBase {
    @Test
    public void setAccessPolicyMinAccess() {
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.CONTAINER, null);
        StepVerifier.create(ccAsync.getProperties())
            .assertNext(r -> assertEquals(PublicAccessType.CONTAINER, r.getBlobPublicAccess()))
            .verifyComplete();
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

        setAccessPolicySleepAsync(ccAsync, null, ids);

        StepVerifier.create(ccAsync.getAccessPolicy())
            .assertNext(r -> assertEquals("0000", r.getIdentifiers().get(0).getId()))
            .verifyComplete();
    }

    @Test
    public void setAccessPolicyError() {
        ccAsync = primaryBlobServiceAsyncClient.getBlobContainerAsyncClient(generateContainerName());

        StepVerifier.create(ccAsync.setAccessPolicy(null, null))
            .verifyError(BlobStorageException.class);
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
        setAccessPolicySleepAsync(ccAsync, PublicAccessType.BLOB, ids);

        StepVerifier.create(ccAsync.getAccessPolicyWithResponse(null))
            .assertNext(r -> {
                assertResponseStatusCode(r, 200);
                assertEquals(PublicAccessType.BLOB, r.getValue().getBlobAccessType());
                assertTrue(validateBasicHeaders(r.getHeaders()));
                assertEquals(identifier.getAccessPolicy().getExpiresOn(),
                    r.getValue().getIdentifiers().get(0).getAccessPolicy().getExpiresOn());
                assertEquals(identifier.getAccessPolicy().getStartsOn(),
                    r.getValue().getIdentifiers().get(0).getAccessPolicy().getStartsOn());
                assertEquals(identifier.getAccessPolicy().getPermissions(),
                    r.getValue().getIdentifiers().get(0).getAccessPolicy().getPermissions());
            })
            .verifyComplete();
    }

    @Test
    public void containerSasIdentifierAndPermissions() {
        BlobSignedIdentifier identifier = new BlobSignedIdentifier()
            .setId("0000")
            .setAccessPolicy(new BlobAccessPolicy().setPermissions("racwdl")
                .setExpiresOn(testResourceNamer.now().plusDays(1)));
        setAccessPolicySleepAsync(ccAsync, null, Arrays.asList(identifier));

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
        String sasWithId = ccAsync.generateSas(sasValues);
        BlobContainerAsyncClient client1 = getContainerAsyncClient(sasWithId, ccAsync.getBlobContainerUrl());
        StepVerifier.create(client1.listBlobs())
            .thenConsumeWhile(r -> true)
            .verifyComplete();

        sasValues = new BlobServiceSasSignatureValues(expiryTime, permissions);
        String sasWithPermissions = ccAsync.generateSas(sasValues);
        BlobContainerAsyncClient client2 = getContainerAsyncClient(sasWithPermissions, ccAsync.getBlobContainerUrl());
        StepVerifier.create(client2.listBlobs())
            .thenConsumeWhile(r -> true)
            .verifyComplete();
    }

    //service async
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void sasSanitization(boolean unsanitize) {
        String identifier = "id with spaces";
        String blobName = generateBlobName();
        setAccessPolicySleepAsync(ccAsync, null, Collections.singletonList(new BlobSignedIdentifier()
            .setId(identifier)
            .setAccessPolicy(new BlobAccessPolicy()
                .setPermissions("racwdl")
                .setExpiresOn(testResourceNamer.now().plusDays(1)))));
        ccAsync.getBlobAsyncClient(blobName).upload(BinaryData.fromBytes("test".getBytes())).block();
        String sas = ccAsync.generateSas(new BlobServiceSasSignatureValues(identifier));
        if (unsanitize) {
            sas = sas.replace("%20", " ");
        }

        // when: "Endpoint with SAS built in, works as expected"
        String finalSas = sas;
        BlobContainerAsyncClient client1 = instrument(new BlobContainerClientBuilder()
            .endpoint(ccAsync.getBlobContainerUrl() + "?" + finalSas))
            .buildAsyncClient();
        StepVerifier.create(client1.getBlobAsyncClient(blobName).downloadContent())
            .expectNextCount(1)
            .verifyComplete();


        String connectionString = "AccountName=" + BlobUrlParts.parse(ccAsync.getAccountUrl()).getAccountName()
            + ";SharedAccessSignature=" + sas;
        BlobContainerAsyncClient client2 = instrument(new BlobContainerClientBuilder()
            .connectionString(connectionString)
            .containerName(ccAsync.getBlobContainerName()))
            .buildAsyncClient();
        StepVerifier.create(client2.getBlobAsyncClient(blobName).downloadContent())
            .expectNextCount(1)
            .verifyComplete();
    }
}
