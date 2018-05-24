package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.BlockBlobURL
import com.microsoft.azure.storage.blob.CommonRestResponse
import com.microsoft.azure.storage.blob.ContainerURL
import com.microsoft.azure.storage.blob.PipelineOptions
import com.microsoft.azure.storage.blob.RequestRetryOptions
import com.microsoft.azure.storage.blob.ServiceURL
import com.microsoft.azure.storage.blob.StorageURL
import com.microsoft.azure.storage.blob.TransferManager
import com.microsoft.rest.v2.http.HttpClient
import com.microsoft.rest.v2.http.HttpClientConfiguration
import com.microsoft.rest.v2.http.HttpPipeline
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Function

import java.nio.ReadOnlyBufferException
import java.nio.channels.FileChannel

class TransferManagerTest extends APISpec {
    def "https parallel file upload"() {
        setup:
        PipelineOptions po = new PipelineOptions()
        RequestRetryOptions retryOptions = new RequestRetryOptions(null, null, 300,
                null, null, null)
        po.requestRetryOptions = retryOptions
        po.client = getHttpClient()

        HttpPipeline pipeline = StorageURL.createPipeline(primaryCreds, po)

        // This test requires https.
        ServiceURL surl = new ServiceURL(new URL("https://" + primaryCreds.getAccountName() + ".blob.core.windows.net"),
                pipeline)

        ContainerURL containerURL = surl.createContainerURL(generateContainerName())
        containerURL.create(null, null).blockingGet()

        when:
        /*
         We are simply testing for no errors here. There has historically been a problem with Netty that caused it to
         crash when uploading multiple medium size files in parallel over https. Here we validate that behavior is
         fixed. We will test for correctness of the parallel upload elsewhere.
         */
        Observable.range(0, 4000)
                .flatMap(new Function<Integer, ObservableSource>() {
            @Override
            ObservableSource apply(@NonNull Integer i) throws Exception {
                BlockBlobURL asyncblob = containerURL.createBlockBlobURL("asyncblob" + i)
                TransferManager.UploadToBlockBlobOptions asyncOptions = new TransferManager.UploadToBlockBlobOptions(
                        null, null, null, null, 1)

                return TransferManager.uploadFileToBlockBlob(
                        FileChannel.open(new File(getClass().getClassLoader().getResource("15mb.txt").getFile())
                                .toPath()), asyncblob,BlockBlobURL.MAX_PUT_BLOCK_BYTES, asyncOptions).toObservable()
            }
        }, 2000)
        .onErrorReturn((new Function<Throwable, Object>() {
            @Override
            public Object apply(Throwable throwable) throws Exception {
                /*
                We only care about the ReadOnlyBufferException as an indication of the netty failure with memory mapped
                files. Everything else, like throttling, is fine here.
                 */
                if (throwable instanceof ReadOnlyBufferException) {
                    throw throwable
                }
                // This value is not meaningful. We just want the observable to continue.
                return new Object()
            }
        })).blockingSubscribe()
        containerURL.delete(null).blockingGet()

        then:
        notThrown(ReadOnlyBufferException)
    }
}
