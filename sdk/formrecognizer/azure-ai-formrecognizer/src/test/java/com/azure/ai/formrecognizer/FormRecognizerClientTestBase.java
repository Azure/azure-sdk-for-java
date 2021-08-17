// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.models.FieldBoundingBox;
import com.azure.ai.formrecognizer.models.FieldData;
import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormContentType;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormLine;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormPageRange;
import com.azure.ai.formrecognizer.models.FormSelectionMark;
import com.azure.ai.formrecognizer.models.FormTable;
import com.azure.ai.formrecognizer.models.FormWord;
import com.azure.ai.formrecognizer.models.LengthUnit;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.SelectionMarkState;
import com.azure.ai.formrecognizer.models.TextStyleName;
import com.azure.ai.formrecognizer.training.FormTrainingClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.AZURE_FORM_RECOGNIZER_ENDPOINT;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.FORM_RECOGNIZER_MULTIPAGE_TRAINING_BLOB_CONTAINER_SAS_URL;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.FORM_RECOGNIZER_SELECTION_MARK_BLOB_CONTAINER_SAS_URL;
import static com.azure.ai.formrecognizer.FormTrainingClientTestBase.FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL;
import static com.azure.ai.formrecognizer.TestUtils.FAKE_ENCODED_EMPTY_SPACE_URL;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.TestUtils.ONE_NANO_DURATION;
import static com.azure.ai.formrecognizer.TestUtils.TEST_DATA_PNG;
import static com.azure.ai.formrecognizer.TestUtils.URL_TEST_FILE_FORMAT;
import static com.azure.ai.formrecognizer.implementation.Utility.DEFAULT_POLL_INTERVAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class FormRecognizerClientTestBase extends TestBase {

    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("[^0-9]+");
    private static final String EXPECTED_RECEIPT_ADDRESS_VALUE = "123 Main Street Redmond, WA 98052";
    private static final String EXPECTED_JPEG_RECEIPT_PHONE_NUMBER_VALUE = "+19876543210";
    private static final String ITEMIZED_RECEIPT_VALUE = "Itemized";
    static final String RECEIPT_CONTOSO_JPG = "contoso-allinone.jpg";
    static final String RECEIPT_CONTOSO_PNG = "contoso-receipt.png";
    static final String INVOICE_6_PDF = "Invoice_6.pdf";
    static final String MULTIPAGE_INVOICE_PDF = "multipage_invoice1.pdf";
    static final String MULTIPAGE_RECEIPT_PDF = "multipage-receipt.pdf";
    static final String BUSINESS_CARD_JPG = "businessCard.jpg";
    static final String BUSINESS_CARD_PNG = "businessCard.png";
    static final String MULTIPAGE_BUSINESS_CARD_PDF = "business-card-multipage.pdf";
    static final String INVOICE_PDF = "Invoice_1.pdf";
    static final String INVOICE_NO_SUB_LINE_PDF = "ErrorImage.tiff";
    static final String MULTIPAGE_VENDOR_INVOICE_PDF = "multipage_vendor_invoice.pdf";
    static final String LICENSE_CARD_JPG = "license.jpg";

    // Error code
    static final String BAD_ARGUMENT_CODE = "BadArgument";
    static final String INVALID_IMAGE_ERROR_CODE = "InvalidImage";
    static final String INVALID_MODEL_ID_ERROR_CODE = "1001";
    static final String MODEL_ID_NOT_FOUND_ERROR_CODE = "1022";

    // Error Message
    static final String INVALID_UUID_EXCEPTION_MESSAGE = "Invalid UUID string: ";
    static final String INVALID_SOURCE_URL_EXCEPTION_MESSAGE = "Failed to download the image from the submitted URL. "
        + "The URL may either be invalid or the server hosting the image is experiencing some technical difficulties.";
    static final String MODEL_ID_IS_REQUIRED_EXCEPTION_MESSAGE = "'modelId' is required and cannot be null.";
    static final String COPY_OPERATION_FAILED_STATUS_MESSAGE = "Copy operation failed";

    static final String INVALID_ENDPOINT = "https://notreal.azure.com";
    static final String LOCAL_FILE_PATH = "src/test/resources/sample_files/Test/";
    static final String ENCODED_EMPTY_SPACE = "{\"source\":\"https://fakeuri.com/blank%20space\"}";

    // Business Card fields
    static final List<String> BUSINESS_CARD_FIELDS =
        Arrays.asList("ContactNames", "FirstName", "LastName", "JobTitles", "Departments",
            "Emails", "Websites", "MobilePhones", "OtherPhones", "WorkPhones", "Faxes", "Addresses", "CompanyNames");

    // Receipt fields
    static final List<String> RECEIPT_FIELDS = Arrays.asList("MerchantName", "MerchantPhoneNumber", "MerchantAddress",
        "Total", "Subtotal", "Tax", "TransactionDate", "TransactionDate", "TransactionTime", "Items");

    // Invoice fields
    static final List<String> INVOICE_FIELDS = Arrays.asList("CustomerAddressRecipient", "InvoiceId", "VendorName",
        "VendorAddress", "CustomerAddress", "CustomerName", "InvoiceTotal", "DueDate", "InvoiceDate");

    // Identity Document fields
    static final List<String> ID_DOCUMENT_FIELDS = Arrays.asList("Country", "DateOfBirth", "DateOfExpiration",
        "DocumentNumber", "FirstName", "LastName", "Nationality", "Sex", "MachineReadableZone", "DocumentType",
        "Address", "Region");

    public static final String EXPECTED_MERCHANT_NAME = "Contoso";

    Duration durationTestMode;

    /**
     * Use duration of nearly zero value for PLAYBACK test mode, otherwise, use default duration value for LIVE mode.
     */
    @Override
    protected void beforeTest() {
        if (interceptorManager.isPlaybackMode()) {
            durationTestMode = ONE_NANO_DURATION;
        } else {
            durationTestMode = DEFAULT_POLL_INTERVAL;
        }
    }

    FormRecognizerClientBuilder getFormRecognizerClientBuilder(HttpClient httpClient,
                                                               FormRecognizerServiceVersion serviceVersion) {
        FormRecognizerClientBuilder builder = new FormRecognizerClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy());

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(INVALID_KEY));
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder;
    }

    FormTrainingClientBuilder getFormTrainingClientBuilder(HttpClient httpClient,
                                                           FormRecognizerServiceVersion serviceVersion) {
        FormTrainingClientBuilder builder = new FormTrainingClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy());
        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(INVALID_KEY));
        } else {
            builder.credential(new DefaultAzureCredentialBuilder().build());
        }
        return builder;
    }

    private static void validatePageRangeData(int expectedPageInfo, FormPageRange actualPageInfo) {
        assertEquals(expectedPageInfo, actualPageInfo.getFirstPageNumber());
        assertEquals(expectedPageInfo, actualPageInfo.getLastPageNumber());
    }

    // Receipt recognition

    @Test
    abstract void recognizeReceiptData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptDataNullData(HttpClient httpClient,
                                               FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                   FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptDataIncludeFieldElements(HttpClient httpClient,
                                                           FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptDataWithPngFile(HttpClient httpClient,
                                                  FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptDataWithBlankPdf(HttpClient httpClient,
                                                   FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptFromDataMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    // Receipt - URL

    @Test
    abstract void recognizeReceiptSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptInvalidSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptFromUrlIncludeFieldElements(HttpClient httpClient,
                                                              FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptSourceUrlWithPngFile(HttpClient httpClient,
                                                       FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeReceiptFromUrlMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    // Content recognition

    // Content - non-URL

    @Test
    abstract void recognizeContent(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentResultWithNullData(HttpClient httpClient,
                                                     FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentResultWithContentTypeAutoDetection(HttpClient httpClient,
                                                                     FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentResultWithBlankPdf(HttpClient httpClient,
                                                     FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentFromDataMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentWithSelectionMarks(HttpClient httpClient,
                                                     FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentAppearance(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    // Content - URL

    @Test
    abstract void recognizeContentFromUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentFromUrlWithPdf(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentInvalidSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentFromUrlMultiPage(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeContentWithSelectionMarksFromUrl(HttpClient httpClient,
                                                            FormRecognizerServiceVersion serviceVersion);

    // Custom form recognition

    // Custom form - non-URL - labeled data

    @Test
    abstract void recognizeCustomFormLabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithJpgContentType(HttpClient httpClient,
                                                                   FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithBlankPdfContentType(HttpClient httpClient,
                                                                        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataExcludeFieldElements(HttpClient httpClient,
                                                                     FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithNullFormData(HttpClient httpClient,
                                                                 FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithNullModelId(HttpClient httpClient,
                                                                FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithEmptyModelId(HttpClient httpClient,
                                                                 FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormInvalidStatus(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                             FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormMultiPageLabeled(HttpClient httpClient,
                                                      FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormLabeledDataWithSelectionMark(HttpClient httpClient,
                                                                  FormRecognizerServiceVersion serviceVersion);

    // Custom form - non-URL - unlabeled data
    @Test
    abstract void recognizeCustomFormUnlabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUnlabeledDataIncludeFieldElements(HttpClient httpClient,
                                                                       FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormMultiPageUnlabeled(HttpClient httpClient,
                                                        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUnlabeledDataWithJpgContentType(HttpClient httpClient,
                                                                     FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUnlabeledDataWithBlankPdfContentType(HttpClient httpClient,
                                                                          FormRecognizerServiceVersion serviceVersion);

    // Custom form - URL - unlabeled data

    @Test
    abstract void recognizeCustomFormUrlUnlabeledData(HttpClient httpClient,
                                                      FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUrlUnlabeledDataIncludeFieldElements(HttpClient httpClient,
                                                                          FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUrlMultiPageUnlabeled(HttpClient httpClient,
                                                           FormRecognizerServiceVersion serviceVersion);

    // Custom form - URL - labeled data

    @Test
    abstract void recognizeCustomFormInvalidSourceUrl(HttpClient httpClient,
                                                      FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormFromUrlLabeledDataWithNullModelId(HttpClient httpClient,
                                                                       FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormFromUrlLabeledDataWithEmptyModelId(HttpClient httpClient,
                                                                        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUrlLabeledData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUrlLabeledDataIncludeFieldElements(HttpClient httpClient,
                                                                        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUrlMultiPageLabeled(HttpClient httpClient,
                                                         FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeCustomFormUrlLabeledDataWithSelectionMark(HttpClient httpClient,
                                                                     FormRecognizerServiceVersion serviceVersion);

    // Business Card - data
    @Test
    abstract void recognizeBusinessCardData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardDataNullData(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardDataWithContentTypeAutoDetection(HttpClient httpClient,
                                                                        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardDataIncludeFieldElements(HttpClient httpClient,
                                                                FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardDataWithPngFile(HttpClient httpClient,
                                                       FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardDataWithBlankPdf(HttpClient httpClient,
                                                        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardFromDamagedPdf(HttpClient httpClient,
                                                      FormRecognizerServiceVersion serviceVersion);

    // Business card - URL

    @Test
    abstract void recognizeBusinessCardSourceUrl(HttpClient httpClient, FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardFromUrlWithEncodedBlankSpaceSourceUrl(HttpClient httpClient,
                                                                             FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardInvalidSourceUrl(HttpClient httpClient,
                                                        FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardFromUrlIncludeFieldElements(HttpClient httpClient,
                                                                   FormRecognizerServiceVersion serviceVersion);

    @Test
    abstract void recognizeBusinessCardSourceUrlWithPngFile(HttpClient httpClient,
                                                            FormRecognizerServiceVersion serviceVersion);

    // Others

    void invalidSourceUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(TestUtils.INVALID_RECEIPT_URL);
    }

    void encodedBlankSpaceSourceUrlRunner(Consumer<String> testRunner) {
        testRunner.accept(FAKE_ENCODED_EMPTY_SPACE_URL);
    }

    void urlRunner(Consumer<String> testRunner, String fileName) {
        testRunner.accept(URL_TEST_FILE_FORMAT + fileName);
    }

    void testingContainerUrlRunner(Consumer<String> testRunner, String fileName) {
        testRunner.accept(getStorageTestingFileUrl(fileName));
    }

    void dataRunner(BiConsumer<InputStream, Long> testRunner, String fileName) {
        final long fileLength = new File(LOCAL_FILE_PATH + fileName).length();

        if (interceptorManager.isPlaybackMode()) {
            testRunner.accept(new ByteArrayInputStream(TEST_DATA_PNG.getBytes(StandardCharsets.UTF_8)), fileLength);
        } else {
            try {
                testRunner.accept(new FileInputStream(LOCAL_FILE_PATH + fileName), fileLength);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("Local file not found.", e);
            }
        }
    }

    void localFilePathRunner(BiConsumer<String, Long> testRunner, String fileName) {
        final long fileLength = new File(LOCAL_FILE_PATH + fileName).length();
        testRunner.accept(LOCAL_FILE_PATH + fileName, fileLength);
    }

    void damagedPdfDataRunner(BiConsumer<InputStream, Integer> testRunner) {
        testRunner.accept(new ByteArrayInputStream(new byte[] {0x25, 0x50, 0x44, 0x46, 0x55, 0x55, 0x55}), 7);
    }

    void beginTrainingUnlabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(getTrainingSasUri(), false);
    }

    void beginTrainingLabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(getTrainingSasUri(), true);
    }

    void beginSelectionMarkTrainingLabeledRunner(BiConsumer<String, Boolean> testRunner) {
        testRunner.accept(getSelectionMarkTrainingSasUri(), true);
    }

    void beginTrainingMultipageRunner(Consumer<String> testRunner) {
        testRunner.accept(getMultipageTrainingSasUri());
    }

    void validateBlankCustomForm(List<RecognizedForm> actualForms,
                                 int expectedPageNumber, boolean isLabeled) {
        Assertions.assertEquals(expectedPageNumber, actualForms.size());

        RecognizedForm actualForm = actualForms.get(expectedPageNumber - 1);
        validatePageRangeData(expectedPageNumber, actualForm.getPageRange());
        actualForm.getPages().forEach(actualFormPage -> {
            Assertions.assertEquals(8.5, actualFormPage.getWidth());
            Assertions.assertEquals(11, actualFormPage.getHeight());
            Assertions.assertEquals(LengthUnit.INCH, actualFormPage.getUnit());
            Assertions.assertEquals(0, actualFormPage.getTables().size());
        });

        if (isLabeled) {
            Assertions.assertEquals(15, actualForm.getFields().size());
        } else {
            Assertions.assertEquals(0, actualForm.getFields().size());
        }
    }

    void validateJpegCustomForm(List<RecognizedForm> actualForms, boolean includeFieldElements,
                                int expectedPageNumber, boolean isLabeled) {
        Assertions.assertEquals(expectedPageNumber, actualForms.size());

        RecognizedForm actualForm = actualForms.get(expectedPageNumber - 1);
        validatePageRangeData(expectedPageNumber, actualForm.getPageRange());
        actualForm.getPages().forEach(actualFormPage -> {
            Assertions.assertEquals(1700, actualFormPage.getWidth());
            Assertions.assertEquals(2200, actualFormPage.getHeight());
            Assertions.assertEquals(LengthUnit.PIXEL, actualFormPage.getUnit());

            if (isLabeled) {
                int[][] table = new int[][] {{5, 4, 20}, {4, 2, 8}};
                Assertions.assertEquals(2, actualFormPage.getTables().size());
                for (int i = 0; i < actualFormPage.getTables().size(); i++) {
                    int j = 0;
                    FormTable actualFormTable = actualFormPage.getTables().get(i);
                    Assertions.assertEquals(table[i][j], actualFormTable.getRowCount());
                    Assertions.assertEquals(table[i][++j], actualFormTable.getColumnCount());
                    Assertions.assertEquals(table[i][++j], actualFormTable.getCells().size());

                }
            } else {
                int[] table = new int[] {5, 4, 20};
                Assertions.assertEquals(1, actualFormPage.getTables().size());
                for (int i = 0; i < actualFormPage.getTables().size(); i++) {
                    FormTable actualFormTable = actualFormPage.getTables().get(i);
                    Assertions.assertEquals(table[i], actualFormTable.getRowCount());
                    Assertions.assertEquals(table[++i], actualFormTable.getColumnCount());
                    Assertions.assertEquals(table[++i], actualFormTable.getCells().size());
                }
            }
            validateFormPage(actualFormPage, includeFieldElements);
        });

        if (isLabeled) {
            assertTrue(actualForm.getFormType().startsWith("custom:"));

            actualForm.getFields().forEach((label, actualFormField) -> {
                validateBoundingBoxData(actualFormField.getValueData().getBoundingBox());
                if ("Tax".equals(label)) {
                    assertEquals("$4.00", actualFormField.getValue().asString());
                } else if ("Signature".equals(label)) {
                    assertEquals("Bernie Sanders", actualFormField.getValue().asString());
                } else if ("Email".equals(label)) {
                    assertEquals("accounts@herolimited.com", actualFormField.getValue().asString());
                } else if ("PhoneNumber".equals(label)) {
                    assertEquals("555-348-6512", actualFormField.getValue().asString());
                } else if ("Quantity".equals(label)) {
                    assertEquals(20.0f, actualFormField.getValue().asFloat());
                } else if ("CompanyPhoneNumber".equals(label)) {
                    assertEquals("938-294-2949", actualFormField.getValue().asString());
                } else if ("DatedAs".equals(label)) {
                    assertEquals("12/20/2020", actualFormField.getValue().asString());
                } else if ("Total".equals(label)) {
                    assertEquals("$144.00", actualFormField.getValue().asString());
                } else if ("CompanyName".equals(label)) {
                    assertEquals("Higgly Wiggly Books", actualFormField.getValue().asString());
                } else if ("VendorName".equals(label)) {
                    assertEquals("Hillary Swank", actualFormField.getValue().asString());
                } else if ("Website".equals(label)) {
                    assertEquals("www.herolimited.com", actualFormField.getValue().asString());
                } else if ("Merchant".equals(label)) {
                    assertEquals("Hero Limited", actualFormField.getValue().asString());
                } else if ("PurchaseOrderNumber".equals(label)) {
                    assertEquals("948284", actualFormField.getValue().asString());
                } else if ("CompanyAddress".equals(label)) {
                    assertEquals("938 NE Burner Road Boulder City, CO 92848",
                        actualFormField.getValue().asString());
                } else if ("Subtotal".equals(label)) {
                    assertEquals("$140.00", actualFormField.getValue().asString());
                } else {
                    throw new IllegalStateException("Unexpected value: " + label);
                }
            });
        } else {
            Assertions.assertEquals(17, actualForm.getFields().size());
            // validating the field name is of type field-0 and verify some label and valueData
            actualForm.getFields().forEach((key, formField) -> {
                validateBoundingBoxData(formField.getValueData().getBoundingBox());

                if ("field-0".equals(key)) {
                    assertEquals("Company Phone:", formField.getLabelData().getText());
                    assertEquals("555-348-6512", formField.getValue().asString());
                } else if ("field-1".equals(key)) {
                    assertEquals("Website:", formField.getLabelData().getText());
                    assertEquals("www.herolimited.com", formField.getValue().asString());
                } else if ("field-2".equals(key)) {
                    assertEquals("Email:", formField.getLabelData().getText());
                    assertEquals("accounts@herolimited.com", formField.getValue().asString());
                } else if ("field-3".equals(key)) {
                    assertEquals("Dated As:", formField.getLabelData().getText());
                    assertEquals("12/20/2020", formField.getValue().asString());
                } else if ("field-4".equals(key)) {
                    assertEquals("Purchase Order #:", formField.getLabelData().getText());
                    assertEquals("948284", formField.getValue().asString());
                } else if ("field-5".equals(key)) {
                    assertEquals("Vendor Name:", formField.getLabelData().getText());
                    assertEquals("Hillary Swank", formField.getValue().asString());
                } else if ("field-6".equals(key)) {
                    assertEquals("Company Name:", formField.getLabelData().getText());
                    assertEquals("Higgly Wiggly Books", formField.getValue().asString());
                }
            });
        }
    }

    void validateCustomFormWithSelectionMarks(List<RecognizedForm> actualForms, boolean includeFieldElements,
                                              int expectedPageNumber) {
        Assertions.assertEquals(expectedPageNumber, actualForms.size());

        RecognizedForm actualForm = actualForms.get(expectedPageNumber - 1);
        validatePageRangeData(expectedPageNumber, actualForm.getPageRange());
        assertTrue(actualForm.getFormType().startsWith("custom:"));

        actualForm.getPages().forEach(actualFormPage -> {
            Assertions.assertEquals(8.5, actualFormPage.getWidth());
            Assertions.assertEquals(11, actualFormPage.getHeight());
            Assertions.assertEquals(LengthUnit.INCH, actualFormPage.getUnit());

            Assertions.assertEquals(0, actualFormPage.getTables().size());
            validateFormPage(actualFormPage, includeFieldElements);
        });

        actualForm.getFields().forEach((label, actualFormField) -> {
            validateBoundingBoxData(actualFormField.getValueData().getBoundingBox());
            if ("AMEX_SELECTION_MARK".equals(label)) {
                assertEquals(SelectionMarkState.SELECTED, actualFormField.getValue().asSelectionMarkState());
            } else if ("VISA_SELECTION_MARK".equals(label)) {
                assertEquals(SelectionMarkState.UNSELECTED, actualFormField.getValue().asSelectionMarkState());
            } else if ("MASTERCARD_SELECTION_MARK".equals(label)) {
                assertEquals(SelectionMarkState.UNSELECTED, actualFormField.getValue().asSelectionMarkState());
            } else {
                throw new IllegalStateException("Unexpected value: " + label);
            }
        });
    }

    void validateUnlabeledCustomForm(List<RecognizedForm> actualForms, boolean includeFieldElements,
                                     int expectedPageNumber) {
        Assertions.assertEquals(expectedPageNumber, actualForms.size());

        RecognizedForm actualForm = actualForms.get(expectedPageNumber - 1);
        validatePageRangeData(expectedPageNumber, actualForm.getPageRange());
        assertTrue(actualForm.getFormType().startsWith("form-"));

        actualForm.getPages().forEach(actualFormPage -> {
            Assertions.assertEquals(8.5, actualFormPage.getWidth());
            Assertions.assertEquals(11, actualFormPage.getHeight());
            Assertions.assertEquals(LengthUnit.INCH, actualFormPage.getUnit());

            int[] table = new int[] {2, 5, 10};
            Assertions.assertEquals(1, actualFormPage.getTables().size());
            for (int i = 0; i < actualFormPage.getTables().size(); i++) {
                FormTable actualFormTable = actualFormPage.getTables().get(i);
                Assertions.assertEquals(table[i], actualFormTable.getRowCount());
                Assertions.assertEquals(table[++i], actualFormTable.getColumnCount());
                Assertions.assertEquals(table[++i], actualFormTable.getCells().size());
            }
            validateFormPage(actualFormPage, includeFieldElements);
        });

        actualForm.getFields().forEach((key, formField) -> {

            validateBoundingBoxData(formField.getValueData().getBoundingBox());

            if ("field-0".equals(key)) {
                assertEquals("Address:", formField.getLabelData().getText());
                assertEquals("14564 Main St. Saratoga, CA 94588", formField.getValue().asString());
            } else if ("field-1".equals(key)) {
                assertEquals("Invoice For:", formField.getLabelData().getText());
                assertEquals("First Up Consultants 1234 King St Redmond, WA 97624", formField.getValue().asString());
            } else if ("field-2".equals(key)) {
                assertEquals("Invoice Number", formField.getLabelData().getText());
                assertEquals("7689302", formField.getValue().asString());
            } else if ("field-3".equals(key)) {
                assertEquals("Invoice Date", formField.getLabelData().getText());
                assertEquals("3/09/2015", formField.getValue().asString());
            } else if ("field-4".equals(key)) {
                assertEquals("Invoice Due Date", formField.getLabelData().getText());
                assertEquals("6/29/2016", formField.getValue().asString());
            } else if ("field-5".equals(key)) {
                assertEquals("Charges", formField.getLabelData().getText());
                assertEquals("$22,123.24", formField.getValue().asString());
            } else if ("field-6".equals(key)) {
                assertEquals("VAT ID", formField.getLabelData().getText());
                assertEquals("QR", formField.getValue().asString());
            } else {
                throw new IllegalStateException("Unexpected value: " + key);
            }
        });
    }

    void validateBlankPdfData(List<RecognizedForm> actualRecognizedForm) {
        assertEquals(1, actualRecognizedForm.size());
        final RecognizedForm actualForm = actualRecognizedForm.get(0);
        assertTrue(actualForm.getFields().isEmpty());
    }

    void validateContentData(List<FormPage> actualFormPages, boolean includeFieldElements) {
        actualFormPages.forEach(formPage -> {
            Assertions.assertTrue(
                formPage.getTextAngle() > -180.0 && formPage.getTextAngle() < 180.0);
            validateFormPage(formPage, includeFieldElements);
        });
    }

    void validateFormPage(FormPage formPage, boolean includeFieldElements) {

        Assertions.assertNotNull(formPage.getLines());

        // content recognition will always return lines.
        if (!includeFieldElements) {
            Assertions.assertEquals(0, formPage.getLines().size());
        }

        formPage.getLines().forEach(formLine -> {
            validateBoundingBoxData(formLine.getBoundingBox());
            Assertions.assertNotNull(formLine.getText());

            if (formLine.getAppearance() != null) {
                Assertions.assertNotNull(formLine.getAppearance().getStyleName());
                Assertions.assertTrue(formLine.getAppearance().getStyleName() == TextStyleName.HANDWRITING
                    || formLine.getAppearance().getStyleName() == TextStyleName.OTHER);
            }

            Assertions.assertNotNull(formLine.getWords());
            formLine.getWords().forEach(formWord -> {
                Assertions.assertNotNull(formWord.getBoundingBox().getPoints());
                Assertions.assertEquals(4, formWord.getBoundingBox().getPoints().size());
                Assertions.assertNotNull(formWord.getText());

            });
        });

        Assertions.assertNotNull(formPage.getTables());

        formPage.getTables().forEach(formTable -> {
            validateBoundingBoxData(formTable.getBoundingBox());
            Assertions.assertNotNull(formTable.getCells());

            formTable.getCells().forEach(formTableCell -> {
                validateBoundingBoxData(formTableCell.getBoundingBox());

                Assertions.assertNotNull(formTableCell.getText());
                Assertions.assertNotNull(formTableCell.getFieldElements());

                // content recognition will always return lines.
                if (!includeFieldElements) {
                    Assertions.assertEquals(0, formTableCell.getFieldElements().size());
                }

                formTableCell.getFieldElements().forEach(formElement -> {
                    validateBoundingBoxData(formElement.getBoundingBox());

                    Assertions.assertTrue(formElement instanceof FormWord
                        || formElement instanceof FormLine || formElement instanceof FormSelectionMark);

                    if (formElement instanceof FormWord || formElement instanceof FormLine) {
                        Assertions.assertNotNull(formElement.getText());
                    } else {
                        // formElement instanceof FormSelectionMark then
                        Assertions.assertNull(formElement.getText());
                    }
                });
            });
        });

        Assertions.assertNotNull(formPage.getSelectionMarks());

        formPage.getSelectionMarks().forEach(formSelectionMark -> {
            validateBoundingBoxData(formSelectionMark.getBoundingBox());
            Assertions.assertNull(formSelectionMark.getText());
            Assertions.assertNotNull(formSelectionMark.getState());
        });
    }

    void validateReceiptData(List<RecognizedForm> actualPrebuiltRecognizedForms, boolean includeFieldElements,
                             FormContentType imageType) {
        for (final RecognizedForm actualForm : actualPrebuiltRecognizedForms) {
            Assertions.assertEquals("prebuilt:receipt", actualForm.getFormType());
            Assertions.assertNull(actualForm.getModelId());
            actualForm.getPages().forEach(formPage -> validateFormPage(formPage, includeFieldElements));

            RECEIPT_FIELDS.forEach(receiptField -> {
                final Map<String, FormField> actualRecognizedReceiptFields = actualForm.getFields();
                if (actualRecognizedReceiptFields.get(receiptField) != null) {
                    validateFieldValueData(actualRecognizedReceiptFields.get(receiptField), includeFieldElements);
                }
            });
        }
        assertEquals(1, actualPrebuiltRecognizedForms.size());
        RecognizedForm receiptPage1 = actualPrebuiltRecognizedForms.get(0);

        assertEquals(1, receiptPage1.getPageRange().getFirstPageNumber());
        assertEquals(1, receiptPage1.getPageRange().getLastPageNumber());
        assertNotNull(receiptPage1.getPages());

        Map<String, FormField> receiptPage1Fields = receiptPage1.getFields();
        assertEquals(ITEMIZED_RECEIPT_VALUE, receiptPage1Fields.get("ReceiptType").getValue().asString());
        receiptPage1Fields.get("ReceiptType");
        assertEquals(EXPECTED_RECEIPT_ADDRESS_VALUE, receiptPage1Fields.get("MerchantAddress")
            .getValue().asString());
        assertEquals(EXPECTED_MERCHANT_NAME, receiptPage1Fields.get("MerchantName")
            .getValue().asString());

        if (FormContentType.IMAGE_JPEG.equals(imageType)) {
            validateJpegReceiptFields(receiptPage1Fields);
        } else if (FormContentType.IMAGE_PNG.equals(imageType)) {
            validatePngReceiptFields(receiptPage1Fields);
        } else {
            throw new IllegalStateException("Unexpected value: " + imageType);
        }
    }

    void validateBusinessCardData(List<RecognizedForm> actualPrebuiltRecognizedForms, boolean includeFieldElements) {
        for (final RecognizedForm actualForm : actualPrebuiltRecognizedForms) {
            Assertions.assertEquals("prebuilt:businesscard", actualForm.getFormType());
            Assertions.assertNull(actualForm.getModelId());
            actualForm.getPages().forEach(formPage -> validateFormPage(formPage, includeFieldElements));

            BUSINESS_CARD_FIELDS.forEach(businessField -> {
                final Map<String, FormField> actualRecognizedBusinessCardFields = actualForm.getFields();
                if (actualRecognizedBusinessCardFields.get(businessField) != null) {
                    validateFieldValueData(actualRecognizedBusinessCardFields.get(businessField), includeFieldElements);
                }
            });
        }
        assertEquals(1, actualPrebuiltRecognizedForms.size());
        RecognizedForm businessCardPage1 = actualPrebuiltRecognizedForms.get(0);

        assertEquals(1, businessCardPage1.getPageRange().getFirstPageNumber());
        assertEquals(1, businessCardPage1.getPageRange().getLastPageNumber());
        assertNotNull(businessCardPage1.getPages());

        Map<String, FormField> businessCardPage1Fields = businessCardPage1.getFields();
        assertEquals(10, businessCardPage1Fields.size());
        assertEquals("2 Kingdom Street Paddington, London, W2 6BD", businessCardPage1Fields.get("Addresses")
            .getValue().asList().get(0).getValue().asString());
        assertEquals(EXPECTED_MERCHANT_NAME, businessCardPage1Fields.get("CompanyNames")
            .getValue().asList().get(0).getValue().asString());
        assertEquals("Cloud & Al Department", businessCardPage1Fields.get("Departments")
            .getValue().asList().get(0).getValue().asString());
        assertEquals("avery.smith@contoso.com", businessCardPage1Fields.get("Emails")
            .getValue().asList().get(0).getValue().asString());
        assertEquals(FieldValueType.PHONE_NUMBER, businessCardPage1Fields.get("Faxes")
            .getValue().asList().get(0).getValue().getValueType());
        assertEquals("Senior Researcher", businessCardPage1Fields.get("JobTitles")
            .getValue().asList().get(0).getValue().asString());
        assertEquals(FieldValueType.PHONE_NUMBER, businessCardPage1Fields.get("MobilePhones")
            .getValue().asList().get(0).getValue().getValueType());
        assertEquals("https://www.contoso.com/", businessCardPage1Fields.get("Websites")
            .getValue().asList().get(0).getValue().asString());
        assertEquals(FieldValueType.PHONE_NUMBER, businessCardPage1Fields.get("WorkPhones")
            .getValue().asList().get(0).getValue().getValueType());
        Map<String, FormField> contactNamesMap
            = businessCardPage1Fields.get("ContactNames").getValue().asList().get(0).getValue().asMap();
        assertEquals("Avery", contactNamesMap.get("FirstName").getValue().asString());
        assertEquals("Smith", contactNamesMap.get("LastName").getValue().asString());
    }

    void validateInvoiceData(List<RecognizedForm> actualPrebuiltRecognizedForms, boolean includeFieldElements) {
        for (final RecognizedForm actualForm : actualPrebuiltRecognizedForms) {
            Assertions.assertEquals("prebuilt:invoice", actualForm.getFormType());
            Assertions.assertNull(actualForm.getModelId());
            actualForm.getPages().forEach(formPage -> validateFormPage(formPage, includeFieldElements));

            INVOICE_FIELDS.forEach(invoiceField -> {
                final Map<String, FormField> actualRecognizedInvoiceFields = actualForm.getFields();
                if (actualRecognizedInvoiceFields.get(invoiceField) != null) {
                    validateFieldValueData(actualRecognizedInvoiceFields.get(invoiceField), includeFieldElements);
                }
            });
        }
        assertEquals(1, actualPrebuiltRecognizedForms.size());
        RecognizedForm invoicePage1 = actualPrebuiltRecognizedForms.get(0);

        assertEquals(1, invoicePage1.getPageRange().getFirstPageNumber());
        assertEquals(1, invoicePage1.getPageRange().getLastPageNumber());
        assertNotNull(invoicePage1.getPages());

        Map<String, FormField> invoicePage1Fields = invoicePage1.getFields();
        assertEquals(9, invoicePage1Fields.size());
        assertEquals("1020 Enterprise Way Sunnayvale, CA 87659", invoicePage1Fields.get("CustomerAddress")
            .getValue().asString());
        assertEquals("Microsoft", invoicePage1Fields.get("CustomerAddressRecipient")
            .getValue().asString());
        assertEquals("Microsoft", invoicePage1Fields.get("CustomerName")
            .getValue().asString());
        assertEquals(LocalDate.of(2017, 6, 24), invoicePage1Fields.get("DueDate")
            .getValue().asDate());
        assertEquals(LocalDate.of(2017, 6, 18), invoicePage1Fields.get("InvoiceDate")
            .getValue().asDate());
        assertEquals("34278587", invoicePage1Fields.get("InvoiceId")
            .getValue().asString());
        assertEquals("1 Redmond way Suite 6000 Redmond, WA 99243", invoicePage1Fields.get("VendorAddress")
            .getValue().asString());
        assertEquals(EXPECTED_MERCHANT_NAME, invoicePage1Fields.get("VendorName")
            .getValue().asString());

        Map<String, FormField> itemsMap
            = invoicePage1Fields.get("Items").getValue().asList().get(0).getValue().asMap();
        assertEquals(56651.49f, itemsMap.get("Amount").getValue().asFloat());
        assertEquals(LocalDate.of(2017, 6, 18), itemsMap.get("Date").getValue().asDate());
        assertEquals("34278587", itemsMap.get("ProductCode").getValue().asString());
        assertEquals(FieldValueType.FLOAT, itemsMap.get("Tax").getValue().getValueType());
    }

    void validateIdentityData(List<RecognizedForm> actualPrebuiltRecognizedForms, boolean includeFieldElements) {
        for (final RecognizedForm actualForm : actualPrebuiltRecognizedForms) {
            Assertions.assertEquals("prebuilt:idDocument:driverLicense", actualForm.getFormType());
            Assertions.assertNull(actualForm.getModelId());
            actualForm.getPages().forEach(formPage -> validateFormPage(formPage, includeFieldElements));

            ID_DOCUMENT_FIELDS.forEach(licenseField -> {
                final Map<String, FormField> actualRecognizedInvoiceFields = actualForm.getFields();
                if (actualRecognizedInvoiceFields.get(licenseField) != null) {
                    validateFieldValueData(actualRecognizedInvoiceFields.get(licenseField), includeFieldElements);
                }
            });
        }
        assertEquals(1, actualPrebuiltRecognizedForms.size());
        RecognizedForm licensePage1 = actualPrebuiltRecognizedForms.get(0);

        assertEquals(1, licensePage1.getPageRange().getFirstPageNumber());
        assertEquals(1, licensePage1.getPageRange().getLastPageNumber());
        assertNotNull(licensePage1.getPages());

        Map<String, FormField> licensePageFields = licensePage1.getFields();
        assertEquals(9, licensePageFields.size());
        assertEquals("123 STREET ADDRESS YOUR CITY WA 99999-1234", licensePageFields.get("Address")
            .getValue().asString());
        assertEquals("USA", licensePageFields.get("CountryRegion")
            .getValue().asCountryRegion());
        assertEquals(LocalDate.of(1958, 1, 6), licensePageFields.get("DateOfBirth")
            .getValue().asDate());
        assertEquals(LocalDate.of(2020, 8, 12), licensePageFields.get("DateOfExpiration")
            .getValue().asDate());
        assertEquals("WDLABCD456DG", licensePageFields.get("DocumentNumber")
            .getValue().asString());
        assertEquals("LIAM R.", licensePageFields.get("FirstName")
            .getValue().asString());
        assertEquals("TALBOT", licensePageFields.get("LastName")
            .getValue().asString());
        assertEquals("Washington", licensePageFields.get("Region")
            .getValue().asString());
        assertEquals("M", licensePageFields.get("Sex")
            .getValue().asString());
    }

    static void validateMultipageBusinessData(List<RecognizedForm> recognizedBusinessCards) {
        assertEquals(2, recognizedBusinessCards.size());
        RecognizedForm businessCard1 = recognizedBusinessCards.get(0);
        RecognizedForm businessCard2 = recognizedBusinessCards.get(1);

        assertEquals(1, businessCard1.getPageRange().getFirstPageNumber());
        assertEquals(1, businessCard1.getPageRange().getLastPageNumber());
        Map<String, FormField> businessCard1Fields = businessCard1.getFields();
        List<FormField> emailList = businessCard1Fields.get("Emails").getValue().asList();
        assertEquals("johnsinger@contoso.com", emailList.get(0).getValue().asString());
        List<FormField> phoneNumberList = businessCard1Fields.get("OtherPhones").getValue().asList();
        assertEquals("+14257793479", phoneNumberList.get(0).getValue().asPhoneNumber());
        assertNotNull(businessCard1.getPages());

        // assert contact name page number
        FormField contactNameField = businessCard1Fields.get("ContactNames").getValue().asList().get(0);
        assertEquals(1, contactNameField.getValueData().getPageNumber());
        assertEquals("JOHN SINGER", contactNameField.getValueData().getText());

        assertEquals(2, businessCard2.getPageRange().getFirstPageNumber());
        assertEquals(2, businessCard2.getPageRange().getLastPageNumber());
        Map<String, FormField> businessCard2Fields = businessCard2.getFields();
        List<FormField> email2List = businessCard2Fields.get("Emails").getValue().asList();
        assertEquals("avery.smith@contoso.com", email2List.get(0).getValue().asString());
        List<FormField> phoneNumber2List = businessCard2Fields.get("WorkPhones").getValue().asList();
        assertEquals("+44 (0) 20 9876 5432", phoneNumber2List.get(0).getValueData().getText());
        assertNotNull(businessCard2.getPages());

        // assert contact name page number
        FormField contactName2Field = businessCard2Fields.get("ContactNames").getValue().asList().get(0);
        assertEquals(2, contactName2Field.getValueData().getPageNumber());
        assertEquals("Dr. Avery Smith", contactName2Field.getValueData().getText());
    }

    void validateMultipageReceiptData(List<RecognizedForm> recognizedReceipts) {
        assertEquals(2, recognizedReceipts.size());
        RecognizedForm receiptPage1 = recognizedReceipts.get(0);
        RecognizedForm receiptPage2 = recognizedReceipts.get(1);

        assertEquals(1, receiptPage1.getPageRange().getFirstPageNumber());
        assertEquals(1, receiptPage1.getPageRange().getLastPageNumber());
        Map<String, FormField> receiptPage1Fields = receiptPage1.getFields();
        validateJpegReceiptFields(receiptPage1Fields);

        assertNotNull(receiptPage2.getFields());
        List<FormPage> receipt2Pages = receiptPage2.getPages();
        assertEquals(1, receipt2Pages.size());
        assertEquals(2, receiptPage2.getPageRange().getFirstPageNumber());
        assertEquals(2, receiptPage2.getPageRange().getLastPageNumber());

        Map<String, FormField> receiptPage2Fields = receiptPage2.getFields();
        validatePngReceiptFields(receiptPage2Fields);
    }

    static void validateMultipageInvoiceData(List<RecognizedForm> recognizedInvoices) {
        assertEquals(1, recognizedInvoices.size());
        RecognizedForm recognizedForm = recognizedInvoices.get(0);

        assertEquals(1, recognizedForm.getPageRange().getFirstPageNumber());
        assertEquals(2, recognizedForm.getPageRange().getLastPageNumber());
        Map<String, FormField> recognizedInvoiceFields = recognizedForm.getFields();
        final FormField remittanceAddressRecipient = recognizedInvoiceFields.get("RemittanceAddressRecipient");

        assertEquals("Contoso Ltd.", remittanceAddressRecipient.getValue().asString());
        assertEquals(1, remittanceAddressRecipient.getValueData().getPageNumber());
        final FormField remittanceAddress = recognizedInvoiceFields.get("RemittanceAddress");

        assertEquals("2345 Dogwood Lane Birch, Kansas 98123", remittanceAddress.getValue().asString());
        assertEquals(1, remittanceAddress.getValueData().getPageNumber());

        final FormField vendorName = recognizedInvoiceFields.get("VendorName");
        assertEquals("Southridge Video", vendorName.getValue().asString());
        assertEquals(2, vendorName.getValueData().getPageNumber());

        assertEquals(2, recognizedForm.getPages().size());
    }

    static void validateMultiPageDataLabeled(List<RecognizedForm> actualRecognizedFormsList, String modelId) {
        actualRecognizedFormsList.forEach(recognizedForm -> {
            assertEquals("custom:" + modelId, recognizedForm.getFormType());
            assertEquals(1, recognizedForm.getPageRange().getFirstPageNumber());
            assertEquals(3, recognizedForm.getPageRange().getLastPageNumber());
            assertEquals(3, recognizedForm.getPages().size());
            recognizedForm.getFields().forEach((label, formField) -> {
                assertNotNull(formField.getName());
                assertNotNull(formField.getValue());
                assertNotNull(formField.getValueData().getText());
                assertNull(formField.getLabelData());
            });
        });
    }

    static void validateMultiPageDataUnlabeled(List<RecognizedForm> actualRecognizedFormsList) {
        actualRecognizedFormsList.forEach(recognizedForm -> {
            assertNotNull(recognizedForm.getFormType());
            assertEquals(1, (long) recognizedForm.getPages().size());
            recognizedForm.getFields().forEach((label, formField) -> {
                assertNotNull(formField.getName());
                assertNotNull(formField.getValue());
                assertNotNull(formField.getValueData().getText());
                assertNotNull(formField.getLabelData().getText());
            });
        });
    }

    String getEndpoint() {
        return interceptorManager.isPlaybackMode() ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get(AZURE_FORM_RECOGNIZER_ENDPOINT);
    }

    private void validatePngReceiptFields(Map<String, FormField> receiptPageFields) {
        //  "123-456-7890" is not a valid US telephone number since no area code can start with 1, so the service
        //  returns a null instead.
        assertNull(receiptPageFields.get("MerchantPhoneNumber").getValue().asPhoneNumber());
        assertNotNull(receiptPageFields.get("Subtotal").getValue().asFloat());
        assertNotNull(receiptPageFields.get("Total").getValue().asFloat());
        assertNotNull(receiptPageFields.get("Tax").getValue().asFloat());
        assertNotNull(receiptPageFields.get("Items"));
        List<FormField> itemizedItems = receiptPageFields.get("Items").getValue().asList();

        for (int i = 0; i < itemizedItems.size(); i++) {
            if (itemizedItems.get(i).getValue() != null) {
                String[] itemizedNames = new String[] {"Surface Pro 6", "SurfacePen"};
                Float[] itemizedTotalPrices = new Float[] {999f, 99.99f};

                Map<String, FormField> actualReceiptItems = itemizedItems.get(i).getValue().asMap();
                int finalI = i;
                actualReceiptItems.forEach((key, formField) -> {
                    assertNotNull(formField.getValue());
                    if ("Name".equals(key)) {
                        assertNotNull(formField.getValue());
                        if (FieldValueType.STRING == formField.getValue().getValueType()) {
                            String name = formField.getValue().asString();
                            assertEquals(itemizedNames[finalI], name);
                        }
                    }
                    if ("Quantity".equals(key)) {
                        assertNotNull(formField.getValue());
                        if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                            Float quantity = formField.getValue().asFloat();
                            assertEquals(1.f, quantity);
                        }
                    }
                    if ("Price".equals(key)) {
                        assertNull(formField.getValue().asFloat());
                    }

                    if ("TotalPrice".equals(key)) {
                        if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                            Float totalPrice = formField.getValue().asFloat();
                            assertEquals(itemizedTotalPrices[finalI], totalPrice);
                        }
                    }
                });
            }
        }
    }

    private void validateJpegReceiptFields(Map<String, FormField> receiptPageFields) {
        assertEquals(EXPECTED_JPEG_RECEIPT_PHONE_NUMBER_VALUE, receiptPageFields.get("MerchantPhoneNumber")
            .getValue().asPhoneNumber());
        assertNotNull(receiptPageFields.get("Subtotal").getValue().asFloat());
        assertNotNull(receiptPageFields.get("Total").getValue().asFloat());
        assertNotNull(receiptPageFields.get("Tax").getValue().asFloat());
        assertNotNull(receiptPageFields.get("Items"));
        List<FormField> itemizedItems = receiptPageFields.get("Items").getValue().asList();

        for (int i = 0; i < itemizedItems.size(); i++) {
            if (itemizedItems.get(i).getValue() != null) {
                String[] itemizedNames = new String[] {"Cappuccino", "BACON & EGGS"};
                Float[] itemizedTotalPrices = new Float[] {2.2f, 9.5f};

                Map<String, FormField> actualReceiptItems = itemizedItems.get(i).getValue().asMap();
                int finalI = i;
                actualReceiptItems.forEach((key, formField) -> {
                    assertNotNull(formField.getValue());
                    if ("Name".equals(key)) {
                        assertNotNull(formField.getValue());
                        if (FieldValueType.STRING == formField.getValue().getValueType()) {
                            String name = formField.getValue().asString();
                            assertEquals(itemizedNames[finalI], name);
                        }
                    }
                    if ("Quantity".equals(key)) {
                        assertNotNull(formField.getValue());
                        if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                            Float quantity = formField.getValue().asFloat();
                            assertEquals(1.f, quantity);
                        }
                    }
                    if ("Price".equals(key)) {
                        assertNull(formField.getValue().asFloat());
                    }

                    if ("TotalPrice".equals(key)) {
                        if (FieldValueType.FLOAT == formField.getValue().getValueType()) {
                            Float totalPrice = formField.getValue().asFloat();
                            assertEquals(itemizedTotalPrices[finalI], totalPrice);
                        }
                    }
                });
            }
        }
    }

    private void validateFieldValueData(FormField formField, boolean includeFieldElements) {
        assertNotNull(formField.getName());
        FieldData fieldData = formField.getLabelData();
        if (fieldData != null) {
            validateBoundingBoxData(fieldData.getBoundingBox());

            if (!includeFieldElements) {
                assertEquals(0, fieldData.getFieldElements().size());
            }
        }
    }

    private void validateBoundingBoxData(FieldBoundingBox fieldBoundingBox) {
        assertNotNull(fieldBoundingBox);
        assertEquals(4, fieldBoundingBox.getPoints().size());
    }

    /**
     * Get the training data set SAS Url value based on the test running mode.
     *
     * @return the training data set Url
     */
    private String getTrainingSasUri() {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode";
        } else {
            return Configuration.getGlobalConfiguration().get(FORM_RECOGNIZER_TRAINING_BLOB_CONTAINER_SAS_URL);
        }
    }

    private String getSelectionMarkTrainingSasUri() {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode";
        } else {
            return Configuration.getGlobalConfiguration().get(FORM_RECOGNIZER_SELECTION_MARK_BLOB_CONTAINER_SAS_URL);
        }
    }

    /**
     * Get the training data set SAS Url value based on the test running mode.
     *
     * @return the training data set Url
     */
    private String getMultipageTrainingSasUri() {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode";
        } else {
            return Configuration.getGlobalConfiguration()
                .get(FORM_RECOGNIZER_MULTIPAGE_TRAINING_BLOB_CONTAINER_SAS_URL);
        }
    }

    /**
     * Get the testing data set SAS Url value based on the test running mode.
     *
     * @return the testing data set Url
     */
    private String getTestingSasUri() {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode?SASToken";
        } else {
            return Configuration.getGlobalConfiguration().get("FORM_RECOGNIZER_TESTING_BLOB_CONTAINER_SAS_URL");
        }
    }

    /**
     * Prepare the file url from the testing data set SAS Url value.
     *
     * @return the testing data specific file Url
     */
    private String getStorageTestingFileUrl(String fileName) {
        if (interceptorManager.isPlaybackMode()) {
            return "https://isPlaybackmode";
        } else {
            final String[] urlParts = getTestingSasUri().split("\\?");
            return urlParts[0] + "/" + fileName + "?" + urlParts[1];
        }
    }
}
