// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.vision.face.tests.commands;

import java.util.function.BiFunction;

public class CommandProvider<TFaceClient, TFaceAsyncClient, TCommand> implements BiFunction<TFaceClient, TFaceAsyncClient, TCommand> {
    private final BiFunction<TFaceClient, TFaceAsyncClient, TCommand> creator;
    private final String tag;

    public CommandProvider(String tag, BiFunction<TFaceClient, TFaceAsyncClient, TCommand> creator) {
        this.tag = tag;
        this.creator = creator;
    }

    @Override
    public TCommand apply(TFaceClient faceClient, TFaceAsyncClient faceAsyncClient) {
        return this.creator.apply(faceClient, faceAsyncClient);
    }

    public String getTag() {
        return this.tag;
    }
}
