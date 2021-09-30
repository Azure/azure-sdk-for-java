// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation.util;

import com.azure.ai.formrecognizer.administration.models.AccountProperties;
import com.azure.ai.formrecognizer.administration.models.CopyAuthorization;
import com.azure.ai.formrecognizer.administration.models.DocTypeInfo;
import com.azure.ai.formrecognizer.administration.models.DocumentFieldSchema;
import com.azure.ai.formrecognizer.administration.models.DocumentModel;
import com.azure.ai.formrecognizer.administration.models.DocumentModelInfo;
import com.azure.ai.formrecognizer.administration.models.FormRecognizerError;
import com.azure.ai.formrecognizer.administration.models.InnerError;
import com.azure.ai.formrecognizer.administration.models.ModelOperation;
import com.azure.ai.formrecognizer.administration.models.ModelOperationInfo;
import com.azure.ai.formrecognizer.administration.models.ModelOperationKind;
import com.azure.ai.formrecognizer.administration.models.ModelOperationStatus;
import com.azure.ai.formrecognizer.implementation.models.Error;
import com.azure.ai.formrecognizer.implementation.models.ErrorResponseException;
import com.azure.ai.formrecognizer.implementation.models.GetInfoResponse;
import com.azure.ai.formrecognizer.implementation.models.GetOperationResponse;
import com.azure.ai.formrecognizer.implementation.models.ModelInfo;
import com.azure.ai.formrecognizer.implementation.models.ModelSummary;
import com.azure.ai.formrecognizer.implementation.models.OperationInfo;
import com.azure.ai.formrecognizer.models.AnalyzeResult;
import com.azure.ai.formrecognizer.models.AnalyzedDocument;
import com.azure.ai.formrecognizer.models.BoundingRegion;
import com.azure.ai.formrecognizer.models.DocumentAnalysisException;
import com.azure.ai.formrecognizer.models.DocumentEntity;
import com.azure.ai.formrecognizer.models.DocumentField;
import com.azure.ai.formrecognizer.models.DocumentFieldType;
import com.azure.ai.formrecognizer.models.DocumentKeyValueElement;
import com.azure.ai.formrecognizer.models.DocumentKeyValuePair;
import com.azure.ai.formrecognizer.models.DocumentLine;
import com.azure.ai.formrecognizer.models.DocumentOperationResult;
import com.azure.ai.formrecognizer.models.DocumentPage;
import com.azure.ai.formrecognizer.models.DocumentSelectionMark;
import com.azure.ai.formrecognizer.models.DocumentSignatureType;
import com.azure.ai.formrecognizer.models.DocumentSpan;
import com.azure.ai.formrecognizer.models.DocumentStyle;
import com.azure.ai.formrecognizer.models.DocumentTable;
import com.azure.ai.formrecognizer.models.DocumentTableCell;
import com.azure.ai.formrecognizer.models.DocumentTableCellKind;
import com.azure.ai.formrecognizer.models.DocumentWord;
import com.azure.ai.formrecognizer.models.LengthUnit;
import com.azure.ai.formrecognizer.models.SelectionMarkState;
import com.azure.ai.formrecognizer.models.StringIndexType;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.CoreUtils;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class to convert service level models to SDK exposed models.
 */
public class Transforms {
    public static AnalyzeResult toAnalyzeResultOperation(
        com.azure.ai.formrecognizer.implementation.models.AnalyzeResult innerAnalyzeResult) {
        AnalyzeResult analyzeResult = new AnalyzeResult();

        // add documents
        if (!CoreUtils.isNullOrEmpty(innerAnalyzeResult.getDocuments())) {
            AnalyzeResultHelper.setDocuments(analyzeResult, innerAnalyzeResult.getDocuments().stream()
                .map(document -> {
                    AnalyzedDocument analyzedDocument = new AnalyzedDocument();
                    AnalyzedDocumentHelper.setBoundingRegions(analyzedDocument, document.getBoundingRegions()
                        .stream()
                        .map(boundingRegion -> toBoundingRegion(boundingRegion))
                        .collect(Collectors.toList()));
                    AnalyzedDocumentHelper.setConfidence(analyzedDocument, document.getConfidence());
                    AnalyzedDocumentHelper.setDocType(analyzedDocument, document.getDocType());
                    AnalyzedDocumentHelper.setFields(analyzedDocument, toDocumentFields(document.getFields()));
                    AnalyzedDocumentHelper.setSpans(analyzedDocument, document.getSpans()
                        .stream()
                        .map(innerDocumentSpan -> toDocumentSpan(innerDocumentSpan))
                        .collect(Collectors.toList()));
                    return analyzedDocument;
                })
                .collect(Collectors.toList()));
        }

        AnalyzeResultHelper.setContent(analyzeResult, innerAnalyzeResult.getContent());
        AnalyzeResultHelper.setModelId(analyzeResult, innerAnalyzeResult.getModelId());
        AnalyzeResultHelper.setStringIndexType(analyzeResult,
            StringIndexType.fromString(innerAnalyzeResult.getStringIndexType().toString()));

        // add document entities
        if (!CoreUtils.isNullOrEmpty(innerAnalyzeResult.getEntities())) {
            AnalyzeResultHelper.setEntities(analyzeResult, innerAnalyzeResult.getEntities()
                .stream()
                .map(innerDocumentEntity -> {
                    DocumentEntity documentEntity = new DocumentEntity();
                    DocumentEntityHelper.setBoundingRegions(documentEntity, innerDocumentEntity.getBoundingRegions()
                        .stream()
                        .map(boundingRegion -> toBoundingRegion(boundingRegion))
                        .collect(Collectors.toList()));

                    DocumentEntityHelper.setContent(documentEntity, innerDocumentEntity.getContent());
                    DocumentEntityHelper.setConfidence(documentEntity, innerDocumentEntity.getConfidence());
                    DocumentEntityHelper.setSpans(documentEntity, innerDocumentEntity.getSpans()
                        .stream()
                        .map(innerDocumentSpan -> toDocumentSpan(innerDocumentSpan))
                        .collect(Collectors.toList()));
                    DocumentEntityHelper.setCategory(documentEntity, innerDocumentEntity.getCategory());
                    DocumentEntityHelper.setSubCategory(documentEntity, innerDocumentEntity.getSubCategory());
                    return documentEntity;
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
                DocumentPageHelper.setUnit(documentPage, LengthUnit.fromString(innerDocumentPage.getUnit().toString()));
                DocumentPageHelper.setSpans(documentPage, innerDocumentPage.getSpans()
                    .stream()
                    .map(innerDocumentSpan -> toDocumentSpan(innerDocumentSpan))
                    .collect(Collectors.toList()));
                DocumentPageHelper.setSelectionMarks(documentPage, innerDocumentPage.getSelectionMarks() == null
                    ? null
                    : innerDocumentPage.getSelectionMarks()
                    .stream()
                    .map(innerSelectionMark -> {
                        DocumentSelectionMark documentSelectionMark = new DocumentSelectionMark();
                        DocumentSelectionMarkHelper.setBoundingBox(documentSelectionMark,
                            innerSelectionMark.getBoundingBox());
                        DocumentSelectionMarkHelper.setConfidence(documentSelectionMark,
                            innerSelectionMark.getConfidence());
                        DocumentSelectionMarkHelper.setSpan(documentSelectionMark,
                            toDocumentSpan(innerSelectionMark.getSpan()));
                        DocumentSelectionMarkHelper.setState(documentSelectionMark,
                            SelectionMarkState.fromString(innerSelectionMark.getState().toString()));
                        return documentSelectionMark;
                    })
                    .collect(Collectors.toList()));
                DocumentPageHelper.setLines(documentPage,
                    innerDocumentPage.getLines() == null ? null : innerDocumentPage.getLines()
                        .stream()
                        .map(innerDocumentLine -> {
                            DocumentLine documentLine = new DocumentLine();
                            DocumentLineHelper.setBoundingBox(documentLine, innerDocumentLine.getBoundingBox());
                            DocumentLineHelper.setContent(documentLine, innerDocumentLine.getContent());
                            DocumentLineHelper.setSpans(documentLine, innerDocumentLine.getSpans()
                                .stream()
                                .map(documentSpan -> toDocumentSpan(documentSpan))
                                .collect(Collectors.toList()));
                            return documentLine;
                        })
                        .collect(Collectors.toList()));
                DocumentPageHelper.setWords(documentPage,
                    innerDocumentPage.getWords() == null ? null : innerDocumentPage.getWords()
                        .stream()
                        .map(innerDocumentWord -> {
                            DocumentWord documentWord = new DocumentWord();
                            DocumentWordHelper.setBoundingBox(documentWord, innerDocumentWord.getBoundingBox());
                            DocumentWordHelper.setConfidence(documentWord, innerDocumentWord.getConfidence());
                            DocumentWordHelper.setSpan(documentWord, toDocumentSpan(innerDocumentWord.getSpan()));
                            DocumentWordHelper.setContent(documentWord, innerDocumentWord.getContent());
                            return documentWord;
                        })
                        .collect(Collectors.toList()));
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
                    DocumentStyleHelper.setSpans(documentStyle, innerDocumentStyle.getSpans()
                        .stream()
                        .map(innerDocumentSpan -> toDocumentSpan(innerDocumentSpan))
                        .collect(Collectors.toList()));
                    return documentStyle;
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
                                innerDocumentTable.getBoundingRegions() == null
                                    ? null : innerDocumentTable.getBoundingRegions()
                                    .stream()
                                    .map(boundingRegion -> toBoundingRegion(boundingRegion))
                                    .collect(Collectors.toList()));
                            DocumentTableCellHelper.setSpans(documentTableCell,
                                innerDocumentTable.getSpans()
                                    .stream()
                                    .map(innerDocumentSpan -> toDocumentSpan(innerDocumentSpan))
                                    .collect(Collectors.toList()));
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
                        innerDocumentTable.getBoundingRegions() == null
                            ? null : innerDocumentTable.getBoundingRegions()
                            .stream()
                            .map(boundingRegion -> toBoundingRegion(boundingRegion))
                            .collect(Collectors.toList()));
                    DocumentTableHelper.setSpans(documentTable, innerDocumentTable.getSpans()
                        .stream()
                        .map(innerDocumentSpan -> toDocumentSpan(innerDocumentSpan))
                        .collect(Collectors.toList()));
                    DocumentTableHelper.setColumnCount(documentTable, innerDocumentTable.getColumnCount());
                    DocumentTableHelper.setRowCount(documentTable, innerDocumentTable.getRowCount());
                    return documentTable;
                })
                .collect(Collectors.toList()));
        }

        return analyzeResult;
    }

    /**
     * Mapping a {@link ErrorResponseException} to {@link HttpResponseException} if exists. Otherwise, return
     * original {@link Throwable}.
     *
     * @param throwable A {@link Throwable}.s
     * @return A {@link HttpResponseException} or the original throwable type.
     */
    public static Throwable mapToHttpResponseExceptionIfExists(Throwable throwable) {
        if (throwable instanceof ErrorResponseException) {
            ErrorResponseException errorResponseException = (ErrorResponseException) throwable;
            Error error = null;
            if (errorResponseException.getValue() != null && errorResponseException.getValue().getError() != null) {
                error = (errorResponseException.getValue().getError());
            }
            return new HttpResponseException(
                errorResponseException.getMessage(),
                errorResponseException.getResponse(),
                toFormRecognizerError(error)
            );
        }
        return throwable;
    }

    public static CopyAuthorization toCopyAuthorization(
        com.azure.ai.formrecognizer.implementation.models.CopyAuthorization innerCopyAuthorization) {
        CopyAuthorization copyAuthorization = new CopyAuthorization();
        CopyAuthorizationHelper.setTargetModelId(copyAuthorization, innerCopyAuthorization.getTargetModelId());
        CopyAuthorizationHelper.setAccessToken(copyAuthorization, innerCopyAuthorization.getAccessToken());
        CopyAuthorizationHelper.setExpirationDateTime(copyAuthorization,
            innerCopyAuthorization.getExpirationDateTime());
        CopyAuthorizationHelper.setTargetModelLocation(copyAuthorization,
            innerCopyAuthorization.getTargetModelLocation());
        CopyAuthorizationHelper.setTargetResourceId(copyAuthorization, innerCopyAuthorization.getTargetResourceId());
        CopyAuthorizationHelper.setTargetResourceRegion(copyAuthorization,
            innerCopyAuthorization.getTargetResourceRegion());
        return copyAuthorization;
    }

    public static AccountProperties toAccountProperties(GetInfoResponse getInfoResponse) {
        AccountProperties accountProperties = new AccountProperties();
        AccountPropertiesHelper.setDocumentModelCount(accountProperties,
            getInfoResponse.getCustomDocumentModels().getCount());
        AccountPropertiesHelper.setDocumentModelLimit(accountProperties,
            getInfoResponse.getCustomDocumentModels().getLimit());
        return accountProperties;
    }

    public static DocumentModel toDocumentModel(ModelInfo modelInfo) {
        DocumentModel documentModel = new DocumentModel();
        DocumentModelHelper.setModelId(documentModel, modelInfo.getModelId());
        DocumentModelHelper.setDescription(documentModel, modelInfo.getDescription());
        Map<String, DocTypeInfo> docTypeMap = getStringDocTypeInfoMap(modelInfo);
        DocumentModelHelper.setDocTypes(documentModel, docTypeMap);
        DocumentModelHelper.setCreatedOn(documentModel, modelInfo.getCreatedDateTime());
        return documentModel;
    }

    private static Map<String, DocTypeInfo> getStringDocTypeInfoMap(ModelInfo modelInfo) {
        if (!CoreUtils.isNullOrEmpty(modelInfo.getDocTypes())) {
            Map<String, DocTypeInfo> docTypeMap = new HashMap<>();
            modelInfo.getDocTypes().forEach((key, innerDocTypeInfo) -> {
                DocTypeInfo docTypeInfo = new DocTypeInfo();
                DocTypeInfoHelper.setDescription(docTypeInfo, innerDocTypeInfo.getDescription());
                Map<String, DocumentFieldSchema> schemaMap = new HashMap<>();
                innerDocTypeInfo.getFieldSchema().forEach((schemaKey, innerDocSchema)
                    -> schemaMap.put(schemaKey, toDocumentFieldSchema(innerDocSchema)));
                DocTypeInfoHelper.setFieldSchema(docTypeInfo, schemaMap);
                DocTypeInfoHelper.setFieldConfidence(docTypeInfo, innerDocTypeInfo.getFieldConfidence());
                docTypeMap.put(key, docTypeInfo);
            });
            return docTypeMap;
        }
        return  null;
    }

    private static DocumentFieldSchema toDocumentFieldSchema(
        com.azure.ai.formrecognizer.implementation.models.DocumentFieldSchema innerDocSchema) {
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
        Map<String, com.azure.ai.formrecognizer.implementation.models.DocumentFieldSchema> properties) {
        Map<String, DocumentFieldSchema> schemaMap = new HashMap<>();
        properties.forEach((key, innerDocFieldSchema) ->
            schemaMap.put(key, toDocumentFieldSchema(innerDocFieldSchema)));
        return schemaMap;
    }

    private static DocumentKeyValueElement toDocumentKeyValueElement(
        com.azure.ai.formrecognizer.implementation.models.DocumentKeyValueElement innerDocKeyValElement) {
        if (innerDocKeyValElement == null) {
            return null;
        }
        DocumentKeyValueElement documentKeyValueElement = new DocumentKeyValueElement();
        DocumentKeyValueElementHelper.setContent(documentKeyValueElement, innerDocKeyValElement.getContent());
        DocumentKeyValueElementHelper.setBoundingRegions(documentKeyValueElement,
            innerDocKeyValElement.getBoundingRegions() == null
                ? null
                : innerDocKeyValElement.getBoundingRegions()
                    .stream()
                    .map(innerBoundingRegion -> toBoundingRegion(innerBoundingRegion))
                    .collect(Collectors.toList()));
        DocumentKeyValueElementHelper.setSpans(documentKeyValueElement, innerDocKeyValElement.getSpans()
            .stream()
            .map(innerDocumentSpan -> toDocumentSpan(innerDocumentSpan))
            .collect(Collectors.toList()));
        return documentKeyValueElement;
    }

    private static Map<String, DocumentField> toDocumentFields(
        Map<String, com.azure.ai.formrecognizer.implementation.models.DocumentField> innerFields) {
        Map<String, DocumentField> documentFieldMap = new HashMap<>();
        innerFields.forEach((key, innerDocumentField) ->
            documentFieldMap.put(key, toDocumentField(innerDocumentField)));
        return documentFieldMap;
    }

    private static DocumentField toDocumentField(
        com.azure.ai.formrecognizer.implementation.models.DocumentField innerDocumentField) {
        DocumentField documentField = new DocumentField();

        DocumentFieldHelper.setType(documentField,
            DocumentFieldType.fromString(innerDocumentField.getType().toString()));
        DocumentFieldHelper.setBoundingRegions(documentField,
            innerDocumentField.getBoundingRegions() == null
                ? null
                : innerDocumentField.getBoundingRegions().stream()
                .map(boundingRegion -> toBoundingRegion(boundingRegion))
                .collect(Collectors.toList()));
        DocumentFieldHelper.setContent(documentField, innerDocumentField.getContent());
        DocumentFieldHelper.setSpans(documentField, innerDocumentField.getSpans() == null
            ? null
            : innerDocumentField.getSpans()
            .stream()
            .map(innerDocumentSpan -> toDocumentSpan(innerDocumentSpan))
            .collect(Collectors.toList()));
        setDocumentFieldValue(innerDocumentField, documentField);
        return documentField;
    }

    private static void setDocumentFieldValue(
        com.azure.ai.formrecognizer.implementation.models.DocumentField innerDocumentField, DocumentField documentField) {

        if (com.azure.ai.formrecognizer.implementation.models.DocumentFieldType.STRING.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValueString(documentField, innerDocumentField.getValueString());
        } else if (com.azure.ai.formrecognizer.implementation.models.DocumentFieldType.DATE.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValueDate(documentField, innerDocumentField.getValueDate());
        } else if (com.azure.ai.formrecognizer.implementation.models.DocumentFieldType.TIME.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValueTime(documentField, innerDocumentField.getValueTime() == null
                ? null : LocalTime.parse(innerDocumentField.getValueTime(),
                DateTimeFormatter.ofPattern("HH:mm:ss")));
        } else if (com.azure.ai.formrecognizer.implementation.models.DocumentFieldType.PHONE_NUMBER.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValuePhoneNumber(documentField, innerDocumentField.getValuePhoneNumber());
        } else if (com.azure.ai.formrecognizer.implementation.models.DocumentFieldType.NUMBER.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValueNumber(documentField, innerDocumentField.getValueNumber());
        } else if (com.azure.ai.formrecognizer.implementation.models.DocumentFieldType.INTEGER.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValueInteger(documentField, innerDocumentField.getValueInteger());
        } else if (com.azure.ai.formrecognizer.implementation.models.DocumentFieldType.SELECTION_MARK.equals(
            innerDocumentField.getType())) {
            if (innerDocumentField.getValueSelectionMark() == null) {
                DocumentFieldHelper.setValueSelectionMark(documentField, null);
            } else {
                DocumentFieldHelper.setValueSelectionMark(documentField,
                    SelectionMarkState.fromString(innerDocumentField.getValueSelectionMark().toString()));
            }
        } else if (com.azure.ai.formrecognizer.implementation.models.DocumentFieldType.COUNTRY_REGION.equals(
            innerDocumentField.getType())) {
            DocumentFieldHelper.setValueCountryRegion(documentField, innerDocumentField.getValueCountryRegion());
        } else if (com.azure.ai.formrecognizer.implementation.models.DocumentFieldType.SIGNATURE.equals(
            innerDocumentField.getType())) {
            if (innerDocumentField.getValueSignature() != null) {
                DocumentFieldHelper.setValueSignature(documentField,
                    DocumentSignatureType.fromString(innerDocumentField.getValueSignature().toString()));
            }
        } else if (com.azure.ai.formrecognizer.implementation.models.DocumentFieldType.ARRAY.equals(
            innerDocumentField.getType())) {
            if (CoreUtils.isNullOrEmpty(innerDocumentField.getValueArray())) {
                DocumentFieldHelper.setValueArray(documentField, null);
            } else {
                DocumentFieldHelper.setValueArray(documentField, innerDocumentField.getValueArray()
                    .stream()
                    .map(innerArrayDocumentField -> toDocumentField(innerArrayDocumentField))
                    .collect(Collectors.toList()));
            }
        } else if (com.azure.ai.formrecognizer.implementation.models.DocumentFieldType.OBJECT.equals(
            innerDocumentField.getType())) {
            if (CoreUtils.isNullOrEmpty(innerDocumentField.getValueObject())) {
                DocumentFieldHelper.setValueObject(documentField, null);
            } else {
                HashMap<String, DocumentField> documentFieldMap = new HashMap<>();
                innerDocumentField.getValueObject()
                    .forEach((key, innerMapDocumentField)
                        -> documentFieldMap.put(key, toDocumentField(innerMapDocumentField)));
                DocumentFieldHelper.setValueObject(documentField, documentFieldMap);
            }
        }
    }

    private static DocumentSpan toDocumentSpan(
        com.azure.ai.formrecognizer.implementation.models.DocumentSpan innerDocumentSpan) {
        com.azure.ai.formrecognizer.models.DocumentSpan
            documentSpan = new com.azure.ai.formrecognizer.models.DocumentSpan();
        DocumentSpanHelper.setLength(documentSpan, innerDocumentSpan.getLength());
        DocumentSpanHelper.setOffset(documentSpan, innerDocumentSpan.getOffset());
        return documentSpan;
    }

    private static BoundingRegion toBoundingRegion(
        com.azure.ai.formrecognizer.implementation.models.BoundingRegion innerBoundingRegion) {
        BoundingRegion boundingRegion = new BoundingRegion();
        BoundingRegionHelper.setBoundingBox(boundingRegion, innerBoundingRegion.getBoundingBox());
        BoundingRegionHelper.setPageNumber(boundingRegion, innerBoundingRegion.getPageNumber());
        return boundingRegion;
    }

    public static List<DocumentModelInfo> toDocumentModelInfo(List<ModelSummary> modelSummaryList) {
        return modelSummaryList
            .stream()
            .map(modelSummary -> {
                DocumentModelInfo documentModelInfo = new DocumentModelInfo();
                DocumentModelInfoHelper.setModelId(documentModelInfo, modelSummary.getModelId());
                DocumentModelInfoHelper.setDescription(documentModelInfo, modelSummary.getDescription());
                DocumentModelInfoHelper.setCreatedOn(documentModelInfo, modelSummary.getCreatedDateTime());
                return documentModelInfo;
            }).collect(Collectors.toList());
    }

    public static ModelOperation toModelOperation(GetOperationResponse getOperationResponse) {
        ModelOperation modelOperation = new ModelOperation();
        ModelInfo modelInfo = getOperationResponse.getResult();
        ModelOperationHelper.setModelId(modelOperation, modelInfo.getModelId());
        ModelOperationHelper.setDescription(modelOperation, modelInfo.getDescription());
        ModelOperationHelper.setCreatedOn(modelOperation, modelInfo.getCreatedDateTime());
        ModelOperationHelper.setOperationId(modelOperation, getOperationResponse.getOperationId());
        ModelOperationHelper.setCreatedOn(modelOperation, getOperationResponse.getCreatedDateTime());
        ModelOperationHelper.setKind(modelOperation,
            ModelOperationKind.fromString(getOperationResponse.getKind().toString()));
        ModelOperationHelper.setLastUpdatedOn(modelOperation, getOperationResponse.getLastUpdatedDateTime());
        ModelOperationHelper.setPercentCompleted(modelOperation,
            getOperationResponse.getPercentCompleted() == null ? Integer.valueOf(0)
                : getOperationResponse.getPercentCompleted());
        ModelOperationHelper.setStatus(modelOperation,
            ModelOperationStatus.fromString(getOperationResponse.getStatus().toString()));
        ModelOperationHelper.setResourceLocation(modelOperation, getOperationResponse.getResourceLocation());
        Map<String, DocTypeInfo> docTypeMap = getStringDocTypeInfoMap(modelInfo);
        ModelOperationHelper.setDocTypes(modelOperation, docTypeMap);
        FormRecognizerError error = toFormRecognizerError(getOperationResponse.getError());
        ModelOperationHelper.setError(modelOperation, error);
        return modelOperation;
    }

    public static List<ModelOperationInfo> toModelOperationInfo(List<OperationInfo> operationInfoList) {
        return operationInfoList
            .stream()
            .map(operationInfo -> {
                ModelOperationInfo modelOperationInfo = new ModelOperationInfo();
                ModelOperationInfoHelper.setOperationId(modelOperationInfo, operationInfo.getOperationId());
                ModelOperationInfoHelper.setCreatedOn(modelOperationInfo, operationInfo.getCreatedDateTime());
                ModelOperationInfoHelper.setKind(modelOperationInfo, operationInfo.getKind() == null
                    ? null : ModelOperationKind.fromString(operationInfo.getKind().toString()));
                ModelOperationInfoHelper.setLastUpdatedOn(modelOperationInfo, operationInfo.getLastUpdatedDateTime());
                ModelOperationInfoHelper.setPercentCompleted(modelOperationInfo,
                    operationInfo.getPercentCompleted() == null ? Integer.valueOf(0)
                        : operationInfo.getPercentCompleted());
                ModelOperationInfoHelper.setStatus(modelOperationInfo,
                    ModelOperationStatus.fromString(operationInfo.getStatus().toString()));
                ModelOperationInfoHelper.setResourceLocation(modelOperationInfo, operationInfo.getResourceLocation());
                return modelOperationInfo;
            }).collect(Collectors.toList());
    }

    public static DocumentOperationResult toFormRecognizerOperationResult(
        String operationLocation) {
        DocumentOperationResult documentOperationResult = new DocumentOperationResult();
        DocumentOperationResultHelper.setResultId(
            documentOperationResult,
            Utility.parseResultId(operationLocation));

        return documentOperationResult;
    }

    public static DocumentAnalysisException toDocumentAnalysisException(Error error) {
        DocumentAnalysisException documentAnalysisException = new DocumentAnalysisException();
        FormRecognizerError formRecognizerError = toFormRecognizerError(error);
        DocumentAnalysisExceptionHelper.setErrorInformation(documentAnalysisException, formRecognizerError);
        return documentAnalysisException;
    }

    private static FormRecognizerError toFormRecognizerError(Error error) {
        if (error != null) {
            FormRecognizerError formRecognizerError = new FormRecognizerError();
            FormRecognizerErrorHelper.setCode(formRecognizerError, error.getCode());
            FormRecognizerErrorHelper.setInnerError(formRecognizerError, toInnerError(error.getInnererror()));
            FormRecognizerErrorHelper.setDetails(formRecognizerError, toErrorDetails(error.getDetails()));
            FormRecognizerErrorHelper.setMessage(formRecognizerError, error.getMessage());
            FormRecognizerErrorHelper.setTarget(formRecognizerError, error.getTarget());
            return formRecognizerError;
        }
        return null;
    }

    private static InnerError toInnerError(
        com.azure.ai.formrecognizer.implementation.models.InnerError serviceInnerError) {
        if (serviceInnerError == null) {
            return null;
        }
        InnerError innerError = new InnerError();
        InnerErrorHelper.setCode(innerError, serviceInnerError.getCode());
        InnerErrorHelper.setMessage(innerError, serviceInnerError.getMessage());
        InnerErrorHelper.setInnerError(innerError, toInnerError(serviceInnerError.getInnererror()));
        return innerError;
    }

    private static List<FormRecognizerError> toErrorDetails(List<Error> details) {
        return !CoreUtils.isNullOrEmpty(details) ? details
            .stream()
            .map(error -> toFormRecognizerError(error))
            .collect(Collectors.toList()) : null;
    }
}
