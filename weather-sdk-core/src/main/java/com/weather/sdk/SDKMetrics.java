/*
 * Copyright © 2025 by vyacheslav.v.pl@yandex.ru
 *
 * This code is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit
 * https://creativecommons.org/licenses/by-nc-nd/4.0/
 */
package com.weather.sdk;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable snapshot of SDK performance metrics and statistics.
 *
 * <p>This class provides operational metrics including:
 *
 * <ul>
 *   <li>Total API requests made
 *   <li>Cache hit/miss statistics
 *   <li>API success/failure rates
 *   <li>Average response times
 *   <li>Last API call timestamp
 * </ul>
 *
 * <p><strong>Thread Safety:</strong> This class is immutable and thread-safe. Instances represent a
 * point-in-time snapshot and will not be updated.
 *
 * @since 1.0.0
 */
public final class SDKMetrics {

  /** Total number of API requests made. */
  private final long totalRequests;

  /** Number of successful API requests. */
  private final long successCount;

  /** Number of failed API requests. */
  private final long failedRequests;

  /** Number of cache hits. */
  private final long cacheHits;

  /** Number of cache misses. */
  private final long cacheMisses;

  /** Average response time in milliseconds. */
  private final double avgResponseMs;

  /** Timestamp of the last API call, or null if no calls made. */
  private final Instant lastApiCallTime;

  /**
   * Creates a new SDKMetrics instance with the specified values.
   *
   * @param totalRequests total number of API requests made
   * @param successCount number of successful API requests
   * @param failedRequests number of failed API requests
   * @param cacheHits number of cache hits
   * @param cacheMisses number of cache misses
   * @param avgResponseMs average response time in milliseconds
   * @param lastApiCallTime timestamp of the last API call, may be null
   */
  public SDKMetrics(
      final long totalRequests,
      final long successCount,
      final long failedRequests,
      final long cacheHits,
      final long cacheMisses,
      final double avgResponseMs,
      final Instant lastApiCallTime) {
    this.totalRequests = totalRequests;
    this.successCount = successCount;
    this.failedRequests = failedRequests;
    this.cacheHits = cacheHits;
    this.cacheMisses = cacheMisses;
    this.avgResponseMs = avgResponseMs;
    this.lastApiCallTime = lastApiCallTime;
  }

  /**
   * Returns the total number of API requests made.
   *
   * @return total request count
   */
  public long getTotalRequests() {
    return totalRequests;
  }

  /**
   * Returns the number of successful API requests.
   *
   * @return successful request count
   */
  public long getSuccessfulRequests() {
    return successCount;
  }

  /**
   * Returns the number of failed API requests.
   *
   * @return failed request count
   */
  public long getFailedRequests() {
    return failedRequests;
  }

  /**
   * Returns the number of cache hits.
   *
   * @return cache hit count
   */
  public long getCacheHits() {
    return cacheHits;
  }

  /**
   * Returns the number of cache misses.
   *
   * @return cache miss count
   */
  public long getCacheMisses() {
    return cacheMisses;
  }

  /**
   * Returns the average response time in milliseconds.
   *
   * @return average response time in ms
   */
  public double getAverageResponseTimeMs() {
    return avgResponseMs;
  }

  /**
   * Returns the timestamp of the last API call.
   *
   * @return last API call timestamp, or null if no calls have been made
   */
  public Instant getLastApiCallTime() {
    return lastApiCallTime;
  }

  /**
   * Returns the cache hit rate as a percentage (0.0 to 100.0).
   *
   * @return cache hit rate percentage, or 0.0 if no cache operations
   */
  public double getCacheHitRate() {
    final long totalCacheOps = cacheHits + cacheMisses;
    final double result;
    if (totalCacheOps == 0) {
      result = 0.0;
    } else {
      result = cacheHits * 100.0 / totalCacheOps;
    }
    return result;
  }

  /**
   * Returns the API success rate as a percentage (0.0 to 100.0).
   *
   * @return success rate percentage, or 0.0 if no requests made
   */
  public double getSuccessRate() {
    final double result;
    if (totalRequests == 0) {
      result = 0.0;
    } else {
      result = successCount * 100.0 / totalRequests;
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    final boolean result;
    if (this == obj) {
      result = true;
    } else if (obj == null || getClass() != obj.getClass()) {
      result = false;
    } else {
      final SDKMetrics other = (SDKMetrics) obj;
      result =
          totalRequests == other.totalRequests
              && successCount == other.successCount
              && failedRequests == other.failedRequests
              && cacheHits == other.cacheHits
              && cacheMisses == other.cacheMisses
              && Double.compare(other.avgResponseMs, avgResponseMs) == 0
              && Objects.equals(lastApiCallTime, other.lastApiCallTime);
    }
    return result;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        totalRequests,
        successCount,
        failedRequests,
        cacheHits,
        cacheMisses,
        avgResponseMs,
        lastApiCallTime);
  }

  @Override
  public String toString() {
    return String.format(
        "SDKMetrics{totalRequests=%d, successCount=%d, failedRequests=%d, "
            + "cacheHits=%d, cacheMisses=%d, avgResponseMs=%.2fms, lastCall=%s}",
        totalRequests,
        successCount,
        failedRequests,
        cacheHits,
        cacheMisses,
        avgResponseMs,
        lastApiCallTime);
  }
}
