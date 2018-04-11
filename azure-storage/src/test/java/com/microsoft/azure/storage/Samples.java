package com.microsoft.azure.storage;

import com.microsoft.azure.storage.blob.*;
import com.microsoft.azure.storage.blob.models.*;
import com.microsoft.rest.v2.http.HttpPipeline;
import com.microsoft.rest.v2.http.HttpPipelineLogLevel;
import com.microsoft.rest.v2.http.HttpPipelineLogger;
import com.microsoft.rest.v2.util.FlowableUtil;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
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
        URL u = new URL(String.format("https://%s.blob.core.windows.net", accountName));

        // Create a ServiceURL objet that wraps the service URL and a request pipeline.
        ServiceURL serviceURL = new ServiceURL(u, pipeline);

        // Now you can use the ServiceURL to perform various container and blob operations.

        // This example shows several common operations just to get you started.

        /*
        Create a URL that references a to-be-created container in your Azure Storage account. This returns a
        ContainerURL object that wraps the container's URL and a request pipeline (inherited from serviceURL).
        Note that container names require lowercase.
         */
        ContainerURL containerURL = serviceURL.createContainerURL("myjavacontainer");

        /*
        Create a URL that references a to-be-created blob in your Azure Storage account's container.
        This returns a BlockBlobURL objec that wraps the blob's URl and a request pipeline
        (inherited from containerURL). Note that blob names can be mixed case.
         */
        BlockBlobURL blobURL = containerURL.createBlockBlobURL("HelloWorld.txt");

        String data = "Hello world!";

        // Create the container on the service (with no metadata and no public access)
        containerURL.create(null, null)
                .flatMap(containersCreateResponse -> {
                    return blobURL.upload(Flowable.just(ByteBuffer.wrap(data.getBytes())), data.length(),
                            null, null, null);
                })
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
                        containerURL.listBlobsFlatSegment(null, new ListBlobsOptions(null, null, 1)))
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
                System.out.println("Blob name: " + b.name());
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
            return containerURL.listBlobsFlatSegment(nextMarker, new ListBlobsOptions(null, null, 1))
                    .flatMap(containersListBlobFlatSegmentResponse ->
                            listBlobsHelper(containerURL, containersListBlobFlatSegmentResponse));
        }
    }

    @Test
    // This example shows how you can configure a pipeline for making HTTP requests to the Azure Storage blob Service.
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
                }
                else if (httpPipelineLogLevel == HttpPipelineLogLevel.WARNING) {
                    level = Level.WARNING;
                }
                else if (httpPipelineLogLevel == HttpPipelineLogLevel.INFO) {
                    level = Level.INFO;
                }
                else if (httpPipelineLogLevel == HttpPipelineLogLevel.OFF) {
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

    @Test
    /*
    This example shows how to break a URL into its parts so you can examine and/or change some of its values and then
    construct a new URL.
     */
    public void exampleBlobURLParts() throws MalformedURLException, UnknownHostException {
        /*
         Start with a URL that identifies a snapshot of a blob in a container and includes a Shared Access Signature
         (SAS).
         */
        URL u = new URL("https://myaccount.blob.core.windows.net/mycontainter/ReadMe.txt?" +
                "snapshot=2011-03-09T01:42:34.9360000Z&" +
                "sv=2015-02-21&sr=b&st=2111-01-09T01:42:34Z&se=2222-03-09T01:42:34Z&sp=rw" +
                "&sip=168.1.5.60-168.1.5.70&spr=https,http&si=myIdentifier&ss=bf&srt=s" +
                "&sig=92836758923659283652983562==");

        // You can parse this URL into its constituent parts:
        BlobURLParts parts = URLParser.parse(u);

        // Now, we access the parts (this example prints them).
        System.out.println(String.join("\n",
                new String[]{parts.host,
                        parts.containerName,
                        parts.blobName,
                        parts.snapshot}));
        System.out.println("");
        SASQueryParameters sas = parts.sasQueryParameters;
        System.out.println(String.join("\n",
                new String[]{sas.getVersion(),
                sas.getResource(),
                sas.getStartTime().toString(),
                sas.getExpiryTime().toString(),
                sas.getPermissions(),
                sas.getIpRange().toString(),
                sas.getProtocol().toString(),
                sas.getIdentifier(),
                sas.getServices(),
                sas.getSignature()}));

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
}

