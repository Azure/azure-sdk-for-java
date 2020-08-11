package com.azure.storage.blob.nio

import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.models.AccessTier
import com.azure.storage.blob.models.BlobHttpHeaders
import spock.lang.Unroll

import java.nio.file.attribute.FileTime
import java.security.MessageDigest

class AttributeViewTest extends APISpec {
    /*
    Get attributes--All properties set
     */
    BlobClient bc
    AzureFileSystem fs

    def setup() {
        fs = createFS(initializeConfigMap())
        cc = rootNameToContainerClient(getDefaultDir(fs))
        bc = cc.getBlobClient(generateBlobName())
        bc.upload(defaultInputStream.get(), defaultDataSize)
    }

    def cleanup() {
        cc.delete()
    }

    def "AzureBasicFileAttributeView readAttributes"() {
        setup:
        def path = fs.getPath(bc.getBlobName())

        when:
        def attr = new AzureBasicFileAttributeView(path).readAttributes()
        def props = bc.getProperties()

        then:
        props.getBlobSize() == attr.size()
        FileTime.from(props.getLastModified().toInstant()) == attr.lastModifiedTime()
        FileTime.from(props.getCreationTime().toInstant()) == attr.creationTime()
        attr.isRegularFile()
        !attr.isDirectory()
        !attr.isSymbolicLink()
        !attr.isOther()
    }

    def "AzureBasicFileAttributeView directory"() {
        setup:
        def path = fs.getPath(generateBlobName())
        putDirectoryBlob(new AzureResource(path).getBlobClient().getBlockBlobClient())

        when:
        def attr = new AzureBasicFileAttributeView(path).readAttributes()

        then:
        attr.isDirectory()
        !attr.isRegularFile()
        !attr.isOther()
        !attr.isSymbolicLink()
    }

    def "AzureBasicFileAttributeView fs closed"() {
        setup:
        def path = fs.getPath(generateBlobName())

        when:
        fs.close()
        new AzureBasicFileAttributeView(path).readAttributes()

        then:
        thrown(IOException)
    }

    def "AzureBlobFileAttributeView readAttributes"() {
        setup:
        def path = fs.getPath(bc.getBlobName())

        when:
        def attr = new AzureBlobFileAttributeView(path).readAttributes()
        def suppliers = AzureBlobFileAttributes.getAttributeSuppliers(attr)
        def props = bc.getProperties()

        then:
        // getters
        props.getBlobSize() == attr.size()
        FileTime.from(props.getLastModified().toInstant()) == attr.lastModifiedTime()
        FileTime.from(props.getCreationTime().toInstant()) == attr.creationTime()
        attr.isRegularFile()
        !attr.isDirectory()
        !attr.isSymbolicLink()
        !attr.isOther()
        props.getETag() == attr.eTag()
        props.getContentType() == attr.blobHttpHeaders().getContentType()
        props.getContentMd5() == attr.blobHttpHeaders().getContentMd5()
        props.getContentLanguage() == attr.blobHttpHeaders().getContentLanguage()
        props.getContentEncoding() == attr.blobHttpHeaders().getContentEncoding()
        props.getContentDisposition() == attr.blobHttpHeaders().getContentDisposition()
        props.getCacheControl() == attr.blobHttpHeaders().getCacheControl()
        props.getBlobType() == attr.blobType()
        props.getCopyId() == attr.copyId()
        props.getCopyStatus() == attr.copyStatus()
        props.getCopySource() == attr.copySource()
        props.getCopyProgress() == attr.copyProgress()
        props.getCopyCompletionTime() == attr.copyCompletionTime()
        props.getCopyStatusDescription() == attr.copyStatusDescription()
        props.isServerEncrypted() == attr.isServerEncrypted()
        props.getAccessTier() == attr.accessTier()
        props.isAccessTierInferred() == attr.isAccessTierInferred()
        props.getArchiveStatus() == attr.archiveStatus()
        props.getAccessTierChangeTime() == attr.accessTierChangeTime()
        props.getMetadata() == attr.metadata()

        /*
        suppliers. Used in FileSystemProvider.readAttributes(String)
        Unlike the consumers used for setting properties, we test these here rather than
        on the FileSystemProvider because there are so many of them and it is more feasible this way rather
        than having a test for each method like the consumers.
         */
        props.getBlobSize() == suppliers.get("size").get()
        FileTime.from(props.getLastModified().toInstant()) == suppliers.get("lastModifiedTime").get()
        FileTime.from(props.getCreationTime().toInstant()) == suppliers.get("creationTime").get()
        attr.isRegularFile()
        !attr.isDirectory()
        !attr.isSymbolicLink()
        !attr.isOther()
        props.getETag() == suppliers.get("eTag").get()
        props.getContentType() == ((BlobHttpHeaders) suppliers.get("blobHttpHeaders").get()).getContentType()
        props.getContentMd5() == ((BlobHttpHeaders) suppliers.get("blobHttpHeaders").get()).getContentMd5()
        props.getContentLanguage() == ((BlobHttpHeaders) suppliers.get("blobHttpHeaders").get()).getContentLanguage()
        props.getContentEncoding() == ((BlobHttpHeaders) suppliers.get("blobHttpHeaders").get()).getContentEncoding()
        props.getContentDisposition() == ((BlobHttpHeaders) suppliers.get("blobHttpHeaders").get())
            .getContentDisposition()
        props.getCacheControl() == ((BlobHttpHeaders) suppliers.get("blobHttpHeaders").get()).getCacheControl()
        props.getBlobType() == suppliers.get("blobType").get()
        props.getCopyId() == suppliers.get("copyId").get()
        props.getCopyStatus() == suppliers.get("copyStatus").get()
        props.getCopySource() == suppliers.get("copySource").get()
        props.getCopyProgress() == suppliers.get("copyProgress").get()
        props.getCopyCompletionTime() == suppliers.get("copyCompletionTime").get()
        props.getCopyStatusDescription() == suppliers.get("copyStatusDescription").get()
        props.isServerEncrypted() == suppliers.get("isServerEncrypted").get()
        props.getAccessTier() == suppliers.get("accessTier").get()
        props.isAccessTierInferred() == suppliers.get("isAccessTierInferred").get()
        props.getArchiveStatus() == suppliers.get("archiveStatus").get()
        props.getAccessTierChangeTime() == suppliers.get("accessTierChangeTime").get()
        props.getMetadata() == suppliers.get("metadata").get()
    }

    def "AzureBlobFileAttributeView read fs closed"() {
        setup:
        def path = fs.getPath(generateBlobName())

        when:
        fs.close()
        new AzureBlobFileAttributeView(path).readAttributes()

        then:
        thrown(IOException)
    }

    @Unroll
    def "AzureBlobFileAttributeView setBlobHttpHeaders"() {
        setup:
        def path = fs.getPath(bc.getBlobName())
        def view = new AzureBlobFileAttributeView(path)
        def headers = new BlobHttpHeaders().setCacheControl(cacheControl)
            .setContentDisposition(contentDisposition)
            .setContentEncoding(contentEncoding)
            .setContentLanguage(contentLanguage)
            .setContentMd5(contentMD5)
            .setContentType(contentType)

        when:
        view.setBlobHttpHeaders(headers)
        def response = bc.getProperties()

        then:
        response.getCacheControl() == cacheControl
        response.getContentDisposition() == contentDisposition
        response.getContentEncoding() == contentEncoding
        response.getContentLanguage() == contentLanguage
        response.getContentMd5() == contentMD5
        response.getContentType() == contentType

        where:
        cacheControl | contentDisposition | contentEncoding | contentLanguage | contentMD5                                                                               | contentType
        null         | null               | null            | null            | null                                                                                     | null
        "control"    | "disposition"      | "encoding"      | "language"      | Base64.getEncoder().encode(MessageDigest.getInstance("MD5").digest(defaultData.array())) | "type"
    }

    def "AzureBlobFileAttributeView setHeaders fs closed"() {
        setup:
        def path = fs.getPath(generateBlobName())

        when:
        fs.close()
        new AzureBlobFileAttributeView(path).setBlobHttpHeaders(new BlobHttpHeaders())

        then:
        thrown(IOException)
    }

    @Unroll
    def "AzureBlobFileAttributeView setMetadata"() {
        setup:
        def path = fs.getPath(bc.getBlobName())
        def view = new AzureBlobFileAttributeView(path)
        def metadata = new HashMap<String, String>()
        if (key1 != null && value1 != null) {
            metadata.put(key1, value1)
        }
        if (key2 != null && value2 != null) {
            metadata.put(key2, value2)
        }

        when:
        view.setMetadata(metadata)

        then:
        bc.getProperties().getMetadata() == metadata

        where:
        key1  | value1 | key2   | value2 || statusCode
        null  | null   | null   | null   || 200
        "foo" | "bar"  | "fizz" | "buzz" || 200
        "i0"  | "a"    | "i_"   | "a"    || 200 /* Test culture sensitive word sort */
    }

    def "AzureBlobFileAttributeView setMetadata fs closed"() {
        setup:
        def path = fs.getPath(generateBlobName())

        when:
        fs.close()
        new AzureBlobFileAttributeView(path).setMetadata(Collections.emptyMap())

        then:
        thrown(IOException)
    }

    @Unroll
    def "AzureBlobFileAttributeView setTier"() {
        setup:
        def path = fs.getPath(bc.getBlobName())
        def view = new AzureBlobFileAttributeView(path)

        when:
        view.setTier(tier)

        then:
        bc.getProperties().getAccessTier() == tier

        where:
        tier            | _
        AccessTier.HOT  | _
        AccessTier.COOL | _
        /*
        We don't test archive because it takes a while to take effect, and testing these two demonstrates that the tier
        is successfully being passed to the underlying client.
         */
    }

    def "AzureBlobFileAttributeView setTier fs closed"() {
        setup:
        def path = fs.getPath(generateBlobName())

        when:
        fs.close()
        new AzureBlobFileAttributeView(path).setTier(AccessTier.HOT)

        then:
        thrown(IOException)
    }

    @Unroll
    def "AttributeView setTimes unsupported"() {
        setup:
        def path = fs.getPath(bc.getBlobName())
        def blobView = new AzureBlobFileAttributeView(path)
        def basicView = new AzureBasicFileAttributeView(path)

        when:
        blobView.setTimes(t1, t2, t3)

        then:
        thrown(UnsupportedOperationException)

        when:
        basicView.setTimes(t1, t2, t3)

        then:
        thrown(UnsupportedOperationException)

        where:
        t1                                              | t2                                              | t3
        FileTime.fromMillis(System.currentTimeMillis()) | null                                            | null
        null                                            | FileTime.fromMillis(System.currentTimeMillis()) | null
        null                                            | null                                            | FileTime.fromMillis(System.currentTimeMillis())
    }
}
