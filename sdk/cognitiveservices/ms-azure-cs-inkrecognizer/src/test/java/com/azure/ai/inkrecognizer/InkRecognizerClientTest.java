// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.inkrecognizer;

import com.azure.ai.inkrecognizer.model.InkRecognitionRoot;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.azure.ai.inkrecognizer.TestUtils.*;

public class InkRecognizerClientTest {

    private InkRecognizerClient inkRecognizerClient;
    private InkRecognizerCredentials credential = new InkRecognizerCredentials("");
    private String endpoint = "https://api.cognitive.microsoft.com/inkrecognizer";
    private final int retryCount = 3;
    private final int retryTimeout = 300;
    private ApplicationKind applicationKind = ApplicationKind.DRAWING;
    private String language = "en-US";
    private InkPointUnit unit = InkPointUnit.MM;
    private final float unitMultiple = 1.2f;
    private ServiceVersion serviceVersion = ServiceVersion.PREVIEW_1_0_0;

    @Before
    public void setUp() throws Exception {

        InkRecognizerClientBuilder inkRecognizerClientBuilder = new InkRecognizerClientBuilder();
        inkRecognizerClient = inkRecognizerClientBuilder.credentials(credential)
            .endpoint(endpoint)
            .retryCount(retryCount)
            .retryTimeout(retryTimeout)
            .applicationKind(applicationKind)
            .language(language)
            .unit(unit)
            .unitMultiple(unitMultiple)
            .serviceVersion(serviceVersion)
            .buildClient();

    }

    @Test
    public void recognizeInkWithSimpleStrokesTest() throws Exception {

        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(TestUtils.CIRCLE_REQUEST_FILE);
        Response<InkRecognitionRoot> response = inkRecognizerClient.recognizeInk(strokes);
        Assert.assertTrue(response.body.length() > 0);
        Assert.assertEquals(response.status, 200);
        Assert.assertNotEquals(response.root, null);

    }

    @Test
    public void recognizeInkWithManyStrokesTest() throws Exception {

        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(ALL_INK_RECOGNITION_UNIT_KINDS_REQUEST_FILE);
        Response<InkRecognitionRoot> response = inkRecognizerClient.recognizeInk(strokes);
        Assert.assertTrue(response.body.length() > 0);
        Assert.assertEquals(response.status, 200);
        Assert.assertNotEquals(response.root, null);

    }

    @Test
    public void recognizeInkWithSimpleStrokesAndLanguageTest() throws Exception {

        String customLanguage = "hi-IN";
        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(TestUtils.TWO_STROKES_REQUEST_FILE);
        Response<InkRecognitionRoot> response = inkRecognizerClient.recognizeInk(strokes, customLanguage);
        Assert.assertTrue(response.body.length() > 0);
        Assert.assertEquals(response.status, 200);
        Assert.assertNotEquals(response.root, null);

    }

    @Test
    public void recognizeInkCustomTest() throws Exception {

        String customLanguage = "hi-IN";
        InkPointUnit customUnit = InkPointUnit.CM;
        float customMultiple = 2.0f;
        ApplicationKind customApplicationKind = ApplicationKind.WRITING;

        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(TestUtils.TWO_STROKES_REQUEST_FILE);
        Response<InkRecognitionRoot> response = inkRecognizerClient.recognizeInk(
            strokes,
            customUnit,
            customMultiple,
            customApplicationKind,
            customLanguage);
        Assert.assertTrue(response.body.length() > 0);
        Assert.assertEquals(response.status, 200);
        Assert.assertNotEquals(response.root, null);

    }

    @Test
    public void multiThreads() throws Exception {

        List<Thread> threads = new ArrayList<>();
        int THREAD_COUNT = 30;
        for (int i = 0; i < THREAD_COUNT; i++) {
            threads.add(new Thread("" + i) {
                public void run() {
                    try {
                        int fileIndex = new Random().nextInt(TestUtils.FILES.length);
                        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(TestUtils.FILES[fileIndex]);
                        Response<InkRecognitionRoot> response = inkRecognizerClient.recognizeInk(strokes);
                        Assert.assertTrue(response.body.length() > 0);
                        Assert.assertEquals(response.status, 200);
                        Assert.assertNotEquals(response.root, null);
                    } catch (Exception e) {
                        Thread t = Thread.currentThread();
                        t.getUncaughtExceptionHandler().uncaughtException(t, e);
                    }
                }
            });
        }

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads.get(i).start();
        }

        for (int i = 0; i < THREAD_COUNT; i++) {
            threads.get(i).join();
        }

    }

    @Test(expected = Exception.class)
    public void badCredential() throws Exception {

        InkRecognizerClientBuilder inkRecognizerClientBuilder = new InkRecognizerClientBuilder();
        InkRecognizerClient inkRecognizerClient = inkRecognizerClientBuilder
            .credentials(new InkRecognizerCredentials(""))
            .endpoint(endpoint)
            .retryCount(retryCount)
            .retryTimeout(retryTimeout)
            .applicationKind(applicationKind)
            .language(language)
            .unit(unit)
            .unitMultiple(unitMultiple)
            .serviceVersion(serviceVersion)
            .buildClient();

        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(TestUtils.TWO_STROKES_REQUEST_FILE);
        Response<InkRecognitionRoot> response = inkRecognizerClient.recognizeInk(strokes);

    }

    @Test(expected = Exception.class)
    public void badEndpoint() throws Exception {

        InkRecognizerClientBuilder inkRecognizerClientBuilder = new InkRecognizerClientBuilder();
        InkRecognizerClient inkRecognizerClient = inkRecognizerClientBuilder
            .credentials(credential)
            .endpoint("")
            .retryCount(retryCount)
            .retryTimeout(retryTimeout)
            .applicationKind(applicationKind)
            .language(language)
            .unit(unit)
            .unitMultiple(unitMultiple)
            .serviceVersion(serviceVersion)
            .buildClient();

        Iterable<InkStroke> strokes = TestUtils.loadStrokesFromJSON(TestUtils.TWO_STROKES_REQUEST_FILE);
        Response<InkRecognitionRoot> response = inkRecognizerClient.recognizeInk(strokes);

    }

    @After
    public void tearDown() {

        inkRecognizerClient = null;
        credential = null;
        endpoint = null;
        applicationKind = null;
        language = null;
        unit = null;
        serviceVersion = null;

    }

}
