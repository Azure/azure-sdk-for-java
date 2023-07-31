package com.azure.identity;

import com.azure.core.util.CoreUtils;
import com.microsoft.aad.msal4j.SystemBrowserOptions;

public class BrowserCustomizationOptions {
    private String htmlMessageSuccess;
    private String htmlMessageError;

    public BrowserCustomizationOptions setHtmlMessageSuccess(String htmlMessageSuccess) {
        this.htmlMessageSuccess = htmlMessageSuccess;
        return this;
    }

    public BrowserCustomizationOptions setHtmlMessageError(String htmlMessageError) {
        this.htmlMessageError = htmlMessageError;
        return this;
    }

    public String getHtmlMessageSuccess() {
        return this.htmlMessageSuccess;
    }

    public String getHtmlMessageError() {
        return this.htmlMessageError;
    }
}
