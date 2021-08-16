// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.netty.implementation.graalvm.features;

import org.graalvm.nativeimage.hosted.Feature;

/**
 * This class registers native libraries that Netty supports. If the feature is enabled, these libraries will be
 * statically linked into the resulting native image. If this feature is not enabled, the standard JDK implementations
 * of these features will be used instead (which is slower, but results in a small native image).
 */
public class NettyFeature implements Feature {
//    @Override
//    public void duringSetup(DuringSetupAccess access) {
//        ResourcesRegistry resourceRegistry = ImageSingletons.lookup(ResourcesRegistry.class);
//
//        if (Platform.includedIn(Platform.WINDOWS_AMD64.class)) {
//            resourceRegistry.addResources("\\QMETA-INF/native/netty_tcnative_windows_x86_64.dll\\E");
//        }
//
//        if (Platform.includedIn(Platform.LINUX_AMD64.class)) {
//            resourceRegistry.addResources("\\QMETA-INF/native/libnetty_transport_native_epoll_x86_64.so\\E");
//            resourceRegistry.addResources("\\QMETA-INF/native/libnetty_tcnative_linux_x86_64.so\\E");
//        }
//        if (Platform.includedIn(Platform.LINUX_AARCH64.class)) {
//            resourceRegistry.addResources("\\QMETA-INF/native/libnetty_tcnative_linux_aarch_64.so\\E");
//        }
//
//        if (Platform.includedIn(Platform.DARWIN_AMD64.class)) {
//            resourceRegistry.addResources("\\QMETA-INF/native/libnetty_resolver_dns_native_macos_x86_64.jnilib\\E");
//            resourceRegistry.addResources("\\QMETA-INF/native/libnetty_tcnative_osx_x86_64.jnilib\\E");
//            resourceRegistry.addResources("\\QMETA-INF/native/libnetty_transport_native_kqueue_x86_64.jnilib\\E");
//        }
//        if (Platform.includedIn(Platform.DARWIN_AARCH64.class)) {
//            // nothing yet...
//        }
//    }

//    @Override
//    public void beforeAnalysis(BeforeAnalysisAccess access) {
//        if (Platform.includedIn(Platform.DARWIN_AMD64.class)) {
////            NativeLibrarySupport.singleton().preregisterUninitializedBuiltinLibrary("apr");
//
//            NativeLibrarySupport.singleton().preregisterUninitializedBuiltinLibrary("netty_resolver_dns_native_macos");
//            PlatformNativeLibrarySupport.singleton().addBuiltinPkgNativePrefix("io.netty.resolver.dns.macos");
////                "io_netty_resolver_dns_macos_MacOSDnsServerAddressStreamProvider");
//
////            NativeLibrarySupport.singleton().preregisterUninitializedBuiltinLibrary("netty_tcnative");
////            NativeLibrarySupport.singleton().preregisterUninitializedBuiltinLibrary("io_netty_internal_tcnative_Library_netty_tcnative");
//            NativeLibrarySupport.singleton().preregisterUninitializedBuiltinLibrary("netty_tcnative_osx_x86_64");
////            NativeLibrarySupport.singleton().preregisterUninitializedBuiltinLibrary("io_netty_internal_tcnative_Library_netty_tcnative_osx_x86_64");
//            PlatformNativeLibrarySupport.singleton().addBuiltinPkgNativePrefix("io.netty.internal.tcnative");
//            PlatformNativeLibrarySupport.singleton().addBuiltinPkgNativePrefix("io_netty_internal_tcnative");
////            PlatformNativeLibrarySupport.singleton().addBuiltinPkgNativePrefix("Java_io_netty_internal_tcnative");
////            PlatformNativeLibrarySupport.singleton().addBuiltinPkgNativePrefix(
////                "io_netty_internal_tcnative_SSL," +
////                    "io_netty_internal_tcnative_SSLContext," +
////                    "io_netty_internal_tcnative_NativeStaticallyReferencedJniMethods," +
////                    "io_netty_internal_tcnative_Buffer,"+
////                    "io_netty_internal_tcnative_Library");
//
//            NativeLibrarySupport.singleton().preregisterUninitializedBuiltinLibrary("netty_transport_native_kqueue");
//            PlatformNativeLibrarySupport.singleton().addBuiltinPkgNativePrefix("io.netty.channel.kqueue");
////                "io_netty_channel_kqueue_BsdSocket," +
////                    "io_netty_channel_kqueue_Native," +
////                    "io_netty_channel_kqueue_KQueueEventArray," +
////                    "io_netty_channel_kqueue_KQueueStaticallyReferencedJniMethods");
//
//            NativeLibraries nativeLibraries = ((FeatureImpl.BeforeAnalysisAccessImpl) access).getNativeLibraries();
//            nativeLibraries.addStaticJniLibrary("netty_resolver_dns_native_macos");
////            nativeLibraries.addStaticJniLibrary("netty_tcnative");
////            nativeLibraries.addStaticJniLibrary("io_netty_internal_tcnative_Library_netty_tcnative");
//            nativeLibraries.addStaticJniLibrary("netty_tcnative_osx_x86_64");
////            nativeLibraries.addStaticJniLibrary("io_netty_internal_tcnative_Library_netty_tcnative_osx_x86_64");
//
//            nativeLibraries.addStaticJniLibrary("netty_transport_native_kqueue");
//
////            nativeLibraries.addStaticJniLibrary("apr");
////            nativeLibraries.addDynamicNonJniLibrary("libnetty_tcnative_osx_x86_64.jnilib");
////            nativeLibraries.addDynamicNonJniLibrary("libnetty_transport_native_kqueue_x86_64.jnilib");
//        }
//    }
}
