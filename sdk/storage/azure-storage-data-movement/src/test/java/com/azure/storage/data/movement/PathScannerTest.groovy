// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.data.movement

import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.attribute.AclEntry
import java.nio.file.attribute.AclEntryPermission
import java.nio.file.attribute.AclEntryType
import java.nio.file.attribute.AclFileAttributeView
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.nio.file.attribute.UserPrincipal

class PathScannerTest extends Specification {
    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)
    }

    @Unroll
    def "Scan local folder containing items of mixed permissions"() {
        given:
        Path folder = Files.createTempDirectory(Paths.get(System.getProperty("java.io.tmpdir")), null);
        Path openChild = Files.createTempFile(folder, null, null);
        Path lockedChild = Files.createTempFile(folder, null, null);

        Path openSubfolder = Files.createTempDirectory(folder, null);
        Path openSubchild = Files.createTempFile(openSubfolder, null, null);

        Path lockedSubfolder = Files.createTempDirectory(folder, null);
        Path lockedSubchild = Files.createTempFile(lockedSubfolder, null, null);

        allowReadData(lockedChild, false);
        allowReadData(lockedSubfolder, false);

        PathScannerFactory scannerFactory = new PathScannerFactory(folder.toAbsolutePath().toString());
        PathScanner scanner = scannerFactory.getPathScanner();

        List<String> expectedResult = [folder, openChild, lockedChild, openSubfolder, lockedSubfolder, openSubchild]
            .stream()
            .map({ path -> path.toString() })
            .collect()
            .asList();

        when:
        Flux<String> result = scanner.scan(true);

        then:
        // Ordering should go from shallowest to deepest level in file hierarchy
        StepVerifier.create(result)
            .expectNextMatches({ path -> liesWithin(path, expectedResult, 0, 1) })
            .expectNextMatches({ path -> liesWithin(path, expectedResult, 1, 5) })
            .expectNextMatches({ path -> liesWithin(path, expectedResult, 1, 5) })
            .expectNextMatches({ path -> liesWithin(path, expectedResult, 1, 5) })
            .expectNextMatches({ path -> liesWithin(path, expectedResult, 1, 5) })
            .expectNextMatches({ path -> liesWithin(path, expectedResult, 5, 6) })
            .expectComplete()
            .verify();

        cleanup:
        allowReadData(lockedChild, true);
        allowReadData(lockedSubfolder, true);

        Files.walk(folder)
            .sorted({ o1, o2 -> -(o1 <=> o2) })
            .forEach(Files.&delete);
    }

    def static allowReadData(Path path, boolean allowRead) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            UserPrincipal user = path.getFileSystem().getUserPrincipalLookupService().lookupPrincipalByName(System.getProperty("user.name"));

            AclFileAttributeView view = Files.getFileAttributeView(path, AclFileAttributeView.class);

            AclEntry entry = AclEntry.newBuilder()
                .setType(allowRead ? AclEntryType.ALLOW : AclEntryType.DENY)
                .setPrincipal(user)
                .setPermissions(AclEntryPermission.READ_DATA)
                .build();

            List<AclEntry> acl = view.getAcl();
            acl.add(0, entry);
            view.setAcl(acl);
        } else {
            Set<PosixFilePermission> permissions =
                PosixFilePermissions.fromString(allowRead ? "rwxrwxrwx" : "-w--w--w-");

            Files.setPosixFilePermissions(path, permissions);
        }
    }

    static boolean liesWithin(String searchValue, List<String> list, int start, int end) {
        System.out.println(searchValue);
        return list.subList(start, end).contains(searchValue);
    }
}
