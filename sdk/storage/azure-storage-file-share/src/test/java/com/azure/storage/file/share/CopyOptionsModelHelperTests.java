// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.storage.file.share.implementation.models.CopyFileSmbInfo;
import com.azure.storage.file.share.implementation.util.ModelHelper;
import com.azure.storage.file.share.models.CopyableFileSmbPropertiesList;
import com.azure.storage.file.share.models.NtfsFileAttributes;
import com.azure.storage.file.share.models.PermissionCopyModeType;
import com.azure.storage.file.share.options.ShareFileCopyOptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the file copy option validation and SMB information shared by {@link ShareFileClient#beginCopy} and
 * {@link ShareFileAsyncClient#beginCopy}.
 */
public class CopyOptionsModelHelperTests {
    private static final OffsetDateTime TIME = OffsetDateTime.parse("2026-01-02T03:04:05.1234567Z");
    private static final String SMB_TIME = "2026-01-02T03:04:05.1234567Z";

    @ParameterizedTest
    @MethodSource("permissionCopyModeSupplier")
    public void validateCopyOptionsPermission(PermissionCopyModeType mode, boolean setPermission, boolean setKey,
        boolean rejected) {
        ShareFileCopyOptions options = permissionOptions(mode, setPermission, setKey);

        if (rejected) {
            assertThrows(IllegalArgumentException.class,
                () -> ModelHelper.validateCopyOptions(options, options.getSmbProperties()));
        } else {
            assertDoesNotThrow(() -> ModelHelper.validateCopyOptions(options, options.getSmbProperties()));
        }
    }

    /**
     * With {@link PermissionCopyModeType#SOURCE} or no copy mode, neither a permission nor a permission key may be set.
     * With {@link PermissionCopyModeType#OVERRIDE}, they're mutually exclusive.
     *
     * @return Arguments of copy mode, whether a permission is set, whether a permission key is set, and whether the
     * options are rejected.
     */
    static Stream<Arguments> permissionCopyModeSupplier() {
        List<Arguments> arguments = new ArrayList<>();
        List<PermissionCopyModeType> modes = new ArrayList<>(EnumSet.allOf(PermissionCopyModeType.class));
        modes.add(null);
        for (PermissionCopyModeType mode : modes) {
            for (boolean setPermission : new boolean[] { false, true }) {
                for (boolean setKey : new boolean[] { false, true }) {
                    boolean rejected
                        = mode == PermissionCopyModeType.OVERRIDE ? setPermission && setKey : setPermission || setKey;
                    arguments.add(Arguments.of(mode, setPermission, setKey, rejected));
                }
            }
        }
        return arguments.stream();
    }

    static ShareFileCopyOptions permissionOptions(PermissionCopyModeType mode, boolean setPermission, boolean setKey) {
        return new ShareFileCopyOptions().setPermissionCopyModeType(mode)
            .setFilePermission(setPermission ? "permission" : null)
            .setSmbProperties(new FileSmbProperties().setFilePermissionKey(setKey ? "permission-key" : null));
    }

    @Test
    public void validateCopyOptionsPermissionTooLarge() {
        ShareFileCopyOptions options
            = new ShareFileCopyOptions().setPermissionCopyModeType(PermissionCopyModeType.OVERRIDE)
                .setFilePermission(new String(new char[8 * 1024 + 1]).replace('\0', 'a'));

        assertThrows(IllegalArgumentException.class,
            () -> ModelHelper.validateCopyOptions(options, new FileSmbProperties()));
    }

    @ParameterizedTest
    @MethodSource("copyFlagConflictSupplier")
    public void validateCopyOptionsCopyFlagConflict(
        Function<CopyableFileSmbPropertiesList, CopyableFileSmbPropertiesList> flag,
        Function<FileSmbProperties, FileSmbProperties> property, String message) {
        FileSmbProperties smbProperties = property.apply(new FileSmbProperties());
        ShareFileCopyOptions options = new ShareFileCopyOptions().setSmbProperties(smbProperties)
            .setSmbPropertiesToCopy(flag.apply(new CopyableFileSmbPropertiesList()));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> ModelHelper.validateCopyOptions(options, smbProperties));
        assertTrue(exception.getMessage().contains(message), exception.getMessage());
    }

    static Stream<Arguments> copyFlagConflictSupplier() {
        return Stream.of(Arguments.of(flag(list -> list.setFileAttributes(true)),
            property(p -> p.setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY))), "isSetFileAttributes"),
            Arguments.of(flag(list -> list.setCreatedOn(true)), property(p -> p.setFileCreationTime(TIME)),
                "isSetCreatedOn"),
            Arguments.of(flag(list -> list.setLastWrittenOn(true)), property(p -> p.setFileLastWriteTime(TIME)),
                "isSetLastWrittenOn"),
            Arguments.of(flag(list -> list.setChangedOn(true)), property(p -> p.setFileChangeTime(TIME)),
                "isSetChangedOn"));
    }

    @Test
    public void createCopyFileSmbInfoSetsProperties() {
        FileSmbProperties smbProperties = new FileSmbProperties()
            .setNtfsFileAttributes(EnumSet.of(NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.ARCHIVE))
            .setFileCreationTime(TIME)
            .setFileLastWriteTime(TIME)
            .setFileChangeTime(TIME);
        ShareFileCopyOptions options
            = new ShareFileCopyOptions().setPermissionCopyModeType(PermissionCopyModeType.SOURCE)
                .setIgnoreReadOnly(true)
                .setArchiveAttribute(false);

        CopyFileSmbInfo smbInfo = ModelHelper.createCopyFileSmbInfo(options, smbProperties);

        assertEquals(PermissionCopyModeType.SOURCE, smbInfo.getFilePermissionCopyMode());
        assertEquals(NtfsFileAttributes.toString(smbProperties.getNtfsFileAttributes()), smbInfo.getFileAttributes());
        assertEquals(SMB_TIME, smbInfo.getFileCreationTime());
        assertEquals(SMB_TIME, smbInfo.getFileLastWriteTime());
        assertEquals(SMB_TIME, smbInfo.getFileChangeTime());
        assertEquals(true, smbInfo.isIgnoreReadOnly());
        assertEquals(false, smbInfo.isSetArchiveAttribute());
    }

    @Test
    public void createCopyFileSmbInfoLeavesCopiedPropertiesUnset() {
        CopyableFileSmbPropertiesList copyAll = new CopyableFileSmbPropertiesList().setFileAttributes(true)
            .setCreatedOn(true)
            .setLastWrittenOn(true)
            .setChangedOn(true);
        ShareFileCopyOptions options = new ShareFileCopyOptions().setSmbPropertiesToCopy(copyAll);

        CopyFileSmbInfo smbInfo = ModelHelper.createCopyFileSmbInfo(options, new FileSmbProperties());

        assertNull(smbInfo.getFileAttributes());
        assertNull(smbInfo.getFileCreationTime());
        assertNull(smbInfo.getFileLastWriteTime());
        assertNull(smbInfo.getFileChangeTime());
        assertNull(smbInfo.getFilePermissionCopyMode());
    }

    private static Function<CopyableFileSmbPropertiesList, CopyableFileSmbPropertiesList>
        flag(Function<CopyableFileSmbPropertiesList, CopyableFileSmbPropertiesList> f) {
        return f;
    }

    private static Function<FileSmbProperties, FileSmbProperties>
        property(Function<FileSmbProperties, FileSmbProperties> f) {
        return f;
    }
}
