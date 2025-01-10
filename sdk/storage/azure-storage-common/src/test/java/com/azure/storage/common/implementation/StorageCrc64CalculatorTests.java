// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.stream.IntStream;
import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StorageCrc64CalculatorTests {
    private static final int KB = 1024;
    private static final String HEX_DATA = "C8E11B40D793D1526018"; // '\\xC8\\xE1\\x1B\\x40\\xD7\\x93\\xD1\\x52\\x60\\x18'
    private static final byte[] HEX_BYTES = hexStringToByteArray();

    @ParameterizedTest
    @CsvSource({
        "'', 0, 0",
        "'Hello World!', 0, 208604604655264165",
        "'123456789!@#$%^&*()', 0, 2153758901452455624",
        "'This is a test where the data is longer than 64 characters so that we can test that code path.', 0, 2736107658526394369", })
    void testCompute(String data, long initial, long expected) {
        byte[] bytes = data.getBytes();
        long actual = StorageCrc64Calculator.compute(bytes, initial);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 3386042136331673945",
        "208604604655264165, 4570059697646401418",
        "2153758901452455624, 13366433516720813220",
        "12345, 5139183895903464380" })
    void testComputeWithBinaryData(long initial, String expectedStr) {
        //byte[] hexBytes = hexStringToByteArray(hexData);
        long expected = unsignedLongFromString(expectedStr);
        long actual = StorageCrc64Calculator.compute(HEX_BYTES, initial);
        assertEquals(expected, actual);
    }

    private static byte[] hexStringToByteArray() {
        //int length = StorageCrc64CalculatorTests.hexData.length();
        int length = HEX_DATA.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(StorageCrc64CalculatorTests.HEX_DATA.charAt(i), 16) << 4)
                + Character.digit(StorageCrc64CalculatorTests.HEX_DATA.charAt(i + 1), 16));
        }
        return data;
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 10 })
    void testCompose(int numSegments) {
        int blockSize = KB;
        byte[] data = new byte[numSegments * blockSize];
        new Random().nextBytes(data);

        long wholeCrc = StorageCrc64Calculator.compute(data, 0);
        Queue<Long> blockCrcs = new LinkedList<>();
        IntStream.range(0, numSegments)
            .forEach(i -> blockCrcs
                .add(StorageCrc64Calculator.compute(Arrays.copyOfRange(data, i * blockSize, (i + 1) * blockSize), 0)));

        long composedCrc = blockCrcs.poll();
        int i = 1;
        while (!blockCrcs.isEmpty()) {
            long nextBlockCrc = blockCrcs.poll();
            composedCrc
                = StorageCrc64Calculator.concat(0, 0, composedCrc, ((long) blockSize * i), 0, nextBlockCrc, blockSize);
            i++;
        }

        assertEquals(wholeCrc, composedCrc);
    }

    @ParameterizedTest
    @ValueSource(ints = { 2, 3, 10 })
    void testComposeDifferingBlockSizes(int numSegments) {
        int minBlockSize = KB, maxBlockSize = 4 * KB;
        Random random = new Random();

        List<Integer> blockLengths = new ArrayList<>();
        IntStream.range(0, numSegments)
            .forEach(i -> blockLengths.add(random.nextInt(maxBlockSize - minBlockSize) + minBlockSize));

        byte[] data = new byte[blockLengths.stream().mapToInt(Integer::intValue).sum()];
        random.nextBytes(data);

        long wholeCrc = StorageCrc64Calculator.compute(data, 0);
        Queue<Long> blockCrcs = new LinkedList<>();
        int offset = 0;
        for (int length : blockLengths) {
            blockCrcs.add(StorageCrc64Calculator.compute(Arrays.copyOfRange(data, offset, offset + length), 0));
            offset += length;
        }

        long composedCrc = blockCrcs.poll();
        int lengthIndex = 1;
        while (!blockCrcs.isEmpty()) {
            long nextBlockCrc = blockCrcs.poll();
            composedCrc = StorageCrc64Calculator.concat(0, 0, composedCrc, blockLengths.get(lengthIndex - 1), 0,
                nextBlockCrc, blockLengths.get(lengthIndex));
            lengthIndex++;
        }

        assertEquals(wholeCrc, composedCrc);
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0, 0, 0, 0",
        "17360427831495520774, 949533, 13068224794440996385, 99043, 2942932174470096852",
        "11788770130477425887, 505156, 11825964890373840515, 543420, 11679439596881108042",
        "3295333047304801182, 732633, 15304759627474960884, 315943, 31840984168952505",
        "16590039424904606984, 550299, 6063316096266934453, 498277, 4430932446378441680",
        "4505532069077416052, 852279, 4910763717047934640, 196297, 15119506491662968913",
        "390777554329396866, 834642, 2639871931800812330, 213934, 11705441749781302341",
        "3373070654000205532, 282713, 998330282635826126, 765863, 9830625244855600085",
        "2124306943908111903, 737293, 11017351202683543503, 311283, 8163771928713973931",
        "15994440356403990005, 85861, 13803536430055425947, 962715, 1941624554903785510",
        "3705122932895036835, 444701, 17573219681510991482, 603875, 4421337306620606216" })
    void testConcat(String crc1Str, long size1, String crc2Str, long size2, String expectedStr) {
        // Convert the large numbers from String to BigInteger for unsigned support
        long crc1 = unsignedLongFromString(crc1Str);
        long crc2 = unsignedLongFromString(crc2Str);
        long expected = unsignedLongFromString(expectedStr);

        long actual = StorageCrc64Calculator.concat(0, 0, crc1, size1, 0, crc2, size2);
        assertEquals(expected, actual);
    }

    private long unsignedLongFromString(String value) {
        // Convert a string to a signed long interpreting it as unsigned 64-bit integer
        return new BigInteger(value).longValue();
    }

    @ParameterizedTest
    @CsvSource({
        "0, 0, 0, 0, 0, 0, 0, 0",
        "556425425686929588, 346224202686926702, 16342296696377857982, 332915, 1153230192133190692, 12153371329672466699, 715661, 1441822370130745021",
        "6707243468313456313, 572263087298867634, 16994544883182326144, 75745, 9131338361339398429, 10182915179976307502, 972831, 14966971284513070994",
        "5753644013440131291, 5049702011265556767, 17549647897932809624, 255140, 7204171574261853450, 1993084328138883374, 793436, 6041621697050742380",
        "6856094926385348025, 5380840211500611709, 9696539459657763690, 537777, 4787042077805010903, 13660128687379374948, 510799, 17784586126519415898",
        "7768574238870932405, 97145001356670685, 607054043350981298, 706788, 667444555190985522, 10677778047180339455, 341788, 5763961866513573791",
        "3302120679354661969, 7763531798276712053, 8827557196489825944, 490442, 8582969104890206846, 6702182603435500761, 558134, 4787302867829109706",
        "7553023568245626261, 9093436341919279996, 10815569438302788871, 785480, 8305342016037017917, 6633140726058569127, 263096, 14625483825524673467",
        "8894905328920043035, 9101951045389247372, 10098427678135105249, 782758, 8101576936188464286, 8318237935995533450, 265818, 5983082645903611588",
        "1084935736425738155, 5378644106529179816, 13762475631325587388, 1014816, 8473418370223760471, 10401355811619715622, 33760, 3692020299859515126",
        "889000539881195835, 2971048229276949174, 5346315327374690144, 307387, 1407121768110541356, 10535852615249992663, 741189, 3634018251978804152" })
    void testConcatWithInitials(String initialStr, String initial1Str, String crc1Str, long size1, String initial2Str,
        String crc2Str, long size2, String expectedStr) {
        // Convert large unsigned values to signed long
        long initial = unsignedLongFromString(initialStr);
        long initial1 = unsignedLongFromString(initial1Str);
        long crc1 = unsignedLongFromString(crc1Str);
        long initial2 = unsignedLongFromString(initial2Str);
        long crc2 = unsignedLongFromString(crc2Str);
        long expected = unsignedLongFromString(expectedStr);

        long actual = StorageCrc64Calculator.concat(initial, initial1, crc1, size1, initial2, crc2, size2);
        assertEquals(expected, actual);
    }
}
