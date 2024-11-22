// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.training.FormTrainingAsyncClient;
import com.azure.ai.formrecognizer.training.FormTrainingClient;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.ai.formrecognizer.training.models.TrainingOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.test.SyncAsyncExtension;
import com.azure.core.test.annotation.SyncAsyncTest;
import com.azure.core.test.http.MockHttpResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static com.azure.ai.formrecognizer.FormTrainingClientBuilderTest.TEST_FILE;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_MODEL_ID_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.NULL_SOURCE_URL_ERROR;
import static com.azure.ai.formrecognizer.TestUtils.ONE_NANO_DURATION;
import static com.azure.ai.formrecognizer.TestUtils.VALID_HTTPS_LOCALHOST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Class to test client side validations for Form Training clients.
 */
public class FormTrainingClientUnitTest {

    private static FormTrainingClient client;
    private static FormTrainingAsyncClient asyncClient;

    @BeforeAll
    protected static void beforeTest() {
        FormTrainingClientBuilder builder
            = new FormTrainingClientBuilder().httpClient(request -> Mono.just(new MockHttpResponse(request, 200)))
                .endpoint(VALID_HTTPS_LOCALHOST)
                .credential(new AzureKeyCredential("fakeKey"));

        client = builder.buildClient();
        asyncClient = builder.buildAsyncClient();
    }

    /**
     * Verifies that an exception is thrown for invalid model Id.
     */
    @SyncAsyncTest
    public void getCustomModelInvalidModelId() {
        IllegalArgumentException throwable = assertThrows(IllegalArgumentException.class,
            () -> SyncAsyncExtension.execute(() -> client.getCustomModel(INVALID_MODEL_ID),
                () -> asyncClient.getCustomModel(INVALID_MODEL_ID)));
        assertEquals(throwable.getMessage(), INVALID_MODEL_ID_ERROR);
    }

    /**
     * Verifies that an exception is thrown for invalid status model Id.
     */
    @SyncAsyncTest
    public void deleteModelInvalidModelId() {
        IllegalArgumentException throwable = assertThrows(IllegalArgumentException.class, () -> SyncAsyncExtension
            .execute(() -> client.deleteModel(INVALID_MODEL_ID), () -> asyncClient.deleteModel(INVALID_MODEL_ID)));
        assertEquals(throwable.getMessage(), INVALID_MODEL_ID_ERROR);
    }

    /**
     * Verifies that an exception is thrown for null model Id parameter.
     */
    @SyncAsyncTest
    public void getCustomModelNullModelId() {
        assertThrows(IllegalArgumentException.class, () -> SyncAsyncExtension.execute(() -> client.getCustomModel(null),
            () -> asyncClient.getCustomModel(null)));
    }

    /**
     * Verifies that an exception is thrown for null source url input.
     */
    @Test
    public void beginTrainingNullInput() {
        NullPointerException thrown = assertThrows(NullPointerException.class,
            () -> asyncClient.beginTraining(null, false, new TrainingOptions().setPollInterval(ONE_NANO_DURATION))
                .getSyncPoller()
                .getFinalResult());

        assertEquals(NULL_SOURCE_URL_ERROR, thrown.getMessage());
    }

    /**
     * Verifies that an exception is thrown for null source url input.
     */
    @Test
    public void beginTrainingNullInputSync() {
        Exception exception = assertThrows(NullPointerException.class, () -> client.beginTraining(null, false));
        assertEquals(NULL_SOURCE_URL_ERROR, exception.getMessage());
    }

    /**
     * Test for invalid endpoint.
     */
    @Test
    public void trainingClientBuilderInvalidEndpoint() {
        assertThrows(RuntimeException.class,
            () -> client.getFormRecognizerClient().beginRecognizeContentFromUrl(TEST_FILE).getFinalResult());
    }

}
