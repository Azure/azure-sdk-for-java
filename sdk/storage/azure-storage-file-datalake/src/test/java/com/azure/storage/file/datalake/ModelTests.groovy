package com.azure.storage.file.datalake

import com.azure.storage.file.datalake.models.AccessControlType
import com.azure.storage.file.datalake.models.PathAccessControlEntry
import com.azure.storage.file.datalake.models.PathPermissions
import com.azure.storage.file.datalake.models.RolePermissions
import spock.lang.Unroll

class ModelTests extends APISpec{

    def "RolePermissions creation"() {
        when:
        def permissions = new RolePermissions(true, true, true)
        def other = new RolePermissions(permissions)

        then:
        permissions.read()
        permissions.execute()
        permissions.write()

        other.read()
        other.execute()
        other.write()

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
        permissions.owner() == owner
        permissions.group() == group
        permissions.other() == other
        permissions.stickyBit()
        permissions.extendedInfoInAcl()
    }

    def "PathPermissions create"() {
        when:
        def permissions = PathPermissions.parseSymbolic("r---w---t+")
        def otherPermission = new PathPermissions(permissions)
        def owner = RolePermissions.parseOctal(4)
        def group = RolePermissions.parseOctal(2)
        def other = RolePermissions.parseOctal(1)

        then:
        otherPermission.owner() == owner
        otherPermission.group() == group
        otherPermission.other() == other
        otherPermission.stickyBit()
        otherPermission.extendedInfoInAcl()
        permissions.equals(otherPermission)
    }

    @Unroll
    def "PathPermissions parse"() {
        when:
        def permissions = PathPermissions.parseSymbolic(symbol)

        then:
        permissions.other().execute() == execute
        permissions.other().read() == read
        permissions.other().write() == write
        permissions.stickyBit() == stickyBit
        permissions.extendedInfoInAcl() == extendedInfoInAcl

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
        owner == null ? true : permissions.owner() == owner
        group == null ? true : permissions.group() == group
        other == null ? true : permissions.other() == other
        permissions.stickyBit() == stickyBit

        where:
        octal   || owner                         | group                         | other                         | stickyBit
        "1421"  || RolePermissions.parseOctal(4) | RolePermissions.parseOctal(2) | RolePermissions.parseOctal(1) | true
        "0123"  || null                          | null                          | null                          | false
    }

    def "PathAccessControlEntry"() {
        when:
        def entry = new PathAccessControlEntry(true, AccessControlType.GROUP, "a", RolePermissions.parseOctal(0))
        def fromStr = PathAccessControlEntry.parse("default:group:a:---")

        then:
        entry.defaultScope()
        entry.accessControlType() == AccessControlType.GROUP
        entry.entityID() == "a"
        entry.permissions() == RolePermissions.parseOctal(0)
        entry.toString() == "default:group:a:---"
        entry.equals(fromStr)

        when:
        entry = new PathAccessControlEntry(false, AccessControlType.MASK, null, RolePermissions.parseOctal(4))
        fromStr = PathAccessControlEntry.parse("mask::r--")

        then:
        entry.toString() == "mask::r--"
        entry.equals(fromStr)

        when:
        entry = new PathAccessControlEntry(false, AccessControlType.USER, "b", RolePermissions.parseOctal(2))

        then:
        entry.toString() == "user:b:-w-"
    }

    def "PathAccessControlEntry List"() {
        when:
        List<PathAccessControlEntry> acl = new ArrayList<>()
        acl.add(new PathAccessControlEntry(true, AccessControlType.USER, "c", RolePermissions.parseOctal(1)))
        acl.add(new PathAccessControlEntry(false, AccessControlType.OTHER, null, RolePermissions.parseOctal(7)))
        def listFromStr = PathAccessControlEntry.parseList("default:user:c:--x,other::rwx")

        then:
        PathAccessControlEntry.serializeList(acl) == "default:user:c:--x,other::rwx"
        compareACL(acl, listFromStr)
    }


}
