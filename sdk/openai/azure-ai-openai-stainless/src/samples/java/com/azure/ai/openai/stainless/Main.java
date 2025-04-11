// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.stainless;

public final class Main {
    private Main() {}

    public static void main(String[] args) throws Exception {
        CompletionsExample.main(args);
        CompletionsStreamingExample.main(args);
        AudioTranscriptionsExample.main(args);
    }
}
