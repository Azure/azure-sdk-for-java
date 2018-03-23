package com.microsoft.azure.storage.samples

import com.microsoft.azure.storage.blob.BlobRange
import com.microsoft.azure.storage.blob.ServiceURL
import com.microsoft.rest.v2.RestException
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Completable
import io.reactivex.Flowable
import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Paths
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    val accountName = System.getenv("AZURE_STORAGE_ACCOUNT_NAME")
    val pipeline = BasicSample.getPipeline(
            accountName,
            System.getenv("AZURE_STORAGE_ACCOUNT_KEY"))

    val serviceURL = ServiceURL(URL("http://$accountName.blob.core.windows.net"), pipeline)
    val containerURL = serviceURL.createContainerURL("javasdktest")
    val blobURL = containerURL.createBlockBlobURL("testBlob")

    val data = ByteBuffer.wrap(byteArrayOf(0, 1, 2, 3, 4))

    val disposable = containerURL.create(null, null)
            .toCompletable()
            .onErrorResumeNext({ throwable ->
                if (throwable is RestException && throwable.response().statusCode() == 409) {
                    Completable.complete()
                } else {
                    Completable.error(throwable)
                }
            })
            .andThen(blobURL.putBlob(Flowable.just(data), data.remaining().toLong(), null, null, null))
            .flatMap { _ -> blobURL.getBlob(BlobRange(0L, data.remaining().toLong()), null, false) }
            .flatMapCompletable({ response ->
                val outFile = AsynchronousFileChannel.open(Paths.get("myFilePath"))
                FlowableUtil.writeFile(response.body(), outFile)
                        .timeout(1, TimeUnit.SECONDS)
                        .doOnTerminate(outFile::close)
            }).subscribe({ System.out.println("Finished blob download." )})

    readLine()
}