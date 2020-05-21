package com.azure.search.documents.implementation.converters;

import com.azure.core.util.logging.ClientLogger;
import com.azure.search.documents.implementation.util.PrivateFieldAccessHelper;
import com.azure.search.documents.models.AnalyzedTokenInfo;

/**
 * A converter between {@link com.azure.search.documents.implementation.models.AnalyzedTokenInfo} and
 * {@link AnalyzedTokenInfo}.
 */
public final class AnalyzedTokenInfoConverter {
    private static final ClientLogger LOGGER = new ClientLogger(AnalyzedTokenInfoConverter.class);

    /**
     * Maps from {@link com.azure.search.documents.implementation.models.AnalyzedTokenInfo} to
     * {@link AnalyzedTokenInfo}.
     */
    public static AnalyzedTokenInfo map(com.azure.search.documents.implementation.models.AnalyzedTokenInfo obj) {
        if (obj == null) {
            return null;
        }
        AnalyzedTokenInfo analyzedTokenInfo = new AnalyzedTokenInfo();

        int _endOffset = obj.getEndOffset();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "endOffset", _endOffset);

        int _startOffset = obj.getStartOffset();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "startOffset", _startOffset);

        int _position = obj.getPosition();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "position", _position);

        String _token = obj.getToken();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "token", _token);
        return analyzedTokenInfo;
    }

    /**
     * Maps from {@link AnalyzedTokenInfo} to
     * {@link com.azure.search.documents.implementation.models.AnalyzedTokenInfo}.
     */
    public static com.azure.search.documents.implementation.models.AnalyzedTokenInfo map(AnalyzedTokenInfo obj) {
        if (obj == null) {
            return null;
        }
        com.azure.search.documents.implementation.models.AnalyzedTokenInfo analyzedTokenInfo =
            new com.azure.search.documents.implementation.models.AnalyzedTokenInfo();

        int _endOffset = obj.getEndOffset();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "endOffset", _endOffset);

        int _startOffset = obj.getStartOffset();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "startOffset", _startOffset);

        int _position = obj.getPosition();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "position", _position);

        String _token = obj.getToken();
        PrivateFieldAccessHelper.set(analyzedTokenInfo, "token", _token);
        return analyzedTokenInfo;
    }
}
