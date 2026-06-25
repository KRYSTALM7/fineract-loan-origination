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

import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.los.scoring.ScoringFactor;
import org.apache.fineract.los.scoring.ScoringWeightsProperties;
import org.apache.fineract.los.scoring.model.ApplicantScoringProfile;
import org.apache.fineract.los.scoring.model.FactorScore;
import org.springframework.stereotype.Component;

/**
 * Evaluates the income-to-loan ratio as a credit scoring factor.
 *
 * <p>Measures whether the applicant's monthly income is sufficient relative to the requested loan
 * amount. A higher ratio indicates stronger repayment capacity.
 *
 * <p>Scoring bands (ratio = monthlyIncome / requestedAmount):
 *
 * <ul>
 *   <li>&ge; 0.50 (income covers loan in &le; 2 months) &rarr; full points
 *   <li>&ge; 0.25 (&le; 4 months) &rarr; 75% of max points
 *   <li>&ge; 0.15 (&le; ~6.7 months) &rarr; 50% of max points
 *   <li>&ge; 0.10 (&le; 10 months) &rarr; 25% of max points
 *   <li>&lt; 0.10 &rarr; 0 points
 * </ul>
 *
 * <p>If {@code requestedAmount} is null or zero, scores 0 (cannot evaluate an undefined loan size).
 * If {@code monthlyIncome} is null or zero, scores 0 (no income means no repayment capacity).
 */
@Component
@RequiredArgsConstructor
public class IncomeLoanRatioFactor implements ScoringFactor {

    private static final String FACTOR_NAME = "income-ratio";

    private final ScoringWeightsProperties weights;

    @Override
    public FactorScore score(ApplicantScoringProfile profile) {
        final int max = maxPoints();

        BigDecimal income = profile.getMonthlyIncome();
        BigDecimal requested = profile.getRequestedAmount();

        if (income == null || requested == null
                || income.compareTo(BigDecimal.ZERO) <= 0
                || requested.compareTo(BigDecimal.ZERO) <= 0) {
            return FactorScore.builder()
                    .points(0)
                    .maxPoints(max)
                    .explanation("Insufficient data to evaluate income-to-loan ratio.")
                    .build();
        }

        // ratio = monthlyIncome / requestedAmount
        BigDecimal ratio = income.divide(requested, 4, RoundingMode.HALF_UP);
        double r = ratio.doubleValue();

        int points;
        String explanation;

        if (r >= 0.50) {
            points = max;
            explanation = String.format(
                    "Income-to-loan ratio of %.2fx indicates strong repayment capacity.", r);
        } else if (r >= 0.25) {
            points = (int) Math.round(max * 0.75);
            explanation = String.format(
                    "Income-to-loan ratio of %.2fx indicates adequate repayment capacity.", r);
        } else if (r >= 0.15) {
            points = (int) Math.round(max * 0.50);
            explanation = String.format(
                    "Income-to-loan ratio of %.2fx indicates moderate repayment capacity.", r);
        } else if (r >= 0.10) {
            points = (int) Math.round(max * 0.25);
            explanation = String.format(
                    "Income-to-loan ratio of %.2fx indicates limited repayment capacity.", r);
        } else {
            points = 0;
            explanation = String.format(
                    "Income-to-loan ratio of %.2fx is below minimum threshold — high risk.", r);
        }

        return FactorScore.builder()
                .points(points)
                .maxPoints(max)
                .explanation(explanation)
                .build();
    }

    @Override
    public int maxPoints() {
        return weights.getIncomeRatio();
    }

    @Override
    public String factorName() {
        return FACTOR_NAME;
    }
}