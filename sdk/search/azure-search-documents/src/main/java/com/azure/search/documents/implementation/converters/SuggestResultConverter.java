package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.SuggestResult;

import java.util.Map;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.SuggestResult} and {@link SuggestResult}.
 */
public final class SuggestResultConverter {
    private static final ClientLogger LOGGER = new ClientLogger(SuggestResultConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.SuggestResult} to {@link SuggestResult}.
     */
    public static SuggestResult map(com.azure.search.documents.implementation.models.SuggestResult obj) {
        if (obj == null) {
            return null;
        }
        SuggestResult suggestResult = new SuggestResult();

        SearchDocument _additionalProperties = new SearchDocument(obj.getAdditionalProperties());
        PrivateFieldAccessHelper.set(suggestResult, "additionalProperties", _additionalProperties);

        String _text = obj.getText();
        PrivateFieldAccessHelper.set(suggestResult, "text", _text);
        return suggestResult;
    }

    /**
     * Maps from {@link SuggestResult} to {@link com.azure.search.documents.implementation.models.SuggestResult}.
     */
    public static com.azure.search.documents.implementation.models.SuggestResult map(SuggestResult obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.SuggestResult suggestResult =
            new com.azure.search.documents.implementation.models.SuggestResult();

        Map<String, Object> _additionalProperties = obj.getDocument();
        PrivateFieldAccessHelper.set(suggestResult, "additionalProperties", _additionalProperties);

        String _text = obj.getText();
        PrivateFieldAccessHelper.set(suggestResult, "text", _text);
        return suggestResult;
    }
}
