/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.datalake.store.uploader;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.text.MessageFormat;

/**
 * An internally used class for generating the metadata used for upload.
 */
public class UploadMetadataGenerator {

    private UploadParameters _parameters;
    private int _maxAppendLength;

    /**
     * Creates a new instance of the UploadMetadataGenerator with the given parameters and the default maximum append length.
     *
     * @param parameters The parameters.
     */
    public UploadMetadataGenerator(UploadParameters parameters) {
        this(parameters, SingleSegmentUploader.BufferLength);
    }

    /**
     * Creates a new instance of the UploadMetadataGenerator with the given parameters and the given maximum append length.
     *
     * @param parameters The parameters
     * @param maxAppendLength The maximum allowed append length when uploading a file.
     */
    public UploadMetadataGenerator(UploadParameters parameters, int maxAppendLength) {
        _parameters = parameters;
        _maxAppendLength = maxAppendLength;
    }

    /**
     * Attempts to load the metadata from an existing file in its canonical location.
     *
     * @param metadataFilePath The metadata file path.
     * @return The deserialized {@link UploadMetadata} from the specified file path.
     * @throws FileNotFoundException
     * @throws InvalidMetadataException
     */
    public UploadMetadata GetExistingMetadata(String metadataFilePath) throws FileNotFoundException, InvalidMetadataException {
        //load from file (based on input parameters)
        UploadMetadata metadata = UploadMetadata.LoadFrom(metadataFilePath);
        metadata.ValidateConsistency();
        return metadata;
    }

    /**
     * Creates a new metadata based on the given input parameters, and saves it to its canonical location.
     *
     * @param metadataFilePath Where the serialized metadata will be saved
     * @return A new {@link UploadMetadata} object.
     * @throws IOException
     * @throws UploadFailedException
     * @throws InvalidMetadataException
     */
    public UploadMetadata CreateNewMetadata(String metadataFilePath) throws IOException, UploadFailedException, InvalidMetadataException {
        //determine segment count, segment length and Upload Id
        //create metadata
        UploadMetadata metadata = new UploadMetadata(metadataFilePath, _parameters);

        if (!_parameters.isBinary() && metadata.SegmentCount > 1) {
            this.AlignSegmentsToRecordBoundaries(metadata);
        }

        //save the initial version
        metadata.Save();

        return metadata;
    }

    /**
     * Aligns segments to match record boundaries (where a record boundary = a new line).
     * If not possible (max record size = 4MB), throws an exception.
     *
     * @param metadata The metadata to realign
     * @throws IOException
     * @throws UploadFailedException
     */
    private void AlignSegmentsToRecordBoundaries(UploadMetadata metadata) throws IOException, UploadFailedException {
        int remainingSegments = 0;

        try (RandomAccessFile stream = new RandomAccessFile(metadata.InputFilePath, "r")) {
            long offset = 0;
            for (int i = 0; i < metadata.Segments.length; i++) {
                UploadSegmentMetadata segment = metadata.Segments[i];

                //updating segment lengths means that both the offset and the length of the next segment needs to be recalculated, to keep the segment lengths somewhat balanced
                long diff = segment.Offset - offset;
                segment.Offset = offset;
                segment.Length += diff;
                if (segment.Offset >= metadata.FileLength) {
                    continue;
                }

                if (segment.SegmentNumber == metadata.Segments.length - 1) {
                    //last segment picks up the slack
                    segment.Length = metadata.FileLength - segment.Offset;
                } else {
                    //figure out how much do we need to adjust the length of the segment so it ends on a record boundary (this can be negative or positive)
                    int lengthAdjustment = DetermineLengthAdjustment(segment, stream, Charset.forName(metadata.EncodingName), metadata.Delimiter) + 1;

                    //adjust segment length and offset
                    segment.Length += lengthAdjustment;
                }
                offset += segment.Length;
                remainingSegments++;
            }
        }

        //since we adjusted the segment lengths, it's possible that the last segment(s) became of zero length; so remove it
        UploadSegmentMetadata[] segments = metadata.Segments;
        if (remainingSegments < segments.length) {
            ArrayUtils.subarray(segments, 0, remainingSegments);
            metadata.Segments = segments;
            metadata.SegmentCount = segments.length;
        }

        //NOTE: we are not validating consistency here; this method is called by CreateNewMetadata which calls Save() after this, which validates consistency anyway.
    }

    /**
     * Calculates the value by which we'd need to adjust the length of the given segment, by searching for the nearest newline around it (before and after),
     * and returning the distance to it (which can be positive, if after, or negative, if before).
     *
     * @param segment The segment to do the calculation on.
     * @param stream The full stream used to figure out the adjustment
     * @param encoding The encoding to use to determine where the cutoffs are
     * @param delimiter The delimiter that determines how we adjust. If null then '\\r', \\n' and '\\r\\n' are used.
     * @return How much to adjust the segment length by.
     * @throws UploadFailedException
     * @throws IOException
     */
    private int DetermineLengthAdjustment(UploadSegmentMetadata segment, RandomAccessFile stream, Charset encoding, String delimiter) throws UploadFailedException, IOException {
        long referenceFileOffset = segment.Offset + segment.Length;
        byte[] buffer = new byte[_maxAppendLength];

        //read 2MB before the segment boundary and 2MB after (for a total of 4MB = max append length)
        int bytesRead = ReadIntoBufferAroundReference(stream, buffer, referenceFileOffset);
        if (bytesRead > 0) {
            int middlePoint = bytesRead / 2;
            //search for newline in it
            int newLinePosBefore = StringExtensions.FindNewline(buffer, middlePoint + 1, middlePoint + 1, true, encoding, delimiter);

            //in some cases, we may have a newline that is 2 characters long, and it occurrs exactly on the midpoint, which means we won't be able to find its end.
            //see if that's the case, and then search for a new candidate before it.
            if ((delimiter == null || StringUtils.isEmpty(delimiter)) && newLinePosBefore == middlePoint + 1 && buffer[newLinePosBefore] == (byte) '\r') {
                int newNewLinePosBefore = StringExtensions.FindNewline(buffer, middlePoint, middlePoint, true, encoding, delimiter);
                if (newNewLinePosBefore >= 0) {
                    newLinePosBefore = newNewLinePosBefore;
                }
            }

            int newLinePosAfter = StringExtensions.FindNewline(buffer, middlePoint, middlePoint, false, encoding, delimiter);
            if ((delimiter == null || StringUtils.isEmpty(delimiter)) && newLinePosAfter == buffer.length - 1 && buffer[newLinePosAfter] == (byte) '\r' && newLinePosBefore >= 0) {
                newLinePosAfter = -1;
            }

            int closestNewLinePos = FindClosestToCenter(newLinePosBefore, newLinePosAfter, middlePoint);

            //middle point of the buffer corresponds to the reference file offset, so all we need to do is return the difference between the closest newline and the center of the buffer
            if (closestNewLinePos >= 0) {
                return closestNewLinePos - middlePoint;
            }
        }

        //if we get this far, we were unable to find a record boundary within our limits => fail the upload
        throw new UploadFailedException(
                MessageFormat.format(
                        "Unable to locate a record boundary within {0}MB on either side of segment {1} (offset {2}). This means the record at that offset is larger than {0}MB.",
                        _maxAppendLength / 1024 / 1024 / 2,
                        segment.SegmentNumber,
                        segment.Offset,
                        _maxAppendLength / 1024 / 1024));
    }

    /**
     * Returns the value (of the given two) that is closest in absolute terms to the center value.
     * Values that are negative are ignored (since these are assumed to represent array indices).
     *
     * @param value1 First value to compare
     * @param value2 Second value to compare
     * @param centerValue The center value they are compared against.
     * @return Either value1 or value2 depending on which is closest to the centerValue
     */
    private static int FindClosestToCenter(int value1, int value2, int centerValue) {
        if (value1 >= 0) {
            if (value2 >= 0) {
                return Math.abs(value2 - centerValue) > Math.abs(value1 - centerValue) ? value1 : value2;
            } else {
                return value1;
            }
        } else {
            return value2;
        }
    }

    /**
     * Reads data from the given file into the given buffer, centered around the given file offset. The first half of the buffer will be
     * filled with data right before the given offset, while the remainder of the buffer will contain data right after it (of course, containing the byte at the given offset).
     * @param stream The stream to read from
     * @param buffer The buffer to read data into
     * @param fileReferenceOffset The offset to start reading from in the stream.
     * @return The number of bytes reads, which could be less than the length of the input buffer if we can't read due to the beginning or the end of the file.
     * @throws IOException
     */
    private static int ReadIntoBufferAroundReference(RandomAccessFile stream, byte[] buffer, long fileReferenceOffset) throws IOException {
        int length = buffer.length;
        //calculate start offset
        long fileStartOffset = fileReferenceOffset - length / 2;

        if (fileStartOffset < 0) {
            //offset is less than zero, adjust it, as well as the length we want to read
            length += (int) fileStartOffset;
            fileStartOffset = 0;
            if (length <= 0) {
                return 0;
            }
        }

        if (fileStartOffset + length > stream.length()) {
            //startOffset + length is beyond the end of the stream, adjust the length accordingly
            length = (int) (stream.length() - fileStartOffset);
            if (length <= 0) {
                return 0;
            }
        }

        //read the appropriate block of the file into the buffer, using symmetry with respect to its midpoint
        // we always initiate a seek from the origin of the file.
        stream.seek(0);
        stream.seek(fileStartOffset);
        int bufferOffset = 0;
        while (bufferOffset < length) {
            int bytesRead = stream.read(buffer, bufferOffset, length - bufferOffset);
            bufferOffset += bytesRead;
        }
        return length;
    }
}
