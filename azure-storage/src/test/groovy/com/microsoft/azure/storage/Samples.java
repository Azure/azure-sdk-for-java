package com.microsoft.azure.storage;

import com.microsoft.azure.storage.blob.*;
import com.microsoft.azure.storage.blob.models.*;
import com.microsoft.rest.v2.RestException;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpPipelineLogLevel;
import com.microsoft.rest.v2.http.HttpPipelineLogger;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.reactivex.*;
import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Function;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Samples {
    private String getAccountName() {
        return System.getenv("ACCOUNT_NAME");
    }

    private String getAccountKey() {
        return System.getenv("ACCOUNT_KEY");
    }

    /**
     * This example shows how to start using the Azure Storage Blob SDK for Java.
     */
    @Test
    public void basicExample() throws InvalidKeyException, MalformedURLException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Use your Storage account's name and key to create a credential object; this is used to access your account.
        SharedKeyCredentials credential = new SharedKeyCredentials(accountName, accountKey);

        /*
        Create a request pipeline that is used to process HTTP(S) requests and responses. It requires your accont
        credentials. In more advanced scenarios, you can configure telemetry, retry policies, logging, and other
        options. Also you can configure multiple pipelines for different scenarios.
         */
        HttpPipeline pipeline = StorageURL.createPipeline(credential, new PipelineOptions());

        /*
        From the Azure portal, get your Storage account blob service URL endpoint.
        The URL typically looks like this:
         */
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName));

        // Create a ServiceURL objet that wraps the service URL and a request pipeline.
        ServiceURL serviceURL = new ServiceURL(u, pipeline);

        // Now you can use the ServiceURL to perform various container and blob operations.

        // This example shows several common operations just to get you started.

        /*
        Create a URL that references a to-be-created container in your Azure Storage account. This returns a
        ContainerURL object that wraps the container's URL and a request pipeline (inherited from serviceURL).
        Note that container names require lowercase.
         */
        ContainerURL containerURL = serviceURL.createContainerURL("myjavacontainerbasic");

        /*
        Create a URL that references a to-be-created blob in your Azure Storage account's container.
        This returns a BlockBlobURL object that wraps the blob's URl and a request pipeline
        (inherited from containerURL). Note that blob names can be mixed case.
         */
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("HelloWorld.txt");

        String data = "Hello world!";

        // Create the container on the service (with no metadata and no public access)
        containerURL.create(null, null)
                .flatMap(containersCreateResponse ->
                        /*
                         Create the blob with string (plain text) content.
                         NOTE: It is imperative that the provided length matches the actual length exactly.
                         */
                        blobURL.upload(Flowable.just(ByteBuffer.wrap(data.getBytes())), data.length(),
                                null, null, null)
                )
                .flatMap(blobsDownloadResponse ->
                        // Download the blob's content.
                        blobURL.download(null, null, false))
                .flatMap(blobsDownloadResponse ->
                        // Verify that the blob data round-tripped correctly.
                        FlowableUtil.collectBytesInBuffer(blobsDownloadResponse.body())
                                .doOnSuccess(byteBuffer -> {
                                    if (byteBuffer.compareTo(ByteBuffer.wrap(data.getBytes())) != 0) {
                                        throw new Exception("The downloaded data does not match the uploaded data.");
                                    }
                                }))
                .flatMap(byteBuffer ->
                        /*
                         List the blob(s) in our container; since a container may hold millions of blobs, this is done
                         one segment at a time.
                         */
                        containerURL.listBlobsFlatSegment(null, new ListBlobsOptions(null, null,
                                1)))
                .flatMap(containersListBlobFlatSegmentResponse ->
                        // The asynchronous requests require we use recursion to continue our listing.
                        listBlobsHelper(containerURL, containersListBlobFlatSegmentResponse))
                .flatMap(containersListBlobFlatSegmentResponse ->
                        // Delete the blob we created earlier.
                        blobURL.delete(null, null))
                .flatMap(blobsDeleteResponse ->
                        // Delete the container we created earlier.
                        containerURL.delete(null))
                /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
    }

    public Single<ContainersListBlobFlatSegmentResponse> listBlobsHelper(
            ContainerURL containerURL, ContainersListBlobFlatSegmentResponse response) {

        // Process the blobs returned in this result segment (if the segment is empty, blob() will be null.
        if (response.body().blobs().blob() != null) {
            for (Blob b : response.body().blobs().blob()) {
                String output = "Blob name: " + b.name();
                if (b.snapshot() != null) {
                    output += ", Snapshot: " + b.snapshot();
                }
                System.out.println(output);
            }
        }

        // If there is not another segment, return this response as the final response.
        if (response.body().nextMarker() == null) {
            return Single.just(response);
        } else {
            /*
             IMPORTANT: ListBlobsFlatSegment returns the start of the next segment; you MUST use this to get the next
             segment (after processing the current result segment
             */
            String nextMarker = response.body().nextMarker();

            /*
            The presence of the marker indicates that there are more blobs to list, so we make another call to
            listBlobsFlatSegment and pass the result through this helper function.
             */
            return containerURL.listBlobsFlatSegment(nextMarker, new ListBlobsOptions(null, null,
                    1))
                    .flatMap(containersListBlobFlatSegmentResponse ->
                            listBlobsHelper(containerURL, containersListBlobFlatSegmentResponse));
        }
    }

    // This example shows how you can configure a pipeline for making HTTP requests to the Azure Storage blob Service.
    @Test
    public void exampleNewPipeline() throws MalformedURLException {
        // This shows how to wire in your own logging mechanism. Here we use the built in java logger.
        Logger logger = Logger.getGlobal();

        /*
         Create/configure a request pipeline options object. All PipelineOptions' fields are optional;
         reasonable defaults are set for anything you do not specify.
         */
        PipelineOptions po = new PipelineOptions();

        /*
        Set RetryOptions to control how HTTP requests are retried when retryable failures occur.
        Here we:
        - Use exponential backoff as opposed to linear.
        - Try at most 3 times to perform the operation (set to 1 to disable retries).
        - Maximum 3 seconds allowed for any single try.
        - Backoff delay starts at 1 second.
        - Maximum delay between retries is 3 seconds.
        - We will not retry against a secondary host.
         */
        po.requestRetryOptions = new RequestRetryOptions(RetryPolicyType.EXPONENTIAL, 3, 3,
                1000L, 3000L, null);

        /*
         Set LoggingOptions to control how each HTTP request and its response is logged. A successful response taking
         more than 200ms will be logged as a warning.
         */
        po.loggingOptions = new LoggingOptions(200);

        // Set LogOptions to control what & where all pipeline log events go.
        po.logger = new HttpPipelineLogger() {
            @Override
            public HttpPipelineLogLevel minimumLogLevel() {
                // Log all events from informational to more severe.
                return HttpPipelineLogLevel.INFO;
            }

            @Override
            public void log(HttpPipelineLogLevel httpPipelineLogLevel, String s, Object... objects) {
                // This function is called to log each event. It is not called for filtered-out severities.
                Level level = null;
                if (httpPipelineLogLevel == HttpPipelineLogLevel.ERROR) {
                    level = Level.SEVERE;
                } else if (httpPipelineLogLevel == HttpPipelineLogLevel.WARNING) {
                    level = Level.WARNING;
                } else if (httpPipelineLogLevel == HttpPipelineLogLevel.INFO) {
                    level = Level.INFO;
                } else if (httpPipelineLogLevel == HttpPipelineLogLevel.OFF) {
                    level = Level.OFF;
                }
                logger.log(level, s);
            }
        };

        /*
        Create a request pipeline object configured with credentials and with pipeline options. Once created, a
        pipeline object is thread-safe and can be safely used with many XxxURL objects simultaneously. A pipeline
        always requires some credential object.
         */
        HttpPipeline p = ServiceURL.createPipeline(new AnonymousCredentials(), po);

        // Once you've created a pipeline object, associate it with an XxxURL object so that you can perform HTTP
        // requests with it.
        URL u = new URL("https://myaccount.blob.core.windows.net");
        ServiceURL serviceURL = new ServiceURL(u, p);

        // Use the serviceURL as desired...

        /*
        NOTE: When you using an XxxURL object to create another XxxURl object, the new XxxURL object inherits the same
        pipeline object as its parent. For example, the containerURL and blobURL objects below all share the same
        pipeline. Any operations you perform with these objects will share the same behavior configured above.
         */
        ContainerURL containerURL = serviceURL.createContainerURL("mycontainer");
        BlobURL blobURL = containerURL.createBlobURL("ReadMe.txt");

        /*
        If you would like to perform some operations with different behavior, create a new pipeline object and associate
        it with a new XxxURL object by passing the new pipeline to the XxxURL object's withPipeline method.
         */

        /*
        In this example, we reconfigure the retry policies, create a new pipeline, and then create a new ContainerURL
        object that has the same URL as its parent.

        Here we:
        - Use exponential backoff as opposed to linear.
        - Try at most  times to perform the operation (set to 1 to disable retries).
        - Maximum 1 minute allowed for any single try.
        - Backoff delay starts at 5 seconds.
        - Maximum delay between retries is 10 seconds.
        - We will not retry against a secondary host.
         */
        po.requestRetryOptions = new RequestRetryOptions(RetryPolicyType.EXPONENTIAL, 4, 60,
                5000L, 10000L, null);
        ContainerURL newContainerURL = containerURL.withPipeline(
                ServiceURL.createPipeline(new AnonymousCredentials(), po));

        /*
        Now any XxxBlobURL object created using newContainerURL inherits the pipeline with the new retry policy.
         */
        BlobURL newBlobURL = newContainerURL.createBlobURL("ReadMe2.txt");
    }

    @Test
    public void exampleStorageError() {
        // TODO: Once we add better error code support.
    }

    /*
    This example shows how to break a URL into its parts so you can examine and/or change some of its values and then
    construct a new URL.
     */
    @Test
    public void exampleBlobURLParts() throws MalformedURLException, UnknownHostException {
        /*
         Start with a URL that identifies a snapshot of a blob in a container and includes a Shared Access Signature
         (SAS).
         */
        URL u = new URL("https://myaccount.blob.core.windows.net/mycontainter/ReadMe.txt?" +
                "snapshot=2011-03-09T01:42:34.9360000Z" +
                "&sv=2015-02-21&sr=b&st=2111-01-09T01:42:34Z&se=2222-03-09T01:42:34Z&sp=rw" +
                "&sip=168.1.5.60-168.1.5.70&spr=https,http&si=myIdentifier&ss=bf&srt=s" +
                "&sig=92836758923659283652983562==");

        // You can parse this URL into its constituent parts:
        BlobURLParts parts = URLParser.parse(u);

        // Now, we access the parts (this example prints them).
        System.out.println(String.join("\n",
                parts.host,
                parts.containerName,
                parts.blobName,
                parts.snapshot));
        System.out.println("");
        SASQueryParameters sas = parts.sasQueryParameters;
        System.out.println(String.join("\n",
                sas.getVersion(),
                sas.getResource(),
                sas.getStartTime().toString(),
                sas.getExpiryTime().toString(),
                sas.getPermissions(),
                sas.getIpRange().toString(),
                sas.getProtocol().toString(),
                sas.getIdentifier(),
                sas.getServices(),
                sas.getSignature()));

        // You can then change some of the fields and construct a new URL.
        parts.sasQueryParameters = null; // Remove the SAS query parameters.
        parts.snapshot = null; // Remove the snapshot timestamp.
        parts.containerName = "othercontainer"; // Change the container name.
        // In this example, we'll keep the blob name as it is.

        // Construct a new URL from the parts:
        URL newURL = parts.toURL();
        System.out.println(newURL);
        // NOTE: You can pass the new URL to the constructor for any XxxURL to manipulate the resource.
    }

    // This example shows how to create and use an Azure Storage account Shared Access Signature(SAS).
    @Test
    public void exampleAccountSASSignatureValues() throws InvalidKeyException, MalformedURLException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Use your Storage account's name and key to create a credential object; this is required to sign a SAS.
        SharedKeyCredentials credential = new SharedKeyCredentials(accountName, accountKey);

        /*
        Set the desired SAS signature values and sign them with the shared key credentials to get the SAS query
        parameters.
         */
        AccountSASSignatureValues values = new AccountSASSignatureValues();
        values.protocol = SASProtocol.HTTPS_ONLY; // Users MUST use HTTPS (not HTTP).
        values.expiryTime = OffsetDateTime.now().plusDays(2); // 2 days before expiration.

        AccountSASPermission permission = new AccountSASPermission();
        permission.read = true;
        permission.list = true;
        values.permissions = permission.toString();

        AccountSASService service = new AccountSASService();
        service.blob = true;
        values.services = service.toString();

        AccountSASResourceType resourceType = new AccountSASResourceType();
        resourceType.container = true;
        resourceType.object = true;
        values.resourceTypes = resourceType.toString();

        SASQueryParameters params = values.generateSASQueryParameters(credential);

        // Calling encode will generate the query string.
        String encodedParams = params.encode();

        String urlToSendToSomeone = String.format(Locale.ROOT, "https://%s.blob.core.windows.net?%s",
                accountName, encodedParams);
        // At this point, you can send the urlToSendSomeone to someone via email or any other mechanism you choose.

        // ***************************************************************************************************

        // When someone receives the URL, the access the SAS-protected resource with code like this:
        URL u = new URL(urlToSendToSomeone);

        /*
         Create a ServiceURL object that wraps the serviceURL (and its SAS) and a pipeline. When using SAS URLs,
         AnonymousCredentials are required.
         */
        ServiceURL serviceURL = new ServiceURL(u,
                StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()));
        // Now, you can use this serviceURL just like any other to make requests of the resource.
    }

    // This example shows how to create and use a Blob Service Shared Access Signature (SAS).
    @Test
    public void exampleBlobSASSignatureValues() throws InvalidKeyException, MalformedURLException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Use your Storage account's name and key to create a credential object; this is required to sign a SAS.
        SharedKeyCredentials credential = new SharedKeyCredentials(accountName, accountKey);

        // This is the name of the container and blob that we're creating a SAS to.
        String containerName = "mycontainer"; // Container names require lowercase.
        String blobName = "HelloWorld.txt"; // Blob names can be mixed case.

        /*
        Set the desired SAS signature values and sign them with the shared key credentials to get the SAS query
        parameters.
         */
        ServiceSASSignatureValues values = new ServiceSASSignatureValues();
        values.protocol = SASProtocol.HTTPS_ONLY; // Users MUST use HTTPS (not HTTP).
        values.expiryTime = OffsetDateTime.now().plusDays(2); // 2 days before expiration.
        values.containerName = containerName;
        values.blobName = blobName;

        /*
        To produce a container SAS (as opposed to a blob SAS), assign to Permissions using ContainerSASPermissions, and
        make sure the blobName field is null (the default).
         */
        BlobSASPermission permission = new BlobSASPermission();
        permission.read = true;
        permission.add = true;
        permission.write = true;
        values.permissions = permission.toString();

        SASQueryParameters params = values.generateSASQueryParameters(credential);

        // Calling encode will generate the query string.
        String encodedParams = params.encode();

        String urlToSendToSomeone = String.format(Locale.ROOT, "https://%s.blob.core.windows.net/%s/%s?%s",
                accountName, containerName, blobName, encodedParams);
        // At this point, you can send the urlToSendSomeone to someone via email or any other mechanism you choose.

        // ***************************************************************************************************

        // When someone receives the URL, the access the SAS-protected resource with code like this:
        URL u = new URL(urlToSendToSomeone);

        /*
         Create a BlobURL object that wraps the blobURL (and its SAS) and a pipeline. When using SAS URLs,
         AnonymousCredentials are required.
         */
        BlobURL blobURL = new BlobURL(u,
                StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()));
        // Now, you can use this blobURL just like any other to make requests of the resource.
    }

    // This example shows how to manipulate a container's permissions.
    @Test
    public void exampleContainerURL_SetPermissions() throws InvalidKeyException, MalformedURLException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Use your Storage account's name and key to create a credential object; this is required to sign a SAS.
        SharedKeyCredentials credential = new SharedKeyCredentials(accountName, accountKey);

        // Create a containerURL object that wraps the container's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/myjavacontainerpermissions",
                accountName));
        ContainerURL containerURL = new ContainerURL(u, StorageURL.createPipeline(credential, new PipelineOptions()));

        /*
         Create a URL that references a to-be-created blob in your Azure Storage account's container. This returns a
         BlockBlobURL object that wraps the blob's URL and a request pipeline (inherited from containerURL).
         */
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("HelloWorld.txt");

        // A blob URL with anonymous credentials to demonstrate public access.
        BlobURL anonymousURL = new BlobURL(blobURL.toURL(),
                StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()));

        String data = "Hello World!";

        // Create the container (with no metadata and no public access)
        containerURL.create(null, null)
                .flatMap(containersCreateResponse ->
                        blobURL.upload(Flowable.just(ByteBuffer.wrap(data.getBytes())), data.length(),
                                null, null, null)
                )
                .flatMap(blockBlobsUploadResponse ->
                        // Attempt to read the blob with anonymous credentials.
                        anonymousURL.download(null, null, false)
                )
                .toCompletable()
                .onErrorResumeNext(throwable -> {
                    /*
                    We expected this error because the service returns an HTTP 404 status code when a blob exists but
                    the request does not have permission to access it.
                     */
                    if (throwable instanceof RestException &&
                            ((RestException) throwable).response().statusCode() == 404) {
                        // This is how we change the container's permission to allow public/anonymous access.
                        return containerURL.setAccessPolicy(PublicAccessType.BLOB, null, null)
                                .toCompletable();
                    } else {
                        return Completable.error(throwable);
                    }
                })
                /*
                 Container property changes may take up to 15 seconds to take effect. It would also be possible to poll
                 the container properties to check for the access policy to be updated. See the startCopy example for
                 an example of this pattern.
                 */
                .delay(31, TimeUnit.SECONDS)
                // Now this will work.
                .andThen(anonymousURL.download(null, null,
                                false))
                .flatMap(blobsDownloadResponse ->
                        // Delete the container and the blob within in.
                        containerURL.delete(null))
                /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
    }

    // This example shows how to perform operations on blobs conditionally.
    @Test
    public void exampleBlobAccessConditions() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontaineraccessconditions");
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("Data.txt");

        // Create the container (unconditionally; succeeds)
        containerURL.create(null, null)
                .flatMap(containersCreateResponse ->
                        // Create the blob (unconditionally; succeeds)
                        blobURL.upload(Flowable.just(ByteBuffer.wrap("Text-1".getBytes())), "Text-1".length(),
                                null, null, null))
                .flatMap(blockBlobsUploadResponse -> {
                    System.out.println("Success: " + blockBlobsUploadResponse.statusCode());

                    // Download blob content if the blob has been modified since we uploaded it (fails).
                    return blobURL.download(null,
                            new BlobAccessConditions(
                                    new HTTPAccessConditions(
                                            blockBlobsUploadResponse.headers().lastModified(),
                                            null,
                                            null,
                                            null),
                                    null,
                                    null,
                                    null),
                            false);
                })
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof RestException) {
                        System.out.println("Failure: " + ((RestException) throwable).response().statusCode());
                    } else {
                        return Single.error(throwable); // Network failure.
                    }
                    // Download the blob content if the blob hasn't been modified in the last 24 hours (fails):
                    return blobURL.download(null,
                            new BlobAccessConditions(
                                    new HTTPAccessConditions(
                                            null,
                                            OffsetDateTime.now().minusDays(1),
                                            null,
                                            null),
                                    null,
                                    null,
                                    null),
                            false);
                })
                /*
                 onErrorResume next expects to return a Single of the same type. Here, we are changing operations, which
                 means we will get a different return type and cannot directly recover from the error. To solve this,
                 we go through a completable which will give us more flexibility with types.
                 */
                .toCompletable()
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof RestException) {
                        System.out.println("Failure: " + ((RestException) throwable).response().statusCode());
                    } else {
                        return Completable.error(throwable);
                    }
                    // We've logged the error, and now returning an empty Completable allows us to change course.
                    return Completable.complete();
                })
                // Get the blob properties to retrieve the current ETag.
                .andThen(blobURL.getProperties(null))
                .flatMap(getPropertiesResponse ->
                        /*
                         Upload new content if the blob hasn't changed since the version identified by the ETag
                         (succeeds).
                         */
                        blobURL.upload(Flowable.just(ByteBuffer.wrap("Text-2".getBytes())), "Text-2".length(),
                                null, null,
                                new BlobAccessConditions(
                                        new HTTPAccessConditions(
                                                null,
                                                null,
                                                new ETag(getPropertiesResponse.headers().eTag()),
                                                null),
                                        null,
                                        null,
                                        null))
                )
                .flatMap(blockBlobsUploadResponse -> {
                    System.out.println("Success: " + blockBlobsUploadResponse.statusCode());

                    // Download content if it has changed since the version identified by ETag (fails):
                    return blobURL.download(null,
                            new BlobAccessConditions(
                                    new HTTPAccessConditions(
                                            null,
                                            null,
                                            null,
                                            new ETag(blockBlobsUploadResponse.headers().eTag())
                                    ),
                                    null,
                                    null,
                                    null
                            ), false);
                })
                .toCompletable()
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof RestException) {
                        System.out.println("Failure: " + ((RestException) throwable).response().statusCode());
                    } else {
                        return Completable.error(throwable);
                    }
                    // We've logged the error, and now returning an empty Completable allows us to change course.
                    return Completable.complete();
                }).andThen(
                // Delete the blob if it exists (succeeds).
                blobURL.delete(DeleteSnapshotsOptionType.INCLUDE,
                        new BlobAccessConditions(
                                new HTTPAccessConditions(
                                        null,
                                        null,
                                        ETag.ANY,
                                        null),
                                null,
                                null,
                                null)
                )
        )
                .flatMap(blobsDeleteResponse -> {
                    System.out.println("Success: " + blobsDeleteResponse.statusCode());
                    return containerURL.delete(null);
                })
                /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
    }

    // This example shows how to create a container with metadata and then how to read & update the metadata.
    @Test
    public void exampleMetadata_containers() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a ContainerURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainercontainermetadata");

        /*
         Create a container with some metadata (string key/value pairs).
         NOTE: Metadata key names are always converted to lowercase before being sent to the Storage Service. Therefore,
         you should always use lowercase letters; especially when querying a map for a metadata key.
         */
        Metadata metadata = new Metadata();
        metadata.put("createdby", "Rick");
        metadata.put("createdon", "4/13/18");
        containerURL.create(metadata, null)
                .flatMap(containersCreateResponse ->
                        // Query the container's metadata.
                        containerURL.getProperties(null)
                )
                .flatMap(containersGetPropertiesResponse -> {
                    Metadata receivedMetadata = new Metadata(containersGetPropertiesResponse.headers().metadata());

                    // Show the container's metadata.
                    System.out.println(receivedMetadata);

                    // Update the metadata and write it back to the container.
                    receivedMetadata.put("createdby", "Mary"); // NOTE: The keyname is in all lowercase.
                    return containerURL.setMetadata(receivedMetadata, null);
                })
                .flatMap(response -> containerURL.delete(null))
                /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();

        // NOTE: The SetMetadata & SetProperties methods update the container's ETag & LastModified properties.
    }

    /*
    This example shows how to create a blob with metadata and then how to read & update the blob's read-only properties
    and metadata.
     */
    @Test
    public void exampleMetadata_blob() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainerblobmetadata");
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("Data.txt");

        // Create the container.
        containerURL.create(null, null)
                .flatMap(containersCreateResponse -> {
                    /*
                    Create the blob with metadata (string key/value pairs).
                    NOTE: Metadata key names are always converted to lowercase before being sent to the Storage
                    Service. Therefore, you should always use lowercase letters; especially when querying a map for
                    a metadata key.
                     */
                    Metadata metadata = new Metadata();
                    metadata.put("createdby", "Rick");
                    metadata.put("createdon", "4/13/18");
                    return blobURL.upload(Flowable.just(ByteBuffer.wrap("Text-1".getBytes())), "Text-1".length(),
                            null, metadata, null);
                })
                .flatMap(response ->
                        // Query the blob's properties and metadata.
                        blobURL.getProperties(null))
                .flatMap(response -> {
                    // Show some of the blob's read-only properties.
                    System.out.println(response.headers().blobType());
                    System.out.println(response.headers().eTag());
                    System.out.println(response.headers().lastModified());

                    // Show the blob's metadata.
                    System.out.println(response.headers().metadata());

                    // Update the blob's metadata and write it back to the blob.
                    Metadata receivedMetadata = new Metadata(response.headers().metadata());
                    receivedMetadata.put("createdby", "Joseph");
                    return blobURL.setMetadata(receivedMetadata, null);
                })
                .flatMap(response ->
                        // Delete the container.
                        containerURL.delete(null)
                )
                 /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();

        // NOTE: The SetMetadata method updates the blob's ETag & LastModified properties.
    }

    // This example shows how to create a blob with HTTP Headers and then how to read & update the blob's HTTP Headers.
    @Test
    public void exampleBlobHTTPHeaders() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainerheaders");
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("Data.txt");

        // Create the container.
        containerURL.create(null, null)
                .flatMap(containersCreateResponse -> {
                    /*
                    Create the blob with HTTP headers.
                     */
                    BlobHTTPHeaders headers = new BlobHTTPHeaders(
                            null,
                            "attachment",
                            null,
                            null,
                            null,
                            "text/html; charset=utf-8");
                    return blobURL.upload(Flowable.just(ByteBuffer.wrap("Text-1".getBytes())), "Text-1".length(),
                            headers, null, null);
                })
                .flatMap(response ->
                        // Query the blob's properties and metadata.
                        blobURL.getProperties(null))
                .flatMap(response -> {
                    // Show some of the blob's read-only properties.
                    System.out.println(response.headers().blobType());
                    System.out.println(response.headers().eTag());
                    System.out.println(response.headers().lastModified());

                    // Show the blob's HTTP headers..
                    System.out.println(response.headers().contentType());
                    System.out.println(response.headers().contentDisposition());

                    /*
                     Update the blob's properties and write it back to the blob.
                     NOTE: If one of the HTTP properties is updated, any that are not included in the update request
                     will be cleared. In order to preserve the existing HTTP properties, they must be re-set along with
                     the added or updated properties.
                     */
                    BlobHTTPHeaders headers = new BlobHTTPHeaders(
                            null,
                            null,
                            null,
                            null,
                            null,
                            "text/plain"
                    );
                    return blobURL.setHTTPHeaders(headers, null);
                })
                .flatMap(response ->
                        // Delete the container.
                        containerURL.delete(null)
                )
                 /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();

        // NOTE: The SetHTTPHeaders method updates the blob's ETag & LastModified properties.
    }

    /*
    This example shows how to upload a lot of data (in blocks) to a blob. A block blob can have a maximum of 50,000
    blocks; each block can have a maximum of 100MB. Therefore, the maximum size ofa  block blob is slightly more than
    4.75TB (100MB X 50,000 blocks).
    NOTE: The TransferManager class contains methods which will upload large blobs in parallel using
    stageBlock/commitBlockList. We recommend you use those methods if possible.
     */
    @Test
    public void exampleBlockBlobURL() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainerblock");
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("Data.txt");

        String[] data = {"Michael", "Gabriel", "Raphael", "John"};

        // Create the container. We convert to an Observable to be able to work with the block list effectively.
        containerURL.create(null, null).toObservable()
                .flatMap(response ->
                        // Create an Observable that will yield each of the Strings one at a time.
                        Observable.fromIterable(Arrays.asList(data))
                )
                // Items emitted by an Observable that results from a concatMap call will preserve the original order.
                .concatMap(block -> {
                    /*
                     Generate a base64 encoded blockID. Note that all blockIDs must be the same length. It is generally
                     considered best practice to use UUIDs for the blockID.
                     */
                    String blockId = Base64.getEncoder().encodeToString(
                            UUID.randomUUID().toString().getBytes());

                    /*
                     Upload a block to this blob specifying the BlockID and its content (up to 100MB); this block is
                     uncommitted.
                     NOTE: It is imperative that the provided length match the actual length of the data exactly.
                     */
                    return blobURL.stageBlock(blockId, Flowable.just(ByteBuffer.wrap(block.getBytes())),
                            block.length(), null)
                            /*
                             We do not care for any data on the response object, but we do want to keep track of the
                             ID.
                             */
                            .map(x -> blockId).toObservable();
                })
                // Gather all of the IDs emitted by the previous observable into a single list.
                .collectInto(new ArrayList<>(data.length), (BiConsumer<ArrayList<String>, String>) ArrayList::add)
                .flatMap(idList ->
                        /*
                        By this point, all the blocks are upload and we have an ordered list of their IDs. Here, we
                        atomically commit the whole list.
                        NOTE: The block list order need not match the order in which the blocks were uploaded. The order
                        of IDs in the commitBlockList call will determine the structure of the blob.
                         */
                        blobURL.commitBlockList(idList, null, null, null))
                .flatMap(response ->
                        /*
                         For the blob, show each block (ID and size) that is a committed part of it. It is also possible
                         to include blocks that have been staged but not committed.
                         */
                        blobURL.getBlockList(BlockListType.ALL, null))
                .flatMap(response -> {
                    for (Block block : response.body().committedBlocks()) {
                        System.out.println(String.format(Locale.ROOT, "Block ID=%s, Size=%d", block.name(),
                                block.size()));
                    }

                    /*
                     Download the blob in its entirety; download operations do not take blocks into account.
                     NOTE: For really large blobs, downloading them like this allocates a lot of memory.
                     */
                    return blobURL.download(null, null, false);
                })
                .flatMap(response ->
                        // Print out the data.
                        FlowableUtil.collectBytesInBuffer(response.body())
                                .doOnSuccess(bytes ->
                                        System.out.println(new String(bytes.array())))
                )
                .flatMap(response ->
                        // Delete the container.
                        containerURL.delete(null)
                )
                 /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
    }

    /*
    This example shows how to append data (in blocks) to an append blob. An append blob can have a maximum of 50,000
    blocks; each block can have a maximum of 100MB. Therefore, the maximum size of an append blob is slightly more than
    4.75TB (100MB X 50,000 blocks).
     */
    @Test
    public void exmapleAppendBlobURL() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainerappend");
        AppendBlobURL blobURL = containerURL.createAppendBlobURL("Data.txt");

        // Create the container.
        containerURL.create(null, null)
                .flatMap(response ->
                        // Create the append blob. This creates a zero-length blob that we can now append to.
                        blobURL.create(null, null, null))
                .toObservable()
                .flatMap(response ->
                        // This range will act as our for loop to create 5 blocks
                        Observable.range(0, 5))
                .concatMapCompletable(i -> {
                    String text = String.format(Locale.ROOT, "Appending block #%d\n", i);
                    return blobURL.appendBlock(Flowable.just(ByteBuffer.wrap(text.getBytes())), text.length(),
                            null).toCompletable();
                })
                // Download the blob.
                .andThen(blobURL.download(null, null, false))
                .flatMap(response ->
                        // Print out the data.
                        FlowableUtil.collectBytesInBuffer(response.body())
                                .doOnSuccess(bytes ->
                                        System.out.println(new String(bytes.array())))
                )
                .flatMap(response ->
                        // Delete the container.
                        containerURL.delete(null)
                )
                 /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();

    }

    // This example shows how to work with Page Blobs.
    @Test
    public void examplePageBlobURL() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainerpage");
        PageBlobURL blobURL = containerURL.createPageBlobURL("Data.txt");

        // Create the container.
        containerURL.create(null, null)
                .flatMap(response ->
                        // Create the page blob with 4 512-byte pages.
                        blobURL.create(4 * PageBlobURL.PAGE_BYTES, null, null,
                                null, null))
                .flatMap(response -> {
                    /*
                     Upload data to a page.
                     NOTE: The page range must start on a multiple of the page size and end on
                     (multiple of page size) - 1.
                     */
                    byte[] data = new byte[PageBlobURL.PAGE_BYTES];
                    for (int i = 0; i < PageBlobURL.PAGE_BYTES; i++) {
                        data[i] = 'a';
                    }
                    return blobURL.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                            Flowable.just(ByteBuffer.wrap(data)), null);
                })
                .flatMap(response -> {
                    // Upload data to the third page in the blob.
                    byte[] data = new byte[PageBlobURL.PAGE_BYTES];
                    for (int i = 0; i < PageBlobURL.PAGE_BYTES; i++) {
                        data[i] = 'b';
                    }
                    return blobURL.uploadPages(new PageRange().withStart(2 * PageBlobURL.PAGE_BYTES)
                                    .withEnd(3 * PageBlobURL.PAGE_BYTES - 1),
                            Flowable.just(ByteBuffer.wrap(data)), null);
                })
                .flatMap(response ->
                        // Get the page ranges which have valid data.
                        blobURL.getPageRanges(null, null))
                .flatMap(response -> {
                    // Print the pages that are valid.
                    for (PageRange range : response.body().pageRange()) {
                        System.out.println(String.format(Locale.ROOT, "Start=%d, End=%d\n", range.start(),
                                range.end()));
                    }

                    // Clear and invalidate the first range.
                    return blobURL.clearPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                            null);
                })
                .flatMap(response ->
                        // Get the page ranges which have valid data.
                        blobURL.getPageRanges(null, null))
                .flatMap(response -> {
                    // Print the pages that are valid.
                    for (PageRange range : response.body().pageRange()) {
                        System.out.println(String.format(Locale.ROOT, "Start=%d, End=%d\n", range.start(),
                                range.end()));
                    }

                    // Get the content of the whole blob.
                    return blobURL.download(null, null, false);
                })
                .flatMap(response ->
                        // Print the received content.
                        FlowableUtil.collectBytesInBuffer(response.body())
                                .doOnSuccess(data ->
                                        System.out.println(new String(data.array())))
                                .flatMap(data ->
                                        // Delete the container.
                                        containerURL.delete(null))
                )
                 /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
    }

    /*
    This example shows how to create a blob, take a snapshot of it, update the base blob, read from the blob snapshot,
    list blobs with their snapshots, and how to delete blob snapshots.
     */
    @Test
    public void example_blobSnapshot() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainersnapshot");
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("Original.txt");

        // Create the container.
        containerURL.create(null, null)
                .flatMap(response ->
                        // Create the original blob.
                        blobURL.upload(Flowable.just(ByteBuffer.wrap("Some text".getBytes())), "Some text".length(),
                                null, null, null))
                .flatMap(response ->
                        // Create a snapshot of the original blob.
                        blobURL.createSnapshot(null, null))
                .flatMap(response ->
                        blobURL.upload(Flowable.just(ByteBuffer.wrap("New text".getBytes())), "New text".length(),
                                null, null, null)
                                .flatMap(response1 ->
                                        blobURL.download(null, null, false))
                                .flatMap(response1 ->
                                        // Print the received content.
                                        FlowableUtil.collectBytesInBuffer(response1.body())
                                                .doOnSuccess(data ->
                                                        System.out.println(new String(data.array()))))
                                .flatMap(response1 -> {
                                    // Show the snapshot blob via original blob URI & snapshot time.
                                    BlockBlobURL snapshotURL = blobURL.withSnapshot(response.headers().snapshot());

                                    /*
                                    FYI: You can get the base blob URL from one of its snapshots by passing null to
                                    withSnapshot.
                                     */
                                    BlockBlobURL baseBlob = snapshotURL.withSnapshot(null);

                                    return snapshotURL
                                            .download(null, null, false)
                                            .flatMap(response2 ->
                                                    /*
                                                    List the blob(s) in our container, including their snapshots; since
                                                    a container may hold millions of blobs, this is done one segment at
                                                    a time.
                                                    */
                                                    containerURL.listBlobsFlatSegment(null,
                                                            new ListBlobsOptions(null, null,
                                                                    1)))
                                            .flatMap(response2 ->
                                                    /*
                                                     The asynchronous requests require we use recursion to continue our
                                                     listing.
                                                     */
                                                    listBlobsHelper(containerURL, response2))
                                            .flatMap(response2 ->
                                                    blobURL.startCopyFromURL(snapshotURL.toURL(), null,
                                                            null, null));
                                }))
                .flatMap(response ->
                        // Delete the container.
                        containerURL.delete(null)
                )
                 /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
    }

    public void progressUploadDownload() {
        // TODO:
    }

    // This example shows how to copy a source document on the Internet to a blob.
    @Test
    public void exampleBlobURL_startCopy() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainercopy");
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("CopiedBlob.bin");

        // Create the container.
        containerURL.create(null, null)
                .flatMap(response ->
                        blobURL.startCopyFromURL(
                                new URL("https://cdn2.auth0.com/docs/media/addons/azure_blob.svg"),
                                null, null, null))
                .flatMap(response ->
                        blobURL.getProperties(null))
                .flatMap(response ->
                        waitForCopyHelper(blobURL, response))
                .flatMap(response ->
                        // Delete the container we created earlier.
                        containerURL.delete(null))
                /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();

    }

    public Single<BlobsGetPropertiesResponse> waitForCopyHelper(BlobURL blobURL, BlobsGetPropertiesResponse response)
            throws InterruptedException {
        System.out.println(response.headers().copyStatus());
        if (response.headers().copyStatus() == CopyStatusType.SUCCESS) {
            return Single.just(response);
        }

        Thread.sleep(2000);
        return blobURL.getProperties(null)
                .flatMap(response1 ->
                    waitForCopyHelper(blobURL, response1));

    }

    // This example shows how to copy a large stream in blocks (chunks) to a block blob.
    @Test
    public void exampleUploadStreamToBlockBlob() throws IOException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainerparallelupload");
        String filename = "BigFile.bin";
        BlockBlobURL blobURL = containerURL.createBlockBlobURL(filename);
        File tempFile = File.createTempFile("BigFile", ".bin");
        tempFile.deleteOnExit();

        // Create the container.
        containerURL.create(null, null)
                .flatMap(response -> {
                    AsynchronousFileChannel channel =
                            AsynchronousFileChannel.open(tempFile.toPath(), StandardOpenOption.WRITE);
                    return Single.fromFuture(channel.write(ByteBuffer.wrap("Big data".getBytes()), 0))
                            .doAfterTerminate(channel::close);
                })
                .flatMap(response -> {
                    FileChannel channel = FileChannel.open(tempFile.toPath(), StandardOpenOption.READ);
                    return TransferManager.uploadFileToBlockBlob(channel, blobURL,
                            BlockBlobURL.MAX_PUT_BLOCK_BYTES, null)
                            .doAfterTerminate(channel::close);
                })
                .flatMap(response ->
                        blobURL.download(null, null, false))
                .flatMap(response ->
                        // Print out the data.
                        FlowableUtil.collectBytesInBuffer(response.body())
                                .doOnSuccess(bytes ->
                                        System.out.println(new String(bytes.array())))
                )
                .flatMap(response ->
                        // Delete the container.
                        containerURL.delete(null))
                /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
    }

    /*
    This example shows how to download a large stream with intelligent retries. Specifically, if the connection fails
    while reading, continuing to read form this stream initiates a new downloadBlob call passing a range that starts
    from where the last byte successfully read before the failure.
     */
    @Test
    public void exampleDownloadStream() {
        // TODO
    }

    // TODO: Lease? Root container?

    /*
    This example demonstrates two common patterns: 1. Creating a container if it does not exist and continuing normally
    if it does already exist. 2. Deleting a container if it does exist and continuing normally if it does not.
     */
    @Test
    public void exampleCreateContainerIfNotExists() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainercreateifnotexist");

        createContainerIfNotExists(containerURL)
                .flatMap(r -> {
                    System.out.println("Container created: " + r.toString());
                    return createContainerIfNotExists(containerURL);
                })
                .flatMap(r -> {
                    System.out.println("Container created: " + r.toString());
                    return deleteContainerIfExists(containerURL);
                })
                .flatMap(r -> {
                    System.out.println("Container deleted: " + r.toString());
                    return deleteContainerIfExists(containerURL);
                })
                .doOnSuccess(r -> System.out.println("Container deleted: " + r.toString()))
                /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
    }

    public static Single<Boolean> createContainerIfNotExists(ContainerURL containerURL)
    {
        return containerURL.create(null, null).map((r) -> true).onErrorResumeNext((e) -> {
            if (e instanceof RestException) {
                RestException re = (RestException)e;
                if (re.getMessage().contains("ContainerAlreadyExists")) {
                    return Single.just(false);
                }
            }

            return Single.error(e);
        });
    }

    public static Single<Boolean> deleteContainerIfExists(ContainerURL containerURL)
    {
        return containerURL.delete(null).map((r) -> true).onErrorResumeNext((e) -> {
            if (e instanceof RestException)
            {
                RestException re = (RestException)e;
                if (re.getMessage().contains("ContainerNotFound"))
                {
                    return Single.just(false);
                }
            }

            return Single.error(e);
        });
    }

    /*
    The following example demonstrates a useful scenario in which it is desirable to receive listed elements as
    individual items in an observable rather than a seqeuence of lists.
     */
    @Test
    public void exampleLazyEnumeration() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainerlistlazy");

        containerURL.create(null, null).toCompletable()
                .andThen(Observable.range(0, 5))
                .flatMap(integer -> {
                    AppendBlobURL bu = containerURL.createAppendBlobURL(integer.toString());
                    return bu.create(null, null, null).toObservable();
                })
                .ignoreElements()
                .andThen(listBlobsLazy(containerURL, null))
                .doOnNext(b -> System.out.println("Blob: " + b.name()))
                .ignoreElements()
                .andThen(containerURL.delete(null))
                /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
    }

    public static Observable<Blob> listBlobsLazy(ContainerURL containerURL, ListBlobsOptions listBlobsOptions)
    {
        return containerURL.listBlobsFlatSegment(null, listBlobsOptions)
                .flatMapObservable((r) -> listContainersResultToContainerObservable(containerURL, listBlobsOptions, r));
    }

    private static Observable<Blob> listContainersResultToContainerObservable(
            ContainerURL containerURL, ListBlobsOptions listBlobsOptions,
            ContainersListBlobFlatSegmentResponse response)
    {
        Observable<Blob> result = Observable.fromIterable(response.body().blobs().blob());

        System.out.println("!!! count: " + response.body().blobs().blob().size());

        if (response.body().nextMarker() != null)
        {
            System.out.println("Hit continuation in listing at " + response.body().blobs().blob().get(
                    response.body().blobs().blob().size()-1).name());
            // Recursively add the continuation items to the observable.
            result = result.concatWith(containerURL.listBlobsFlatSegment(response.body().nextMarker(), listBlobsOptions)
                    .flatMapObservable((r) ->
                            listContainersResultToContainerObservable(containerURL, listBlobsOptions, r)));
        }

        return result;
    }

}

