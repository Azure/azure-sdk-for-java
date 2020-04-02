// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.ai.formrecognizer.implementation.Utility;
import com.azure.ai.formrecognizer.models.BoundingBox;
import com.azure.ai.formrecognizer.models.DateValue;
import com.azure.ai.formrecognizer.models.DimensionUnit;
import com.azure.ai.formrecognizer.models.Element;
import com.azure.ai.formrecognizer.models.ExtractedReceipt;
import com.azure.ai.formrecognizer.models.FieldValue;
import com.azure.ai.formrecognizer.models.FloatValue;
import com.azure.ai.formrecognizer.models.PageMetadata;
import com.azure.ai.formrecognizer.models.PageRange;
import com.azure.ai.formrecognizer.models.Point;
import com.azure.ai.formrecognizer.models.ReceiptItem;
import com.azure.ai.formrecognizer.models.ReceiptType;
import com.azure.ai.formrecognizer.models.StringValue;
import com.azure.ai.formrecognizer.models.WordElement;
import com.azure.core.util.IterableStream;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Contains helper methods for generating inputs for test methods
 */
final class TestUtils {

    static final String INVALID_KEY = "invalid key";
    static final String INVALID_URL = "htttttttps://localhost:8080";
    static final String VALID_HTTPS_LOCALHOST = "https://localhost:8080";
    static final String RECEIPT_LOCAL_URL = "src/test/resources/sample-files/contoso-allinone.jpg";

    static final Long FILE_LENGTH = new File(RECEIPT_LOCAL_URL).length();

    // Receipts
    static final String RECEIPT_URL = "https://raw.githubusercontent.com/Azure-Samples/"
        + "cognitive-services-REST-api-samples/master/curl/form-recognizer/contoso-allinone.jpg";

    static final String INVALID_RECEIPT_URL = "https://invalid.blob.core.windows.net/fr/contoso-allinone.jpg";

    private TestUtils() {
    }

    static IterableStream<ExtractedReceipt> getExtractedReceipts() {
        final int pageNumber = 1;
        PageMetadata pageMetadata = new PageMetadata(3000,
            1, 1688, 0.689300000667572, DimensionUnit.PIXEL);
        ReceiptType receiptType = new ReceiptType("Itemized", 0.692f);

        Point merchantNameTL = new Point(378.2f, 292.4f);
        Point merchantNameTR = new Point(1117.7f, 468.3f);
        Point merchantNameBR = new Point(1035.7f, 812.7f);
        Point merchantNameBL = new Point(96.3f, 636.8f);
        BoundingBox boundingBox = new BoundingBox(merchantNameTL, merchantNameTR, merchantNameBR, merchantNameBL);

        Point merchantNameElement1TL = new Point(639f, 510f);
        Point merchantNameElement1TR = new Point(1087f, 461f);
        Point merchantNameElement1BR = new Point(1098f, 551f);
        Point merchantNameElement1BL = new Point(646f, 604f);
        BoundingBox merchantNameElement1Box = new BoundingBox(merchantNameElement1TL, merchantNameElement1TR, merchantNameElement1BR, merchantNameElement1BL);
        Element merchantNameElement1 = new WordElement("Contoso", merchantNameElement1Box);

        Point merchantNameElement2TL = new Point(311, 575f);
        Point merchantNameElement2TR = new Point(517, 623f);
        Point merchantNameElement2BR = new Point(503f, 686f);
        Point merchantNameElement2BL = new Point(297f, 636f);
        BoundingBox merchantNameElement2Box = new BoundingBox(merchantNameElement2TL, merchantNameElement2TR, merchantNameElement2BR, merchantNameElement2BL);
        Element merchantNameElement2 = new WordElement("Contoso", merchantNameElement2Box);

        List<Element> merchantNameElementList = new ArrayList<>(Arrays.asList(merchantNameElement1, merchantNameElement2));
        FieldValue<?> merchantNameField = new StringValue("Contoso Contoso", boundingBox, "Contoso Contoso", pageNumber).setElements(merchantNameElementList);

        Point merchantAddrTL = new Point(302f, 675.8f);
        Point merchantAddrTR = new Point(848.1f, 793.7f);
        Point merchantAddrBR = new Point(809.9f, 70.4f);
        Point merchantAddrBL = new Point(263.9f, 852.5f);
        BoundingBox boundingBox1 = new BoundingBox(merchantAddrTL, merchantAddrTR, merchantAddrBR, merchantAddrBL);

        Point merchantAddressElement1TL = new Point(302f, 676f);
        Point merchantAddressElement1TR = new Point(390f, 695f);
        Point merchantAddressElement1BR = new Point(375f, 770f);
        Point merchantAddressElement1BL = new Point(287f, 751f);
        BoundingBox merchantAddrElement1Box = new BoundingBox(merchantAddressElement1TL, merchantAddressElement1TR, merchantAddressElement1BR, merchantAddressElement1BL);
        Element merchantAddrElement1 = new WordElement("123", merchantAddrElement1Box);

        Point merchantAddressElement2TL = new Point(405f, 698f);
        Point merchantAddressElement2TR = new Point(528f, 726f);
        Point merchantAddressElement2BR = new Point(512f, 802f);
        Point merchantAddressElement2BL = new Point(390f, 774f);
        BoundingBox merchantAddrElement2Box = new BoundingBox(merchantAddressElement2TL, merchantAddressElement2TR, merchantAddressElement2BR, merchantAddressElement2BL);
        Element merchantAddrElement2 = new WordElement("Main", merchantAddrElement2Box);

        Point merchantAddressElement3TL = new Point(542f, 730f);
        Point merchantAddressElement3TR = new Point(702f, 767f);
        Point merchantAddressElement3BR = new Point(685f, 845f);
        Point merchantAddressElement3BL = new Point(527f, 806f);
        BoundingBox merchantAddrElement3Box = new BoundingBox(merchantAddressElement3TL, merchantAddressElement3TR, merchantAddressElement3BR, merchantAddressElement3BL);
        Element merchantAddrElement3 = new WordElement("Street", merchantAddrElement3Box);

        Point merchantAddressElement4TL = new Point(293f, 784f);
        Point merchantAddressElement4TR = new Point(550f, 826f);
        Point merchantAddressElement4BR = new Point(540f, 905f);
        Point merchantAddressElement4BL = new Point(280f, 856f);
        BoundingBox merchantAddrElement4Box = new BoundingBox(merchantAddressElement4TL, merchantAddressElement4TR, merchantAddressElement4BR, merchantAddressElement4BL);
        Element merchantAddrElement4 = new WordElement("Redmond,", merchantAddrElement4Box);

        Point merchantAddressElement5TL = new Point(565f, 828f);
        Point merchantAddressElement5TR = new Point(645f, 837f);
        Point merchantAddressElement5BR = new Point(637f, 917f);
        Point merchantAddressElement5BL = new Point(555f, 907f);
        BoundingBox merchantAddrElement5Box = new BoundingBox(merchantAddressElement5TL, merchantAddressElement5TR, merchantAddressElement5BR, merchantAddressElement5BL);
        Element merchantAddrElement5 = new WordElement("WA", merchantAddrElement5Box);

        Point merchantAddressElement6TL = new Point(660f, 838f);
        Point merchantAddressElement6TR = new Point(824f, 849f);
        Point merchantAddressElement6BR = new Point(818f, 933f);
        Point merchantAddressElement6BL = new Point(651f, 919f);
        BoundingBox merchantAddrElement6Box = new BoundingBox(merchantAddressElement6TL, merchantAddressElement6TR, merchantAddressElement6BR, merchantAddressElement6BL);
        Element merchantAddrElement6 = new WordElement("98052", merchantAddrElement6Box);

        List<Element> merchantAddressElementList = new ArrayList<>(Arrays.asList(merchantAddrElement1, merchantAddrElement2, merchantAddrElement3, merchantAddrElement4, merchantAddrElement5, merchantAddrElement6));
        FieldValue<?> merchantAddress = new StringValue("123 Main Street Redmond, WA 98052", boundingBox1, "123 Main Street Redmond, WA 98052", pageNumber).setElements(merchantAddressElementList);

        Point merchantPhoneTL = new Point(278f, 1004f);
        Point merchantPhoneTR = new Point(656.3f, 1054.7f);
        Point merchantPhoneBR = new Point(646.8f, 1125.3f);
        Point merchantPhoneBL = new Point(268.5f, 1074.7f);
        BoundingBox boundingBox2 = new BoundingBox(merchantPhoneTL, merchantPhoneTR, merchantPhoneBR, merchantPhoneBL);

        Point merchantPhoneNumberElement1TL = new Point(278f, 1004f);
        Point merchantPhoneNumberElement1TR = new Point(656f, 1057f);
        Point merchantPhoneNumberElement1BR = new Point(647f, 1123f);
        Point merchantPhoneNumberElement1BL = new Point(271f, 1075f);
        BoundingBox merchantPhoneNumberElement1Box = new BoundingBox(merchantPhoneNumberElement1TL, merchantPhoneNumberElement1TR, merchantPhoneNumberElement1BR, merchantPhoneNumberElement1BL);
        Element merchantPhoneNumberElement1 = new WordElement("987-654-3210", merchantPhoneNumberElement1Box);
        List<Element> merchantPhoneNumberElementList = new ArrayList<>(Collections.singletonList(merchantPhoneNumberElement1));
        FieldValue<?> merchantPhoneNumber = new StringValue("987-654-3210", boundingBox2, "+19876543210", pageNumber).setElements(merchantPhoneNumberElementList);

        Point transactionDateTL = new Point(265.1f, 1228.4f);
        Point transactionDateTR = new Point(525f, 1247f);
        Point transactionDateBR = new Point(518.9f, 1332.1f);
        Point transactionDateBL = new Point(259f, 1313.5f);
        BoundingBox boundingBox3 = new BoundingBox(transactionDateTL, transactionDateTR, transactionDateBR, transactionDateBL);

        Point transactionDateElement1TL = new Point(267f, 1229f);
        Point transactionDateElement1TR = new Point(525f, 1247f);
        Point transactionDateElement1BR = new Point(517f, 1332f);
        Point transactionDateElement1BL = new Point(259f, 1313f);
        BoundingBox transactionDateElement1Box = new BoundingBox(transactionDateElement1TL, transactionDateElement1TR, transactionDateElement1BR, transactionDateElement1BL);
        Element transactionDateElement1 = new WordElement("6/10/2019", transactionDateElement1Box);
        List<Element> transactionDateElementList = new ArrayList<>(Collections.singletonList(transactionDateElement1));

        FieldValue<?> transactionDate = new DateValue("6/10/2019", boundingBox3, LocalDate.of(2019, 06, 10), pageNumber).setElements(transactionDateElementList);

        Point transactionTimeTL = new Point(541f, 1248f);
        Point transactionTimeTR = new Point(677.3f, 1261.5f);
        Point transactionTimeBR = new Point(668.9f, 1346.5f);
        Point transactionTimeBL = new Point(532.6f, 1333f);
        BoundingBox boundingBox4 = new BoundingBox(transactionTimeTL, transactionTimeTR, transactionTimeBR, transactionTimeBL);

        Point transactionTimeElement1TL = new Point(541f, 1248f);
        Point transactionTimeElement1TR = new Point(677f, 1263f);
        Point transactionTimeElement1BR = new Point(669f, 1345f);
        Point transactionTimeElement1BL = new Point(533f, 1333f);
        BoundingBox transactionTimeElement1Box = new BoundingBox(transactionTimeElement1TL, transactionTimeElement1TR, transactionTimeElement1BR, transactionTimeElement1BL);
        Element transactionTimeElement1 = new WordElement("13:59", transactionTimeElement1Box);
        List<Element> transactionTimeElementList = new ArrayList<>(Collections.singletonList(transactionTimeElement1));

        FieldValue<?> transactionTime = new StringValue("13:59", boundingBox4, "13:59:00", pageNumber).setElements(transactionTimeElementList);

        Point quantity1TL = new Point(245.1f, 1581.5f);
        Point quantity1TR = new Point(300.9f, 1585.1f);
        Point quantity1BL = new Point(295f, 1676f);
        Point quantity1BR = new Point(239.2f, 1672.4f);
        BoundingBox boundingBox5 = new BoundingBox(quantity1TL, quantity1TR, quantity1BL, quantity1BR);

        Point quantity1Element1TL = new Point(245f, 1583f);
        Point quantity1Element1TR = new Point(299f, 1585f);
        Point quantity1Element1BR = new Point(295f, 1676f);
        Point quantity1Element1BL = new Point(241f, 1671f);
        BoundingBox quantity1Element1Box = new BoundingBox(quantity1Element1TL, quantity1Element1TR, quantity1Element1BR, quantity1Element1BL);
        Element quantity1Element1 = new WordElement("1", quantity1Element1Box);
        List<Element> quantity1ElementList = new ArrayList<>(Collections.singletonList(quantity1Element1));

        FieldValue<?> quantity1 = new FloatValue("1", boundingBox5, null, pageNumber).setElements(quantity1ElementList);

        Point name1TL = new Point(322f, 1586f);
        Point name1TR = new Point(654.2f, 1601.1f);
        Point name1BL = new Point(650f, 1693f);
        Point name1BR = new Point(317.8f, 1678f);
        BoundingBox boundingBox6 = new BoundingBox(name1TL, name1TR, name1BL, name1BR);

        Point name1Element1TL = new Point(322f, 1586f);
        Point name1Element1TR = new Point(654f, 1605f);
        Point name1Element1BR = new Point(648f, 1689f);
        Point name1Element1BL = new Point(318f, 1678f);
        BoundingBox name1Element1Box = new BoundingBox(name1Element1TL, name1Element1TR, name1Element1BR, name1Element1BL);
        Element name1Element1 = new WordElement("Cappuccino", name1Element1Box);

        List<Element> name1ElementList = new ArrayList<>(Collections.singletonList(name1Element1));

        FieldValue<?> name1 = new StringValue("Cappuccino", boundingBox6, "Cappuccino", pageNumber).setElements(name1ElementList);

        Point total1TL = new Point(1107.7f, 1584f);
        Point total1TR = new Point(1263f, 1574f);
        Point total1BL = new Point(1268.3f, 1656f);
        Point total1BR = new Point(1113f, 1666f);
        BoundingBox boundingBox7 = new BoundingBox(total1TL, total1TR, total1BL, total1BR);

        Point total1Element1TL = new Point(1108f, 1584f);
        Point total1Element1TR = new Point(1263f, 1574f);
        Point total1Element1BR = new Point(1268f, 1656f);
        Point total1Element1BL = new Point(1113f, 1666f);
        BoundingBox total1Element1Box = new BoundingBox(total1Element1TL, total1Element1TR, total1Element1BR, total1Element1BL);
        Element total1Element1 = new WordElement("$2.20", total1Element1Box);
        List<Element> total1ElementList = new ArrayList<>(Collections.singletonList(total1Element1));
        FieldValue<?> total1 = new FloatValue("$2.20", boundingBox7, 2.2f, pageNumber).setElements(total1ElementList);

        Point quantity2TL = new Point(232f, 1834f);
        Point quantity2TR = new Point(286.6f, 1836f);
        Point quantity2BL = new Point(285f, 1921f);
        Point quantity2BR = new Point(230.4f, 1920f);

        Point quantity2Element1TL = new Point(232f, 1834f);
        Point quantity2Element1TR = new Point(286f, 1836f);
        Point quantity2Element1BR = new Point(285f, 1920f);
        Point quantity2Element1BL = new Point(231f, 1920f);
        BoundingBox quantity2Element1Box = new BoundingBox(quantity2Element1TL, quantity2Element1TR, quantity2Element1BR, quantity2Element1BL);
        Element quantity2Element1 = new WordElement("1", quantity2Element1Box);
        List<Element> quantity2ElementList = new ArrayList<>(Collections.singletonList(quantity2Element1));

        BoundingBox boundingBox8 = new BoundingBox(quantity2TL, quantity2TR, quantity2BL, quantity2BR);
        FieldValue<?> quantity2 = new FloatValue("1", boundingBox8, null, pageNumber).setElements(quantity2ElementList);

        Point name2TL = new Point(232f, 1834f);
        Point name2TR = new Point(232f, 1834f);
        Point name2BL = new Point(285f, 1920f);
        Point name2BR = new Point(231f, 1920f);
        BoundingBox boundingBox9 = new BoundingBox(name2TL, name2TR, name2BL, name2BR);

        Point name2Element1TL = new Point(308f, 1836f);
        Point name2Element1TR = new Point(506f, 1841f);
        Point name2Element1BR = new Point(504f, 1920f);
        Point name2Element1BL = new Point(307f, 1920f);
        BoundingBox name2Element1Box = new BoundingBox(name2Element1TL, name2Element1TR, name2Element1BR, name2Element1BL);
        Element name2Element1 = new WordElement("BACON", name2Element1Box);

        Point name2Element2TL = new Point(523f, 1841f);
        Point name2Element2TR = new Point(568f, 1842f);
        Point name2Element2BR = new Point(566f, 1921f);
        Point name2Element2BL = new Point(521f, 1921f);
        BoundingBox name2Element2Box = new BoundingBox(name2Element2TL, name2Element2TR, name2Element2BR, name2Element2BL);
        Element name2Element2 = new WordElement("&", name2Element2Box);

        Point name2Element3TL = new Point(585f, 1842f);
        Point name2Element3TR = new Point(746f, 1843f);
        Point name2Element3BR = new Point(744f, 1924f);
        Point name2Element3BL = new Point(583f, 1921f);
        BoundingBox name2Element3Box = new BoundingBox(name2Element3TL, name2Element3TR, name2Element3BR, name2Element3BL);
        Element name2Element3 = new WordElement("EGGS", name2Element3Box);

        List<Element> name2ElementList = new ArrayList<>(Arrays.asList(name2Element1, name2Element2, name2Element3));
        FieldValue<?> name2 = new StringValue("BACON & EGGS", boundingBox9, "BACON & EGGS", pageNumber).setElements(name2ElementList);

        Point total2TL = new Point(1107.7f, 1584f);
        Point total2TR = new Point(1263f, 1574f);
        Point total2BL = new Point(1268.3f, 1656f);
        Point total2BR = new Point(1113f, 1666f);
        BoundingBox boundingBox10 = new BoundingBox(total2TL, total2TR, total2BL, total2BR);

        Point total2Element1TL = new Point(1135f, 1955f);
        Point total2Element1TR = new Point(1257f, 1952f);
        Point total2Element1BR = new Point(1259f, 2036f);
        Point total2Element1BL = new Point(1136f, 2039f);
        BoundingBox total2Element1Box = new BoundingBox(total2Element1TL, total2Element1TR, total2Element1BR, total2Element1BL);
        Element total2Element1 = new WordElement("$9.5", total2Element1Box);
        List<Element> total2ElementList = new ArrayList<>(Collections.singletonList(total2Element1));
        FieldValue<?> total2 = new FloatValue("$9.5", boundingBox10, null, pageNumber).setElements(total2ElementList);

        List<ReceiptItem> receiptItemList = new ArrayList<>(Arrays.asList(
            new ReceiptItem().setName(name1).setQuantity(quantity1).setTotalPrice(total1),
            new ReceiptItem().setName(name2).setQuantity(quantity2).setTotalPrice(total2)));
        ExtractedReceipt extractedReceipt = new ExtractedReceipt(pageMetadata, new PageRange(1, 1)).setReceiptItems(receiptItemList)
            .setReceiptType(receiptType).setMerchantAddress(merchantAddress).setMerchantName(merchantNameField)
            .setMerchantPhoneNumber(merchantPhoneNumber).setTransactionDate(transactionDate)
            .setTransactionTime(transactionTime);

        return new IterableStream<>(new ArrayList<>(Collections.singletonList(extractedReceipt)));
    }

    static InputStream getReceiptFileData() {
        try {
            return new FileInputStream(RECEIPT_LOCAL_URL);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Local Receipt file not found.", e);
        }
    }

    static Flux<ByteBuffer> getReceiptFileBufferData() {
        return Utility.convertStreamToByteBuffer(getReceiptFileData());
    }
}
