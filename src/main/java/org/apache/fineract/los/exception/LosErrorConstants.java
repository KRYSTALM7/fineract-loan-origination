/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.fineract.los.exception;

/**
 * Centralised error codes and message constants for the Loan Origination Service.
 *
 * <p>All exception messages must reference constants from this class — no hardcoded strings in
 * business logic. This ensures consistent error responses across all layers and makes message
 * changes a single-point update.
 */
public final class LosErrorConstants {

  private LosErrorConstants() {
    // Utility class — prevent instantiation
  }

  // ─────────────────────────────────────────────────────────
  // Error Codes
  // ─────────────────────────────────────────────────────────

  /** Error code prefix for all LOS validation errors. */
  public static final String ERR_VALIDATION = "los.error.validation";

  /** Error code for invalid state transition attempts. */
  public static final String ERR_INVALID_TRANSITION = "los.error.state.invalid-transition";

  /** Error code for terminal state transition attempts. */
  public static final String ERR_TERMINAL_STATE = "los.error.state.terminal";

  /** Error code for null input violations. */
  public static final String ERR_NULL_INPUT = "los.error.input.null";

  /** Error code for uninitialised entity state. */
  public static final String ERR_UNINITIALISED_STATUS = "los.error.entity.uninitialised-status";

  // ─────────────────────────────────────────────────────────
  // State Machine Messages
  // ─────────────────────────────────────────────────────────

  /** Message when application argument is null. */
  public static final String MSG_APPLICATION_NULL = "Application must not be null";

  /** Message when target status argument is null. */
  public static final String MSG_TARGET_STATUS_NULL = "Target status must not be null";

  /** Message when application status is uninitialised. */
  public static final String MSG_STATUS_UNINITIALISED =
      "Application status must not be null — " + "ensure entity is properly initialised";

  // ─────────────────────────────────────────────────────────
  // Transition Message Templates
  // ─────────────────────────────────────────────────────────

  /** Template for invalid transition error message. Parameters: fromStatus, toStatus. */
  public static final String MSG_INVALID_TRANSITION_TEMPLATE =
      "Invalid state transition: cannot move from [%s] to [%s]. "
          + "Check LoanOriginationStateMachine "
          + "for valid transitions.";

  /** Template for terminal state error message. Parameters: applicationRef, currentStatus. */
  public static final String MSG_TERMINAL_STATE_TEMPLATE =
      "Application [%s] is in terminal state [%s]. " + "No further transitions are permitted.";
}
