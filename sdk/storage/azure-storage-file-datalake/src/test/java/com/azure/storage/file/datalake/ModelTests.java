// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.file.datalake;

import com.azure.storage.file.datalake.models.AccessControlType;
import com.azure.storage.file.datalake.models.CopyStatusType;
import com.azure.storage.file.datalake.models.FileQueryHeaders;
import com.azure.storage.file.datalake.models.FileReadHeaders;
import com.azure.storage.file.datalake.models.PathAccessControlEntry;
import com.azure.storage.file.datalake.models.PathPermissions;
import com.azure.storage.file.datalake.models.RolePermissions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.azure.storage.file.datalake.DataLakeTestBase.compareACL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelTests {
    @ParameterizedTest
    @CsvSource({ "1,--x", "2,-w-", "4,r--" })
    public void rolePermissionsParseOctal(int octal, String permission) {
        assertEquals(permission, RolePermissions.parseOctal(octal).toSymbolic());
    }

    @ParameterizedTest
    @CsvSource({"--x,1", "-w-,2", "r--,4"})
    public void rolePermissionsParseSymbolic(String symbol, int permission) {
        assertEquals(String.valueOf(permission), RolePermissions.parseSymbolic(symbol, false).toOctal());
    }

    @Test
    public void pathPermissionsParseSymbolic() {
        PathPermissions permissions = PathPermissions.parseSymbolic("r---w---t+");

        assertEquals(RolePermissions.parseOctal(4), permissions.getOwner());
        assertEquals(RolePermissions.parseOctal(2), permissions.getGroup());
        assertEquals(RolePermissions.parseOctal(1), permissions.getOther());
        assertTrue(permissions.isStickyBitSet());
        assertTrue(permissions.isExtendedInfoInAcl());
    }

    @Test
    public void pathPermissionsParseSymbolicError() {
        assertThrows(IllegalArgumentException.class, () ->  PathPermissions.parseSymbolic("r---w---t0"));
    }

    @Test
    public void pathPermissionsCreate() {
        PathPermissions permissions = PathPermissions.parseSymbolic("r---w---t+");

        assertEquals(RolePermissions.parseOctal(4), permissions.getOwner());
        assertEquals(RolePermissions.parseOctal(2), permissions.getGroup());
        assertEquals(RolePermissions.parseOctal(1), permissions.getOther());
        assertTrue(permissions.isStickyBitSet());
        assertTrue(permissions.isExtendedInfoInAcl());
    }

    @ParameterizedTest
    @CsvSource({"rwxrwxrwT,false,true,true,true,false", "rwxrwxrwx,true,true,true,false,false",
        "rwxrwxrw-,false,true,true,false,false"})
    public void pathPermissionsParse(String symbol, boolean execute, boolean read, boolean write, boolean stickyBit,
        boolean extendedInfoInAcl) {
        PathPermissions permissions = PathPermissions.parseSymbolic(symbol);

        assertEquals(execute, permissions.getOther().hasExecutePermission());
        assertEquals(read, permissions.getOther().hasReadPermission());
        assertEquals(write, permissions.getOther().hasWritePermission());
        assertEquals(stickyBit, permissions.isStickyBitSet());
        assertEquals(extendedInfoInAcl, permissions.isExtendedInfoInAcl());
    }

    private static Stream<Arguments> pathPermissionsParseOctalSupplier() {
        return Stream.of(
            // octal, owner, group, other, stickyBit
            Arguments.of("1421", RolePermissions.parseOctal(4), RolePermissions.parseOctal(2),
                RolePermissions.parseOctal(1), true),
            Arguments.of("0123", null, null, null, false)
        );
    }

    @Test
    public void pathPermissionsParseOctalError() {
        assertThrows(IllegalArgumentException.class, () -> PathPermissions.parseOctal("0"));
    }

    @Test
    public void pathPermissionsHashCode() {
        PathPermissions perm1 = PathPermissions.parseSymbolic("r---w---t+");
        PathPermissions perm2 = PathPermissions.parseSymbolic("r---w---t+");
        PathPermissions perm3 = PathPermissions.parseSymbolic("r---w---t");

        assertEquals(perm1, perm2);
        assertNotEquals(perm1, perm3);
    }

    @Test
    public void pathPermissionsEquals() {
        PathPermissions perm1 = PathPermissions.parseSymbolic("r---w---t+");
        PathPermissions notSticky = PathPermissions.parseSymbolic("r---w---x+");
        PathPermissions notExtended = PathPermissions.parseSymbolic("r---w---t");
        PathPermissions noOwner = PathPermissions.parseSymbolic("----w---t+");
        PathPermissions noGroup = PathPermissions.parseSymbolic("r-------t+");
        PathPermissions noOther = PathPermissions.parseSymbolic("r---w----+");

        assertTrue(perm1.equals(perm1));
        assertFalse(perm1.equals(null));
        assertFalse(perm1.equals(notSticky));
        assertFalse(perm1.equals(notExtended));
        assertFalse(perm1.equals(noOwner));
        assertFalse(perm1.equals(noGroup));
        assertFalse(perm1.equals(noOther));
    }

    @Test
    public void pathPermissionsToString() {
        PathPermissions sticky = PathPermissions.parseSymbolic("r---w---t+");
        PathPermissions notSticky = PathPermissions.parseSymbolic("r---w---x+");

        assertEquals(sticky.toString(), "1421");
        assertEquals(notSticky.toString(), "0421");
    }

    @Test
    public void pathPermissionsSetStickyBit() {
        PathPermissions notSticky = PathPermissions.parseSymbolic("r---w---x+");
        assertFalse(notSticky.isStickyBitSet());
        notSticky.setStickyBit(true);
        assertTrue(notSticky.isStickyBitSet());
    }

    @Test
    public void pathPermissionsSetExtendedInfo() {
        PathPermissions notExtended = PathPermissions.parseSymbolic("r---w---x");
        assertFalse(notExtended.isExtendedInfoInAcl());
        notExtended.setExtendedInfoInAcl(true);
        assertTrue(notExtended.isExtendedInfoInAcl());
    }

    @Test
    public void setFileReadHeadersNull() {
        FileReadHeaders headers = new FileReadHeaders();
        headers.setCopyCompletionTime(null);
        headers.setLastModified(null);
        headers.setDateProperty(null);

        assertNull(headers.getCopyCompletionTime());
        assertNull(headers.getLastModified());
        assertNull(headers.getDateProperty());
    }

    @Test
    public void getFileReadHeaders() {
        FileReadHeaders headers = new FileReadHeaders();
        assertNull(headers.getLastModified());
        assertNull(headers.getCopyCompletionTime());
        assertNull(headers.getDateProperty());
        assertNull(headers.getContentMd5());
        assertNull(headers.getFileContentMd5());
        assertNull(headers.getContentCrc64());
        assertNull(headers.getMetadata());
        assertNull(headers.getContentLength());
        assertNull(headers.getContentType());
        assertNull(headers.getContentRange());
        assertNull(headers.getETag());
        assertNull(headers.getContentEncoding());
        assertNull(headers.getCacheControl());
        assertNull(headers.getContentDisposition());
        assertNull(headers.getContentLanguage());
        assertNull(headers.getCopyStatusDescription());
        assertNull(headers.getCopyId());
        assertNull(headers.getCopyProgress());
        assertNull(headers.getCopySource());
        assertNull(headers.getCopyStatus());
        assertNull(headers.getLeaseDuration());
        assertNull(headers.getLeaseState());
        assertNull(headers.getLeaseStatus());
        assertNull(headers.getClientRequestId());
        assertNull(headers.getRequestId());
        assertNull(headers.getVersion());
        assertNull(headers.getAcceptRanges());
        assertNull(headers.isServerEncrypted());
        assertNull(headers.getEncryptionKeySha256());
        assertNull(headers.getErrorCode());
    }

    @Test
    public void setFileQueryHeadersNull() {
        FileQueryHeaders headers = new FileQueryHeaders();
        headers.setCopyCompletionTime(null);
        headers.setLastModified(null);
        headers.setDateProperty(null);

        assertNull(headers.getCopyCompletionTime());
        assertNull(headers.getLastModified());
        assertNull(headers.getDateProperty());
    }

    @Test
    public void getFileQueryHeaders() {
        FileQueryHeaders headers = new FileQueryHeaders();
        assertNull(headers.getLastModified());
        assertNull(headers.getCopyCompletionTime());
        assertNull(headers.getDateProperty());
        assertNull(headers.getContentMd5());
        assertNull(headers.getFileContentMd5());
        assertNull(headers.getContentCrc64());
        assertNull(headers.getMetadata());
        assertNull(headers.getContentLength());
        assertNull(headers.getContentType());
        assertNull(headers.getContentRange());
        assertNull(headers.getETag());
        assertNull(headers.getContentEncoding());
        assertNull(headers.getCacheControl());
        assertNull(headers.getContentDisposition());
        assertNull(headers.getContentLanguage());
        assertNull(headers.getCopyStatusDescription());
        assertNull(headers.getCopyId());
        assertNull(headers.getCopyProgress());
        assertNull(headers.getCopySource());
        assertNull(headers.getCopyStatus());
        assertNull(headers.getLeaseDuration());
        assertNull(headers.getLeaseState());
        assertNull(headers.getLeaseStatus());
        assertNull(headers.getClientRequestId());
        assertNull(headers.getRequestId());
        assertNull(headers.getVersion());
        assertNull(headers.getAcceptRanges());
        assertNull(headers.isServerEncrypted());
        assertNull(headers.getEncryptionKeySha256());
        assertNull(headers.getErrorCode());
    }

    @Test
    public void copyStatusTypeFromString() {
        CopyStatusType type = CopyStatusType.fromString("pending");
        assertEquals(type.toString(), "pending");
    }

    @Test
    public void copyStatusTypeFromStringError() {
        CopyStatusType type = CopyStatusType.fromString("garbage");
        assertNull(type);
    }

    @Test
    public void pathAccessControlEntryHashCode() {
        PathAccessControlEntry acc1 = PathAccessControlEntry.parse("default:group:a:---");
        PathAccessControlEntry acc2 = PathAccessControlEntry.parse("default:group:a:---");
        PathAccessControlEntry acc3 = PathAccessControlEntry.parse("mask::r--");

        assertEquals(acc1, acc2);
        assertNotEquals(acc1, acc3);
    }

    @Test
    public void PathAccessControlEntryEquals() {
        PathAccessControlEntry perm1 = PathAccessControlEntry.parse("default:group:a:---");
        PathAccessControlEntry scope = PathAccessControlEntry.parse("group:a:---");
        PathAccessControlEntry accessControlType = PathAccessControlEntry.parse("default:user:a:---");
        PathAccessControlEntry entityID = PathAccessControlEntry.parse("default:group::---");
        PathAccessControlEntry perms = PathAccessControlEntry.parse("default:group:a:r--");

        assertTrue(perm1.equals(perm1));
        assertFalse(perm1.equals(null));
        assertFalse(perm1.equals(scope));
        assertFalse(perm1.equals(accessControlType));
        assertFalse(perm1.equals(entityID));
        assertFalse(perm1.equals(perms));
    }

    @Test
    public void pathAccessControlEntry() {
        PathAccessControlEntry entry = new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.GROUP)
            .setPermissions(RolePermissions.parseOctal(0))
            .setDefaultScope(true)
            .setEntityId("a");
        PathAccessControlEntry fromStr = PathAccessControlEntry.parse("default:group:a:---");

        assertTrue(entry.isInDefaultScope());
        assertEquals(AccessControlType.GROUP, entry.getAccessControlType());
        assertEquals("a", entry.getEntityId());
        assertEquals(RolePermissions.parseOctal(0), entry.getPermissions());
        assertEquals("default:group:a:---", entry.toString());
        assertEquals(fromStr, entry);

        entry = new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.MASK)
            .setPermissions(RolePermissions.parseOctal(4))
            .setDefaultScope(false)
            .setEntityId(null);
        fromStr = PathAccessControlEntry.parse("mask::r--");

        assertEquals("mask::r--", entry.toString());
        assertEquals(fromStr, entry);

        entry = new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.USER)
            .setPermissions(RolePermissions.parseOctal(2))
            .setDefaultScope(false)
            .setEntityId("b");

        assertEquals("user:b:-w-", entry.toString());
    }

    @Test
    public void pathAccessControlEntryList() {
        List<PathAccessControlEntry> acl = new ArrayList<>();
        acl.add(new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.USER)
            .setPermissions(RolePermissions.parseOctal(1))
            .setDefaultScope(true)
            .setEntityId("c"));
        acl.add(new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.OTHER)
            .setPermissions(RolePermissions.parseOctal(7))
            .setDefaultScope(false)
            .setEntityId(null));
        List<PathAccessControlEntry> listFromStr = PathAccessControlEntry.parseList("default:user:c:--x,other::rwx");

        assertEquals("default:user:c:--x,other::rwx", PathAccessControlEntry.serializeList(acl));
        compareACL(acl, listFromStr);
    }
}
