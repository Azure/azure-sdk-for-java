/*
 * Copyright (C) 2009 The Guava Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/*
 * Portions Copyright (c) Microsoft Corporation
 */

package com.azure.cosmos.implementation.guava25.base;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Locale;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;


/**
 * Methods factored out so that they can be emulated differently in GWT.
 *
 * @author Jesse Wilson
 */
final class Platform {
  private static final Logger logger = Logger.getLogger(Platform.class.getName());
  private static final PatternCompiler patternCompiler = loadPatternCompiler();

  private Platform() {}

  /** Calls {@link System#nanoTime()}. */
  static long systemNanoTime() {
    return System.nanoTime();
  }

  static CharMatcher precomputeCharMatcher(CharMatcher matcher) {
    return matcher.precomputedInternal();
  }

  static <T extends Enum<T>> Optional<T> getEnumIfPresent(Class<T> enumClass, String value) {
    WeakReference<? extends Enum<?>> ref = Enums.getEnumConstants(enumClass).get(value);
    return ref == null ? Optional.<T>absent() : Optional.of(enumClass.cast(ref.get()));
  }

  static String formatCompact4Digits(double value) {
    return String.format(Locale.ROOT, "%.4g", value);
  }

  static boolean stringIsNullOrEmpty(String string) {
    return string == null || string.isEmpty();
  }

  static String nullToEmpty(String string) {
    return (string == null) ? "" : string;
  }

  static String emptyToNull(String string) {
    return stringIsNullOrEmpty(string) ? null : string;
  }

  static CommonPattern compilePattern(String pattern) {
    Preconditions.checkNotNull(pattern);
    return patternCompiler.compile(pattern);
  }

  static boolean usingJdkPatternCompiler() {
    return patternCompiler instanceof JdkPatternCompiler;
  }

  private static PatternCompiler loadPatternCompiler() {
    ServiceLoader<PatternCompiler> loader = ServiceLoader.load(PatternCompiler.class);
    // Returns the first PatternCompiler that loads successfully.
    try {
      for (Iterator<PatternCompiler> it = loader.iterator(); it.hasNext(); ) {
        try {
          return it.next();
        } catch (ServiceConfigurationError e) {
          logPatternCompilerError(e);
        }
      }
    } catch (ServiceConfigurationError e) { // from hasNext()
      logPatternCompilerError(e);
    }
    // Fall back to the JDK regex library.
    return new JdkPatternCompiler();
  }

  private static void logPatternCompilerError(ServiceConfigurationError e) {
    logger.log(Level.WARNING, "Error loading regex compiler, falling back to next option", e);
  }

  private static final class JdkPatternCompiler implements PatternCompiler {
    @Override
    public CommonPattern compile(String pattern) {
      return new JdkPattern(Pattern.compile(pattern));
    }
  }
}
