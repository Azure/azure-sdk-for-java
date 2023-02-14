package com.azure.storage.file.share.options;

import com.azure.storage.common.StorageChannelMode;
import com.azure.storage.file.share.models.FileLastWrittenMode;
import com.azure.storage.file.share.models.ShareRequestConditions;

import java.util.Objects;

public class ShareFileSeekableByteChannelOptions {
    private final StorageChannelMode channelMode;
    private ShareRequestConditions requestConditions;
    private FileLastWrittenMode fileLastWrittenMode;

    public ShareFileSeekableByteChannelOptions(StorageChannelMode mode) {
        channelMode = Objects.requireNonNull(mode, "'mode' cannot be null.");
    }

    public StorageChannelMode getChannelMode() {
        return channelMode;
    }

    public ShareRequestConditions getRequestConditions() {
        return requestConditions;
    }

    public ShareFileSeekableByteChannelOptions setRequestConditions(ShareRequestConditions requestConditions) {
        this.requestConditions = requestConditions;
        return this;
    }

    public FileLastWrittenMode getFileLastWrittenMode() {
        return fileLastWrittenMode;
    }

    public ShareFileSeekableByteChannelOptions setFileLastWrittenMode(FileLastWrittenMode fileLastWrittenMode) {
        if (channelMode != StorageChannelMode.WRITE) {
            throw new IllegalArgumentException("'fileLastWrittenMode' not allowed for channel mode "
                + channelMode.toString());
        }
        this.fileLastWrittenMode = fileLastWrittenMode;
        return this;
    }
}
