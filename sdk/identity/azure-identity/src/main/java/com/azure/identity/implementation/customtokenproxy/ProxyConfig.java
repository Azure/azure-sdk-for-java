package com.azure.identity.implementation.customtokenproxy;

import java.net.URL;

public class ProxyConfig {
    private final URL tokenProxyUrl;
    private final String sniName;
    private final String caFile;
    private final byte[] caData;

    public ProxyConfig(URL tokenProxyUrl, String sniName, String caFile, byte[] caData) {
        this.tokenProxyUrl = tokenProxyUrl;
        this.sniName = sniName;
        this.caFile = caFile;
        this.caData = caData;
    }

    public URL getTokenProxyUrl() {
        return tokenProxyUrl;
    }

    public String getSniName() {
        return sniName;
    }

    public String getCaFile() {
        return caFile;
    }

    public byte[] getCaData() {
        return caData;
    }
}
