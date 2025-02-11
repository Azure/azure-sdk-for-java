package com.azure.storage.file.share.implementation.accesshelpers;

import com.azure.storage.file.share.FileSmbProperties;
import com.azure.storage.file.share.models.FilePosixProperties;
import com.azure.storage.file.share.models.ShareFileInfo;
import com.azure.storage.file.share.models.ShareFileSymbolicLinkInfo;

import java.time.OffsetDateTime;

public class ShareFileSymbolicLinkInfoHelper {
    private static ShareFileSymbolicLinkInfoAccessor accessor;

    private ShareFileSymbolicLinkInfoHelper() { }

    /**
     * Type defining the methods to set the non-public properties of an {@link ShareFileSymbolicLinkInfo} instance.
     */
    public interface ShareFileSymbolicLinkInfoAccessor {
        ShareFileSymbolicLinkInfo create(String eTag, OffsetDateTime lastModified, String linkText);
    }

    /**
     * The method called from {@link ShareFileSymbolicLinkInfo} to set its accessor.
     *
     * @param shareFileSymbolicLinkInfoAccessor The accessor.
     */
    public static void setAccessor(final ShareFileSymbolicLinkInfoAccessor shareFileSymbolicLinkInfoAccessor) {
        ShareFileSymbolicLinkInfoHelper.accessor = shareFileSymbolicLinkInfoAccessor;
    }

    public static ShareFileSymbolicLinkInfo create(String eTag, OffsetDateTime lastModified, String linkText) {
        // This looks odd but is necessary, it is possible to engage the access helper before anywhere else in the
        // application accesses ShareFileInfo which triggers the accessor to be configured. So, if the accessor
        // is null this effectively pokes the class to set up the accessor.
        if (accessor == null) {
            new ShareFileSymbolicLinkInfo(null, null, null);
        }

        assert accessor != null;
        return accessor.create(eTag, lastModified, linkText);
    }
}
