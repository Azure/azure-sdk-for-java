package com.azure.storage.file.datalake

import com.azure.storage.file.datalake.models.AccessControlType
import com.azure.storage.file.datalake.models.PathAccessControlEntry
import com.azure.storage.file.datalake.models.PathPermissions
import com.azure.storage.file.datalake.models.RolePermissions
import spock.lang.Unroll

class ModelTests extends APISpec {

    def "RolePermissions creation"() {
        when:
        def permissions = new RolePermissions().setReadPermission(true).setExecutePermission(true).setWritePermission(true)
        def other = new RolePermissions(permissions)

        then:
        permissions.hasReadPermission()
        permissions.hasExecutePermission()
        permissions.hasWritePermission()

        other.hasReadPermission()
        other.hasExecutePermission()
        other.hasWritePermission()

        permissions.equals(other)
    }

    @Unroll
    def "RolePermissions parse Octal"() {
        expect:
        RolePermissions.parseOctal(octal as int).toSymbolic() == permission

        where:
        octal || permission
        1     || "--x"
        2     || "-w-"
        4     || "r--"
    }

    @Unroll
    def "RolePermissions parse Symbolic"() {
        expect:
        RolePermissions.parseSymbolic(symbol, false).toOctal() == permission

        where:
        symbol || permission
        "--x"  || "1"
        "-w-"  || "2"
        "r--"  || "4"
    }

    def "PathPermissions parse Symbolic"() {
        when:
        def permissions = PathPermissions.parseSymbolic("r---w---t+")
        def owner = RolePermissions.parseOctal(4)
        def group = RolePermissions.parseOctal(2)
        def other = RolePermissions.parseOctal(1)

        then:
        permissions.getOwner() == owner
        permissions.getGroup() == group
        permissions.getOther() == other
        permissions.isStickyBitSet()
        permissions.isExtendedInfoInAcl()
    }

    def "PathPermissions create"() {
        when:
        def permissions = PathPermissions.parseSymbolic("r---w---t+")
        def owner = RolePermissions.parseOctal(4)
        def group = RolePermissions.parseOctal(2)
        def other = RolePermissions.parseOctal(1)

        then:
        permissions.getOwner() == owner
        permissions.getGroup() == group
        permissions.getOther() == other
        permissions.isStickyBitSet()
        permissions.isExtendedInfoInAcl()
    }

    @Unroll
    def "PathPermissions parse"() {
        when:
        def permissions = PathPermissions.parseSymbolic(symbol)

        then:
        permissions.getOther().hasExecutePermission() == execute
        permissions.getOther().hasReadPermission() == read
        permissions.getOther().hasWritePermission() == write
        permissions.isStickyBitSet() == stickyBit
        permissions.isExtendedInfoInAcl() == extendedInfoInAcl

        where:
        // These test the value of other
        symbol      || execute | read | write | stickyBit | extendedInfoInAcl
        "rwxrwxrwT" || false   | true | true  | true      | false
        "rwxrwxrwx" || true    | true | true  | false     | false
        "rwxrwxrw-" || false   | true | true  | false     | false
    }

    @Unroll
    def "PathPermissions parse Octal"() {
        when:
        def permissions = PathPermissions.parseOctal(octal)

        then:
        owner == null ? true : permissions.getOwner() == owner
        group == null ? true : permissions.getGroup() == group
        other == null ? true : permissions.getOther() == other
        permissions.isStickyBitSet() == stickyBit

        where:
        octal   || owner                         | group                         | other                         | stickyBit
        "1421"  || RolePermissions.parseOctal(4) | RolePermissions.parseOctal(2) | RolePermissions.parseOctal(1) | true
        "0123"  || null                          | null                          | null                          | false
    }

    def "PathAccessControlEntry"() {
        when:
        def entry = new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.GROUP)
            .setPermissions(RolePermissions.parseOctal(0))
            .setDefaultScope(true)
            .setEntityId("a")
        def fromStr = PathAccessControlEntry.parse("default:group:a:---")

        then:
        entry.isInDefaultScope()
        entry.getAccessControlType() == AccessControlType.GROUP
        entry.getEntityId() == "a"
        entry.getPermissions() == RolePermissions.parseOctal(0)
        entry.toString() == "default:group:a:---"
        entry.equals(fromStr)

        when:
        entry = new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.MASK)
            .setPermissions(RolePermissions.parseOctal(4))
            .setDefaultScope(false)
            .setEntityId(null)
        fromStr = PathAccessControlEntry.parse("mask::r--")

        then:
        entry.toString() == "mask::r--"
        entry.equals(fromStr)

        when:
        entry = new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.USER)
            .setPermissions(RolePermissions.parseOctal(2))
            .setDefaultScope(false)
            .setEntityId("b")

        then:
        entry.toString() == "user:b:-w-"
    }

    def "PathAccessControlEntry List"() {
        when:
        List<PathAccessControlEntry> acl = new ArrayList<>()

        acl.add(new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.USER)
            .setPermissions(RolePermissions.parseOctal(1))
            .setDefaultScope(true)
            .setEntityId("c"))
        acl.add(new PathAccessControlEntry()
            .setAccessControlType(AccessControlType.OTHER)
            .setPermissions(RolePermissions.parseOctal(7))
            .setDefaultScope(false)
            .setEntityId(null))
        def listFromStr = PathAccessControlEntry.parseList("default:user:c:--x,other::rwx")

        then:
        PathAccessControlEntry.serializeList(acl) == "default:user:c:--x,other::rwx"
        compareACL(acl, listFromStr)
    }


}
