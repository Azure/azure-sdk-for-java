// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeOperationResult;
import com.azure.ai.formrecognizer.implementation.models.PageResult;
import com.azure.ai.formrecognizer.implementation.models.ReadResult;
import com.azure.ai.formrecognizer.models.AccountProperties;
import com.azure.ai.formrecognizer.models.CustomFormModel;
import com.azure.ai.formrecognizer.models.CustomFormModelField;
import com.azure.ai.formrecognizer.models.CustomFormModelStatus;
import com.azure.ai.formrecognizer.models.CustomFormSubModel;
import com.azure.ai.formrecognizer.models.DimensionUnit;
import com.azure.ai.formrecognizer.models.FormLine;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormTable;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.models.TrainingDocumentInfo;
import com.azure.ai.formrecognizer.models.TrainingStatus;
import com.azure.ai.formrecognizer.models.USReceipt;
import com.azure.core.util.IterableStream;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.azure.ai.formrecognizer.Transforms.toReceipt;
import static com.azure.ai.formrecognizer.Transforms.toRecognizedForm;

/**
 * Contains helper methods for generating inputs for test methods
 */
final class TestUtils {
    static final String INVALID_MODEL_ID = "a0a3998a-4c4affe66b7";
    static final String INVALID_RECEIPT_URL = "https://invalid.blob.core.windows.net/fr/contoso-allinone.jpg";
    static final String INVALID_SOURCE_URL_ERROR = "Status code 400, \"{\"error\":{\"code\":\"1003\","
        + "\"message\":\"Parameter 'Source' is not a valid Uri.\"}}\"";
    static final String INVALID_MODEL_ID_ERROR = "Invalid UUID string: " + INVALID_MODEL_ID;
    static final String NULL_SOURCE_URL_ERROR = "'fileSourceUrl' cannot be null.";
    static final String INVALID_URL = "htttttttps://localhost:8080";
    static final String VALID_HTTPS_LOCALHOST = "https://localhost:8080";
    static final String RECEIPT_LOCAL_URL = "src/test/resources/sample_files/Test/contoso-allinone.jpg";
    static final String LAYOUT_LOCAL_URL = "src/test/resources/sample_files/Test/layout1.jpg";
    static final String FORM_LOCAL_URL = "src/test/resources/sample_files/Test/Invoice_6.pdf";
    static final long RECEIPT_FILE_LENGTH = new File(RECEIPT_LOCAL_URL).length();
    static final long LAYOUT_FILE_LENGTH = new File(LAYOUT_LOCAL_URL).length();
    static final long CUSTOM_FORM_FILE_LENGTH = new File(FORM_LOCAL_URL).length();

    static final String RECEIPT_URL = "https://raw.githubusercontent"
        + ".com/Azure/azure-sdk-for-java/master/sdk/formrecognizer/azure-ai-formrecognizer/src/test/resources"
        + "/sample_files/Test/contoso-allinone.jpg";
    static final String LAYOUT_URL = "https://raw.githubusercontent"
        + ".com/Azure/azure-sdk-for-java/master/sdk/formrecognizer/azure-ai-formrecognizer/src/test/resources"
        + "/sample_files/Test/layout1.jpg";

    private static final String CUSTOM_FORM_LABELED_DATA = "src/test/resources/sample_files/Content"
        + "/customFormLabeledContent.json";
    private static final String CUSTOM_FORM_DATA = "src/test/resources/sample_files/Content/customFormContent.json";
    private static final String RECEIPT_FORM_DATA = "src/test/resources/sample_files/Content/receiptContent.json";
    private static final String LAYOUT_FORM_DATA = "src/test/resources/sample_files/Content/layoutContent.json";


    private TestUtils() {
    }

    static AnalyzeOperationResult getRawResponse(String filePath) {
        String content;
        try {
            content = new String(Files.readAllBytes(Paths.get(filePath)));
            return getSerializerAdapter().deserialize(content, AnalyzeOperationResult.class, SerializerEncoding.JSON);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    static List<List<FormTable>> getPagedTables() {
        List<PageResult> pageResults = getRawResponse(LAYOUT_FORM_DATA).getAnalyzeResult().getPageResults();
        List<ReadResult> readResults = getRawResponse(LAYOUT_FORM_DATA).getAnalyzeResult().getReadResults();
        return IntStream.range(0, pageResults.size())
            .mapToObj(i -> Transforms.getPageTables(pageResults.get(i), readResults, i + 1))
            .collect(Collectors.toList());
    }

    static List<List<FormLine>> getPagedLines() {
        List<ReadResult> readResults = getRawResponse(LAYOUT_FORM_DATA).getAnalyzeResult().getReadResults();
        return readResults.stream().map(Transforms::getReadResultFormLines).collect(Collectors.toList());
    }

    static IterableStream<RecognizedReceipt> getRawExpectedReceipt(boolean includeTextDetails) {
        return toReceipt(getRawResponse(RECEIPT_FORM_DATA).getAnalyzeResult(), includeTextDetails);
    }

    static IterableStream<FormPage> getExpectedFormPages() {
        FormPage formPage = new FormPage(2200, 0, DimensionUnit.PIXEL, 1700,
            new IterableStream<FormLine>(getPagedLines().get(0)),
            new IterableStream<FormTable>(getPagedTables().get(0)));
        return new IterableStream<>(Collections.singletonList(formPage));
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

    static IterableStream<RecognizedForm> getExpectedRecognizedForms() {
        return new IterableStream<RecognizedForm>(toRecognizedForm(getRawResponse(CUSTOM_FORM_DATA).getAnalyzeResult(), false));
    }

    static IterableStream<RecognizedForm> getExpectedRecognizedLabeledForms() {
        return new IterableStream<RecognizedForm>(
            toRecognizedForm(getRawResponse(CUSTOM_FORM_LABELED_DATA).getAnalyzeResult(), true));
    }

    static List<TrainingDocumentInfo> getExpectedTrainingDocuments() {
        TrainingDocumentInfo trainingDocumentInfo1 = new TrainingDocumentInfo("Invoice_1.pdf",
            TrainingStatus.SUCCEEDED, 1, Collections.emptyList());
        TrainingDocumentInfo trainingDocumentInfo2 = new TrainingDocumentInfo("Invoice_2.pdf",
            TrainingStatus.SUCCEEDED, 1, Collections.emptyList());
        TrainingDocumentInfo trainingDocumentInfo3 = new TrainingDocumentInfo("Invoice_3.pdf",
            TrainingStatus.SUCCEEDED, 1, Collections.emptyList());
        TrainingDocumentInfo trainingDocumentInfo4 = new TrainingDocumentInfo("Invoice_4.pdf",
            TrainingStatus.SUCCEEDED, 1, Collections.emptyList());
        TrainingDocumentInfo trainingDocumentInfo5 = new TrainingDocumentInfo("Invoice_5.pdf",
            TrainingStatus.SUCCEEDED, 1, Collections.emptyList());
        return Arrays.asList(trainingDocumentInfo1, trainingDocumentInfo2,
            trainingDocumentInfo3, trainingDocumentInfo4, trainingDocumentInfo5);
    }

    static CustomFormModel getExpectedUnlabeledModel() {
        Map<String, CustomFormModelField> fieldMap = new HashMap<String, CustomFormModelField>() {
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
        return new CustomFormModel("95537f1b-aac4-4da8-8292-f1b93ac4c8f8", CustomFormModelStatus.READY,
            OffsetDateTime.parse("2020-04-09T21:30:28Z"),
            OffsetDateTime.parse("2020-04-09T18:24:56Z"),
            new IterableStream<>(Collections.singletonList(customFormSubModel)),
            Collections.emptyList(), getExpectedTrainingDocuments());
    }

    static CustomFormModel getExpectedLabeledModel() {
        Map<String, CustomFormModelField> fieldMap = new HashMap<String, CustomFormModelField>() {
            {
                put("InvoiceCharges", new CustomFormModelField(null, "InvoiceCharges", 1.0f));
                put("InvoiceDate", new CustomFormModelField(null, "InvoiceDate", 0.8f));
                put("InvoiceDueDate", new CustomFormModelField(null, "InvoiceDueDate", 0.8f));
                put("InvoiceNumber", new CustomFormModelField(null, "InvoiceNumber", 1.0f));
                put("InvoiceVatId", new CustomFormModelField(null, "InvoiceVatId", 1.0f));
            }
        };
        CustomFormSubModel customFormSubModel = new CustomFormSubModel(0.92f, fieldMap, "form-" + "{labeled_model_Id}");
        return new CustomFormModel("{labeled_model_Id}", CustomFormModelStatus.READY,
            OffsetDateTime.parse("2020-04-09T18:24:49Z"),
            OffsetDateTime.parse("2020-04-09T18:24:56Z"),
            new IterableStream<>(Collections.singletonList(customFormSubModel)),
            Collections.emptyList(), getExpectedTrainingDocuments());
    }

    static AccountProperties getExpectedAccountProperties() {
        return new AccountProperties(14, 5000);
    }

    static InputStream getFileData(String localFileUrl) {
        try {
            return new FileInputStream(localFileUrl);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Local file not found.", e);
        }
    }

    static Flux<ByteBuffer> getReplayableBufferData(String localFileUrl) {
        Mono<InputStream> dataMono = Mono.defer(() -> {
            try {
                return Mono.just(new FileInputStream(localFileUrl));
            } catch (FileNotFoundException e) {
                return Mono.error(new RuntimeException("Local file not found.", e));
            }
        });
        return dataMono.flatMapMany(new Function<InputStream, Flux<ByteBuffer>>() {
            @Override
            public Flux<ByteBuffer> apply(InputStream inputStream) {
                return Utility.toFluxByteBuffer(inputStream);
            }
        });
    }

    private static SerializerAdapter getSerializerAdapter() {
        return JacksonAdapter.createDefaultSerializerAdapter();
    }
}

