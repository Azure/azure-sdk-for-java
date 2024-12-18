// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.documentintelligence;

import com.azure.ai.documentintelligence.models.AddressValue;
import com.azure.ai.documentintelligence.models.AnalyzeResult;
import com.azure.ai.documentintelligence.models.CurrencyValue;
import com.azure.ai.documentintelligence.models.DocumentField;
import com.azure.ai.documentintelligence.models.DocumentPage;
import com.azure.ai.documentintelligence.models.DocumentTable;
import com.azure.ai.documentintelligence.models.LengthUnit;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.BodilessMatcher;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.identity.AzurePowerShellCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.junit.jupiter.api.Assertions;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.azure.ai.documentintelligence.TestUtils.DEFAULT_POLL_INTERVAL;
import static com.azure.ai.documentintelligence.TestUtils.EXPECTED_MERCHANT_NAME;
import static com.azure.ai.documentintelligence.TestUtils.ONE_NANO_DURATION;
import static com.azure.ai.documentintelligence.TestUtils.REMOVE_SANITIZER_ID;
import static com.azure.ai.documentintelligence.TestUtils.getTestProxySanitizers;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class DocumentIntelligenceClientTestBase extends TestProxyTestBase {
    Duration durationTestMode;
    private boolean sanitizersRemoved = false;

    /**
     * Use duration of nearly zero value for PLAYBACK test mode, otherwise, use default duration value for LIVE mode.
     */
    @Override
    protected void beforeTest() {
        durationTestMode = interceptorManager.isPlaybackMode() ? ONE_NANO_DURATION : DEFAULT_POLL_INTERVAL;
    }

    public DocumentIntelligenceClientBuilder getDocumentAnalysisBuilder(HttpClient httpClient,
                                                                        DocumentIntelligenceServiceVersion serviceVersion) {
        String endpoint = getEndpoint();

        DocumentIntelligenceClientBuilder builder = new DocumentIntelligenceClientBuilder()
            .endpoint(endpoint)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion);

        if (interceptorManager.isPlaybackMode()) {
            builder.credential(new MockTokenCredential());
            setMatchers();
        } else if (interceptorManager.isRecordMode()) {
            builder.credential(new DefaultAzureCredentialBuilder().build());
            builder.addPolicy(interceptorManager.getRecordPolicy());
        } else if (interceptorManager.isLiveMode()) {
            builder.credential(new AzurePowerShellCredentialBuilder().build());
        }
        if (!interceptorManager.isLiveMode() && !sanitizersRemoved) {
            interceptorManager.addSanitizers(getTestProxySanitizers());
            interceptorManager.removeSanitizers(REMOVE_SANITIZER_ID);
            sanitizersRemoved = true;
        }
        return builder;
    }

    private void setMatchers() {
        interceptorManager.addMatchers(Collections.singletonList(new BodilessMatcher()));
    }
    public DocumentIntelligenceAdministrationClientBuilder getDocumentModelAdminClientBuilder(HttpClient httpClient,
                                                                                              DocumentIntelligenceServiceVersion serviceVersion) {
        String endpoint = getEndpoint();

        DocumentIntelligenceAdministrationClientBuilder builder = new DocumentIntelligenceAdministrationClientBuilder()
            .endpoint(endpoint)
            .httpClient(interceptorManager.isPlaybackMode() ? interceptorManager.getPlaybackClient() : httpClient)
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .serviceVersion(serviceVersion);


        if (interceptorManager.isPlaybackMode()) {
            builder.credential(new MockTokenCredential());
            setMatchers();
        } else if (interceptorManager.isRecordMode()) {
            builder.credential(new DefaultAzureCredentialBuilder().build());
            builder.addPolicy(interceptorManager.getRecordPolicy());
        } else if (interceptorManager.isLiveMode()) {
            builder.credential(new AzurePowerShellCredentialBuilder().build());
        }
        if (!interceptorManager.isLiveMode() && !sanitizersRemoved) {
            interceptorManager.addSanitizers(getTestProxySanitizers());
            interceptorManager.removeSanitizers(REMOVE_SANITIZER_ID);
            sanitizersRemoved = true;
        }
        return builder;
    }
    void dataRunner(BiConsumer<byte[], Long> testRunner, String fileName) {
        TestUtils.getDataRunnerHelper(testRunner, fileName);
    }

    void buildModelRunner(Consumer<String> testRunner) {
        TestUtils.getTrainingDataContainerHelper(testRunner, interceptorManager.isPlaybackMode());
    }

    void buildBatchModelRunner(Consumer<String> testRunner) {
        TestUtils.getBatchTrainingDataContainerHelper(testRunner, interceptorManager.isPlaybackMode());
    }

    void beginClassifierRunner(Consumer<String> testRunner) {
        TestUtils.getClassifierTrainingDataContainerHelper(testRunner, interceptorManager.isPlaybackMode());
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

    void validateInvoiceData(AnalyzeResult analyzeResult) {
        Assertions.assertEquals("prebuilt-invoice", analyzeResult.getModelId());
        analyzeResult.getPages().forEach(documentPage -> {
            assertNotNull(documentPage.getLines());
            documentPage.getLines().forEach(documentLine -> {
                validateBoundingBoxData(documentLine.getPolygon());
                assertNotNull(documentLine.getContent());
            });

            assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getPolygon());
                assertNotNull(documentWord.getContent());
            });
        });

        assertEquals(1, analyzeResult.getPages().size());
        DocumentPage invoicePage1 = analyzeResult.getPages().get(0);

        assertEquals(1, invoicePage1.getPageNumber());

        assertNotNull(analyzeResult.getDocuments());
        assertEquals(1, analyzeResult.getDocuments().size());
        Map<String, DocumentField> invoicePage1Fields = analyzeResult.getDocuments().get(0).getFields();
        assertNotNull(invoicePage1Fields.get("CustomerAddress")
            .getValueAddress().getStreetAddress());
        assertNotNull(invoicePage1Fields.get("CustomerAddress").getConfidence());
        assertEquals("Microsoft", invoicePage1Fields.get("CustomerAddressRecipient")
            .getValueString());
        assertNotNull(invoicePage1Fields.get("CustomerAddressRecipient").getConfidence());
        assertEquals("Microsoft", invoicePage1Fields.get("CustomerName")
            .getValueString());
        assertNotNull(invoicePage1Fields.get("CustomerName").getConfidence());
        assertEquals(LocalDate.of(2017, 6, 24), invoicePage1Fields.get("DueDate")
            .getValueDate());
        assertNotNull(invoicePage1Fields.get("DueDate").getConfidence());
        assertEquals(LocalDate.of(2017, 6, 18), invoicePage1Fields.get("InvoiceDate")
            .getValueDate());
        assertNotNull(invoicePage1Fields.get("InvoiceDate").getConfidence());
        assertEquals("34278587", invoicePage1Fields.get("InvoiceId")
            .getValueString());
        assertNotNull(invoicePage1Fields.get("InvoiceId").getConfidence());
        assertEquals("1 Redmond way Suite\n6000", invoicePage1Fields.get("VendorAddress")
            .getValueAddress().getStreetAddress());
        assertNotNull(invoicePage1Fields.get("VendorAddress").getConfidence());
        assertEquals(EXPECTED_MERCHANT_NAME, invoicePage1Fields.get("VendorName")
            .getValueString());
        assertNotNull(invoicePage1Fields.get("VendorName").getConfidence());
        DocumentField subtotalField = invoicePage1Fields.get("Subtotal");
        if (subtotalField != null) {
            CurrencyValue subtotal = subtotalField.getValueCurrency();

            Assertions.assertEquals(100.0, subtotal.getAmount());
            Assertions.assertEquals("USD", subtotal.getCurrencyCode());
            Assertions.assertEquals("$", subtotal.getCurrencySymbol());
        }

        Map<String, DocumentField> itemsMap
            = invoicePage1Fields.get("Items").getValueArray().get(0).getValueObject();
        assertEquals(56651.49, itemsMap.get("Amount").getValueCurrency().getAmount());
        assertNotNull(itemsMap.get("Amount").getConfidence());
        assertEquals(LocalDate.of(2017, 6, 18), itemsMap.get("Date").getValueDate());
        assertNotNull(itemsMap.get("Date").getConfidence());
        assertEquals("34278587", itemsMap.get("ProductCode").getValueString());
        assertNotNull(itemsMap.get("ProductCode").getConfidence());
        Assertions.assertNotNull(analyzeResult.getPages());
    }


    void validateIdentityData(AnalyzeResult analyzeResult) {
        Assertions.assertEquals("prebuilt-idDocument", analyzeResult.getModelId());
        analyzeResult.getPages().forEach(documentPage -> {
            assertNotNull(documentPage.getLines());
            documentPage.getLines().forEach(documentLine -> {
                validateBoundingBoxData(documentLine.getPolygon());
                assertNotNull(documentLine.getContent());
            });

            assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getPolygon());
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
            .getValueAddress().getStreetAddress());
        assertNotNull(licensePageFields.get("Address").getConfidence());
        assertEquals("USA", licensePageFields.get("CountryRegion").getValueCountryRegion());
        assertNotNull(licensePageFields.get("CountryRegion").getConfidence());
        assertEquals(LocalDate.of(1988, 3, 23), licensePageFields.get("DateOfBirth")
            .getValueDate());
        assertNotNull(licensePageFields.get("DateOfBirth").getConfidence());
        assertEquals(LocalDate.of(2026, 3, 23), licensePageFields.get("DateOfExpiration")
            .getValueDate());
        assertNotNull(licensePageFields.get("DateOfExpiration").getConfidence());
        assertEquals("034568", licensePageFields.get("DocumentNumber")
            .getValueString());
        assertNotNull(licensePageFields.get("DocumentNumber").getConfidence());
        assertEquals("CHRIS", licensePageFields.get("FirstName").getValueString());
        assertNotNull(licensePageFields.get("FirstName").getConfidence());
        assertEquals("SMITH", licensePageFields.get("LastName").getValueString());
        assertNotNull(licensePageFields.get("LastName").getConfidence());
        assertEquals("West Virginia", licensePageFields.get("Region").getValueString());
        assertNotNull(licensePageFields.get("Region").getConfidence());
        assertEquals("M", licensePageFields.get("Sex").getValueString());
        assertNotNull(licensePageFields.get("Sex").getConfidence());
        assertEquals("NONE", licensePageFields.get("Endorsements").getValueString());
        assertNotNull(licensePageFields.get("Endorsements").getConfidence());
        assertEquals("NONE", licensePageFields.get("Restrictions").getValueString());
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
            Assertions.assertEquals(LengthUnit.INCH, documentPage.getUnit());

        });

        assertNotNull(analyzeResult.getTables());
        int[] table = new int[] {8, 3, 24};
        Assertions.assertEquals(1, analyzeResult.getTables().size());
        for (int i = 0; i < analyzeResult.getTables().size(); i++) {
            DocumentTable actualDocumentTable = analyzeResult.getTables().get(i);
            Assertions.assertEquals(table[i], actualDocumentTable.getRowCount());
            Assertions.assertEquals(table[++i], actualDocumentTable.getColumnCount());
            Assertions.assertEquals(table[++i], actualDocumentTable.getCells().size());
            // actualDocumentTable.getCells().forEach(documentTableCell
            //    -> assertNotNull(documentTableCell.getKind()));
        }
    }

    void validateContentData(AnalyzeResult analyzeResult) {
        assertNotNull(analyzeResult.getPages());
        analyzeResult.getPages().forEach(documentPage -> {
            Assertions.assertTrue(
                documentPage.getAngle() > -180.0 && documentPage.getAngle() < 180.0);
            assertNotNull(analyzeResult.getTables());
            Assertions.assertEquals(1700, documentPage.getWidth());
            Assertions.assertEquals(2200, documentPage.getHeight());
            Assertions.assertEquals(LengthUnit.PIXEL, documentPage.getUnit());

            validateDocumentPage(documentPage);
        });

        assertNotNull(analyzeResult.getTables());
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
        assertNotNull(analyzeResult.getStyles());
        assertEquals(1, analyzeResult.getStyles().size());
        assertNull(analyzeResult.getDocuments());
    }

    void validateDocumentPage(DocumentPage documentPage) {
        assertNotNull(documentPage.getLines());
        documentPage.getLines().forEach(documentLine -> {
            validateBoundingBoxData(documentLine.getPolygon());
            assertNotNull(documentLine.getContent());
        });

        assertNotNull(documentPage.getWords());
        documentPage.getWords().forEach(documentWord -> {
            validateBoundingBoxData(documentWord.getPolygon());
            assertNotNull(documentWord.getContent());
        });
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
                    assertEquals("$4.00", documentField.getValueString());
                }
//                if ("Signature".equals(key)) {
//                    // Service regression
//                    // assertEquals("Bernie Sanders", documentField.getValueString());
//                } else
                if ("Email".equals(key)) {
                    assertEquals("accounts@herolimited.com", documentField.getValueString());
                } else if ("PhoneNumber".equals(key)) {
                    assertEquals("555-348-6512", documentField.getValueString());
                } else if ("Quantity".equals(key)) {
                    assertEquals(20.0f, documentField.getValueNumber());
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

    void validateW2Data(AnalyzeResult analyzeResult) {
        Assertions.assertEquals("prebuilt-tax.us.w2", analyzeResult.getModelId());
        analyzeResult.getPages().forEach(documentPage -> {
            assertNotNull(documentPage.getLines());
            documentPage.getLines().forEach(documentLine -> {
                validateBoundingBoxData(documentLine.getPolygon());
                assertNotNull(documentLine.getContent());
            });

            assertNotNull(documentPage.getWords());
            documentPage.getWords().forEach(documentWord -> {
                validateBoundingBoxData(documentWord.getPolygon());
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

        Map<String, DocumentField> employeeFields = w2Fields.get("Employee").getValueObject();
        AddressValue employeeAddrFields = employeeFields.get("Address")
            .getValueAddress();
        if (employeeAddrFields != null) {
            assertEquals("WA", employeeAddrFields.getState());
            // service regression
            // assertEquals("12345", employeeAddrFields.getPostalCode());
            assertEquals("BUFFALO", employeeAddrFields.getCity());
            assertEquals("4567 MAIN STREET", employeeAddrFields.getStreetAddress());
            assertEquals("4567", employeeAddrFields.getHouseNumber());
            assertEquals("BUFFALO", employeeAddrFields.getCity());
        }

        assertEquals("ANGEL BROWN", employeeFields.get("Name")
            .getValueString());
        assertEquals("123-45-6789", employeeFields.get("SocialSecurityNumber")
            .getValueString());

        Map<String, DocumentField> employerFields = w2Fields.get("Employer").getValueObject();
        AddressValue employerAddress = employerFields.get("Address").getValueAddress();
        if (employerAddress != null) {
            assertEquals("WA", employerAddress.getState());
            // service regression
            // assertEquals("98765", employerAddress.getPostalCode());
            assertEquals("REDMOND", employerAddress.getCity());
        }
        assertEquals("CONTOSO LTD", employerFields.get("Name")
            .getValueString());
        assertEquals("98-7654321", employerFields.get("IdNumber")
            .getValueString());

        Assertions.assertEquals(3894.54f, w2Fields.get("FederalIncomeTaxWithheld").getValueNumber(), .01);
        assertEquals(9873.2f, w2Fields.get("DependentCareBenefits").getValueNumber(), .01);

        List<DocumentField> localTaxInfoFieldsList = w2Fields.get("LocalTaxInfos").getValueArray();
        Map<String, DocumentField> localTaxInfoFields1 = localTaxInfoFieldsList.get(0).getValueObject();
        Map<String, DocumentField> localTaxInfoFields2 = localTaxInfoFieldsList.get(1).getValueObject();

        assertEquals(51f, localTaxInfoFields1.get("LocalIncomeTax").getValueNumber());
        assertEquals("Cmberland Vly/Mddl", localTaxInfoFields1.get("LocalityName").getValueString());
        assertEquals(37160.56f, localTaxInfoFields1.get("LocalWagesTipsEtc").getValueNumber(), 0.01);

        assertEquals(594.54f, localTaxInfoFields2.get("LocalIncomeTax").getValueNumber(), 0.01);
        assertEquals("E.Pennsboro/E.Pnns", localTaxInfoFields2.get("LocalityName").getValueString());
        assertEquals(37160.56f, localTaxInfoFields2.get("LocalWagesTipsEtc").getValueNumber(), 0.01);

        Assertions.assertEquals(538.83f, w2Fields.get("MedicareTaxWithheld").getValueNumber(), 0.01);
        assertEquals(37160.56f, w2Fields.get("MedicareWagesAndTips").getValueNumber(), 0.01);
        Assertions.assertEquals(653.21f, w2Fields.get("NonQualifiedPlans").getValueNumber(), 0.01);
        assertEquals(2303.95f, w2Fields.get("SocialSecurityTaxWithheld").getValueNumber(), 0.01);
        Assertions.assertEquals(302.3f, w2Fields.get("SocialSecurityTips").getValueNumber(), 0.01);
        assertEquals(37160.56f, w2Fields.get("SocialSecurityWages").getValueNumber(), 0.01);

        List<DocumentField> stateTaxInfoFieldsList = w2Fields.get("StateTaxInfos").getValueArray();
        Map<String, DocumentField> stateTaxInfoFields1 = stateTaxInfoFieldsList.get(0).getValueObject();
        Map<String, DocumentField> stateTaxInfoFields2 = stateTaxInfoFieldsList.get(1).getValueObject();

        assertNotNull(stateTaxInfoFields1.get("EmployerStateIdNumber").getValueString());
        assertEquals("PA", stateTaxInfoFields1.get("State")
            .getValueString());
        assertEquals(1135.65f, stateTaxInfoFields1.get("StateIncomeTax").getValueNumber(), 0.01);

        assertEquals(37160.56f, stateTaxInfoFields1.get("StateWagesTipsEtc").getValueNumber(), 0.01);

        assertEquals(1032.3f, stateTaxInfoFields2.get("StateIncomeTax").getValueNumber(), 0.01);
        assertEquals(9631.2f, stateTaxInfoFields2.get("StateWagesTipsEtc").getValueNumber(), 0.01);

        Assertions.assertEquals("2018", w2Fields.get("TaxYear").getValueString());
        assertEquals("W-2", w2Fields.get("W2FormVariant").getValueString());
        assertEquals(37160.56f, w2Fields.get("WagesTipsAndOtherCompensation").getValueNumber(), 0.01);
    }

    private void validateBoundingBoxData(List<Double> points) {
        assertNotNull(points);
        assertEquals(8, points.size());
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
                Assertions.assertEquals("en-US", documentField.getValueString());
                assertNotNull(documentField.getConfidence());
            } else if ("MerchantAddress".equals(key)) {
                Assertions.assertEquals("123 Main Street", documentField.getValueAddress().getStreetAddress());
                assertNotNull(documentField.getConfidence());
            } else if ("MerchantName".equals(key)) {
                Assertions.assertEquals("Contoso", documentField.getValueString());
                assertNotNull(documentField.getConfidence());
            } else if ("MerchantPhoneNumber".equals(key)) {
                Assertions.assertEquals("+19876543210", documentField.getValuePhoneNumber());
                assertNotNull(documentField.getConfidence());
            } else if ("ReceiptType".equals(key)) {
                Assertions.assertEquals("Itemized", documentField.getValueString());
                // TODO: (service bug) confidence is returned as null
                // assertNotNull(documentField.getConfidence());
            } else if ("Subtotal".equals(key)) {
                Assertions.assertEquals("$ 11.70", documentField.getContent());
                assertNotNull(documentField.getConfidence());
            } else if ("Tax".equals(key)) {
                Assertions.assertEquals(1.17f, documentField.getValueCurrency().getAmount());
                assertNotNull(documentField.getConfidence());
            } else if ("Tip".equals(key)) {
                Assertions.assertEquals(1.63f, documentField.getValueCurrency().getAmount(), .01);
                assertNotNull(documentField.getConfidence());
            } else if ("TransactionDate".equals(key)) {
                Assertions.assertEquals(LocalDate.of(2019, 6, 10), documentField.getValueDate());
                assertNotNull(documentField.getConfidence());
            } else if ("TransactionTime".equals(key)) {
                Assertions.assertNotNull(documentField.getValueTime());
                assertNotNull(documentField.getConfidence());
            } else if ("Total".equals(key)) {
                Assertions.assertEquals(14.5f, documentField.getValueCurrency().getAmount());
                assertNotNull(documentField.getConfidence());
            }
        });
    }

    private String getEndpoint() {
        return interceptorManager.isPlaybackMode()
            ? "https://localhost:8080" : TestUtils.AZURE_DOCUMENTINTELLIGENCE_ENDPOINT_CONFIGURATION;
    }
}
