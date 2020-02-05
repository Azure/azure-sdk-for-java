package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.models.AnalyzeResult;
import com.azure.ai.formrecognizer.implementation.models.DocumentResult;
import com.azure.ai.formrecognizer.implementation.models.ReadResult;
import com.azure.ai.formrecognizer.implementation.models.TextWord;
import com.azure.ai.formrecognizer.models.BoundingBox;
import com.azure.ai.formrecognizer.models.DimensionUnit;
import com.azure.ai.formrecognizer.models.ExtractedLine;
import com.azure.ai.formrecognizer.models.ExtractedWord;
import com.azure.ai.formrecognizer.models.FieldValue;
import com.azure.ai.formrecognizer.models.PageInfo;
import com.azure.ai.formrecognizer.models.ReceiptItem;
import com.azure.ai.formrecognizer.models.ReceiptPageResult;
import com.azure.ai.formrecognizer.models.ReceiptType;
import com.azure.ai.formrecognizer.models.TextLanguage;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to convert service level models to SDK exposes models.
 */
final class Transforms {
    private Transforms() {
    }

    static IterableStream<ReceiptPageResult> toReceipt(AnalyzeResult analyzeResult) {
        List<ReadResult> readResult = analyzeResult.getReadResults();
        List<DocumentResult> documentResult = analyzeResult.getDocumentResults();

        List<ReceiptPageResult> receiptPageResultList = new ArrayList<>();
        for (int i = 0; i < readResult.size(); i++) {
            ReceiptPageResult receiptPageResultItem = getReceiptReadResult(readResult, i);
            DocumentResult documentResultItem = documentResult.get(i);
            documentResultItem.getFields().forEach((key, fieldValue) -> {
                switch (key) {
                    case "ReceiptType":
                        receiptPageResultItem.setReceiptType(new ReceiptType(fieldValue.getText(),
                            fieldValue.getConfidence()));
                        break;
                    case "MerchantName":
                        receiptPageResultItem.setMerchantName(toFieldValueString(fieldValue));
                        break;
                    case "MerchantAddress":
                        receiptPageResultItem.setMerchantAddress(toFieldValueString(fieldValue));
                        break;
                    case "MerchantPhoneNumber":
                        receiptPageResultItem.setMerchantPhoneNumber(toFieldValueString(fieldValue));
                        break;
                    case "Subtotal":
                        receiptPageResultItem.setSubtotal(toFieldValueNumber(fieldValue));
                        break;
                    case "Tax":
                        receiptPageResultItem.setTax(toFieldValueNumber(fieldValue));
                        break;
                    case "Tip":
                        receiptPageResultItem.setTip(toFieldValueNumber(fieldValue));
                        break;
                    case "Total":
                        receiptPageResultItem.setTotal(toFieldValueNumber(fieldValue));
                        break;
                    case "TransactionDate":
                        receiptPageResultItem.setTransactionDate(toFieldValueString(fieldValue));
                        break;
                    case "TransactionTime":
                        receiptPageResultItem.setTransactionTime(toFieldValueString(fieldValue));
                        break;
                    case "Items":
                        receiptPageResultItem.setReceiptItems(toReceiptItems(fieldValue.getValueArray()));
                        break;
                    default:
                        break;
                }
            });
            receiptPageResultList.add(receiptPageResultItem);
        }
        return new IterableStream<>(receiptPageResultList);
    }

    private static ReceiptPageResult getReceiptReadResult(List<ReadResult> readResult, int i) {
        ReadResult readResultItem = readResult.get(i);
        PageInfo pageInfo = new PageInfo(TextLanguage.fromString(readResultItem.getLanguage().toString()),
            readResultItem.getHeight(), readResultItem.getPage(), readResultItem.getWidth(),
            readResultItem.getAngle(), DimensionUnit.fromString(readResultItem.getUnit().toString()));
        ReceiptPageResult receiptPageResultItem = new ReceiptPageResult(pageInfo);

        // raw ocr
        List<ExtractedLine> extractedLines = new ArrayList<>();
        if (readResultItem.getLines() != null) {
            readResultItem.getLines().forEach(textLine -> extractedLines.add(new ExtractedLine(new BoundingBox(textLine.getBoundingBox()), textLine.getText(),
                toWords(textLine.getWords()))));
            receiptPageResultItem.setExtractedLines(extractedLines);
        }
        return receiptPageResultItem;
    }

    static List<ExtractedWord> toWords(List<TextWord> words) {
        List<ExtractedWord> extractedWordList = new ArrayList<>();
        words.forEach(textWord -> extractedWordList.add(new ExtractedWord(new BoundingBox(textWord.getBoundingBox()),
            textWord.getText(), textWord.getConfidence())));
        return extractedWordList;
    }

    private static List<ReceiptItem> toReceiptItems(
        List<com.azure.ai.formrecognizer.implementation.models.FieldValue> fieldValue) {
        List<ReceiptItem> receiptItemList = new ArrayList<>();
        fieldValue.forEach(fieldValue1 -> {
            ReceiptItem receiptItem = new ReceiptItem();
            fieldValue1.getValueObject().forEach((key, fieldValue2) -> {
                switch (key) {
                    case "Quantity":
                        receiptItem.setQuantity(toFieldValueNumber(fieldValue2));
                        break;
                    case "Name":
                        receiptItem.setName(toFieldValueString(fieldValue2));
                        break;
                    case "TotalPrice":
                        receiptItem.setTotalPrice(toFieldValueNumber(fieldValue2));
                        break;
                    default:
                        break;
                }
            });
            receiptItemList.add(receiptItem);
        });
        return receiptItemList;
    }

    static FieldValue toFieldValueNumber(com.azure.ai.formrecognizer.implementation.models.FieldValue fieldValue2) {
        if (fieldValue2.getValueNumber() != null) {
            // TODO: Do not need this check, service team bug
            return new FieldValue().setText(fieldValue2.getText()).setConfidence(fieldValue2.getConfidence())
                .setValueNumber(fieldValue2.getValueNumber()).setBoundingBox(fieldValue2.getBoundingBox());
        }
        return new FieldValue().setText(fieldValue2.getText()).setConfidence(fieldValue2.getConfidence())
            .setBoundingBox(fieldValue2.getBoundingBox());
    }

    static FieldValue toFieldValueString(com.azure.ai.formrecognizer.implementation.models.FieldValue fieldValue2) {

        return new FieldValue().setText(fieldValue2.getText()).setConfidence(fieldValue2.getConfidence())
            .setValue(fieldValue2.getValueString()).setBoundingBox(fieldValue2.getBoundingBox());
    }

}
