// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.data.movement

import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.AccessDeniedException
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
    static def temp = Paths.get(System.getProperty("java.io.tmpdir"));

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)
    }

    @Unroll
    def "Scan folder containing mixed permissions"() {
        given:
        Path folder = Files.createTempDirectory(temp, null);
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
            .map({ path -> path.toAbsolutePath().toString() })
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

    @Unroll
    def "Scan folder without read permission"() {
        given:
        Path folder = Files.createTempDirectory(temp, null);
        Path child = Files.createTempFile(folder, null, null);

        allowReadData(folder, false);

        PathScannerFactory scannerFactory = new PathScannerFactory(folder.toAbsolutePath().toString());
        PathScanner scanner = scannerFactory.getPathScanner();

        when:
        Flux<String> result = scanner.scan(false);

        then:
        StepVerifier.create(result)
            .expectNext(folder.toAbsolutePath().toString())
            .expectError(AccessDeniedException.class)
            .verify();

        cleanup:
        allowReadData(folder, true);

        Files.walk(folder)
            .sorted({ o1, o2 -> -(o1 <=> o2) })
            .forEach(Files.&delete);
    }

    @Unroll
    def "Scan single file path"() {
        given:
        Path file = Files.createTempFile(temp, null, null);

        PathScannerFactory scannerFactory = new PathScannerFactory(file.toAbsolutePath().toString());
        PathScanner scanner = scannerFactory.getPathScanner();

        when:
        Flux<String> result = scanner.scan(false);

        then:
        StepVerifier.create(result)
            .expectNext(file.toAbsolutePath().toString())
            .expectComplete()
            .verify();

        cleanup:
        Files.delete(file);
    }

    @Unroll
    def "Scan unreadable file path"() {
        given:
        Path file = Files.createTempFile(temp, null, null);

        allowReadData(file, false);

        PathScannerFactory scannerFactory = new PathScannerFactory(file.toAbsolutePath().toString());
        PathScanner scanner = scannerFactory.getPathScanner();

        when:
        Flux<String> result = scanner.scan(false);

        then:
        StepVerifier.create(result)
            .expectNext(file.toAbsolutePath().toString())
            .expectComplete()
            .verify();

        cleanup:
        allowReadData(file, true);

        Files.delete(file);
    }

    def "Scan nonexistent item"() {
        given:
        Path file = Paths.get(temp.toAbsolutePath().toString(), UUID.randomUUID().toString());

        when:
        PathScannerFactory scannerFactory = new PathScannerFactory(file.toAbsolutePath().toString());
        PathScanner scanner = scannerFactory.getPathScanner();

        then:
        thrown IllegalArgumentException;
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
