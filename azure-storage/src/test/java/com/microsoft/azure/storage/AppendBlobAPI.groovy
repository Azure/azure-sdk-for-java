package com.microsoft.azure.storage

import com.microsoft.azure.storage.blob.AppendBlobURL
import com.microsoft.azure.storage.blob.models.AppendBlobCreateResponse
import io.reactivex.Flowable

class AppendBlobAPI extends APISpec {
    AppendBlobURL bu

    def setup() {
        bu = cu.createAppendBlobURL(generateBlobName())
        bu.create(null, null, null).blockingGet()
    }

    def "Append blob create all null"() {
        setup:
        bu = cu.createAppendBlobURL(generateBlobName())

        when:
        AppendBlobCreateResponse response =
                bu.create(null, null, null).blockingGet()

        then:
        response.statusCode() == 201
        response.headers().eTag() != null
        response.headers().dateProperty() != null
        response.headers().lastModified() != null
        response.headers().requestId() != null
        response.headers().version() != null
    }

    def "Append blob append block"() {
        expect:
        bu.appendBlock(Flowable.just(defaultData), defaultData.remaining(), null).blockingGet()
                .statusCode() == 201
    }


}
