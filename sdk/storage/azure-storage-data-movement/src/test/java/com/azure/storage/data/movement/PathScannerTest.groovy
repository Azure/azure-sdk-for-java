package com.azure.storage.data.movement

import com.azure.storage.data.movement.PathScanner
import com.azure.storage.data.movement.PathScannerBuilder
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

        PathScannerBuilder scannerFactory = new PathScannerBuilder(folder.toAbsolutePath().toString());
        PathScanner scanner = scannerFactory.BuildPathScanner();

            def expectedResult = [openChild, lockedChild, openSubchild]
                .stream()
                .map({ path -> path.toString() })
                .collect();

        when:
            Flux<String> result = scanner.scan(true);

        then:
            StepVerifier.create(result)
                .expectNextMatches({ path -> expectedResult.contains(path) })
                .expectNextMatches({ path -> expectedResult.contains(path) })
                .expectNextMatches({ path -> expectedResult.contains(path) })
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
                PosixFilePermissions.fromString(allowRead ? "rw-rw-rw-" : "-w--w--w-");

            Files.setPosixFilePermissions(path, permissions);
        }
    }


}
