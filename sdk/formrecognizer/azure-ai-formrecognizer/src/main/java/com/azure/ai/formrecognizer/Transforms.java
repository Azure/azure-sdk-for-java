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
import com.azure.ai.formrecognizer.implementation.models.TextWord;
import com.azure.ai.formrecognizer.models.ArrayValue;
import com.azure.ai.formrecognizer.models.BoundingBox;
import com.azure.ai.formrecognizer.models.CustomFormModelInfo;
import com.azure.ai.formrecognizer.models.DateValue;
import com.azure.ai.formrecognizer.models.DimensionUnit;
import com.azure.ai.formrecognizer.models.FieldValue;
import com.azure.ai.formrecognizer.models.FloatValue;
import com.azure.ai.formrecognizer.models.FormContent;
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
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/**
 * Helper class to convert service level models to SDK exposed models.
 */
final class Transforms {
    private static final ClientLogger LOGGER = new ClientLogger(Transforms.class);
    // Pattern match to find all non-digits in the provided string.
    private static final Pattern COMPILE = Pattern.compile("[^0-9]+");

    private Transforms() {
    }

    static IterableStream<RecognizedForm> toRecognizedForm(AnalyzeResult analyzeResult, boolean includeTextDetails) {
        List<ReadResult> readResults = analyzeResult.getReadResults();
        List<DocumentResult> documentResults = analyzeResult.getDocumentResults();
        List<PageResult> pageResults = analyzeResult.getPageResults();
        List<RecognizedForm> extractedFormList = new ArrayList<>();
        List<FormPage> formPages = new ArrayList<>();
        List<Map<String, FieldValue<?>>> extractedFieldMapList = new ArrayList<>();
        List<PageRange> pageRangeList = new ArrayList<>();
        String formType = null;

        if (documentResults != null) {
            for (DocumentResult documentResultItem : documentResults) {
                formType = documentResultItem.getDocType();
                Map<String, FieldValue<?>> extractedFieldMap = new HashMap<>();
                documentResultItem.getFields().forEach((key, fieldValue) -> {
                    if (fieldValue != null) {
                        extractedFieldMap.put(key, setFieldValue(fieldValue, readResults, includeTextDetails));
                    }
                });
                PageRange pageRange = getPageRange(documentResultItem);
                // getDocumentResults(includeTextDetails, readResults, documentResultItem, extractedFieldMap);
                extractedFieldMapList.add(extractedFieldMap);
                pageRangeList.add(pageRange);
            }
        }

        for (int i = 0; i < readResults.size(); i++) {
            ReadResult readResultItem = readResults.get(i);
            PageResult pageResultItem;
            int pageNumber = readResultItem.getPage();
            List<FormTable> extractedTablesList = new ArrayList<>();

            if (pageResults != null) {
                pageResultItem = pageResults.get(i);
                extractedTablesList = getPageTables(pageResultItem, pageNumber);

                if (documentResults == null) {
                    System.out.println("Need to update");
                    // pageResultItem.getKeyValuePairs().forEach(keyValuePair -> {
                    //     pageExtractedFields.put(keyValuePair.get, setFieldValue(keyValuePair.getValue(), readResults, includeTextDetails));
                    // });
                    // TODO: Add extracted field form page result.
                }
            }

            // add form lines
            List<FormLine> formLines = new ArrayList<>();
            if (readResultItem.getLines() != null) {
                formLines = getReadResultFormLines(readResultItem);
            }

            // get form tables
            FormPage formPage = new FormPage(readResultItem.getHeight(), formLines,
                extractedTablesList, readResultItem.getAngle(),
                DimensionUnit.fromString(readResultItem.getUnit().toString()), readResultItem.getWidth());
            formPages.add(formPage);

            // TODO: update document result to be paged?
            RecognizedForm recognizedForm = new RecognizedForm(extractedFieldMapList.size() == 0 ? null : extractedFieldMapList.get(0), formType, pageRangeList.size() == 0 ? null : pageRangeList.get(0), formPages);

            extractedFormList.add(recognizedForm);
        }
        return new IterableStream<>(extractedFormList);
    }

    static IterableStream<FormPage> toRecognizedLayout(AnalyzeResult analyzeResult) {
        List<ReadResult> readResults = analyzeResult.getReadResults();
        List<PageResult> pageResults = analyzeResult.getPageResults();
        List<FormPage> formPages = new ArrayList<>();

        for (int i = 0; i < readResults.size(); i++) {
            ReadResult readResultItem = readResults.get(i);
            PageResult pageResultItem;
            int pageNumber = readResultItem.getPage();
            List<FormTable> extractedTablesList = new ArrayList<>();

            if (pageResults != null) {
                pageResultItem = pageResults.get(i);
                extractedTablesList = getPageTables(pageResultItem, pageNumber);
            }

            // add form lines
            List<FormLine> formLines = new ArrayList<>();
            if (readResultItem.getLines() != null) {
                formLines = getReadResultFormLines(readResultItem);
            }

            // get form tables
            FormPage formPage = new FormPage(readResultItem.getHeight(), formLines,
                extractedTablesList, readResultItem.getAngle(),
                DimensionUnit.fromString(readResultItem.getUnit().toString()), readResultItem.getWidth());
            formPages.add(formPage);
        }
        return new IterableStream<>(formPages);
    }

    static IterableStream<RecognizedReceipt> toReceipt(AnalyzeResult analyzeResult, boolean includeTextDetails) {
        List<ReadResult> readResults = analyzeResult.getReadResults();
        List<DocumentResult> documentResult = analyzeResult.getDocumentResults();
        List<PageResult> pageResults = analyzeResult.getPageResults();
        List<RecognizedReceipt> extractedReceiptList = new ArrayList<>();
        List<FormPage> formPages = new ArrayList<>();

        for (int i = 0; i < readResults.size(); i++) {
            ReadResult readResultItem = readResults.get(i);
            PageResult pageResultItem;
            int pageNumber = readResultItem.getPage();
            List<FormTable> extractedTablesList = null;

            if (pageResults != null) {
                pageResultItem = pageResults.get(i);
                extractedTablesList = getPageTables(pageResultItem, pageNumber);
            }

            // add form lines
            List<FormLine> formLines = null;
            if (includeTextDetails) {
                formLines = getReadResultFormLines(readResultItem);
            }

            // get form tables
            FormPage formPage = new FormPage(readResultItem.getHeight(), formLines,
                extractedTablesList, readResultItem.getAngle(),
                DimensionUnit.fromString(readResultItem.getUnit().toString()), readResultItem.getWidth());
            formPages.add(formPage);
            String formType = null;
            PageRange pageRange = null;
            Map<String, FieldValue<?>> extractedFieldMap = new HashMap<>();
            DocumentResult documentResultItem;
            if (documentResult != null) {
                documentResultItem = documentResult.get(i);

                List<Integer> receiptPageRange = documentResultItem.getPageRange();
                if (receiptPageRange.size() == 2) {
                    pageRange = new PageRange(receiptPageRange.get(0), receiptPageRange.get(1));
                }

                formType = documentResultItem.getDocType();
                // add receipt fields
                documentResultItem.getFields().forEach((key, fieldValue) ->
                    extractedFieldMap.put(key, setFieldValue(fieldValue, readResults, includeTextDetails)));
            }

            RecognizedForm recognizedForm = new RecognizedForm(extractedFieldMap, formType, pageRange, formPages);
            RecognizedReceipt extractedReceiptItem = new RecognizedReceipt("en-US", recognizedForm);

            extractedReceiptList.add(extractedReceiptItem);
        }
        return new IterableStream<>(extractedReceiptList);
    }

    private static List<FormTable> getPageTables(PageResult pageResultItem, int pageNumber) {
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

    private static List<FormLine> getReadResultFormLines(ReadResult readResultItem) {
        List<FormLine> formLines = new ArrayList<>();
        // get Form lines
        readResultItem.getLines().forEach(textLine -> {
            FormLine formLine = new FormLine(textLine.getText(), toBoundingBox(textLine.getBoundingBox()), readResultItem.getPage(), new IterableStream<>(toWords(textLine.getWords(), readResultItem.getPage())));
            formLines.add(formLine);
        });
        return formLines;
    }

    private static Iterable<FormWord> toWords(List<TextWord> words, int pageNumber) {
        List<FormWord> wordList = new ArrayList<>();
        words.forEach(textWord -> {
            FormWord word = new FormWord(textWord.getText(), toBoundingBox(textWord.getBoundingBox()), pageNumber,
                textWord.getConfidence());
            wordList.add(word);
        });
        return wordList;
    }

    /**
     * Helper method that converts the incoming service field value to one of the strongly typed SDK level {@link FieldValue} with
     * reference elements set when {@code includeTextDetails} is set to true.
     *
     * @param fieldValue The named field values returned by the service.
     * @param readResults The result containing the list of element references when includeTextDetails is set to true.
     * @param includeTextDetails When set to true, a list of references to the text elements is returned in the read result.
     *
     * @return The strongly typed {@link FieldValue} for the field input.
     */
    private static FieldValue<?> setFieldValue(com.azure.ai.formrecognizer.implementation.models.FieldValue fieldValue,
                                               List<ReadResult> readResults, boolean includeTextDetails) {
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
                value = toFieldValueArray(fieldValue, readResults, includeTextDetails);
                break;
            case OBJECT:
                value = toFieldValueObject(fieldValue, readResults, includeTextDetails);
                break;
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException("FieldValue Type not supported"));
        }
        // if (includeTextDetails && value != null) {
        //     value.setElements(setReferenceElements(readResults, fieldValue.getElements()));
        //     System.out.println("TODO:");
        // }
        return value;
    }

    /**
     * Helper method to convert the service level modeled eight numbers representing the four points to SDK level
     * {@link BoundingBox}.
     *
     * @param boundingBox A list of eight numbers representing the four points of a box.
     *
     * @return A {@link BoundingBox}.
     */
    private static BoundingBox toBoundingBox(List<Float> boundingBox) {
        BoundingBox boundingBox1;
        if (boundingBox.size() == 8) {
            Point topLeft = new Point(boundingBox.get(0), boundingBox.get(1));
            Point topRight = new Point(boundingBox.get(2), boundingBox.get(3));
            Point bottomLeft = new Point(boundingBox.get(4), boundingBox.get(5));
            Point bottomRight = new Point(boundingBox.get(6), boundingBox.get(7));
            boundingBox1 = new BoundingBox(topLeft, topRight, bottomLeft, bottomRight);
        } else {
            return null;
        }
        return boundingBox1;
    }

    /**
     * Helper method to set the text reference elements on FieldValue/fields when {@code includeTextDetails} set to true.
     *
     * @param readResults The ReadResult containing the resolved references for text elements.
     * elements constituting this field value.
     *
     * @return The updated {@link FieldValue} object with list if referenced elements.
     */
    // private static List<FormContent> setReferenceElements(List<ReadResult> readResults, List<String> elements) {
    //     List<FormContent> elementList = new ArrayList<>();
    //     elements.forEach(elementString -> {
    //         String[] indices = COMPILE.matcher(elementString).replaceAll(" ").trim().split(" ");
    //         int readResultIndex, lineIndex;
    //         if (indices.length >= 1) {
    //             readResultIndex = Integer.parseInt(indices[0]);
    //             lineIndex = Integer.parseInt(indices[1]);
    //         } else {
    //             throw LOGGER.logExceptionAsError(new RuntimeException("Reference Elements not found"));
    //         }
    //         if (indices.length == 3) {
    //             int wordIndex = Integer.parseInt(indices[2]);
    //             TextWord textWord = readResults.get(readResultIndex).getLines().get(lineIndex).getWords()
    //                 .get(wordIndex);
    //             FormWord wordElement = new FormWord(textWord.getText(), toBoundingBox(textWord.getBoundingBox()), readResultIndex +1, textWord.getConfidence());
    //             elementList.add(wordElement);
    //         } else {
    //             TextLine textLine = readResults.get(readResultIndex).getLines().get(lineIndex);
    //             FormLine lineElement = new FormLine(textLine.getText(), toBoundingBox(textLine.getBoundingBox()), readResultIndex +1, toWords());
    //             elementList.add(lineElement);
    //         }
    //     });
    //     return elementList;
    // }

    private static ArrayValue toFieldValueArray(
        com.azure.ai.formrecognizer.implementation.models.FieldValue fieldValueItems, List<ReadResult> readResults, boolean includeTextDetails) {
        List<FieldValue<?>> receiptItemList = new ArrayList<>();
        int pageNumber = 0;
        List<FormContent> elements = new ArrayList<>();
        for (com.azure.ai.formrecognizer.implementation.models.FieldValue eachFieldValue : fieldValueItems.getValueArray()) {
            FieldValue<?> receiptItem = setFieldValue(eachFieldValue, readResults, includeTextDetails);
            pageNumber = receiptItem.getPageNumber();
            receiptItemList.add(receiptItem);
        }
        return new ArrayValue(null, null, receiptItemList, pageNumber, elements);
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
    private static IntegerValue toFieldValueInteger(com.azure.ai.formrecognizer.implementation.models.FieldValue
                                                        serviceIntegerValue) {
        List<FormContent> elements = new ArrayList<>();

        if (serviceIntegerValue.getValueNumber() != null) {
            // TODO: Do not need this check, service team bug
            return new IntegerValue(serviceIntegerValue.getText(), toBoundingBox(serviceIntegerValue.getBoundingBox()),
                serviceIntegerValue.getValueInteger(), serviceIntegerValue.getPage(), elements);
        }

        return new IntegerValue(serviceIntegerValue.getText(), toBoundingBox(serviceIntegerValue.getBoundingBox()), null, serviceIntegerValue.getPage(), elements);
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
    private static StringValue toFieldValueString(com.azure.ai.formrecognizer.implementation.models.FieldValue
                                                      serviceStringValue) {
        List<FormContent> elements = new ArrayList<>();

        return new StringValue(serviceStringValue.getText(), toBoundingBox(serviceStringValue.getBoundingBox()),
            serviceStringValue.getValueString(), serviceStringValue.getPage(), elements);
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
    private static FloatValue toFieldValueNumber(com.azure.ai.formrecognizer.implementation.models.FieldValue
                                                     serviceFloatValue) {
        List<FormContent> elements = new ArrayList<>();

        if (serviceFloatValue.getValueNumber() != null) {
            // TODO: Do not need this check, service team bug
            return new FloatValue(serviceFloatValue.getText(), toBoundingBox(serviceFloatValue.getBoundingBox()),
                serviceFloatValue.getValueNumber(), serviceFloatValue.getPage(), elements);
        }

        return new FloatValue(serviceFloatValue.getText(), toBoundingBox(serviceFloatValue.getBoundingBox()), null, serviceFloatValue.getPage(), elements);
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
    private static StringValue toFieldValuePhoneNumber(com.azure.ai.formrecognizer.implementation.models.FieldValue
                                                           serviceDateValue) {
        List<FormContent> elements = new ArrayList<>();
        return new StringValue(serviceDateValue.getText(), toBoundingBox(serviceDateValue.getBoundingBox()),
            serviceDateValue.getValuePhoneNumber(), serviceDateValue.getPage(), elements);
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
    private static DateValue toFieldValueDate(com.azure.ai.formrecognizer.implementation.models.FieldValue
                                                  serviceDateValue) {
        List<FormContent> elements = new ArrayList<>();

        return new DateValue(serviceDateValue.getText(), toBoundingBox(serviceDateValue.getBoundingBox()),
            serviceDateValue.getValueDate(), serviceDateValue.getPage(), elements);
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
    private static TimeValue toFieldValueTime(com.azure.ai.formrecognizer.implementation.models.FieldValue
                                                  serviceDateValue) {
        List<FormContent> elements = new ArrayList<>();

        return new TimeValue(serviceDateValue.getText(), toBoundingBox(serviceDateValue.getBoundingBox()),
            serviceDateValue.getValueTime(), serviceDateValue.getPage(), elements);
        // TODO: currently returning a string, waiting on swagger update.
    }

    /**
     * Transform a list of {@link ModelInfo} to a list of {@link CustomFormModelInfo}.
     *
     * @param list A list of {@link ModelInfo}.
     * @return A list of {@link CustomFormModelInfo}.
     */
    static List<CustomFormModelInfo> toCustomFormModelInfo(List<ModelInfo> list) {
        CollectionTransformer<ModelInfo, CustomFormModelInfo> transformer =
            new CollectionTransformer<ModelInfo, CustomFormModelInfo>() {
            @Override
            CustomFormModelInfo transform(ModelInfo modelInfo) {
                return new CustomFormModelInfo(modelInfo.getModelId().toString(),
                    ModelTrainingStatus.fromString(modelInfo.getStatus().toString()),
                    modelInfo.getCreatedDateTime(), modelInfo.getLastUpdatedDateTime());
            }
        };
        return transformer.transform(list);
    }

    /**
     * A generic transformation class for collection that transform from type {@code E} to type {@code F}.
     *
     * @param <E> Transform type E to another type.
     * @param <F> Transform to type F from another type.
     */
    abstract static class CollectionTransformer<E, F> {
        abstract F transform(E e);

        List<F> transform(List<E> list) {
            List<F> newList = new ArrayList<>();
            for (E e : list) {
                newList.add(transform(e));
            }
            return newList;
        }
    }
    
    private static ObjectValue toFieldValueObject(com.azure.ai.formrecognizer.implementation.models.FieldValue
                                                      serviceFieldValue, List<ReadResult> readResults, boolean includeTextDetails) {
        Map<String, FieldValue<?>> stringFieldValueMap = new HashMap<>();
        List<FormContent> elements = new ArrayList<>();
        AtomicInteger pageNumber = new AtomicInteger();
        serviceFieldValue.getValueObject().forEach((key, fieldValue) -> {
            pageNumber.set(fieldValue.getPage());
            stringFieldValueMap.put(key, setFieldValue(fieldValue, readResults, includeTextDetails));
        });
        return new ObjectValue(null, null, stringFieldValueMap, pageNumber.get(), elements);
    }

    private static PageRange getPageRange(DocumentResult documentResultItem) {
        PageRange pageRange = null;
        List<Integer> documentResultItemPageRange = documentResultItem.getPageRange();
        if (documentResultItemPageRange.size() == 2) {
            pageRange = new PageRange(documentResultItemPageRange.get(0), documentResultItemPageRange.get(1));
        }
        return pageRange;
    }
}
