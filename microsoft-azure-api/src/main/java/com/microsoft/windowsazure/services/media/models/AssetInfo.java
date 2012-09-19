package com.microsoft.windowsazure.services.media.models;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.AssetType;

public class AssetInfo extends ODataEntity<AssetType> {

    public AssetInfo(EntryType entry, AssetType content) {
        super(entry, content);
    }

    /* (non-Javadoc)
     * @see com.microsoft.windowsazure.services.media.implementation.ODataEntity#getJAXBContentClass()
     */
    @Override
    public Class<?> getJAXBContentClass() {
        return AssetType.class;
    }
}
