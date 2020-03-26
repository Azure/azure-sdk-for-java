/*
 * Copyright (C) 2008 The Guava Authors
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

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;
import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkState;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * An object that measures elapsed time in nanoseconds. It is useful to measure elapsed time using
 * this class instead of direct calls to {@link System#nanoTime} for a few reasons:
 *
 * <ul>
 *   <li>An alternate time source can be substituted, for testing or performance reasons.
 *   <li>As documented by {@code nanoTime}, the value returned has no absolute meaning, and can only
 *       be interpreted as relative to another timestamp returned by {@code nanoTime} at a different
 *       time. {@code Stopwatch} is a more effective abstraction because it exposes only these
 *       relative values, not the absolute ones.
 * </ul>
 *
 * <p>Basic usage:
 *
 * <pre>{@code
 * Stopwatch stopwatch = Stopwatch.createStarted();
 * doSomething();
 * stopwatch.stop(); // optional
 *
 * Duration duration = stopwatch.elapsed();
 *
 * log.info("time: " + stopwatch); // formatted string like "12.3 ms"
 * }</pre>
 *
 * <p>Stopwatch methods are not idempotent; it is an error to start or stop a stopwatch that is
 * already in the desired state.
 *
 * <p>When testing code that uses this class, use {@link #createUnstarted(Ticker)} or {@link
 * #createStarted(Ticker)} to supply a fake or mock ticker. This allows you to simulate any valid
 * behavior of the stopwatch.
 *
 * <p><b>Note:</b> This class is not thread-safe.
 *
 * <p><b>Warning for Android users:</b> a stopwatch with default behavior may not continue to keep
 * time while the device is asleep. Instead, create one like this:
 *
 * <pre>{@code
 * Stopwatch.createStarted(
 *      new Ticker() {
 *        public long read() {
 *          return android.os.SystemClock.elapsedRealtimeNanos();
 *        }
 *      });
 * }</pre>
 *
 * @author Kevin Bourrillion
 * @since 10.0
 */
public final class Stopwatch {
  private final Ticker ticker;
  private boolean isRunning;
  private long elapsedNanos;
  private long startTick;

  /**
   * Creates (but does not start) a new stopwatch using {@link System#nanoTime} as its time source.
   *
   * @since 15.0
   */
  public static Stopwatch createUnstarted() {
    return new Stopwatch();
  }

  /**
   * Creates (but does not start) a new stopwatch, using the specified time source.
   *
   * @since 15.0
   */
  public static Stopwatch createUnstarted(Ticker ticker) {
    return new Stopwatch(ticker);
  }

  /**
   * Creates (and starts) a new stopwatch using {@link System#nanoTime} as its time source.
   *
   * @since 15.0
   */
  public static Stopwatch createStarted() {
    return new Stopwatch().start();
  }

  /**
   * Creates (and starts) a new stopwatch, using the specified time source.
   *
   * @since 15.0
   */
  public static Stopwatch createStarted(Ticker ticker) {
    return new Stopwatch(ticker).start();
  }

  Stopwatch() {
    this.ticker = Ticker.systemTicker();
  }

  Stopwatch(Ticker ticker) {
    this.ticker = checkNotNull(ticker, "ticker");
  }

  /**
   * Returns {@code true} if {@link #start()} has been called on this stopwatch, and {@link #stop()}
   * has not been called since the last call to {@code start()}.
   */
  public boolean isRunning() {
    return isRunning;
  }

  /**
   * Starts the stopwatch.
   *
   * @return this {@code Stopwatch} instance
   * @throws IllegalStateException if the stopwatch is already running.
   */
  public Stopwatch start() {
    checkState(!isRunning, "This stopwatch is already running.");
    isRunning = true;
    startTick = ticker.read();
    return this;
  }

  /**
   * Stops the stopwatch. Future reads will return the fixed duration that had elapsed up to this
   * point.
   *
   * @return this {@code Stopwatch} instance
   * @throws IllegalStateException if the stopwatch is already stopped.
   */
  public Stopwatch stop() {
    long tick = ticker.read();
    checkState(isRunning, "This stopwatch is already stopped.");
    isRunning = false;
    elapsedNanos += tick - startTick;
    return this;
  }

  /**
   * Sets the elapsed time for this stopwatch to zero, and places it in a stopped state.
   *
   * @return this {@code Stopwatch} instance
   */
  public Stopwatch reset() {
    elapsedNanos = 0;
    isRunning = false;
    return this;
  }

  private long elapsedNanos() {
    return isRunning ? ticker.read() - startTick + elapsedNanos : elapsedNanos;
  }

  /**
   * Returns the current elapsed time shown on this stopwatch, expressed in the desired time unit,
   * with any fraction rounded down.
   *
   * <p><b>Note:</b> the overhead of measurement can be more than a microsecond, so it is generally
   * not useful to specify {@link TimeUnit#NANOSECONDS} precision here.
   *
   * <p>It is generally not a good idea to use an ambiguous, unitless {@code long} to represent
   * elapsed time. Therefore, we recommend using {@link #elapsed()} instead, which returns a
   * strongly-typed {@link Duration} instance.
   *
   * @since 14.0 (since 10.0 as {@code elapsedTime()})
   */
  public long elapsed(TimeUnit desiredUnit) {
    return desiredUnit.convert(elapsedNanos(), NANOSECONDS);
  }

  /**
   * Returns the current elapsed time shown on this stopwatch as a {@link Duration}. Unlike {@link
   * #elapsed(TimeUnit)}, this method does not lose any precision due to rounding.
   *
   * @since 22.0
   */
  public Duration elapsed() {
    return Duration.ofNanos(elapsedNanos());
  }

  /** Returns a string representation of the current elapsed time. */
  @Override
  public String toString() {
    long nanos = elapsedNanos();

    TimeUnit unit = chooseUnit(nanos);
    double value = (double) nanos / NANOSECONDS.convert(1, unit);

    // Too bad this functionality is not exposed as a regular method call
    return Platform.formatCompact4Digits(value) + " " + abbreviate(unit);
  }

  private static TimeUnit chooseUnit(long nanos) {
    if (DAYS.convert(nanos, NANOSECONDS) > 0) {
      return DAYS;
    }
    if (HOURS.convert(nanos, NANOSECONDS) > 0) {
      return HOURS;
    }
    if (MINUTES.convert(nanos, NANOSECONDS) > 0) {
      return MINUTES;
    }
    if (SECONDS.convert(nanos, NANOSECONDS) > 0) {
      return SECONDS;
    }
    if (MILLISECONDS.convert(nanos, NANOSECONDS) > 0) {
      return MILLISECONDS;
    }
    if (MICROSECONDS.convert(nanos, NANOSECONDS) > 0) {
      return MICROSECONDS;
    }
    return NANOSECONDS;
  }

  private static String abbreviate(TimeUnit unit) {
    switch (unit) {
      case NANOSECONDS:
        return "ns";
      case MICROSECONDS:
        return "\u03bcs"; // μs
      case MILLISECONDS:
        return "ms";
      case SECONDS:
        return "s";
      case MINUTES:
        return "min";
      case HOURS:
        return "h";
      case DAYS:
        return "d";
      default:
        throw new AssertionError();
    }
  }
}
