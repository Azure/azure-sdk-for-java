// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.broker;

import com.azure.identity.InteractiveBrowserCredential;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import javafx.stage.Stage;

public class JavaDocCodeSnippets {
    public void interactiveBrowserBrokerCredentialBuilderSnippet() {
        // BEGIN: com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.construct
        InteractiveBrowserBrokerCredentialBuilder builder = new InteractiveBrowserBrokerCredentialBuilder();
        InteractiveBrowserCredential credential = builder.build();
        // END: com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.construct
    }


    // BEGIN: com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.getwindowhandle.javafx
    public long getWindowHandle(Stage stage) {
        try {
            WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, stage.getTitle());
            return Pointer.nativeValue(hwnd.getPointer());
        } catch (Exception e) {
            // Handle exceptions in an appropriate manner for your application.
            // Not being able to retrieve a window handle for Windows is a fatal error.
            throw e;
        }
    }
    // END: com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.getwindowhandle.javafx

    public void configureCredentialForWindows() {
        // BEGIN: com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.useinteractivebrowserbroker.windows
        long windowHandle = getWindowHandle(); // Samples below
        InteractiveBrowserCredential cred = new InteractiveBrowserBrokerCredentialBuilder()
            .setWindowHandle(windowHandle)
            .build();
        // END: com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.useinteractivebrowserbroker.windows
    }

    public void configureCredentialForDefaultAccount() {
        // BEGIN: com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.useinteractivebrowserbroker.defaultaccount
        long windowHandle = getWindowHandle(); // Samples below
        InteractiveBrowserCredential cred = new InteractiveBrowserBrokerCredentialBuilder()
            .setWindowHandle(windowHandle)
            .useDefaultBrokerAccount()
            .build();
        // END: com.azure.identity.broker.interactivebrowserbrokercredentialbuilder.useinteractivebrowserbroker.defaultaccount
    }

    private long getWindowHandle() {
        return 0;
    }
}
