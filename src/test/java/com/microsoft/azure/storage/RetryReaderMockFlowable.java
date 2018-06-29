package com.microsoft.azure.storage;

import com.microsoft.azure.storage.blob.RetryReader;
import com.microsoft.azure.storage.blob.models.BlobsDownloadHeaders;
import com.microsoft.rest.v2.RestResponse;
import io.reactivex.Flowable;
import io.reactivex.Single;
import org.reactivestreams.Subscriber;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class RetryReaderMockFlowable extends Flowable<ByteBuffer> {

    public static final int RR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK = 0;

    public static final int RR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK = 1;

    private int scenario;

    private int tryNumber;

    private ByteBuffer scenarioData;

    public ByteBuffer getScenarioData() {
        return this.scenarioData;
    }

    public RetryReaderMockFlowable(int scenario) {
        this.scenario = scenario;
        switch(this.scenario) {
            case RR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK:
                this.scenarioData = APISpec.getRandomData(512*1024);
                break;
            case RR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK:
                this.scenarioData = APISpec.getRandomData(1024);
                break;
        }
    }

    @Override
    protected void subscribeActual(Subscriber<? super ByteBuffer> s) {
        switch(this.scenario) {
            case RR_TEST_SCENARIO_SUCCESSFUL_ONE_CHUNK:
                s.onNext(this.scenarioData.duplicate());
                s.onComplete();
                break;
            case RR_TEST_SCENARIO_SUCCESSFUL_MULTI_CHUNK:
                for (int i=0; i<4; i++) {
                    ByteBuffer toSend = this.scenarioData.duplicate();
                    toSend.position(i*256);
                    toSend.limit((i+1)*256);
                    s.onNext(toSend);
                }
                s.onComplete();
                break;
        }
    }

    public Single<? extends RestResponse<?, Flowable<ByteBuffer>>> getter(RetryReader.HTTPGetterInfo info) {
        tryNumber++;
        RestResponse<BlobsDownloadHeaders, Flowable<ByteBuffer>> response =
                new RestResponse<>(200, new BlobsDownloadHeaders(), new HashMap<>(), this);
        return Single.just(response);
    }
}
