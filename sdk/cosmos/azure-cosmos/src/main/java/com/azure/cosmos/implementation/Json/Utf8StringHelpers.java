package com.azure.cosmos.implementation.Json;
import java.nio.charset.StandardCharsets;
import java.nio.ByteBuffer;

public class Utf8StringHelpers
{
    public static String toString(ByteBuffer buffer)
    {
        String jsonText;
        if(buffer.hasArray())
        {
            jsonText = new String(buffer.array(), buffer.arrayOffset() + buffer.position(), buffer.remaining(), StandardCharsets.UTF_8);
        }
        else
        {
            byte[] result = new byte[buffer.remaining()];
            buffer.get(result);
            jsonText = new String(result, StandardCharsets.UTF_8);
        }

        return jsonText;
    }

}
