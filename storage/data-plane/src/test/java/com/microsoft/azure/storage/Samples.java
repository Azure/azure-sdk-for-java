// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.storage;

import com.microsoft.azure.storage.blob.AccountSASPermission;
import com.microsoft.azure.storage.blob.AccountSASResourceType;
import com.microsoft.azure.storage.blob.AccountSASService;
import com.microsoft.azure.storage.blob.AccountSASSignatureValues;
import com.microsoft.azure.storage.blob.AnonymousCredentials;
import com.microsoft.azure.storage.blob.AppendBlobURL;
import com.microsoft.azure.storage.blob.BlobAccessConditions;
import com.microsoft.azure.storage.blob.BlobRange;
import com.microsoft.azure.storage.blob.BlobSASPermission;
import com.microsoft.azure.storage.blob.BlobURL;
import com.microsoft.azure.storage.blob.BlobURLParts;
import com.microsoft.azure.storage.blob.BlockBlobURL;
import com.microsoft.azure.storage.blob.ContainerURL;
import com.microsoft.azure.storage.blob.DownloadResponse;
import com.microsoft.azure.storage.blob.ListBlobsOptions;
import com.microsoft.azure.storage.blob.ListContainersOptions;
import com.microsoft.azure.storage.blob.LoggingOptions;
import com.microsoft.azure.storage.blob.Metadata;
import com.microsoft.azure.storage.blob.PageBlobURL;
import com.microsoft.azure.storage.blob.PipelineOptions;
import com.microsoft.azure.storage.blob.ProgressReporter;
import com.microsoft.azure.storage.blob.ReliableDownloadOptions;
import com.microsoft.azure.storage.blob.RequestRetryOptions;
import com.microsoft.azure.storage.blob.RetryPolicyType;
import com.microsoft.azure.storage.blob.SASProtocol;
import com.microsoft.azure.storage.blob.SASQueryParameters;
import com.microsoft.azure.storage.blob.ServiceSASSignatureValues;
import com.microsoft.azure.storage.blob.ServiceURL;
import com.microsoft.azure.storage.blob.SharedKeyCredentials;
import com.microsoft.azure.storage.blob.StorageException;
import com.microsoft.azure.storage.blob.StorageURL;
import com.microsoft.azure.storage.blob.TransferManager;
import com.microsoft.azure.storage.blob.TransferManagerUploadToBlockBlobOptions;
import com.microsoft.azure.storage.blob.URLParser;
import com.microsoft.azure.storage.blob.models.AccessPolicy;
import com.microsoft.azure.storage.blob.models.AccessTier;
import com.microsoft.azure.storage.blob.models.BlobGetPropertiesResponse;
import com.microsoft.azure.storage.blob.models.BlobHTTPHeaders;
import com.microsoft.azure.storage.blob.models.BlobItem;
import com.microsoft.azure.storage.blob.models.BlobPrefix;
import com.microsoft.azure.storage.blob.models.BlockListType;
import com.microsoft.azure.storage.blob.models.ContainerCreateResponse;
import com.microsoft.azure.storage.blob.models.ContainerItem;
import com.microsoft.azure.storage.blob.models.ContainerListBlobFlatSegmentResponse;
import com.microsoft.azure.storage.blob.models.ContainerListBlobHierarchySegmentResponse;
import com.microsoft.azure.storage.blob.models.CopyStatusType;
import com.microsoft.azure.storage.blob.models.DeleteSnapshotsOptionType;
import com.microsoft.azure.storage.blob.models.ModifiedAccessConditions;
import com.microsoft.azure.storage.blob.models.PageRange;
import com.microsoft.azure.storage.blob.models.PublicAccessType;
import com.microsoft.azure.storage.blob.models.SequenceNumberActionType;
import com.microsoft.azure.storage.blob.models.ServiceListContainersSegmentResponse;
import com.microsoft.azure.storage.blob.models.SignedIdentifier;
import com.microsoft.azure.storage.blob.models.StorageErrorCode;
import com.microsoft.azure.storage.blob.models.StorageServiceProperties;
import com.microsoft.rest.v2.RestException;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpPipelineLogLevel;
import com.microsoft.rest.v2.http.HttpPipelineLogger;
import com.microsoft.rest.v2.http.HttpResponse;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.BiConsumer;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Samples {

    public static final String PLAYBACK_MODE = "PlAYBACK";

    //Samples only run in Live/Record mode.
    void checkMode() {
        String testMode = System.getenv("AZURE_TEST_MODE");
        if (testMode == null) {
            testMode = PLAYBACK_MODE;
        }
        Assume.assumeTrue("The test only runs in Live mode.", testMode.equalsIgnoreCase("RECORD"));
    }

    public static Single<Boolean> createContainerIfNotExists(ContainerURL containerURL) {
        return containerURL.create(null, null, null).map((r) -> true).onErrorResumeNext((e) -> {
            if (e instanceof RestException) {
                RestException re = (RestException) e;
                if (re.getMessage().contains("ContainerAlreadyExists")) {
                    return Single.just(false);
                }
            }

            return Single.error(e);
        });
    }

    public static Single<Boolean> deleteContainerIfExists(ContainerURL containerURL) {
        return containerURL.delete(null, null).map((r) -> true).onErrorResumeNext((e) -> {
            if (e instanceof RestException) {
                RestException re = (RestException) e;
                if (re.getMessage().contains("ContainerNotFound")) {
                    return Single.just(false);
                }
            }

            return Single.error(e);
        });
    }

    public static Observable<BlobItem> listBlobsLazy(ContainerURL containerURL, ListBlobsOptions listBlobsOptions) {
        return containerURL.listBlobsFlatSegment(null, listBlobsOptions, null)
                .flatMapObservable((r) -> listContainersResultToContainerObservable(containerURL, listBlobsOptions, r));
    }

    private static Observable<BlobItem> listContainersResultToContainerObservable(
            ContainerURL containerURL, ListBlobsOptions listBlobsOptions,
            ContainerListBlobFlatSegmentResponse response) {
        Observable<BlobItem> result = Observable.fromIterable(response.body().segment().blobItems());

        System.out.println("!!! count: " + response.body().segment().blobItems());

        if (response.body().nextMarker() != null) {
            System.out.println("Hit continuation in listing at " + response.body().segment().blobItems().get(
                    response.body().segment().blobItems().size() - 1).name());
            // Recursively add the continuation items to the observable.
            result = result.concatWith(containerURL.listBlobsFlatSegment(response.body().nextMarker(), listBlobsOptions,
                    null)
                    .flatMapObservable((r) ->
                            listContainersResultToContainerObservable(containerURL, listBlobsOptions, r)));
        }

        return result;
    }

    private String getAccountName() {
        checkMode();
        return System.getenv("PRIMARY_STORAGE_ACCOUNT_NAME");
    }

    private String getAccountKey() {
        return System.getenv("PRIMARY_STORAGE_ACCOUNT_KEY");
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
        Create a request pipeline that is used to process HTTP(S) requests and responses. It requires your account
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
        ContainerURL containerURL = serviceURL.createContainerURL("myjavacontainerbasic" + System.currentTimeMillis());

        /*
        Create a URL that references a to-be-created blob in your Azure Storage account's container.
        This returns a BlockBlobURL object that wraps the blob's URl and a request pipeline
        (inherited from containerURL). Note that blob names can be mixed case.
         */
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("HelloWorld.txt");

        String data = "Hello world!";

        // Create the container on the service (with no metadata and no public access)
        containerURL.create(null, null, null)
                .flatMap(containerCreateResponse ->
                        /*
                         Create the blob with string (plain text) content.
                         NOTE: The Flowable containing the data must be replayable to support retries. That is, it must
                         yield the same data every time it is subscribed to.
                         NOTE: If the provided length does not match the actual length, this method will throw.
                         */
                        blobURL.upload(Flowable.just(ByteBuffer.wrap(data.getBytes())), data.length(),
                                null, null, null, null))
                .flatMap(blobUploadResponse ->
                        // Download the blob's content.
                        blobURL.download(null, null, false, null))
                .flatMap(blobDownloadResponse ->
                        // Verify that the blob data round-tripped correctly.
                        FlowableUtil.collectBytesInBuffer(blobDownloadResponse.body(null))
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
                        containerURL.listBlobsFlatSegment(null, new ListBlobsOptions().withMaxResults(1), null))
                .flatMap(containerListBlobFlatSegmentResponse ->
                        // The asynchronous requests require we use recursion to continue our listing.
                        listBlobsFlatHelper(containerURL, containerListBlobFlatSegmentResponse))
                .flatMap(containerListBlobFlatSegmentResponse ->
                        // Delete the blob we created earlier.
                        blobURL.delete(null, null, null))
                .flatMap(blobDeleteResponse ->
                        // Delete the container we created earlier.
                        containerURL.delete(null, null))
                /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
    }

    // <list_blobs_flat_helper>
    public Single<ContainerListBlobFlatSegmentResponse> listBlobsFlatHelper(
            ContainerURL containerURL, ContainerListBlobFlatSegmentResponse response) {

        // Process the blobs returned in this result segment (if the segment is empty, blob() will be null.
        if (response.body().segment().blobItems() != null) {
            for (BlobItem b : response.body().segment().blobItems()) {
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
            return containerURL.listBlobsFlatSegment(nextMarker, new ListBlobsOptions().withMaxResults(1), null)
                    .flatMap(containersListBlobFlatSegmentResponse ->
                            listBlobsFlatHelper(containerURL, containersListBlobFlatSegmentResponse));
        }
    }
    // </list_blobs_flat_helper>

    // <list_blobs_hierarchy_helper>
    public Single<ContainerListBlobHierarchySegmentResponse> listBlobsHierarchyHelper(
            ContainerURL containerURL, ContainerListBlobHierarchySegmentResponse response) {

        // Process the blobs returned in this result segment (if the segment is empty, blob() will be null.
        if (response.body().segment().blobItems() != null) {
            for (BlobItem b : response.body().segment().blobItems()) {
                String output = "Blob name: " + b.name();
                if (b.snapshot() != null) {
                    output += ", Snapshot: " + b.snapshot();
                }
                System.out.println(output);
            }
        }

        // Process the blobsPrefixes returned in this result segment
        if (response.body().segment().blobPrefixes() != null) {
            for (BlobPrefix bp : response.body().segment().blobPrefixes()) {
                // Process the prefixes.
                System.out.println("Blob prefix is " + bp.name());
            }
        }

        // If there is not another segment, return this response as the final response.
        if (response.body().nextMarker() == null) {
            return Single.just(response);
        } else {
            /*
             IMPORTANT: ListBlobHierarchySegment returns the start of the next segment; you MUST use this to get the
             next segment (after processing the current result segment
             */
            String nextMarker = response.body().nextMarker();

            /*
            The presence of the marker indicates that there are more blobs to list, so we make another call to
            listBlobsHierarchySegment and pass the result through this helper function.
             */
            return containerURL.listBlobsHierarchySegment(nextMarker, response.body().delimiter(),
                    new ListBlobsOptions().withMaxResults(1), null)
                    .flatMap(containersListBlobHierarchySegmentResponse ->
                            listBlobsHierarchyHelper(containerURL, containersListBlobHierarchySegmentResponse));
        }
    }
    // </list_blobs_hierarchy_helper>

    // <service_list_helper>
    public Single<ServiceListContainersSegmentResponse> listContainersHelper(
            ServiceURL serviceURL, ServiceListContainersSegmentResponse response) {

        // Process the containers returned in this result segment (if the segment is empty, containerItems will be null.
        if (response.body().containerItems() != null) {
            for (ContainerItem b : response.body().containerItems()) {
                String output = "Container name: " + b.name();
                System.out.println(output);
            }
        }

        // If there is not another segment, return this response as the final response.
        if (response.body().nextMarker() == null) {
            return Single.just(response);
        } else {
            /*
             IMPORTANT: ListContainersSegment returns the start of the next segment; you MUST use this to get the
             next segment (after processing the current result segment
             */
            String nextMarker = response.body().nextMarker();

            /*
            The presence of the marker indicates that there are more blobs to list, so we make another call to
            listContainersSegment and pass the result through this helper function.
             */
            return serviceURL.listContainersSegment(nextMarker, new ListContainersOptions(), null)
                    .flatMap(containersListBlobHierarchySegmentResponse ->
                            listContainersHelper(serviceURL, response));
        }
    }
    // </service_list_helper>

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
        po.withRequestRetryOptions(new RequestRetryOptions(RetryPolicyType.EXPONENTIAL, 3, 3,
                1000L, 3000L, null));

        /*
         Set LoggingOptions to control how each HTTP request and its response is logged. A successful response taking
         more than 200ms will be logged as a warning.
         */
        po.withLoggingOptions(new LoggingOptions(200));

        // Set LogOptions to control what & where all pipeline log events go.
        po.withLogger(new HttpPipelineLogger() {
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
        });

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
        po.withRequestRetryOptions(new RequestRetryOptions(RetryPolicyType.EXPONENTIAL, 4, 60,
                5000L, 10000L, null));
        ContainerURL newContainerURL = containerURL.withPipeline(
                ServiceURL.createPipeline(new AnonymousCredentials(), po));

        /*
        Now any XxxBlobURL object created using newContainerURL inherits the pipeline with the new retry policy.
         */
        BlobURL newBlobURL = newContainerURL.createBlobURL("ReadMe2.txt");
    }

    @Test
    /*
     * This example shows how to handle errors thrown by various XxxURL methods. Any client-side error will be
     * propagated unmodified. However, any response from the service with an unexpected status code will be wrapped in a
     * StorageException. If the pipeline includes the RequestRetryFactory, which is the default, some of these errors
     * will be automatically retried if it makes sense to do so. The StorageException type exposes rich error
     * information returned by the service.
     */
    public void exampleStorageError() throws MalformedURLException {
        ContainerURL containerURL = new ContainerURL(new URL("http://myaccount.blob.core.windows.net/mycontainer"),
                StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()));

        containerURL.create(null, null, null)
                // An error occurred.
                .onErrorResumeNext(throwable -> {
                    // Check if this error is from the service.
                    if (throwable instanceof StorageException) {
                        StorageException exception = (StorageException) throwable;
                        // StorageErrorCode defines constants corresponding to all error codes returned by the service.
                        if (exception.errorCode() == StorageErrorCode.CONTAINER_BEING_DELETED) {
                            // Log more detailed information.
                            System.out.println("Extended details: " + exception.message());

                            // Examine the raw response.
                            HttpResponse response = exception.response();
                        } else if (exception.errorCode() == StorageErrorCode.CONTAINER_ALREADY_EXISTS) {
                            // Process the error
                            System.out.println("The container url is " + containerURL.toString());
                        }
                    }
                    // We just fake a successful response to prevent the example from crashing.
                    return Single.just(
                            new ContainerCreateResponse(null, 200, null, null, null));
                })
                /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
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
        URL u = new URL("https://myaccount.blob.core.windows.net/mycontainter/ReadMe.txt?"
                + "snapshot=2011-03-09T01:42:34.9360000Z"
                + "&sv=2015-02-21&sr=b&st=2111-01-09T01:42:34Z&se=2222-03-09T01:42:34Z&sp=rw"
                + "&sip=168.1.5.60-168.1.5.70&spr=https,http&si=myIdentifier&ss=bf&srt=s"
                + "&sig=92836758923659283652983562==");

        // You can parse this URL into its constituent parts:
        BlobURLParts parts = URLParser.parse(u);

        // Now, we access the parts (this example prints them).
        System.out.println(String.join("\n",
                parts.host(),
                parts.containerName(),
                parts.blobName(),
                parts.snapshot()));
        System.out.println("");
        SASQueryParameters sas = parts.sasQueryParameters();
        System.out.println(String.join("\n",
                sas.version(),
                sas.resource(),
                sas.startTime().toString(),
                sas.expiryTime().toString(),
                sas.permissions(),
                sas.ipRange().toString(),
                sas.protocol().toString(),
                sas.identifier(),
                sas.services(),
                sas.signature()));

        // You can then change some of the fields and construct a new URL.
        parts.withSasQueryParameters(null) // Remove the SAS query parameters.
                .withSnapshot(null) // Remove the snapshot timestamp.
                .withContainerName("othercontainer"); // Change the container name.
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
        values.withProtocol(SASProtocol.HTTPS_ONLY) // Users MUST use HTTPS (not HTTP).
                .withExpiryTime(OffsetDateTime.now().plusDays(2)); // 2 days before expiration.

        AccountSASPermission permission = new AccountSASPermission()
                .withRead(true)
                .withList(true);
        values.withPermissions(permission.toString());

        AccountSASService service = new AccountSASService()
                .withBlob(true);
        values.withServices(service.toString());

        AccountSASResourceType resourceType = new AccountSASResourceType()
                .withContainer(true)
                .withObject(true);
        values.withResourceTypes(resourceType.toString());

        SASQueryParameters params = values.generateSASQueryParameters(credential);

        // Calling encode will generate the query string.
        String encodedParams = params.encode();

        String urlToSendToSomeone = String.format(Locale.ROOT, "https://%s.blob.core.windows.net%s",
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
        String snapshotId = "2018-01-01T00:00:00.0000000Z"; // SAS can be restricted to a specific snapshot

        /*
        Set the desired SAS signature values and sign them with the shared key credentials to get the SAS query
        parameters.
         */
        ServiceSASSignatureValues values = new ServiceSASSignatureValues()
                .withProtocol(SASProtocol.HTTPS_ONLY) // Users MUST use HTTPS (not HTTP).
                .withExpiryTime(OffsetDateTime.now().plusDays(2)) // 2 days before expiration.
                .withContainerName(containerName)
                .withBlobName(blobName)
                .withSnapshotId(snapshotId);

        /*
        To produce a container SAS (as opposed to a blob SAS), assign to Permissions using ContainerSASPermissions, and
        make sure the blobName and snapshotId fields are null (the default).
         */
        BlobSASPermission permission = new BlobSASPermission()
                .withRead(true)
                .withAdd(true)
                .withWrite(true);
        values.withPermissions(permission.toString());

        SASQueryParameters params = values.generateSASQueryParameters(credential);

        // Calling encode will generate the query string.
        String encodedParams = params.encode();
        // Colons are not safe characters in a URL; they must be properly encoded.
        snapshotId = snapshotId.replace(":", "%3A");

        String urlToSendToSomeone = String.format(Locale.ROOT, "https://%s.blob.core.windows.net/%s/%s?%s&%s",
                accountName, containerName, blobName, snapshotId, encodedParams);
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
    public void exampleContainerURLSetPermissions() throws InvalidKeyException, MalformedURLException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Use your Storage account's name and key to create a credential object; this is required to sign a SAS.
        SharedKeyCredentials credential = new SharedKeyCredentials(accountName, accountKey);

        // Create a containerURL object that wraps the container's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/myjavacontainerpermissions"
                + System.currentTimeMillis(), accountName));
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
        containerURL.create(null, null, null)
                .flatMap(containersCreateResponse ->
                        blobURL.upload(Flowable.just(ByteBuffer.wrap(data.getBytes())), data.length(),
                                null, null, null, null)
                )
                .flatMap(blockBlobUploadResponse ->
                        // Attempt to read the blob with anonymous credentials.
                        anonymousURL.download(null, null, false, null)
                )
                .ignoreElement()
                .onErrorResumeNext(throwable -> {
                    /*
                    We expected this error because the service returns an HTTP 404 status code when a blob exists but
                    the request does not have permission to access it.
                     */
                    if (throwable instanceof RestException
                            && ((RestException) throwable).response().statusCode() == 404) {
                        // This is how we change the container's permission to allow public/anonymous access.
                        return containerURL.setAccessPolicy(PublicAccessType.BLOB, null, null, null)
                                .ignoreElement();
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
                        false, null))
                .flatMap(blobDownloadResponse ->
                        // Delete the container and the blob within in.
                        containerURL.delete(null, null))
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
        ContainerURL containerURL = s.createContainerURL("myjavacontaineraccessconditions"
                + System.currentTimeMillis());
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("Data.txt");

        // Create the container (unconditionally; succeeds)
        containerURL.create(null, null, null)
                .flatMap(containersCreateResponse ->
                        // Create the blob (unconditionally; succeeds)
                        blobURL.upload(Flowable.just(ByteBuffer.wrap("Text-1".getBytes())), "Text-1".length(),
                                null, null, null, null))
                .flatMap(blockBlobUploadResponse -> {
                    System.out.println("Success: " + blockBlobUploadResponse.statusCode());

                    // Download blob content if the blob has been modified since we uploaded it (fails).
                    return blobURL.download(null,
                            new BlobAccessConditions().withModifiedAccessConditions(
                                    new ModifiedAccessConditions().withIfModifiedSince(
                                            blockBlobUploadResponse.headers().lastModified())),

                            false, null);
                })
                .onErrorResumeNext(throwable -> {
                    if (throwable instanceof RestException) {
                        System.out.println("Failure: " + ((RestException) throwable).response().statusCode());
                    } else {
                        return Single.error(throwable); // Network failure.
                    }
                    // Download the blob content if the blob hasn't been modified in the last 24 hours (fails):
                    return blobURL.download(null,
                            new BlobAccessConditions().withModifiedAccessConditions(
                                    new ModifiedAccessConditions().withIfUnmodifiedSince(
                                            OffsetDateTime.now().minusDays(1))),
                            false, null);
                })
                /*
                 onErrorResume next expects to return a Single of the same type. Here, we are changing operations, which
                 means we will get a different return type and cannot directly recover from the error. To solve this,
                 we go through a completable which will give us more flexibility with types.
                 */
                .ignoreElement()
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
                .andThen(blobURL.getProperties(null, null))
                .flatMap(getPropertiesResponse ->
                        /*
                         Upload new content if the blob hasn't changed since the version identified by the ETag
                         (succeeds).
                         */
                        blobURL.upload(Flowable.just(ByteBuffer.wrap("Text-2".getBytes())), "Text-2".length(),
                                null, null,
                                new BlobAccessConditions().withModifiedAccessConditions(
                                        new ModifiedAccessConditions().withIfMatch(
                                                getPropertiesResponse.headers().eTag())), null))
                .flatMap(blockBlobUploadResponse -> {
                    System.out.println("Success: " + blockBlobUploadResponse.statusCode());

                    // Download content if it has changed since the version identified by ETag (fails):
                    return blobURL.download(null,
                            new BlobAccessConditions().withModifiedAccessConditions(
                                    new ModifiedAccessConditions().withIfNoneMatch(
                                            blockBlobUploadResponse.headers().eTag())), false, null);
                })
                .ignoreElement()
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
                        new BlobAccessConditions().withModifiedAccessConditions(
                                // Wildcard will match any etag.
                                new ModifiedAccessConditions().withIfMatch("*")), null))
                .flatMap(blobDeleteResponse -> {
                    System.out.println("Success: " + blobDeleteResponse.statusCode());
                    return containerURL.delete(null, null);
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
    public void exampleMetadataContainers() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a ContainerURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainercontainermetadata"
                + System.currentTimeMillis());

        /*
         Create a container with some metadata (string key/value pairs).
         NOTE: Metadata key names are always converted to lowercase before being sent to the Storage Service. Therefore,
         you should always use lowercase letters; especially when querying a map for a metadata key.
         */
        Metadata metadata = new Metadata();
        metadata.put("createdby", "Rick");
        metadata.put("createdon", "4/13/18");
        containerURL.create(metadata, null, null)
                .flatMap(containersCreateResponse ->
                        // Query the container's metadata.
                        containerURL.getProperties(null, null)
                )
                .flatMap(containersGetPropertiesResponse -> {
                    Metadata receivedMetadata = new Metadata(containersGetPropertiesResponse.headers().metadata());

                    // Show the container's metadata.
                    System.out.println(receivedMetadata);

                    // Update the metadata and write it back to the container.
                    receivedMetadata.put("createdby", "Mary"); // NOTE: The keyname is in all lowercase.
                    return containerURL.setMetadata(receivedMetadata, null, null);
                })
                .flatMap(response -> containerURL.delete(null, null))
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
    public void exampleMetadataBlob() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainerblobmetadata" + System.currentTimeMillis());
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("Data.txt");

        // Create the container.
        containerURL.create(null, null, null)
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
                            null, metadata, null, null);
                })
                .flatMap(response ->
                        // Query the blob's properties and metadata.
                        blobURL.getProperties(null, null))
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
                    return blobURL.setMetadata(receivedMetadata, null, null);
                })
                .flatMap(response ->
                        // Delete the container.
                        containerURL.delete(null, null)
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

        ContainerURL containerURL = s.createContainerURL("myjavacontainerheaders" + System.currentTimeMillis());
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("Data.txt");

        // Create the container.
        containerURL.create(null, null, null)
                .flatMap(containersCreateResponse -> {
                    /*
                    Create the blob with HTTP headers.
                     */
                    BlobHTTPHeaders headers = new BlobHTTPHeaders().withBlobContentDisposition("attachment")
                            .withBlobContentType("text/html; charset=utf-8");
                    return blobURL.upload(Flowable.just(ByteBuffer.wrap("Text-1".getBytes())), "Text-1".length(),
                            headers, null, null, null);
                })
                .flatMap(response ->
                        // Query the blob's properties and metadata.
                        blobURL.getProperties(null, null))
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
                    BlobHTTPHeaders headers = new BlobHTTPHeaders().withBlobContentType("text/plain");
                    return blobURL.setHTTPHeaders(headers, null, null);
                })
                .flatMap(response ->
                        // Delete the container.
                        containerURL.delete(null, null)
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

        ContainerURL containerURL = s.createContainerURL("myjavacontainerblock" + System.currentTimeMillis());
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("Data.txt");

        String[] data = {"Michael", "Gabriel", "Raphael", "John"};

        // Create the container. We convert to an Observable to be able to work with the block list effectively.
        containerURL.create(null, null, null)
                .flatMapObservable(response ->
                        // Create an Observable that will yield each of the Strings one at a time.
                        Observable.fromIterable(Arrays.asList(data))
                )
                // Items emitted by an Observable that results from a concatMap call will preserve the original order.
                .concatMapEager(block -> {
                    /*
                     Generate a base64 encoded blockID. Note that all blockIDs must be the same length. It is generally
                     considered best practice to use UUIDs for the blockID.
                     */
                    String blockId = Base64.getEncoder().encodeToString(
                            UUID.randomUUID().toString().getBytes());

                    /*
                     Upload a block to this blob specifying the BlockID and its content (up to 100MB); this block is
                     uncommitted.
                     NOTE: The Flowable containing the data must be replayable to support retries. That is, it must
                         yield the same data every time it is subscribed to.
                     NOTE: It is imperative that the provided length match the actual length of the data exactly.
                     */
                    return blobURL.stageBlock(blockId, Flowable.just(ByteBuffer.wrap(block.getBytes())),
                            block.length(), null, null)
                            /*
                             We do not care for any data on the response object, but we do want to keep track of the
                             ID.
                             */
                            .map(x -> blockId).toObservable();
                })
                // Gather all of the IDs emitted by the previous observable into a single list.
                .collectInto(new ArrayList<>(data.length), (BiConsumer<ArrayList<String>, String>) ArrayList::add)
                .flatMap(idList -> {
                        /*
                        By this point, all the blocks are upload and we have an ordered list of their IDs. Here, we
                        atomically commit the whole list.
                        NOTE: The block list order need not match the order in which the blocks were uploaded. The order
                        of IDs in the commitBlockList call will determine the structure of the blob.
                         */
                    return blobURL.commitBlockList(idList, null, null, null, null);
                })
                .flatMap(response ->
                        /*
                         For the blob, show each block (ID and size) that is a committed part of it. It is also possible
                         to include blocks that have been staged but not committed.
                         */
                        blobURL.getBlockList(BlockListType.ALL, null, null))
                .flatMap(response ->
                        // Delete the container
                        containerURL.delete(null, null))
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
    public void exampleAppendBlobURL() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));

        ContainerURL containerURL = s.createContainerURL("myjavacontainerappend" + System.currentTimeMillis());
        AppendBlobURL blobURL = containerURL.createAppendBlobURL("Data.txt");

        // Create the container.
        containerURL.create(null, null, null)
                .flatMap(response ->
                        // Create the append blob. This creates a zero-length blob that we can now append to.
                        blobURL.create(null, null, null, null))
                .toObservable()
                .flatMap(response ->
                        // This range will act as our for loop to create 5 blocks
                        Observable.range(0, 5))
                .concatMapCompletable(i -> {
                    String text = String.format(Locale.ROOT, "Appending block #%d\n", i);
                    /*
                    NOTE: The Flowable containing the data must be replayable to support retries. That is, it must
                    yield the same data every time it is subscribed to.
                     */
                    return blobURL.appendBlock(Flowable.just(ByteBuffer.wrap(text.getBytes())), text.length(), null,
                            null).ignoreElement();
                })
                // Download the blob.
                .andThen(blobURL.download(null, null, false, null))
                .flatMap(response ->
                        // Print out the data.
                        FlowableUtil.collectBytesInBuffer(response.body(null))
                                .doOnSuccess(bytes ->
                                        System.out.println(new String(bytes.array())))
                )
                .flatMap(response ->
                        // Delete the container.
                        containerURL.delete(null, null)
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
        ContainerURL containerURL = s.createContainerURL("myjavacontainerpage" + System.currentTimeMillis());
        PageBlobURL blobURL = containerURL.createPageBlobURL("Data.txt");

        // Create the container.
        containerURL.create(null, null, null)
                .flatMap(response ->
                        // Create the page blob with 4 512-byte pages.
                        blobURL.create(4 * PageBlobURL.PAGE_BYTES, null, null,
                                null, null, null))
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
                    /*
                    NOTE: The Flowable containing the data must be replayable to support retries. That is, it must
                    yield the same data every time it is subscribed to.
                     */
                    return blobURL.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                            Flowable.just(ByteBuffer.wrap(data)), null, null);
                })
                .flatMap(response -> {
                    // Upload data to the third page in the blob.
                    byte[] data = new byte[PageBlobURL.PAGE_BYTES];
                    for (int i = 0; i < PageBlobURL.PAGE_BYTES; i++) {
                        data[i] = 'b';
                    }
                    return blobURL.uploadPages(new PageRange().withStart(2 * PageBlobURL.PAGE_BYTES)
                                    .withEnd(3 * PageBlobURL.PAGE_BYTES - 1),
                            Flowable.just(ByteBuffer.wrap(data)), null, null);
                })
                .flatMap(response ->
                        // Get the page ranges which have valid data.
                        blobURL.getPageRanges(null, null, null))
                .flatMap(response -> {
                    // Print the pages that are valid.
                    for (PageRange range : response.body().pageRange()) {
                        System.out.println(String.format(Locale.ROOT, "Start=%d, End=%d\n", range.start(),
                                range.end()));
                    }

                    // Clear and invalidate the first range.
                    return blobURL.clearPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                            null, null);
                })
                .flatMap(response ->
                        // Get the page ranges which have valid data.
                        blobURL.getPageRanges(null, null, null))
                .flatMap(response -> {
                    // Print the pages that are valid.
                    for (PageRange range : response.body().pageRange()) {
                        System.out.println(String.format(Locale.ROOT, "Start=%d, End=%d\n", range.start(),
                                range.end()));
                    }

                    // Get the content of the whole blob.
                    return blobURL.download(null, null, false, null);
                })
                .flatMap(response ->
                        // Print the received content.
                        FlowableUtil.collectBytesInBuffer(response.body(null))
                                .doOnSuccess(data ->
                                        System.out.println(new String(data.array())))
                                .flatMap(data ->
                                        // Delete the container.
                                        containerURL.delete(null, null))
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
    public void exampleBlobSnapshot() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));

        ContainerURL containerURL = s.createContainerURL("myjavacontainersnapshot" + System.currentTimeMillis());
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("Original.txt");

        // Create the container.
        containerURL.create(null, null, null)
                .flatMap(response ->
                        // Create the original blob.
                        blobURL.upload(Flowable.just(ByteBuffer.wrap("Some text".getBytes())), "Some text".length(),
                                null, null, null, null))
                .flatMap(response ->
                        // Create a snapshot of the original blob.
                        blobURL.createSnapshot(null, null, null))
                .flatMap(response ->
                        blobURL.upload(Flowable.just(ByteBuffer.wrap("New text".getBytes())), "New text".length(),
                                null, null, null, null)
                                .flatMap(response1 ->
                                        blobURL.download(null, null, false, null))
                                .flatMap(response1 ->
                                        // Print the received content.
                                        FlowableUtil.collectBytesInBuffer(response1.body(null))
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
                                            .download(null, null, false, null)
                                            .flatMap(response2 ->
                                                    /*
                                                    List the blob(s) in our container, including their snapshots; since
                                                    a container may hold millions of blobs, this is done one segment at
                                                    a time.
                                                    */
                                                    containerURL.listBlobsFlatSegment(null,
                                                            new ListBlobsOptions().withMaxResults(1), null))
                                            .flatMap(response2 ->
                                                    /*
                                                     The asynchronous requests require we use recursion to continue our
                                                     listing.
                                                     */
                                                    listBlobsFlatHelper(containerURL, response2))
                                            .flatMap(response2 ->
                                                    blobURL.startCopyFromURL(snapshotURL.toURL(), null,
                                                            null, null, null));
                                }))
                .flatMap(response ->
                        // Delete the container.
                        containerURL.delete(null, null)
                )
                 /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
    }

    /*
    This example shows how to add progress reporting to the upload and download of blobs.
     */
    @Test
    public void exampleProgressReporting() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));

        ContainerURL containerURL = s.createContainerURL("myjavacontainerprogress" + System.currentTimeMillis());
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("Data.bin");
        Flowable<ByteBuffer> data = Flowable.just(ByteBuffer.wrap("Data".getBytes()));

        // Create the container.
        containerURL.create(null, null, null)
            .flatMap(response ->
                    /*
                    In the call to upload, we add progress reporting to the flowable. Here we choose to just print
                    out the progress. Note that for operations with the TransferManager, progress reporting need
                    not be pre-applied. A ProgressReceiver may simply be set on the options, and the TransferManager
                    will handle coordinating the reporting between parallel requests.
                     */
                    blobURL.upload(ProgressReporter.addProgressReporting(data, System.out::println),
                            4L, null, null, null, null))
            .flatMap(response ->
                    blobURL.download(null, null, false, null))
            .flatMapPublisher(response ->
                    /*
                    Here we add progress reporting to the download response in the same manner.
                     */
                    ProgressReporter.addProgressReporting(response.body(null), System.out::println))
             /*
            This will synchronize all the above operations. This is strongly discouraged for use in production as
            it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
            demonstrate its effectiveness.
             */
            .blockingSubscribe();
    }

    // This example shows how to copy a source document on the Internet to a blob.
    @Test
    public void exampleBlobURLStartCopy() throws MalformedURLException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));

        ContainerURL containerURL = s.createContainerURL("myjavacontainercopy" + System.currentTimeMillis());
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("CopiedBlob.bin");

        // Create the container.
        containerURL.create(null, null, null)
            .flatMap(response ->
                    // Start the copy from the source url to the destination, which is the url pointed to by blobURL
                    blobURL.startCopyFromURL(
                            new URL("https://cdn2.auth0.com/docs/media/addons/azure_blob.svg"),
                            null, null, null, null))
            .flatMap(response ->
                    blobURL.getProperties(null, null))
            .flatMap(response ->
                    waitForCopyHelper(blobURL, response))
            .flatMap(response ->
                    // Delete the container we created earlier.
                    containerURL.delete(null, null))
            /*
            This will synchronize all the above operations. This is strongly discouraged for use in production as
            it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
            demonstrate its effectiveness.
             */
            .blockingGet();

    }

    // <start_copy_helper>
    public Single<BlobGetPropertiesResponse> waitForCopyHelper(BlobURL blobURL, BlobGetPropertiesResponse response)
            throws InterruptedException {
        System.out.println(response.headers().copyStatus());
        if (response.headers().copyStatus() == CopyStatusType.SUCCESS) {
            return Single.just(response);
        }

        Thread.sleep(2000);
        return blobURL.getProperties(null, null)
                .flatMap(response1 ->
                        waitForCopyHelper(blobURL, response1));

    }
    // </start_copy_helper>

    /*
    This example shows how to copy a large file in blocks (chunks) to a block blob and then download it from the blob
    back to a file.
     */
    @Test
    public void exampleFileTransfer() throws IOException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainerparallelupload" + System.currentTimeMillis());
        String filename = "BigFile.bin";
        BlockBlobURL blobURL = containerURL.createBlockBlobURL(filename);
        File tempFile = File.createTempFile("BigFile", ".bin");
        tempFile.deleteOnExit();

        // Create the container.
        containerURL.create(null, null, null)
            .flatMap(response -> Single.using(
                () -> AsynchronousFileChannel.open(tempFile.toPath(), StandardOpenOption.WRITE),
                channel -> Single.fromFuture(channel
                        .write(ByteBuffer.wrap("Big data".getBytes()), 0)),
                AsynchronousFileChannel::close
            ))
            .flatMap(response -> Single.using(
                () -> AsynchronousFileChannel.open(tempFile.toPath(), StandardOpenOption.READ),
                channel -> TransferManager.uploadFileToBlockBlob(channel, blobURL,
                        BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null, null),
                AsynchronousFileChannel::close)
            )
            .flatMap(response -> Single.using(
                () -> AsynchronousFileChannel.open(tempFile.toPath(), StandardOpenOption.WRITE),
                channel -> TransferManager.downloadBlobToFile(channel, blobURL, null, null),
                AsynchronousFileChannel::close)
            )
            .flatMap(response ->
                    // Delete the container.
                    containerURL.delete(null, null))
            /*
            This will synchronize all the above operations. This is strongly discouraged for use in production as
            it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
            demonstrate its effectiveness.
             */
            .blockingGet();
    }

    /*
    This example shows how to upload an arbitrary data stream to a block blob.
     */
    @Test public void exampleUploadNonReplayableFlowable() throws IOException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainerparallelupload" + System.currentTimeMillis());
        String filename = "BigFile.bin";
        BlockBlobURL blobURL = containerURL.createBlockBlobURL(filename);
        File tempFile = File.createTempFile("BigFile", ".bin");
        tempFile.deleteOnExit();

        // Create the container.
        containerURL.create(null, null, null)
                .flatMap(response -> {
                    /*
                    We create a simple flowable for the purposes of demonstration, but the Flowable in question need not
                    produce a repeatable sequence of items. A network stream would be a common use for this api.
                     */
                    Flowable<ByteBuffer> data = Flowable.just(ByteBuffer.allocate(1));
                    return TransferManager.uploadFromNonReplayableFlowable(data, blobURL, 4 * 1024 * 1024, 2, null);
                })
                .flatMap(response ->
                        // Delete the container
                        containerURL.delete())
                /*
                This will synchronize all the above operations. This is strongly discouraged for use in production as
                it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
                demonstrate its effectiveness.
                 */
                .blockingGet();
    }

    /*
    This example shows how to download a large stream with intelligent retries. Specifically, if the connection fails
    while reading, the stream automatically initiates a new downloadBlob call passing a range that starts from the last
    byte successfully read before the failure.
     */
    @Test
    public void exampleReliableDownloadStream() throws IOException, InvalidKeyException {
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Create a BlockBlobURL object that wraps a blob's URL and a default pipeline.
        URL u = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net/", accountName));
        ServiceURL s = new ServiceURL(u,
                StorageURL.createPipeline(new SharedKeyCredentials(accountName, accountKey), new PipelineOptions()));
        ContainerURL containerURL = s.createContainerURL("myjavacontainerretrystream" + System.currentTimeMillis());
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("Data.txt");

        ReliableDownloadOptions options = new ReliableDownloadOptions();
        options.withMaxRetryRequests(5);

        File file = File.createTempFile("tempfile", "txt");
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(5);
        file.deleteOnExit();

        /*
        Passing ReliableDownloadOptions to a call to body() will ensure the download stream is intelligently retried in case
        of failures. The returned body is still a Flowable<ByteBuffer> and may be used as a normal download stream.
         */
        containerURL.create(null, null, null)
            .flatMap(response ->
                    // Upload some data to a blob
                    Single.using(() -> AsynchronousFileChannel.open(file.toPath()),
                        fileChannel -> TransferManager.uploadFileToBlockBlob(fileChannel, blobURL,
                                BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null,
                                new TransferManagerUploadToBlockBlobOptions()),
                        AsynchronousFileChannel::close))
            .flatMap(response ->
                    blobURL.download(null, null, false, null))
            .flatMapPublisher(response ->
                    response.body(options))
            .lastOrError() // Place holder for processing all the intermediary data.
            // After the last piece of data, clean up by deleting the container and all its contents.
            .flatMap(buffer ->
                    // Delete the container
                    containerURL.delete(null, null))
            /*
            This will synchronize all the above operations. This is strongly discouraged for use in production as
            it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
            demonstrate its effectiveness.
             */
            .blockingGet();

    }

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
        ContainerURL containerURL = s.createContainerURL("myjavacontainercreateifnotexist"
                + System.currentTimeMillis());

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
        ContainerURL containerURL = s.createContainerURL("myjavacontainerlistlazy" + System.currentTimeMillis());

        containerURL.create(null, null, null).toCompletable()
            .andThen(Observable.range(0, 5))
            .flatMap(integer -> {
                AppendBlobURL bu = containerURL.createAppendBlobURL(integer.toString());
                return bu.create(null, null, null, null).toObservable();
            })
            .ignoreElements()
            .andThen(listBlobsLazy(containerURL, null))
            .doOnNext(b -> System.out.println("Blob: " + b.name()))
            .ignoreElements()
            .andThen(containerURL.delete(null, null))
            /*
            This will synchronize all the above operations. This is strongly discouraged for use in production as
            it eliminates the benefits of asynchronous IO. We use it here to enable the sample to complete and
            demonstrate its effectiveness.
             */
            .blockingGet();
    }

    /*
    The following is just used a place for quick code snippets that will be included in online documentation. This
    is not meant to serve as a comprehensive example as the above examples are.
     */
    public void apiRefs() throws IOException, InvalidKeyException {
        // <service_url>
        // From the Azure portal, get your Storage account's name and account key.
        String accountName = getAccountName();
        String accountKey = getAccountKey();

        // Use your Storage account's name and key to create a credential object; this is used to access your account.
        SharedKeyCredentials sharedKeyCredentials = new SharedKeyCredentials(accountName, accountKey);

        /*
        Create a request pipeline that is used to process HTTP(S) requests and responses. It requires your account
        credentials. In more advanced scenarios, you can configure telemetry, retry policies, logging, and other
        options. Also you can configure multiple pipelines for different scenarios.
         */
        HttpPipeline pipeline = StorageURL.createPipeline(sharedKeyCredentials, new PipelineOptions());

        /*
        From the Azure portal, get your Storage account blob service URL endpoint.
        The URL typically looks like this:
         */
        URL urlToBlob = new URL(String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName));

        // Create a ServiceURL objet that wraps the service URL and a request pipeline.
        ServiceURL serviceURL = new ServiceURL(urlToBlob, pipeline);
        // </service_url>

        // <pipeline_options>
        LoggingOptions loggingOptions = new LoggingOptions(2000);
        RequestRetryOptions requestRetryOptions = new RequestRetryOptions(RetryPolicyType.EXPONENTIAL, 5,
                4, 1000L, 10000L, "secondary-host");
        PipelineOptions customOptions = new PipelineOptions()
                .withLoggingOptions(loggingOptions)
                .withRequestRetryOptions(requestRetryOptions);
        StorageURL.createPipeline(new AnonymousCredentials(), customOptions);
        // </pipeline_options>

        // <upload_download>
        ContainerURL containerURL = serviceURL.createContainerURL("myjavacontainerbasic");

        BlockBlobURL blobURL = containerURL.createBlockBlobURL("HelloWorld.txt");
        AppendBlobURL appendBlobURL = containerURL.createAppendBlobURL("Data.txt");
        PageBlobURL pageBlobURL = containerURL.createPageBlobURL("pageBlob");

        String data = "Hello world!";

        // Create the container on the service (with no metadata and no public access)
        Single<DownloadResponse> downloadResponse = containerURL.create(null, null, null)
            .flatMap(containersCreateResponse ->
                    /*
                     Create the blob with string (plain text) content.
                     NOTE: It is imperative that the provided length matches the actual length exactly.
                     */
                    blobURL.upload(Flowable.just(ByteBuffer.wrap(data.getBytes())), data.length(),
                            null, null, null, null))
            .flatMap(blobUploadResponse ->
                    // Download the blob's content.
                    blobURL.download(null, null, false, null));
        downloadResponse.flatMap(blobDownloadResponse ->
                // Verify that the blob data round-tripped correctly.
            FlowableUtil.collectBytesInBuffer(blobDownloadResponse.body(null))
                    .doOnSuccess(byteBuffer -> {
                        if (byteBuffer.compareTo(ByteBuffer.wrap(data.getBytes())) != 0) {
                            throw new Exception("The downloaded data does not match the uploaded data.");
                        }
                    }));
        downloadResponse.subscribe();
        // </upload_download>

        // <exception>
        containerURL.create(null, null, null)
                // An error occurred.
            .onErrorResumeNext(throwable -> {
                // Check if this error is from the service.
                if (throwable instanceof StorageException) {
                    StorageException exception = (StorageException) throwable;
                    // StorageErrorCode defines constants corresponding to all error codes returned by the service.
                    if (exception.errorCode() == StorageErrorCode.CONTAINER_BEING_DELETED) {
                        // Log more detailed information.
                        System.out.println("Extended details: " + exception.message());

                        // Examine the raw response.
                        HttpResponse response = exception.response();
                    } else if (exception.errorCode() == StorageErrorCode.CONTAINER_ALREADY_EXISTS) {
                        // Process the error
                        System.out.println("The container url is " + containerURL.toString());
                    }
                }
                // We just fake a successful response to prevent the example from crashing.
                return Single.just(
                        new ContainerCreateResponse(null, 200, null, null, null));
            }).subscribe();
        // </exception>

        // <url_parts>
        /*
         Start with a URL that identifies a snapshot of a blob in a container and includes a Shared Access Signature
         (SAS).
         */
        URL u = new URL("https://myaccount.blob.core.windows.net/mycontainter/ReadMe.txt?"
                + "snapshot=2011-03-09T01:42:34.9360000Z"
                + "&sv=2015-02-21&sr=b&st=2111-01-09T01:42:34Z&se=2222-03-09T01:42:34Z&sp=rw"
                + "&sip=168.1.5.60-168.1.5.70&spr=https,http&si=myIdentifier&ss=bf&srt=s"
                + "&sig=92836758923659283652983562==");

        // You can parse this URL into its constituent parts:
        BlobURLParts parts = URLParser.parse(u);

        // Now, we access the parts (this example prints them).
        System.out.println(String.join("\n",
                parts.host(),
                parts.containerName(),
                parts.blobName(),
                parts.snapshot()));
        System.out.println("");
        SASQueryParameters sas = parts.sasQueryParameters();
        System.out.println(String.join("\n",
                sas.version(),
                sas.resource(),
                sas.startTime().toString(),
                sas.expiryTime().toString(),
                sas.permissions(),
                sas.ipRange().toString(),
                sas.protocol().toString(),
                sas.identifier(),
                sas.services(),
                sas.signature()));

        // You can then change some of the fields and construct a new URL.
        parts.withSasQueryParameters(null) // Remove the SAS query parameters.
                .withSnapshot(null) // Remove the snapshot timestamp.
                .withContainerName("othercontainer"); // Change the container name.
        // In this example, we'll keep the blob name as it is.

        // Construct a new URL from the parts:
        URL newURL = parts.toURL();
        System.out.println(newURL);
        // NOTE: You can pass the new URL to the constructor for any XxxURL to manipulate the resource.
        // </url_parts>

        // <account_sas>
        // Use your Storage account's name and key to create a credential object; this is required to sign a SAS.
        SharedKeyCredentials credential = new SharedKeyCredentials(getAccountName(), getAccountKey());

        /*
        Set the desired SAS signature values and sign them with the shared key credentials to get the SAS query
        parameters.
         */
        AccountSASSignatureValues values = new AccountSASSignatureValues();
        values.withProtocol(SASProtocol.HTTPS_ONLY) // Users MUST use HTTPS (not HTTP).
                .withExpiryTime(OffsetDateTime.now().plusDays(2)); // 2 days before expiration.

        AccountSASPermission permission = new AccountSASPermission()
                .withRead(true)
                .withList(true);
        values.withPermissions(permission.toString());

        AccountSASService service = new AccountSASService()
                .withBlob(true);
        values.withServices(service.toString());

        AccountSASResourceType resourceType = new AccountSASResourceType()
                .withContainer(true)
                .withObject(true);
        values.withResourceTypes(resourceType.toString());

        SASQueryParameters params = values.generateSASQueryParameters(credential);

        // Calling encode will generate the query string.
        String encodedParams = params.encode();

        String urlToSendToSomeone = String.format(Locale.ROOT, "https://%s.blob.core.windows.net?%s",
                getAccountName(), encodedParams);
        // At this point, you can send the urlToSendSomeone to someone via email or any other mechanism you choose.

        // ***************************************************************************************************

        // When someone receives the URL, the access the SAS-protected resource with code like this:
        u = new URL(urlToSendToSomeone);

        /*
         Create a ServiceURL object that wraps the serviceURL (and its SAS) and a pipeline. When using SAS URLs,
         AnonymousCredentials are required.
         */
        ServiceURL sURL = new ServiceURL(u,
                StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()));
        // Now, you can use this serviceURL just like any other to make requests of the resource.
        // </account_sas>

        // <service_sas>
        // Use your Storage account's name and key to create a credential object; this is required to sign a SAS.
        credential = new SharedKeyCredentials(getAccountName(), getAccountKey());

        // This is the name of the container and blob that we're creating a SAS to.
        String containerName = "mycontainer"; // Container names require lowercase.
        String blobName = "HelloWorld.txt"; // Blob names can be mixed case.
        String snapshotId = "2018-01-01T00:00:00.0000000Z"; // SAS can be restricted to a specific snapshot

        /*
        Set the desired SAS signature values and sign them with the shared key credentials to get the SAS query
        parameters.
         */
        ServiceSASSignatureValues blobValues = new ServiceSASSignatureValues()
                .withProtocol(SASProtocol.HTTPS_ONLY) // Users MUST use HTTPS (not HTTP).
                .withExpiryTime(OffsetDateTime.now().plusDays(2)) // 2 days before expiration.
                .withContainerName(containerName)
                .withBlobName(blobName)
                .withSnapshotId(snapshotId);

        /*
        To produce a container SAS (as opposed to a blob SAS), assign to Permissions using ContainerSASPermissions, and
        make sure the blobName and snapshotId fields are null (the default).
         */
        BlobSASPermission blobPermission = new BlobSASPermission()
                .withRead(true)
                .withAdd(true)
                .withWrite(true);
        values.withPermissions(permission.toString());

        SASQueryParameters serviceParams = values.generateSASQueryParameters(credential);

        // Calling encode will generate the query string.
        encodedParams = serviceParams.encode();
        // Colons are not safe characters in a URL; they must be properly encoded.
        snapshotId = snapshotId.replace(":", "%3A");

        urlToSendToSomeone = String.format(Locale.ROOT, "https://%s.blob.core.windows.net/%s/%s?%s&%s",
                getAccountName(), containerName, blobName, snapshotId, encodedParams);
        // At this point, you can send the urlToSendSomeone to someone via email or any other mechanism you choose.

        // ***************************************************************************************************

        // When someone receives the URL, the access the SAS-protected resource with code like this:
        u = new URL(urlToSendToSomeone);

        /*
         Create a BlobURL object that wraps the blobURL (and its SAS) and a pipeline. When using SAS URLs,
         AnonymousCredentials are required.
         */
        BlobURL bURL = new BlobURL(u,
                StorageURL.createPipeline(new AnonymousCredentials(), new PipelineOptions()));
        // Now, you can use this blobURL just like any other to make requests of the resource.
        // </service_sas>

        // <blocks>
        BlockBlobURL blockBlobURL = containerURL.createBlockBlobURL("Data.txt");

        String[] blockData = {"Michael", "Gabriel", "Raphael", "John"};
        String initialBlockID = Base64.getEncoder().encodeToString(
                UUID.randomUUID().toString().getBytes());

        // Create the container. We convert to an Observable to be able to work with the block list effectively.
        containerURL.create(null, null, null)
                .flatMapObservable(response ->
                        // Create an Observable that will yield each of the Strings one at a time.
                        Observable.fromIterable(Arrays.asList(blockData))
                )
                // Items emitted by an Observable that results from a concatMap call will preserve the original order.
                .concatMapEager(block -> {
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
                    return blockBlobURL.stageBlock(blockId, Flowable.just(ByteBuffer.wrap(block.getBytes())),
                            block.length(), null, null)
                            /*
                             We do not care for any data on the response object, but we do want to keep track of the
                             ID.
                             */
                            .map(x -> blockId).toObservable();
                })
                // Gather all of the IDs emitted by the previous observable into a single list.
                .collectInto(new ArrayList<>(blockData.length), (BiConsumer<ArrayList<String>, String>) ArrayList::add)
                .flatMap(idList -> {
                        /*
                        By this point, all the blocks are upload and we have an ordered list of their IDs. Here, we
                        atomically commit the whole list.
                        NOTE: The block list order need not match the order in which the blocks were uploaded. The order
                        of IDs in the commitBlockList call will determine the structure of the blob.
                         */
                    idList.add(0, initialBlockID);
                    return blockBlobURL.commitBlockList(idList, null, null, null, null);
                })
                .flatMap(response ->
                        /*
                         For the blob, show each block (ID and size) that is a committed part of it. It is also possible
                         to include blocks that have been staged but not committed.
                         */
                        blockBlobURL.getBlockList(BlockListType.ALL, null, null))
                .subscribe();
        // </blocks>

        // <block_from_url>
        String blockID = Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
        blockBlobURL.stageBlockFromURL(blockID, blobURL.toURL(), null, null, null, null, null)
                .flatMap(response ->
                        blockBlobURL.commitBlockList(Arrays.asList(blockID), null, null, null, null))
                .subscribe();
        // </block_from_url>

        // <append_blob>

        // Create the container.
        containerURL.create(null, null, null)
                .flatMap(response ->
                        // Create the append blob. This creates a zero-length blob that we can now append to.
                        appendBlobURL.create(null, null, null, null))
                .flatMapObservable(response ->
                        // This range will act as our for loop to create 5 blocks
                        Observable.range(0, 5))
                .concatMapEager(i -> {
                    String text = String.format(Locale.ROOT, "Appending block #%d\n", i);
                    return appendBlobURL.appendBlock(Flowable.just(ByteBuffer.wrap(text.getBytes())), text.length(),
                            null, null).toObservable();
                }).subscribe();
        // </append_blob>

        // <append_from_url>
        appendBlobURL.appendBlockFromUrl(blobURL.toURL(), new BlobRange().withOffset(50), null, null, null, null)
                .subscribe();
        // </append_from_url>

        // <snapshot>
        // Create the container.
        containerURL.create(null, null, null)
                .flatMap(response ->
                        // Create the original blob.
                        blobURL.upload(Flowable.just(ByteBuffer.wrap("Some text".getBytes())), "Some text".length(),
                                null, null, null, null))
                .flatMap(response ->
                        // Create a snapshot of the original blob.
                        blobURL.createSnapshot(null, null, null))
                .flatMap(response -> {
                    BlobURL snapshotURL = blobURL.withSnapshot(response.headers().snapshot());
                    return snapshotURL.getProperties(null, null);
                }).subscribe();
        // </snapshot>

        // <start_copy>
        // Create the container.
        containerURL.create(null, null, null)
                .flatMap(response ->
                        // Start the copy from the source url to the destination, which is the url pointed to by blobURL
                        blobURL.startCopyFromURL(
                                new URL("https://cdn2.auth0.com/docs/media/addons/azure_blob.svg"),
                                null, null, null, null))
                .flatMap(response ->
                        blobURL.getProperties(null, null))
                .flatMap(response ->
                        waitForCopyHelper(blobURL, response))
                .subscribe();
        // </start_copy>

        // <abort_copy>
        containerURL.create(null, null, null)
                .flatMap(response ->
                        // Start the copy from the source url to the destination, which is the url pointed to by blobURL
                        blobURL.startCopyFromURL(
                                new URL("https://cdn2.auth0.com/docs/media/addons/azure_blob.svg"),
                                null, null, null, null))
                .flatMap(response ->
                        blobURL.getProperties(null, null))
                .flatMap(response ->
                        blobURL.abortCopyFromURL(response.headers().copyId(), null, null))
                .subscribe();
        // </abort_copy>

        // <sync_copy>
        // Create the container.
        containerURL.create()
                .flatMap(response ->
                        /*
                        Copy from the source url to the destination, which is the url pointed to by blobURL. Note that
                        the service will not return a response until the copy is complete, hence "sync" copy.
                         */
                        blobURL.syncCopyFromURL(new URL("https://cdn2.auth0.com/docs/media/addons/azure_blob.svg")))
                .subscribe();
        // </sync_copy>

        // <blob_delete>
        blobURL.delete(null, null, null)
                .subscribe();
        // </blob_delete>

        // <undelete>
        // This sample assumes that the account has a delete retention policy set.
        blobURL.delete(null, null, null)
                .flatMap(response ->
                        blobURL.undelete(null))
                .subscribe();
        // </undelete>

        // <tier>
        // BlockBlobs and PageBlobs have different sets of tiers.
        blockBlobURL.setTier(AccessTier.HOT, null, null)
                .subscribe();
        pageBlobURL.setTier(AccessTier.P6, null, null)
                .subscribe();
        // </tier>

        // <properties_metadata>
        containerURL.create(null, null, null)
                .flatMap(containersCreateResponse ->
                        /*
                         Create the blob with string (plain text) content.
                         NOTE: It is imperative that the provided length matches the actual length exactly.
                         */
                        blobURL.upload(Flowable.just(ByteBuffer.wrap(data.getBytes())), data.length(),
                                null, null, null, null))
                .flatMap(response ->
                        blobURL.getProperties(null, null))
                .flatMap(response -> {
                    Metadata newMetadata = new Metadata(response.headers().metadata());
                    // If one of the HTTP properties is set, all must be set again or they will be cleared.
                    BlobHTTPHeaders newHeaders = new BlobHTTPHeaders()
                            .withBlobCacheControl(response.headers().cacheControl())
                            .withBlobContentDisposition(response.headers().contentDisposition())
                            .withBlobContentEncoding(response.headers().contentEncoding())
                            .withBlobContentLanguage("new language")
                            .withBlobContentMD5(response.headers().contentMD5())
                            .withBlobContentType("new content");
                    return blobURL.setMetadata(newMetadata, null, null)
                            .flatMap(nextResponse -> blobURL.setHTTPHeaders(newHeaders, null, null));
                })
                .subscribe();
        // </properties_metadata>

        // <container_basic>
        containerURL.create(null, null, null)
                .flatMap(response ->
                        containerURL.getProperties(null, null))
                .flatMap(response -> {
                    Metadata metadata = new Metadata();
                    metadata.put("key", "value");
                    return containerURL.setMetadata(metadata, null, null);
                })
                .flatMap(response ->
                        containerURL.delete(null, null))
                .subscribe();
        // </container_basic>

        // <container_policy>
        containerURL.create(null, null, null)
                .flatMap(response -> {
                    /*
                    Create a SignedIdentifier that gives read permissions and expires one day for now. This means that
                    any SAS associated with this policy has these properties.
                     */
                    BlobSASPermission perms = new BlobSASPermission()
                            .withRead(true);
                    SignedIdentifier id = new SignedIdentifier().withId("policy1").withAccessPolicy(
                            new AccessPolicy().withPermission(perms.toString()).withExpiry(OffsetDateTime.now()
                                    .plusDays(1)));
                    // Give public access to the blobs in this container and apply the SignedIdentifier.
                    return containerURL.setAccessPolicy(PublicAccessType.BLOB, Arrays.asList(id), null, null);
                })
                .subscribe();
        // </container_policy>

        // <list_blobs_flat>
        containerURL.listBlobsFlatSegment(null, new ListBlobsOptions().withMaxResults(1), null)
                .flatMap(containersListBlobFlatSegmentResponse ->
                        // The asynchronous requests require we use recursion to continue our listing.
                        listBlobsFlatHelper(containerURL, containersListBlobFlatSegmentResponse))
                .subscribe();
        // </list_blobs_flat>

        // <list_blobs_hierarchy>
        containerURL.listBlobsHierarchySegment(null, "my_delimiter", new ListBlobsOptions().withMaxResults(1), null)
                .flatMap(containersListBlobHierarchySegmentResponse ->
                        // The asynchronous requests require we use recursion to continue our listing.
                        listBlobsHierarchyHelper(containerURL, containersListBlobHierarchySegmentResponse))
                .subscribe();
        // </list_blobs_hierarchy>

        // <page_blob_basic>
        containerURL.create(null, null, null)
                .flatMap(response ->
                        // Create the page blob with 4 512-byte pages.
                        pageBlobURL.create(4 * PageBlobURL.PAGE_BYTES, null, null,
                                null, null, null))
                .flatMap(response -> {
                    /*
                     Upload data to a page.
                     NOTE: The page range must start on a multiple of the page size and end on
                     (multiple of page size) - 1.
                     */
                    byte[] pageData = new byte[PageBlobURL.PAGE_BYTES];
                    for (int i = 0; i < PageBlobURL.PAGE_BYTES; i++) {
                        pageData[i] = 'a';
                    }
                    return pageBlobURL.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                            Flowable.just(ByteBuffer.wrap(pageData)), null, null);
                })
                .flatMap(response ->
                        // Get the page ranges which have valid data.
                        pageBlobURL.getPageRanges(null, null, null))
                .flatMap(response -> {
                    // Print the pages that are valid.
                    for (PageRange range : response.body().pageRange()) {
                        System.out.println(String.format(Locale.ROOT, "Start=%d, End=%d\n", range.start(),
                                range.end()));
                    }

                    // Clear and invalidate the first range.
                    return pageBlobURL.clearPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                            null, null);
                })
                .flatMap(response ->
                        pageBlobURL.resize(1024, null, null))
                .flatMap(rsponse ->
                        pageBlobURL.updateSequenceNumber(SequenceNumberActionType.INCREMENT, null,
                                null, null))
                .subscribe();
        // </page_blob_basic>

        // <page_diff>
        pageBlobURL.create(4 * PageBlobURL.PAGE_BYTES, null, null,
                null, null, null)
                .flatMap(response ->
                        pageBlobURL.createSnapshot(null, null, null))
                .flatMap(response -> {
                    /*
                     Upload data to a page.
                     NOTE: The page range must start on a multiple of the page size and end on
                     (multiple of page size) - 1.
                     */
                    byte[] pageData = new byte[PageBlobURL.PAGE_BYTES];
                    for (int i = 0; i < PageBlobURL.PAGE_BYTES; i++) {
                        pageData[i] = 'a';
                    }
                    return pageBlobURL.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                            Flowable.just(ByteBuffer.wrap(pageData)), null, null)
                            // We still need access to the snapshotResponse.
                            .flatMap(uploadResponse ->
                                    pageBlobURL.getPageRangesDiff(null, response.headers().snapshot(),
                                            null, null));
                });
        // </page_diff>

        // <incremental_copy>
        PageBlobURL incrementalCopy = containerURL.createPageBlobURL("incremental");
        pageBlobURL.createSnapshot(null, null, null)
            .flatMap(response ->
                    incrementalCopy.copyIncremental(pageBlobURL.toURL(), response.headers().snapshot(), null, null))
            .flatMap(response -> {
                byte[] pageData = new byte[PageBlobURL.PAGE_BYTES];
                for (int i = 0; i < PageBlobURL.PAGE_BYTES; i++) {
                    pageData[i] = 'a';
                }
                return pageBlobURL.uploadPages(new PageRange().withStart(0).withEnd(PageBlobURL.PAGE_BYTES - 1),
                        Flowable.just(ByteBuffer.wrap(pageData)), null, null);
            })
            .flatMap(response ->
                    pageBlobURL.createSnapshot(null, null, null))
            .flatMap(response ->
                    incrementalCopy.copyIncremental(pageBlobURL.toURL(), response.headers().snapshot(), null, null))
            .subscribe();
        /*
        The result is a new blob with two new snapshots that correspond to the source blob snapshots but with different
        IDs. These snapshots may be read from like normal snapshots.
         */
        // </incremental_copy>

        // <page_from_url>
        pageBlobURL.uploadPagesFromURL(new PageRange().withStart(0).withEnd(511), blobURL.toURL(), 2048L, null, null,
                null, null)
                .subscribe();
        // </page_from_url>

        // <blob_lease>
        blobURL.acquireLease(null, 20, null, null)
                .flatMap(response ->
                        blobURL.changeLease(response.headers().leaseId(), "proposed", null, null))
                .flatMap(response ->
                        blobURL.renewLease(response.headers().leaseId(), null, null))
                .flatMap(response ->
                        blobURL.breakLease(null, null, null)
                                .flatMap(breakResponse ->
                                        blobURL.releaseLease(response.headers().leaseId(), null, null)))
                .subscribe();
        // </blob_lease>

        // <container_lease>
        containerURL.acquireLease(null, 20, null, null)
                .flatMap(response ->
                        containerURL.changeLease(response.headers().leaseId(), "proposed",
                                null, null))
                .flatMap(response ->
                        containerURL.renewLease(response.headers().leaseId(), null, null))
                .flatMap(response ->
                        containerURL.breakLease(null, null, null)
                                .flatMap(breakResponse ->
                                        containerURL.releaseLease(response.headers().leaseId(), null, null)))
                .subscribe();
        // </container_lease>

        ByteBuffer largeData = ByteBuffer.wrap("LargeData".getBytes());

        ByteBuffer largeBuffer = ByteBuffer.allocate(10 * 1024);

        File tempFile = File.createTempFile("BigFile", ".bin");
        tempFile.deleteOnExit();
        // <tm_file>
        Single.using(
            () -> AsynchronousFileChannel.open(tempFile.toPath(), StandardOpenOption.WRITE),
            channel -> Single.fromFuture(channel
                    .write(ByteBuffer.wrap("Big data".getBytes()), 0)), AsynchronousFileChannel::close)
            .flatMap(response -> Single.using(
                () -> AsynchronousFileChannel.open(tempFile.toPath(), StandardOpenOption.READ),
                channel -> TransferManager.uploadFileToBlockBlob(channel, blobURL,
                        BlockBlobURL.MAX_STAGE_BLOCK_BYTES, null, null),
                AsynchronousFileChannel::close)
            )
            .flatMap(response -> Single.using(
                () -> AsynchronousFileChannel.open(tempFile.toPath(), StandardOpenOption.WRITE),
                channel -> TransferManager.downloadBlobToFile(channel, blobURL, null, null),
                AsynchronousFileChannel::close)
            )
            .flatMap(response ->
                    // Delete the container.
                    containerURL.delete(null, null));
        // </tm_file>

        // <tm_nrf>
        /*
         We create a simple flowable for the purposes of demonstration, but the Flowable in question need not
         produce a repeatable sequence of items. A network stream would be a common use for this api.
         */
        Flowable<ByteBuffer> nonReplayableFlowable = Flowable.just(ByteBuffer.allocate(1));
        TransferManager.uploadFromNonReplayableFlowable(nonReplayableFlowable, blobURL, 4 * 1024 * 1024, 2, null);
        // </tm_nrf>

        // <service_getsetprops>
        serviceURL.getProperties(null)
                .flatMap(response -> {
                    StorageServiceProperties newProps = response.body();

                    // Remove the delete retention policy to disable soft delete.
                    newProps.withDeleteRetentionPolicy(null);

                    return serviceURL.setProperties(newProps, null);
                })
                .subscribe();
        // </service_getsetprops>

        // <service_stats>
        serviceURL.getStatistics(null)
                .subscribe();
        // </service_stats>

        // <service_list>
        serviceURL.listContainersSegment(null, new ListContainersOptions(), null)
                .flatMap(listContainersSegmentResponse ->
                        // The asynchronous requests require we use recursion to continue our listing.
                        listContainersHelper(serviceURL, listContainersSegmentResponse))
                .subscribe();
        // </service_list>

        // <account_info>
        serviceURL.getAccountInfo(null)
                .subscribe();
        containerURL.getAccountInfo(null)
                .subscribe();
        blobURL.getAccountInfo(null)
                .subscribe();
        // </account_info>

        // <progress>
        Flowable<ByteBuffer> flowableData = Flowable.just(ByteBuffer.wrap("Data".getBytes()));
        flowableData = ProgressReporter.addProgressReporting(flowableData, System.out::println);
        // </progress>
    }
}

