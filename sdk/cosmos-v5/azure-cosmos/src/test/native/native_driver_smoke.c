// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

#include "com_azure_v2_cosmos_implementation_NativeDriverSmoke.h"
#include "azurecosmosdriver.h"
#include <stdio.h>
#include <string.h>

static void fail(JNIEnv *env, const char *message) {
    jclass type = (*env)->FindClass(env, "java/lang/IllegalStateException");
    if (type != NULL) {
        (*env)->ThrowNew(env, type, message);
    }
}

JNIEXPORT void JNICALL Java_com_azure_v2_cosmos_implementation_NativeDriverSmoke_verifyAndCreate(
    JNIEnv *env, jclass type, jstring expected_version) {
    (void)type;
    const char *expected = (*env)->GetStringUTFChars(env, expected_version, NULL);
    if (expected == NULL) {
        return; // GetStringUTFChars has already raised an exception.
    }
    const char *linked = cosmos_version();
    int matches = linked != NULL
        && strcmp(expected, AZURECOSMOSDRIVER_H_VERSION) == 0
        && strcmp(expected, linked) == 0;
    (*env)->ReleaseStringUTFChars(env, expected_version, expected);
    if (!matches) {
        fail(env, "Cosmos native header/library/pin version mismatch");
        return;
    }

    // Do not use ABI structs until the header, linked library and Java pin agree.
    cosmos_runtime_options_t options = cosmos_runtime_options_default();
    cosmos_runtime_t *runtime = NULL;
    cosmos_status_code_t status = cosmos_runtime_build(&options, &runtime, NULL);
    if (status != COSMOS_STATUS_SUCCESS) {
        char message[96];
        snprintf(message, sizeof(message), "Cosmos runtime construction failed: status=%d", (int)status);
        fail(env, message);
        return;
    }
    if (runtime == NULL) {
        fail(env, "Cosmos runtime construction returned a null handle");
        return;
    }
    cosmos_runtime_free(runtime);
}
