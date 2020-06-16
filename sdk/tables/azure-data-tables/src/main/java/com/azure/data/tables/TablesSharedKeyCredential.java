package com.azure.data.tables;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.util.CoreUtils;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.implementation.StorageImplUtils;
import com.azure.storage.common.policy.StorageSharedKeyCredentialPolicy;

import java.net.URL;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

public class TablesSharedKeyCredential {

    private static final String AUTHORIZATION_HEADER_FORMAT = "SharedKey %s:%s";
    private static final String ACCOUNT_NAME = "accountname";
    private static final String ACCOUNT_KEY = "accountkey";
    private final String accountName;
    private final String accountKey;

    public TablesSharedKeyCredential(String accountName, String accountKey) {
        Objects.requireNonNull(accountName, "'accountName' cannot be null.");
        Objects.requireNonNull(accountKey, "'accountKey' cannot be null.");
        this.accountName = accountName;
        this.accountKey = accountKey;
    }

    public static TablesSharedKeyCredential fromConnectionString(String connectionString) {
        HashMap<String, String> connectionStringPieces = new HashMap();
        String[] var2 = connectionString.split(";");
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String connectionStringPiece = var2[var4];
            String[] kvp = connectionStringPiece.split("=", 2);
            connectionStringPieces.put(kvp[0].toLowerCase(Locale.ROOT), kvp[1]);
        }

        String accountName = (String)connectionStringPieces.get("accountname");
        String accountKey = (String)connectionStringPieces.get("accountkey");
        if (!CoreUtils.isNullOrEmpty(accountName) && !CoreUtils.isNullOrEmpty(accountKey)) {
            return new TablesSharedKeyCredential(accountName, accountKey);
        } else {
            throw new IllegalArgumentException("Connection string must contain 'AccountName' and 'AccountKey'.");
        }
    }

    public String getAccountName() {
        return this.accountName;
    }

    public String generateAuthorizationHeader(URL requestURL, String httpMethod, Map<String, String> headers) {
        String signature = StorageImplUtils.computeHMac256(this.accountKey, this.buildStringToSign(requestURL, httpMethod, headers));
        return String.format("SharedKey %s:%s", this.accountName, signature);
    }

    public String computeHmac256(String stringToSign) {
        return StorageImplUtils.computeHMac256(this.accountKey, stringToSign);
    }

    private String buildStringToSign(URL requestURL, String httpMethod, Map<String, String> headers) {
        String contentLength = (String)headers.get("Content-Length");
        contentLength = contentLength.equals("0") ? "" : contentLength;
        String dateHeader = headers.containsKey("x-ms-date") ? "" : this.getStandardHeaderValue(headers, "Date");
        String s = String.join("\n",
            httpMethod, //verb
            this.getStandardHeaderValue(headers, "Content-MD5"), //content-md5
            this.getStandardHeaderValue(headers, "Content-Type"), //content-type
            dateHeader,  //date
            this.getCanonicalizedResource(requestURL)); //canonicalized resoucre
        return s;
    }

    private String getStandardHeaderValue(Map<String, String> headers, String headerName) {
        String headerValue = (String)headers.get(headerName);
//        if (headerName == "Content-Type"){
//            return "application/atom+xml";
//        }
        return headerValue == null ? "" : headerValue;
    }

    private String getAdditionalXmsHeaders(Map<String, String> headers) {
        List<String> xmsHeaderNameArray = (List)headers.entrySet().stream().filter((entry) -> {
            return ((String)entry.getKey()).toLowerCase(Locale.ROOT).startsWith("x-ms-");
        }).filter((entry) -> {
            return entry.getValue() != null;
        }).map(Map.Entry::getKey).collect(Collectors.toList());
        if (xmsHeaderNameArray.isEmpty()) {
            return "";
        } else {
            Collections.sort(xmsHeaderNameArray, Collator.getInstance(Locale.ROOT));
            StringBuilder canonicalizedHeaders = new StringBuilder();

            String key;
            for(Iterator var4 = xmsHeaderNameArray.iterator(); var4.hasNext(); canonicalizedHeaders.append(key.toLowerCase(Locale.ROOT)).append(':').append((String)headers.get(key))) {
                key = (String)var4.next();
                if (canonicalizedHeaders.length() > 0) {
                    canonicalizedHeaders.append('\n');
                }
            }

            return canonicalizedHeaders.toString();
        }
    }

    private String getCanonicalizedResource(URL requestURL) {
        StringBuilder canonicalizedResource = new StringBuilder("/");
        canonicalizedResource.append(this.accountName);
        if (requestURL.getPath().length() > 0) {
            canonicalizedResource.append(requestURL.getPath());
        } else {
            canonicalizedResource.append('/');
        }

        if (requestURL.getQuery() == null) {
            return canonicalizedResource.toString();
        } else {
            Map<String, String[]> queryParams = StorageImplUtils.parseQueryStringSplitValues(requestURL.getQuery());
            ArrayList<String> queryParamNames = new ArrayList(queryParams.keySet());
            Collections.sort(queryParamNames);
            Iterator var5 = queryParamNames.iterator();

            while(var5.hasNext()) {
                String queryParamName = (String)var5.next();
                String[] queryParamValues = (String[])queryParams.get(queryParamName);
                Arrays.sort(queryParamValues);
                String queryParamValuesStr = String.join(",", queryParamValues);
                canonicalizedResource.append("\n").append(queryParamName.toLowerCase(Locale.ROOT)).append(":").append(queryParamValuesStr);
            }

            return canonicalizedResource.toString();
        }
    }

    public static com.azure.storage.common.StorageSharedKeyCredential getSharedKeyCredentialFromPipeline(HttpPipeline httpPipeline) {
        for(int i = 0; i < httpPipeline.getPolicyCount(); ++i) {
            HttpPipelinePolicy httpPipelinePolicy = httpPipeline.getPolicy(i);
            if (httpPipelinePolicy instanceof TablesSharedKeyCredentialPolicy) {
                StorageSharedKeyCredentialPolicy storageSharedKeyCredentialPolicy = (StorageSharedKeyCredentialPolicy)httpPipelinePolicy;
                return storageSharedKeyCredentialPolicy.sharedKeyCredential();
            }
        }

        return null;
    }
}
