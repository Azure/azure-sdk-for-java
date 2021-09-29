// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.DocumentField;
import com.azure.ai.formrecognizer.models.DocumentFieldType;
import com.azure.ai.formrecognizer.models.DocumentPage;
import com.azure.ai.formrecognizer.models.DocumentSelectionMark;
import com.azure.ai.formrecognizer.models.DocumentTable;
import com.azure.ai.formrecognizer.models.LengthUnit;
import com.azure.ai.formrecognizer.models.SelectionMarkState;
import com.azure.ai.formrecognizer.models.StringIndexType;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.FluxUtil;
import org.junit.jupiter.api.Assertions;
import reactor.test.StepVerifier;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.azure.ai.formrecognizer.TestUtils.AZURE_FORM_RECOGNIZER_API_KEY_CONFIGURATION;
import static com.azure.ai.formrecognizer.TestUtils.AZURE_FORM_RECOGNIZER_ENDPOINT_CONFIGURATION;
import static com.azure.ai.formrecognizer.TestUtils.EXPECTED_MERCHANT_NAME;
import static com.azure.ai.formrecognizer.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.TestUtils.ONE_NANO_DURATION;
import static com.azure.ai.formrecognizer.implementation.util.Constants.DEFAULT_POLL_INTERVAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class DocumentAnalysisClientTestBase extends TestBase {
    static final String ENCODED_EMPTY_SPACE =
        "{\"urlSource\":\"https://fakeuri.com/blank%20space\"}";

    Duration durationTestMode;

    /**
     * Use duration of nearly zero value for PLAYBACK test mode, otherwise, use default duration value for LIVE mode.
     */
    @Override
    protected void beforeTest() {
        durationTestMode = interceptorManager.isPlaybackMode() ? ONE_NANO_DURATION : DEFAULT_POLL_INTERVAL;
    }

    DocumentAnalysisClientBuilder getDocumentAnalysisBuilder(HttpClient httpClient,
                                                             DocumentAnalysisServiceVersion serviceVersion) {
        DocumentAnalysisClientBuilder builder = new DocumentAnalysisClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy());

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(INVALID_KEY));
        } else {
            builder.credential(new AzureKeyCredential(TestUtils.AZURE_FORM_RECOGNIZER_API_KEY_CONFIGURATION));
        }
        return builder;
    }

    DocumentModelAdministrationClientBuilder getDocumentModelAdminClientBuilder(HttpClient httpClient,
                                                                                DocumentAnalysisServiceVersion serviceVersion) {
        DocumentModelAdministrationClientBuilder builder = new DocumentModelAdministrationClientBuilder()
            .endpoint(getEndpoint())
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy());

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(INVALID_KEY));
        } else {
            builder.credential(new AzureKeyCredential(AZURE_FORM_RECOGNIZER_API_KEY_CONFIGURATION));
        }
        return builder;
    }

    static void validateEncodedUrlExceptionSource(HttpResponseException errorResponseException) {
        StepVerifier.create(FluxUtil.collectBytesInByteBufferStream(
                errorResponseException.getResponse().getRequest().getBody()))
            .assertNext(bytes -> assertEquals(ENCODED_EMPTY_SPACE, new String(bytes, StandardCharsets.UTF_8)))
            .verifyComplete();
    }

    void dataRunner(BiConsumer<InputStream, Long> testRunner, String fileName) {
        TestUtils.getDataRunnerHelper(testRunner, fileName, interceptorManager.isPlaybackMode());
    }

    void testingContainerUrlRunner(Consumer<String> testRunner, String fileName) {
        TestUtils.getTestingContainerHelper(testRunner, fileName, interceptorManager.isPlaybackMode());
    }

    void buildModelRunner(Consumer<String> testRunner) {
        TestUtils.getTrainingDataContainerHelper(testRunner, interceptorManager.isPlaybackMode());
    }

    void multipageTrainingRunner(Consumer<String> testRunner) {
        TestUtils.getMultipageTrainingContainerHelper(testRunner, interceptorManager.isPlaybackMode());
    }

    void selectionMarkTrainingRunner(Consumer<String> testRunner) {
        TestUtils.getSelectionMarkTrainingContainerHelper(testRunner, interceptorManager.isPlaybackMode());
    }

    void validatePngReceiptData(AnalyzeResult actualAnalyzeResult) {
        validateReceipt(actualAnalyzeResult);

        // pages
        Assertions.assertEquals(1, actualAnalyzeResult.getPages().size());

        // styles
        Assertions.assertNull(actualAnalyzeResult.getStyles());

        // documents
        Assertions.assertEquals(1, actualAnalyzeResult.getDocuments().size());
        actualAnalyzeResult.getDocuments().forEach(actualDocument -> {
            Assertions.assertEquals("prebuilt:receipt", actualDocument.getDocType());
            // document fields
            validatePngReceiptFields(actualDocument.getFields());
        });
    }

    void validateJpegReceiptData(AnalyzeResult actualAnalyzeResult) {
        validateReceipt(actualAnalyzeResult);

        // pages
        Assertions.assertEquals(1, actualAnalyzeResult.getPages().size());

        // styles
        // confirm with service team, spans should be two?
        // Assertions.assertEquals(2, actualAnalyzeResult.getStyles().get(0).getSpans().size());
        Assertions.assertTrue(actualAnalyzeResult.getStyles().get(0).isHandwritten());

        actualAnalyzeResult.getStyles()
            .forEach(actualDocumentStyle -> Assertions.assertTrue(actualDocumentStyle.isHandwritten()));

        // documents
        Assertions.assertEquals(1, actualAnalyzeResult.getDocuments().size());
        actualAnalyzeResult.getDocuments().forEach(actualDocument -> {
            Assertions.assertEquals("prebuilt:receipt", actualDocument.getDocType());
            // document fields
            validateJpegReceiptFields(actualDocument.getFields());
        });

    }

    void validateMultipageReceiptData(AnalyzeResult analyzeResult) {
        validateReceipt(analyzeResult);
        assertEquals(2, analyzeResult.getPages().size());
        DocumentPage page1 = analyzeResult.getPages().get(0);
        DocumentPage page2 = analyzeResult.getPages().get(1);
        assertEquals(1, page1.getPageNumber());
        assertEquals(2, page2.getPageNumber());
        assertEquals(1, page1.getSpans().size());
        assertEquals(1, page2.getSpans().size());
        assertEquals(207, page1.getSpans().get(0).getLength());
        assertEquals(207, page2.getSpans().get(0).getOffset());
        assertEquals(1, analyzeResult.getStyles().size());

        DocumentPage receiptPage1 = analyzeResult.getPages().get(0);
        DocumentPage receiptPage2 = analyzeResult.getPages().get(1);

        assertEquals(1, receiptPage1.getPageNumber());
        assertEquals(2, analyzeResult.getDocuments().size());
        Map<String, DocumentField> receiptPage1Fields = analyzeResult.getDocuments().get(0).getFields();
        validateJpegReceiptFields(receiptPage1Fields);

        Assertions.assertNotNull(analyzeResult.getDocuments().get(1).getFields());
        assertEquals(2, receiptPage2.getPageNumber());

        Map<String, DocumentField> receiptPage2Fields = analyzeResult.getDocuments().get(1).getFields();
        validatePngReceiptFields(receiptPage2Fields);
    }

    void validateBlankPdfData(AnalyzeResult actualAnalyzeResult) {
        assertEquals(1, actualAnalyzeResult.getPages().size());
    }

    void validateBusinessCardData(AnalyzeResult analyzeResult) {
        Assertions.assertEquals("prebuilt-businessCard", analyzeResult.getModelId());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertNotNull(documentPage.getLines());
            documentPage.getLines().forEach(documentLine -> {
                validateBoundingBoxData(documentLine.getBoundingBox());
                Assertions.assertNotNull(documentLine.getContent());
            });

            Assertions.assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getBoundingBox());
                // should be getContent()
                Assertions.assertNotNull(documentWord.getContent());
            });
        });
        Assertions.assertEquals(1, analyzeResult.getDocuments().size());

        Assertions.assertNotNull(analyzeResult.getPages());
        DocumentPage businessCardPage1 = analyzeResult.getPages().get(0);
        assertEquals(1, businessCardPage1.getPageNumber());

        final Map<String, DocumentField> actualBusinessCardFields = analyzeResult.getDocuments().get(0).getFields();
        assertEquals("2 Kingdom Street Paddington, London, W2 6BD",
            actualBusinessCardFields.get("Addresses").getValueList().get(0).getValueString());
        assertEquals(EXPECTED_MERCHANT_NAME, actualBusinessCardFields.get("CompanyNames")
            .getValueList().get(0).getValueString());
        assertEquals("Cloud & Al Department", actualBusinessCardFields.get("Departments")
            .getValueList().get(0).getValueString());
        assertEquals("avery.smith@contoso.com", actualBusinessCardFields.get("Emails")
            .getValueList().get(0).getValueString());
        assertEquals(DocumentFieldType.PHONE_NUMBER, actualBusinessCardFields.get("Faxes")
            .getValueList().get(0).getType());
        assertEquals("Senior Researcher", actualBusinessCardFields.get("JobTitles")
            .getValueList().get(0).getValueString());
        assertEquals(DocumentFieldType.PHONE_NUMBER, actualBusinessCardFields.get("MobilePhones")
            .getValueList().get(0).getType());
        assertEquals("https://www.contoso.com/", actualBusinessCardFields.get("Websites")
            .getValueList().get(0).getValueString());
        assertEquals(DocumentFieldType.PHONE_NUMBER, actualBusinessCardFields.get("WorkPhones")
            .getValueList().get(0).getType());
        Map<String, DocumentField> contactNamesMap
            = actualBusinessCardFields.get("ContactNames").getValueList().get(0).getValueMap();
        assertEquals("Avery", contactNamesMap.get("FirstName").getValueString());
        assertEquals("Smith", contactNamesMap.get("LastName").getValueString());
    }

    static void validateMultipageBusinessData(AnalyzeResult analyzeResult) {
        assertEquals(2, analyzeResult.getPages().size());
        assertEquals(2, analyzeResult.getDocuments().size());
        DocumentPage businessCard1 = analyzeResult.getPages().get(0);
        DocumentPage businessCard2 = analyzeResult.getPages().get(1);

        assertEquals(1, businessCard1.getPageNumber());
        Map<String, DocumentField> businessCard1Fields = analyzeResult.getDocuments().get(0).getFields();
        List<DocumentField> emailList = businessCard1Fields.get("Emails").getValueList();
        assertEquals("johnsinger@contoso.com", emailList.get(0).getValueString());
        List<DocumentField> phoneNumberList = businessCard1Fields.get("OtherPhones").getValueList();
        assertEquals("+14257793479", phoneNumberList.get(0).getValuePhoneNumber());
        assertEquals(1, businessCard1.getPageNumber());

        // assert contact name page number
        DocumentField contactNameField = businessCard1Fields.get("ContactNames").getValueList().get(0);
        assertEquals("JOHN SINGER", contactNameField.getContent());

        assertEquals(2, businessCard2.getPageNumber());
        Map<String, DocumentField> businessCard2Fields = analyzeResult.getDocuments().get(1).getFields();
        List<DocumentField> email2List = businessCard2Fields.get("Emails").getValueList();
        assertEquals("avery.smith@contoso.com", email2List.get(0).getValueString());
        List<DocumentField> phoneNumber2List = businessCard2Fields.get("WorkPhones").getValueList();
        assertEquals("+44 (0) 20 9876 5432", phoneNumber2List.get(0).getContent());

        // assert contact name page number
        DocumentField contactName2Field = businessCard2Fields.get("ContactNames").getValueList().get(0);
        assertEquals(2, contactName2Field.getBoundingRegions().get(0).getPageNumber());
        assertEquals("Dr. Avery Smith", contactName2Field.getContent());
    }

    void validateInvoiceData(AnalyzeResult analyzeResult) {
        Assertions.assertEquals("prebuilt-invoice", analyzeResult.getModelId());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertNotNull(documentPage.getLines());
            documentPage.getLines().forEach(documentLine -> {
                validateBoundingBoxData(documentLine.getBoundingBox());
                Assertions.assertNotNull(documentLine.getContent());
            });

            Assertions.assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getBoundingBox());
                Assertions.assertNotNull(documentWord.getContent());
            });
        });

        assertEquals(1, analyzeResult.getPages().size());
        DocumentPage invoicePage1 = analyzeResult.getPages().get(0);

        assertEquals(1, invoicePage1.getPageNumber());

        Assertions.assertNotNull(analyzeResult.getDocuments());
        assertEquals(1, analyzeResult.getDocuments().size());
        Map<String, DocumentField> invoicePage1Fields = analyzeResult.getDocuments().get(0).getFields();
        assertEquals("1020 Enterprise Way Sunnayvale, CA 87659", invoicePage1Fields.get("CustomerAddress")
            .getValueString());
        assertEquals("Microsoft", invoicePage1Fields.get("CustomerAddressRecipient")
            .getValueString());
        assertEquals("Microsoft", invoicePage1Fields.get("CustomerName")
            .getValueString());
        assertEquals(LocalDate.of(2017, 6, 24), invoicePage1Fields.get("DueDate")
            .getValueDate());
        assertEquals(LocalDate.of(2017, 6, 18), invoicePage1Fields.get("InvoiceDate")
            .getValueDate());
        assertEquals("34278587", invoicePage1Fields.get("InvoiceId")
            .getValueString());
        assertEquals("1 Redmond way Suite 6000 Redmond, WA 99243", invoicePage1Fields.get("VendorAddress")
            .getValueString());
        assertEquals(EXPECTED_MERCHANT_NAME, invoicePage1Fields.get("VendorName")
            .getValueString());

        Map<String, DocumentField> itemsMap
            = invoicePage1Fields.get("Items").getValueList().get(0).getValueMap();
        assertEquals(56651.49f, itemsMap.get("Amount").getValueFloat());
        assertEquals(LocalDate.of(2017, 6, 18), itemsMap.get("Date").getValueDate());
        assertEquals("34278587", itemsMap.get("ProductCode").getValueString());
        assertEquals(DocumentFieldType.FLOAT, itemsMap.get("Tax").getType());
    }

    static void validateMultipageInvoiceData(AnalyzeResult analyzeResult) {
        assertEquals(2, analyzeResult.getPages().size());
        DocumentPage invoicePage1 = analyzeResult.getPages().get(0);

        assertEquals(1, invoicePage1.getPageNumber());
        assertEquals(1, analyzeResult.getDocuments().size());
        Map<String, DocumentField> recognizedInvoiceFields = analyzeResult.getDocuments().get(0).getFields();
        final DocumentField remittanceAddressRecipient = recognizedInvoiceFields.get("RemittanceAddressRecipient");

        assertEquals("Contoso Ltd.", remittanceAddressRecipient.getValueString());
        assertEquals(1, remittanceAddressRecipient.getBoundingRegions().get(0).getPageNumber());
        final DocumentField remittanceAddress = recognizedInvoiceFields.get("RemittanceAddress");

        assertEquals("2345 Dogwood Lane Birch, Kansas 98123", remittanceAddress.getValueString());
        assertEquals(1, remittanceAddress.getBoundingRegions().get(0).getPageNumber());

        final DocumentField vendorName = recognizedInvoiceFields.get("VendorName");
        assertEquals("Southridge Video", vendorName.getValueString());
        assertEquals(2, vendorName.getBoundingRegions().get(0).getPageNumber());
    }

    void validateIdentityData(AnalyzeResult analyzeResult) {
        Assertions.assertEquals("prebuilt-idDocument", analyzeResult.getModelId());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertNotNull(documentPage.getLines());
            documentPage.getLines().forEach(documentLine -> {
                validateBoundingBoxData(documentLine.getBoundingBox());
                Assertions.assertNotNull(documentLine.getContent());
            });

            Assertions.assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getBoundingBox());
                // should be getContent()
                Assertions.assertNotNull(documentWord.getContent());
            });
        });

        assertEquals(1, analyzeResult.getPages().size());
        DocumentPage licensePage1 = analyzeResult.getPages().get(0);
        assertEquals(1, licensePage1.getPageNumber());

        Assertions.assertNotNull(analyzeResult.getDocuments());
        assertEquals("prebuilt:idDocument:driverLicense", analyzeResult.getDocuments().get(0).getDocType());
        Map<String, DocumentField> licensePageFields = analyzeResult.getDocuments().get(0).getFields();
        assertEquals("123 STREET ADDRESS YOUR CITY WA 99999-1234", licensePageFields.get("Address")
            .getValueString());
        assertEquals("USA", licensePageFields.get("CountryRegion").getValueCountryRegion());
        assertEquals(LocalDate.of(1958, 1, 6), licensePageFields.get("DateOfBirth")
            .getValueDate());
        assertEquals(LocalDate.of(2020, 8, 12), licensePageFields.get("DateOfExpiration")
            .getValueDate());
        assertEquals("WDLABCD456DG", licensePageFields.get("DocumentNumber")
            .getValueString());
        assertEquals("LIAM R.", licensePageFields.get("FirstName")
            .getValueString());
        assertEquals("TALBOT", licensePageFields.get("LastName")
            .getValueString());
        assertEquals("Washington", licensePageFields.get("Region")
            .getValueString());
        assertEquals("M", licensePageFields.get("Sex")
            .getValueString());
        assertEquals("L", licensePageFields.get("Endorsements")
            .getValueString());
        assertEquals("B", licensePageFields.get("Restrictions")
            .getValueString());
    }

    void validateGermanContentData(AnalyzeResult analyzeResult) {
        Assertions.assertNotNull(analyzeResult.getPages());
        assertEquals(1, analyzeResult.getPages().size());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertTrue(
                documentPage.getAngle() > -180.0 && documentPage.getAngle() < 180.0);
            Assertions.assertNotNull(analyzeResult.getTables());
            Assertions.assertEquals(8.5, documentPage.getWidth());
            Assertions.assertEquals(11, documentPage.getHeight());
            Assertions.assertEquals(LengthUnit.INCH, documentPage.getUnit());

        });

        Assertions.assertNotNull(analyzeResult.getTables());
        int[] table = new int[] {8, 3, 24};
        Assertions.assertEquals(1, analyzeResult.getTables().size());
        for (int i = 0; i < analyzeResult.getTables().size(); i++) {
            DocumentTable actualDocumentTable = analyzeResult.getTables().get(i);
            Assertions.assertEquals(table[i], actualDocumentTable.getRowCount());
            Assertions.assertEquals(table[++i], actualDocumentTable.getColumnCount());
            Assertions.assertEquals(table[++i], actualDocumentTable.getCells().size());
            actualDocumentTable.getCells().forEach(documentTableCell
                -> Assertions.assertNotNull(documentTableCell.getKind()));
        }
    }

    void validateSelectionMarkContentData(AnalyzeResult analyzeResult) {
        Assertions.assertNotNull(analyzeResult.getPages());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertTrue(
                documentPage.getAngle() > -180.0 && documentPage.getAngle() < 180.0);
            Assertions.assertEquals(8.5, documentPage.getWidth());
            Assertions.assertEquals(11, documentPage.getHeight());
            Assertions.assertEquals(LengthUnit.INCH, documentPage.getUnit());

            Assertions.assertNotNull(documentPage.getSelectionMarks());
            Assertions.assertEquals(3, documentPage.getSelectionMarks().size());
            List<DocumentSelectionMark> selectionMarks = documentPage.getSelectionMarks();
            for (int i = 0; i < selectionMarks.size(); i++) {
                DocumentSelectionMark documentSelectionMark = selectionMarks.get(i);
                validateBoundingBoxData(documentSelectionMark.getBoundingBox());
                Assertions.assertNotNull(documentSelectionMark.getState());
                if (i == 0) {
                    Assertions.assertEquals(SelectionMarkState.UNSELECTED, documentSelectionMark.getState());
                } else if (i == 1) {
                    assertEquals(SelectionMarkState.SELECTED, documentSelectionMark.getState());
                } else {
                    assertEquals(SelectionMarkState.UNSELECTED, documentSelectionMark.getState());
                }
            }
        });
        assertNull(analyzeResult.getKeyValuePairs());
        assertNull(analyzeResult.getEntities());
        Assertions.assertNotNull(analyzeResult.getStyles());
        assertEquals(1, analyzeResult.getStyles().size());
        assertNull(analyzeResult.getDocuments());
    }

    void validatePdfContentData(AnalyzeResult analyzeResult) {
        Assertions.assertNotNull(analyzeResult.getPages());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertTrue(
                documentPage.getAngle() > -180.0 && documentPage.getAngle() < 180.0);
            Assertions.assertNotNull(analyzeResult.getTables());
            Assertions.assertEquals(8.5, documentPage.getWidth());
            Assertions.assertEquals(11, documentPage.getHeight());
            Assertions.assertEquals(LengthUnit.INCH, documentPage.getUnit());

            validateDocumentPage(documentPage);
        });

        Assertions.assertNotNull(analyzeResult.getTables());
        int[] table = new int[] {3, 5, 10};
        Assertions.assertEquals(1, analyzeResult.getTables().size());
        for (int i = 0; i < analyzeResult.getTables().size(); i++) {
            DocumentTable actualDocumentTable = analyzeResult.getTables().get(i);
            Assertions.assertEquals(table[i], actualDocumentTable.getRowCount());
            Assertions.assertEquals(table[++i], actualDocumentTable.getColumnCount());
            Assertions.assertEquals(table[++i], actualDocumentTable.getCells().size());
        }

        assertNull(analyzeResult.getKeyValuePairs());
        assertNull(analyzeResult.getEntities());
        assertNull(analyzeResult.getStyles());
        assertNull(analyzeResult.getDocuments());
    }

    void validateContentData(AnalyzeResult analyzeResult) {
        Assertions.assertNotNull(analyzeResult.getPages());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertTrue(
                documentPage.getAngle() > -180.0 && documentPage.getAngle() < 180.0);
            Assertions.assertNotNull(analyzeResult.getTables());
            Assertions.assertEquals(1700, documentPage.getWidth());
            Assertions.assertEquals(2200, documentPage.getHeight());
            Assertions.assertEquals(LengthUnit.PIXEL, documentPage.getUnit());

            validateDocumentPage(documentPage);
        });

        Assertions.assertNotNull(analyzeResult.getTables());
        int[][] table = new int[][] {{5, 4, 20}, {4, 2, 8}};
        Assertions.assertEquals(2, analyzeResult.getTables().size());
        for (int i = 0; i < analyzeResult.getTables().size(); i++) {
            int j = 0;
            DocumentTable actualDocumentTable = analyzeResult.getTables().get(i);
            Assertions.assertEquals(table[i][j], actualDocumentTable.getRowCount());
            Assertions.assertEquals(table[i][++j], actualDocumentTable.getColumnCount());
            Assertions.assertEquals(table[i][++j], actualDocumentTable.getCells().size());
        }

        assertNull(analyzeResult.getKeyValuePairs());
        assertNull(analyzeResult.getEntities());
        Assertions.assertNotNull(analyzeResult.getStyles());
        assertEquals(1, analyzeResult.getStyles().size());
        assertNull(analyzeResult.getDocuments());
    }

    void validateDocumentPage(DocumentPage documentPage) {
        Assertions.assertNotNull(documentPage.getLines());
        documentPage.getLines().forEach(documentLine -> {
            validateBoundingBoxData(documentLine.getBoundingBox());
            Assertions.assertNotNull(documentLine.getContent());
        });

        Assertions.assertNotNull(documentPage.getWords());
        documentPage.getWords().forEach(documentWord -> {
            validateBoundingBoxData(documentWord.getBoundingBox());
            Assertions.assertNotNull(documentWord.getContent());
        });

        Assertions.assertNotNull(documentPage.getSelectionMarks());
        documentPage.getSelectionMarks().forEach(documentSelectionMark -> {
            validateBoundingBoxData(documentSelectionMark.getBoundingBox());
            Assertions.assertNotNull(documentSelectionMark.getState());
        });
    }

    void validateMultipageLayoutContent(AnalyzeResult analyzeResult) {
        Assertions.assertNotNull(analyzeResult.getPages());
        List<DocumentPage> pages = analyzeResult.getPages();
        for (int i = 0; i < pages.size(); i++) {
            DocumentPage documentPage = pages.get(i);
            if (i == 0) {
                assertEquals(1, documentPage.getSelectionMarks().size());
            }
            if (i == 1) {
                // empty page
                assertEquals(2, analyzeResult.getPages().get(1).getPageNumber());
                // getting empty instead of null confirm
                assertEquals(0, documentPage.getLines().size());
            }
            Assertions.assertTrue(
                documentPage.getAngle() > -180.0 && documentPage.getAngle() < 180.0);
            validateDocumentPage(documentPage);
        }

        Assertions.assertNotNull(analyzeResult.getTables());
        int[][] table = new int[][] {{8, 3, 24}, {8, 3, 24}};
        Assertions.assertEquals(2, analyzeResult.getTables().size());
        for (int i = 0; i < analyzeResult.getTables().size(); i++) {
            int j = 0;
            DocumentTable actualDocumentTable = analyzeResult.getTables().get(i);
            Assertions.assertEquals(table[i][j], actualDocumentTable.getRowCount());
            Assertions.assertEquals(table[i][++j], actualDocumentTable.getColumnCount());
            Assertions.assertEquals(table[i][++j], actualDocumentTable.getCells().size());
        }

        assertNull(analyzeResult.getKeyValuePairs());
        assertNull(analyzeResult.getEntities());
        assertNull(analyzeResult.getStyles());
        assertNull(analyzeResult.getDocuments());
    }

    void validateJpegCustomDocument(AnalyzeResult actualAnalyzeResult, String modelId) {
        List<DocumentPage> documentPages = actualAnalyzeResult.getPages();
        Assertions.assertEquals(1, documentPages.size());
        documentPages.forEach(documentPage -> validateDocumentPage(documentPage));
        int[][] table = new int[][] {{5, 4, 20}, {4, 2, 8}};
        Assertions.assertEquals(2, actualAnalyzeResult.getTables().size());
        for (int i = 0; i < actualAnalyzeResult.getTables().size(); i++) {
            int j = 0;
            DocumentTable actualDocumentTable = actualAnalyzeResult.getTables().get(i);
            Assertions.assertEquals(table[i][j], actualDocumentTable.getRowCount());
            Assertions.assertEquals(table[i][++j], actualDocumentTable.getColumnCount());
            Assertions.assertEquals(table[i][++j], actualDocumentTable.getCells().size());
        }

        actualAnalyzeResult.getDocuments().forEach(actualDocument -> {
            Assertions.assertEquals(modelId + ":" + modelId, actualDocument.getDocType());
            actualDocument.getFields().forEach((key, documentField) -> {
                // document fields

                // if ("Tax".equals(key)) {
                //     // incorrect reporting to 140
                //     assertEquals("$4.00", documentField.getValueString());
                // }
                if ("Signature".equals(key)) {
                    assertEquals("Bernie Sanders", documentField.getValueString());
                } else if ("Email".equals(key)) {
                    assertEquals("accounts@herolimited.com", documentField.getValueString());
                } else if ("PhoneNumber".equals(key)) {
                    assertEquals("555-348-6512", documentField.getValueString());
                } else if ("Quantity".equals(key)) {
                    assertEquals(20.0f, documentField.getValueFloat());
                } else if ("CompanyPhoneNumber".equals(key)) {
                    assertEquals("938-294-2949", documentField.getValueString());
                } else if ("DatedAs".equals(key)) {
                    assertEquals("12/20/2020", documentField.getValueString());
                } else if ("Total".equals(key)) {
                    assertEquals("$144.00", documentField.getValueString());
                } else if ("CompanyName".equals(key)) {
                    assertEquals("Higgly Wiggly Books", documentField.getValueString());
                } else if ("VendorName".equals(key)) {
                    assertEquals("Hillary Swank", documentField.getValueString());
                } else if ("Website".equals(key)) {
                    assertEquals("www.herolimited.com", documentField.getValueString());
                }
                else if ("Merchant".equals(key)) {
                    assertEquals("Hero Limited", documentField.getValueString());
                } else if ("PurchaseOrderNumber".equals(key)) {
                    assertEquals("948284", documentField.getValueString());
                } else if ("CompanyAddress".equals(key)) {
                    assertEquals("938 NE Burner Road Boulder City, CO 92848",
                        documentField.getValueString());
                }
                // else if ("Subtotal".equals(key)) {
                // returned as null currently
                // assertEquals("$140.00", documentField.getValueString());
                // }
            });
        });
    }

    void validateMultiPagePdfData(AnalyzeResult analyzeResult, String modelId) {
        assertEquals(3, analyzeResult.getPages().size());
        analyzeResult.getDocuments().forEach(analyzedDocument -> {
            assertEquals(modelId + ":" + modelId, analyzedDocument.getDocType());
            analyzedDocument.getFields().forEach((key, documentField) -> {
                Assertions.assertNotNull(documentField.getType());
            });
        });
    }

    void validateCustomDocumentWithSelectionMarks(AnalyzeResult analyzeResult) {
        Assertions.assertEquals(1, analyzeResult.getPages().size());
        analyzeResult.getPages().forEach(actualDocumentPage -> {
            Assertions.assertEquals(8.5, actualDocumentPage.getWidth());
            Assertions.assertEquals(11, actualDocumentPage.getHeight());
            Assertions.assertEquals(LengthUnit.INCH, actualDocumentPage.getUnit());

            validateDocumentPage(actualDocumentPage);
        });
        Assertions.assertEquals(0, analyzeResult.getTables().size());

        analyzeResult.getDocuments().forEach(actualDocument -> {
            Assertions.assertEquals("custom:", actualDocument.getDocType());
            actualDocument.getFields().forEach((key, documentField) -> {
                if ("AMEX_SELECTION_MARK".equals(key)) {
                    assertEquals(SelectionMarkState.SELECTED, documentField.getValueSelectionMark());
                } else if ("VISA_SELECTION_MARK".equals(key)) {
                    assertEquals(SelectionMarkState.UNSELECTED, documentField.getValueSelectionMark());
                } else if ("MASTERCARD_SELECTION_MARK".equals(key)) {
                    assertEquals(SelectionMarkState.UNSELECTED, documentField.getValueSelectionMark());
                } else {
                    throw new IllegalStateException("Unexpected value: " + key);
                }
            });
        });
    }

    private void validateBoundingBoxData(List<Float> points) {
        Assertions.assertNotNull(points);
        assertEquals(8, points.size());
    }

    private void validatePngReceiptFields(Map<String, DocumentField> actualFields) {
        //  "123-456-7890" is not a valid US telephone number since no area code can start with 1, so the service
        //  returns a null instead.
        assertNull(actualFields.get("MerchantPhoneNumber").getValuePhoneNumber());
        Assertions.assertNotNull(actualFields.get("Subtotal").getValueFloat());
        Assertions.assertNotNull(actualFields.get("Total").getValueFloat());
        Assertions.assertNotNull(actualFields.get("Tax").getValueFloat());
        Assertions.assertNotNull(actualFields.get("Items"));
        List<DocumentField> itemizedItems = actualFields.get("Items").getValueList();

        for (int i = 0; i < itemizedItems.size(); i++) {
            if (itemizedItems.get(i).getContent() != null) {
                String[] itemizedNames = new String[] {"Surface Pro 6", "SurfacePen"};
                Float[] itemizedTotalPrices = new Float[] {999f, 99.99f};

                Map<String, DocumentField> actualReceiptItems = itemizedItems.get(i).getValueMap();
                int finalI = i;
                actualReceiptItems.forEach((key, documentField) -> {
                    if ("Name".equals(key)) {
                        if (DocumentFieldType.STRING == documentField.getType()) {
                            String name = documentField.getValueString();
                            assertEquals(itemizedNames[finalI], name);
                        }
                    }
                    if ("Quantity".equals(key)) {
                        if (DocumentFieldType.FLOAT == documentField.getType()) {
                            Float quantity = documentField.getValueFloat();
                            assertEquals(1.f, quantity);
                        }
                    }
                    if ("Price".equals(key)) {
                        assertNull(documentField.getValueFloat());
                    }

                    if ("TotalPrice".equals(key)) {
                        if (DocumentFieldType.FLOAT == documentField.getType()) {
                            Float totalPrice = documentField.getValueFloat();
                            assertEquals(itemizedTotalPrices[finalI], totalPrice);
                        }
                    }
                });
            }
        }
    }

    private void validateReceipt(AnalyzeResult actualAnalyzeResult) {
        Assertions.assertEquals("prebuilt-receipt", actualAnalyzeResult.getModelId());
        Assertions.assertEquals(StringIndexType.UTF16CODE_UNIT, actualAnalyzeResult.getStringIndexType());
        Assertions.assertNotNull(actualAnalyzeResult.getPages());
    }

    private void validateJpegReceiptFields(Map<String, DocumentField> actualFields) {
        actualFields.forEach((key, documentField) -> {
            if (documentField.getBoundingRegions() != null) {
                Assertions.assertEquals(1, documentField.getBoundingRegions().get(0).getPageNumber());
            }
            if ("Locale".equals(key)) {
                Assertions.assertEquals("en-US", documentField.getValueString());
            } else if ("MerchantAddress".equals(key)) {
                Assertions.assertEquals("123 Main Street Redmond, WA 98052", documentField.getValueString());
            } else if ("MerchantName".equals(key)) {
                Assertions.assertEquals("Contoso", documentField.getValueString());
            } else if ("MerchantPhoneNumber".equals(key)) {
                Assertions.assertEquals("+19876543210", documentField.getValuePhoneNumber());
            } else if ("ReceiptType".equals(key)) {
                Assertions.assertEquals("Itemized", documentField.getValueString());
            } else if ("Subtotal".equals(key)) {
                Assertions.assertEquals(11.7f, documentField.getValueFloat());
            } else if ("Tax".equals(key)) {
                Assertions.assertEquals(1.17f, documentField.getValueFloat());
            } else if ("Tip".equals(key)) {
                Assertions.assertEquals(1.63f, documentField.getValueFloat());
            } else if ("TransactionDate".equals(key)) {
                Assertions.assertEquals(LocalDate.of(2019, 6, 10), documentField.getValueDate());
            } else if ("TransactionTime".equals(key)) {
                Assertions.assertEquals(LocalTime.of(13, 59), documentField.getValueTime());
            } else if ("Total".equals(key)) {
                Assertions.assertEquals(14.5f, documentField.getValueFloat());
            }
        });
    }

    private String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080" : AZURE_FORM_RECOGNIZER_ENDPOINT_CONFIGURATION;
    }
}
