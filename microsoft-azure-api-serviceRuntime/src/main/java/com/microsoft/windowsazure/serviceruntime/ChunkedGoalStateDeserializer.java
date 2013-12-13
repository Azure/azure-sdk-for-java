/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
