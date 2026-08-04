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

package org.apache.fineract.los.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.los.domain.CustomerCredential;
import org.apache.fineract.los.repository.CustomerCredentialRepository;
import org.apache.fineract.los.security.FineractAuthResponse;
import org.apache.fineract.los.security.JwtService;
import org.apache.fineract.los.service.FineractCredentialValidationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final CustomerCredentialRepository credentialRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final FineractCredentialValidationService fineractCredentialValidationService;

  public record LoginRequest(
      @NotBlank String username, @NotBlank String password, @NotBlank String tenantId) {}

  public record LoginResponse(
      String token, String username, Long clientId, String tenantId, int expiresInMinutes) {}

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody final LoginRequest request) {

    final Optional<CustomerCredential> customerCredential =
        credentialRepository.findByUsername(request.username());

    if (customerCredential.isPresent()) {
      final CustomerCredential credential = customerCredential.get();

      if (!credential.getTenantId().equals(request.tenantId())) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
      }

      if (passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
        final String token =
            jwtService.generateToken(
                credential.getUsername(),
                credential.getFineractClientId(),
                credential.getTenantId());

        return new LoginResponse(
            token,
            credential.getUsername(),
            credential.getFineractClientId(),
            credential.getTenantId(),
            15);
      }
    }

    final FineractAuthResponse fineractResponse =
        fineractCredentialValidationService.validate(request.username(), request.password());

    if (fineractResponse != null && fineractResponse.isAuthenticated()) {
      final String token =
          jwtService.generateToken(
              request.username(),
              fineractResponse.getUserId() != null ? fineractResponse.getUserId() : 0L,
              request.tenantId());

      return new LoginResponse(
          token,
          request.username(),
          fineractResponse.getUserId() != null ? fineractResponse.getUserId() : 0L,
          request.tenantId(),
          15);
    }

    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
  }
}
