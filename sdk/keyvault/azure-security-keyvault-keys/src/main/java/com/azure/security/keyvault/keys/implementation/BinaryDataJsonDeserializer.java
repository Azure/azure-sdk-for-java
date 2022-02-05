package com.azure.security.keyvault.keys.implementation;

import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class BinaryDataJsonDeserializer  extends JsonDeserializer<BinaryData> {
    @Override
    public BinaryData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException {

        String text = jsonParser.getText();

        if (text != null) {
            return BinaryData.fromString(text);
        }

        return null;
    }
}
