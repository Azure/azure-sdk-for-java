package com.azure.messaging.webpubsub.client.implementation;

import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.messaging.webpubsub.client.message.WebPubSubMessage;
import jakarta.websocket.DecodeException;
import jakarta.websocket.Decoder;
import org.glassfish.tyrus.core.coder.CoderAdapter;

import java.io.IOException;

public class MessageDecoder extends CoderAdapter implements Decoder.Text<WebPubSubMessage> {

    private final static SerializerAdapter SERIALIZER_ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();

    @Override
    public WebPubSubMessage decode(String s) throws DecodeException {
        try {
            WebPubSubMessage msg = SERIALIZER_ADAPTER.deserialize(s, WebPubSubMessage.class, SerializerEncoding.JSON);
            System.out.println("decode msg: " + s);
            return msg;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean willDecode(String s) {
        return true;
    }
}
