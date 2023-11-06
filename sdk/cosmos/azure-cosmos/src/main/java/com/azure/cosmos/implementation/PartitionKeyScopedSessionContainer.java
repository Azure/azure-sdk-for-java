package com.azure.cosmos.implementation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PartitionKeyScopedSessionContainer implements ISessionContainer {

    private SessionContainer sessionContainer;
    private ConcurrentHashMap<String, Map<String, ISessionToken>> collectionResourceIdToPartitionKeyScopedSessionTokens;

    @Override
    public String resolveGlobalSessionToken(RxDocumentServiceRequest entity) {
        return null;
    }

    @Override
    public ISessionToken resolvePartitionLocalSessionToken(RxDocumentServiceRequest entity, String partitionKeyRangeId) {
        return sessionContainer.resolvePartitionLocalSessionToken(entity, partitionKeyRangeId);
    }

    @Override
    public void clearTokenByResourceId(String resourceId) {

    }

    @Override
    public void clearTokenByCollectionFullName(String collectionFullName) {

    }

    @Override
    public void setSessionToken(RxDocumentServiceRequest request, Map<String, String> responseHeaders) {

    }

    @Override
    public void setSessionToken(String collectionRid, String collectionFullName, Map<String, String> responseHeaders) {

    }
}
