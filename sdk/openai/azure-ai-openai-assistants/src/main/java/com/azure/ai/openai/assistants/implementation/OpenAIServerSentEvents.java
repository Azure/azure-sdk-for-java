package com.azure.ai.openai.assistants.implementation;

import com.azure.core.util.serializer.ObjectSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class OpenAIServerSentEvents<T> {

    private static final List<String> STREAM_COMPLETION_EVENT = List.of("data: [DONE]", "data:[DONE]");
    private static final String STREAM_EVENT_DELTA_NAME = "event:";
    private final Flux<ByteBuffer> source;
    private ByteArrayOutputStream outStream;

    public OpenAIServerSentEvents(Flux<ByteBuffer> source) {
        this.source = source;
        this.outStream = new ByteArrayOutputStream();
    }

    public Flux<T> getEvents() {
        return source
            .publishOn(Schedulers.boundedElastic())
            .concatMap(byteBuffer ->{
                List<T> values = new ArrayList<>();
                byte[] byteArray = byteBuffer.array();
                for (byte currentByte : byteArray) {
                    if (currentByte == 0xA || currentByte == 0xD) {
                        String currentLine;
                        try {
                            currentLine = outStream.toString(StandardCharsets.UTF_8.name());
                            handleCurrentLine(currentLine, values);
                        } catch (UnsupportedEncodingException | JsonProcessingException e) {
                            return Flux.error(e);
                        }
                        outStream = new ByteArrayOutputStream();
                    } else {
                        outStream.write(currentByte);
                    }
                }
                try {
                    handleCurrentLine(outStream.toString(StandardCharsets.UTF_8.name()), values);
                    outStream = new ByteArrayOutputStream();
                } catch (IllegalStateException | JsonProcessingException e) {
                    // return the values collected so far, as this could be because the server sent event is
                    // split across two byte buffers and the last line is incomplete and will be continued in
                    // the next byte buffer
                    return Flux.fromIterable(values);
                } catch (UnsupportedEncodingException e) {
                    return Flux.error(e);
                }
                return Flux.fromIterable(values);
            }).cache();
    }

    private void handleCurrentLine(String currentLine, List<T> values) throws JsonProcessingException{
        if (currentLine.isEmpty() || STREAM_COMPLETION_EVENT.contains(currentLine)) {
            return;
        }

        if (currentLine.startsWith(STREAM_EVENT_DELTA_NAME)) {
            System.out.println("Received event: " + currentLine.split(":")[1]);
            return ;
        }
        System.out.println(currentLine);
    }
}
