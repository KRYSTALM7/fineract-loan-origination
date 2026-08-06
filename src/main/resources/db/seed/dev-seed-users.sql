-- Licensed to the Apache Software Foundation (ASF) under one
-- or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information
-- regarding copyright ownership. The ASF licenses this file
-- to you under the Apache License, Version 2.0 (the
-- "License"); you may not use this file except in compliance
-- with the License. You may obtain a copy of the License at
--
-- http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing,
-- software distributed under the License is distributed on an
-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
-- KIND, either express or implied. See the License for the
-- specific language governing permissions and limitations
-- under the License.

-- =============================================================================
-- DEV SEED: Test users for local development
--
-- Run against a local database that has already been migrated by Flyway.
-- All passwords are BCrypt-hashed (strength 10).
--
--   Admin  user  → username: admin    password: Admin@123
--   Staff  user  → username: staff    password: Staff@123
--   Customer     → username: customer password: Customer@123
--
-- Usage:
--   psql -h localhost -U postgres -d los_db -f src/main/resources/db/seed/dev-seed-users.sql
--
-- The ON CONFLICT DO NOTHING clauses make the script re-runnable safely.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Staff: admin (ROLE_ADMIN)
-- -----------------------------------------------------------------------------
INSERT INTO staff_credentials (username, password_hash, email, role, tenant_id, active, created_at)
VALUES (
    'admin',
    '$2a$10$hN8JrDCYZWTEgpwXeznAJuH66UOUhCJ6hEq5zKBSRd.aFQW8CfPHW',
    'admin@localhost',
    'ROLE_ADMIN',
    'default',
    TRUE,
    NOW()
)
ON CONFLICT (username) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Staff: loan officer (ROLE_STAFF)
-- -----------------------------------------------------------------------------
INSERT INTO staff_credentials (username, password_hash, email, role, tenant_id, active, created_at)
VALUES (
    'staff',
    '$2a$10$z/AbcPt.ifHvynJMAieqgOiSwgQP3fH6X9WzIPh0bb6mntWjAi/ca',
    'staff@localhost',
    'ROLE_STAFF',
    'default',
    TRUE,
    NOW()
)
ON CONFLICT (username) DO NOTHING;

-- -----------------------------------------------------------------------------
-- Customer (fineract_client_id = 1 — adjust to a real client ID as needed)
-- -----------------------------------------------------------------------------
INSERT INTO customer_credentials (username, password_hash, fineract_client_id, tenant_id, created_at)
VALUES (
    'customer',
    '$2a$10$5.PR/xH12Sqw/WklVroh7ecDA2A/9gybKiB7f2/k4gdd9GpHUcQVK',
    1,
    'default',
    NOW()
)
ON CONFLICT (username) DO NOTHING;
