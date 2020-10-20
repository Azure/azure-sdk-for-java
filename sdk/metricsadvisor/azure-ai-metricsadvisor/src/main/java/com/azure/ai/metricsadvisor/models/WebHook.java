// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.http.HttpHeaders;

import java.util.List;

/**
 * A hook that describes web-hook based incident alerts notification.
 */
public final class WebHook extends Hook {
    private String id;
    private String name;
    private String description;
    private List<String> admins;
    private String endpoint;
    private String externalLink;
    private String username;
    private String userPassword;
    private String clientCertificate;
    private String clientCertificatePassword;
    private HttpHeaders httpHeaders;

    /**
     * Create a new instance of WebHook.
     *
     * @param name The web hook name.
     * @param endpoint The web endpoint to notify incident alerts.
     */
    public WebHook(String name, String endpoint) {
        this.name = name;
        this.endpoint = endpoint;
        this.httpHeaders = new HttpHeaders();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public List<String> getAdmins() {
        return this.admins;
    }

    /**
     * Gets the web endpoint that receives incident alerts.
     *
     * @return The endpoint.
     */
    public String getEndpoint() {
        return this.endpoint;
    }

    /**
     * Gets the external link.
     *
     * @return The external link.
     */
    public String getExternalLink() {
        return this.externalLink;
    }

    /**
     * Gets the user name to authenticate the endpoint.
     *
     * @return The user name.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Gets the password to authenticate the endpoint.
     *
     * @return The password.
     */
    public String getPassword() {
        return this.userPassword;
    }

    /**
     * The HTTPS client certificate to access the endpoint.
     *
     * @return The client certificate.
     */
    public String getClientCertificate() {
        return this.clientCertificate;
    }

    /**
     * The password of the HTTPS client certificate to access the endpoint.
     *
     * @return The client certificate password
     */
    public String getClientCertificatePassword() {
        return this.clientCertificatePassword;
    }

    /**
     * Gets the HTTP headers to send while sending alert to the endpoint.
     *
     * @return The HTTP headers.
     */
    public HttpHeaders getHttpHeaders() {
        return new HttpHeaders(this.httpHeaders.toMap());
    }

    /**
     * The web endpoint to notify incident alerts.
     *
     * @param endpoint The endpoint
     * @return The WebHook object itself.
     */
    public WebHook setEndPoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets web hook name.
     *
     * @param name The web hook name.
     * @return The WebHook object itself.
     */
    public WebHook setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets web hook description.
     *
     * @param description The web hook description.
     * @return The WebHook object itself.
     */
    public WebHook setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the customized external link.
     *
     * @param externalLink The customized link.
     * @return The WebHook object itself.
     */
    public WebHook setExternalLink(String externalLink) {
        this.externalLink = externalLink;
        return this;
    }

    /**
     * Sets the credential to access the endpoint.
     *
     * @param username The user name.
     * @param password The password.
     * @return The WebHook object itself.
     */
    public WebHook setUserCredentials(String username, String password) {
        this.username = username;
        this.userPassword = password;
        return this;
    }

    /**
     * Sets the HTTPS certificate to use to access the endpoint.
     *
     * @param certificate The certificate.
     * @param password The certificate password.
     * @return The WebHook object itself.
     */
    public WebHook setClientCertificate(String certificate, String password) {
        this.clientCertificate = certificate;
        this.clientCertificatePassword = password;
        return this;
    }

    /**
     * Sets the HTTP headers to send while sending alert to the endpoint.
     *
     * @param httpHeaders The HTTP headers.
     * @return The WebHook object itself.
     */
    public WebHook setHttpHeaders(HttpHeaders httpHeaders) {
        if (httpHeaders == null) {
            this.httpHeaders = new HttpHeaders();
            return this;
        }
        this.httpHeaders = httpHeaders;
        return this;
    }
}
