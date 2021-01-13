package com.azure.cosmos.benchmark.linkedin.impl.keyextractor;

import com.azure.cosmos.implementation.InternalObjectNode;
import com.google.common.base.Preconditions;
import java.util.function.Function;
import javax.annotation.concurrent.ThreadSafe;


/**
 * Extractor for cases where the key is a String, and the id is used as the
 * partitioningKey for documents stored in CosmosDB.
 */
@ThreadSafe
public class IdentityKeyExtractor implements KeyExtractor<String> {
    private final Function<String, String> _keyEncoder;
    private final Function<String, String> _idDecoder;

    public IdentityKeyExtractor(final Function<String, String> keyEncoder, final Function<String, String> idDecoder) {
        _keyEncoder = keyEncoder;
        _idDecoder = idDecoder;
    }

    @Override
    public String getId(final String key) {
        Preconditions.checkNotNull(key, "The key can't be null");
        return _keyEncoder.apply(key);
    }

    @Override
    public String getPartitioningKey(final String key) {
        Preconditions.checkNotNull(key, "The key can't be null");
        return key;
    }

    @Override
    public String getKey(final InternalObjectNode document) {
        Preconditions.checkNotNull(document, "The Document from CosmosDB can't be null");
        return getKey(getId(document), null);
    }

    @Override
    public String getKey(final String id, final String partitioningKey) {
        Preconditions.checkNotNull(id, "The id from CosmosDB can't be null");
        return _idDecoder.apply(id);
    }
}
