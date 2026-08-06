# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements. See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership. The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied. See the License for the
# specific language governing permissions and limitations
# under the License.

# =============================================================================
# Seed dev test users into a running local LOS database.
#
# Default credentials (matching application.yml):
#   DB host   : localhost
#   DB port   : 5432
#   DB name   : los_db
#   DB user   : postgres
#
# Override via parameters:
#   .\scripts\seed-dev-users.ps1 -DbHost localhost -DbPort 5433 -DbName los_db -DbUser los_user
# =============================================================================

param(
    [string]$DbHost     = "localhost",
    [int]   $DbPort     = 5432,
    [string]$DbName     = "los_db",
    [string]$DbUser     = "postgres",
    [string]$SeedScript = "src\main\resources\db\seed\dev-seed-users.sql"
)

$ErrorActionPreference = "Stop"

Write-Host ""
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  LOS Dev User Seeder" -ForegroundColor Cyan
Write-Host "======================================" -ForegroundColor Cyan
Write-Host "  Host   : $DbHost`:$DbPort"
Write-Host "  DB     : $DbName"
Write-Host "  User   : $DbUser"
Write-Host "  Script : $SeedScript"
Write-Host ""

# Check psql is available
if (-not (Get-Command psql -ErrorAction SilentlyContinue)) {
    Write-Error "psql not found. Install PostgreSQL client tools and ensure they are on PATH."
    exit 1
}

# Run the seed script
$env:PGPASSWORD = Read-Host "Enter password for PostgreSQL user '$DbUser'" -AsSecureString |
    [System.Runtime.InteropServices.Marshal]::PtrToStringAuto(
        [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($_)
    )

psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -f $SeedScript

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "Done! Test users seeded." -ForegroundColor Green
    Write-Host ""
    Write-Host "  Login as admin    : POST /api/v1/auth/staff/login    { username:'admin',    password:'Admin@123',    tenantId:'default' }"
    Write-Host "  Login as staff    : POST /api/v1/auth/staff/login    { username:'staff',    password:'Staff@123',    tenantId:'default' }"
    Write-Host "  Login as customer : POST /api/v1/auth/login          { username:'customer', password:'Customer@123', tenantId:'default' }"
    Write-Host ""
} else {
    Write-Error "Seed script failed (exit code $LASTEXITCODE)."
}
