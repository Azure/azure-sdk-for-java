// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis;

import com.azure.ai.formrecognizer.documentanalysis.administration.DocumentModelAdministrationClientBuilder;
import com.azure.ai.formrecognizer.documentanalysis.models.AddressValue;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnalysisAudience;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentField;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFieldType;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPage;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPageLengthUnit;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSelectionMark;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSelectionMarkState;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentTable;
import com.azure.ai.formrecognizer.documentanalysis.models.Point;
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

import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.AZURE_CLIENT_ID;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.AZURE_FORM_RECOGNIZER_CLIENT_SECRET;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.AZURE_FORM_RECOGNIZER_ENDPOINT_CONFIGURATION;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.AZURE_TENANT_ID;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.EXPECTED_MERCHANT_NAME;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.INVALID_KEY;
import static com.azure.ai.formrecognizer.documentanalysis.TestUtils.ONE_NANO_DURATION;
import static com.azure.ai.formrecognizer.documentanalysis.implementation.util.Constants.DEFAULT_POLL_INTERVAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    public DocumentAnalysisClientBuilder getDocumentAnalysisBuilder(HttpClient httpClient,
                                                             DocumentAnalysisServiceVersion serviceVersion,
                                                             boolean useKeyCredential) {
        String endpoint = getEndpoint();
        DocumentAnalysisAudience audience = TestUtils.getAudience(endpoint);

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


    public DocumentModelAdministrationClientBuilder getDocumentModelAdminClientBuilder(HttpClient httpClient,
                                                                                DocumentAnalysisServiceVersion serviceVersion,
                                                                                boolean useKeyCredential) {
        String endpoint = getEndpoint();
        DocumentAnalysisAudience audience = TestUtils.getAudience(endpoint);

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

    void beginClassifierRunner(Consumer<String> testRunner) {
        TestUtils.getClassifierTrainingDataContainerHelper(testRunner, interceptorManager.isPlaybackMode());
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
        assertEquals(216, page1.getSpans().get(0).getLength());
        assertEquals(217, page2.getSpans().get(0).getOffset());

        DocumentPage receiptPage1 = analyzeResult.getPages().get(0);
        DocumentPage receiptPage2 = analyzeResult.getPages().get(1);

        assertEquals(1, receiptPage1.getPageNumber());
        assertEquals(2, analyzeResult.getDocuments().size());
        Map<String, DocumentField> receiptPage1Fields = analyzeResult.getDocuments().get(0).getFields();
        validateJpegReceiptFields(receiptPage1Fields);

        assertNotNull(analyzeResult.getDocuments().get(1).getFields());
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
            assertNotNull(documentPage.getLines());
            documentPage.getLines().forEach(documentLine -> {
                validateBoundingBoxData(documentLine.getBoundingPolygon());
                assertNotNull(documentLine.getContent());
            });

            assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getBoundingPolygon());
                // should be getContent()
                assertNotNull(documentWord.getContent());
            });
        });
        Assertions.assertEquals(1, analyzeResult.getDocuments().size());

        assertNotNull(analyzeResult.getPages());
        DocumentPage businessCardPage1 = analyzeResult.getPages().get(0);
        assertEquals(1, businessCardPage1.getPageNumber());

        final Map<String, DocumentField> actualBusinessCardFields = analyzeResult.getDocuments().get(0).getFields();
        assertEquals("2 Kingdom Street",
            actualBusinessCardFields.get("Addresses").getValueAsList().get(0).getValueAsAddress().getStreetAddress());
        assertNotNull(actualBusinessCardFields.get("Addresses").getValueAsList().get(0).getConfidence());
        assertEquals(EXPECTED_MERCHANT_NAME, actualBusinessCardFields.get("CompanyNames")
            .getValueAsList().get(0).getValueAsString());
        assertNotNull(actualBusinessCardFields.get("CompanyNames").getValueAsList().get(0).getConfidence());
        assertEquals("Cloud & Al Department", actualBusinessCardFields.get("Departments")
            .getValueAsList().get(0).getValueAsString());
        assertNotNull(actualBusinessCardFields.get("Departments").getValueAsList().get(0).getConfidence());
        assertEquals("avery.smith@contoso.com", actualBusinessCardFields.get("Emails")
            .getValueAsList().get(0).getValueAsString());
        assertNotNull(actualBusinessCardFields.get("Emails").getValueAsList().get(0).getConfidence());
        assertEquals(DocumentFieldType.PHONE_NUMBER, actualBusinessCardFields.get("Faxes")
            .getValueAsList().get(0).getType());
        assertNotNull(actualBusinessCardFields.get("Faxes").getValueAsList().get(0).getConfidence());
        assertEquals("Senior Researcher", actualBusinessCardFields.get("JobTitles")
            .getValueAsList().get(0).getValueAsString());
        assertNotNull(actualBusinessCardFields.get("JobTitles").getValueAsList().get(0).getConfidence());
        assertEquals(DocumentFieldType.PHONE_NUMBER, actualBusinessCardFields.get("MobilePhones")
            .getValueAsList().get(0).getType());
        assertNotNull(actualBusinessCardFields.get("MobilePhones").getValueAsList().get(0).getConfidence());
        assertEquals("https://www.contoso.com/", actualBusinessCardFields.get("Websites")
            .getValueAsList().get(0).getValueAsString());
        assertNotNull(actualBusinessCardFields.get("Websites").getValueAsList().get(0).getConfidence());
        assertEquals(DocumentFieldType.PHONE_NUMBER, actualBusinessCardFields.get("WorkPhones")
            .getValueAsList().get(0).getType());
        assertNotNull(actualBusinessCardFields.get("WorkPhones").getValueAsList().get(0).getConfidence());
        Map<String, DocumentField> contactNamesMap
            = actualBusinessCardFields.get("ContactNames").getValueAsList().get(0).getValueAsMap();
        // "FirstName" and "LastName" confidence returned as null by service, do we default?
        // Assertions.assertNotNull(contactNamesMap.get("FirstName").getConfidence());
        assertEquals("Avery", contactNamesMap.get("FirstName").getValueAsString());
        assertEquals("Smith", contactNamesMap.get("LastName").getValueAsString());
        // Assertions.assertNotNull(contactNamesMap.get("LastName").getConfidence());
    }

    static void validateMultipageBusinessData(AnalyzeResult analyzeResult) {
        assertEquals(2, analyzeResult.getPages().size());
        assertEquals(2, analyzeResult.getDocuments().size());
        DocumentPage businessCard1 = analyzeResult.getPages().get(0);
        DocumentPage businessCard2 = analyzeResult.getPages().get(1);

        assertEquals(1, businessCard1.getPageNumber());
        Map<String, DocumentField> businessCard1Fields = analyzeResult.getDocuments().get(0).getFields();
        List<DocumentField> emailList = businessCard1Fields.get("Emails").getValueAsList();
        assertEquals("johnsinger@contoso.com", emailList.get(0).getValueAsString());
        assertNotNull(emailList.get(0).getConfidence());
        List<DocumentField> phoneNumberList = businessCard1Fields.get("WorkPhones").getValueAsList();
        assertNotNull(phoneNumberList.get(0).getConfidence());
        assertEquals("+14257793479", phoneNumberList.get(0).getValueAsPhoneNumber());
        assertEquals(1, businessCard1.getPageNumber());

        // assert contact name page number
        DocumentField contactNameField = businessCard1Fields.get("ContactNames").getValueAsList().get(0);
        assertEquals("JOHN\nSINGER", contactNameField.getContent());
        assertNotNull(contactNameField.getConfidence());

        assertEquals(2, businessCard2.getPageNumber());
        Map<String, DocumentField> businessCard2Fields = analyzeResult.getDocuments().get(1).getFields();
        List<DocumentField> email2List = businessCard2Fields.get("Emails").getValueAsList();
        assertEquals("avery.smith@contoso.com", email2List.get(0).getValueAsString());
        assertNotNull(email2List.get(0).getConfidence());
        List<DocumentField> phoneNumber2List = businessCard2Fields.get("WorkPhones").getValueAsList();
        assertEquals("+44 (0) 20 9876 5432", phoneNumber2List.get(0).getContent());
        assertNotNull(phoneNumber2List.get(0).getConfidence());

        // assert contact name page number
        DocumentField contactName2Field = businessCard2Fields.get("ContactNames").getValueAsList().get(0);
        assertEquals(2, contactName2Field.getBoundingRegions().get(0).getPageNumber());
        assertEquals("Dr. Avery Smith", contactName2Field.getContent());
        assertNotNull(contactName2Field.getConfidence());
    }

    void validateInvoiceData(AnalyzeResult analyzeResult) {
        Assertions.assertEquals("prebuilt-invoice", analyzeResult.getModelId());
        analyzeResult.getPages().forEach(documentPage -> {
            assertNotNull(documentPage.getLines());
            documentPage.getLines().forEach(documentLine -> {
                validateBoundingBoxData(documentLine.getBoundingPolygon());
                assertNotNull(documentLine.getContent());
            });

            assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getBoundingPolygon());
                assertNotNull(documentWord.getContent());
            });
        });

        assertEquals(1, analyzeResult.getPages().size());
        DocumentPage invoicePage1 = analyzeResult.getPages().get(0);

        assertEquals(1, invoicePage1.getPageNumber());

        assertNotNull(analyzeResult.getDocuments());
        assertEquals(1, analyzeResult.getDocuments().size());
        Map<String, DocumentField> invoicePage1Fields = analyzeResult.getDocuments().get(0).getFields();
        assertEquals("1020 Enterprise Way\n"
            + "Sunnayvale, CA 87659", invoicePage1Fields.get("CustomerAddress")
            .getValueAsAddress().getStreetAddress());
        assertNotNull(invoicePage1Fields.get("CustomerAddress").getConfidence());
        assertEquals("Microsoft", invoicePage1Fields.get("CustomerAddressRecipient")
            .getValueAsString());
        assertNotNull(invoicePage1Fields.get("CustomerAddressRecipient").getConfidence());
        assertEquals("Microsoft", invoicePage1Fields.get("CustomerName")
            .getValueAsString());
        assertNotNull(invoicePage1Fields.get("CustomerName").getConfidence());
        assertEquals(LocalDate.of(2017, 6, 24), invoicePage1Fields.get("DueDate")
            .getValueAsDate());
        assertNotNull(invoicePage1Fields.get("DueDate").getConfidence());
        assertEquals(LocalDate.of(2017, 6, 18), invoicePage1Fields.get("InvoiceDate")
            .getValueAsDate());
        assertNotNull(invoicePage1Fields.get("InvoiceDate").getConfidence());
        assertEquals("34278587", invoicePage1Fields.get("InvoiceId")
            .getValueAsString());
        assertNotNull(invoicePage1Fields.get("InvoiceId").getConfidence());
        assertEquals("1 Redmond way Suite\n6000", invoicePage1Fields.get("VendorAddress")
            .getValueAsAddress().getStreetAddress());
        assertNotNull(invoicePage1Fields.get("VendorAddress").getConfidence());
        assertEquals(EXPECTED_MERCHANT_NAME, invoicePage1Fields.get("VendorName")
            .getValueAsString());
        assertNotNull(invoicePage1Fields.get("VendorName").getConfidence());

        Map<String, DocumentField> itemsMap
            = invoicePage1Fields.get("Items").getValueAsList().get(0).getValueAsMap();
        assertEquals(56651.49, itemsMap.get("Amount").getValueAsCurrency().getAmount());
        assertNotNull(itemsMap.get("Amount").getConfidence());
        assertEquals(LocalDate.of(2017, 6, 18), itemsMap.get("Date").getValueAsDate());
        assertNotNull(itemsMap.get("Date").getConfidence());
        assertEquals("34278587", itemsMap.get("ProductCode").getValueAsString());
        assertNotNull(itemsMap.get("ProductCode").getConfidence());
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
        assertNotNull(recognizedInvoiceFields.get("RemittanceAddressRecipient").getConfidence());
        assertEquals("Contoso Ltd.", remittanceAddressRecipient.getValueAsString());
        assertEquals(1, remittanceAddressRecipient.getBoundingRegions().get(0).getPageNumber());

        final DocumentField remittanceAddress = recognizedInvoiceFields.get("RemittanceAddress");
        assertEquals("2345 Dogwood Lane", remittanceAddress.getValueAsAddress().getStreetAddress());
        assertEquals(1, remittanceAddress.getBoundingRegions().get(0).getPageNumber());
        assertNotNull(remittanceAddress.getConfidence());

        final DocumentField vendorName = recognizedInvoiceFields.get("VendorName");
        assertEquals("Southridge Video", vendorName.getValueAsString());
        assertEquals(2, vendorName.getBoundingRegions().get(0).getPageNumber());
        assertNotNull(vendorName.getConfidence());
    }

    void validateIdentityData(AnalyzeResult analyzeResult) {
        Assertions.assertEquals("prebuilt-idDocument", analyzeResult.getModelId());
        analyzeResult.getPages().forEach(documentPage -> {
            assertNotNull(documentPage.getLines());
            documentPage.getLines().forEach(documentLine -> {
                validateBoundingBoxData(documentLine.getBoundingPolygon());
                assertNotNull(documentLine.getContent());
            });

            assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getBoundingPolygon());
                // should be getContent()
                assertNotNull(documentWord.getContent());
            });
        });

        assertEquals(1, analyzeResult.getPages().size());
        DocumentPage licensePage1 = analyzeResult.getPages().get(0);
        assertEquals(1, licensePage1.getPageNumber());

        assertNotNull(analyzeResult.getDocuments());
        assertEquals("idDocument.driverLicense", analyzeResult.getDocuments().get(0).getDocType());
        Map<String, DocumentField> licensePageFields = analyzeResult.getDocuments().get(0).getFields();
        assertEquals("Main Street", licensePageFields.get("Address")
            .getValueAsAddress().getStreetAddress());
        assertNotNull(licensePageFields.get("Address").getConfidence());
        assertEquals("USA", licensePageFields.get("CountryRegion").getValueAsCountry());
        assertNotNull(licensePageFields.get("CountryRegion").getConfidence());
        assertEquals(LocalDate.of(1988, 3, 23), licensePageFields.get("DateOfBirth")
            .getValueAsDate());
        assertNotNull(licensePageFields.get("DateOfBirth").getConfidence());
        assertEquals(LocalDate.of(2026, 3, 23), licensePageFields.get("DateOfExpiration")
            .getValueAsDate());
        assertNotNull(licensePageFields.get("DateOfExpiration").getConfidence());
        assertEquals("034568", licensePageFields.get("DocumentNumber")
            .getValueAsString());
        assertNotNull(licensePageFields.get("DocumentNumber").getConfidence());
        assertEquals("CHRIS", licensePageFields.get("FirstName").getValueAsString());
        assertNotNull(licensePageFields.get("FirstName").getConfidence());
        assertEquals("SMITH", licensePageFields.get("LastName").getValueAsString());
        assertNotNull(licensePageFields.get("LastName").getConfidence());
        assertEquals("West Virginia", licensePageFields.get("Region").getValueAsString());
        assertNotNull(licensePageFields.get("Region").getConfidence());
        assertEquals("M", licensePageFields.get("Sex").getValueAsString());
        assertNotNull(licensePageFields.get("Sex").getConfidence());
        assertEquals("NONE", licensePageFields.get("Endorsements").getValueAsString());
        assertNotNull(licensePageFields.get("Endorsements").getConfidence());
        assertEquals("NONE", licensePageFields.get("Restrictions").getValueAsString());
        assertNotNull(licensePageFields.get("Restrictions").getConfidence());
    }

    void validateGermanContentData(AnalyzeResult analyzeResult) {
        assertNotNull(analyzeResult.getPages());
        assertEquals(1, analyzeResult.getPages().size());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertTrue(
                documentPage.getAngle() > -180.0 && documentPage.getAngle() < 180.0);
            assertNotNull(analyzeResult.getTables());
            Assertions.assertEquals(8.5f, documentPage.getWidth());
            Assertions.assertEquals(11f, documentPage.getHeight());
            Assertions.assertEquals(DocumentPageLengthUnit.INCH, documentPage.getUnit());

        });

        assertNotNull(analyzeResult.getTables());
        int[] table = new int[] {8, 3, 24};
        Assertions.assertEquals(1, analyzeResult.getTables().size());
        for (int i = 0; i < analyzeResult.getTables().size(); i++) {
            DocumentTable actualDocumentTable = analyzeResult.getTables().get(i);
            Assertions.assertEquals(table[i], actualDocumentTable.getRowCount());
            Assertions.assertEquals(table[++i], actualDocumentTable.getColumnCount());
            Assertions.assertEquals(table[++i], actualDocumentTable.getCells().size());
            actualDocumentTable.getCells().forEach(documentTableCell
                -> assertNotNull(documentTableCell.getKind()));
        }
    }

    void validateSelectionMarkContentData(AnalyzeResult analyzeResult) {
        assertNotNull(analyzeResult.getPages());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertTrue(
                documentPage.getAngle() > -180.0 && documentPage.getAngle() < 180.0);
            Assertions.assertEquals(8.5f, documentPage.getWidth());
            Assertions.assertEquals(11f, documentPage.getHeight());
            Assertions.assertEquals(DocumentPageLengthUnit.INCH, documentPage.getUnit());

            assertNotNull(documentPage.getSelectionMarks());
            Assertions.assertEquals(3, documentPage.getSelectionMarks().size());
            List<DocumentSelectionMark> selectionMarks = documentPage.getSelectionMarks();
            for (int i = 0; i < selectionMarks.size(); i++) {
                DocumentSelectionMark documentSelectionMark = selectionMarks.get(i);
                validateBoundingBoxData(documentSelectionMark.getBoundingPolygon());
                assertNotNull(documentSelectionMark.getSelectionMarkState());
                if (i == 0) {
                    Assertions.assertEquals(DocumentSelectionMarkState.UNSELECTED, documentSelectionMark.getSelectionMarkState());
                } else if (i == 1) {
                    assertEquals(DocumentSelectionMarkState.SELECTED, documentSelectionMark.getSelectionMarkState());
                } else {
                    assertEquals(DocumentSelectionMarkState.UNSELECTED, documentSelectionMark.getSelectionMarkState());
                }
            }
        });
        assertNull(analyzeResult.getKeyValuePairs());
        assertNotNull(analyzeResult.getStyles());
        assertEquals(1, analyzeResult.getStyles().size());
        assertNull(analyzeResult.getDocuments());
    }

    void validatePdfContentData(AnalyzeResult analyzeResult) {
        assertNotNull(analyzeResult.getPages());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertTrue(
                documentPage.getAngle() > -180.0 && documentPage.getAngle() < 180.0);
            assertNotNull(analyzeResult.getTables());
            Assertions.assertEquals(8.5f, documentPage.getWidth());
            Assertions.assertEquals(11f, documentPage.getHeight());
            Assertions.assertEquals(DocumentPageLengthUnit.INCH, documentPage.getUnit());

            validateDocumentPage(documentPage);
        });

        assertNotNull(analyzeResult.getTables());
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
        assertNotNull(analyzeResult.getPages());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertTrue(
                documentPage.getAngle() > -180.0 && documentPage.getAngle() < 180.0);
            assertNotNull(analyzeResult.getTables());
            Assertions.assertEquals(1700, documentPage.getWidth());
            Assertions.assertEquals(2200, documentPage.getHeight());
            Assertions.assertEquals(DocumentPageLengthUnit.PIXEL, documentPage.getUnit());

            validateDocumentPage(documentPage);
        });

        assertNotNull(analyzeResult.getTables());
        int[][] table = new int[][] {{5, 4, 20}, {3, 3, 6}};
        Assertions.assertEquals(2, analyzeResult.getTables().size());
        for (int i = 0; i < analyzeResult.getTables().size(); i++) {
            int j = 0;
            DocumentTable actualDocumentTable = analyzeResult.getTables().get(i);
            Assertions.assertEquals(table[i][j], actualDocumentTable.getRowCount());
            Assertions.assertEquals(table[i][++j], actualDocumentTable.getColumnCount());
            Assertions.assertEquals(table[i][++j], actualDocumentTable.getCells().size());
        }

        assertNull(analyzeResult.getKeyValuePairs());
        assertNotNull(analyzeResult.getStyles());
        assertEquals(1, analyzeResult.getStyles().size());
        assertNull(analyzeResult.getDocuments());
    }

    void validateDocumentPage(DocumentPage documentPage) {
        assertNotNull(documentPage.getLines());
        documentPage.getLines().forEach(documentLine -> {
            validateBoundingBoxData(documentLine.getBoundingPolygon());
            assertNotNull(documentLine.getContent());
        });

        assertNotNull(documentPage.getWords());
        documentPage.getWords().forEach(documentWord -> {
            validateBoundingBoxData(documentWord.getBoundingPolygon());
            assertNotNull(documentWord.getContent());
        });
    }

    void validateMultipageLayoutContent(AnalyzeResult analyzeResult) {
        assertNotNull(analyzeResult.getPages());
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

        assertNotNull(analyzeResult.getTables());
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
        int[][] table = new int[][] {{5, 4, 20}, {3, 2, 6}};
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
                assertNotNull(documentField.getConfidence());
                if ("Tax".equals(key)) {
                    assertEquals("$4.00", documentField.getValueAsString());
                }
                if ("Signature".equals(key)) {
                    assertEquals("Bernie Sanders", documentField.getValueAsString());
                } else if ("Email".equals(key)) {
                    assertEquals("accounts@herolimited.com", documentField.getValueAsString());
                } else if ("PhoneNumber".equals(key)) {
                    assertEquals("555-348-6512", documentField.getValueAsString());
                } else if ("Quantity".equals(key)) {
                    assertEquals(20.0f, documentField.getValueAsDouble());
                } else if ("CompanyPhoneNumber".equals(key)) {
                    assertEquals("938-294-2949", documentField.getValueAsString());
                } else if ("DatedAs".equals(key)) {
                    assertEquals("12/20/2020", documentField.getValueAsString());
                } else if ("Total".equals(key)) {
                    assertEquals("$144.00", documentField.getValueAsString());
                } else if ("CompanyName".equals(key)) {
                    assertEquals("Higgly Wiggly Books", documentField.getValueAsString());
                } else if ("VendorName".equals(key)) {
                    assertEquals("Hillary Swank", documentField.getValueAsString());
                } else if ("Website".equals(key)) {
                    assertEquals("www.herolimited.com", documentField.getValueAsString());
                } else if ("Merchant".equals(key)) {
                    assertEquals("Hero Limited", documentField.getValueAsString());
                } else if ("PurchaseOrderNumber".equals(key)) {
                    assertEquals("948284", documentField.getValueAsString());
                } else if ("CompanyAddress".equals(key)) {
                    assertEquals("938 NE Burner Road Boulder City, CO 92848",
                        documentField.getValueAsString());
                } else if ("Subtotal".equals(key)) {
                    assertEquals("$140.00", documentField.getValueAsString());
                }
            });
        });
    }

    void validateMultiPagePdfData(AnalyzeResult analyzeResult) {
        assertEquals(3, analyzeResult.getPages().size());
        analyzeResult.getDocuments().forEach(analyzedDocument -> {
            analyzedDocument.getFields().forEach((key, documentField) -> {
                assertNotNull(documentField.getType());
                assertNotNull(documentField.getConfidence());
            });
        });
    }

    void validateCustomDocumentWithSelectionMarks(AnalyzeResult analyzeResult) {
        Assertions.assertEquals(1, analyzeResult.getPages().size());
        analyzeResult.getPages().forEach(actualDocumentPage -> {
            Assertions.assertEquals(8.5f, actualDocumentPage.getWidth());
            Assertions.assertEquals(11f, actualDocumentPage.getHeight());
            Assertions.assertEquals(DocumentPageLengthUnit.INCH, actualDocumentPage.getUnit());

            validateDocumentPage(actualDocumentPage);
        });
        assertNotNull(analyzeResult.getTables());

        analyzeResult.getDocuments().forEach(actualDocument ->
            actualDocument.getFields().forEach((key, documentField) -> {
                if ("AMEX_SELECTION_MARK".equals(key)) {
                    assertEquals(DocumentSelectionMarkState.SELECTED, documentField.getValueAsSelectionMark());
                } else if ("VISA_SELECTION_MARK".equals(key)) {
                    assertEquals(DocumentSelectionMarkState.UNSELECTED, documentField.getValueAsSelectionMark());
                } else if ("MASTERCARD_SELECTION_MARK".equals(key)) {
                    assertEquals(DocumentSelectionMarkState.UNSELECTED, documentField.getValueAsSelectionMark());
                } else {
                    throw new IllegalStateException("Unexpected value: " + key);
                }
            }));
    }

    void validateW2Data(AnalyzeResult analyzeResult) {
        Assertions.assertEquals("prebuilt-tax.us.w2", analyzeResult.getModelId());
        analyzeResult.getPages().forEach(documentPage -> {
            assertNotNull(documentPage.getLines());
            documentPage.getLines().forEach(documentLine -> {
                validateBoundingBoxData(documentLine.getBoundingPolygon());
                assertNotNull(documentLine.getContent());
            });

            assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getBoundingPolygon());
                // should be getContent()
                assertNotNull(documentWord.getContent());
            });
        });

        assertEquals(1, analyzeResult.getPages().size());
        DocumentPage licensePage1 = analyzeResult.getPages().get(0);
        assertEquals(1, licensePage1.getPageNumber());

        assertNotNull(analyzeResult.getDocuments());
        assertEquals("tax.us.w2", analyzeResult.getDocuments().get(0).getDocType());
        Map<String, DocumentField> w2Fields = analyzeResult.getDocuments().get(0).getFields();

        Map<String, DocumentField> employeeFields = w2Fields.get("Employee").getValueAsMap();
        AddressValue employeeAddrFields = employeeFields.get("Address")
            .getValueAsAddress();
        assertEquals("WA", employeeAddrFields.getState());
        // service regression
        // assertEquals("12345", employeeAddrFields.getPostalCode());
        assertEquals("BUFFALO", employeeAddrFields.getCity());
        assertEquals("4567 MAIN STREET", employeeAddrFields.getStreetAddress());
        assertEquals("4567", employeeAddrFields.getHouseNumber());
        assertEquals("BUFFALO", employeeAddrFields.getCity());
        assertEquals("ANGEL BROWN", employeeFields.get("Name")
            .getValueAsString());
        assertEquals("123-45-6789", employeeFields.get("SocialSecurityNumber")
            .getValueAsString());

        Map<String, DocumentField> employerFields = w2Fields.get("Employer").getValueAsMap();
        AddressValue employerAddress = employerFields.get("Address").getValueAsAddress();
        assertEquals("WA", employerAddress.getState());
        // service regression
        // assertEquals("98765", employerAddress.getPostalCode());
        assertEquals("REDMOND", employerAddress.getCity());
        assertEquals("CONTOSO LTD", employerFields.get("Name")
            .getValueAsString());
        assertEquals("98-7654321", employerFields.get("IdNumber")
            .getValueAsString());

        Assertions.assertEquals(3894.54f, w2Fields.get("FederalIncomeTaxWithheld").getValueAsDouble());
        assertEquals(9873.2f, w2Fields.get("DependentCareBenefits").getValueAsDouble());

        List<DocumentField> localTaxInfoFieldsList = w2Fields.get("LocalTaxInfos").getValueAsList();
        Map<String, DocumentField> localTaxInfoFields1 = localTaxInfoFieldsList.get(0).getValueAsMap();
        Map<String, DocumentField> localTaxInfoFields2 = localTaxInfoFieldsList.get(1).getValueAsMap();

        assertEquals(51f, localTaxInfoFields1.get("LocalIncomeTax").getValueAsDouble());
        assertEquals("Cmberland Vly/Mddl", localTaxInfoFields1.get("LocalityName").getValueAsString());
        assertEquals(37160.56f, localTaxInfoFields1.get("LocalWagesTipsEtc").getValueAsDouble());

        assertEquals(594.54f, localTaxInfoFields2.get("LocalIncomeTax").getValueAsDouble());
        assertEquals("E.Pennsboro/E.Pnns", localTaxInfoFields2.get("LocalityName").getValueAsString());
        assertEquals(37160.56f, localTaxInfoFields2.get("LocalWagesTipsEtc").getValueAsDouble());

        Assertions.assertEquals(538.83f, w2Fields.get("MedicareTaxWithheld").getValueAsDouble());
        assertEquals(37160.56f, w2Fields.get("MedicareWagesAndTips").getValueAsDouble());
        Assertions.assertEquals(653.21f, w2Fields.get("NonQualifiedPlans").getValueAsDouble());
        assertEquals(2303.95f, w2Fields.get("SocialSecurityTaxWithheld").getValueAsDouble());
        Assertions.assertEquals(302.3f, w2Fields.get("SocialSecurityTips").getValueAsDouble());
        assertEquals(37160.56f, w2Fields.get("SocialSecurityWages").getValueAsDouble());

        List<DocumentField> stateTaxInfoFieldsList = w2Fields.get("StateTaxInfos").getValueAsList();
        Map<String, DocumentField> stateTaxInfoFields1 = stateTaxInfoFieldsList.get(0).getValueAsMap();
        Map<String, DocumentField> stateTaxInfoFields2 = stateTaxInfoFieldsList.get(1).getValueAsMap();

        assertNotNull(stateTaxInfoFields1.get("EmployerStateIdNumber").getValueAsString());
        assertEquals("PA", stateTaxInfoFields1.get("State")
            .getValueAsString());
        assertEquals(1135.65f, stateTaxInfoFields1.get("StateIncomeTax").getValueAsDouble());

        assertEquals(37160.56f, stateTaxInfoFields1.get("StateWagesTipsEtc").getValueAsDouble());

        assertEquals(1032.3f, stateTaxInfoFields2.get("StateIncomeTax").getValueAsDouble());
        assertEquals(9631.2f, stateTaxInfoFields2.get("StateWagesTipsEtc").getValueAsDouble());

        Assertions.assertEquals("2018", w2Fields.get("TaxYear").getValueAsString());
        assertEquals("W-2", w2Fields.get("W2FormVariant").getValueAsString());
        assertEquals(37160.56f, w2Fields.get("WagesTipsAndOtherCompensation").getValueAsDouble());
    }

    private void validateBoundingBoxData(List<Point> points) {
        assertNotNull(points);
        assertEquals(4, points.size());
    }

    private void validatePngReceiptFields(Map<String, DocumentField> actualFields) {
        Assertions.assertEquals("+19876543210", actualFields.get("MerchantPhoneNumber").getValueAsPhoneNumber());
        assertNotNull(actualFields.get("Subtotal").getValueAsDouble());
        assertNotNull(actualFields.get("Total").getValueAsDouble());
        assertNotNull(actualFields.get("Subtotal").getConfidence());
        assertNotNull(actualFields.get("Total").getConfidence());
        assertNotNull(actualFields.get("Items"));
        List<DocumentField> itemizedItems = actualFields.get("Items").getValueAsList();

        for (int i = 0; i < itemizedItems.size(); i++) {
            if (itemizedItems.get(i).getContent() != null) {
                String[] itemizedNames = new String[] {"Surface Pro 6", "Surface Pen"};
                Double[] itemizedTotalPrices = new Double[] {1998.0, 299.9700012207031};
                Double[] itemizedQuantities = new Double[] {2.0, 3.0};

                Map<String, DocumentField> actualReceiptItems = itemizedItems.get(i).getValueAsMap();
                int finalI = i;
                actualReceiptItems.forEach((key, documentField) -> {
                    assertNotNull(documentField.getConfidence());
                    if ("Description".equals(key)) {
                        if (DocumentFieldType.STRING == documentField.getType()) {
                            String name = documentField.getValueAsString();
                            assertEquals(itemizedNames[finalI], name);
                        }
                    }
                    if ("Quantity".equals(key)) {
                        if (DocumentFieldType.DOUBLE == documentField.getType()) {
                            Double quantity = documentField.getValueAsDouble();
                            assertEquals(itemizedQuantities[finalI], quantity);
                        }
                    }
                    if ("Price".equals(key)) {
                        assertNull(documentField.getValueAsDouble());
                    }

                    if ("TotalPrice".equals(key)) {
                        if (DocumentFieldType.DOUBLE == documentField.getType()) {
                            Double totalPrice = documentField.getValueAsDouble();
                            assertEquals(itemizedTotalPrices[finalI], totalPrice);
                        }
                    }
                });
            }
        }
    }

    private void validateReceipt(AnalyzeResult actualAnalyzeResult) {
        Assertions.assertEquals("prebuilt-receipt", actualAnalyzeResult.getModelId());
        assertNotNull(actualAnalyzeResult.getPages());
    }

    private void validateJpegReceiptFields(Map<String, DocumentField> actualFields) {
        actualFields.forEach((key, documentField) -> {
            if (documentField.getBoundingRegions() != null) {
                Assertions.assertEquals(1, documentField.getBoundingRegions().get(0).getPageNumber());
            }
            if ("Locale".equals(key)) {
                Assertions.assertEquals("en-US", documentField.getValueAsString());
                assertNotNull(documentField.getConfidence());
            } else if ("MerchantAddress".equals(key)) {
                Assertions.assertEquals("123 Main Street", documentField.getValueAsAddress().getStreetAddress());
                assertNotNull(documentField.getConfidence());
            } else if ("MerchantName".equals(key)) {
                Assertions.assertEquals("Contoso", documentField.getValueAsString());
                assertNotNull(documentField.getConfidence());
            } else if ("MerchantPhoneNumber".equals(key)) {
                Assertions.assertEquals("+19876543210", documentField.getValueAsPhoneNumber());
                assertNotNull(documentField.getConfidence());
            } else if ("ReceiptType".equals(key)) {
                Assertions.assertEquals("Itemized", documentField.getValueAsString());
                assertNotNull(documentField.getConfidence());
            } else if ("Subtotal".equals(key)) {
                Assertions.assertEquals(11.7f, documentField.getValueAsDouble());
                assertNotNull(documentField.getConfidence());
            } else if ("Tax".equals(key)) {
                Assertions.assertEquals(1.17f, documentField.getValueAsDouble());
                assertNotNull(documentField.getConfidence());
            } else if ("Tip".equals(key)) {
                Assertions.assertEquals(1.63f, documentField.getValueAsDouble());
                assertNotNull(documentField.getConfidence());
            } else if ("TransactionDate".equals(key)) {
                Assertions.assertEquals(LocalDate.of(2019, 6, 10), documentField.getValueAsDate());
                assertNotNull(documentField.getConfidence());
            } else if ("TransactionTime".equals(key)) {
                Assertions.assertEquals(LocalTime.of(13, 59), documentField.getValueAsTime());
                assertNotNull(documentField.getConfidence());
            } else if ("Total".equals(key)) {
                Assertions.assertEquals(14.5f, documentField.getValueAsDouble());
                assertNotNull(documentField.getConfidence());
            }
        });
    }

    private String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080" : AZURE_FORM_RECOGNIZER_ENDPOINT_CONFIGURATION;
    }
}
