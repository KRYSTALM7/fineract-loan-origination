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
 * Evaluates the risk associated with the stated purpose of the loan.
 *
 * <p>Loan purpose is a well-established predictor of default in microfinance — productive loans
 * tied to income-generating activities consistently outperform consumption or speculative loans.
 * Risk categories are derived from CGAP and MIX Market microfinance benchmarks.
 *
 * <p>Purpose-to-score mapping (case-insensitive):
 *
 * <ul>
 *   <li>EDUCATION &rarr; full points (high social value, strong repayment motivation)
 *   <li>AGRICULTURE &rarr; full points (core MFI portfolio, productive)
 *   <li>BUSINESS &rarr; 80% of max points (productive, moderate risk)
 *   <li>HOME_IMPROVEMENT &rarr; 60% of max points (asset-backed, moderate)
 *   <li>MEDICAL &rarr; 60% of max points (necessary expenditure)
 *   <li>CONSUMPTION &rarr; 40% of max points (non-productive, higher risk)
 *   <li>PERSONAL &rarr; 40% of max points (undefined purpose)
 *   <li>SPECULATION &rarr; 0 points (highest risk, volatile returns)
 *   <li>Unknown / null &rarr; 30% of max points (conservative default)
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class LoanPurposeRiskFactor implements ScoringFactor {

  private static final String FACTOR_NAME = "loan-purpose-risk";

  private final ScoringWeightsProperties weights;

  @Override
  public FactorScore score(ApplicantScoringProfile profile) {
    final int max = maxPoints();

    String purpose = profile.getLoanPurpose();

    if (purpose == null || purpose.isBlank()) {
      int defaultPoints = (int) Math.round(max * 0.30);
      return FactorScore.builder()
          .points(defaultPoints)
          .maxPoints(max)
          .explanation("Loan purpose not specified — conservative risk score applied.")
          .build();
    }

    String normalized = purpose.trim().toUpperCase();
    double multiplier = resolveRiskMultiplier(normalized);
    int points = (int) Math.round(max * multiplier);

    String explanation = buildExplanation(normalized, points, max);

    return FactorScore.builder().points(points).maxPoints(max).explanation(explanation).build();
  }

  private double resolveRiskMultiplier(String purpose) {
    return switch (purpose) {
      case "EDUCATION" -> 1.00;
      case "AGRICULTURE" -> 1.00;
      case "BUSINESS" -> 0.80;
      case "HOME_IMPROVEMENT" -> 0.60;
      case "MEDICAL" -> 0.60;
      case "CONSUMPTION" -> 0.40;
      case "PERSONAL" -> 0.40;
      case "SPECULATION" -> 0.00;
      default -> 0.30;
    };
  }

  private String buildExplanation(String purpose, int points, int max) {
    return switch (purpose) {
      case "EDUCATION" -> "Education loan — high social value and repayment motivation.";
      case "AGRICULTURE" ->
          "Agriculture loan — productive purpose aligned with core MFI portfolio.";
      case "BUSINESS" ->
          "Business loan — productive purpose with moderate income variability risk.";
      case "HOME_IMPROVEMENT" -> "Home improvement loan — asset-backed, moderate risk.";
      case "MEDICAL" -> "Medical loan — necessary expenditure, moderate risk.";
      case "CONSUMPTION" -> "Consumption loan — non-productive purpose, elevated risk.";
      case "PERSONAL" -> "Personal loan with undefined purpose — elevated risk.";
      case "SPECULATION" -> "Speculative loan purpose — maximum risk, no points awarded.";
      default ->
          String.format(
              "Unrecognised loan purpose '%s' — conservative risk score applied.", purpose);
    };
  }

  @Override
  public int maxPoints() {
    return weights.getLoanPurposeRisk();
  }

  @Override
  public String factorName() {
    return FACTOR_NAME;
  }
}
