// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.models.AnalyzeResult;
import com.azure.ai.formrecognizer.implementation.models.DocumentResult;
<<<<<<< HEAD
import com.azure.ai.formrecognizer.implementation.models.ModelInfo;
=======
import com.azure.ai.formrecognizer.implementation.models.PageResult;
>>>>>>> 01efeece09... update to new design
import com.azure.ai.formrecognizer.implementation.models.ReadResult;
import com.azure.ai.formrecognizer.implementation.models.TextLine;
import com.azure.ai.formrecognizer.implementation.models.TextWord;
import com.azure.ai.formrecognizer.models.ArrayValue;
import com.azure.ai.formrecognizer.models.BoundingBox;
import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.models.DateValue;
import com.azure.ai.formrecognizer.models.DimensionUnit;
import com.azure.ai.formrecognizer.models.FieldText;
import com.azure.ai.formrecognizer.models.FieldValue;
import com.azure.ai.formrecognizer.models.FloatValue;
import com.azure.ai.formrecognizer.models.FormContent;
import com.azure.ai.formrecognizer.models.FormField;
import com.azure.ai.formrecognizer.models.FormLine;
import com.azure.ai.formrecognizer.models.FormPage;
import com.azure.ai.formrecognizer.models.FormTable;
import com.azure.ai.formrecognizer.models.FormTableCell;
import com.azure.ai.formrecognizer.models.FormWord;
import com.azure.ai.formrecognizer.models.IntegerValue;
<<<<<<< HEAD
import com.azure.ai.formrecognizer.models.LineElement;
import com.azure.ai.formrecognizer.models.ModelTrainingStatus;
import com.azure.ai.formrecognizer.models.PageMetadata;
=======
import com.azure.ai.formrecognizer.models.ObjectValue;
>>>>>>> 01efeece09... update to new design
import com.azure.ai.formrecognizer.models.PageRange;
import com.azure.ai.formrecognizer.models.Point;
import com.azure.ai.formrecognizer.models.RecognizedForm;
import com.azure.ai.formrecognizer.models.RecognizedReceipt;
import com.azure.ai.formrecognizer.models.StringValue;
import com.azure.ai.formrecognizer.models.TimeValue;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper class to convert service level models to SDK exposed models.
 */
final class Transforms {
    private static final ClientLogger LOGGER = new ClientLogger(Transforms.class);
    // Pattern match to find all non-digits in the provided string.
    private static final Pattern COMPILE = Pattern.compile("[^0-9]+");

    private Transforms() {
    }

    /**
     * Helper method to transform the service returned {@link AnalyzeResult} to SDK model {@link FormPage}.
     *
     * @param analyzeResult The service returned result for analyze layouts.
     *
     * @return The IterableStream of {@code FormPage}.
     */
    static IterableStream<FormPage> toRecognizedLayout(AnalyzeResult analyzeResult) {
        List<ReadResult> readResults = analyzeResult.getReadResults();
        List<PageResult> pageResults = analyzeResult.getPageResults();
        List<FormPage> formPages = new ArrayList<>();

        for (int i = 0; i < readResults.size(); i++) {
            ReadResult readResultItem = readResults.get(i);
            PageResult pageResultItem;
            List<FormTable> extractedTablesList = new ArrayList<>();

            if (!CoreUtils.isNullOrEmpty(pageResults)) {
                pageResultItem = pageResults.get(i);
                extractedTablesList = getPageTables(pageResultItem, pageResultItem.getPage());
            }

            // add form lines
            List<FormLine> formLines = new ArrayList<>();
            if (!CoreUtils.isNullOrEmpty(readResultItem.getLines())) {
                formLines = getReadResultFormLines(readResultItem);
            }

            // get form tables
            formPages.add(getFormPage(readResultItem, extractedTablesList, formLines));
        }
        return new IterableStream<>(formPages);
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
        List<ReadResult> readResults = analyzeResult.getReadResults();
        List<DocumentResult> documentResult = analyzeResult.getDocumentResults();
        List<PageResult> pageResults = analyzeResult.getPageResults();
        List<RecognizedReceipt> extractedReceiptList = new ArrayList<>();
        List<FormPage> formPages = new ArrayList<>();
        String formType = null;
        PageRange pageRange = null;
        Map<String, FormField> extractedFieldMap = new HashMap<>();
        if (!CoreUtils.isNullOrEmpty(documentResult)) {
            for (int i = 0; i < documentResult.size(); i++) {

                DocumentResult documentResultItem = documentResult.get(i);
                List<Integer> receiptPageRange = documentResultItem.getPageRange();
                if (receiptPageRange.size() == 2) {
                    pageRange = new PageRange(receiptPageRange.get(0), receiptPageRange.get(1));
                }

                formType = documentResultItem.getDocType();
                // add receipt fields
                documentResultItem.getFields().forEach((key, fieldValue) -> {
                    IterableStream<FormContent> formContentList = null;
                    Integer pageNumber = fieldValue.getPage();
                    if (includeTextDetails && fieldValue.getElements() != null) {
                        formContentList = setReferenceElements(fieldValue.getElements(), readResults, pageNumber);
                    }
                    // TODO (savaity): what bounding box for valueText?
                    FieldText valueText = new FieldText(fieldValue.getText(), null, pageNumber, formContentList);
                    FormField formField = new FormField(fieldValue.getConfidence(), null, key, setFieldValue(fieldValue), valueText, pageNumber);
                    extractedFieldMap.put(key, formField);
                });
            }

            for (int i = 0; i < readResults.size(); i++) {
                ReadResult readResultItem = readResults.get(i);
                PageResult pageResultItem;
                List<FormTable> extractedTablesList = null;

                if (pageResults != null) {
                    pageResultItem = pageResults.get(i);
                    extractedTablesList = getPageTables(pageResultItem, pageResultItem.getPage());
                }

                // add form lines
                List<FormLine> formLines = null;
                if (!CoreUtils.isNullOrEmpty(readResultItem.getLines()) && includeTextDetails) {
                    formLines = getReadResultFormLines(readResultItem);
                }

                // get form tables
                formPages.add(getFormPage(readResultItem, extractedTablesList, formLines));
            }

            RecognizedForm recognizedForm = new RecognizedForm(extractedFieldMap, formType, pageRange, formPages);
            RecognizedReceipt extractedReceiptItem = new RecognizedReceipt("en-US", recognizedForm);

            extractedReceiptList.add(extractedReceiptItem);
        }
        return new IterableStream<>(extractedReceiptList);
    }

    /**
     * Helper method to convert the page results to {@code FormPage form pages}.
     *
     * @param readResultItem The per page text extraction item result returned by the service.
     * @param extractedTablesList The per page tables list.
     * @param formLines The per page form lines.
     *
     * @return The per page {@code FormPage}.
     */
    private static FormPage getFormPage(ReadResult readResultItem, List<FormTable> extractedTablesList, List<FormLine> formLines) {
        return new FormPage(readResultItem.getHeight(), formLines,
            extractedTablesList, readResultItem.getAngle(),
            DimensionUnit.fromString(readResultItem.getUnit().toString()), readResultItem.getWidth());
    }

    /**
     * Helper method to get per-page table information.
     *
     * @param pageResultItem The extracted page level information returned by the service.
     * @param pageNumber The 1 based page number on which these fields exist.
     *
     * @return The list of per page {@code FormTable}.
     */
    private static List<FormTable> getPageTables(PageResult pageResultItem, Integer pageNumber) {
        List<FormTable> extractedTablesList = new ArrayList<>();
        pageResultItem.getTables().forEach(dataTable -> {
            List<FormTableCell> tableCellList = new ArrayList<>();
            dataTable.getCells().forEach(dataTableCell -> {
                FormTableCell tableCell = new FormTableCell(dataTableCell.getRowIndex(), dataTableCell.getColumnIndex(),
                    dataTableCell.getRowSpan(), dataTableCell.getColumnSpan(),
                    dataTableCell.getText(), toBoundingBox(dataTableCell.getBoundingBox()),
                    dataTableCell.getConfidence(), null,
                    dataTableCell.isHeader() == null ? false : dataTableCell.isHeader(),
                    dataTableCell.isFooter() == null ? false : dataTableCell.isHeader(),
                    pageNumber);
                tableCellList.add(tableCell);
            });
            FormTable extractedTable = new FormTable(dataTable.getRows(), dataTable.getColumns(), tableCellList);
            extractedTablesList.add(extractedTable);
        });
        return extractedTablesList;
    }

    /**
     * Helper method to convert the per page {@link ReadResult} item to {@link FormLine}.
     *
     * @param readResultItem The per page text extraction item result returned by the service.
     *
     * @return The list of {@code FormLine}.
     */
    private static List<FormLine> getReadResultFormLines(ReadResult readResultItem) {
        List<FormLine> formLines = new ArrayList<>();
        readResultItem.getLines().forEach(textLine -> {
            FormLine formLine = new FormLine(textLine.getText(), toBoundingBox(textLine.getBoundingBox()),
                readResultItem.getPage(), new IterableStream<>(toWords(textLine.getWords(), readResultItem.getPage())));
            formLines.add(formLine);
        });
        return formLines;
    }

    /**
     * Helper method that converts the incoming service field value to one of the strongly typed SDK level
     * {@link FieldValue} with reference elements set when {@code includeTextDetails} is set to true.
     *
     * @param fieldValue The named field values returned by the service.
     *
     * @return The strongly typed {@link FieldValue} for the field input.
     */
    private static FieldValue<?> setFieldValue(com.azure.ai.formrecognizer.implementation.models.FieldValue fieldValue) {
        FieldValue<?> value;
        switch (fieldValue.getType()) {
            case PHONE_NUMBER:
                value = toFieldValuePhoneNumber(fieldValue);
                break;
            case STRING:
                value = toFieldValueString(fieldValue);
                break;
            case TIME:
                value = toFieldValueTime(fieldValue);
                break;
            case DATE:
                value = toFieldValueDate(fieldValue);
                break;
            case INTEGER:
                value = toFieldValueInteger(fieldValue);
                break;
            case NUMBER:
                value = toFieldValueNumber(fieldValue);
                break;
            case ARRAY:
                value = toFieldValueArray(fieldValue);
                break;
            case OBJECT:
                value = toFieldValueObject(fieldValue);
                break;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException("FieldValue Type not supported"));
        }
        return value;
    }

    /**
     * Helper method to set the {@link RecognizedForm#getFields() fields} from unlabeled result returned from the service.
     *
     * @param includeTextDetails Boolean to indicate if to set reference elements data on fields.
     * @param readResults The text extraction result returned by the service.
     * @param pageResultItem The extracted page level information returned by the service.
     * @param pageNumber The 1 based page number on which these fields exist.
     *
     * @return The fields populated on {@link RecognizedForm#getFields() fields}.
     */
    private static Map<String, FormField> getUnlabeledFieldMap(boolean includeTextDetails, List<ReadResult> readResults,
        PageResult pageResultItem, Integer pageNumber) {
        Map<String, FormField> formFieldMap = new HashMap<>();
        pageResultItem.getKeyValuePairs().forEach(keyValuePair -> {
            IterableStream<FormContent> formContentList = null;
            if (includeTextDetails && !CoreUtils.isNullOrEmpty(keyValuePair.getValue().getElements())) {
                formContentList = setReferenceElements(keyValuePair.getValue().getElements(), readResults, pageNumber);
            }
            FieldText labelFieldText = new FieldText(keyValuePair.getLabel(), null, pageNumber, null);
            // TODO (savaity): what about keyText?
            // FieldText keyText = new FieldText(keyValuePair.getKey().getText(), toBoundingBox(keyValuePair.getKey().getBoundingBox()), pageNumber, setReferenceElementsF(keyValuePair.getKey().getElements()));
            FieldText valueText = new FieldText(keyValuePair.getValue().getText(), toBoundingBox(keyValuePair.getValue().getBoundingBox()), pageNumber, formContentList);
            FieldValue<?> fieldValue = new StringValue(keyValuePair.getValue().getText());
            FormField formField = new FormField(keyValuePair.getConfidence(), labelFieldText, keyValuePair.getLabel(), fieldValue, valueText, pageNumber);
            formFieldMap.put(keyValuePair.getLabel(), formField);
        });
        return formFieldMap;
    }

    /**
     * Helper method to transform the service returned {@link AnalyzeResult} to SDK model {@link RecognizedForm}.
     *
     * @param analyzeResult The service returned result for analyze custom forms.
     * @param includeTextDetails Boolean to indicate if to set reference elements data on fields.
     *
     * @return The IterableStream of {@code RecognizedForm}.
     */
    static IterableStream<RecognizedForm> toRecognizedForm(AnalyzeResult analyzeResult, boolean includeTextDetails) {
        List<ReadResult> readResults = analyzeResult.getReadResults();
        List<DocumentResult> documentResults = analyzeResult.getDocumentResults();
        List<PageResult> pageResults = analyzeResult.getPageResults();
        AtomicReference<PageRange> pageRange = new AtomicReference<>();
        List<RecognizedForm> extractedFormList = new ArrayList<>();
        List<FormPage> formPages = new ArrayList<>();
        Map<String, FormField> extractedFieldMap = new HashMap<>();

        AtomicReference<String> formType = new AtomicReference<>("form-");
        if (!CoreUtils.isNullOrEmpty(documentResults)) {
            Map<String, FormField> finalExtractedFieldMap = extractedFieldMap;
            documentResults.forEach(documentResultItem -> {
                formType.set(documentResultItem.getDocType());
                pageRange.set(new PageRange(documentResultItem.getPageRange().get(0), documentResultItem.getPageRange().get(1)));
                documentResultItem.getFields().forEach((key, fieldValue) -> {
                    IterableStream<FormContent> formContentList = null;
                    Integer pageNumber = fieldValue.getPage();
                    if (includeTextDetails && !CoreUtils.isNullOrEmpty(fieldValue.getElements())) {
                        formContentList = setReferenceElements(fieldValue.getElements(), readResults, pageNumber);
                    }
                    // TODO (savaity): what bounding box for valueText?
                    FieldText valueText = new FieldText(fieldValue.getText(), null, fieldValue.getPage(), formContentList);
                    FormField formField = new FormField(fieldValue.getConfidence(), null, key, setFieldValue(fieldValue), valueText, pageNumber);
                    // TODO (savaity): does this still keep the updated value this way?
                    finalExtractedFieldMap.put(key, formField);
                });
            });
        }

        for (int i = 0; i < readResults.size(); i++) {

            ReadResult readResultItem = readResults.get(i);
            PageResult pageResultItem = null;
            Integer pageNumber = readResultItem.getPage();
            List<FormTable> extractedTablesList = new ArrayList<>();

            // get Tables
            if (!CoreUtils.isNullOrEmpty(pageResults)) {
                pageResultItem = pageResults.get(i);
                pageNumber = pageResultItem.getPage();
                extractedTablesList = getPageTables(pageResultItem, pageNumber);
                if (documentResults == null) {
                    Integer clusterId = pageResultItem.getClusterId();
                    if (clusterId != null) {
                        formType.set(formType.get() + clusterId);
                    }
                    extractedFieldMap = getUnlabeledFieldMap(includeTextDetails, readResults, pageResultItem, pageNumber);
                }
            }

            // add form lines
            List<FormLine> formLines = new ArrayList<>();
            if (!CoreUtils.isNullOrEmpty(readResultItem.getLines()) && includeTextDetails) {
                formLines = getReadResultFormLines(readResultItem);
            }

            // get form tables
            formPages.add(getFormPage(readResultItem, extractedTablesList, formLines));

            RecognizedForm recognizedForm = new RecognizedForm(extractedFieldMap, formType.get(),
                pageRange.get() == null ? new PageRange(pageNumber, pageNumber) : pageRange.get(), formPages);

            extractedFormList.add(recognizedForm);
        }
        return new IterableStream<>(extractedFormList);
    }

    /**
     * Helper method to set the text reference elements on FieldValue/fields when {@code includeTextDetails} set to true.
     *
     * @return The updated {@link FieldValue} object with list if referenced elements.
     */
    private static IterableStream<FormContent> setReferenceElements(List<String> elements, List<ReadResult> readResults, Integer pageNumber) {
        List<FormContent> formContentList = new ArrayList<>();
        elements.forEach(elementString -> {
            String[] indices = COMPILE.matcher(elementString).replaceAll(" ").trim().split(" ");
            int readResultIndex, lineIndex;
            if (indices.length >= 1) {
                readResultIndex = Integer.parseInt(indices[0]);
                lineIndex = Integer.parseInt(indices[1]);
            } else {
                throw LOGGER.logExceptionAsError(new RuntimeException("Reference Elements not found"));
            }
            if (indices.length == 3) {
                int wordIndex = Integer.parseInt(indices[2]);
                TextWord textWord = readResults.get(readResultIndex).getLines().get(lineIndex).getWords()
                    .get(wordIndex);
                FormWord wordElement = new FormWord(textWord.getText(), toBoundingBox(textWord.getBoundingBox()), pageNumber, textWord.getConfidence());
                formContentList.add(wordElement);
            } else {
                TextLine textLine = readResults.get(readResultIndex).getLines().get(lineIndex);
                FormLine lineElement = new FormLine(textLine.getText(), toBoundingBox(textLine.getBoundingBox()), pageNumber, toWords(textLine.getWords(), pageNumber));
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
    static IterableStream<FormWord> toWords(List<TextWord> words, Integer pageNumber) {
        List<FormWord> extractedWordList = words.stream()
            .map(textWord -> new FormWord(textWord.getText(), toBoundingBox(textWord.getBoundingBox()), pageNumber,
                textWord.getConfidence())).collect(Collectors.toList());
        return new IterableStream<FormWord>(extractedWordList);
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
        Point topLeft = new Point(serviceBoundingBox.get(0), serviceBoundingBox.get(1));
        Point topRight = new Point(serviceBoundingBox.get(2), serviceBoundingBox.get(3));
        Point bottomLeft = new Point(serviceBoundingBox.get(4), serviceBoundingBox.get(5));
        Point bottomRight = new Point(serviceBoundingBox.get(6), serviceBoundingBox.get(7));
        return new BoundingBox(Arrays.asList(topLeft, topRight, bottomLeft, bottomRight));
    }

    /**
     * Helper method to convert the service returned {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueArray()} ()}
     * to a SDK level {@link ArrayValue}
     *
     * @param fieldValueItems The array of field values returned by the service in
     * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueArray()}.
     *
     * @return The {@link ArrayValue}.
     */
    private static FieldValue<List<FieldValue<?>>> toFieldValueArray(
        com.azure.ai.formrecognizer.implementation.models.FieldValue fieldValueItems) {
        List<FieldValue<?>> receiptItemList = fieldValueItems.getValueArray().stream()
            .map(eachFieldValue -> setFieldValue(eachFieldValue))
            .collect(Collectors.toList());
        return new ArrayValue(receiptItemList);
    }

    /**
     * Helper method to convert the service returned {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueInteger()}
     * to a SDK level {@link IntegerValue}
     *
     * @param serviceIntegerValue The integer value returned by the service in
     * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueInteger()}.
     *
     * @return The {@link IntegerValue}.
     */
    private static FieldValue<Integer> toFieldValueInteger(com.azure.ai.formrecognizer.implementation.models.FieldValue
        serviceIntegerValue) {
        return new IntegerValue(serviceIntegerValue.getValueInteger());
    }

    /**
     * Helper method to convert the service returned {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueString()}
     * to a SDK level {@link StringValue}.
     *
     * @param serviceStringValue The string value returned by the service in
     * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueString()}.
     *
     * @return The {@link StringValue}.
     */
    private static FieldValue<String> toFieldValueString(com.azure.ai.formrecognizer.implementation.models.FieldValue
        serviceStringValue) {
        return new StringValue(serviceStringValue.getValueString());
    }

    /**
     * Helper method to convert the service returned {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueNumber()}
     * to a SDK level {@link FloatValue}.
     *
     * @param serviceFloatValue The float value returned by the service in
     * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueNumber()} .
     *
     * @return The {@link FloatValue}.
     */
    private static FieldValue<Float> toFieldValueNumber(com.azure.ai.formrecognizer.implementation.models.FieldValue
        serviceFloatValue) {
        return new FloatValue(serviceFloatValue.getValueNumber());
    }

    /**
     * Helper method to convert the service returned {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValuePhoneNumber()}
     * to a SDK level {@link StringValue}.
     *
     * @param serviceDateValue The string value returned by the service in
     * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValuePhoneNumber()}.
     *
     * @return The {@link StringValue}.
     */
    private static FieldValue<String> toFieldValuePhoneNumber(com.azure.ai.formrecognizer.implementation.models.FieldValue
        serviceDateValue) {
        return new StringValue(serviceDateValue.getValuePhoneNumber());
    }

    /**
     * Helper method to convert the service returned {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueDate()}
     * to a SDK level {@link DateValue}.
     *
     * @param serviceDateValue The string value returned by the service in
     * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueDate()}.
     *
     * @return The {@link StringValue}.
     */
    private static FieldValue<LocalDate> toFieldValueDate(com.azure.ai.formrecognizer.implementation.models.FieldValue
        serviceDateValue) {
        return new DateValue(serviceDateValue.getValueDate());
    }

    /**
     * Helper method to convert the service returned {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueTime()}
     * to a SDK level {@link TimeValue}.
     *
     * @param serviceDateValue The string value returned by the service in
     * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueTime()} .
     *
     * @return The {@link TimeValue}.
     */
    private static FieldValue<String> toFieldValueTime(com.azure.ai.formrecognizer.implementation.models.FieldValue
        serviceDateValue) {
        return new TimeValue(serviceDateValue.getValueTime());
        // TODO: currently returning a string, waiting on swagger update.
    }

    private static FieldValue<Map<String, FieldValue<?>>> toFieldValueObject(com.azure.ai.formrecognizer.implementation.models.FieldValue
        serviceFieldValue) {
        Map<String, FieldValue<?>> stringFieldValueMap = new HashMap<>();
        AtomicInteger pageNumber = new AtomicInteger();
        serviceFieldValue.getValueObject().forEach((key, fieldValue) -> {
            pageNumber.set(fieldValue.getPage());
            stringFieldValueMap.put(key, setFieldValue(fieldValue));
        });
        return new ObjectValue(stringFieldValueMap);
    }
}
