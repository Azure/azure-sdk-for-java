// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.streaming;

import com.azure.communication.callautomation.implementation.converters.AudioDataConverter;
import com.azure.communication.callautomation.implementation.converters.AudioMetadataConverter;
import com.azure.communication.callautomation.implementation.converters.TranscriptionDataConverter;
import com.azure.communication.callautomation.implementation.converters.TranscriptionMetadataConverter;
import com.azure.communication.callautomation.models.streaming.media.AudioData;
import com.azure.communication.callautomation.models.streaming.media.AudioMetadata;
import com.azure.communication.callautomation.models.streaming.transcription.TranscriptionData;
import com.azure.communication.callautomation.models.streaming.transcription.TranscriptionMetadata;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * A generic parser for different packages, such as Media(Audio) or Transcription, received as
 * part of streaming over websocket
 */
public final class StreamingDataParser {
    private static final ClientLogger LOGGER = new ClientLogger(StreamingDataParser.class);

    /***
     * Parses StreamingData such as Audio, Transcription or Captions from BinaryData.
     *
     * @param json The MediaStreaming package as a BinaryData object.
     * @throws RuntimeException Any exceptions occur at runtime.
     * @return a MediaStreamingPackageBase object.
     */
    public static StreamingData parse(BinaryData json) {
        return parse(json.toString());
    }

    /***
     * Parses a StreamingData such as Audio, Transcription or Captions from byte array.
     *
     * @param receivedBytes The MediaStreaming package as a byte[].
     * @throws RuntimeException Any exceptions occur at runtime.
     * @return a MediaStreamingPackageBase object.
     */
    public static StreamingData parse(byte[] receivedBytes) {
        return parse(new String(receivedBytes, StandardCharsets.UTF_8));
    }

    /***
     * Parses a StreamingData such as Audio, Transcription or Captions from String.
     *
     * @param stringJson The MediaStreaming package as a String.
     * @throws RuntimeException Any exceptions occur at runtime.
     * @return a MediaStreamingPackageBase object.
     */
    public static StreamingData parse(String stringJson) {
        //region Audio
        if (stringJson.contains("AudioData")) {
            try (JsonReader jsonReader = JsonProviders.createReader(stringJson)) {
                final AudioDataConverter audioInternal = jsonReader.readObject(reader -> {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        String fieldName = reader.getFieldName();
                        reader.nextToken();
                        if ("audioData".equals(fieldName)) {
                            return AudioDataConverter.fromJson(reader);
                        } else {
                            reader.skipChildren();
                        }
                    }
                    return null;
                });
                if (audioInternal != null) {
                    return new AudioData(audioInternal.getData(), audioInternal.getTimestamp(), audioInternal.getParticipantRawID(), audioInternal.isSilent());
                } else {
                    return null;
                }
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        if (stringJson.contains("AudioMetadata")) {
            try (JsonReader jsonReader = JsonProviders.createReader(stringJson)) {
                final AudioMetadataConverter metadataInternal = jsonReader.readObject(reader -> {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        String fieldName = reader.getFieldName();
                        reader.nextToken();
                        if ("audioMetadata".equals(fieldName)) {
                            return AudioMetadataConverter.fromJson(reader);
                        } else {
                            reader.skipChildren();
                        }
                    }
                    return null;
                });
                if (metadataInternal != null) {
                    return new AudioMetadata(metadataInternal.getMediaSubscriptionId(), metadataInternal.getEncoding(), metadataInternal.getSampleRate(), metadataInternal.getChannels(), metadataInternal.getLength());
                } else {
                    return null;
                }
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        //endregion

        //region Transcription
        if (stringJson.contains("TranscriptionData")) {
            try (JsonReader jsonReader = JsonProviders.createReader(stringJson)) {
                final TranscriptionDataConverter transcriptionInternal = jsonReader.readObject(reader -> {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        String fieldName = reader.getFieldName();
                        reader.nextToken();
                        if ("transcriptionData".equals(fieldName)) {
                            return TranscriptionDataConverter.fromJson(reader);
                        } else {
                            reader.skipChildren();
                        }
                    }
                    return null;
                });
                if (transcriptionInternal != null) {
                    return new TranscriptionData(transcriptionInternal.getText(), transcriptionInternal.getFormat(), transcriptionInternal.getConfidence(), transcriptionInternal.getOffset(), transcriptionInternal.getDuration(), transcriptionInternal.getWords(), transcriptionInternal.getParticipantRawID(), transcriptionInternal.getResultStatus());
                } else {
                    return null;
                }
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }

        if (stringJson.contains("TranscriptionMetadata")) {
            try (JsonReader jsonReader = JsonProviders.createReader(stringJson)) {
                final TranscriptionMetadataConverter transcriptionMetadataInternal = jsonReader.readObject(reader -> {
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        String fieldName = reader.getFieldName();
                        reader.nextToken();
                        if ("transcriptionMetadata".equals(fieldName)) {
                            return TranscriptionMetadataConverter.fromJson(reader);
                        } else {
                            reader.skipChildren();
                        }
                    }
                    return null;
                });
                if (transcriptionMetadataInternal != null) {
                    return new TranscriptionMetadata(transcriptionMetadataInternal.getTranscriptionSubscriptionId(), transcriptionMetadataInternal.getLocale(), transcriptionMetadataInternal.getCallConnectionId(), transcriptionMetadataInternal.getCorrelationId());
                } else {
                    return null;
                }
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new RuntimeException(e));
            }
        }
        //endregion

        return null;
    }
}
