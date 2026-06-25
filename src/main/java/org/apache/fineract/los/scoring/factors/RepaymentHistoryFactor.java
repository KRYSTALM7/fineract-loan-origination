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
 * Evaluates the applicant's prior loan repayment history as a credit scoring factor.
 *
 * <p>Uses repayment track record within Fineract to assess reliability. First-time borrowers with
 * no history receive a neutral score rather than a penalty — consistent with CGAP guidelines for
 * inclusive microfinance lending.
 *
 * <p>Scoring logic:
 *
 * <ol>
 *   <li>If no prior loans exist (both counts null or zero) &rarr; neutral score (50% of max).
 *       First-time borrowers are not penalised.
 *   <li>If prior loans exist, compute success rate: {@code successRate = successful / (successful +
 *       missed)}
 *       <ul>
 *         <li>&ge; 0.95 (near-perfect) &rarr; full points
 *         <li>&ge; 0.80 &rarr; 75% of max points
 *         <li>&ge; 0.60 &rarr; 50% of max points
 *         <li>&ge; 0.40 &rarr; 25% of max points
 *         <li>&lt; 0.40 &rarr; 0 points (poor repayment history)
 *       </ul>
 * </ol>
 */
@Component
@RequiredArgsConstructor
public class RepaymentHistoryFactor implements ScoringFactor {

  private static final String FACTOR_NAME = "repayment-history";

  private final ScoringWeightsProperties weights;

  @Override
  public FactorScore score(ApplicantScoringProfile profile) {
    final int max = maxPoints();

    int successful =
        profile.getSuccessfulRepaymentsCount() != null ? profile.getSuccessfulRepaymentsCount() : 0;
    int missed =
        profile.getMissedRepaymentsCount() != null ? profile.getMissedRepaymentsCount() : 0;

    int total = successful + missed;

    // First-time borrower — no history, neutral score
    if (total == 0) {
      int neutralPoints = (int) Math.round(max * 0.50);
      return FactorScore.builder()
          .points(neutralPoints)
          .maxPoints(max)
          .explanation("No prior loan history — neutral score applied for first-time borrower.")
          .build();
    }

    double successRate = (double) successful / total;

    int points;
    String explanation;

    if (successRate >= 0.95) {
      points = max;
      explanation =
          String.format(
              "Excellent repayment history: %d of %d loans repaid successfully (%.0f%%).",
              successful, total, successRate * 100);
    } else if (successRate >= 0.80) {
      points = (int) Math.round(max * 0.75);
      explanation =
          String.format(
              "Good repayment history: %d of %d loans repaid successfully (%.0f%%).",
              successful, total, successRate * 100);
    } else if (successRate >= 0.60) {
      points = (int) Math.round(max * 0.50);
      explanation =
          String.format(
              "Moderate repayment history: %d of %d loans repaid successfully (%.0f%%).",
              successful, total, successRate * 100);
    } else if (successRate >= 0.40) {
      points = (int) Math.round(max * 0.25);
      explanation =
          String.format(
              "Poor repayment history: %d of %d loans repaid successfully (%.0f%%).",
              successful, total, successRate * 100);
    } else {
      points = 0;
      explanation =
          String.format(
              "Very poor repayment history: %d of %d loans repaid successfully (%.0f%%) — high risk.",
              successful, total, successRate * 100);
    }

    return FactorScore.builder().points(points).maxPoints(max).explanation(explanation).build();
  }

  @Override
  public int maxPoints() {
    return weights.getRepaymentHistory();
  }

  @Override
  public String factorName() {
    return FACTOR_NAME;
  }
}
