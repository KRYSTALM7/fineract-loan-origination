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
package org.apache.fineract.los.security;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.los.service.FineractCredentialValidationService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * Authenticates LOS staff (loan officers, branch managers, credit committee members) by delegating
 * credential validation to Apache Fineract's {@code POST /authentication} endpoint via {@link
 * FineractCredentialValidationService}, then resolving the caller's LOS workflow role from the
 * Fineract roles present in that response.
 *
 * <p>Every successfully authenticated staff member is granted {@code ROLE_STAFF}. If their Fineract
 * roles map to a configured LOS workflow role (see {@link FineractRoleResolver}), an additional
 * {@code ROLE_<LosRole>} authority (e.g. {@code ROLE_LOAN_OFFICER}) is granted. {@code
 * ApprovalWorkflowService} relies on that second authority to enforce that only the officer whose
 * role matches the application's current workflow stage can record a decision on it.
 */
@Component
@RequiredArgsConstructor
public class FineractAuthenticationProvider implements AuthenticationProvider {

  private static final String ROLE_STAFF = "ROLE_STAFF";
  private static final String ROLE_PREFIX = "ROLE_";

  private final FineractCredentialValidationService validationService;
  private final FineractRoleResolver roleResolver;

  @Override
  public Authentication authenticate(final Authentication authentication)
      throws AuthenticationException {

    final String username = authentication.getName();
    final String password = authentication.getCredentials().toString();

    final FineractAuthResponse fineractResponse = validationService.validate(username, password);

    if (fineractResponse == null) {
      throw new BadCredentialsException("Invalid Fineract credentials for user: " + username);
    }

    final List<String> fineractRoleNames =
        fineractResponse.getRoles() == null
            ? List.of()
            : fineractResponse.getRoles().stream()
                .map(FineractAuthResponse.FineractRole::getName)
                .toList();

    final List<SimpleGrantedAuthority> authorities = new ArrayList<>();
    authorities.add(new SimpleGrantedAuthority(ROLE_STAFF));

    roleResolver
        .resolve(fineractRoleNames)
        .ifPresent(role -> authorities.add(new SimpleGrantedAuthority(ROLE_PREFIX + role.name())));

    return new UsernamePasswordAuthenticationToken(username, password, authorities);
  }

  @Override
  public boolean supports(final Class<?> authentication) {
    return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
  }
}
