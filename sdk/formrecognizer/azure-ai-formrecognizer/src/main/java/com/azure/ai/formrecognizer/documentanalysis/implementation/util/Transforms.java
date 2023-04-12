// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis.implementation.util;

import com.azure.ai.formrecognizer.documentanalysis.administration.models.AzureBlobContentSource;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.AzureBlobFileListSource;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.BuildDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ClassifierDocumentTypeDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ComposeDocumentModelOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.CopyAuthorizationOptions;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentClassifierDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentFieldSchema;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildMode;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelBuildOperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelComposeOperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelCopyAuthorization;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelCopyToOperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentModelSummary;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.DocumentTypeDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationKind;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationStatus;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.OperationSummary;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.QuotaDetails;
import com.azure.ai.formrecognizer.documentanalysis.administration.models.ResourceDetails;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.AuthorizeCopyRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.BuildDocumentClassifierRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.BuildDocumentModelRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.ComponentDocumentModelDetails;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.ComposeDocumentModelRequest;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.CopyAuthorization;
import com.azure.ai.formrecognizer.documentanalysis.implementation.models.ErrorResponseException;
import com.azure.ai.formrecognizer.documentanalysis.models.AddressValue;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzeResult;
import com.azure.ai.formrecognizer.documentanalysis.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.documentanalysis.models.BoundingRegion;
import com.azure.ai.formrecognizer.documentanalysis.models.CurrencyValue;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnalysisFeature;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnnotation;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentAnnotationKind;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentBarcode;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentBarcodeKind;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentField;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFieldType;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFormula;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentFormulaKind;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentImage;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentKeyValueElement;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentKeyValuePair;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentLanguage;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentLine;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPage;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPageKind;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentPageLengthUnit;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentParagraph;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSelectionMark;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSelectionMarkState;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSignatureType;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentSpan;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentStyle;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentTable;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentTableCell;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentTableCellKind;
import com.azure.ai.formrecognizer.documentanalysis.models.DocumentWord;
import com.azure.ai.formrecognizer.documentanalysis.models.OperationResult;
import com.azure.ai.formrecognizer.documentanalysis.models.ParagraphRole;
import com.azure.ai.formrecognizer.documentanalysis.models.Point;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.models.ResponseError;
import com.azure.core.util.CoreUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class to convert service level models to SDK exposed models.
 */
public class Transforms {
    public static AnalyzeResult toAnalyzeResultOperation(
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.AnalyzeResult innerAnalyzeResult) {
        AnalyzeResult analyzeResult = new AnalyzeResult();

        // add documents
        if (!CoreUtils.isNullOrEmpty(innerAnalyzeResult.getDocuments())) {
            AnalyzeResultHelper.setDocuments(analyzeResult, innerAnalyzeResult.getDocuments().stream()
                .map(document -> {
                    AnalyzedDocument analyzedDocument = new AnalyzedDocument();
                    AnalyzedDocumentHelper.setBoundingRegions(analyzedDocument,
                        toBoundingRegions(document.getBoundingRegions()));
                    AnalyzedDocumentHelper.setConfidence(analyzedDocument, document.getConfidence());
                    AnalyzedDocumentHelper.setDocType(analyzedDocument, document.getDocType());
                    AnalyzedDocumentHelper.setFields(analyzedDocument, toDocumentFields(document.getFields()));
                    AnalyzedDocumentHelper.setSpans(analyzedDocument, toDocumentSpans(document.getSpans()));
                    return analyzedDocument;
                })
                .collect(Collectors.toList()));
        }

        AnalyzeResultHelper.setContent(analyzeResult, innerAnalyzeResult.getContent());
        AnalyzeResultHelper.setModelId(analyzeResult, innerAnalyzeResult.getModelId());
        if (!CoreUtils.isNullOrEmpty(innerAnalyzeResult.getLanguages())) {
            AnalyzeResultHelper.setLanguages(analyzeResult, innerAnalyzeResult.getLanguages()
                .stream()
                .map(innerLanguage -> {
                    DocumentLanguage documentLanguage = new DocumentLanguage();
                    DocumentLanguageHelper.setLocale(documentLanguage, innerLanguage.getLocale());
                    DocumentLanguageHelper.setSpans(documentLanguage, toDocumentSpans(innerLanguage.getSpans()));
                    DocumentLanguageHelper.setConfidence(documentLanguage, innerLanguage.getConfidence());
                    return documentLanguage;
                })
                .collect(Collectors.toList()));
        }

        //add pages
        AnalyzeResultHelper.setPages(analyzeResult, innerAnalyzeResult.getPages().stream()
            .map(innerDocumentPage -> {
                DocumentPage documentPage = new DocumentPage();
                DocumentPageHelper.setAngle(documentPage, innerDocumentPage.getAngle());
                DocumentPageHelper.setHeight(documentPage, innerDocumentPage.getHeight());
                DocumentPageHelper.setWidth(documentPage, innerDocumentPage.getWidth());
                DocumentPageHelper.setPageNumber(documentPage, innerDocumentPage.getPageNumber());
                DocumentPageHelper.setUnit(documentPage, innerDocumentPage.getUnit() == null
                    ? null : DocumentPageLengthUnit.fromString(innerDocumentPage.getUnit().toString()));
                DocumentPageHelper.setSpans(documentPage, toDocumentSpans(innerDocumentPage.getSpans()));

                DocumentPageHelper.setSelectionMarks(documentPage, innerDocumentPage.getSelectionMarks() == null
                    ? null
                    : innerDocumentPage.getSelectionMarks()
                    .stream()
                    .map(innerSelectionMark -> {
                        DocumentSelectionMark documentSelectionMark = new DocumentSelectionMark();
                        DocumentSelectionMarkHelper.setBoundingPolygon(documentSelectionMark,
                            toPolygonPoints(innerSelectionMark.getPolygon()));
                        DocumentSelectionMarkHelper.setConfidence(documentSelectionMark,
                            innerSelectionMark.getConfidence());
                        DocumentSelectionMarkHelper.setSpan(documentSelectionMark,
                            getDocumentSpan(innerSelectionMark.getSpan()));
                        DocumentSelectionMarkHelper.setState(documentSelectionMark,
                            DocumentSelectionMarkState.fromString(innerSelectionMark.getState().toString()));
                        return documentSelectionMark;
                    })
                    .collect(Collectors.toList()));
                DocumentPageHelper.setLines(documentPage,
                    innerDocumentPage.getLines() == null ? null : innerDocumentPage.getLines()
                        .stream()
                        .map(innerDocumentLine -> {
                            DocumentLine documentLine = new DocumentLine();
                            DocumentLineHelper.setBoundingPolygon(documentLine,
                                toPolygonPoints(innerDocumentLine.getPolygon()));
                            DocumentLineHelper.setContent(documentLine, innerDocumentLine.getContent());
                            DocumentLineHelper.setSpans(documentLine, toDocumentSpans(innerDocumentLine.getSpans()));
                            DocumentLineHelper.setPageWords(documentLine, toDocumentWords(innerDocumentPage));
                            return documentLine;
                        })
                        .collect(Collectors.toList()));
                DocumentPageHelper.setWords(documentPage, toDocumentWords(innerDocumentPage));
                DocumentPageHelper.setKind(documentPage,
                    innerDocumentPage.getKind() == null ? null : DocumentPageKind.fromString(innerDocumentPage.getKind().toString()));
                DocumentPageHelper.setAnnotations(documentPage, fromInnerAnnotations(innerDocumentPage.getAnnotations()));
                DocumentPageHelper.setFormulas(documentPage, fromInnerFormulas(innerDocumentPage.getFormulas()));
                DocumentPageHelper.setBarcodes(documentPage, fromInnerBarcodes(innerDocumentPage.getBarcodes()));
                DocumentPageHelper.setImages(documentPage, fromInnerImages(innerDocumentPage.getImages()));
                return documentPage;
            })
            .collect(Collectors.toList()));

        // add key value pairs
        if (!CoreUtils.isNullOrEmpty(innerAnalyzeResult.getKeyValuePairs())) {
            AnalyzeResultHelper.setKeyValuePairs(analyzeResult, innerAnalyzeResult.getKeyValuePairs()
                .stream()
                .map(innerKeyValuePair -> {
                    DocumentKeyValuePair documentKeyValuePair = new DocumentKeyValuePair();
                    DocumentKeyValuePairHelper.setValue(documentKeyValuePair,
                        toDocumentKeyValueElement(innerKeyValuePair.getValue()));
                    DocumentKeyValuePairHelper.setKey(documentKeyValuePair,
                        toDocumentKeyValueElement(innerKeyValuePair.getKey()));
                    DocumentKeyValuePairHelper.setConfidence(documentKeyValuePair, innerKeyValuePair.getConfidence());
                    return documentKeyValuePair;
                })
                .collect(Collectors.toList()));
        }

        // add styles
        if (!CoreUtils.isNullOrEmpty(innerAnalyzeResult.getStyles())) {
            AnalyzeResultHelper.setStyles(analyzeResult, innerAnalyzeResult.getStyles()
                .stream()
                .map(innerDocumentStyle -> {
                    DocumentStyle documentStyle = new DocumentStyle();
                    DocumentStyleHelper.setConfidence(documentStyle, innerDocumentStyle.getConfidence());
                    DocumentStyleHelper.setIsHandwritten(documentStyle, innerDocumentStyle.isHandwritten());
                    DocumentStyleHelper.setSpans(documentStyle, toDocumentSpans(innerDocumentStyle.getSpans()));
                    return documentStyle;
                })
                .collect(Collectors.toList()));
        }

        // add paragraphs
        if (!CoreUtils.isNullOrEmpty(innerAnalyzeResult.getParagraphs())) {
            AnalyzeResultHelper.setParagraphs(analyzeResult, innerAnalyzeResult.getParagraphs()
                .stream()
                .map(innerParagraph -> {
                    DocumentParagraph documentParagraph = new DocumentParagraph();
                    DocumentParagraphHelper.setContent(documentParagraph, innerParagraph.getContent());
                    DocumentParagraphHelper.setRole(documentParagraph, innerParagraph.getRole() == null ? null
                        : ParagraphRole.fromString(innerParagraph.getRole().toString()));
                    DocumentParagraphHelper.setBoundingRegions(documentParagraph,
                        toBoundingRegions(innerParagraph.getBoundingRegions()));
                    DocumentParagraphHelper.setSpans(documentParagraph,
                        toDocumentSpans(innerParagraph.getSpans()));
                    return documentParagraph;
                })
                .collect(Collectors.toList()));
        }

        // add tables
        if (!CoreUtils.isNullOrEmpty(innerAnalyzeResult.getTables())) {
            AnalyzeResultHelper.setTables(analyzeResult, innerAnalyzeResult.getTables()
                .stream()
                .map(innerDocumentTable -> {
                    DocumentTable documentTable = new DocumentTable();
                    DocumentTableHelper.setCells(documentTable, innerDocumentTable.getCells()
                        .stream()
                        .map(innerDocumentCell -> {
                            DocumentTableCell documentTableCell = new DocumentTableCell();
                            DocumentTableCellHelper.setBoundingRegions(documentTableCell,
                                toBoundingRegions(innerDocumentTable.getBoundingRegions()));
                            DocumentTableCellHelper.setSpans(documentTableCell,
                                toDocumentSpans(innerDocumentTable.getSpans()));
                            DocumentTableCellHelper.setContent(documentTableCell, innerDocumentCell.getContent());
                            DocumentTableCellHelper.setColumnIndex(documentTableCell,
                                innerDocumentCell.getColumnIndex());
                            DocumentTableCellHelper.setKind(documentTableCell, innerDocumentCell.getKind() == null
                                ? DocumentTableCellKind.CONTENT
                                : DocumentTableCellKind.fromString(innerDocumentCell.getKind().toString()));
                            DocumentTableCellHelper.setRowIndex(documentTableCell, innerDocumentCell.getRowIndex());
                            DocumentTableCellHelper.setColumnSpan(documentTableCell, innerDocumentCell.getColumnSpan());
                            DocumentTableCellHelper.setRowSpan(documentTableCell, innerDocumentCell.getRowSpan());
                            return documentTableCell;
                        })
                        .collect(Collectors.toList()));
                    DocumentTableHelper.setBoundingRegions(documentTable,
                        toBoundingRegions(innerDocumentTable.getBoundingRegions()));
                    DocumentTableHelper.setSpans(documentTable, toDocumentSpans(innerDocumentTable.getSpans()));
                    DocumentTableHelper.setColumnCount(documentTable, innerDocumentTable.getColumnCount());
                    DocumentTableHelper.setRowCount(documentTable, innerDocumentTable.getRowCount());
                    return documentTable;
                })
                .collect(Collectors.toList()));
        }

        return analyzeResult;
    }

    private static List<DocumentImage> fromInnerImages(List<com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentImage> innerImages) {
        if (innerImages != null) {
            return innerImages
                .stream()
                .map(innerImage -> {
                    DocumentImage documentImage = new DocumentImage();
                    DocumentImageHelper.setSpan(documentImage, getDocumentSpan(innerImage.getSpan()));
                    DocumentImageHelper.setBoundingPolygon(documentImage, toPolygonPoints(innerImage.getPolygon()));
                    DocumentImageHelper.setPageNumber(documentImage, documentImage.getPageNumber());
                    DocumentImageHelper.setConfidence(documentImage, documentImage.getConfidence());
                    return documentImage;
                })
                .collect(Collectors.toList());
        }
        return null;
    }

    private static List<DocumentBarcode> fromInnerBarcodes(
        List<com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentBarcode> barcodes) {
        if (barcodes != null) {
            return barcodes.stream().map(innerBarcode -> {
                DocumentBarcode documentBarcode = new DocumentBarcode();
                DocumentBarcodeHelper.setKind(documentBarcode,
                    innerBarcode.getKind() == null ? null : DocumentBarcodeKind.fromString(
                        innerBarcode.getKind().toString()));
                DocumentBarcodeHelper.setValue(documentBarcode, innerBarcode.getValue());
                DocumentBarcodeHelper.setConfidence(documentBarcode, innerBarcode.getConfidence());
                DocumentBarcodeHelper.setSpan(documentBarcode, getDocumentSpan(innerBarcode.getSpan()));
                return documentBarcode;
            })
                .collect(Collectors.toList());
        }
        return null;
    }

    private static List<DocumentFormula> fromInnerFormulas(
        List<com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFormula> formulas) {
        if (formulas != null) {
            return formulas.stream().map(innerFormula -> {
                DocumentFormula documentFormula = new DocumentFormula();
                DocumentFormulaHelper.setKind(documentFormula,
                    innerFormula.getKind() == null ? null : DocumentFormulaKind.fromString(
                        innerFormula.getKind().toString()));
                DocumentFormulaHelper.setValue(documentFormula, innerFormula.getValue());
                DocumentFormulaHelper.setConfidence(documentFormula, innerFormula.getConfidence());
                DocumentFormulaHelper.setSpan(documentFormula, getDocumentSpan(innerFormula.getSpan()));
                return documentFormula;
            }).collect(Collectors.toList());
        }
        return null;
    }

    private static List<DocumentAnnotation> fromInnerAnnotations(
        List<com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentAnnotation> annotations) {
        if (annotations != null) {
            return annotations.stream().map(innerAnnotation -> {
                DocumentAnnotation documentAnnotation = new DocumentAnnotation();
                DocumentAnnotationHelper.setKind(documentAnnotation,
                    innerAnnotation.getKind() == null ? null : DocumentAnnotationKind.fromString(
                        innerAnnotation.getKind().toString()));
                DocumentAnnotationHelper.setConfidence(documentAnnotation, innerAnnotation.getConfidence());
                DocumentAnnotationHelper.setPolygon(documentAnnotation, toPolygonPoints(innerAnnotation.getPolygon()));
                return documentAnnotation;
            })
                .collect(Collectors.toList());
        }
        return null;
    }

    public static BuildDocumentModelRequest getBuildDocumentModelRequest(String blobContainerUrl,
                                                                         DocumentModelBuildMode buildMode, String modelId, String prefix, String fileList, BuildDocumentModelOptions buildDocumentModelOptions) {
        BuildDocumentModelRequest buildDocumentModelRequest = new BuildDocumentModelRequest(modelId,
            com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentBuildMode
                .fromString(buildMode.toString()))
            .setDescription(buildDocumentModelOptions.getDescription())
            .setTags(buildDocumentModelOptions.getTags());
        if (fileList == null) {
            buildDocumentModelRequest.setAzureBlobSource(new com.azure.ai.formrecognizer.documentanalysis.implementation.models.AzureBlobContentSource(blobContainerUrl)
                .setPrefix(prefix));
        } else {
            buildDocumentModelRequest.setAzureBlobFileListSource(new com.azure.ai.formrecognizer.documentanalysis.implementation.models.AzureBlobFileListSource(blobContainerUrl, fileList));
        }
        return buildDocumentModelRequest;
    }

    public static BuildDocumentClassifierRequest getBuildDocumentClassifierRequest(String classifierId,
                                                                                   String description, Map<String, com.azure.ai.formrecognizer.documentanalysis.implementation.models.ClassifierDocumentTypeDetails> docTypes) {
        BuildDocumentClassifierRequest buildDocumentClassifierRequest
            = new BuildDocumentClassifierRequest(classifierId, docTypes)
            .setDescription(description);
        return buildDocumentClassifierRequest;
    }

    /**
     * Mapping a {@link ErrorResponseException} to {@link HttpResponseException}.
     *
     * @param throwable A {@link Throwable}.
     * @return A {@link HttpResponseException} or the original throwable type.
     */
    public static Throwable mapToHttpResponseExceptionIfExists(Throwable throwable) {
        if (throwable instanceof ErrorResponseException) {
            return getHttpResponseException((ErrorResponseException) throwable);
        }
        return throwable;
    }

    public static HttpResponseException getHttpResponseException(ErrorResponseException throwable) {
        ErrorResponseException errorResponseException = throwable;
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.Error error = null;
        if (errorResponseException.getValue() != null && errorResponseException.getValue().getError() != null) {
            error = (errorResponseException.getValue().getError());
        }
        return new HttpResponseException(
            errorResponseException.getMessage(),
            errorResponseException.getResponse(),
            toResponseError(error)
        );
    }

    public static HttpResponseException mapResponseErrorToHttpResponseException(com.azure.ai.formrecognizer.documentanalysis.implementation.models.Error error) {
        return new HttpResponseException(
            error.getMessage(),
            null,
            toResponseError(error)
        );
    }

    public static DocumentModelCopyAuthorization toCopyAuthorization(
        CopyAuthorization innerCopyAuthorization) {
        return new DocumentModelCopyAuthorization(innerCopyAuthorization.getTargetResourceId(),
            innerCopyAuthorization.getTargetResourceRegion(),
            innerCopyAuthorization.getTargetModelId(),
            innerCopyAuthorization.getTargetModelLocation(),
            innerCopyAuthorization.getAccessToken(),
            innerCopyAuthorization.getExpirationDateTime());
    }

    public static ResourceDetails toAccountProperties(
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.ResourceDetails getInfoResponse) {
        ResourceDetails resourceDetails = new ResourceDetails();
        ResourceDetailsHelper.setDocumentModelCount(resourceDetails,
            getInfoResponse.getCustomDocumentModels().getCount());
        ResourceDetailsHelper.setDocumentModelLimit(resourceDetails,
            getInfoResponse.getCustomDocumentModels().getLimit());
        ResourceDetailsHelper.setCustomNeuralDocumentModelBuilds(resourceDetails, toQuotaDetails(getInfoResponse.getCustomNeuralDocumentModelBuilds()));
        return resourceDetails;
    }

    public static DocumentModelDetails toDocumentModelFromOperationId(com.azure.ai.formrecognizer.documentanalysis.implementation.models.OperationDetails operationDetails) {
        if (operationDetails instanceof com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelBuildOperationDetails) {
            com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelDetails
                buildOperationModelResult = ((com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelBuildOperationDetails) operationDetails).getResult();
            return toDocumentModelDetails(buildOperationModelResult);
        } else if (operationDetails instanceof com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelCopyToOperationDetails) {
            com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelDetails
                copyOperationModelResult = ((com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelCopyToOperationDetails) operationDetails).getResult();
            return toDocumentModelDetails(copyOperationModelResult);
        } else if (operationDetails instanceof com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelComposeOperationDetails) {
            com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelDetails
                composeOperationModelResult = ((com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelComposeOperationDetails) operationDetails).getResult();
            return toDocumentModelDetails(composeOperationModelResult);
        }
        return new DocumentModelDetails();
    }

    public static DocumentClassifierDetails toDocumentClassifierFromOperationId(com.azure.ai.formrecognizer.documentanalysis.implementation.models.OperationDetails operationDetails) {
        if (operationDetails instanceof com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentClassifierBuildOperationDetails) {
            com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentClassifierDetails
                classifierDetails =
                ((com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentClassifierBuildOperationDetails) operationDetails).getResult();
            return fromInnerDocumentClassifierDetails(classifierDetails);
        }
        return null;
    }

    public static DocumentModelDetails toDocumentModelDetails(com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelDetails modelDetails) {
        DocumentModelDetails documentModelDetails = new DocumentModelDetails();
        if (modelDetails != null) {
            DocumentModelDetailsHelper.setModelId(documentModelDetails, modelDetails.getModelId());
            DocumentModelDetailsHelper.setDescription(documentModelDetails, modelDetails.getDescription());
            Map<String, DocumentTypeDetails> docTypeMap = getStringDocTypeInfoMap(modelDetails);
            DocumentModelDetailsHelper.setDocTypes(documentModelDetails, docTypeMap);
            DocumentModelDetailsHelper.setCreatedOn(documentModelDetails, modelDetails.getCreatedDateTime());
            DocumentModelDetailsHelper.setTags(documentModelDetails, modelDetails.getTags());
            DocumentModelDetailsHelper.setExpiresOn(documentModelDetails, modelDetails.getExpirationDateTime());
        }
        return documentModelDetails;
    }

    private static Map<String, DocumentTypeDetails> getStringDocTypeInfoMap(
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelDetails modelInfo) {
        if (!CoreUtils.isNullOrEmpty(modelInfo.getDocTypes())) {
            Map<String, DocumentTypeDetails> docTypeMap = new HashMap<>();
            modelInfo.getDocTypes().forEach((key, innerDocTypeInfo) -> {
                DocumentTypeDetails documentTypeDetails = new DocumentTypeDetails();
                DocumentTypeDetailsHelper.setDescription(documentTypeDetails, innerDocTypeInfo.getDescription());
                Map<String, DocumentFieldSchema> schemaMap = new HashMap<>();
                innerDocTypeInfo.getFieldSchema().forEach((schemaKey, innerDocSchema)
                    -> schemaMap.put(schemaKey, toDocumentFieldSchema(innerDocSchema)));
                DocumentTypeDetailsHelper.setFieldSchema(documentTypeDetails, schemaMap);
                DocumentTypeDetailsHelper.setFieldConfidence(documentTypeDetails, innerDocTypeInfo.getFieldConfidence());
                docTypeMap.put(key, documentTypeDetails);
                DocumentTypeDetailsHelper.setBuildMode(documentTypeDetails,
                    innerDocTypeInfo.getBuildMode() != null
                        ? DocumentModelBuildMode.fromString(innerDocTypeInfo.getBuildMode().toString())
                        : null);
            });
            return docTypeMap;
        }
        return  null;
    }

    private static DocumentFieldSchema toDocumentFieldSchema(
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldSchema innerDocSchema) {
        if (innerDocSchema != null) {
            DocumentFieldSchema documentFieldSchema = new DocumentFieldSchema();
            DocumentFieldSchemaHelper.setDescription(documentFieldSchema, innerDocSchema.getDescription());
            DocumentFieldSchemaHelper.setExample(documentFieldSchema, innerDocSchema.getExample());
            DocumentFieldSchemaHelper.setType(documentFieldSchema,
                DocumentFieldType.fromString(innerDocSchema.getType().toString()));
            if (innerDocSchema.getItems() != null) {
                DocumentFieldSchemaHelper.setItems(documentFieldSchema,
                    toDocumentFieldSchema(innerDocSchema.getItems()));
            }
            if (!CoreUtils.isNullOrEmpty(innerDocSchema.getProperties())) {
                DocumentFieldSchemaHelper.setProperties(documentFieldSchema,
                    toDocumentFieldProperties(innerDocSchema.getProperties()));
            }
            return documentFieldSchema;
        }
        return null;
    }

    private static Map<String, DocumentFieldSchema> toDocumentFieldProperties(
        Map<String, com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldSchema> properties) {
        Map<String, DocumentFieldSchema> schemaMap = new HashMap<>();
        properties.forEach((key, innerDocFieldSchema) ->
            schemaMap.put(key, toDocumentFieldSchema(innerDocFieldSchema)));
        return schemaMap;
    }

    private static DocumentKeyValueElement toDocumentKeyValueElement(
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentKeyValueElement innerDocKeyValElement) {
        if (innerDocKeyValElement == null) {
            return null;
        }
        DocumentKeyValueElement documentKeyValueElement = new DocumentKeyValueElement();
        DocumentKeyValueElementHelper.setContent(documentKeyValueElement, innerDocKeyValElement.getContent());
        DocumentKeyValueElementHelper.setBoundingRegions(documentKeyValueElement,
            toBoundingRegions(innerDocKeyValElement.getBoundingRegions()));
        DocumentKeyValueElementHelper.setSpans(documentKeyValueElement,
            toDocumentSpans(innerDocKeyValElement.getSpans()));
        return documentKeyValueElement;
    }

    private static Map<String, DocumentField> toDocumentFields(
        Map<String, com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentField> innerFields) {
        Map<String, DocumentField> documentFieldMap = new HashMap<>();
        innerFields.forEach((key, innerDocumentField) ->
            documentFieldMap.put(key, toDocumentField(innerDocumentField)));
        return documentFieldMap;
    }

    private static DocumentField toDocumentField(
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentField innerDocumentField) {
        DocumentField documentField = new DocumentField();

        DocumentFieldHelper.setType(documentField,
            DocumentFieldType.fromString(innerDocumentField.getType().toString()));
        DocumentFieldHelper.setBoundingRegions(documentField,
            toBoundingRegions(innerDocumentField.getBoundingRegions()));
        DocumentFieldHelper.setContent(documentField, innerDocumentField.getContent());
        DocumentFieldHelper.setSpans(documentField, toDocumentSpans(innerDocumentField.getSpans()));
        DocumentFieldHelper.setConfidence(documentField, innerDocumentField.getConfidence());
        setDocumentFieldValue(innerDocumentField, documentField);
        return documentField;
    }

    private static void setDocumentFieldValue(
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentField innerDocumentField, DocumentField documentField) {

        if (com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldType.STRING.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValue(documentField, innerDocumentField.getValueString());
        } else if (com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldType.DATE.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValue(documentField, innerDocumentField.getValueDate());
        } else if (com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldType.TIME.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValue(documentField, innerDocumentField.getValueTime() == null
                ? null : LocalTime.parse(innerDocumentField.getValueTime(),
                DateTimeFormatter.ofPattern("HH:mm:ss")));
        } else if (com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldType.PHONE_NUMBER.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValue(documentField, innerDocumentField.getValuePhoneNumber());
        } else if (com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldType.NUMBER.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValue(documentField,
                innerDocumentField.getValueNumber() == null
                    ? null : Double.valueOf(innerDocumentField.getValueNumber().doubleValue()));
        } else if (com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldType.INTEGER.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValue(documentField, innerDocumentField.getValueInteger());
        } else if (com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldType.SELECTION_MARK.equals(
            innerDocumentField.getType())) {
            if (innerDocumentField.getValueSelectionMark() == null) {
                DocumentFieldHelper.setValue(documentField, null);
            } else {
                DocumentFieldHelper.setValue(documentField,
                    DocumentSelectionMarkState.fromString(innerDocumentField.getValueSelectionMark().toString()));
            }
        } else if (com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldType.COUNTRY_REGION.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValue(documentField, innerDocumentField.getValueCountryRegion());
        } else if (com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldType.SIGNATURE.equals(
            innerDocumentField.getType())) {
            if (innerDocumentField.getValueSignature() != null) {
                DocumentFieldHelper.setValue(documentField,
                    DocumentSignatureType.fromString(innerDocumentField.getValueSignature().toString()));
            }
        } else if (com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldType.ARRAY.equals(
            innerDocumentField.getType())) {
            if (CoreUtils.isNullOrEmpty(innerDocumentField.getValueArray())) {
                DocumentFieldHelper.setValue(documentField, null);
            } else {
                DocumentFieldHelper.setValue(documentField, innerDocumentField.getValueArray()
                    .stream()
                    .map(Transforms::toDocumentField)
                    .collect(Collectors.toList()));
            }
        } else if (com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldType.OBJECT.equals(
            innerDocumentField.getType())) {
            if (CoreUtils.isNullOrEmpty(innerDocumentField.getValueObject())) {
                DocumentFieldHelper.setValue(documentField, null);
            } else {
                HashMap<String, DocumentField> documentFieldMap = new HashMap<>();
                innerDocumentField.getValueObject()
                    .forEach((key, innerMapDocumentField)
                        -> documentFieldMap.put(key, toDocumentField(innerMapDocumentField)));
                DocumentFieldHelper.setValue(documentField, documentFieldMap);
            }
        } else if (com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldType.CURRENCY.equals(
            innerDocumentField.getType())) {
            if (innerDocumentField.getValueCurrency() == null) {
                DocumentFieldHelper.setValue(documentField, null);
            } else {
                CurrencyValue currencyValue = new CurrencyValue();
                CurrencyValueHelper.setAmount(currencyValue, innerDocumentField.getValueCurrency().getAmount());
                CurrencyValueHelper.setCurrencySymbol(currencyValue,
                    innerDocumentField.getValueCurrency().getCurrencySymbol());
                DocumentFieldHelper.setValue(documentField, currencyValue);
            }
        } else if (com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentFieldType.ADDRESS.equals(
            innerDocumentField.getType())) {
            if (innerDocumentField.getValueAddress() == null) {
                DocumentFieldHelper.setValue(documentField, null);
            } else {
                AddressValue addressValue = new AddressValue();
                AddressValueHelper.setCity(addressValue, innerDocumentField.getValueAddress().getCity());
                AddressValueHelper.setStreetAddress(addressValue,
                    innerDocumentField.getValueAddress().getStreetAddress());
                AddressValueHelper.setCountryRegion(addressValue,
                    innerDocumentField.getValueAddress().getCountryRegion());
                AddressValueHelper.setHouseNumber(addressValue, innerDocumentField.getValueAddress().getHouseNumber());
                AddressValueHelper.setRoad(addressValue, innerDocumentField.getValueAddress().getRoad());
                AddressValueHelper.setPoBox(addressValue, innerDocumentField.getValueAddress().getPoBox());
                AddressValueHelper.setPostalCode(addressValue, innerDocumentField.getValueAddress().getPostalCode());
                AddressValueHelper.setState(addressValue, innerDocumentField.getValueAddress().getState());
                DocumentFieldHelper.setValue(documentField, addressValue);
            }
        }
    }

    private static List<DocumentSpan> toDocumentSpans(
        List<com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentSpan> innerDocumentSpans) {
        if (!CoreUtils.isNullOrEmpty(innerDocumentSpans)) {
            return innerDocumentSpans
                .stream()
                .map(Transforms::getDocumentSpan)
                .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private static DocumentSpan getDocumentSpan(
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentSpan innerDocumentSpan) {
        DocumentSpan documentSpan = new DocumentSpan();
        DocumentSpanHelper.setLength(documentSpan, innerDocumentSpan.getLength());
        DocumentSpanHelper.setOffset(documentSpan, innerDocumentSpan.getOffset());
        return documentSpan;
    }

    private static List<BoundingRegion> toBoundingRegions(
        List<com.azure.ai.formrecognizer.documentanalysis.implementation.models.BoundingRegion> innerBoundingRegions) {
        if (!CoreUtils.isNullOrEmpty(innerBoundingRegions)) {
            return innerBoundingRegions
                .stream()
                .map(innerBoundingRegion -> {
                    BoundingRegion boundingRegion = new BoundingRegion();
                    BoundingRegionHelper.setBoundingPolygon(boundingRegion, toPolygonPoints(innerBoundingRegion.getPolygon()));
                    BoundingRegionHelper.setPageNumber(boundingRegion, innerBoundingRegion.getPageNumber());
                    return boundingRegion;
                })
                .collect(Collectors.toList());
        } else {
            return null;
        }
    }

    private static List<Point> toPolygonPoints(List<Float> polygonValues) {
        if (CoreUtils.isNullOrEmpty(polygonValues) || (polygonValues.size() % 2) != 0) {
            return null;
        }
        List<Point> pointList = new ArrayList<>();
        for (int i = 0; i < polygonValues.size(); i++) {
            Point polygonPoint = new Point();
            PointHelper.setX(polygonPoint, polygonValues.get(i));
            PointHelper.setY(polygonPoint, polygonValues.get(++i));
            pointList.add(polygonPoint);
        }
        return pointList;
    }

    public static List<DocumentModelSummary> toDocumentModelInfo(
        List<com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelSummary> modelSummaryList) {
        return modelSummaryList
            .stream()
            .map(modelSummary -> {
                DocumentModelSummary documentModelSummary = new DocumentModelSummary();
                DocumentModelSummaryHelper.setModelId(documentModelSummary, modelSummary.getModelId());
                DocumentModelSummaryHelper.setDescription(documentModelSummary, modelSummary.getDescription());
                DocumentModelSummaryHelper.setCreatedOn(documentModelSummary, modelSummary.getCreatedDateTime());
                DocumentModelSummaryHelper.setTags(documentModelSummary, modelSummary.getTags());
                DocumentModelSummaryHelper.setExpiresOn(documentModelSummary, modelSummary.getExpirationDateTime());
                return documentModelSummary;
            }).collect(Collectors.toList());
    }

    public static OperationDetails toOperationDetails(com.azure.ai.formrecognizer.documentanalysis.implementation.models.OperationDetails innerOperationDetails) {
        OperationDetails operationDetails = null;
        if (innerOperationDetails != null) {
            if (innerOperationDetails instanceof com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelBuildOperationDetails) {
                DocumentModelBuildOperationDetails buildOperationDetails = new DocumentModelBuildOperationDetails();
                DocumentModelBuildOperationDetailsHelper.setResult(buildOperationDetails,
                    toDocumentModelDetails(((com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelBuildOperationDetails) innerOperationDetails).getResult()));
                operationDetails = buildOperationDetails;
            } else if (innerOperationDetails instanceof com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelComposeOperationDetails) {
                DocumentModelComposeOperationDetails composeOperationDetails = new DocumentModelComposeOperationDetails();
                DocumentModelComposeOperationDetailsHelper.setResult(composeOperationDetails,
                    toDocumentModelDetails(((com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelComposeOperationDetails) innerOperationDetails).getResult()));
                operationDetails = composeOperationDetails;
            } else if (innerOperationDetails instanceof com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelCopyToOperationDetails) {
                DocumentModelCopyToOperationDetails copyToOperationDetails = new DocumentModelCopyToOperationDetails();
                DocumentModelCopyToOperationDetailsHelper.setResult(copyToOperationDetails,
                    toDocumentModelDetails(((com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentModelCopyToOperationDetails) innerOperationDetails).getResult()));
                operationDetails = copyToOperationDetails;
            }
            OperationDetailsHelper.setOperationId(operationDetails, innerOperationDetails.getOperationId());
            OperationDetailsHelper.setCreatedOn(operationDetails, innerOperationDetails.getCreatedDateTime());
            OperationDetailsHelper.setLastUpdatedOn(operationDetails, innerOperationDetails.getLastUpdatedDateTime());
            OperationDetailsHelper.setPercentCompleted(operationDetails,
                innerOperationDetails.getPercentCompleted() == null ? Integer.valueOf(0)
                    : innerOperationDetails.getPercentCompleted());
            OperationDetailsHelper.setStatus(operationDetails,
                OperationStatus.fromString(innerOperationDetails.getStatus().toString()));
            OperationDetailsHelper.setResourceLocation(operationDetails, innerOperationDetails.getResourceLocation());
            OperationDetailsHelper.setError(operationDetails, toResponseError(innerOperationDetails.getError()));
            OperationDetailsHelper.setTags(operationDetails, innerOperationDetails.getTags());
        }
        return operationDetails;
    }

    public static List<OperationSummary> toOperationSummary(List<com.azure.ai.formrecognizer.documentanalysis.implementation.models.OperationSummary> operationSummary) {
        return operationSummary
            .stream()
            .map(operationSummaryItem -> {
                OperationSummary documentModelOperationSummary = new OperationSummary();
                OperationSummaryHelper.setOperationId(documentModelOperationSummary, operationSummaryItem.getOperationId());
                OperationSummaryHelper.setCreatedOn(documentModelOperationSummary, operationSummaryItem.getCreatedDateTime());
                OperationSummaryHelper.setKind(documentModelOperationSummary, operationSummaryItem.getKind() == null
                    ? null : OperationKind.fromString(operationSummaryItem.getKind().toString()));
                OperationSummaryHelper.setLastUpdatedOn(documentModelOperationSummary, operationSummaryItem.getLastUpdatedDateTime());
                OperationSummaryHelper.setPercentCompleted(documentModelOperationSummary,
                    operationSummaryItem.getPercentCompleted() == null ? Integer.valueOf(0)
                        : operationSummaryItem.getPercentCompleted());
                OperationSummaryHelper.setStatus(documentModelOperationSummary, operationSummaryItem.getStatus() == null
                    ? null : OperationStatus.fromString(operationSummaryItem.getStatus().toString()));
                OperationSummaryHelper.setResourceLocation(documentModelOperationSummary, operationSummaryItem.getResourceLocation());
                OperationSummaryHelper.setTags(documentModelOperationSummary, operationSummaryItem.getTags());
                return documentModelOperationSummary;
            }).collect(Collectors.toList());
    }

    public static OperationResult toDocumentOperationResult(
        String operationLocation) {
        OperationResult operationResult = new OperationResult();
        OperationResultHelper.setResultId(
            operationResult,
            Utility.parseResultId(operationLocation));

        return operationResult;
    }

    public static AuthorizeCopyRequest getAuthorizeCopyRequest(CopyAuthorizationOptions copyAuthorizationOptions,
                                                               String modelId) {
        return new AuthorizeCopyRequest(modelId)
            .setDescription(copyAuthorizationOptions.getDescription())
            .setTags(copyAuthorizationOptions.getTags());
    }

    public static ComposeDocumentModelRequest getComposeDocumentModelRequest(List<String> componentModelIds,
                                                                             ComposeDocumentModelOptions composeDocumentModelOptions,
                                                                             String modelId) {
        return new ComposeDocumentModelRequest(modelId, componentModelIds.stream()
            .map(modelIdString -> new ComponentDocumentModelDetails(modelIdString))
            .collect(Collectors.toList()))
            .setDescription(composeDocumentModelOptions.getDescription())
            .setTags(composeDocumentModelOptions.getTags());
    }

    public static CopyAuthorization getInnerCopyAuthorization(DocumentModelCopyAuthorization target) {
        CopyAuthorization copyRequest
            = new CopyAuthorization(target.getTargetResourceId(),
            target.getTargetModelLocation(),
            target.getTargetModelId(),
            target.getTargetResourceRegion(),
            target.getAccessToken(),
            target.getExpiresOn());
        return copyRequest;
    }

    private static ResponseError toResponseError(com.azure.ai.formrecognizer.documentanalysis.implementation.models.Error error) {
        if (error == null) {
            return null;
        }
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.InnerError innerError = error.getInnererror();
        String message = error.getMessage();
        StringBuilder errorInformationStringBuilder = new StringBuilder().append(message);

        if (innerError != null) {
            errorInformationStringBuilder.append(", " + "errorCode" + ": [")
                .append(innerError.getCode()).append("], ").append("message")
                .append(": ").append(innerError.getMessage());
        }
        return new ResponseError(error.getCode(), errorInformationStringBuilder.toString());
    }

    private static List<DocumentWord> toDocumentWords(
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentPage innerDocumentPage) {
        return innerDocumentPage.getWords() == null ? null : innerDocumentPage.getWords()
            .stream()
            .map(innerDocumentWord -> {
                DocumentWord documentWord = new DocumentWord();
                DocumentWordHelper.setBoundingPolygon(documentWord,
                    toPolygonPoints(innerDocumentWord.getPolygon()));
                DocumentWordHelper.setConfidence(documentWord, innerDocumentWord.getConfidence());
                DocumentWordHelper.setSpan(documentWord, getDocumentSpan(innerDocumentWord.getSpan()));
                DocumentWordHelper.setContent(documentWord, innerDocumentWord.getContent());
                return documentWord;
            })
            .collect(Collectors.toList());
    }

    public static Map<String, com.azure.ai.formrecognizer.documentanalysis.implementation.models.ClassifierDocumentTypeDetails> toInnerDocTypes(Map<String, ClassifierDocumentTypeDetails> tags) {
        Map<String, com.azure.ai.formrecognizer.documentanalysis.implementation.models.ClassifierDocumentTypeDetails>
            innerTags = new HashMap<String, com.azure.ai.formrecognizer.documentanalysis.implementation.models.ClassifierDocumentTypeDetails>();
        tags.forEach((key, classifierDocumentTypeDetails) -> {
            com.azure.ai.formrecognizer.documentanalysis.implementation.models.ClassifierDocumentTypeDetails innerClassifyDocTypeDetails
                = new com.azure.ai.formrecognizer.documentanalysis.implementation.models.ClassifierDocumentTypeDetails();
            if (classifierDocumentTypeDetails.getAzureBlobFileListSource() != null) {
                innerClassifyDocTypeDetails.setAzureBlobFileListSource(
                    new com.azure.ai.formrecognizer.documentanalysis.implementation.models.AzureBlobFileListSource(
                        classifierDocumentTypeDetails.getAzureBlobFileListSource().getContainerUrl(),
                        classifierDocumentTypeDetails.getAzureBlobFileListSource().getFileList()));
            } else {
                innerClassifyDocTypeDetails.setAzureBlobSource(
                    new com.azure.ai.formrecognizer.documentanalysis.implementation.models.AzureBlobContentSource(
                        classifierDocumentTypeDetails.getAzureBlobSource().getContainerUrl())
                        .setPrefix(classifierDocumentTypeDetails.getAzureBlobSource().getPrefix()));
            }
            innerTags.put(key, innerClassifyDocTypeDetails);
        });
        return innerTags;
    }

    public static DocumentClassifierDetails fromInnerDocumentClassifierDetails(com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentClassifierDetails inner) {
        DocumentClassifierDetails classifierDetails = new DocumentClassifierDetails();
        DocumentClassifierDetailsHelper.setClassifierId(classifierDetails, inner.getClassifierId());
        DocumentClassifierDetailsHelper.setDescription(classifierDetails, inner.getDescription());
        DocumentClassifierDetailsHelper.setDocTypes(classifierDetails, fromInnerDocTypes(inner.getDocTypes()));
        DocumentClassifierDetailsHelper.setApiVersion(classifierDetails, inner.getApiVersion());
        DocumentClassifierDetailsHelper.setCreatedOn(classifierDetails, inner.getCreatedDateTime());
        DocumentClassifierDetailsHelper.setExpiresOn(classifierDetails, inner.getExpirationDateTime());

        return classifierDetails;
    }

    public static List<com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentAnalysisFeature> toInnerDocAnalysisFeatures(List<DocumentAnalysisFeature> documentAnalysisFeatures) {
        if (documentAnalysisFeatures != null) {
            return documentAnalysisFeatures
                .stream()
                .map(documentAnalysisFeature -> com.azure.ai.formrecognizer.documentanalysis.implementation.models.DocumentAnalysisFeature.fromString(documentAnalysisFeature.toString()))
                .collect(Collectors.toList());
        }
        return null;
    }

    private static Map<String, ClassifierDocumentTypeDetails> fromInnerDocTypes(
        Map<String, com.azure.ai.formrecognizer.documentanalysis.implementation.models.ClassifierDocumentTypeDetails> innerDocTypes) {
        Map<String, ClassifierDocumentTypeDetails> documentTypeDetailsMap = new HashMap<>();
        innerDocTypes.forEach((s, classifierDocumentTypeDetails) -> documentTypeDetailsMap.put(s, fromInnerClassifierDetails(classifierDocumentTypeDetails)));

        return documentTypeDetailsMap;
    }

    private static ClassifierDocumentTypeDetails fromInnerClassifierDetails(
        com.azure.ai.formrecognizer.documentanalysis.implementation.models.ClassifierDocumentTypeDetails innerClassifier) {
        ClassifierDocumentTypeDetails classifierDocumentTypeDetails = new ClassifierDocumentTypeDetails();
        if (innerClassifier.getAzureBlobSource() != null) {
            com.azure.ai.formrecognizer.documentanalysis.implementation.models.AzureBlobContentSource blobContentSource
                = innerClassifier.getAzureBlobSource();
            classifierDocumentTypeDetails.setAzureBlobSource(new AzureBlobContentSource(blobContentSource.getContainerUrl())
                .setPrefix(blobContentSource.getPrefix()));
        } else if (innerClassifier.getAzureBlobFileListSource() != null) {
            com.azure.ai.formrecognizer.documentanalysis.implementation.models.AzureBlobFileListSource listSource
                = innerClassifier.getAzureBlobFileListSource();
            classifierDocumentTypeDetails.setAzureBlobFileListSource(
                new AzureBlobFileListSource(listSource.getContainerUrl(), listSource.getFileList()));
        }
        return classifierDocumentTypeDetails;
    }

    private static QuotaDetails toQuotaDetails(com.azure.ai.formrecognizer.documentanalysis.implementation.models.QuotaDetails innerQuotaDetails) {
        QuotaDetails quotaDetails = new QuotaDetails();
        QuotaDetailsHelper.setUsed(quotaDetails, innerQuotaDetails.getUsed());
        QuotaDetailsHelper.setQuotaResetDateTime(quotaDetails, innerQuotaDetails.getQuotaResetDateTime());
        QuotaDetailsHelper.setQuota(quotaDetails, innerQuotaDetails.getQuota());
        return quotaDetails;
    }
}
