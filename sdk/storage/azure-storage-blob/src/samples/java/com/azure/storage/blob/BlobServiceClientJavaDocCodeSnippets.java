// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.storage.common.AccountSASPermission;
import com.azure.storage.common.AccountSASResourceType;
import com.azure.storage.common.AccountSASService;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;

import java.time.OffsetDateTime;

public class BlobServiceClientJavaDocCodeSnippets {

    private BlobServiceClient client = JavaDocCodeSnippetsHelpers.getBlobServiceClient();

    /**
     * Generates a code sample for using {@link BlobServiceClient#generateAccountSAS(AccountSASService,
     * AccountSASResourceType, AccountSASPermission, OffsetDateTime, OffsetDateTime, String, IPRange, SASProtocol)}
     */
    public void generateAccountSAS() {
        // BEGIN: com.azure.storage.blob.blobServiceClient.generateAccountSAS#AccountSASService-AccountSASResourceType-AccountSASPermission-OffsetDateTime-OffsetDateTime-String-IPRange-SASProtocol
        AccountSASService service = new AccountSASService()
            .blob(true)
            .file(true)
            .queue(true)
            .table(true);
        AccountSASResourceType resourceType = new AccountSASResourceType()
            .container(true)
            .object(true)
            .service(true);
        AccountSASPermission permission = new AccountSASPermission()
            .read(true)
            .add(true)
            .create(true)
            .write(true)
            .delete(true)
            .list(true)
            .processMessages(true)
            .update(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

        String sas = client.generateAccountSAS(service, resourceType, permission, expiryTime, startTime, version,
            ipRange, sasProtocol);
        // END: com.azure.storage.blob.blobServiceClient.generateAccountSAS#AccountSASService-AccountSASResourceType-AccountSASPermission-OffsetDateTime-OffsetDateTime-String-IPRange-SASProtocol
    }

}
