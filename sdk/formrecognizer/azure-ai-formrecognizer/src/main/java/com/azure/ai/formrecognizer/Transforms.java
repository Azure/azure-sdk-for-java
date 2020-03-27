// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.models.AnalyzeResult;
import com.azure.ai.formrecognizer.implementation.models.DocumentResult;
import com.azure.ai.formrecognizer.implementation.models.ReadResult;
import com.azure.ai.formrecognizer.implementation.models.TextLine;
import com.azure.ai.formrecognizer.implementation.models.TextWord;
import com.azure.ai.formrecognizer.models.BoundingBox;
import com.azure.ai.formrecognizer.models.DimensionUnit;
import com.azure.ai.formrecognizer.models.Element;
import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.FieldValue;
import com.azure.ai.formrecognizer.models.FloatValue;
import com.azure.ai.formrecognizer.models.IntegerValue;
import com.azure.ai.formrecognizer.models.LineElement;
import com.azure.ai.formrecognizer.models.PageMetadata;
import com.azure.ai.formrecognizer.models.Point;
import com.azure.ai.formrecognizer.models.ReceiptItem;
import com.azure.ai.formrecognizer.models.ReceiptType;
import com.azure.ai.formrecognizer.models.StringValue;
import com.azure.ai.formrecognizer.models.TextLanguage;
import com.azure.ai.formrecognizer.models.WordElement;
import com.azure.core.util.IterableStream;
import com.azure.core.util.logging.ClientLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Helper class to convert service level models to SDK exposed models.
 */
final class Transforms {
    private static final ClientLogger LOGGER = new ClientLogger(Transforms.class);
    private static final Pattern COMPILE = Pattern.compile("[^0-9]+");

    private Transforms() {
    }

    static IterableStream<ExtractedReceipt> toReceipt(AnalyzeResult analyzeResult) {
        List<ReadResult> readResults = analyzeResult.getReadResults();
        List<DocumentResult> documentResult = analyzeResult.getDocumentResults();
        List<ExtractedReceipt> extractedReceiptList = new ArrayList<>();

        for (int i = 0; i < readResults.size(); i++) {
            ReadResult readResultItem = readResults.get(i);
            PageMetadata pageMetadata = getPageInfo(readResultItem);
            ExtractedReceipt extractedReceiptItem = new ExtractedReceipt(pageMetadata);

            DocumentResult documentResultItem = documentResult.get(i);
            documentResultItem.getFields().forEach((key, fieldValue) -> {
                switch (key) {
                    case "ReceiptType":
                        extractedReceiptItem.setReceiptType(new ReceiptType(fieldValue.getText(),
                            fieldValue.getConfidence()));
                        break;
                    case "MerchantName":
                        extractedReceiptItem.setMerchantName(setFieldValue(fieldValue, readResults));
                        break;
                    case "MerchantAddress":
                        extractedReceiptItem.setMerchantAddress(setFieldValue(fieldValue, readResults));
                        break;
                    case "MerchantPhoneNumber":
                        extractedReceiptItem.setMerchantPhoneNumber(setFieldValue(fieldValue, readResults));
                        break;
                    case "Subtotal":
                        extractedReceiptItem.setSubtotal(setFieldValue(fieldValue, readResults));
                        break;
                    case "Tax":
                        extractedReceiptItem.setTax(setFieldValue(fieldValue, readResults));
                        break;
                    case "Tip":
                        extractedReceiptItem.setTip(setFieldValue(fieldValue, readResults));
                        break;
                    case "Total":
                        extractedReceiptItem.setTotal(setFieldValue(fieldValue, readResults));
                        break;
                    case "TransactionDate":
                        extractedReceiptItem.setTransactionDate(setFieldValue(fieldValue, readResults));
                        break;
                    case "TransactionTime":
                        extractedReceiptItem.setTransactionTime(setFieldValue(fieldValue, readResults));
                        break;
                    case "Items":
                        extractedReceiptItem.setReceiptItems(toReceiptItems(fieldValue.getValueArray(), readResults));
                        break;
                    default:
                        break;
                }
            });
            extractedReceiptList.add(extractedReceiptItem);
        }
        return new IterableStream<>(extractedReceiptList);
    }

    private static FieldValue<?> setFieldValue(com.azure.ai.formrecognizer.implementation.models.FieldValue fieldValue,
                                            List<ReadResult> readResults) {
        FieldValue<?> value;
        switch (fieldValue.getType()) {
            case PHONE_NUMBER:
            case STRING:
            case TIME:
            case DATE:
                value = toFieldValueString(fieldValue);
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
        value.setElements(setReferenceElements(readResults, fieldValue.getElements()));
        return value;
    }

    private static PageMetadata getPageInfo(ReadResult readResultItem) {
        return new PageMetadata(TextLanguage.fromString(readResultItem.getLanguage().toString()),
            readResultItem.getHeight(), readResultItem.getPage(), readResultItem.getWidth(),
            readResultItem.getAngle(), DimensionUnit.fromString(readResultItem.getUnit().toString()));
    }

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

    private static BoundingBox toBoundingBox(List<Float> boundingBox) {
        List<Point> pointList = new ArrayList<>();
        for (int i = 0; i < boundingBox.size(); i += 2) {
            Point point = new Point(boundingBox.get(i), boundingBox.get(i + 1));
            pointList.add(point);
        }
        return new BoundingBox(pointList);
    }

    private static List<ReceiptItem> toReceiptItems(
        List<com.azure.ai.formrecognizer.implementation.models.FieldValue> fieldValue, List<ReadResult> readResults) {
        List<ReceiptItem> receiptItemList = new ArrayList<>();
        fieldValue.forEach(fieldValue1 -> {
            ReceiptItem receiptItem = new ReceiptItem();
            fieldValue1.getValueObject().forEach((key, fieldValue2) -> {
                switch (key) {
                    case "Quantity":
                        receiptItem.setQuantity(setFieldValue(fieldValue2, readResults));
                        break;
                    case "Name":
                        receiptItem.setName(setFieldValue(fieldValue2, readResults));
                        break;
                    case "TotalPrice":
                        receiptItem.setTotalPrice(setFieldValue(fieldValue2, readResults));
                        break;
                    default:
                        break;
                }
            });
            receiptItemList.add(receiptItem);
        });
        return receiptItemList;
    }

    private static IntegerValue toFieldValueInteger(com.azure.ai.formrecognizer.implementation.models.FieldValue
                                                        fieldValue2) {
        if (fieldValue2.getValueNumber() != null) {
            // TODO: Do not need this check, service team bug
            return new IntegerValue(fieldValue2.getText(), toBoundingBox(fieldValue2.getBoundingBox()),
                fieldValue2.getValueInteger());
        }

        return new IntegerValue(fieldValue2.getText(), toBoundingBox(fieldValue2.getBoundingBox()), 0);
    }

    private static StringValue toFieldValueString(com.azure.ai.formrecognizer.implementation.models.FieldValue
                                                      fieldValue2) {
        return new StringValue(fieldValue2.getText(), toBoundingBox(fieldValue2.getBoundingBox()),
            fieldValue2.getValueString());
    }

    private static FloatValue toFieldValueNumber(com.azure.ai.formrecognizer.implementation.models.FieldValue
                                                     fieldValue2) {
        return new FloatValue(fieldValue2.getText(), toBoundingBox(fieldValue2.getBoundingBox()),
            fieldValue2.getValueNumber());
    }
}
