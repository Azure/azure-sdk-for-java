// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.util.BinaryData;
import com.azure.storage.blob.ContentValidationTestUtils.RecordedRequest;
import com.azure.storage.blob.models.PageRange;
import com.azure.storage.blob.options.AppendBlobAppendBlockOptions;
import com.azure.storage.blob.options.AppendBlobOutputStreamOptions;
import com.azure.storage.blob.options.BlobDownloadContentOptions;
import com.azure.storage.blob.options.BlobDownloadStreamOptions;
import com.azure.storage.blob.options.BlobDownloadToFileOptions;
import com.azure.storage.blob.options.BlobInputStreamOptions;
import com.azure.storage.blob.options.BlobParallelUploadOptions;
import com.azure.storage.blob.options.BlobSeekableByteChannelReadOptions;
import com.azure.storage.blob.options.BlobUploadFromFileOptions;
import com.azure.storage.blob.options.BlockBlobOutputStreamOptions;
import com.azure.storage.blob.options.BlockBlobSeekableByteChannelWriteOptions;
import com.azure.storage.blob.options.BlockBlobSimpleUploadOptions;
import com.azure.storage.blob.options.BlockBlobStageBlockOptions;
import com.azure.storage.blob.options.PageBlobOutputStreamOptions;
import com.azure.storage.blob.options.PageBlobUploadPagesOptions;
import com.azure.storage.common.ContentValidationAlgorithm;
import com.azure.storage.common.ValidatableContent;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.common.implementation.contentvalidation.StructuredMessageConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.azure.storage.blob.ContentValidationTestUtils.allUploadsUseStructuredMessage;
import static com.azure.storage.blob.ContentValidationTestUtils.contentBearingUploadRequests;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that the various upload and download option types implement {@link ValidatableContent} so that content
 * validation can be configured and inspected agnostically across operations.
 */
public class ValidatableContentTests {

    private static Stream<Arguments> validatableOptions() {
        BinaryData data = BinaryData.fromString("data");
        PageRange pageRange = new PageRange().setStart(0).setEnd(511);
        // Every options type that implements ValidatableContent must be represented here so the shared
        // content-validation contract is exercised for all of them, not just a subset.
        return Stream.of(Arguments.of("AppendBlobAppendBlockOptions", new AppendBlobAppendBlockOptions(data)),
            Arguments.of("AppendBlobOutputStreamOptions", new AppendBlobOutputStreamOptions()),
            Arguments.of("PageBlobUploadPagesOptions", new PageBlobUploadPagesOptions(pageRange, data)),
            Arguments.of("PageBlobOutputStreamOptions", new PageBlobOutputStreamOptions(pageRange)),
            Arguments.of("BlockBlobStageBlockOptions", new BlockBlobStageBlockOptions("blockId", data)),
            Arguments.of("BlockBlobSimpleUploadOptions", new BlockBlobSimpleUploadOptions(data)),
            Arguments.of("BlockBlobOutputStreamOptions", new BlockBlobOutputStreamOptions()),
            Arguments.of("BlockBlobSeekableByteChannelWriteOptions",
                new BlockBlobSeekableByteChannelWriteOptions(
                    BlockBlobSeekableByteChannelWriteOptions.WriteMode.OVERWRITE)),
            Arguments.of("BlobParallelUploadOptions", new BlobParallelUploadOptions(data)),
            Arguments.of("BlobUploadFromFileOptions", new BlobUploadFromFileOptions("file.bin")),
            Arguments.of("BlobDownloadContentOptions", new BlobDownloadContentOptions()),
            Arguments.of("BlobDownloadStreamOptions", new BlobDownloadStreamOptions()),
            Arguments.of("BlobDownloadToFileOptions", new BlobDownloadToFileOptions("file.bin")),
            Arguments.of("BlobInputStreamOptions", new BlobInputStreamOptions()),
            Arguments.of("BlobSeekableByteChannelReadOptions", new BlobSeekableByteChannelReadOptions()));
    }

    /**
     * Independently discovers every {@link ValidatableContent} options type on the classpath and asserts that
     * {@link #validatableOptions()} covers exactly that set (and contains no duplicates). Deriving the expected set
     * via reflection — rather than hardcoding a count — means adding a new ValidatableContent options type without
     * updating {@link #validatableOptions()} fails this test.
     */
    @Test
    public void allValidatableOptionTypesAreCovered() throws Exception {
        List<Class<?>> coveredList
            = validatableOptions().map(args -> args.get()[1].getClass()).collect(Collectors.toList());
        Set<Class<?>> covered = new HashSet<>(coveredList);
        assertEquals(coveredList.size(), covered.size(), "validatableOptions() must not contain duplicate types");

        Set<Class<?>> discovered = discoverValidatableContentOptionTypes();
        assertEquals(discovered, covered,
            "validatableOptions() must cover exactly the ValidatableContent options types. Missing: "
                + difference(discovered, covered) + "; unexpected: " + difference(covered, discovered));
    }

    private static Set<Class<?>> difference(Set<Class<?>> a, Set<Class<?>> b) {
        Set<Class<?>> result = new HashSet<>(a);
        result.removeAll(b);
        return result;
    }

    /**
     * Scans {@code com.azure.storage.blob.options} on the classpath for every public, concrete class implementing
     * {@link ValidatableContent}, handling both exploded class directories and jars.
     */
    private static Set<Class<?>> discoverValidatableContentOptionTypes() throws Exception {
        String pkg = "com.azure.storage.blob.options";
        String pkgPath = pkg.replace('.', '/');
        Set<Class<?>> result = new HashSet<>();
        File root
            = new File(BlockBlobStageBlockOptions.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        if (root.isDirectory()) {
            File[] classFiles
                = new File(root, pkgPath).listFiles((dir, name) -> name.endsWith(".class") && !name.contains("$"));
            if (classFiles != null) {
                for (File classFile : classFiles) {
                    String name = classFile.getName();
                    addIfValidatableOption(result, pkg + "." + name.substring(0, name.length() - ".class".length()));
                }
            }
        } else {
            try (JarFile jar = new JarFile(root)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    String name = entries.nextElement().getName();
                    if (name.startsWith(pkgPath + "/")
                        && name.endsWith(".class")
                        && !name.contains("$")
                        && name.indexOf('/', pkgPath.length() + 1) < 0) {
                        addIfValidatableOption(result,
                            name.substring(0, name.length() - ".class".length()).replace('/', '.'));
                    }
                }
            }
        }
        return result;
    }

    private static void addIfValidatableOption(Set<Class<?>> result, String className) throws ClassNotFoundException {
        Class<?> clazz = Class.forName(className);
        int mods = clazz.getModifiers();
        if (ValidatableContent.class.isAssignableFrom(clazz)
            && !clazz.isInterface()
            && !Modifier.isAbstract(mods)
            && Modifier.isPublic(mods)) {
            result.add(clazz);
        }
    }

    @ParameterizedTest
    @MethodSource("validatableOptions")
    public void optionsImplementValidatableContent(String name, Object options) {
        ValidatableContent validatable = assertInstanceOf(ValidatableContent.class, options, name);

        // Defaults to null (no content validation configured).
        assertNull(validatable.getContentValidationAlgorithm(), name);

        // Fluent setter returns the same instance and the value round-trips through the interface.
        ValidatableContent returned = validatable.setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
        assertSame(validatable, returned, name);
        assertSame(ContentValidationAlgorithm.CRC64, validatable.getContentValidationAlgorithm(), name);

        // NONE and AUTO round-trip too, and clearing back to null is supported.
        assertSame(ContentValidationAlgorithm.NONE,
            returned.setContentValidationAlgorithm(ContentValidationAlgorithm.NONE).getContentValidationAlgorithm(),
            name);
        assertSame(ContentValidationAlgorithm.AUTO,
            returned.setContentValidationAlgorithm(ContentValidationAlgorithm.AUTO).getContentValidationAlgorithm(),
            name);
        assertNull(returned.setContentValidationAlgorithm(null).getContentValidationAlgorithm(), name);
    }

    @Test
    public void configureAgnostically() {
        // A single helper can operate on any supported options type via the interface.
        AppendBlobAppendBlockOptions appendOptions = new AppendBlobAppendBlockOptions(BinaryData.fromString("data"));
        applyCrc64(appendOptions);
        assertSame(ContentValidationAlgorithm.CRC64, appendOptions.getContentValidationAlgorithm());
    }

    private static void applyCrc64(ValidatableContent content) {
        content.setContentValidationAlgorithm(ContentValidationAlgorithm.CRC64);
    }

    // ===========================================================================================
    // Options contract: null inputs and unknown-length payloads must be rejected before any request is issued.
    // ===========================================================================================

    private static final PageRange CONTRACT_PAGE_RANGE = new PageRange().setStart(0).setEnd(511);

    @Test
    public void nullDataIsRejected() {
        assertThrows(NullPointerException.class, () -> new AppendBlobAppendBlockOptions(null));
        assertThrows(NullPointerException.class, () -> new BlockBlobStageBlockOptions("blockId", null));
        assertThrows(NullPointerException.class, () -> new BlockBlobSimpleUploadOptions((BinaryData) null));
        assertThrows(NullPointerException.class, () -> new PageBlobUploadPagesOptions(CONTRACT_PAGE_RANGE, null));
    }

    @Test
    public void nullBlockIdIsRejected() {
        assertThrows(NullPointerException.class,
            () -> new BlockBlobStageBlockOptions(null, BinaryData.fromString("data")));
    }

    @Test
    public void nullPageRangeIsRejected() {
        assertThrows(NullPointerException.class,
            () -> new PageBlobUploadPagesOptions(null, BinaryData.fromString("data")));
    }

    @Test
    public void unknownLengthDataIsRejected() {
        // BinaryData.fromStream(stream) has no defined length; content validation requires a known length, so the
        // options constructor must reject it up front rather than sending an un-validatable request.
        assertThrows(NullPointerException.class, () -> new AppendBlobAppendBlockOptions(unknownLengthData()));
        assertThrows(NullPointerException.class, () -> new BlockBlobStageBlockOptions("blockId", unknownLengthData()));
        assertThrows(NullPointerException.class, () -> new BlockBlobSimpleUploadOptions(unknownLengthData()));
        assertThrows(NullPointerException.class,
            () -> new PageBlobUploadPagesOptions(CONTRACT_PAGE_RANGE, unknownLengthData()));
    }

    private static BinaryData unknownLengthData() {
        return BinaryData.fromStream(new ByteArrayInputStream(new byte[] { 1, 2, 3, 4 }));
    }

    // ===========================================================================================
    // Request-helper guards: an upload request missing validation headers must fail the check rather than being
    // silently ignored, so one validated block cannot make an otherwise unvalidated transfer pass.
    // ===========================================================================================

    @Test
    public void helperRejectsTransferWhereOneBlockIsUnvalidated() {
        List<RecordedRequest> recorded = new ArrayList<>();
        recorded.add(structuredAppendRequest(2 * Constants.MB));
        recorded.add(structuredAppendRequest(2 * Constants.MB));
        // A content-bearing upload request that carries NO validation headers (the regression this guards against).
        recorded.add(unvalidatedAppendRequest(Constants.MB));

        assertEquals(3, contentBearingUploadRequests(recorded).size());
        assertFalse(allUploadsUseStructuredMessage(recorded),
            "One unvalidated block must make the whole transfer fail the check");
    }

    @Test
    public void helperAcceptsTransferWhereEveryBlockIsValidated() {
        List<RecordedRequest> recorded = new ArrayList<>();
        recorded.add(structuredAppendRequest(2 * Constants.MB));
        recorded.add(structuredAppendRequest(2 * Constants.MB));
        recorded.add(structuredAppendRequest(Constants.MB));
        assertTrue(allUploadsUseStructuredMessage(recorded));
    }

    @Test
    public void helperIgnoresNonContentBearingRequests() {
        List<RecordedRequest> recorded = new ArrayList<>();
        // Create-append-blob (no body) and commit-block-list requests must not be treated as upload data.
        recorded.add(new RecordedRequest(HttpMethod.PUT, "https://acct.blob.core.windows.net/c/b",
            new HttpHeaders().set(HttpHeaderName.fromString("x-ms-blob-type"), "AppendBlob")
                .set(HttpHeaderName.CONTENT_LENGTH, "0")));
        recorded.add(new RecordedRequest(HttpMethod.PUT, "https://acct.blob.core.windows.net/c/b?comp=blocklist",
            new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, "40")));
        recorded.add(structuredAppendRequest(2 * Constants.MB));
        assertEquals(1, contentBearingUploadRequests(recorded).size());
        assertTrue(allUploadsUseStructuredMessage(recorded));
    }

    private static RecordedRequest structuredAppendRequest(int unencodedLength) {
        HttpHeaders headers = new HttpHeaders()
            .set(Constants.HeaderConstants.STRUCTURED_BODY_TYPE_HEADER_NAME,
                StructuredMessageConstants.STRUCTURED_BODY_TYPE_VALUE)
            .set(Constants.HeaderConstants.STRUCTURED_CONTENT_LENGTH_HEADER_NAME, String.valueOf(unencodedLength))
            .set(HttpHeaderName.CONTENT_LENGTH,
                String.valueOf(ContentValidationTestUtils.expectedStructuredMessageEncodedLength(unencodedLength)));
        return new RecordedRequest(HttpMethod.PUT, "https://acct.blob.core.windows.net/c/b?comp=appendblock", headers);
    }

    private static RecordedRequest unvalidatedAppendRequest(int length) {
        HttpHeaders headers = new HttpHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(length));
        return new RecordedRequest(HttpMethod.PUT, "https://acct.blob.core.windows.net/c/b?comp=appendblock", headers);
    }
}
