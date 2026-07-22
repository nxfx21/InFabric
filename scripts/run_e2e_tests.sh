#!/usr/bin/env bash
set -euo pipefail

echo "============================================================"
echo " Starting InFabric End-to-End (E2E) Test Suite Execution"
echo "============================================================"

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "[1/4] Running Unit & E2E Test Suite via Gradle..."
./gradlew test

echo "[2/4] Executing Full Single-JAR Build..."
./gradlew build -x test

ARTIFACT_JAR="build/libs/infusesmp-2.4.3.jar"
if [ ! -f "$ARTIFACT_JAR" ]; then
    echo "ERROR: Single-JAR artifact $ARTIFACT_JAR was not produced!"
    exit 1
fi
echo "SUCCESS: Produced artifact: $ARTIFACT_JAR ($(du -h "$ARTIFACT_JAR" | cut -f1))"

echo "[3/4] Verifying Java 21 Bytecode Target (Major Version 65)..."
MAJOR_VERSION=$(javap -v -cp "$ARTIFACT_JAR" com.catadmirer.infuseSMP.Infuse | grep "major version" | awk '{print $NF}')
echo "Detected Class Major Version: $MAJOR_VERSION"
if [ "$MAJOR_VERSION" -ne 65 ]; then
    echo "ERROR: Bytecode target mismatch! Expected 65 (Java 21), got $MAJOR_VERSION"
    exit 1
fi
echo "SUCCESS: Java 21 bytecode target confirmed (Major version 65)."

echo "[4/4] Verifying Single-JAR Embedded Dependencies (SGUI, PlaceholderAPI, SnakeYAML)..."
JAR_CONTENTS=$(jar tf "$ARTIFACT_JAR")
for PKG in "sgui" "placeholder-api" "snakeyaml"; do
    if echo "$JAR_CONTENTS" | grep -i "META-INF/jars/" | grep -q "$PKG"; then
        echo "  [OK] Embedded dependency jar found: $PKG"
    else
        echo "  [ERROR] Embedded dependency missing from single-JAR: $PKG"
        exit 1
    fi
done

echo "============================================================"
echo " E2E TEST SUITE EXECUTION & BUILD VERIFICATION COMPLETE!"
echo " ALL CHECKS PASSED CLEANLY (EXIT CODE 0)"
echo "============================================================"
