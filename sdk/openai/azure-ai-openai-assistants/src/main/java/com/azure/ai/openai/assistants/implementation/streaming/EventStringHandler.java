package com.azure.ai.openai.assistants.implementation.streaming;

import com.azure.core.util.BinaryData;

import java.util.List;

public interface EventStringHandler<T>{

    void handleCurrentEvent(String currentEvent, List<T> outputValues) throws IllegalArgumentException;
}
