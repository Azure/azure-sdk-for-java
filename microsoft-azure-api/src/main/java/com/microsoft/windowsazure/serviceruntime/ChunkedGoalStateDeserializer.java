package com.microsoft.windowsazure.serviceruntime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

class ChunkedGoalStateDeserializer implements GoalStateDeserializer {
    private final XmlGoalStateDeserializer deserializer;
    private BufferedReader reader;

    public ChunkedGoalStateDeserializer() {
        this.deserializer = new XmlGoalStateDeserializer();
    }

    @Override
    public void initialize(InputStream inputStream) {
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public GoalState deserialize() {
        try {
            String lengthString = reader.readLine();

            if (lengthString == null) {
                return null;
            }

            int length = Integer.parseInt(lengthString.toString(), 16);
            char chunkData[] = new char[length];

            reader.read(chunkData, 0, length);

            GoalState goalState = deserializer.deserialize(new String(chunkData));

            reader.readLine();

            return goalState;
        }
        catch (IOException e) {
            e.printStackTrace();

            return null;
        }
    }
}
