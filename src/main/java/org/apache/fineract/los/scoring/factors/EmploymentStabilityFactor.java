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

package org.apache.fineract.los.scoring.factors;

import lombok.RequiredArgsConstructor;
import org.apache.fineract.los.scoring.ScoringFactor;
import org.apache.fineract.los.scoring.ScoringWeightsProperties;
import org.apache.fineract.los.scoring.model.ApplicantScoringProfile;
import org.apache.fineract.los.scoring.model.FactorScore;
import org.springframework.stereotype.Component;

/**
 * Evaluates employment stability as a credit scoring factor.
 *
 * <p>Combines employment status and duration to assess income reliability. Stable formal employment
 * over a longer tenure signals a lower risk of income disruption during the loan term.
 *
 * <p>Status multipliers applied to duration-band base scores:
 *
 * <ul>
 *   <li>EMPLOYED &rarr; 1.0 (full weight — formal employment, most predictable income)
 *   <li>SELF_EMPLOYED &rarr; 0.8 (slightly lower — income variability)
 *   <li>INFORMAL &rarr; 0.6 (informal sector — higher income uncertainty)
 *   <li>UNEMPLOYED or unknown &rarr; 0 points regardless of duration
 * </ul>
 *
 * <p>Duration bands (months in current employment):
 *
 * <ul>
 *   <li>&ge; 24 months &rarr; full base points
 *   <li>&ge; 12 months &rarr; 75% of base points
 *   <li>&ge; 6 months &rarr; 50% of base points
 *   <li>&ge; 3 months &rarr; 25% of base points
 *   <li>&lt; 3 months &rarr; 0 base points
 * </ul>
 *
 * <p>Final score = floor(statusMultiplier * durationPoints), capped at {@link #maxPoints()}.
 */
@Component
@RequiredArgsConstructor
public class EmploymentStabilityFactor implements ScoringFactor {

  private static final String FACTOR_NAME = "employment-stability";

  private final ScoringWeightsProperties weights;

  @Override
  public FactorScore score(ApplicantScoringProfile profile) {
    final int max = maxPoints();

    String status = profile.getEmploymentStatus();
    Integer durationMonths = profile.getEmploymentDurationMonths();

    // Unemployed or missing status — no employment stability
    if (status == null || status.isBlank() || "UNEMPLOYED".equalsIgnoreCase(status.trim())) {
      return FactorScore.builder()
          .points(0)
          .maxPoints(max)
          .explanation("Applicant is unemployed or employment status is unknown.")
          .build();
    }

    double statusMultiplier = resolveStatusMultiplier(status.trim().toUpperCase());
    int months = (durationMonths != null && durationMonths > 0) ? durationMonths : 0;

    int durationPoints;
    String durationLabel;

    if (months >= 24) {
      durationPoints = max;
      durationLabel = months + " months — long-term stable employment.";
    } else if (months >= 12) {
      durationPoints = (int) Math.round(max * 0.75);
      durationLabel = months + " months — established employment.";
    } else if (months >= 6) {
      durationPoints = (int) Math.round(max * 0.50);
      durationLabel = months + " months — moderate employment tenure.";
    } else if (months >= 3) {
      durationPoints = (int) Math.round(max * 0.25);
      durationLabel = months + " months — short employment tenure.";
    } else {
      durationPoints = 0;
      durationLabel = months + " months — insufficient employment history.";
    }

    int points = Math.min((int) Math.floor(statusMultiplier * durationPoints), max);

    String explanation =
        String.format(
            "%s applicant with %s Status multiplier: %.0f%%.",
            formatStatus(status), durationLabel, statusMultiplier * 100);

    return FactorScore.builder().points(points).maxPoints(max).explanation(explanation).build();
  }

  private double resolveStatusMultiplier(String status) {
    return switch (status) {
      case "EMPLOYED" -> 1.0;
      case "SELF_EMPLOYED" -> 0.8;
      case "INFORMAL" -> 0.6;
      default -> 0.0;
    };
  }

  private String formatStatus(String status) {
    if (status == null) return "Unknown";
    return switch (status.trim().toUpperCase()) {
      case "EMPLOYED" -> "Formally employed";
      case "SELF_EMPLOYED" -> "Self-employed";
      case "INFORMAL" -> "Informally employed";
      default -> status;
    };
  }

  @Override
  public int maxPoints() {
    return weights.getEmploymentStability();
  }

  @Override
  public String factorName() {
    return FACTOR_NAME;
  }
}
