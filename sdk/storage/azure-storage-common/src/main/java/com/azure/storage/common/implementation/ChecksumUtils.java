// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.BinaryData;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.DownloadTransferValidationOptions;
import com.azure.storage.common.StorageChecksumAlgorithm;
import com.azure.storage.common.TransferValidationOptions;
import com.azure.storage.common.UploadTransferValidationOptions;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class ChecksumUtils {
    public static UploadTransferValidationOptions getDefaultUploadValidationOptions() {
        return new UploadTransferValidationOptions().setChecksumAlgorithm(StorageChecksumAlgorithm.None);
    }

    public static DownloadTransferValidationOptions getDefaultDownloadValidationOptions() {
        return new DownloadTransferValidationOptions().setChecksumAlgorithm(StorageChecksumAlgorithm.None);
    }

    public static TransferValidationOptions getDefaultTransferValidationOptions() {
        return new TransferValidationOptions()
            .setUpload(getDefaultUploadValidationOptions())
            .setDownload(getDefaultDownloadValidationOptions());
    }

    public static boolean isAlgorithmSelected(StorageChecksumAlgorithm algorithm) {
        return algorithm != null && algorithm != StorageChecksumAlgorithm.None;
    }

    public static StorageChecksumAlgorithm resolveAuto(StorageChecksumAlgorithm algorithm) {
        return algorithm == StorageChecksumAlgorithm.Auto
            ? StorageChecksumAlgorithm.StorageCrc64
            : algorithm;
    }

    public static Checksum initializeChecksum(StorageChecksumAlgorithm algorithm) {
        switch (resolveAuto(algorithm)) {
            case MD5:
                try {
                    return MessageDigestChecksum.create(MessageDigest.getInstance("MD5"));
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }
            case StorageCrc64:
                return StorageCrc64Checksum.create();
            default:
                throw new IllegalArgumentException("Could not initialize given algorithm.");
        }
    }

    public static Mono<Tuple2<BinaryData, ChecksumValue>> checksumDataAsync(BinaryData data,
        UploadTransferValidationOptions validationOptions) {
        if (validationOptions == null) {
            return Mono.just(Tuples.of(data, new ChecksumValue(null, StorageChecksumAlgorithm.None)));
        }
        boolean checksumCalculationNeeded = isAlgorithmSelected(validationOptions.getChecksumAlgorithm())
            && validationOptions.getPrecalculatedChecksum() == null;

        if (!checksumCalculationNeeded) {
            return Mono.just(Tuples.of(data, new ChecksumValue(validationOptions)));
        }
        return checksumDataAsync(data, validationOptions.getChecksumAlgorithm());
    }

    public static Mono<Tuple2<BinaryData, ChecksumValue>> checksumDataAsync(BinaryData data,
        StorageChecksumAlgorithm algorithm) {
        StorageChecksumAlgorithm resolvedAlgorithm = resolveAuto(algorithm);
        Mono<BinaryData> replayableData = data.isReplayable() ? Mono.just(data) : data.toReplayableBinaryDataAsync();
        return replayableData
            // must read as flux for payloads larger than max array length
            .flatMapMany(BinaryData::toFluxByteBuffer)
            .collect(
                () -> ChecksumUtils.initializeChecksum(resolvedAlgorithm),
                Checksum::update)
            .flatMap(checksum -> replayableData.map(bData -> Tuples.of(bData,
                new ChecksumValue(checksum.getValue(), resolvedAlgorithm))));
    }

    public static boolean shouldValidateResponse(DownloadTransferValidationOptions validationOptions) {
        if (validationOptions == null || validationOptions.getChecksumAlgorithm() == StorageChecksumAlgorithm.None) {
            return false;
        }
        return validationOptions.getAutoValidateChecksum();
    }

    public static void assertChecksumMatch(ChecksumValue checksumValue, HttpHeaders responseHeaders) {
        if (checksumValue == null || checksumValue.getChecksum() == null) {
            throw new IllegalArgumentException("Did not provide a checksum to compare.");
        }
        String responseChecksumBase64;
        switch (resolveAuto(checksumValue.getAlgorithm())) {
            case MD5:
                responseChecksumBase64 = responseHeaders.getValue("Content-MD5");
                break;
            case StorageCrc64:
                responseChecksumBase64 = responseHeaders.getValue("x-ms-content-crc64");
                break;
            default:
                throw new IllegalArgumentException("Checksum algorithm unrecognized.");
        }
        byte[] responseChecksum = Base64.getDecoder().decode(responseChecksumBase64);
        byte[] calculatedChecksum = checksumValue.getChecksum();

        if (!Arrays.equals(calculatedChecksum, responseChecksum)) {
            throw new RuntimeException("Checksum did not match calculated data.");
        }
    }

    public static UploadTransferValidationOptions md5ToOptions(byte[] md5) {
        if (md5 == null) {
            return null;
        }
        return new UploadTransferValidationOptions().setChecksumAlgorithm(StorageChecksumAlgorithm.MD5)
            .setPrecalculatedChecksum(CoreUtils.clone(md5));
    }

    public static byte[] md5FromOptions(UploadTransferValidationOptions transferValidation) {
        return transferValidation != null && transferValidation.getChecksumAlgorithm() == StorageChecksumAlgorithm.MD5
            ? CoreUtils.clone(transferValidation.getPrecalculatedChecksum())
            : null;
    }

    public static DownloadTransferValidationOptions requestMd5ToOptions(boolean requestMd5) {
        return requestMd5
            ? new DownloadTransferValidationOptions().setChecksumAlgorithm(StorageChecksumAlgorithm.MD5)
            : null;
    }

    public static UploadTransferValidationOptions checksumValueToOptions(ChecksumValue checksum) {
        if (checksum == null || checksum.getAlgorithm() == StorageChecksumAlgorithm.None) {
            return null;
        }
        return new UploadTransferValidationOptions().setChecksumAlgorithm(checksum.getAlgorithm())
            .setPrecalculatedChecksum(checksum.getChecksum());
    }
}
