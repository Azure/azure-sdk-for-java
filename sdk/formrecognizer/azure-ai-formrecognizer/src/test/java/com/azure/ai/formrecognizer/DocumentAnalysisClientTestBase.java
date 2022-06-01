// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.models.AddressValue;
import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.DocumentField;
import com.azure.ai.formrecognizer.models.DocumentFieldType;
import com.azure.ai.formrecognizer.models.DocumentPage;
import com.azure.ai.formrecognizer.models.DocumentSelectionMark;
import com.azure.ai.formrecognizer.models.DocumentTable;
import com.azure.ai.formrecognizer.models.FormRecognizerAudience;
import com.azure.ai.formrecognizer.models.LengthUnit;
import com.azure.ai.formrecognizer.models.Point;
import com.azure.ai.formrecognizer.models.SelectionMarkState;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.FluxUtil;
import com.azure.identity.AzureAuthorityHosts;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Assertions;
import reactor.test.StepVerifier;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.azure.ai.formrecognizer.TestUtils.AZURE_CLIENT_ID;
import static com.azure.ai.formrecognizer.TestUtils.AZURE_FORM_RECOGNIZER_CLIENT_SECRET;
import static com.azure.ai.formrecognizer.TestUtils.AZURE_FORM_RECOGNIZER_ENDPOINT_CONFIGURATION;
import static com.azure.ai.formrecognizer.TestUtils.AZURE_TENANT_ID;
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
                                                             DocumentAnalysisServiceVersion serviceVersion,
                                                             boolean useKeyCredential) {
        String endpoint = getEndpoint();
        FormRecognizerAudience audience = TestUtils.getAudience(endpoint);

        DocumentAnalysisClientBuilder builder = new DocumentAnalysisClientBuilder()
            .endpoint(endpoint)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy())
            .audience(audience);

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(INVALID_KEY));
        } else {
            if (useKeyCredential) {
                builder.credential(new AzureKeyCredential(TestUtils.AZURE_FORM_RECOGNIZER_API_KEY_CONFIGURATION));
            } else {
                builder.credential(getCredentialByAuthority(endpoint));
            }
        }
        return builder;
    }


    DocumentModelAdministrationClientBuilder getDocumentModelAdminClientBuilder(HttpClient httpClient,
                                                                                DocumentAnalysisServiceVersion serviceVersion,
                                                                                boolean useKeyCredential) {
        String endpoint = getEndpoint();
        FormRecognizerAudience audience = TestUtils.getAudience(endpoint);

        DocumentModelAdministrationClientBuilder builder = new DocumentModelAdministrationClientBuilder()
            .endpoint(endpoint)
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion)
            .addPolicy(interceptorManager.getRecordPolicy())
            .audience(audience);

        if (getTestMode() == TestMode.PLAYBACK) {
            builder.credential(new AzureKeyCredential(INVALID_KEY));
        } else {
            if (useKeyCredential) {
                builder.credential(new AzureKeyCredential(TestUtils.AZURE_FORM_RECOGNIZER_API_KEY_CONFIGURATION));
            } else {
                builder.credential(getCredentialByAuthority(endpoint));
            }
        }
        return builder;
    }

    static TokenCredential getCredentialByAuthority(String endpoint) {
        String authority = TestUtils.getAuthority(endpoint);
        if (Objects.equals(authority, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD)) {
            return new DefaultAzureCredentialBuilder()
                .authorityHost(TestUtils.getAuthority(endpoint))
                .build();
        } else {
            return new ClientSecretCredentialBuilder()
                .tenantId(AZURE_TENANT_ID)
                .clientId(AZURE_CLIENT_ID)
                .clientSecret(AZURE_FORM_RECOGNIZER_CLIENT_SECRET)
                .authorityHost(authority)
                .build();
        }
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
            Assertions.assertEquals("receipt.retailMeal", actualDocument.getDocType());
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
            Assertions.assertEquals("receipt.retailMeal", actualDocument.getDocType());
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
        assertEquals(213, page1.getSpans().get(0).getLength());
        assertEquals(214, page2.getSpans().get(0).getOffset());

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
                validateBoundingBoxData(documentLine.getBoundingPolygon());
                Assertions.assertNotNull(documentLine.getContent());
            });

            Assertions.assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getBoundingPolygon());
                // should be getContent()
                Assertions.assertNotNull(documentWord.getContent());
            });
        });
        Assertions.assertEquals(1, analyzeResult.getDocuments().size());

        Assertions.assertNotNull(analyzeResult.getPages());
        DocumentPage businessCardPage1 = analyzeResult.getPages().get(0);
        assertEquals(1, businessCardPage1.getPageNumber());

        final Map<String, DocumentField> actualBusinessCardFields = analyzeResult.getDocuments().get(0).getFields();
        assertEquals("2 Kingdom Street\nPaddington, London, W2 6BD",
            actualBusinessCardFields.get("Addresses").getValueList().get(0).getValueString());
        Assertions.assertNotNull(actualBusinessCardFields.get("Addresses").getValueList().get(0).getConfidence());
        assertEquals(EXPECTED_MERCHANT_NAME, actualBusinessCardFields.get("CompanyNames")
            .getValueList().get(0).getValueString());
        Assertions.assertNotNull(actualBusinessCardFields.get("CompanyNames").getValueList().get(0).getConfidence());
        assertEquals("Cloud & Al Department", actualBusinessCardFields.get("Departments")
            .getValueList().get(0).getValueString());
        Assertions.assertNotNull(actualBusinessCardFields.get("Departments").getValueList().get(0).getConfidence());
        assertEquals("avery.smith@contoso.com", actualBusinessCardFields.get("Emails")
            .getValueList().get(0).getValueString());
        Assertions.assertNotNull(actualBusinessCardFields.get("Emails").getValueList().get(0).getConfidence());
        assertEquals(DocumentFieldType.PHONE_NUMBER, actualBusinessCardFields.get("Faxes")
            .getValueList().get(0).getType());
        Assertions.assertNotNull(actualBusinessCardFields.get("Faxes").getValueList().get(0).getConfidence());
        assertEquals("Senior Researcher", actualBusinessCardFields.get("JobTitles")
            .getValueList().get(0).getValueString());
        Assertions.assertNotNull(actualBusinessCardFields.get("JobTitles").getValueList().get(0).getConfidence());
        assertEquals(DocumentFieldType.PHONE_NUMBER, actualBusinessCardFields.get("MobilePhones")
            .getValueList().get(0).getType());
        Assertions.assertNotNull(actualBusinessCardFields.get("MobilePhones").getValueList().get(0).getConfidence());
        assertEquals("https://www.contoso.com/", actualBusinessCardFields.get("Websites")
            .getValueList().get(0).getValueString());
        Assertions.assertNotNull(actualBusinessCardFields.get("Websites").getValueList().get(0).getConfidence());
        assertEquals(DocumentFieldType.PHONE_NUMBER, actualBusinessCardFields.get("WorkPhones")
            .getValueList().get(0).getType());
        Assertions.assertNotNull(actualBusinessCardFields.get("WorkPhones").getValueList().get(0).getConfidence());
        Map<String, DocumentField> contactNamesMap
            = actualBusinessCardFields.get("ContactNames").getValueList().get(0).getValueMap();
        // "FirstName" and "LastName" confidence returned as null by service, do we default?
        // Assertions.assertNotNull(contactNamesMap.get("FirstName").getConfidence());
        assertEquals("Avery", contactNamesMap.get("FirstName").getValueString());
        assertEquals("Smith", contactNamesMap.get("LastName").getValueString());
        // Assertions.assertNotNull(contactNamesMap.get("LastName").getConfidence());
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
        Assertions.assertNotNull(emailList.get(0).getConfidence());
        List<DocumentField> phoneNumberList = businessCard1Fields.get("WorkPhones").getValueList();
        Assertions.assertNotNull(phoneNumberList.get(0).getConfidence());
        assertEquals("+14257793479", phoneNumberList.get(0).getValuePhoneNumber());
        assertEquals(1, businessCard1.getPageNumber());

        // assert contact name page number
        DocumentField contactNameField = businessCard1Fields.get("ContactNames").getValueList().get(0);
        assertEquals("JOHN SINGER", contactNameField.getContent());
        Assertions.assertNotNull(contactNameField.getConfidence());

        assertEquals(2, businessCard2.getPageNumber());
        Map<String, DocumentField> businessCard2Fields = analyzeResult.getDocuments().get(1).getFields();
        List<DocumentField> email2List = businessCard2Fields.get("Emails").getValueList();
        assertEquals("avery.smith@contoso.com", email2List.get(0).getValueString());
        Assertions.assertNotNull(email2List.get(0).getConfidence());
        List<DocumentField> phoneNumber2List = businessCard2Fields.get("WorkPhones").getValueList();
        assertEquals("+44 (0) 20 9876 5432", phoneNumber2List.get(0).getContent());
        Assertions.assertNotNull(phoneNumber2List.get(0).getConfidence());

        // assert contact name page number
        DocumentField contactName2Field = businessCard2Fields.get("ContactNames").getValueList().get(0);
        assertEquals(2, contactName2Field.getBoundingRegions().get(0).getPageNumber());
        assertEquals("Dr. Avery Smith", contactName2Field.getContent());
        Assertions.assertNotNull(contactName2Field.getConfidence());
    }

    void validateInvoiceData(AnalyzeResult analyzeResult) {
        Assertions.assertEquals("prebuilt-invoice", analyzeResult.getModelId());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertNotNull(documentPage.getLines());
            documentPage.getLines().forEach(documentLine -> {
                validateBoundingBoxData(documentLine.getBoundingPolygon());
                Assertions.assertNotNull(documentLine.getContent());
            });

            Assertions.assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getBoundingPolygon());
                Assertions.assertNotNull(documentWord.getContent());
            });
        });

        assertEquals(1, analyzeResult.getPages().size());
        DocumentPage invoicePage1 = analyzeResult.getPages().get(0);

        assertEquals(1, invoicePage1.getPageNumber());

        Assertions.assertNotNull(analyzeResult.getDocuments());
        assertEquals(1, analyzeResult.getDocuments().size());
        Map<String, DocumentField> invoicePage1Fields = analyzeResult.getDocuments().get(0).getFields();
        assertEquals("1020 Enterprise Way\nSunnayvale, CA 87659", invoicePage1Fields.get("CustomerAddress")
            .getValueString());
        Assertions.assertNotNull(invoicePage1Fields.get("CustomerAddress").getConfidence());
        assertEquals("Microsoft", invoicePage1Fields.get("CustomerAddressRecipient")
            .getValueString());
        Assertions.assertNotNull(invoicePage1Fields.get("CustomerAddressRecipient").getConfidence());
        assertEquals("Microsoft", invoicePage1Fields.get("CustomerName")
            .getValueString());
        Assertions.assertNotNull(invoicePage1Fields.get("CustomerName").getConfidence());
        assertEquals(LocalDate.of(2017, 6, 24), invoicePage1Fields.get("DueDate")
            .getValueDate());
        Assertions.assertNotNull(invoicePage1Fields.get("DueDate").getConfidence());
        assertEquals(LocalDate.of(2017, 6, 18), invoicePage1Fields.get("InvoiceDate")
            .getValueDate());
        Assertions.assertNotNull(invoicePage1Fields.get("InvoiceDate").getConfidence());
        assertEquals("34278587", invoicePage1Fields.get("InvoiceId")
            .getValueString());
        Assertions.assertNotNull(invoicePage1Fields.get("InvoiceId").getConfidence());
        assertEquals("1 Redmond way Suite\n6000 Redmond, WA\n99243", invoicePage1Fields.get("VendorAddress")
            .getValueString());
        Assertions.assertNotNull(invoicePage1Fields.get("VendorAddress").getConfidence());
        assertEquals(EXPECTED_MERCHANT_NAME, invoicePage1Fields.get("VendorName")
            .getValueString());
        Assertions.assertNotNull(invoicePage1Fields.get("VendorName").getConfidence());

        Map<String, DocumentField> itemsMap
            = invoicePage1Fields.get("Items").getValueList().get(0).getValueMap();
        assertEquals(56651.49, itemsMap.get("Amount").getValueCurrency().getAmount());
        Assertions.assertNotNull(itemsMap.get("Amount").getConfidence());
        assertEquals(LocalDate.of(2017, 6, 18), itemsMap.get("Date").getValueDate());
        Assertions.assertNotNull(itemsMap.get("Date").getConfidence());
        assertEquals("34278587", itemsMap.get("ProductCode").getValueString());
        Assertions.assertNotNull(itemsMap.get("ProductCode").getConfidence());
        // assertEquals(DocumentFieldType.CURRENCY, itemsMap.get("Tax").getType());
        // Assertions.assertNotNull(itemsMap.get("Tax").getConfidence());
    }

    static void validateMultipageInvoiceData(AnalyzeResult analyzeResult) {
        assertEquals(2, analyzeResult.getPages().size());
        DocumentPage invoicePage1 = analyzeResult.getPages().get(0);

        assertEquals(1, invoicePage1.getPageNumber());
        assertEquals(1, analyzeResult.getDocuments().size());

        Map<String, DocumentField> recognizedInvoiceFields = analyzeResult.getDocuments().get(0).getFields();
        final DocumentField remittanceAddressRecipient = recognizedInvoiceFields.get("RemittanceAddressRecipient");
        Assertions.assertNotNull(recognizedInvoiceFields.get("RemittanceAddressRecipient").getConfidence());
        assertEquals("Contoso Ltd.", remittanceAddressRecipient.getValueString());
        assertEquals(1, remittanceAddressRecipient.getBoundingRegions().get(0).getPageNumber());

        final DocumentField remittanceAddress = recognizedInvoiceFields.get("RemittanceAddress");
        assertEquals("2345 Dogwood Lane\nBirch, Kansas 98123", remittanceAddress.getValueString());
        assertEquals(1, remittanceAddress.getBoundingRegions().get(0).getPageNumber());
        Assertions.assertNotNull(remittanceAddress.getConfidence());

        final DocumentField vendorName = recognizedInvoiceFields.get("VendorName");
        assertEquals("Southridge Video", vendorName.getValueString());
        assertEquals(2, vendorName.getBoundingRegions().get(0).getPageNumber());
        Assertions.assertNotNull(vendorName.getConfidence());
    }

    void validateIdentityData(AnalyzeResult analyzeResult) {
        Assertions.assertEquals("prebuilt-idDocument", analyzeResult.getModelId());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertNotNull(documentPage.getLines());
            documentPage.getLines().forEach(documentLine -> {
                validateBoundingBoxData(documentLine.getBoundingPolygon());
                Assertions.assertNotNull(documentLine.getContent());
            });

            Assertions.assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getBoundingPolygon());
                // should be getContent()
                Assertions.assertNotNull(documentWord.getContent());
            });
        });

        assertEquals(1, analyzeResult.getPages().size());
        DocumentPage licensePage1 = analyzeResult.getPages().get(0);
        assertEquals(1, licensePage1.getPageNumber());

        Assertions.assertNotNull(analyzeResult.getDocuments());
        assertEquals("idDocument.driverLicense", analyzeResult.getDocuments().get(0).getDocType());
        Map<String, DocumentField> licensePageFields = analyzeResult.getDocuments().get(0).getFields();
        assertEquals("Main Street , Charleston,\n WV 456789", licensePageFields.get("Address")
            .getValueString());
        Assertions.assertNotNull(licensePageFields.get("Address").getConfidence());
        assertEquals("USA", licensePageFields.get("CountryRegion").getValueCountryRegion());
        Assertions.assertNotNull(licensePageFields.get("CountryRegion").getConfidence());
        assertEquals(LocalDate.of(1958, 1, 6), licensePageFields.get("DateOfBirth")
            .getValueDate());
        Assertions.assertNotNull(licensePageFields.get("DateOfBirth").getConfidence());
        assertEquals(LocalDate.of(2020, 8, 12), licensePageFields.get("DateOfExpiration")
            .getValueDate());
        Assertions.assertNotNull(licensePageFields.get("DateOfExpiration").getConfidence());
        assertEquals("WDLABCD456DG", licensePageFields.get("DocumentNumber")
            .getValueString());
        Assertions.assertNotNull(licensePageFields.get("DocumentNumber").getConfidence());
        assertEquals("LIAM R.", licensePageFields.get("FirstName").getValueString());
        Assertions.assertNotNull(licensePageFields.get("FirstName").getConfidence());
        assertEquals("TALBOT", licensePageFields.get("LastName").getValueString());
        Assertions.assertNotNull(licensePageFields.get("LastName").getConfidence());
        assertEquals("Washington", licensePageFields.get("Region").getValueString());
        Assertions.assertNotNull(licensePageFields.get("Region").getConfidence());
        assertEquals("M", licensePageFields.get("Sex").getValueString());
        Assertions.assertNotNull(licensePageFields.get("Sex").getConfidence());
        assertEquals("L", licensePageFields.get("Endorsements").getValueString());
        Assertions.assertNotNull(licensePageFields.get("Endorsements").getConfidence());
        assertEquals("B", licensePageFields.get("Restrictions").getValueString());
        Assertions.assertNotNull(licensePageFields.get("Restrictions").getConfidence());
    }

    void validateGermanContentData(AnalyzeResult analyzeResult) {
        Assertions.assertNotNull(analyzeResult.getPages());
        assertEquals(1, analyzeResult.getPages().size());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertTrue(
                documentPage.getAngle() > -180.0 && documentPage.getAngle() < 180.0);
            Assertions.assertNotNull(analyzeResult.getTables());
            Assertions.assertEquals(8.5f, documentPage.getWidth());
            Assertions.assertEquals(11f, documentPage.getHeight());
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
            Assertions.assertEquals(8.5f, documentPage.getWidth());
            Assertions.assertEquals(11f, documentPage.getHeight());
            Assertions.assertEquals(LengthUnit.INCH, documentPage.getUnit());

            Assertions.assertNotNull(documentPage.getSelectionMarks());
            Assertions.assertEquals(3, documentPage.getSelectionMarks().size());
            List<DocumentSelectionMark> selectionMarks = documentPage.getSelectionMarks();
            for (int i = 0; i < selectionMarks.size(); i++) {
                DocumentSelectionMark documentSelectionMark = selectionMarks.get(i);
                validateBoundingBoxData(documentSelectionMark.getBoundingPolygon());
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
        // TODO (service team): integration bug
        // Assertions.assertNotNull(analyzeResult.getStyles());
        assertEquals(1, analyzeResult.getStyles().size());
        assertNull(analyzeResult.getDocuments());
    }

    void validatePdfContentData(AnalyzeResult analyzeResult) {
        Assertions.assertNotNull(analyzeResult.getPages());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertTrue(
                documentPage.getAngle() > -180.0 && documentPage.getAngle() < 180.0);
            Assertions.assertNotNull(analyzeResult.getTables());
            Assertions.assertEquals(8.5f, documentPage.getWidth());
            Assertions.assertEquals(11f, documentPage.getHeight());
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
        Assertions.assertNotNull(analyzeResult.getStyles());
        assertEquals(1, analyzeResult.getStyles().size());
        assertNull(analyzeResult.getDocuments());
    }

    void validateDocumentPage(DocumentPage documentPage) {
        Assertions.assertNotNull(documentPage.getLines());
        documentPage.getLines().forEach(documentLine -> {
            validateBoundingBoxData(documentLine.getBoundingPolygon());
            Assertions.assertNotNull(documentLine.getContent());
        });

        Assertions.assertNotNull(documentPage.getWords());
        documentPage.getWords().forEach(documentWord -> {
            validateBoundingBoxData(documentWord.getBoundingPolygon());
            Assertions.assertNotNull(documentWord.getContent());
        });
    }

    void validateMultipageLayoutContent(AnalyzeResult analyzeResult) {
        Assertions.assertNotNull(analyzeResult.getPages());
        List<DocumentPage> pages = analyzeResult.getPages();
        for (int i = 0; i < pages.size(); i++) {
            DocumentPage documentPage = pages.get(i);
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
        assertNull(analyzeResult.getStyles());
        assertNull(analyzeResult.getDocuments());
    }

    void validateJpegCustomDocument(AnalyzeResult actualAnalyzeResult) {
        List<DocumentPage> documentPages = actualAnalyzeResult.getPages();
        Assertions.assertEquals(1, documentPages.size());
        documentPages.forEach(this::validateDocumentPage);
        int[][] table = new int[][] {{5, 4, 20}, {4, 2, 6}};
        Assertions.assertEquals(2, actualAnalyzeResult.getTables().size());
        for (int i = 0; i < actualAnalyzeResult.getTables().size(); i++) {
            int j = 0;
            DocumentTable actualDocumentTable = actualAnalyzeResult.getTables().get(i);
            Assertions.assertEquals(table[i][j], actualDocumentTable.getRowCount());
            Assertions.assertEquals(table[i][++j], actualDocumentTable.getColumnCount());
            Assertions.assertEquals(table[i][++j], actualDocumentTable.getCells().size());
        }

        actualAnalyzeResult.getDocuments().forEach(actualDocument -> {
            // Assertions.assertEquals(modelId, actualDocument.getDocType());
            actualDocument.getFields().forEach((key, documentField) -> {
                // document fields
                Assertions.assertNotNull(documentField.getConfidence());
                if ("Tax".equals(key)) {
                    assertEquals("$4.00", documentField.getValueString());
                }
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
                } else if ("Merchant".equals(key)) {
                    assertEquals("Hero Limited", documentField.getValueString());
                } else if ("PurchaseOrderNumber".equals(key)) {
                    assertEquals("948284", documentField.getValueString());
                } else if ("CompanyAddress".equals(key)) {
                    assertEquals("938 NE Burner Road Boulder City, CO 92848",
                        documentField.getValueString());
                } else if ("Subtotal".equals(key)) {
                    assertEquals("$140.00", documentField.getValueString());
                }
            });
        });
    }

    void validateMultiPagePdfData(AnalyzeResult analyzeResult) {
        assertEquals(3, analyzeResult.getPages().size());
        analyzeResult.getDocuments().forEach(analyzedDocument -> {
            analyzedDocument.getFields().forEach((key, documentField) -> {
                Assertions.assertNotNull(documentField.getType());
                Assertions.assertNotNull(documentField.getConfidence());
            });
        });
    }

    void validateCustomDocumentWithSelectionMarks(AnalyzeResult analyzeResult) {
        Assertions.assertEquals(1, analyzeResult.getPages().size());
        analyzeResult.getPages().forEach(actualDocumentPage -> {
            Assertions.assertEquals(8.5f, actualDocumentPage.getWidth());
            Assertions.assertEquals(11f, actualDocumentPage.getHeight());
            Assertions.assertEquals(LengthUnit.INCH, actualDocumentPage.getUnit());

            validateDocumentPage(actualDocumentPage);
        });
        assertNull(analyzeResult.getTables());

        analyzeResult.getDocuments().forEach(actualDocument ->
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
            }));
    }

    void validateW2Data(AnalyzeResult analyzeResult) {
        Assertions.assertEquals("prebuilt-tax.us.w2", analyzeResult.getModelId());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertNotNull(documentPage.getLines());
            documentPage.getLines().forEach(documentLine -> {
                validateBoundingBoxData(documentLine.getBoundingPolygon());
                Assertions.assertNotNull(documentLine.getContent());
            });

            Assertions.assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getBoundingPolygon());
                // should be getContent()
                Assertions.assertNotNull(documentWord.getContent());
            });
        });

        assertEquals(1, analyzeResult.getPages().size());
        DocumentPage licensePage1 = analyzeResult.getPages().get(0);
        assertEquals(1, licensePage1.getPageNumber());

        Assertions.assertNotNull(analyzeResult.getDocuments());
        assertEquals("tax.us.w2", analyzeResult.getDocuments().get(0).getDocType());
        Map<String, DocumentField> w2Fields = analyzeResult.getDocuments().get(0).getFields();

        Map<String, DocumentField> employeeFields = w2Fields.get("Employee").getValueMap();
        AddressValue employeeAddrFields = employeeFields.get("Address")
            .getValueAddress();
        assertEquals("ne", employeeAddrFields.getState());
        assertEquals("98631-5293", employeeAddrFields.getPostalCode());
        assertEquals("kathrynmouth", employeeAddrFields.getCity());
        assertEquals("96541 molly hollow street", employeeAddrFields.getStreetAddress());
        assertEquals("96541", employeeAddrFields.getHouseNumber());
        assertEquals("kathrynmouth", employeeAddrFields.getCity());
        assertEquals("BONNIE F HERNANDEZ", employeeFields.get("Name")
            .getValueString());
        assertEquals("986-62-1002", employeeFields.get("SocialSecurityNumber")
            .getValueString());

        Map<String, DocumentField> employerFields = w2Fields.get("Employer").getValueMap();
        AddressValue employerAddress = employerFields.get("Address").getValueAddress();
        assertEquals("ks", employerAddress.getState());
        assertEquals("po box 856", employerAddress.getPoBox());
        assertEquals("67402-0856", employerAddress.getPostalCode());
        assertEquals("salina", employerAddress.getCity());
        assertEquals("BLUE BEACON USA, LP", employerFields.get("Name")
            .getValueString());
        assertEquals("48-1069918", employerFields.get("IdNumber")
            .getValueString());

        Assertions.assertEquals(3894.54f, w2Fields.get("FederalIncomeTaxWithheld").getValueFloat());
        assertEquals(9873.2f, w2Fields.get("DependentCareBenefits").getValueFloat());

        List<DocumentField> localTaxInfoFieldsList = w2Fields.get("LocalTaxInfos").getValueList();
        Map<String, DocumentField> localTaxInfoFields1 = localTaxInfoFieldsList.get(0).getValueMap();
        Map<String, DocumentField> localTaxInfoFields2 = localTaxInfoFieldsList.get(1).getValueMap();

        assertEquals(51f, localTaxInfoFields1.get("LocalIncomeTax").getValueFloat());
        assertEquals("Cmberland Vly/ Mddl", localTaxInfoFields1.get("LocalityName").getValueString());
        assertEquals(37160.56f, localTaxInfoFields1.get("LocalWagesTipsEtc").getValueFloat());

        assertEquals(594.54f, localTaxInfoFields2.get("LocalIncomeTax").getValueFloat());
        assertEquals("E.Pennsboro/E.Pnns", localTaxInfoFields2.get("LocalityName").getValueString());
        assertEquals(37160.56f, localTaxInfoFields2.get("LocalWagesTipsEtc").getValueFloat());

        Assertions.assertEquals(538.83f, w2Fields.get("MedicareTaxWithheld").getValueFloat());
        assertEquals(37160.56f, w2Fields.get("MedicareWagesAndTips").getValueFloat());
        Assertions.assertEquals(653.21f, w2Fields.get("NonQualifiedPlans").getValueFloat());
        assertEquals(2303.95f, w2Fields.get("SocialSecurityTaxWithheld").getValueFloat());
        Assertions.assertEquals(302.3f, w2Fields.get("SocialSecurityTips").getValueFloat());
        assertEquals(37160.56f, w2Fields.get("SocialSecurityWages").getValueFloat());

        List<DocumentField> stateTaxInfoFieldsList = w2Fields.get("StateTaxInfos").getValueList();
        Map<String, DocumentField> stateTaxInfoFields1 = stateTaxInfoFieldsList.get(0).getValueMap();
        Map<String, DocumentField> stateTaxInfoFields2 = stateTaxInfoFieldsList.get(1).getValueMap();

        assertEquals("18574095 18743231", stateTaxInfoFields1.get("EmployerStateIdNumber")
            .getValueString());
        assertEquals("PA WA", stateTaxInfoFields1.get("State")
            .getValueString());
        assertEquals(1135.65f, stateTaxInfoFields1.get("StateIncomeTax").getValueFloat());

        assertEquals(37160.56f, stateTaxInfoFields1.get("StateWagesTipsEtc").getValueFloat());

        assertEquals(1032.3f, stateTaxInfoFields2.get("StateIncomeTax").getValueFloat());
        assertEquals(9631.2f, stateTaxInfoFields2.get("StateWagesTipsEtc").getValueFloat());

        Assertions.assertEquals("2018", w2Fields.get("TaxYear").getValueString());
        assertEquals("W-2", w2Fields.get("W2FormVariant").getValueString());
        assertEquals(37160.56f, w2Fields.get("WagesTipsAndOtherCompensation").getValueFloat());
    }

    private void validateBoundingBoxData(List<Point> points) {
        Assertions.assertNotNull(points);
        assertEquals(4, points.size());
    }

    private void validatePngReceiptFields(Map<String, DocumentField> actualFields) {
        Assertions.assertEquals("+11234567890", actualFields.get("MerchantPhoneNumber").getValuePhoneNumber());
        Assertions.assertNotNull(actualFields.get("Subtotal").getValueFloat());
        Assertions.assertNotNull(actualFields.get("Total").getValueFloat());
        Assertions.assertNotNull(actualFields.get("Tax").getValueFloat());
        Assertions.assertNotNull(actualFields.get("Tax").getConfidence());
        Assertions.assertNotNull(actualFields.get("Subtotal").getConfidence());
        Assertions.assertNotNull(actualFields.get("Total").getConfidence());
        Assertions.assertNotNull(actualFields.get("Items"));
        List<DocumentField> itemizedItems = actualFields.get("Items").getValueList();

        for (int i = 0; i < itemizedItems.size(); i++) {
            if (itemizedItems.get(i).getContent() != null) {
                String[] itemizedNames = new String[] {"Surface Pro 6", "SurfacePen"};
                Float[] itemizedTotalPrices = new Float[] {999f, 99.99f};

                Map<String, DocumentField> actualReceiptItems = itemizedItems.get(i).getValueMap();
                int finalI = i;
                actualReceiptItems.forEach((key, documentField) -> {
                    Assertions.assertNotNull(documentField.getConfidence());
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
        Assertions.assertNotNull(actualAnalyzeResult.getPages());
    }

    private void validateJpegReceiptFields(Map<String, DocumentField> actualFields) {
        actualFields.forEach((key, documentField) -> {
            if (documentField.getBoundingRegions() != null) {
                Assertions.assertEquals(1, documentField.getBoundingRegions().get(0).getPageNumber());
            }
            if ("Locale".equals(key)) {
                Assertions.assertEquals("en-US", documentField.getValueString());
                Assertions.assertNotNull(documentField.getConfidence());
            } else if ("MerchantAddress".equals(key)) {
                Assertions.assertEquals("123 Main Street Redmond, WA 98052", documentField.getValueString());
                Assertions.assertNotNull(documentField.getConfidence());
            } else if ("MerchantName".equals(key)) {
                Assertions.assertEquals("Contoso", documentField.getValueString());
                Assertions.assertNotNull(documentField.getConfidence());
            } else if ("MerchantPhoneNumber".equals(key)) {
                Assertions.assertEquals("+19876543210", documentField.getValuePhoneNumber());
                Assertions.assertNotNull(documentField.getConfidence());
            } else if ("ReceiptType".equals(key)) {
                Assertions.assertEquals("Itemized", documentField.getValueString());
                Assertions.assertNotNull(documentField.getConfidence());
            } else if ("Subtotal".equals(key)) {
                Assertions.assertEquals(11.7f, documentField.getValueFloat());
                Assertions.assertNotNull(documentField.getConfidence());
            } else if ("Tax".equals(key)) {
                Assertions.assertEquals(1.17f, documentField.getValueFloat());
                Assertions.assertNotNull(documentField.getConfidence());
            } else if ("Tip".equals(key)) {
                Assertions.assertEquals(1.63f, documentField.getValueFloat());
                Assertions.assertNotNull(documentField.getConfidence());
            } else if ("TransactionDate".equals(key)) {
                Assertions.assertEquals(LocalDate.of(2019, 6, 10), documentField.getValueDate());
                Assertions.assertNotNull(documentField.getConfidence());
            } else if ("TransactionTime".equals(key)) {
                Assertions.assertEquals(LocalTime.of(13, 59), documentField.getValueTime());
                Assertions.assertNotNull(documentField.getConfidence());
            } else if ("Total".equals(key)) {
                Assertions.assertEquals(14.5f, documentField.getValueFloat());
                Assertions.assertNotNull(documentField.getConfidence());
            }
        });
    }

    private String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080" : AZURE_FORM_RECOGNIZER_ENDPOINT_CONFIGURATION;
    }
}
