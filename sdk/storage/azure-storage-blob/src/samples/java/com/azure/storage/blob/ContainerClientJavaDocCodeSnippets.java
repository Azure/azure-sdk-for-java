package com.azure.storage.blob;

import com.azure.storage.blob.models.UserDelegationKey;
import com.azure.storage.common.Constants;
import com.azure.storage.common.IPRange;
import com.azure.storage.common.SASProtocol;

import java.time.OffsetDateTime;

public class ContainerClientJavaDocCodeSnippets {

    private ContainerClient client = JavaDocCodeSnippetsHelpers.getContainerClient();

    /**
     * Code snippet for {@link ContainerClient#generateUserDelegationSAS(UserDelegationKey, String, ContainerSASPermission, OffsetDateTime, OffsetDateTime, String, SASProtocol, IPRange, String, String, String, String, String)}
     */
    public void generateUserDelegationSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.ContainerClient.generateUserDelegationSAS
        ContainerSASPermission permissions = new ContainerSASPermission()
            .read(true)
            .write(true)
            .create(true)
            .delete(true)
            .add(true)
            .list(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String cacheControl = "cache";
        String contentDisposition = "disposition";
        String contentEncoding = "encoding";
        String contentLanguage = "language";
        String contentType = "type";
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;
        String accountName = "accountName";
        UserDelegationKey userDelegationKey = new UserDelegationKey();

        String sas = client.generateUserDelegationSAS(userDelegationKey, accountName, permissions, expiryTime, startTime, version, sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);
        // END: com.azure.storage.blob.ContainerClient.generateUserDelegationSAS
    }

    /**
     * Code snippet for {@link ContainerClient#generateSAS(String, ContainerSASPermission, OffsetDateTime, OffsetDateTime, String, SASProtocol, IPRange, String, String, String, String, String)}
     */
    public void generateSASCodeSnippets() {
        // BEGIN: com.azure.storage.blob.ContainerClient.generateSAS
        ContainerSASPermission permissions = new ContainerSASPermission()
            .read(true)
            .write(true)
            .create(true)
            .delete(true)
            .add(true)
            .list(true);
        OffsetDateTime startTime = OffsetDateTime.now().minusDays(1);
        OffsetDateTime expiryTime = OffsetDateTime.now().plusDays(1);
        IPRange ipRange = new IPRange()
            .ipMin("0.0.0.0")
            .ipMax("255.255.255.255");
        SASProtocol sasProtocol = SASProtocol.HTTPS_HTTP;
        String cacheControl = "cache";
        String contentDisposition = "disposition";
        String contentEncoding = "encoding";
        String contentLanguage = "language";
        String contentType = "type";
        String identifier = "";
        String version = Constants.HeaderConstants.TARGET_STORAGE_VERSION;

        // Note either "identifier", or "expiryTime and permissions" are required to be set
        String sas = client.generateSAS(identifier, permissions, expiryTime, startTime, version, sasProtocol, ipRange, cacheControl, contentDisposition, contentEncoding, contentLanguage, contentType);
        // END: com.azure.storage.blob.ContainerClient.generateSAS
    }
}
