// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.models.AnalyzeResult;
import com.azure.ai.formrecognizer.implementation.models.DocumentResult;
import com.azure.ai.formrecognizer.implementation.models.FieldValue;
import com.azure.ai.formrecognizer.implementation.models.KeyValuePair;
import com.azure.ai.formrecognizer.implementation.models.PageResult;
import com.azure.ai.formrecognizer.implementation.models.ReadResult;
import com.azure.ai.formrecognizer.implementation.models.TextLine;
import com.azure.ai.formrecognizer.implementation.models.TextWord;
import com.azure.ai.formrecognizer.models.BoundingBox;
import com.azure.ai.formrecognizer.models.DimensionUnit;
import com.azure.ai.formrecognizer.models.FieldText;
import com.azure.ai.formrecognizer.models.FormContent;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormLine;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormTable;
import com.azure.ai.formrecognizer.models.FormTableCell;
import com.azure.ai.formrecognizer.models.FormWord;
import com.azure.ai.formrecognizer.models.PageRange;
import com.azure.ai.formrecognizer.models.Point;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper class to convert service level models to SDK exposed models.
 */
final class Transforms {
    private static final ClientLogger LOGGER = new ClientLogger(Transforms.class);
    // Pattern match to find all non-digits in the provided string.
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("[^0-9]+");
    private static final float DEFAULT_CONFIDENCE_VALUE = 1.0f;

    private Transforms() {
    }

    /**
     * Helper method to transform the service returned {@link AnalyzeResult} to SDK model {@link RecognizedForm}.
     *
     * @param analyzeResult The service returned result for analyze custom forms.
     * @param includeTextDetails Boolean to indicate if to set reference elements data on fields.
     *
     * @return The IterableStream of {@code RecognizedForm}.
     */
    static List<RecognizedForm> toRecognizedForm(AnalyzeResult analyzeResult, boolean includeTextDetails) {
        List<ReadResult> readResults = analyzeResult.getReadResults();
        List<DocumentResult> documentResults = analyzeResult.getDocumentResults();
        List<PageResult> pageResults = analyzeResult.getPageResults();
        List<RecognizedForm> extractedFormList;

        List<FormPage> formPages = toRecognizedLayout(analyzeResult, includeTextDetails);

        if (!CoreUtils.isNullOrEmpty(documentResults)) {
            extractedFormList = new ArrayList<>();
            for (DocumentResult documentResultItem : documentResults) {
                PageRange pageRange;
                List<Integer> documentPageRange = documentResultItem.getPageRange();
                if (documentPageRange.size() == 2) {
                    pageRange = new PageRange(documentPageRange.get(0), documentPageRange.get(1));
                } else {
                    pageRange = new PageRange(1, 1);
                }

                Map<String, FormField<?>> extractedFieldMap = getUnlabeledFieldMap(documentResultItem, readResults,
                    includeTextDetails);
                extractedFormList.add(new RecognizedForm(
                    extractedFieldMap,
                    documentResultItem.getDocType(),
                    pageRange,
                    new IterableStream<>(formPages.subList(pageRange.getStartPageNumber() - 1,
                        pageRange.getEndPageNumber()))));
            }
        } else {
            extractedFormList = new ArrayList<>();
            for (PageResult pageResultItem : pageResults) {
                StringBuffer formType = new StringBuffer("form-");
                int pageNumber = pageResultItem.getPage();
                Integer clusterId = pageResultItem.getClusterId();
                if (clusterId != null) {
                    formType.append(clusterId);
                }
                Map<String, FormField<?>> extractedFieldMap = getLabeledFieldMap(includeTextDetails, readResults,
                    pageResultItem, pageNumber);

                extractedFormList.add(new RecognizedForm(
                    extractedFieldMap,
                    formType.toString(),
                    new PageRange(pageNumber, pageNumber),
                    new IterableStream<>(Collections.singletonList(formPages.get(pageNumber - 1)))));
            }
        }
        return extractedFormList;
    }

    /**
     * Helper method to transform the service returned {@link AnalyzeResult} to SDK model {@link RecognizedReceipt}.
     *
     * @param analyzeResult The service returned result for analyze receipts.
     * @param includeTextDetails Boolean to indicate if to set reference elements data on fields.
     *
     * @return The IterableStream of {@code RecognizedReceipt}.
     */
    static IterableStream<RecognizedReceipt> toReceipt(AnalyzeResult analyzeResult, boolean includeTextDetails) {
        return new IterableStream<>(
            toRecognizedForm(analyzeResult, includeTextDetails).stream()
                .map(recognizedForm ->
                    new RecognizedReceipt("en-US", recognizedForm))
                .collect(Collectors.toList()));
    }

    /**
     * Helper method to transform the service returned {@link AnalyzeResult} to SDK model {@link FormPage}.
     *
     * @param analyzeResult The service returned result for analyze layouts.
     * @param includeTextDetails Boolean to indicate if to set reference elements data on fields.
     *
     * @return The IterableStream of {@code FormPage}.
     */
    static List<FormPage> toRecognizedLayout(AnalyzeResult analyzeResult, boolean includeTextDetails) {
        List<ReadResult> readResults = analyzeResult.getReadResults();
        List<PageResult> pageResults = analyzeResult.getPageResults();
        List<FormPage> formPages = new ArrayList<>();
        boolean pageResultsIsNullOrEmpty = CoreUtils.isNullOrEmpty(pageResults);

        forEachWithIndex(readResults, ((index, readResultItem) -> {
            List<FormTable> perPageTableList = new ArrayList<>();

            if (!pageResultsIsNullOrEmpty) {
                PageResult pageResultItem = pageResults.get(index);
                perPageTableList = getPageTables(pageResultItem, readResults, pageResultItem.getPage());
            }

            // add form lines
            List<FormLine> perPageFormLineList = new ArrayList<>();
            if (includeTextDetails && !CoreUtils.isNullOrEmpty(readResultItem.getLines())) {
                perPageFormLineList = getReadResultFormLines(readResultItem);
            }

            // get form tables
            formPages.add(getFormPage(readResultItem, perPageTableList, perPageFormLineList));
        }));

        return formPages;
    }

    /**
     * Given an iterable will apply the indexing function to it and return the index and each item of the iterable.
     *
     * @param iterable the list to apply the mapping function to.
     * @param biConsumer the function which accepts the index and the each value of the iterable.
     * @param <T> the type of items being returned.
     */
    static <T> void forEachWithIndex(Iterable<T> iterable, BiConsumer<Integer, T> biConsumer) {
        int[] index = new int[]{0};
        iterable.forEach(element -> biConsumer.accept(index[0]++, element));
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
    static List<FormTable> getPageTables(PageResult pageResultItem, List<ReadResult> readResults, Integer pageNumber) {
        return pageResultItem.getTables().stream()
            .map(dataTable ->
                new FormTable(dataTable.getRows(), dataTable.getColumns(),
                    new IterableStream<>(dataTable.getCells().stream()
                        .map(dataTableCell -> new FormTableCell(
                            dataTableCell.getRowIndex(), dataTableCell.getColumnIndex(),
                            dataTableCell.getRowSpan(), dataTableCell.getColumnSpan(),
                            dataTableCell.getText(), toBoundingBox(dataTableCell.getBoundingBox()),
                            dataTableCell.getConfidence(),
                            dataTableCell.isHeader() == null ? false : dataTableCell.isHeader(),
                            dataTableCell.isFooter() == null ? false : dataTableCell.isFooter(),
                            pageNumber, setReferenceElements(dataTableCell.getElements(), readResults, pageNumber)))
                        .collect(Collectors.toList()))))
            .collect(Collectors.toList());
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
            .map(textLine -> new FormLine(
                textLine.getText(),
                toBoundingBox(textLine.getBoundingBox()),
                readResultItem.getPage(),
                toWords(textLine.getWords(), readResultItem.getPage())))
            .collect(Collectors.toList());
    }

    /**
     * The field map returned on analyze with an unlabeled model id.
     *
     * @param documentResultItem The extracted document level information.
     * @param readResults The text extraction result returned by the service.
     * @param includeTextDetails Boolean to indicate if to set reference elements data on fields.
     *
     * @return The {@code RecognizedForm#getFields}.
     */
    private static Map<String, FormField<?>> getUnlabeledFieldMap(DocumentResult documentResultItem,
        List<ReadResult> readResults, boolean includeTextDetails) {
        Map<String, FormField<?>> extractedFieldMap = new TreeMap<>();
        // add receipt fields
        documentResultItem.getFields().forEach((key, fieldValue) -> {
            if (fieldValue != null) {
                Integer pageNumber = fieldValue.getPage();
                FieldText labelText = new FieldText(key, null, pageNumber, null);
                IterableStream<FormContent> formContentList = null;
                if (includeTextDetails) {
                    formContentList = setReferenceElements(fieldValue.getElements(), readResults, pageNumber);
                }
                FieldText valueText = new FieldText(fieldValue.getText(), toBoundingBox(fieldValue.getBoundingBox()),
                    pageNumber, formContentList);
                extractedFieldMap.put(key, setFormField(labelText, key, fieldValue, valueText, pageNumber,
                    readResults));
            } else {
                FieldText labelText = new FieldText(key, null, null, null);
                extractedFieldMap.put(key, new FormField<>(DEFAULT_CONFIDENCE_VALUE, labelText,
                    key, null, null, null));
            }
        });
        return extractedFieldMap;
    }

    /**
     * Helper method that converts the incoming service field value to one of the strongly typed SDK level
     * {@link FormField} with reference elements set when {@code includeTextDetails} is set to true.
     *
     * @param labelText The label text of the field.
     * @param key The name of the field.
     * @param fieldValue The named field values returned by the service.
     * @param valueText The value text of the field.
     * @param pageNumber The 1-based page number.
     * @param readResults The text extraction result returned by the service.
     *
     * @return The strongly typed {@link FormField} for the field input.
     */
    private static FormField<?> setFormField(FieldText labelText, String key, FieldValue fieldValue,
        FieldText valueText, Integer pageNumber, List<ReadResult> readResults) {
        FormField<?> value;
        switch (fieldValue.getType()) {
            case PHONE_NUMBER:
                value = new FormField<>(setDefaultConfidenceValue(fieldValue.getConfidence()), labelText,
                    key, fieldValue.getValuePhoneNumber(), valueText, pageNumber);
                break;
            case STRING:
                value = new FormField<>(setDefaultConfidenceValue(fieldValue.getConfidence()), labelText,
                    key, fieldValue.getValueString(), valueText, pageNumber);
                break;
            case TIME:
                value = new FormField<>(setDefaultConfidenceValue(fieldValue.getConfidence()), labelText,
                    key, fieldValue.getValueTime(), valueText, pageNumber);
                break;
            case DATE:
                value = new FormField<>(setDefaultConfidenceValue(fieldValue.getConfidence()), labelText,
                    key, fieldValue.getValueDate(), valueText, pageNumber);
                break;
            case INTEGER:
                value = new FormField<>(setDefaultConfidenceValue(fieldValue.getConfidence()), labelText,
                    key, fieldValue.getValueInteger(), valueText, pageNumber);
                break;
            case NUMBER:
                value = new FormField<Number>(setDefaultConfidenceValue(fieldValue.getConfidence()), labelText,
                    key, fieldValue.getValueNumber(), valueText, pageNumber);
                break;
            case ARRAY:
                value = new FormField<>(setDefaultConfidenceValue(fieldValue.getConfidence()), null, key,
                    toFormFieldArray(fieldValue.getValueArray(), readResults), null, pageNumber);
                break;
            case OBJECT:
                value = new FormField<>(setDefaultConfidenceValue(fieldValue.getConfidence()), labelText,
                    key, toFormFieldObject(fieldValue.getValueObject(), pageNumber, readResults), valueText,
                    pageNumber);
                break;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException("FieldValue Type not supported"));
        }
        return value;
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
     * @param valueObject The array of field values returned by the service in
     * {@link FieldValue#getValueObject()} .
     *
     * @return The Map of {@link FormField}.
     */
    private static Map<String, FormField<?>> toFormFieldObject(Map<String, FieldValue> valueObject,
        Integer pageNumber, List<ReadResult> readResults) {
        Map<String, FormField<?>> fieldValueObjectMap = new TreeMap<>();
        valueObject.forEach((key, fieldValue) ->
            fieldValueObjectMap.put(key, setFormField(null, key, fieldValue,
                new FieldText(fieldValue.getText(),
                    toBoundingBox(fieldValue.getBoundingBox()),
                    fieldValue.getPage(),
                    setReferenceElements(fieldValue.getElements(), readResults, pageNumber)
                ), fieldValue.getPage(), readResults)));
        return fieldValueObjectMap;
    }

    /**
     * Helper method to convert the service returned
     * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueArray()}
     * to a SDK level List of {@link FormField}.
     *
     * @param valueArray The array of field values returned by the service in
     * {@link FieldValue#getValueArray()}.
     * @param readResults The text extraction result returned by the service.
     *
     * @return The List of {@link FormField}.
     */
    private static List<FormField<?>> toFormFieldArray(List<FieldValue> valueArray, List<ReadResult> readResults) {
        return valueArray.stream()
            .map(fieldValue -> setFormField(null, null, fieldValue, null, fieldValue.getPage(), readResults))
            .collect(Collectors.toList());
    }

    /**
     * Helper method to convert the page results to {@code FormPage form pages}.
     *
     * @param readResultItem The per page text extraction item result returned by the service.
     * @param perPageTableList The per page tables list.
     * @param perPageLineList The per page form lines.
     *
     * @return The per page {@code FormPage}.
     */
    private static FormPage getFormPage(ReadResult readResultItem, List<FormTable> perPageTableList,
        List<FormLine> perPageLineList) {
        return new FormPage(
            readResultItem.getHeight(),
            readResultItem.getAngle(),
            DimensionUnit.fromString(readResultItem.getUnit().toString()),
            readResultItem.getWidth(),
            new IterableStream<FormLine>(perPageLineList),
            new IterableStream<FormTable>(perPageTableList)
        );
    }

    /**
     * Helper method to set the {@link RecognizedForm#getFields() fields} from unlabeled result returned from the
     * service.
     *
     * @param includeTextDetails Boolean to indicate if to set reference elements data on fields.
     * @param readResults The text extraction result returned by the service.
     * @param pageResultItem The extracted page level information returned by the service.
     * @param pageNumber The 1 based page number on which these fields exist.
     *
     * @return The fields populated on {@link RecognizedForm#getFields() fields}.
     */
    private static Map<String, FormField<?>> getLabeledFieldMap(boolean includeTextDetails,
        List<ReadResult> readResults,
        PageResult pageResultItem, Integer pageNumber) {
        Map<String, FormField<?>> formFieldMap = new TreeMap<>();
        List<KeyValuePair> keyValuePairs = pageResultItem.getKeyValuePairs();
        forEachWithIndex(keyValuePairs, ((index, keyValuePair) -> {
            IterableStream<FormContent> formKeyContentList = null;
            IterableStream<FormContent> formValueContentList = null;
            if (includeTextDetails) {
                formKeyContentList = setReferenceElements(keyValuePair.getKey().getElements(), readResults, pageNumber);
                formValueContentList = setReferenceElements(keyValuePair.getValue().getElements(), readResults,
                    pageNumber);
            }
            FieldText labelFieldText = new FieldText(keyValuePair.getKey().getText(),
                toBoundingBox(keyValuePair.getKey().getBoundingBox()), pageNumber, formKeyContentList);
            FieldText valueText = new FieldText(keyValuePair.getValue().getText(),
                toBoundingBox(keyValuePair.getValue().getBoundingBox()), pageNumber, formValueContentList);

            String fieldName = "field-" + index;
            FormField<String> formField = new FormField<>(setDefaultConfidenceValue(keyValuePair.getConfidence()),
                labelFieldText, fieldName, keyValuePair.getValue().getText(), valueText, pageNumber);
            formFieldMap.put(fieldName, formField);
        }));
        return formFieldMap;
    }

    /**
     * Helper method to set the text reference elements on FieldValue/fields when {@code includeTextDetails} set to
     * true.
     *
     * @return The list if referenced elements.
     */
    private static IterableStream<FormContent> setReferenceElements(List<String> elements,
        List<ReadResult> readResults, Integer pageNumber) {
        if (CoreUtils.isNullOrEmpty(elements)) {
            return IterableStream.of(null);
        }
        List<FormContent> formContentList = new ArrayList<>();
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
                    pageNumber,
                    setDefaultConfidenceValue(textWord.getConfidence()));
                formContentList.add(wordElement);
            } else {
                TextLine textLine = readResults.get(readResultIndex).getLines().get(lineIndex);
                FormLine lineElement = new FormLine(textLine.getText(), toBoundingBox(textLine.getBoundingBox()),
                    pageNumber, toWords(textLine.getWords(), pageNumber));
                formContentList.add(lineElement);
            }
        });
        return new IterableStream<>(formContentList);
    }

    /**
     * Helper method to convert the service level {@link TextWord}  to list of SDK level model {@link FormWord}.
     *
     * @param words A list of word reference elements returned by the service.
     * @param pageNumber The 1 based page number on which this word element exists.
     *
     * @return The list of {@code FormWord words}.
     */
    private static IterableStream<FormWord> toWords(List<TextWord> words, Integer pageNumber) {
        return new IterableStream<>(words.stream()
            .map(textWord -> new FormWord(
                textWord.getText(),
                toBoundingBox(textWord.getBoundingBox()),
                pageNumber,
                setDefaultConfidenceValue(textWord.getConfidence()))
            ).collect(Collectors.toList()));
    }

    /**
     * Helper method to convert the service level modeled eight numbers representing the four points to SDK level
     * {@link BoundingBox}.
     *
     * @param serviceBoundingBox A list of eight numbers representing the four points of a box.
     *
     * @return A {@link BoundingBox}.
     */
    private static BoundingBox toBoundingBox(List<Float> serviceBoundingBox) {
        if (CoreUtils.isNullOrEmpty(serviceBoundingBox) || (serviceBoundingBox.size() % 2) != 0) {
            return null;
        }
        List<Point> pointList = new ArrayList<>();
        for (int i = 0; i < serviceBoundingBox.size(); i++) {
            pointList.add(new Point(serviceBoundingBox.get(i), serviceBoundingBox.get(++i)));
        }
        return new BoundingBox(pointList);
    }
}
