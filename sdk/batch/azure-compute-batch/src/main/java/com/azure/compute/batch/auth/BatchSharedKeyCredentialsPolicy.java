package com.azure.compute.batch.auth;

import com.azure.core.http.*;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.Header;
import java.util.Base64;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static java.time.OffsetDateTime.now;

public final class BatchSharedKeyCredentialsPolicy implements HttpPipelinePolicy {
    private final BatchSharedKeyCredentials batchSharedKeyCred;
    private Mac hmacSha256;

    /**
     * Creates a SharedKey pipeline policy that adds the SharedKey into the request's authorization header.
     *
     * @param credential the SharedKey credential used to create the policy.
     */
    public BatchSharedKeyCredentialsPolicy(BatchSharedKeyCredentials credential) {
        this.batchSharedKeyCred = credential;
    }

    /**
     * @return the {@link BatchSharedKeyCredentials} linked to the policy.
     */

    private String headerValue(HttpRequest request, HttpHeaderName headerName) {
        HttpHeaders headers = request.getHeaders();
        Header header = headers.get(headerName);
        if (header == null) {
            return "";
        }

        return header.getValue();
    }

    private synchronized String sign(String stringToSign) {
        try {
            // Encoding the Signature
            // Signature=Base64(HMAC-SHA256(UTF8(StringToSign)))
            byte[] digest = getHmac256().doFinal(stringToSign.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalArgumentException("accessKey", e);
        }
    }

    private synchronized Mac getHmac256() throws NoSuchAlgorithmException, InvalidKeyException {
        if (this.hmacSha256 == null) {
            // Initializes the HMAC-SHA256 Mac and SecretKey.
            byte[] key = Base64.getDecoder().decode(batchSharedKeyCred.keyValue());
            this.hmacSha256 = Mac.getInstance("HmacSHA256");
            this.hmacSha256.init(new SecretKeySpec(key, "HmacSHA256"));
        }
        return this.hmacSha256;
    }

    public String signHeader(HttpRequest request) throws IOException {

        // Set Headers
        String dateHeaderToSign = headerValue(request, HttpHeaderName.DATE);
        HttpHeaderName ocpDateHeader = HttpHeaderName.fromString("ocp-date");
        if (request.getHeaders().get(ocpDateHeader) == null) {
            if (dateHeaderToSign == null) {
                DateTimeRfc1123 rfcDate = new DateTimeRfc1123(now());
                request.setHeader(ocpDateHeader, rfcDate.toString());
                dateHeaderToSign = "";      //Cannot append both ocp-date and date header values
            }
        }
        else {
            dateHeaderToSign = "";      //Cannot append both ocp-date and date header values
        }

        StringBuffer signature = new StringBuffer(request.getHttpMethod().toString());
        signature.append("\n");
        signature.append(headerValue(request, HttpHeaderName.CONTENT_ENCODING)).append("\n");
        signature.append(headerValue(request, HttpHeaderName.CONTENT_LANGUAGE)).append("\n");

        // Special handle content length
        String contentLength = headerValue(request, HttpHeaderName.CONTENT_LENGTH);

        signature.append((contentLength == null || Long.parseLong(contentLength) < 0  ? "" : contentLength)).append("\n");

        signature.append(headerValue(request, HttpHeaderName.CONTENT_MD5)).append("\n");

        String contentType = headerValue(request, HttpHeaderName.CONTENT_TYPE);
        signature.append(contentType).append("\n");

        signature.append(dateHeaderToSign).append("\n");
        signature.append(headerValue(request, HttpHeaderName.IF_MODIFIED_SINCE)).append("\n");
        signature.append(headerValue(request, HttpHeaderName.IF_MATCH)).append("\n");
        signature.append(headerValue(request, HttpHeaderName.IF_NONE_MATCH)).append("\n");
        signature.append(headerValue(request, HttpHeaderName.IF_UNMODIFIED_SINCE)).append("\n");
        signature.append(headerValue(request, HttpHeaderName.RANGE)).append("\n");

        ArrayList<String> customHeaders = new ArrayList<>();
        for (HttpHeader name : request.getHeaders()) {
            if (name.getName().toLowerCase().startsWith("ocp-")) {
                customHeaders.add(name.getName().toLowerCase());
            }
        }

        Collections.sort(customHeaders);
        for (String canonicalHeader : customHeaders) {
            String value = request.getHeaders().getValue(HttpHeaderName.fromString(canonicalHeader));
            value = value.replace('\n', ' ').replace('\r', ' ')
                    .replaceAll("^[ ]+", "");
            signature.append(canonicalHeader).append(":").append(value).append("\n");
        }

        signature.append("/")
                .append(batchSharedKeyCred.accountName().toLowerCase()).append("/")
                .append(request.getUrl().getPath().replaceAll("^[/]+", ""));

        String query = request.getUrl().getQuery();

        if (query != null) {
            Map<String, String> queryComponents = new TreeMap<>();
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf("=");
                String key = URLDecoder.decode(pair.substring(0, idx), "UTF-8")
                        .toLowerCase(Locale.US);
                queryComponents.put(
                        key,
                        key + ":" + URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
            }

            for (Map.Entry<String, String> entry : queryComponents.entrySet()) {
                signature.append("\n").append(entry.getValue());
            }
        }

        String signedSignature = sign(signature.toString());
        String authorization = "SharedKey " + batchSharedKeyCred.accountName()
                + ":" + signedSignature;

        return authorization;
    }

    @Override
    public Mono<HttpResponse> process(HttpPipelineCallContext context, HttpPipelineNextPolicy next) {
        try {
            String authorizationValue = this.signHeader(context.getHttpRequest());
            context.getHttpRequest().setHeader(HttpHeaderName.fromString("Authorization"), authorizationValue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return next.process();
    }
}
