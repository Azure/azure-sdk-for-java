// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized.cryptography;

import com.azure.core.cryptography.AsyncKeyEncryptionKey;
import com.azure.storage.blob.BlobServiceVersion;
import org.reactivestreams.Subscriber;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This type generates Flowables that emit ByteBuffers in specific patterns depending on test case. It is used to
 * exercise the decrypt logic and ensure it always returns only the data requested by appropriately trimming data that
 * was downloaded only for the sake of successful decryption.
 *
 * There are nine interesting locations for the start/end of a ByteBuffer:
 * 1. Start of the download
 * 2. Middle of offsetAdjustment
 * 3. Last byte of offsetAdjustment
 * 4. First byte of user requested data
 * 5. Middle of user requested data
 * 6. Last byte of user requested data
 * 7. First byte of end adjustment
 * 8. Middle of end adjustment
 * 9. Last byte of download
 *
 * The tests below will cover all meaningful pairs, of which there are 36, in as few distinct runs as possible.
 * The notation a/b indicates that the ByteBuffer starts at location a and ends in location b inclusive.
 *
 */
public class EncryptedFlux extends Flux<ByteBuffer> {

    private ByteBuffer plainText;

    private ByteBuffer cipherText;

    private int testCase;

    private EncryptionData encryptionData;

    public static final int DATA_OFFSET = 10;

    public static final int DATA_COUNT = 40;

    private static final int DOWNLOAD_SIZE = 64; // The total size of the "download" after expanding the requested range.

    /*
    These constants correspond to the positions above.
    ByteBuffer limit() is exclusive, which is why we add one to a limit if that byte is supposed to be included, as in
    the case of the last byte of the offsetAdjustment.
    */
    private static final int POSITION_ONE = 0;

    private static final int POSITION_TWO = DATA_OFFSET / 2;

    private static final int POSITION_THREE_POSITION = DATA_OFFSET - 1;

    private static final int POSITION_THREE_LIMIT = POSITION_THREE_POSITION + 1;

    private static final int POSITION_FOUR_POSITION  = DATA_OFFSET;

    private static final int POSITION_FOUR_LIMIT = POSITION_FOUR_POSITION + 1;

    private static final int POSITION_FIVE = DATA_OFFSET + (DATA_COUNT / 2);

    private static final int POSITION_SIX_POSITION = DATA_OFFSET + DATA_COUNT - 1;

    private static final int POSITION_SIX_LIMIT = POSITION_SIX_POSITION + 1;

    private static final int POSITION_SEVEN_POSITION = DATA_OFFSET + DATA_COUNT;

    private static final int POSITION_SEVEN_LIMIT = POSITION_SEVEN_POSITION + 1;

    private static final int POSITION_EIGHT = DOWNLOAD_SIZE - 10;

    private static final int POSITION_NINE_POSITION = DOWNLOAD_SIZE - 1;

    private static final int POSITION_NINE_LIMIT = POSITION_NINE_POSITION + 1;

    /*
    Test cases. Unfortunately because each case covers multiple combinations, the name of each case cannot provide
    much insight into to the pairs being tested, but each is commented with the pairs that it tests. There are a
    couple redundancies that are necessary to complete sending the entire data when one combination excludes using other
    new combinations.
     */

    public static final int CASE_ZERO = 0; // 1/2; 2/3; 4/5; 5/6; 7/8; 8/9

    public static final int CASE_ONE = 1; // 1/3; 4/6; 7/9

    public static final int CASE_TWO = 2; // 1/4; 5/7; 8/9

    public static final int CASE_THREE = 3; // 1/5; 6/7; 8/9

    public static final int CASE_FOUR = 4; // 1/6; 7/9

    public static final int CASE_FIVE = 5; // 1/7; 8/9

    public static final int CASE_SIX = 6; // 1/8; 8/9

    public static final int CASE_SEVEN = 7; // 1/9;

    public static final int CASE_EIGHT = 8; // 1/2; 2/4; 5/8; 8/9

    public static final int CASE_NINE = 9; // 1/2; 2/5; 6/8; 8/9

    public static final int CASE_TEN = 10; // 1/2; 2/6; 7/9

    public static final int CASE_ELEVEN  = 11; // 1/2; 2/7; 8/9

    public static final int CASE_TWELVE = 12; // 1/2; 2/8; 8/9

    public static final int CASE_THIRTEEN = 13; // 1/2; 2/9

    public static final int CASE_FOURTEEN = 14; // 1/2; 3/4; 5/9

    public static final int CASE_FIFTEEN = 15; // 1/2; 3/5; 6/9

    public static final int CASE_SIXTEEN = 16; // 1/2; 3/6; 7/9

    public static final int CASE_SEVENTEEN = 17; // 1/2; 3/7; 8/9

    public static final int CASE_EIGHTEEN = 18; // 1/2; 3/8; 8/9

    public static final int CASE_NINETEEN = 19; // 1/2; 3/9

    public static final int CASE_TWENTY = 20; // 1/3; 4/7; 8/9;

    public static final int CASE_TWENTY_ONE = 21; // 1/3; 4/8; 8/9;

    public static final int CASE_TWENTY_TWO = 22; // 1/3; 4/9;

    public EncryptedFlux(int testCase, AsyncKeyEncryptionKey key, APISpec spec) throws InvalidKeyException {
        this.testCase = testCase;
        this.plainText = spec.getRandomData(DOWNLOAD_SIZE - 2); // This will yield two bytes of padding... for fun.

        EncryptedBlob encryptedBlob = new EncryptedBlobAsyncClient(
            null, "https://random.blob.core.windows.net", BlobServiceVersion.getLatest(), null, null, null, null, null, null, key, "keyWrapAlgorithm", null)
            .encryptBlob(Flux.just(this.plainText)).block();
        this.cipherText = APISpec.collectBytesInBuffer(encryptedBlob.getCiphertextFlux()).block();
        this.encryptionData = encryptedBlob.getEncryptionData();
    }

    private void sendBuffs(List<Integer> positionArr, List<Integer> limitArr, Subscriber<? super ByteBuffer> s) {
        for (int i = 0; i < positionArr.size(); i++) {
            ByteBuffer next = this.cipherText.duplicate();
            next.position(positionArr.get(i)).limit(limitArr.get(i));
            s.onNext(next);
        }
    }

    @Override
    public void subscribe(CoreSubscriber<? super ByteBuffer> s) {
        List<Integer> positionArr;
        List<Integer> limitArr;
        switch (this.testCase) {
            case CASE_ZERO:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_TWO, POSITION_FOUR_POSITION, POSITION_FIVE,
                    POSITION_SEVEN_POSITION, POSITION_EIGHT);
                limitArr = Arrays.asList(POSITION_TWO, POSITION_THREE_LIMIT, POSITION_FIVE, POSITION_SIX_LIMIT,
                    POSITION_EIGHT, POSITION_NINE_LIMIT);
                break;
            case CASE_ONE:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_FOUR_POSITION, POSITION_SEVEN_POSITION);
                limitArr = Arrays.asList(POSITION_THREE_LIMIT, POSITION_SIX_LIMIT, POSITION_NINE_LIMIT);
                break;
            case CASE_TWO:
                /*
                Note here and in some cases below, without loss of generality, we use FOUR_LIMIT instead of FIVE for the
                range 5/7. This is because if we did strictly 1/4 and 5/7, we would skip the values between the
                constants that are set as positions 4 and 5. Using FOUR_LIMIT is equivalent in effect to using FIVE as
                POSITION_FIVE is representative of any byte within the range.
                 */
                positionArr = Arrays.asList(POSITION_ONE, POSITION_FOUR_LIMIT, POSITION_SEVEN_LIMIT);
                limitArr = Arrays.asList(POSITION_FOUR_LIMIT, POSITION_SEVEN_LIMIT, POSITION_NINE_LIMIT);
                break;
            case CASE_THREE:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_SIX_POSITION, POSITION_SEVEN_LIMIT);
                limitArr = Arrays.asList(POSITION_SIX_POSITION, POSITION_SEVEN_LIMIT, POSITION_NINE_LIMIT);
                break;
            case CASE_FOUR:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_SEVEN_POSITION);
                limitArr = Arrays.asList(POSITION_SIX_LIMIT, POSITION_NINE_LIMIT);
                break;
            case CASE_FIVE:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_SEVEN_LIMIT);
                limitArr = Arrays.asList(POSITION_SEVEN_LIMIT, POSITION_NINE_LIMIT);
                break;
            case CASE_SIX:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_EIGHT);
                limitArr = Arrays.asList(POSITION_EIGHT, POSITION_NINE_LIMIT);
                break;
            case CASE_SEVEN:
                positionArr = Collections.singletonList(POSITION_ONE);
                limitArr = Collections.singletonList(POSITION_NINE_LIMIT);
                break;
            case CASE_EIGHT:
            case CASE_THIRTEEN:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_TWO);
                limitArr = Arrays.asList(POSITION_TWO, POSITION_NINE_LIMIT);
                break;
            case CASE_NINE:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_TWO, POSITION_SIX_POSITION, POSITION_EIGHT);
                limitArr = Arrays.asList(POSITION_TWO, POSITION_SIX_POSITION, POSITION_EIGHT, POSITION_NINE_LIMIT);
                break;
            case CASE_TEN:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_TWO, POSITION_SEVEN_POSITION);
                limitArr = Arrays.asList(POSITION_TWO, POSITION_SIX_LIMIT, POSITION_NINE_LIMIT);
                break;
            case CASE_ELEVEN:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_TWO, POSITION_SEVEN_LIMIT);
                limitArr = Arrays.asList(POSITION_TWO, POSITION_SEVEN_LIMIT, POSITION_NINE_LIMIT);
                break;
            case CASE_TWELVE:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_TWO, POSITION_EIGHT);
                limitArr = Arrays.asList(POSITION_TWO, POSITION_EIGHT, POSITION_NINE_LIMIT);
                break;
            case CASE_FOURTEEN:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_THREE_POSITION, POSITION_FOUR_LIMIT);
                limitArr = Arrays.asList(POSITION_THREE_POSITION, POSITION_FOUR_LIMIT, POSITION_NINE_LIMIT);
                break;
            case CASE_FIFTEEN:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_THREE_POSITION, POSITION_SIX_POSITION);
                limitArr = Arrays.asList(POSITION_THREE_POSITION, POSITION_SIX_POSITION, POSITION_NINE_LIMIT);
                break;
            case CASE_SIXTEEN:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_THREE_POSITION, POSITION_SEVEN_POSITION);
                limitArr = Arrays.asList(POSITION_THREE_POSITION, POSITION_SIX_LIMIT, POSITION_NINE_LIMIT);
                break;
            case CASE_SEVENTEEN:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_THREE_POSITION, POSITION_SEVEN_LIMIT);
                limitArr = Arrays.asList(POSITION_THREE_POSITION, POSITION_SEVEN_LIMIT, POSITION_NINE_LIMIT);
                break;
            case CASE_EIGHTEEN:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_THREE_POSITION, POSITION_EIGHT);
                limitArr = Arrays.asList(POSITION_THREE_POSITION, POSITION_EIGHT, POSITION_NINE_LIMIT);
                break;
            case CASE_NINETEEN:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_THREE_POSITION);
                limitArr = Arrays.asList(POSITION_THREE_POSITION, POSITION_NINE_LIMIT);
                break;
            case CASE_TWENTY:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_THREE_LIMIT, POSITION_SEVEN_LIMIT);
                limitArr = Arrays.asList(POSITION_THREE_LIMIT, POSITION_SEVEN_LIMIT, POSITION_NINE_LIMIT);
                break;
            case CASE_TWENTY_ONE:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_THREE_LIMIT, POSITION_EIGHT);
                limitArr = Arrays.asList(POSITION_THREE_LIMIT, POSITION_EIGHT, POSITION_NINE_LIMIT);
                break;
            case CASE_TWENTY_TWO:
                positionArr = Arrays.asList(POSITION_ONE, POSITION_THREE_LIMIT);
                limitArr = Arrays.asList(POSITION_THREE_LIMIT, POSITION_NINE_LIMIT);
                break;
            default:
                throw new IllegalStateException("Unexpected case number");
        }
        sendBuffs(positionArr, limitArr, s);
        s.onComplete();
    }

    public ByteBuffer getPlainText() {
        return this.plainText;
    }

    public EncryptionData getEncryptionData() {
        return this.encryptionData;
    }
}
