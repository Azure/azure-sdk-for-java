// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.logging;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import io.netty.handler.ssl.SslHandshakeTimeoutException;
import org.slf4j.MDC;
import reactor.util.annotation.Nullable;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.monitor.opentelemetry.exporter.implementation.utils.AzureMonitorMsgId.FRIENDLY_NETWORK_ERROR;

public class NetworkFriendlyExceptions {

    private static final List<FriendlyExceptionDetector> DETECTORS;
    private static final ClientLogger logger = new ClientLogger(NetworkFriendlyExceptions.class);

    static {
        DETECTORS = new ArrayList<>();
        // Note this order is important to determine the right exception!
        // For example SSLHandshakeException extends IOException
        DETECTORS.add(SslExceptionDetector.create());
        DETECTORS.add(UnknownHostExceptionDetector.create());
        try {
            DETECTORS.add(CipherExceptionDetector.create());
        } catch (NoSuchAlgorithmException e) {
            logger.verbose(e.getMessage(), e);
        }
    }

    // returns true if the exception was "handled" and the caller should not log it
    public static boolean logSpecialOneTimeFriendlyException(Throwable error, String url, AtomicBoolean alreadySeen,
        ClientLogger logger) {
        return logSpecialOneTimeFriendlyException(error, url, alreadySeen, logger, DETECTORS);
    }

    @SuppressWarnings("try")
    public static boolean logSpecialOneTimeFriendlyException(Throwable error, String url, AtomicBoolean alreadySeen,
        ClientLogger logger, List<FriendlyExceptionDetector> detectors) {

        for (FriendlyExceptionDetector detector : detectors) {
            if (detector.detect(error)) {
                if (!alreadySeen.getAndSet(true)) {
                    try (MDC.MDCCloseable ignored = FRIENDLY_NETWORK_ERROR.makeActive()) {
                        // using a placeholder because otherwise Azure Core ClientLogger removes newlines from
                        // the message
                        logger.error("{}", detector.message(url));
                    }
                }
                return true;
            }
        }
        return false;
    }

    private static boolean hasCausedByWithMessage(Throwable throwable, String message) {
        if (throwable.getMessage().contains(message)) {
            return true;
        }
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return false;
        }
        return hasCausedByWithMessage(cause, message);
    }

    private static boolean hasCausedByOfType(Throwable throwable, Class<?> type) {
        if (type.isInstance(throwable)) {
            return true;
        }
        Throwable cause = throwable.getCause();
        if (cause == null) {
            return false;
        }
        return hasCausedByOfType(cause, type);
    }

    private static String getFriendlyExceptionBanner(String url) {
        return "Application Insights Java failed to connect to " + url;
    }

    private static String populateFriendlyMessage(String description, String action, String banner, String note) {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append(System.lineSeparator());
        messageBuilder.append("*************************").append(System.lineSeparator());
        messageBuilder.append(banner).append(System.lineSeparator());
        messageBuilder.append("*************************").append(System.lineSeparator());
        if (!CoreUtils.isNullOrEmpty(description)) {
            messageBuilder.append(System.lineSeparator());
            messageBuilder.append("Description:").append(System.lineSeparator());
            messageBuilder.append(description).append(System.lineSeparator());
        }
        if (!CoreUtils.isNullOrEmpty(action)) {
            messageBuilder.append(System.lineSeparator());
            messageBuilder.append("Action:").append(System.lineSeparator());
            messageBuilder.append(action).append(System.lineSeparator());
        }
        if (!CoreUtils.isNullOrEmpty(note)) {
            messageBuilder.append(System.lineSeparator());
            messageBuilder.append("Note:").append(System.lineSeparator());
            messageBuilder.append(note).append(System.lineSeparator());
        }
        return messageBuilder.toString();
    }

    interface FriendlyExceptionDetector {
        boolean detect(Throwable error);

        String message(String url);
    }

    static class SslExceptionDetector implements FriendlyExceptionDetector {

        static SslExceptionDetector create() {
            return new SslExceptionDetector();
        }

        @Override
        public boolean detect(Throwable error) {
            if (error instanceof SslHandshakeTimeoutException) {
                return false;
            }
            // we are getting lots of SSLHandshakeExceptions in app services, and we suspect some may not
            // be certificate errors, so further restricting the condition to include the message
            return hasCausedByOfType(error, SSLHandshakeException.class)
                && hasCausedByWithMessage(error, "unable to find valid certification path to requested target");
        }

        @Override
        public String message(String url) {
            return populateFriendlyMessage("Unable to find valid certification path to requested target.",
                getSslFriendlyExceptionAction(url), getFriendlyExceptionBanner(url),
                "This message is only logged the first time it occurs after startup.");
        }

        private static String getJavaCacertsPath() {
            String javaHome = System.getProperty("java.home");
            return new File(javaHome, "lib/security/cacerts").getPath();
        }

        @Nullable
        private static String getCustomJavaKeystorePath() {
            String cacertsPath = System.getProperty("javax.net.ssl.trustStore");
            if (cacertsPath != null) {
                return new File(cacertsPath).getPath();
            }
            return null;
        }

        private static String getSslFriendlyExceptionAction(String url) {
            if (!url.contains("profiles")) { // ../api/profiles/../appId
                return "";
            }
            String customJavaKeyStorePath = getCustomJavaKeystorePath();
            if (customJavaKeyStorePath != null) {
                return "Please import the ROOT SSL certificate from " + getHostOnly(url)
                    + ", into your custom java key store located at:" + System.lineSeparator() + customJavaKeyStorePath
                    + System.lineSeparator()
                    + "Learn more about importing the certificate here: https://go.microsoft.com/fwlink/?linkid=2151450";
            }
            return "Please import the ROOT SSL certificate from " + getHostOnly(url)
                + ", into the default java key store located at:" + System.lineSeparator() + getJavaCacertsPath()
                + System.lineSeparator()
                + "Learn more about importing the certificate here: https://go.microsoft.com/fwlink/?linkid=2151450";
        }

        private static String getHostOnly(String url) {
            try {
                return "https://" + new URL(url).getHost();
            } catch (MalformedURLException e) {
                return url;
            }
        }
    }

    static class UnknownHostExceptionDetector implements FriendlyExceptionDetector {

        static UnknownHostExceptionDetector create() {
            return new UnknownHostExceptionDetector();
        }

        @Override
        public boolean detect(Throwable error) {
            return hasCausedByOfType(error, UnknownHostException.class);
        }

        @Override
        public String message(String url) {
            return populateFriendlyMessage("Unable to resolve host in url", getUnknownHostFriendlyExceptionAction(url),
                getFriendlyExceptionBanner(url), "This message is only logged the first time it occurs after startup.");
        }

        private static String getUnknownHostFriendlyExceptionAction(String url) {
            return "Please update your network configuration so that the host in this url can be resolved: " + url
                + System.lineSeparator()
                + "Learn more about troubleshooting unknown host exception here: https://go.microsoft.com/fwlink/?linkid=2185830";
        }
    }

    static class CipherExceptionDetector implements FriendlyExceptionDetector {

        private static final List<String> EXPECTED_CIPHERS
            = Arrays.asList("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256",
                "TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", "TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256");
        private final List<String> cipherSuitesFromJvm;

        static CipherExceptionDetector create() throws NoSuchAlgorithmException {
            SSLSocketFactory socketFactory = SSLContext.getDefault().getSocketFactory();
            return new CipherExceptionDetector(Arrays.asList(socketFactory.getSupportedCipherSuites()));
        }

        CipherExceptionDetector(List<String> cipherSuitesFromJvm) {
            this.cipherSuitesFromJvm = cipherSuitesFromJvm;
        }

        @Override
        public boolean detect(Throwable error) {
            if (!hasCausedByOfType(error, IOException.class)) {
                return false;
            }
            for (String cipher : EXPECTED_CIPHERS) {
                if (cipherSuitesFromJvm.contains(cipher)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String message(String url) {
            String description
                = "The JVM does not have any of the cipher suites which are supported by the endpoint \"" + url + "\"";
            String enableEcc = System.getProperty("com.sun.net.ssl.enableECC");
            if ("false".equalsIgnoreCase(enableEcc)) {
                return populateFriendlyMessage(
                    description + ", because the system property \"com.sun.net.ssl.enableECC\" is set" + " to \""
                        + enableEcc + "\".",
                    "Remove \"-Dcom.sun.net.ssl.enableECC=" + enableEcc + "\" from your command line.",
                    getFriendlyExceptionBanner(url),
                    "This message is only logged the first time it occurs after startup.");
            }
            return populateFriendlyMessage(description + ".", getCipherFriendlyExceptionAction(),
                getFriendlyExceptionBanner(url), "This message is only logged the first time it occurs after startup.");
        }

        private String getCipherFriendlyExceptionAction() {
            StringBuilder actionBuilder = new StringBuilder();
            actionBuilder
                .append("Investigate why the security providers in your Java distribution's"
                    + " java.security configuration file differ from a standard Java distribution.")
                .append(System.lineSeparator())
                .append(System.lineSeparator());
            for (String missingCipher : EXPECTED_CIPHERS) {
                actionBuilder.append("    ").append(missingCipher).append(System.lineSeparator());
            }
            actionBuilder.append(System.lineSeparator())
                .append("Here are the cipher suites that the JVM does have, in case this is"
                    + " helpful in identifying why the ones above are missing:")
                .append(System.lineSeparator());
            for (String foundCipher : cipherSuitesFromJvm) {
                actionBuilder.append(foundCipher).append(System.lineSeparator());
            }
            // even though we log this info at startup, this info is particularly important for this error
            // so we duplicate it here to make sure we get it as quickly and as easily as possible
            return actionBuilder.append(System.lineSeparator())
                .append("Java version:")
                .append(System.getProperty("java.version"))
                .append(", vendor: ")
                .append(System.getProperty("java.vendor"))
                .append(", home: ")
                .append(System.getProperty("java.home"))
                .append(System.lineSeparator())
                .append("Learn more about troubleshooting this network issue related to cipher suites here:"
                    + " https://go.microsoft.com/fwlink/?linkid=2185426")
                .toString();
        }
    }

    private NetworkFriendlyExceptions() {
    }
}
