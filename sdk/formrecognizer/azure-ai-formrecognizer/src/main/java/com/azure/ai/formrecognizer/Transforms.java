// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.AppearanceHelper;
import com.azure.ai.formrecognizer.implementation.FormLineHelper;
import com.azure.ai.formrecognizer.implementation.FormPageHelper;
import com.azure.ai.formrecognizer.implementation.FormSelectionMarkHelper;
import com.azure.ai.formrecognizer.implementation.FormTableHelper;
import com.azure.ai.formrecognizer.implementation.RecognizedFormHelper;
import com.azure.ai.formrecognizer.implementation.StyleHelper;
import com.azure.ai.formrecognizer.implementation.models.AnalyzeResult;
import com.azure.ai.formrecognizer.implementation.models.DocumentResult;
import com.azure.ai.formrecognizer.implementation.models.FieldValue;
import com.azure.ai.formrecognizer.implementation.models.FieldValueSelectionMark;
import com.azure.ai.formrecognizer.implementation.models.KeyValuePair;
import com.azure.ai.formrecognizer.implementation.models.PageResult;
import com.azure.ai.formrecognizer.implementation.models.ReadResult;
import com.azure.ai.formrecognizer.implementation.models.SelectionMarkState;
import com.azure.ai.formrecognizer.implementation.models.TextLine;
import com.azure.ai.formrecognizer.implementation.models.TextWord;
import com.azure.ai.formrecognizer.models.Appearance;
import com.azure.ai.formrecognizer.models.FieldBoundingBox;
import com.azure.ai.formrecognizer.models.FieldData;
import com.azure.ai.formrecognizer.models.FieldValueType;
import com.azure.ai.formrecognizer.models.FormElement;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormLine;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormPageRange;
import com.azure.ai.formrecognizer.models.FormSelectionMark;
import com.azure.ai.formrecognizer.models.FormTable;
import com.azure.ai.formrecognizer.models.FormTableCell;
import com.azure.ai.formrecognizer.models.FormWord;
import com.azure.ai.formrecognizer.models.LengthUnit;
import com.azure.ai.formrecognizer.models.Point;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.Style;
import com.azure.ai.formrecognizer.models.TextStyle;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.azure.ai.formrecognizer.implementation.Utility.forEachWithIndex;
import static com.azure.ai.formrecognizer.implementation.models.FieldValueType.ARRAY;

/**
 * Helper class to convert service level models to SDK exposed models.
 */
final class Transforms {
    private static final ClientLogger LOGGER = new ClientLogger(Transforms.class);
    // Pattern match to find all non-digits in the provided string.
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("[^0-9]+");
    private static final float DEFAULT_CONFIDENCE_VALUE = 1.0f;
    private static final int DEFAULT_TABLE_SPAN = 1;

    private Transforms() {
    }

    /**
     * Helper method to transform the service returned {@link AnalyzeResult} to SDK model {@link RecognizedForm}.
     *
     * @param analyzeResult The service returned result for analyze custom forms.
     * @param includeFieldElements Boolean to indicate if to set reference elements data on fields.
     *
     * @param modelId the unlabeled model Id used for recognition.
     * @return The List of {@code RecognizedForm}.
     */
    static List<RecognizedForm> toRecognizedForm(AnalyzeResult analyzeResult, boolean includeFieldElements,
                                                 String modelId) {
        List<ReadResult> readResults = analyzeResult.getReadResults();
        List<DocumentResult> documentResults = analyzeResult.getDocumentResults();
        List<PageResult> pageResults = analyzeResult.getPageResults();
        List<RecognizedForm> extractedFormList;

        List<FormPage> formPages = toRecognizedLayout(analyzeResult, includeFieldElements);

        if (!CoreUtils.isNullOrEmpty(documentResults)) {
            extractedFormList = new ArrayList<>();
            for (DocumentResult documentResultItem : documentResults) {
                FormPageRange formPageRange;
                List<Integer> documentPageRange = documentResultItem.getPageRange();
                if (documentPageRange.size() == 2) {
                    formPageRange = new FormPageRange(documentPageRange.get(0), documentPageRange.get(1));
                } else {
                    formPageRange = new FormPageRange(1, 1);
                }

                Map<String, FormField> extractedFieldMap = getLabeledFieldMap(documentResultItem, readResults);
                final RecognizedForm recognizedForm = new RecognizedForm(
                    extractedFieldMap,
                    documentResultItem.getDocType(),
                    formPageRange,
                    formPages.subList(formPageRange.getFirstPageNumber() - 1, formPageRange.getLastPageNumber()));

                RecognizedFormHelper.setFormTypeConfidence(recognizedForm, documentResultItem.getDocTypeConfidence());
                if (documentResultItem.getModelId() != null) {
                    RecognizedFormHelper.setModelId(recognizedForm, documentResultItem.getModelId().toString());
                }
                extractedFormList.add(recognizedForm);
            }
        } else {
            extractedFormList = new ArrayList<>();
            forEachWithIndex(pageResults, ((index, pageResultItem) -> {
                StringBuilder formType = new StringBuilder("form-");
                int pageNumber = pageResultItem.getPage();
                Integer clusterId = pageResultItem.getClusterId();
                if (clusterId != null) {
                    formType.append(clusterId);
                }
                Map<String, FormField> extractedFieldMap = getUnlabeledFieldMap(includeFieldElements, readResults,
                    pageResultItem, pageNumber);

                final RecognizedForm recognizedForm = new RecognizedForm(
                    extractedFieldMap,
                    formType.toString(),
                    new FormPageRange(pageNumber, pageNumber),
                    Collections.singletonList(formPages.get(index)));

                RecognizedFormHelper.setModelId(recognizedForm, modelId);
                extractedFormList.add(recognizedForm);
            }));
        }
        return extractedFormList;
    }

    /**
     * Helper method to transform the service returned {@link AnalyzeResult} to SDK model {@link FormPage}.
     *
     * @param analyzeResult The service returned result for analyze layouts.
     * @param includeFieldElements Boolean to indicate if to set reference elements data on fields.
     *
     * @return The List of {@code FormPage}.
     */
    static List<FormPage> toRecognizedLayout(AnalyzeResult analyzeResult, boolean includeFieldElements) {
        List<ReadResult> readResults = analyzeResult.getReadResults();
        List<PageResult> pageResults = analyzeResult.getPageResults();
        List<FormPage> formPages = new ArrayList<>();
        boolean pageResultsIsNullOrEmpty = CoreUtils.isNullOrEmpty(pageResults);

        forEachWithIndex(readResults, ((index, readResultItem) -> {
            List<FormTable> perPageTableList = new ArrayList<>();

            // add form tables
            if (!pageResultsIsNullOrEmpty) {
                PageResult pageResultItem = pageResults.get(index);
                if (pageResultItem != null) {
                    perPageTableList = getPageTables(pageResultItem, readResults, pageResultItem.getPage());
                }
            }

            // add form lines
            List<FormLine> perPageFormLineList = new ArrayList<>();
            if (includeFieldElements && !CoreUtils.isNullOrEmpty(readResultItem.getLines())) {
                perPageFormLineList = getReadResultFormLines(readResultItem);
            }

            // add selection marks
            List<FormSelectionMark> perPageFormSelectionMarkList = new ArrayList<>();
            if (includeFieldElements && !CoreUtils.isNullOrEmpty(readResultItem.getSelectionMarks())) {
                PageResult pageResultItem = pageResults.get(index);
                perPageFormSelectionMarkList = getReadResultFormSelectionMarks(readResultItem,
                    pageResultItem.getPage());
            }

            formPages.add(getFormPage(readResultItem, perPageTableList, perPageFormLineList,
                perPageFormSelectionMarkList));
        }));

        return formPages;
    }

    /**
     * Helper method to convert the per page {@link ReadResult} item to {@link FormSelectionMark}.
     *
     * @param readResultItem The per page text extraction item result returned by the service.
     * @param pageNumber The page number.
     *
     * @return A list of {@code FormSelectionMark}.
     */
    static List<FormSelectionMark> getReadResultFormSelectionMarks(ReadResult readResultItem, int pageNumber) {
        return readResultItem.getSelectionMarks().stream()
            .map(selectionMark -> {
                final FormSelectionMark formSelectionMark = new FormSelectionMark(
                    null, toBoundingBox(selectionMark.getBoundingBox()), pageNumber);
                final SelectionMarkState selectionMarkStateImpl = selectionMark.getState();
                com.azure.ai.formrecognizer.models.SelectionMarkState selectionMarkState;
                if (SelectionMarkState.SELECTED.equals(selectionMarkStateImpl)) {
                    selectionMarkState = com.azure.ai.formrecognizer.models.SelectionMarkState.SELECTED;
                } else if (SelectionMarkState.UNSELECTED.equals(selectionMarkStateImpl)) {
                    selectionMarkState = com.azure.ai.formrecognizer.models.SelectionMarkState.UNSELECTED;
                } else {
                    throw LOGGER.logThrowableAsError(new RuntimeException(
                            String.format("%s, unsupported selection mark state.", selectionMarkStateImpl)));
                }
                FormSelectionMarkHelper.setConfidence(formSelectionMark, selectionMark.getConfidence());
                FormSelectionMarkHelper.setState(formSelectionMark, selectionMarkState);
                return formSelectionMark;
            })
            .collect(Collectors.toList());
    }

    /**
     * Helper method to get per-page table information.
     *
     * @param pageResultItem The extracted page level information returned by the service.
     * @param readResults The text extraction result returned by the service.
     * @param pageNumber The 1 based page number on which these fields exist.
     *
     * @return The list of per page {@code FormTable}.
     */
    static List<FormTable> getPageTables(PageResult pageResultItem, List<ReadResult> readResults, int pageNumber) {
        if (pageResultItem.getTables() == null) {
            return new ArrayList<>();
        } else {
            return pageResultItem.getTables().stream()
                .map(dataTable -> {
                    FormTable formTable = new FormTable(dataTable.getRows(), dataTable.getColumns(),
                        dataTable.getCells()
                            .stream()
                            .map(dataTableCell -> new FormTableCell(
                                dataTableCell.getRowIndex(), dataTableCell.getColumnIndex(),
                                dataTableCell.getRowSpan() == null ? DEFAULT_TABLE_SPAN : dataTableCell.getRowSpan(),
                                dataTableCell.getColumnSpan() == null
                                    ? DEFAULT_TABLE_SPAN : dataTableCell.getColumnSpan(),
                                dataTableCell.getText(), toBoundingBox(dataTableCell.getBoundingBox()),
                                dataTableCell.getConfidence(),
                                dataTableCell.isHeader() == null ? false : dataTableCell.isHeader(),
                                dataTableCell.isFooter() == null ? false : dataTableCell.isFooter(),
                                pageNumber, setReferenceElements(dataTableCell.getElements(), readResults)))
                            .collect(Collectors.toList()), pageNumber);

                    FormTableHelper.setBoundingBox(formTable, toBoundingBox(dataTable.getBoundingBox()));
                    return formTable;
                })
                .collect(Collectors.toList());
        }
    }

    /**
     * Helper method to convert the per page {@link ReadResult} item to {@link FormLine}.
     *
     * @param readResultItem The per page text extraction item result returned by the service.
     *
     * @return The list of {@code FormLine}.
     */
    static List<FormLine> getReadResultFormLines(ReadResult readResultItem) {
        return readResultItem.getLines().stream()
            .map(textLine -> {
                FormLine formLine = new FormLine(
                    textLine.getText(),
                    toBoundingBox(textLine.getBoundingBox()),
                    readResultItem.getPage(),
                    toWords(textLine.getWords(), readResultItem.getPage()));

                FormLineHelper.setAppearance(formLine, getAppearance(textLine));
                return formLine;
            })
            .collect(Collectors.toList());
    }

    /**
     * Private method to get the appearance from the service side text line object.
     * @param textLine The service side text line object.
     * @return the custom type Appearance model.
     */
    private static Appearance getAppearance(TextLine textLine) {
        Style style = new Style();
        if (textLine.getAppearance() != null && textLine.getAppearance().getStyle() != null) {
            if (textLine.getAppearance().getStyle().getName() != null) {
                StyleHelper.setName(style,
                    TextStyle.fromString(textLine.getAppearance().getStyle().getName().toString()));
            }
            StyleHelper.setConfidence(style, textLine.getAppearance().getStyle().getConfidence());
        }

        Appearance appearance = new Appearance();
        AppearanceHelper.setStyle(appearance, style);
        return appearance;
    }

    /**
     * The field map returned on analyze with an unlabeled model id.
     *
     * @param documentResultItem The extracted document level information.
     * @param readResults The text extraction result returned by the service.
     * @return The {@link RecognizedForm#getFields}.
     */
    private static Map<String, FormField> getLabeledFieldMap(DocumentResult documentResultItem,
                                                             List<ReadResult> readResults) {
        Map<String, FormField> recognizedFieldMap = new LinkedHashMap<>();
        // add receipt fields
        if (!CoreUtils.isNullOrEmpty(documentResultItem.getFields())) {
            documentResultItem.getFields().forEach((key, fieldValue) -> {
                if (fieldValue != null) {
                    List<FormElement> formElementList = setReferenceElements(fieldValue.getElements(), readResults);
                    FieldData valueData;
                    if ("ReceiptType".equals(key) || ARRAY == fieldValue.getType()) {
                        valueData = null;
                    } else {
                        valueData = new FieldData(fieldValue.getText(), toBoundingBox(fieldValue.getBoundingBox()),
                            fieldValue.getPage(), formElementList);
                    }
                    recognizedFieldMap.put(key, setFormField(key, valueData, fieldValue, readResults));
                } else {
                    recognizedFieldMap.put(key, new FormField(key, null, null, null,
                        DEFAULT_CONFIDENCE_VALUE));
                }
            });
        }
        return recognizedFieldMap;
    }

    /**
     * Helper method that converts the incoming service field value to one of the strongly typed SDK level
     * {@link FormField} with reference elements set when {@code includeFieldElements} is set to true.
     *
     * @param name The name of the field.
     * @param valueData The value text of the field.
     * @param fieldValue The named field values returned by the service.
     * @param readResults The text extraction result returned by the service.
     *
     * @return The strongly typed {@link FormField} for the field input.
     */
    private static FormField setFormField(String name, FieldData valueData, FieldValue fieldValue,
                                          List<ReadResult> readResults) {
        com.azure.ai.formrecognizer.models.FieldValue value;
        switch (fieldValue.getType()) {
            case PHONE_NUMBER:
                value = new com.azure.ai.formrecognizer.models.FieldValue(fieldValue.getValuePhoneNumber(),
                    FieldValueType.PHONE_NUMBER);
                break;
            case STRING:
                value = new com.azure.ai.formrecognizer.models.FieldValue(fieldValue.getValueString(),
                    FieldValueType.STRING);
                break;
            case TIME:
                LocalTime fieldTime = fieldValue.getValueTime() == null ? null : LocalTime
                    .parse(fieldValue.getValueTime(), DateTimeFormatter.ofPattern("HH:mm:ss"));
                value = new com.azure.ai.formrecognizer.models.FieldValue(fieldTime, FieldValueType.TIME);
                break;
            case DATE:
                value = new com.azure.ai.formrecognizer.models.FieldValue(fieldValue.getValueDate(),
                    FieldValueType.DATE);
                break;
            case INTEGER:
                com.azure.ai.formrecognizer.models.FieldValue longFieldValue;
                if (fieldValue.getValueInteger() == null) {
                    longFieldValue =
                        new com.azure.ai.formrecognizer.models.FieldValue(null, FieldValueType.LONG);
                } else {
                    longFieldValue =
                        new com.azure.ai.formrecognizer.models.FieldValue(fieldValue.getValueInteger().longValue(),
                            FieldValueType.LONG);
                }
                value = longFieldValue;
                break;
            case NUMBER:
                value = new com.azure.ai.formrecognizer.models.FieldValue(fieldValue.getValueNumber(),
                    FieldValueType.FLOAT);
                break;
            case ARRAY:
                value = new com.azure.ai.formrecognizer.models.FieldValue(
                    toFieldValueArray(fieldValue.getValueArray(), readResults), FieldValueType.LIST);
                break;
            case OBJECT:
                value = new com.azure.ai.formrecognizer.models.FieldValue(
                    toFieldValueObject(fieldValue.getValueObject(), readResults), FieldValueType.MAP);
                break;
            case SELECTION_MARK:
                com.azure.ai.formrecognizer.models.SelectionMarkState selectionMarkState;
                final FieldValueSelectionMark fieldValueSelectionMarkState = fieldValue.getValueSelectionMark();
                if (FieldValueSelectionMark.SELECTED.equals(fieldValueSelectionMarkState)) {
                    selectionMarkState = com.azure.ai.formrecognizer.models.SelectionMarkState.SELECTED;
                } else if (FieldValueSelectionMark.UNSELECTED.equals(fieldValueSelectionMarkState)) {
                    selectionMarkState = com.azure.ai.formrecognizer.models.SelectionMarkState.UNSELECTED;
                } else {
                    // TODO: (ServiceBug)
                    // Currently, the fieldValue's valueSelectionMark is null which is incorrect.
                    // Use the fieldValue's text as the temperately solution.
                    selectionMarkState = com.azure.ai.formrecognizer.models.SelectionMarkState.fromString(
                        fieldValue.getText());
                    //        throw LOGGER.logThrowableAsError(new RuntimeException(
                    //                String.format("%s, unsupported selection mark state.", selectionMarkState)));
                }
                value = new com.azure.ai.formrecognizer.models.FieldValue(selectionMarkState,
                    FieldValueType.SELECTION_MARK_STATE);
                break;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException("FieldValue Type not supported"));
        }

        return new FormField(name, null, valueData, value,
            setDefaultConfidenceValue(fieldValue.getConfidence()));
    }

    /**
     * Helper method to set default confidence value if confidence returned by service is null.
     *
     * @param confidence the confidence returned by service.
     *
     * @return the field confidence value.
     */
    private static float setDefaultConfidenceValue(Float confidence) {
        return confidence == null ? DEFAULT_CONFIDENCE_VALUE : confidence;
    }

    /**
     * Helper method to convert the service returned
     * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueObject()}
     * to a SDK level map of {@link FormField}.
     *
     * @param valueObject The array of field values returned by the service in {@link FieldValue#getValueObject()}.
     *
     * @return The Map of {@link FormField}.
     */
    private static Map<String, FormField> toFieldValueObject(Map<String, FieldValue> valueObject,
                                                             List<ReadResult> readResults) {
        Map<String, FormField> fieldValueObjectMap = new TreeMap<>();
        valueObject.forEach((key, fieldValue) ->
            fieldValueObjectMap.put(key,
                setFormField(key,
                    new FieldData(fieldValue.getText(),
                        toBoundingBox(fieldValue.getBoundingBox()),
                        fieldValue.getPage(),
                        setReferenceElements(fieldValue.getElements(), readResults)),
                    fieldValue,
                    readResults)
            ));
        return fieldValueObjectMap;
    }

    /**
     * Helper method to convert the service returned
     * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueArray()}
     * to a SDK level List of {@link FormField}.
     *
     * @param valueArray The array of field values returned by the service in {@link FieldValue#getValueArray()}.
     * @param readResults The text extraction result returned by the service.
     * @return The List of {@link FormField}.
     */
    private static List<FormField> toFieldValueArray(List<FieldValue> valueArray, List<ReadResult> readResults) {
        return valueArray.stream()
            .map(fieldValue -> {
                FieldData valueData = null;
                // ARRAY has ho value data, such as bounding box.
                if (ARRAY != fieldValue.getType()
                    && (fieldValue.getPage() != null && fieldValue.getBoundingBox() != null
                    && fieldValue.getText() != null)) {
                    valueData = new FieldData(fieldValue.getText(), toBoundingBox(fieldValue.getBoundingBox()),
                        fieldValue.getPage(),
                        setReferenceElements(fieldValue.getElements(), readResults));
                }
                return setFormField(null, valueData, fieldValue, readResults);
            })
            .collect(Collectors.toList());
    }

    /**
     * Helper method to convert the page results to {@code FormPage form pages}.
     *
     * @param readResultItem The per page text extraction item result returned by the service.
     * @param perPageTableList The per page tables list.
     * @param perPageLineList The per page form lines.
     * @param perPageSelectionMarkList The per page selection marks.
     *
     * @return The per page {@code FormPage}.
     */
    private static FormPage getFormPage(ReadResult readResultItem, List<FormTable> perPageTableList,
        List<FormLine> perPageLineList, List<FormSelectionMark> perPageSelectionMarkList) {
        FormPage formPage = new FormPage(
            readResultItem.getHeight(),
            readResultItem.getAngle(),
            LengthUnit.fromString(readResultItem.getUnit().toString()),
            readResultItem.getWidth(),
            perPageLineList,
            perPageTableList,
            readResultItem.getPage());
        FormPageHelper.setSelectionMarks(formPage, perPageSelectionMarkList);
        return formPage;
    }

    /**
     * Helper method to set the {@link RecognizedForm#getFields() fields} from unlabeled result returned from the
     * service.
     *
     * @param includeFieldElements Boolean to indicate if to set reference elements data on fields.
     * @param readResults The text extraction result returned by the service.
     * @param pageResultItem The extracted page level information returned by the service.
     * @param pageNumber The 1 based page number on which these fields exist.
     *
     * @return The fields populated on {@link RecognizedForm#getFields() fields}.
     */
    private static Map<String, FormField> getUnlabeledFieldMap(boolean includeFieldElements,
        List<ReadResult> readResults,
        PageResult pageResultItem, int pageNumber) {
        Map<String, FormField> formFieldMap = new LinkedHashMap<>();
        List<KeyValuePair> keyValuePairs = pageResultItem.getKeyValuePairs();
        forEachWithIndex(keyValuePairs, ((index, keyValuePair) -> {
            List<FormElement> formKeyContentList = new ArrayList<>();
            List<FormElement> formValueContentList = new ArrayList<>();
            if (includeFieldElements) {
                formKeyContentList = setReferenceElements(keyValuePair.getKey().getElements(), readResults);
                formValueContentList = setReferenceElements(keyValuePair.getValue().getElements(), readResults
                );
            }
            FieldData labelData = new FieldData(keyValuePair.getKey().getText(),
                toBoundingBox(keyValuePair.getKey().getBoundingBox()), pageNumber, formKeyContentList);
            FieldData valueData = new FieldData(keyValuePair.getValue().getText(),
                toBoundingBox(keyValuePair.getValue().getBoundingBox()), pageNumber, formValueContentList);

            String fieldName = "field-" + index;
            FormField formField = new FormField(fieldName, labelData, valueData,
                new com.azure.ai.formrecognizer.models.FieldValue(keyValuePair.getValue().getText(),
                    FieldValueType.STRING),
                setDefaultConfidenceValue(keyValuePair.getConfidence())
            );
            formFieldMap.put(fieldName, formField);
        }));
        return formFieldMap;
    }

    /**
     * Helper method to set the text reference elements on FieldValue/fields when {@code includeFieldElements} set to
     * true.
     *
     * @return The list if referenced elements.
     */
    private static List<FormElement> setReferenceElements(List<String> elements,
        List<ReadResult> readResults) {
        if (CoreUtils.isNullOrEmpty(elements)) {
            return new ArrayList<>();
        }
        List<FormElement> formElementList = new ArrayList<>();
        elements.forEach(elementString -> {
            String[] indices = NON_DIGIT_PATTERN.matcher(elementString).replaceAll(" ").trim().split(" ");

            if (indices.length < 2) {
                throw LOGGER.logExceptionAsError(new RuntimeException("Cannot find corresponding reference elements "
                    + "for the field value."));
            }

            int readResultIndex = Integer.parseInt(indices[0]);
            int lineIndex = Integer.parseInt(indices[1]);

            if (indices.length == 3) {
                int wordIndex = Integer.parseInt(indices[2]);
                TextWord textWord =
                    readResults.get(readResultIndex).getLines().get(lineIndex).getWords().get(wordIndex);
                FormWord wordElement = new FormWord(textWord.getText(), toBoundingBox(textWord.getBoundingBox()),
                    readResultIndex + 1, setDefaultConfidenceValue(textWord.getConfidence()));
                formElementList.add(wordElement);
            } else {
                TextLine textLine = readResults.get(readResultIndex).getLines().get(lineIndex);
                FormLine lineElement = new FormLine(textLine.getText(), toBoundingBox(textLine.getBoundingBox()),
                    readResultIndex + 1, toWords(textLine.getWords(), readResultIndex + 1));
                FormLineHelper.setAppearance(lineElement, getAppearance(textLine));
                formElementList.add(lineElement);
            }
        });
        return formElementList;
    }

    /**
     * Helper method to convert the service level {@link TextWord}  to list of SDK level model {@link FormWord}.
     *
     * @param words A list of word reference elements returned by the service.
     * @param pageNumber The 1 based page number on which this word element exists.
     *
     * @return The list of {@code FormWord words}.
     */
    private static List<FormWord> toWords(List<TextWord> words, int pageNumber) {
        return words.stream()
            .map(textWord -> new FormWord(
                textWord.getText(),
                toBoundingBox(textWord.getBoundingBox()),
                pageNumber,
                setDefaultConfidenceValue(textWord.getConfidence()))
            ).collect(Collectors.toList());
    }

    /**
     * Helper method to convert the service level modeled eight numbers representing the four points to SDK level
     * {@link FieldBoundingBox}.
     *
     * @param serviceBoundingBox A list of eight numbers representing the four points of a box.
     *
     * @return A {@link FieldBoundingBox}.
     */
    private static FieldBoundingBox toBoundingBox(List<Float> serviceBoundingBox) {
        if (CoreUtils.isNullOrEmpty(serviceBoundingBox) || (serviceBoundingBox.size() % 2) != 0) {
            return null;
        }
        List<Point> pointList = new ArrayList<>();
        for (int i = 0; i < serviceBoundingBox.size(); i++) {
            pointList.add(new Point(serviceBoundingBox.get(i), serviceBoundingBox.get(++i)));
        }
        return new FieldBoundingBox(pointList);
    }
}
