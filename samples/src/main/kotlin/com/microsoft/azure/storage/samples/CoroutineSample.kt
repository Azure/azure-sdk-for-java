package com.microsoft.azure.storage.samples

import com.microsoft.azure.storage.blob.BlobRange
import com.microsoft.azure.storage.blob.ServiceURL
import com.microsoft.rest.v2.RestException
import com.microsoft.rest.v2.util.FlowableUtil
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.experimental.async
import java.net.URL
import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.concurrent.TimeUnit
import kotlin.coroutines.experimental.suspendCoroutine

fun main(args: Array<String>) {
    async {
        try {
            runSample()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    readLine()
}

suspend fun runSample() {
    val accountName = System.getenv("AZURE_STORAGE_ACCOUNT_NAME")
    val pipeline = BasicSample.getPipeline(
            accountName,
            System.getenv("AZURE_STORAGE_ACCOUNT_KEY"))

    val serviceURL = ServiceURL(URL("http://$accountName.blob.core.windows.net"), pipeline)
    val containerURL = serviceURL.createContainerURL("javasdktest")
    val blobURL = containerURL.createBlockBlobURL("testBlob")

    val data = byteArrayOf(0, 1, 2, 3, 4)

    try {
        containerURL.create(null, null).suspendCoroutine()
    } catch (ex: RestException) {
        if (ex.response().statusCode() != 409) {
            throw ex
        } else { }
    }

    blobURL.putBlob(Flowable.just(ByteBuffer.wrap(data)), data.size.toLong(), null, null, null).suspendCoroutine()
    val getRes = blobURL.getBlob(BlobRange(0L, data.size.toLong()), null, false).suspendCoroutine()
    val outFile = AsynchronousFileChannel.open(Paths.get("myFilePath"), StandardOpenOption.WRITE, StandardOpenOption.CREATE)

    FlowableUtil.writeFile(getRes.body(), outFile)
        .timeout(1, TimeUnit.SECONDS)
        .suspendCoroutine()

    outFile.close()
    println("Finished blob download.")
}

suspend fun <T> Single<T>.suspendCoroutine(): T {
    return suspendCoroutine { cont ->
        subscribe(cont::resume, cont::resumeWithException)
    }
}

suspend fun Completable.suspendCoroutine() {
    return suspendCoroutine { cont ->
        subscribe({ cont.resume(Unit) }, cont::resumeWithException)
    }
}
