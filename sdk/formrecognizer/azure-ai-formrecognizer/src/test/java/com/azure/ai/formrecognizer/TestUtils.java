// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelField;
import com.azure.ai.formrecognizer.models.CustomFormSubModel;
import com.azure.ai.formrecognizer.models.DimensionUnit;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.ModelTrainingStatus;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.models.TrainingDocumentInfo;
import com.azure.ai.formrecognizer.models.TrainingStatus;
import com.azure.ai.formrecognizer.models.USReceipt;
import com.azure.core.implementation.serializer.jsonwrapper.api.JsonApi;
import com.azure.core.util.IterableStream;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.azure.ai.formrecognizer.TestJsonUtil.getPagedLines;
import static com.azure.ai.formrecognizer.TestJsonUtil.getPagedTables;
import static com.azure.ai.formrecognizer.TestJsonUtil.getRawExpectedForms;
import static com.azure.ai.formrecognizer.TestJsonUtil.getRawExpectedReceipt;

/**
 * Contains helper methods for generating inputs for test methods
 */
final class TestUtils {

    static final String VALID_MODEL_ID = "95537f1b-aac4-4da8-8292-f1b93ac4c8f8";
    static final String SUPERVISED_MODEL_ID = "a0a3998a-b3c0-4075-aa6b-c4c4affe66b7";
    static final String INVALID_MODEL_ID = "a0a3998a-4c4affe66b7";
    static final String INVALID_STATUS_MODEL_ID = "22138c4e-c4b0-4901-a0e1-6c5beb73fc1d";
    static final String INVALID_STATUS_ERROR = "Invalid status Model Id.";
    // TODO (savaity): Do not hardcode, generate SAS URL
    static final String VALID_SUPERVISED_SAS_URL = "";
    static final String VALID_UNSUPERVISED_SAS_URL = "";
    static final Object INVALID_MODEL_ID_ERROR = "Invalid UUID string: " + INVALID_MODEL_ID;
    static final Object SOURCE_URL_ERROR = "'fileSourceUrl' cannot be null.";
    static final String INVALID_KEY = "invalid key";
    static final String INVALID_URL = "htttttttps://localhost:8080";
    static final String VALID_HTTPS_LOCALHOST = "https://localhost:8080";
    static final String RECEIPT_LOCAL_URL = "src/test/resources/sample-files/contoso-allinone.jpg";
    static final String LAYOUT_LOCAL_URL = "src/test/resources/sample-files/layout1.jpg";
    static final String FORM_LOCAL_URL = "src/test/resources/sample-files/Invoice_6.pdf";

    static final Long FILE_LENGTH = new File(RECEIPT_LOCAL_URL).length();
    // Receipts
    static final String RECEIPT_URL = "https://raw.githubusercontent.com/Azure-Samples/"
        + "cognitive-services-REST-api-samples/master/curl/form-recognizer/contoso-allinone.jpg";
    static final String INVALID_RECEIPT_URL = "https://invalid.blob.core.windows.net/fr/contoso-allinone.jpg";
    JsonApi jsonApi;

    private TestUtils() {
    }

    static IterableStream<FormPage> getExpectedFormPages() {
        FormPage formPage = new FormPage(3000, 1.2666f, DimensionUnit.PIXEL, 1688,
            getPagedLines().get(0), getPagedTables().get(0));
        return new IterableStream<>(Arrays.asList(formPage));
    }

    static IterableStream<RecognizedReceipt> getExpectedReceipts(boolean includeTextDetails) {
        return getRawExpectedReceipt(includeTextDetails);
    }

    static USReceipt getExpectedUSReceipt() {
        USReceipt usReceipt = null;
        for (RecognizedReceipt recognizedReceipt : getRawExpectedReceipt(true)) {
            usReceipt = ReceiptExtensions.asUSReceipt(recognizedReceipt);
        }
        return usReceipt;
    }

    static IterableStream<RecognizedForm> getExpectedRecognizedForms(boolean includeTextDetails) {
        return new IterableStream<RecognizedForm>(getRawExpectedForms(includeTextDetails));
    }

    static List<TrainingDocumentInfo> getTrainingDocuments() {
        TrainingDocumentInfo trainingDocumentInfo1 = new TrainingDocumentInfo("Invoice_1.pdf", TrainingStatus.SUCCEEDED, 1, Collections.emptyList());
        TrainingDocumentInfo trainingDocumentInfo2 = new TrainingDocumentInfo("Invoice_2.pdf", TrainingStatus.SUCCEEDED, 1, Collections.emptyList());
        TrainingDocumentInfo trainingDocumentInfo3 = new TrainingDocumentInfo("Invoice_3.pdf", TrainingStatus.SUCCEEDED, 1, Collections.emptyList());
        TrainingDocumentInfo trainingDocumentInfo4 = new TrainingDocumentInfo("Invoice_4.pdf", TrainingStatus.SUCCEEDED, 1, Collections.emptyList());
        TrainingDocumentInfo trainingDocumentInfo5 = new TrainingDocumentInfo("Invoice_5.pdf", TrainingStatus.SUCCEEDED, 1, Collections.emptyList());
        return new ArrayList<>(Arrays.asList(trainingDocumentInfo1, trainingDocumentInfo2, trainingDocumentInfo3, trainingDocumentInfo4, trainingDocumentInfo5));
    }

    static CustomFormModel getExpectedUnsupervisedModel() {
        Map<String, CustomFormModelField> fieldMap = new HashMap<>() {
            {
                put("field-0", new CustomFormModelField("field-0", "Address", null));
                put("field-1", new CustomFormModelField("field-1", "Charges", null));
                put("field-2", new CustomFormModelField("field-2", "Invoice Date", null));
                put("field-3", new CustomFormModelField("field-3", "Invoice Due Date", null));
                put("field-4", new CustomFormModelField("field-4", "Invoice For:", null));
                put("field-5", new CustomFormModelField("field-5", "Invoice Number", null));
                put("field-6", new CustomFormModelField("field-6", "Microsoft", null));
                put("field-7", new CustomFormModelField("field-7", "Page", null));
                put("field-8", new CustomFormModelField("field-8", "VAT ID", null));
            }
        };
        CustomFormSubModel customFormSubModel = new CustomFormSubModel(null, fieldMap, "form-0");
        return new CustomFormModel(VALID_MODEL_ID, ModelTrainingStatus.READY,
            OffsetDateTime.parse("2020-04-09T21:30:28Z"),
            OffsetDateTime.parse("2020-04-09T18:24:56Z"),
            new IterableStream<>(Collections.singletonList(customFormSubModel)),
            Collections.emptyList(), getTrainingDocuments());
    }

    static CustomFormModel getExpectedSupervisedModel() {
        Map<String, CustomFormModelField> fieldMap = new HashMap<>() {
            {
                put("InvoiceCharges", new CustomFormModelField(null, "InvoiceCharges", 1.0f));
                put("InvoiceDate", new CustomFormModelField(null, "InvoiceDate", 0.8f));
                put("InvoiceDueDate", new CustomFormModelField(null, "InvoiceDueDate", 0.8f));
                put("InvoiceNumber", new CustomFormModelField(null, "InvoiceNumber", 1.0f));
                put("InvoiceVatId", new CustomFormModelField(null, "InvoiceVatId", 1.0f));
            }
        };
        CustomFormSubModel customFormSubModel = new CustomFormSubModel(0.92f, fieldMap, "form-" + SUPERVISED_MODEL_ID);
        return new CustomFormModel(SUPERVISED_MODEL_ID, ModelTrainingStatus.READY,
            OffsetDateTime.parse("2020-04-09T18:24:49Z"),
            OffsetDateTime.parse("2020-04-09T18:24:56Z"),
            new IterableStream<>(Collections.singletonList(customFormSubModel)),
            Collections.emptyList(), getTrainingDocuments());
    }

    static AccountProperties getExpectedAccountProperties() {
        return new AccountProperties(14, 5000);
    }

    static InputStream getFileData(String localFileUrl) {
        try {
            return new FileInputStream(localFileUrl);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Local Receipt file not found.", e);
        }
    }

    static Flux<ByteBuffer> getFileBufferData(InputStream data) {
        return Utility.convertStreamToByteBuffer(data);
    }
}

