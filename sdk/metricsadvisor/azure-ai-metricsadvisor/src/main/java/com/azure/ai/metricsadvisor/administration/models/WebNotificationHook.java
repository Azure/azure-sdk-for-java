// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.http.HttpHeaders;

import java.util.List;

/**
 * A hook that describes web-hook based incident alerts notification.
 */
@Fluent
public final class WebNotificationHook extends NotificationHook {
    private String name;
    private String description;
    private String endpoint;
    private String externalLink;
    private String username;
    private String userPassword;
    private String clientCertificate;
    private String clientCertificatePassword;
    private HttpHeaders httpHeaders;

    /**
     * Create a new instance of WebNotificationHook.
     *
     * @param name The web hook name.
     * @param endpoint The web endpoint to notify incident alerts.
     */
    public WebNotificationHook(String name, String endpoint) {
        this.name = name;
        this.endpoint = endpoint;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
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
     * Gets the external link url, this will be included in the notification sent to the users,
     * this is usually a reference to any troubleshooting guide.
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
        if (this.httpHeaders != null) {
            return new HttpHeaders(this.httpHeaders.toMap());
        } else {
            return new HttpHeaders();
        }
    }

    /**
     * The web endpoint to notify incident alerts.
     *
     * @param endpoint The endpoint
     * @return The WebNotificationHook object itself.
     */
    public WebNotificationHook setEndPoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    /**
     * Sets web hook name.
     *
     * @param name The web hook name.
     * @return The WebNotificationHook object itself.
     */
    public WebNotificationHook setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets web hook description.
     *
     * @param description The web hook description.
     * @return The WebNotificationHook object itself.
     */
    public WebNotificationHook setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the customized external link, it will be included in the notification sent to the users,
     * this is usually a reference to any troubleshooting guide.
     *
     * @param externalLink The customized link.
     * @return The WebNotificationHook object itself.
     */
    public WebNotificationHook setExternalLink(String externalLink) {
        this.externalLink = externalLink;
        return this;
    }

    /**
     * Sets the credential to access the endpoint.
     *
     * @param username The user name.
     * @param password The password.
     * @return The WebNotificationHook object itself.
     */
    public WebNotificationHook setUserCredentials(String username, String password) {
        this.username = username;
        this.userPassword = password;
        return this;
    }

    /**
     * Sets the HTTPS certificate to use to access the endpoint.
     *
     * @param certificate The certificate.
     * @param password The certificate password.
     * @return The WebNotificationHook object itself.
     */
    public WebNotificationHook setClientCertificate(String certificate, String password) {
        this.clientCertificate = certificate;
        this.clientCertificatePassword = password;
        return this;
    }

    /**
     * Sets the HTTP headers to send while sending alert to the endpoint.
     *
     * @param httpHeaders The HTTP headers.
     * @return The WebNotificationHook object itself.
     */
    public WebNotificationHook setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
        return this;
    }

    /**
     * Sets the user e-mails and clientIds with administrative rights to manage the hook.
     * <p>
     * The administrators have total control over the hook, being allowed to update or delete the hook.
     * Each element in this list represents a user with administrator access, but the value of each string element
     * as either user email address or clientId uniquely identifying the user service principal.
     *
     * @param admins A list containing email or clientId of admins
     * @return The WebNotificationHook object itself.
     */
    public WebNotificationHook setAdmins(List<String> admins) {
        super.setAdministrators(admins);
        return this;
    }

    HttpHeaders getHttpHeadersRaw() {
        // package private method that won't translate null headers to empty-headers.
        return this.httpHeaders;
    }
}
