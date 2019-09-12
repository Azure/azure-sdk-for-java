// Copyright (c) Microsoft Corporation.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

import android.util.DisplayMetrics;
import com.azure.ai.inkrecognizer.model.InkRecognitionRoot;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static com.azure.ai.inkrecognizer.TestUtils.*;

public class InkRecognizerAsyncClientTest {

    private InkRecognizerAsyncClient inkRecognizerAsyncClient;
    private InkRecognizerCredentials credential = new InkRecognizerCredentials("");
    private String endpoint = "https://api.cognitive.microsoft.com/inkrecognizer";
    private final int retryCount = 3;
    private final int retryTimeout = 300;
    private ApplicationKind applicationKind = ApplicationKind.MIXED;
    private String language = "en-US";
    private InkPointUnit unit = InkPointUnit.MM;
    private final float unitMultiple = 1.2f;
    private ServiceVersion serviceVersion = ServiceVersion.PREVIEW_1_0_0;
    private DisplayMetrics displayMetrics;

    @Before
    public void setUp() throws Exception {

        displayMetrics = new DisplayMetrics();
        displayMetrics.xdpi = XDPI;
        displayMetrics.ydpi = YDPI;

        InkRecognizerClientBuilder inkRecognizerClientBuilder = new InkRecognizerClientBuilder();
        inkRecognizerAsyncClient = inkRecognizerClientBuilder.credentials(credential)
//                .endpoint(testEndpoint)
                .endpoint(endpoint)
                .retryCount(retryCount)
                .retryTimeout(retryTimeout)
                .applicationKind(applicationKind)
                .language(language)
                .unit(unit)
                .unitMultiple(unitMultiple)
                .serviceVersion(serviceVersion)
                .displayMetrics(displayMetrics)
                .buildAsyncClient();

    }

    @Test
    public void recognizeInkWithSimpleStrokesTest() throws Exception {

        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(TestUtils.TWO_STROKES_REQUEST_FILE);
        Mono<Response<InkRecognitionRoot>> response = inkRecognizerAsyncClient.recognizeInk(strokes);
        response.subscribe((r) -> {
            Assert.assertTrue(r.body.length() > 0);
            Assert.assertEquals(r.status, 200);
            Assert.assertNotEquals(r.root, null);
        });
        response.block();

    }

    @Test
    public void recognizeInkWithManyStrokesTest() throws Exception {

        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(ALL_INK_RECOGNITION_UNIT_KINDS_REQUEST_FILE);
        Mono<Response<InkRecognitionRoot>> response = inkRecognizerAsyncClient.recognizeInk(strokes);
        response.doOnSuccess((r) -> {
            Assert.assertTrue(r.body.length() > 0);
            Assert.assertEquals(r.status, 200);
            Assert.assertNotEquals(r.root, null);
        })
                .block();

    }

    @Test
    public void recognizeInkWithSimpleStrokesAndLanguageTest() throws Exception {

        String customLanguage = "hi-IN";
        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(TestUtils.TWO_STROKES_REQUEST_FILE);
        Mono<Response<InkRecognitionRoot>> response = inkRecognizerAsyncClient.recognizeInk(strokes, customLanguage);
        response.subscribe((r) -> {
            Assert.assertTrue(r.body.length() > 0);
            Assert.assertEquals(r.status, 200);
            Assert.assertNotEquals(r.root, null);
        });
        response.block();

    }

    @Test
    public void recognizeInkCustomTest() throws Exception {

        String customLanguage = "hi-IN";
        InkPointUnit customUnit = InkPointUnit.INCH;
        float customMultiple = 2.0f;
        ApplicationKind customApplicationKind = ApplicationKind.WRITING;

        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(TestUtils.TWO_STROKES_REQUEST_FILE);
        Mono<Response<InkRecognitionRoot>> response = inkRecognizerAsyncClient.recognizeInk(
                strokes,
                customUnit,
                customMultiple,
                customApplicationKind,
                customLanguage,
                displayMetrics);
        response.doOnSuccess((r) -> {
            Assert.assertTrue(r.body.length() > 0);
            Assert.assertEquals(r.status, 200);
            Assert.assertNotEquals(r.root, null);
        });
        response.block();

    }

    @Test
    public void multiThreads() throws Exception {

        List<Thread> threads = new ArrayList<>();
        int THREAD_COUNT = 30;
        for(int i=0; i<THREAD_COUNT; i++){
            threads.add(new Thread("" + i){
                public void run() {
                    try {
                        int fileIndex = new Random().nextInt(TestUtils.FILES.length);
                        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(TestUtils.FILES[fileIndex]);
                        Mono<Response<InkRecognitionRoot>> response = inkRecognizerAsyncClient.recognizeInk(strokes);
                        response.subscribe((r) -> {
                            Assert.assertTrue(r.body.length() > 0);
                            Assert.assertEquals(r.status, 200);
                            Assert.assertNotEquals(r.root, null);
                        });
                        response.block();
                    } catch(Exception e) {
                        Thread t = Thread.currentThread();
                        t.getUncaughtExceptionHandler().uncaughtException(t, e);
                    }
                }
            });
        }

        for(int i=0; i<THREAD_COUNT; i++){
            threads.get(i).start();
        }

        for(int i=0; i<THREAD_COUNT; i++){
            threads.get(i).join();
        }

    }

    @Test(expected = Exception.class)
    public void badCredential() throws Exception {

        InkRecognizerClientBuilder inkRecognizerClientBuilder = new InkRecognizerClientBuilder();
        InkRecognizerAsyncClient inkRecognizerAsyncClient = inkRecognizerClientBuilder
                .credentials(new InkRecognizerCredentials(""))
//                .endpoint(testEndpoint)
                .endpoint(endpoint)
                .retryCount(retryCount)
                .retryTimeout(retryTimeout)
                .applicationKind(applicationKind)
                .language(language)
                .unit(unit)
                .unitMultiple(unitMultiple)
                .serviceVersion(serviceVersion)
                .displayMetrics(displayMetrics)
                .buildAsyncClient();

        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(TestUtils.TWO_STROKES_REQUEST_FILE);
        Mono<Response<InkRecognitionRoot>> response = inkRecognizerAsyncClient.recognizeInk(strokes);
        response.block();

    }

    @Test(expected = Exception.class)
    public void badEndpoint() throws Exception {

        InkRecognizerClientBuilder inkRecognizerClientBuilder = new InkRecognizerClientBuilder();
        InkRecognizerAsyncClient inkRecognizerAsyncClient = inkRecognizerClientBuilder
                .credentials(new InkRecognizerCredentials(""))
//                .endpoint(testEndpoint)
                .endpoint(endpoint)
                .retryCount(retryCount)
                .retryTimeout(retryTimeout)
                .applicationKind(applicationKind)
                .language(language)
                .unit(unit)
                .unitMultiple(unitMultiple)
                .serviceVersion(serviceVersion)
                .displayMetrics(displayMetrics)
                .buildAsyncClient();

        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(TestUtils.TWO_STROKES_REQUEST_FILE);
        Mono<Response<InkRecognitionRoot>> response = inkRecognizerAsyncClient.recognizeInk(strokes);
        response.block();

    }

    @After
    public void tearDown() {

        inkRecognizerAsyncClient = null;
        credential = null;
        endpoint = null;
        applicationKind = null;
        language = null;
        unit = null;
        serviceVersion = null;
        displayMetrics = null;

    }

}

