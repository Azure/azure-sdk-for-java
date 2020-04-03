// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.models.AnalyzeResult;
import com.azure.ai.formrecognizer.implementation.models.DocumentResult;
import com.azure.ai.formrecognizer.implementation.models.ReadResult;
import com.azure.ai.formrecognizer.implementation.models.TextLine;
import com.azure.ai.formrecognizer.implementation.models.TextWord;
import com.azure.ai.formrecognizer.models.BoundingBox;
import com.azure.ai.formrecognizer.models.DateValue;
import com.azure.ai.formrecognizer.models.DimensionUnit;
import com.azure.ai.formrecognizer.models.Element;
import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.FieldValue;
import com.azure.ai.formrecognizer.models.FloatValue;
import com.azure.ai.formrecognizer.models.IntegerValue;
import com.azure.ai.formrecognizer.models.LineElement;
import com.azure.ai.formrecognizer.models.PageMetadata;
import com.azure.ai.formrecognizer.models.PageRange;
import com.azure.ai.formrecognizer.models.Point;
import com.azure.ai.formrecognizer.models.ReceiptItem;
import com.azure.ai.formrecognizer.models.ReceiptItemType;
import com.azure.ai.formrecognizer.models.ReceiptType;
import com.azure.ai.formrecognizer.models.StringValue;
import com.azure.ai.formrecognizer.models.TimeValue;
import com.azure.ai.formrecognizer.models.WordElement;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.azure.ai.formrecognizer.models.ReceiptItemType.NAME;
import static com.azure.ai.formrecognizer.models.ReceiptItemType.PRICE;
import static com.azure.ai.formrecognizer.models.ReceiptItemType.QUANTITY;
import static com.azure.ai.formrecognizer.models.ReceiptItemType.TOTAL_PRICE;

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
     * Helper method to convert the {@link com.azure.ai.formrecognizer.implementation.models.AnalyzeOperationResult}
     * service level receipt model to list of {@link ExtractedReceipt}.
     *
     * @param analyzeResult The result of the analyze receipt operation returned by the service.
     * @param includeTextDetails When set to true, a list of references to the text elements is returned in the read result.
     *
     * @return A list of {@link ExtractedReceipt} to represent the list of extracted receipt information.
     */
    static IterableStream<ExtractedReceipt> toReceipt(AnalyzeResult analyzeResult, boolean includeTextDetails) {
        List<ReadResult> readResults = analyzeResult.getReadResults();
        List<DocumentResult> documentResult = analyzeResult.getDocumentResults();
        List<ExtractedReceipt> extractedReceiptList = new ArrayList<>();

        for (int i = 0; i < readResults.size(); i++) {
            ReadResult readResultItem = readResults.get(i);

            // add page Info
            PageMetadata pageMetadata = getPageInfo(readResultItem);
            PageRange pageRange = null;
            DocumentResult documentResultItem = documentResult.get(i);
            List<Integer> receiptPageRange = documentResultItem.getPageRange();
            if (receiptPageRange.size() == 2) {
                pageRange = new PageRange(receiptPageRange.get(0), receiptPageRange.get(1));
            }
            ExtractedReceipt extractedReceiptItem = new ExtractedReceipt(pageMetadata, pageRange);
            Map<String, FieldValue<?>> extractedFieldMap = new HashMap<>();
            // add receipt fields
            documentResultItem.getFields().forEach((key, fieldValue) -> {
                switch (key) {
                    case "ReceiptType":
                        extractedReceiptItem.setReceiptType(new ReceiptType(fieldValue.getValueString(),
                            fieldValue.getConfidence()));
                        break;
                    case "MerchantName":
                        extractedReceiptItem.setMerchantName(setFieldValue(fieldValue, readResults, includeTextDetails));
                        break;
                    case "MerchantAddress":
                        extractedReceiptItem.setMerchantAddress(setFieldValue(fieldValue, readResults, includeTextDetails));
                        break;
                    case "MerchantPhoneNumber":
                        extractedReceiptItem.setMerchantPhoneNumber(setFieldValue(fieldValue, readResults, includeTextDetails));
                        break;
                    case "Subtotal":
                        extractedReceiptItem.setSubtotal(setFieldValue(fieldValue, readResults, includeTextDetails));
                        break;
                    case "Tax":
                        extractedReceiptItem.setTax(setFieldValue(fieldValue, readResults, includeTextDetails));
                        break;
                    case "Tip":
                        extractedReceiptItem.setTip(setFieldValue(fieldValue, readResults, includeTextDetails));
                        break;
                    case "Total":
                        extractedReceiptItem.setTotal(setFieldValue(fieldValue, readResults, includeTextDetails));
                        break;
                    case "TransactionDate":
                        extractedReceiptItem.setTransactionDate(setFieldValue(fieldValue, readResults, includeTextDetails));
                        break;
                    case "TransactionTime":
                        extractedReceiptItem.setTransactionTime(setFieldValue(fieldValue, readResults, includeTextDetails));
                        break;
                    case "Items":
                        extractedReceiptItem.setReceiptItems(toReceiptItems(fieldValue.getValueArray(), readResults, includeTextDetails));
                        break;
                    default:
                        extractedFieldMap.putIfAbsent(key, setFieldValue(fieldValue, readResults, includeTextDetails));
                        break;
                }
            });
            // adding other detected extra fields
            extractedReceiptItem.setExtractedFields(extractedFieldMap);
            extractedReceiptList.add(extractedReceiptItem);
        }
        return new IterableStream<>(extractedReceiptList);
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
            case OBJECT:
            default:
                throw LOGGER.logExceptionAsError(new RuntimeException("FieldValue Type not supported"));
        }
        if (includeTextDetails) {
            value.setElements(setReferenceElements(readResults, fieldValue.getElements()));
        }
        return value;
    }

    /**
     * Helper method that converts the service returned page information to SDK model {@link PageMetadata}.
     *
     * @param readResultItem A read result item returned from the service containing the page information for provided
     * input.
     *
     * @return The {@link PageMetadata} for the receipt page.
     */
    private static PageMetadata getPageInfo(ReadResult readResultItem) {
        return new PageMetadata(readResultItem.getHeight(), readResultItem.getPage(), readResultItem.getWidth(),
            readResultItem.getAngle(), DimensionUnit.fromString(readResultItem.getUnit().toString()));
    }

    /**
     * Helper method to set the text reference elements on FieldValue/fields when {@code includeTextDetails} set to true.
     *
     * @param readResults The ReadResult containing the resolved references for text elements.
     * @param elements When includeTextDetails is set to true, a list of references to the text
     * elements constituting this field value.
     *
     * @return The updated {@link FieldValue} object with list if referenced elements.
     */
    private static List<Element> setReferenceElements(List<ReadResult> readResults, List<String> elements) {
        List<Element> elementList = new ArrayList<>();
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
                WordElement wordElement = new WordElement(textWord.getText(), toBoundingBox(textWord.getBoundingBox()));
                elementList.add(wordElement);
            } else {
                TextLine textLine = readResults.get(readResultIndex).getLines().get(lineIndex);
                LineElement lineElement = new LineElement(textLine.getText(), toBoundingBox(textLine.getBoundingBox()));
                elementList.add(lineElement);
            }
        });
        return elementList;
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
     * Helper method to convert the service level {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueArray() value items}
     * to SDK level {@link ReceiptItem receipt items}.
     *
     * @param fieldValueItems The named field values returned by the service.
     * @param readResults The result containing the list of element references when includeTextDetails is set to true.
     * @param includeTextDetails When set to true, a list of references to the text elements is returned in the read result.
     *
     * @return A list of {@link ReceiptItem}.
     */
    private static List<ReceiptItem> toReceiptItems(
        List<com.azure.ai.formrecognizer.implementation.models.FieldValue> fieldValueItems, List<ReadResult> readResults, boolean includeTextDetails) {
        List<ReceiptItem> receiptItemList = new ArrayList<>();
        for (com.azure.ai.formrecognizer.implementation.models.FieldValue eachFieldValue : fieldValueItems) {
            ReceiptItem receiptItem = new ReceiptItem();

            for (ReceiptItemType key : ReceiptItemType.values()) {
                com.azure.ai.formrecognizer.implementation.models.FieldValue item = eachFieldValue.getValueObject().get(key.toString());
                if (QUANTITY.equals(key) && item != null) {
                    receiptItem.setQuantity(setFieldValue(item, readResults, includeTextDetails));
                } else if (NAME.equals(key) && item != null) {
                    receiptItem.setName(setFieldValue(item, readResults, includeTextDetails));
                } else if (PRICE.equals(key) && item != null) {
                    receiptItem.setPrice(setFieldValue(item, readResults, includeTextDetails));
                } else if (TOTAL_PRICE.equals(key) && item != null) {
                    receiptItem.setTotalPrice(setFieldValue(item, readResults, includeTextDetails));
                }
            }
            receiptItemList.add(receiptItem);
        }
        return receiptItemList;
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
        if (serviceIntegerValue.getValueNumber() != null) {
            // TODO: Do not need this check, service team bug
            return new IntegerValue(serviceIntegerValue.getText(), toBoundingBox(serviceIntegerValue.getBoundingBox()),
                serviceIntegerValue.getValueInteger(), serviceIntegerValue.getPage());
        }

        return new IntegerValue(serviceIntegerValue.getText(), toBoundingBox(serviceIntegerValue.getBoundingBox()), null, serviceIntegerValue.getPage());
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
        return new StringValue(serviceStringValue.getText(), toBoundingBox(serviceStringValue.getBoundingBox()),
            serviceStringValue.getValueString(), serviceStringValue.getPage());
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
        if (serviceFloatValue.getValueNumber() != null) {
            // TODO: Do not need this check, service team bug
            return new FloatValue(serviceFloatValue.getText(), toBoundingBox(serviceFloatValue.getBoundingBox()),
                serviceFloatValue.getValueNumber(), serviceFloatValue.getPage());
        }

        return new FloatValue(serviceFloatValue.getText(), toBoundingBox(serviceFloatValue.getBoundingBox()), null, serviceFloatValue.getPage());
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
        return new StringValue(serviceDateValue.getText(), toBoundingBox(serviceDateValue.getBoundingBox()),
            serviceDateValue.getValuePhoneNumber(), serviceDateValue.getPage());
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
        return new DateValue(serviceDateValue.getText(), toBoundingBox(serviceDateValue.getBoundingBox()),
            serviceDateValue.getValueDate(), serviceDateValue.getPage());
    }

    /**
     * Helper method to convert the service returned {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueTime()}
     * to a SDK level {@link TimeValue}.
     *
     * @param serviceDateValue The string value returned by the service in
     * {@link com.azure.ai.formrecognizer.implementation.models.FieldValue#getValueTime()} .
     *
     * @return The {@link TimeValue}.
     *
     */
    private static TimeValue toFieldValueTime(com.azure.ai.formrecognizer.implementation.models.FieldValue
                                                  serviceDateValue) {
        return new TimeValue(serviceDateValue.getText(), toBoundingBox(serviceDateValue.getBoundingBox()),
            serviceDateValue.getValueTime(), serviceDateValue.getPage());
        // TODO: currently returning a string, waiting on swagger update.
    }
}
