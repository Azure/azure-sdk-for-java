package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.AnonymousCredentials
import com.microsoft.azure.storage.blob.BlockBlobURL
import com.microsoft.azure.storage.blob.ContainerURL
import com.microsoft.azure.storage.blob.Metadata
import com.microsoft.azure.storage.blob.PipelineOptions
import com.microsoft.azure.storage.blob.ServiceSASSignatureValues
import com.microsoft.azure.storage.blob.StorageException
import com.microsoft.azure.storage.blob.StorageURL
import com.microsoft.azure.storage.blob.models.SignedIdentifier

import java.time.OffsetDateTime

class HelperTest extends APISpec {

    def "responseError auth detail"() {
        setup:
        ServiceSASSignatureValues values = new ServiceSASSignatureValues()
        values.containerName = "containerName" // mismatching container name should give auth failure
        values.permissions = "r"
        values.expiryTime = OffsetDateTime.now().plusMinutes(1)
        URL sasURL = new URL(cu.toString() + values.generateSASQueryParameters(getPrimaryCreds()).encode())
        ContainerURL sasContainer = new ContainerURL(sasURL,
                StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()))

        when:
        sasContainer.getProperties(null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.body().code() == "AuthenticationFailed"
        e.body().authenticationErrorDetail().contains("Signature did not match.")
    }

    def "responseError queryName"() {
        when:
        cu.listBlobsFlatSegment("garbage", null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.body().code() == "InvalidQueryParameterValue"
        e.body().queryParameterName() == "marker"
        e.body().queryParameterValue() == "garbage"
    }

    def "responseError headerName"() {
        when:
        // Lease duration can be -1 or [15,60]
        cu.acquireLease(null, 70, null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.body().code() == "InvalidHeaderValue"
        e.body().headerName() == "x-ms-lease-duration"
        e.body().headerValue() == "70"
    }

    def "responseError xmlDocument"() {
        setup:
        List<SignedIdentifier> ids = new ArrayList<>()
        SignedIdentifier id = new SignedIdentifier().withId(
                "invalidtoolong----------------------------------------------------------------------------------")
        ids.add(id)

        when:
        cu.setAccessPolicy(null, ids, null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.body().code() == "InvalidXmlDocument"
        e.body().lineNumber() == 1
        e.body().linePosition() == 202
        e.body().reason().contains("Signed identifier ID cannot be empty")
    }

    def "responseError metadataValue"() {
        setup:
        Metadata metadata = new Metadata()
        metadata.put("", "value")

        when:
        cu.setMetadata(metadata, null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.body().metadataValue() == "value"
    }

    def "responseError invlidBlock"() {
        setup:
        BlockBlobURL bu = cu.createBlockBlobURL(generateBlobName())
        List<String> ids = new ArrayList<>()
        ids.add("notstaged")

        when:
        bu.commitBlockList(ids, null, null, null).blockingGet()

        then:
        def e = thrown(StorageException)
        e.body().invalidBlockId() == "notstaged"
        e.errorCode() == "InvalidBlockId"
    }

    /*
     MinimumAllowed and MaximumAllowed are not testable because the protocol layer will perform client-side validation
     in all cases where the service will return these elements.
     Unable to force service to return XmlNodeName and XmlNodeValue for a similar reason.
     */

}
