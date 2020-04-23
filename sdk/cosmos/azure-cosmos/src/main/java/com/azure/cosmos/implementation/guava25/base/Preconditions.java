/*
 * Copyright (C) 2007 The Guava Authors
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



/**
 * Static convenience methods that help a method or constructor check whether it was invoked
 * correctly (that is, whether its <i>preconditions</i> were met).
 *
 * <p>If the precondition is not met, the {@code Preconditions} method throws an unchecked exception
 * of a specified type, which helps the method in which the exception was thrown communicate that
 * its caller has made a mistake. This allows constructs such as
 *
 * <pre>{@code
 * public static double sqrt(double value) {
 *   if (value < 0) {
 *     throw new IllegalArgumentException("input is negative: " + value);
 *   }
 *   // calculate square root
 * }
 * }</pre>
 *
 * <p>to be replaced with the more compact
 *
 * <pre>{@code
 * public static double sqrt(double value) {
 *   checkArgument(value >= 0, "input is negative: %s", value);
 *   // calculate square root
 * }
 * }</pre>
 *
 * <p>so that a hypothetical bad caller of this method, such as:
 *
 * <pre>{@code
 *   void exampleBadCaller() {
 *     double d = sqrt(-1.0);
 * }
 * }</pre>
 *
 * <p>would be flagged as having called {@code sqrt()} with an illegal argument.
 *
 * <h3>Performance</h3>
 *
 * <p>Avoid passing message arguments that are expensive to compute; your code will always compute
 * them, even though they usually won't be needed. If you have such arguments, use the conventional
 * if/throw idiom instead.
 *
 * <p>Depending on your message arguments, memory may be allocated for boxing and varargs array
 * creation. However, the methods of this class have a large number of overloads that prevent such
 * allocations in many common cases.
 *
 * <p>The message string is not formatted unless the exception will be thrown, so the cost of the
 * string formatting itself should not be a concern.
 *
 * <p>As with any performance concerns, you should consider profiling your code (in a production
 * environment if possible) before spending a lot of effort on tweaking a particular element.
 *
 * <h3>Other types of preconditions</h3>
 *
 * <p>Not every type of precondition failure is supported by these methods. Continue to throw
 * standard JDK exceptions such as {@link java.util.NoSuchElementException} or {@link
 * UnsupportedOperationException} in the situations they are intended for.
 *
 * <h3>Non-preconditions</h3>
 *
 * <p>It is of course possible to use the methods of this class to check for invalid conditions
 * which are <i>not the caller's fault</i>. Doing so is <b>not recommended</b> because it is
 * misleading to future readers of the code and of stack traces. See <a
 * href="https://github.com/google/guava/wiki/ConditionalFailuresExplained">Conditional failures
 * explained</a> in the Guava User Guide for more advice. Notably, {@link Verify} offers assertions
 * similar to those in this class for non-precondition checks.
 *
 * <h3>{@code java.util.Objects.requireNonNull()}</h3>
 *
 * <p>Projects which use {@code com.google.common} should generally avoid the use of {@link
 * java.util.Objects#requireNonNull(Object)}. Instead, use whichever of {@link
 * #checkNotNull(Object)} or {@link Verify#verifyNotNull(Object)} is appropriate to the situation.
 * (The same goes for the message-accepting overloads.)
 *
 * <h3>Only {@code %s} is supported</h3>
 *
 * <p>In {@code Preconditions} error message template strings, only the {@code "%s"} specifier is
 * supported, not the full range of {@link java.util.Formatter} specifiers.
 *
 * <h3>More information</h3>
 *
 * <p>See the Guava User Guide on <a
 * href="https://github.com/google/guava/wiki/PreconditionsExplained">using {@code
 * Preconditions}</a>.
 *
 * @author Kevin Bourrillion
 * @since 2.0
 */
public final class Preconditions {
  private Preconditions() {}

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * @param expression a boolean expression
   * @throws IllegalArgumentException if {@code expression} is false
   */
  public static void checkArgument(boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * @param expression a boolean expression
   * @param errorMessage the exception message to use if the check fails; will be converted to a
   *     string using {@link String#valueOf(Object)}
   * @throws IllegalArgumentException if {@code expression} is false
   */
  public static void checkArgument(boolean expression, Object errorMessage) {
    if (!expression) {
      throw new IllegalArgumentException(String.valueOf(errorMessage));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * @param expression a boolean expression
   * @param errorMessageTemplate a template for the exception message should the check fail. The
   *     message is formed by replacing each {@code %s} placeholder in the template with an
   *     argument. These are matched by position - the first {@code %s} gets {@code
   *     errorMessageArgs[0]}, etc. Unmatched arguments will be appended to the formatted message in
   *     square braces. Unmatched placeholders will be left as-is.
   * @param errorMessageArgs the arguments to be substituted into the message template. Arguments
   *     are converted to strings using {@link String#valueOf(Object)}.
   * @throws IllegalArgumentException if {@code expression} is false
   */
  public static void checkArgument(
      boolean expression,
      String errorMessageTemplate,
      Object... errorMessageArgs) {
    if (!expression) {
      throw new IllegalArgumentException(format(errorMessageTemplate, errorMessageArgs));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(boolean b, String errorMessageTemplate, char p1) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(boolean b, String errorMessageTemplate, int p1) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(boolean b, String errorMessageTemplate, long p1) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, Object p1) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, char p1, char p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, char p1, int p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, char p1, long p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, char p1, Object p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, int p1, char p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, int p1, int p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, int p1, long p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, int p1, Object p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, long p1, char p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, long p1, int p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, long p1, long p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, long p1, Object p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, Object p1, char p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, Object p1, int p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b, String errorMessageTemplate, Object p1, long p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b,
      String errorMessageTemplate,
      Object p1,
      Object p2) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b,
      String errorMessageTemplate,
      Object p1,
      Object p2,
      Object p3) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2, p3));
    }
  }

  /**
   * Ensures the truth of an expression involving one or more parameters to the calling method.
   *
   * <p>See {@link #checkArgument(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkArgument(
      boolean b,
      String errorMessageTemplate,
      Object p1,
      Object p2,
      Object p3,
      Object p4) {
    if (!b) {
      throw new IllegalArgumentException(format(errorMessageTemplate, p1, p2, p3, p4));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * @param expression a boolean expression
   * @throws IllegalStateException if {@code expression} is false
   * @see Verify#verify Verify.verify()
   */
  public static void checkState(boolean expression) {
    if (!expression) {
      throw new IllegalStateException();
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * @param expression a boolean expression
   * @param errorMessage the exception message to use if the check fails; will be converted to a
   *     string using {@link String#valueOf(Object)}
   * @throws IllegalStateException if {@code expression} is false
   * @see Verify#verify Verify.verify()
   */
  public static void checkState(boolean expression, Object errorMessage) {
    if (!expression) {
      throw new IllegalStateException(String.valueOf(errorMessage));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * @param expression a boolean expression
   * @param errorMessageTemplate a template for the exception message should the check fail. The
   *     message is formed by replacing each {@code %s} placeholder in the template with an
   *     argument. These are matched by position - the first {@code %s} gets {@code
   *     errorMessageArgs[0]}, etc. Unmatched arguments will be appended to the formatted message in
   *     square braces. Unmatched placeholders will be left as-is.
   * @param errorMessageArgs the arguments to be substituted into the message template. Arguments
   *     are converted to strings using {@link String#valueOf(Object)}.
   * @throws IllegalStateException if {@code expression} is false
   * @see Verify#verify Verify.verify()
   */
  public static void checkState(
      boolean expression,
      String errorMessageTemplate,
      Object... errorMessageArgs) {
    if (!expression) {
      throw new IllegalStateException(format(errorMessageTemplate, errorMessageArgs));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(boolean b, String errorMessageTemplate, char p1) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(boolean b, String errorMessageTemplate, int p1) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(boolean b, String errorMessageTemplate, long p1) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, Object p1) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, char p1, char p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, char p1, int p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, char p1, long p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, char p1, Object p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, int p1, char p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, int p1, int p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, int p1, long p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, int p1, Object p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, long p1, char p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, long p1, int p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, long p1, long p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, long p1, Object p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, Object p1, char p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, Object p1, int p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b, String errorMessageTemplate, Object p1, long p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b,
      String errorMessageTemplate,
      Object p1,
      Object p2) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b,
      String errorMessageTemplate,
      Object p1,
      Object p2,
      Object p3) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2, p3));
    }
  }

  /**
   * Ensures the truth of an expression involving the state of the calling instance, but not
   * involving any parameters to the calling method.
   *
   * <p>See {@link #checkState(boolean, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static void checkState(
      boolean b,
      String errorMessageTemplate,
      Object p1,
      Object p2,
      Object p3,
      Object p4) {
    if (!b) {
      throw new IllegalStateException(format(errorMessageTemplate, p1, p2, p3, p4));
    }
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * @param reference an object reference
   * @return the non-null reference that was validated
   * @throws NullPointerException if {@code reference} is null
   * @see Verify#verifyNotNull Verify.verifyNotNull()
   */
  public static <T> T checkNotNull(T reference) {
    if (reference == null) {
      throw new NullPointerException();
    }
    return reference;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * @param reference an object reference
   * @param errorMessage the exception message to use if the check fails; will be converted to a
   *     string using {@link String#valueOf(Object)}
   * @return the non-null reference that was validated
   * @throws NullPointerException if {@code reference} is null
   * @see Verify#verifyNotNull Verify.verifyNotNull()
   */
  public static <T> T checkNotNull(T reference, Object errorMessage) {
    if (reference == null) {
      throw new NullPointerException(String.valueOf(errorMessage));
    }
    return reference;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * @param reference an object reference
   * @param errorMessageTemplate a template for the exception message should the check fail. The
   *     message is formed by replacing each {@code %s} placeholder in the template with an
   *     argument. These are matched by position - the first {@code %s} gets {@code
   *     errorMessageArgs[0]}, etc. Unmatched arguments will be appended to the formatted message in
   *     square braces. Unmatched placeholders will be left as-is.
   * @param errorMessageArgs the arguments to be substituted into the message template. Arguments
   *     are converted to strings using {@link String#valueOf(Object)}.
   * @return the non-null reference that was validated
   * @throws NullPointerException if {@code reference} is null
   * @see Verify#verifyNotNull Verify.verifyNotNull()
   */
  public static <T> T checkNotNull(
      T reference,
      String errorMessageTemplate,
      Object... errorMessageArgs) {
    if (reference == null) {
      throw new NullPointerException(format(errorMessageTemplate, errorMessageArgs));
    }
    return reference;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(T obj, String errorMessageTemplate, char p1) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(T obj, String errorMessageTemplate, int p1) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(T obj, String errorMessageTemplate, long p1) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, Object p1) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, char p1, char p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, char p1, int p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, char p1, long p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, char p1, Object p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, int p1, char p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, int p1, int p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, int p1, long p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, int p1, Object p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, long p1, char p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, long p1, int p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, long p1, long p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, long p1, Object p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, Object p1, char p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, Object p1, int p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj, String errorMessageTemplate, Object p1, long p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj,
      String errorMessageTemplate,
      Object p1,
      Object p2) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj,
      String errorMessageTemplate,
      Object p1,
      Object p2,
      Object p3) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2, p3));
    }
    return obj;
  }

  /**
   * Ensures that an object reference passed as a parameter to the calling method is not null.
   *
   * <p>See {@link #checkNotNull(Object, String, Object...)} for details.
   *
   * @since 20.0 (varargs overload since 2.0)
   */
  public static <T> T checkNotNull(
      T obj,
      String errorMessageTemplate,
      Object p1,
      Object p2,
      Object p3,
      Object p4) {
    if (obj == null) {
      throw new NullPointerException(format(errorMessageTemplate, p1, p2, p3, p4));
    }
    return obj;
  }

  /*
   * All recent hotspots (as of 2009) *really* like to have the natural code
   *
   * if (guardExpression) {
   *    throw new BadException(messageExpression);
   * }
   *
   * refactored so that messageExpression is moved to a separate String-returning method.
   *
   * if (guardExpression) {
   *    throw new BadException(badMsg(...));
   * }
   *
   * The alternative natural refactorings into void or Exception-returning methods are much slower.
   * This is a big deal - we're talking factors of 2-8 in microbenchmarks, not just 10-20%. (This is
   * a hotspot optimizer bug, which should be fixed, but that's a separate, big project).
   *
   * The coding pattern above is heavily used in java.util, e.g. in ArrayList. There is a
   * RangeCheckMicroBenchmark in the JDK that was used to test this.
   *
   * But the methods in this class want to throw different exceptions, depending on the args, so it
   * appears that this pattern is not directly applicable. But we can use the ridiculous, devious
   * trick of throwing an exception in the middle of the construction of another exception. Hotspot
   * is fine with that.
   */

  /**
   * Ensures that {@code index} specifies a valid <i>element</i> in an array, list or string of size
   * {@code size}. An element index may range from zero, inclusive, to {@code size}, exclusive.
   *
   * @param index a user-supplied index identifying an element of an array, list or string
   * @param size the size of that array, list or string
   * @return the value of {@code index}
   * @throws IndexOutOfBoundsException if {@code index} is negative or is not less than {@code size}
   * @throws IllegalArgumentException if {@code size} is negative
   */
  public static int checkElementIndex(int index, int size) {
    return checkElementIndex(index, size, "index");
  }

  /**
   * Ensures that {@code index} specifies a valid <i>element</i> in an array, list or string of size
   * {@code size}. An element index may range from zero, inclusive, to {@code size}, exclusive.
   *
   * @param index a user-supplied index identifying an element of an array, list or string
   * @param size the size of that array, list or string
   * @param desc the text to use to describe this index in an error message
   * @return the value of {@code index}
   * @throws IndexOutOfBoundsException if {@code index} is negative or is not less than {@code size}
   * @throws IllegalArgumentException if {@code size} is negative
   */
  public static int checkElementIndex(int index, int size, String desc) {
    // Carefully optimized for execution by hotspot (explanatory comment above)
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException(badElementIndex(index, size, desc));
    }
    return index;
  }

  private static String badElementIndex(int index, int size, String desc) {
    if (index < 0) {
      return format("%s (%s) must not be negative", desc, index);
    } else if (size < 0) {
      throw new IllegalArgumentException("negative size: " + size);
    } else { // index >= size
      return format("%s (%s) must be less than size (%s)", desc, index, size);
    }
  }

  /**
   * Ensures that {@code index} specifies a valid <i>position</i> in an array, list or string of
   * size {@code size}. A position index may range from zero to {@code size}, inclusive.
   *
   * @param index a user-supplied index identifying a position in an array, list or string
   * @param size the size of that array, list or string
   * @return the value of {@code index}
   * @throws IndexOutOfBoundsException if {@code index} is negative or is greater than {@code size}
   * @throws IllegalArgumentException if {@code size} is negative
   */
  public static int checkPositionIndex(int index, int size) {
    return checkPositionIndex(index, size, "index");
  }

  /**
   * Ensures that {@code index} specifies a valid <i>position</i> in an array, list or string of
   * size {@code size}. A position index may range from zero to {@code size}, inclusive.
   *
   * @param index a user-supplied index identifying a position in an array, list or string
   * @param size the size of that array, list or string
   * @param desc the text to use to describe this index in an error message
   * @return the value of {@code index}
   * @throws IndexOutOfBoundsException if {@code index} is negative or is greater than {@code size}
   * @throws IllegalArgumentException if {@code size} is negative
   */
  public static int checkPositionIndex(int index, int size, String desc) {
    // Carefully optimized for execution by hotspot (explanatory comment above)
    if (index < 0 || index > size) {
      throw new IndexOutOfBoundsException(badPositionIndex(index, size, desc));
    }
    return index;
  }

  private static String badPositionIndex(int index, int size, String desc) {
    if (index < 0) {
      return format("%s (%s) must not be negative", desc, index);
    } else if (size < 0) {
      throw new IllegalArgumentException("negative size: " + size);
    } else { // index > size
      return format("%s (%s) must not be greater than size (%s)", desc, index, size);
    }
  }

  /**
   * Ensures that {@code start} and {@code end} specify a valid <i>positions</i> in an array, list
   * or string of size {@code size}, and are in order. A position index may range from zero to
   * {@code size}, inclusive.
   *
   * @param start a user-supplied index identifying a starting position in an array, list or string
   * @param end a user-supplied index identifying a ending position in an array, list or string
   * @param size the size of that array, list or string
   * @throws IndexOutOfBoundsException if either index is negative or is greater than {@code size},
   *     or if {@code end} is less than {@code start}
   * @throws IllegalArgumentException if {@code size} is negative
   */
  public static void checkPositionIndexes(int start, int end, int size) {
    // Carefully optimized for execution by hotspot (explanatory comment above)
    if (start < 0 || end < start || end > size) {
      throw new IndexOutOfBoundsException(badPositionIndexes(start, end, size));
    }
  }

  private static String badPositionIndexes(int start, int end, int size) {
    if (start < 0 || start > size) {
      return badPositionIndex(start, size, "start index");
    }
    if (end < 0 || end > size) {
      return badPositionIndex(end, size, "end index");
    }
    // end < start
    return format("end index (%s) must not be less than start index (%s)", end, start);
  }

  /**
   * Substitutes each {@code %s} in {@code template} with an argument. These are matched by
   * position: the first {@code %s} gets {@code args[0]}, etc. If there are more arguments than
   * placeholders, the unmatched arguments will be appended to the end of the formatted message in
   * square braces.
   *
   * @param template a string containing 0 or more {@code %s} placeholders. null is treated as
   *     "null".
   * @param args the arguments to be substituted into the message template. Arguments are converted
   *     to strings using {@link String#valueOf(Object)}. Arguments can be null.
   */
  // Note that this is somewhat-improperly used from Verify.java as well.
  static String format(String template, Object... args) {
    template = String.valueOf(template); // null -> "null"

    args = args == null ? new Object[] {"(Object[])null"} : args;

    // start substituting the arguments into the '%s' placeholders
    StringBuilder builder = new StringBuilder(template.length() + 16 * args.length);
    int templateStart = 0;
    int i = 0;
    while (i < args.length) {
      int placeholderStart = template.indexOf("%s", templateStart);
      if (placeholderStart == -1) {
        break;
      }
      builder.append(template, templateStart, placeholderStart);
      builder.append(args[i++]);
      templateStart = placeholderStart + 2;
    }
    builder.append(template, templateStart, template.length());

    // if we run out of placeholders, append the extra args in square braces
    if (i < args.length) {
      builder.append(" [");
      builder.append(args[i++]);
      while (i < args.length) {
        builder.append(", ");
        builder.append(args[i++]);
      }
      builder.append(']');
    }

    return builder.toString();
  }
}
