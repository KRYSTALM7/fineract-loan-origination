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
 * Evaluates the applicant's existing debt burden relative to monthly income.
 *
 * <p>Computes the Debt-to-Income (DTI) ratio: {@code existingLoanObligations / monthlyIncome}.
 * Lower DTI means less of the applicant's income is already committed to debt, leaving more
 * capacity to service a new loan.
 *
 * <p>Scoring bands (DTI = existingLoanObligations / monthlyIncome):
 *
 * <ul>
 *   <li>&le; 0.20 (up to 20% of income committed) &rarr; full points
 *   <li>&le; 0.35 &rarr; 75% of max points
 *   <li>&le; 0.50 &rarr; 50% of max points
 *   <li>&le; 0.65 &rarr; 25% of max points
 *   <li>&gt; 0.65 &rarr; 0 points
 * </ul>
 *
 * <p>If {@code monthlyIncome} is null or zero, scores 0 — cannot compute DTI without income.
 * If {@code existingLoanObligations} is null or zero, the applicant has no existing debt and
 * receives full points.
 */
@Component
@RequiredArgsConstructor
public class DebtBurdenFactor implements ScoringFactor {

    private static final String FACTOR_NAME = "debt-burden";

    private final ScoringWeightsProperties weights;

    @Override
    public FactorScore score(ApplicantScoringProfile profile) {
        final int max = maxPoints();

        BigDecimal income = profile.getMonthlyIncome();
        BigDecimal obligations = profile.getExistingLoanObligations();

        if (income == null || income.compareTo(BigDecimal.ZERO) <= 0) {
            return FactorScore.builder()
                    .points(0)
                    .maxPoints(max)
                    .explanation("Cannot evaluate debt burden without monthly income data.")
                    .build();
        }

        // No existing obligations — clean slate, full points
        if (obligations == null || obligations.compareTo(BigDecimal.ZERO) <= 0) {
            return FactorScore.builder()
                    .points(max)
                    .maxPoints(max)
                    .explanation("No existing loan obligations — full debt capacity available.")
                    .build();
        }

        // DTI = existingObligations / monthlyIncome
        BigDecimal dti = obligations.divide(income, 4, RoundingMode.HALF_UP);
        double d = dti.doubleValue();

        int points;
        String explanation;

        if (d <= 0.20) {
            points = max;
            explanation = String.format(
                    "Debt-to-income ratio of %.0f%% is low — minimal existing debt burden.", d * 100);
        } else if (d <= 0.35) {
            points = (int) Math.round(max * 0.75);
            explanation = String.format(
                    "Debt-to-income ratio of %.0f%% is manageable.", d * 100);
        } else if (d <= 0.50) {
            points = (int) Math.round(max * 0.50);
            explanation = String.format(
                    "Debt-to-income ratio of %.0f%% indicates moderate debt burden.", d * 100);
        } else if (d <= 0.65) {
            points = (int) Math.round(max * 0.25);
            explanation = String.format(
                    "Debt-to-income ratio of %.0f%% indicates high debt burden.", d * 100);
        } else {
            points = 0;
            explanation = String.format(
                    "Debt-to-income ratio of %.0f%% exceeds safe threshold — very high risk.", d * 100);
        }

        return FactorScore.builder()
                .points(points)
                .maxPoints(max)
                .explanation(explanation)
                .build();
    }

    @Override
    public int maxPoints() {
        return weights.getDebtBurden();
    }

    @Override
    public String factorName() {
        return FACTOR_NAME;
    }
}