#!/bin/bash

# CORS Validation Script for Spring Boot Backend
# This script validates that CORS headers are correctly configured

set -e

API_URL="http://localhost:8080"
ENDPOINTS=("/api/users" "/api/hotels" "/api/bookings")
ORIGINS=("http://localhost:3000" "http://localhost:5173" "http://127.0.0.1:3000" "http://127.0.0.1:5173")

echo "=== CORS Headers Validation ==="
echo "Target: $API_URL"
echo ""

PASSED=0
FAILED=0

for ENDPOINT in "${ENDPOINTS[@]}"; do
    echo "Testing endpoint: $ENDPOINT"
    for ORIGIN in "${ORIGINS[@]}"; do
        echo -n "  Origin: $ORIGIN ... "
        
        RESPONSE=$(curl -s -w "\n%{http_code}" \
            -H "Origin: $ORIGIN" \
            -H "Access-Control-Request-Method: POST" \
            -H "Access-Control-Request-Headers: Content-Type,Authorization" \
            -X OPTIONS "$API_URL$ENDPOINT")
        
        HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
        HEADERS=$(echo "$RESPONSE" | head -n-1)
        
        if [[ "$HTTP_CODE" == "200" ]] || [[ "$HTTP_CODE" == "204" ]]; then
            ALLOW_ORIGIN=$(echo "$HEADERS" | grep -i "access-control-allow-origin" | head -n1 || true)
            ALLOW_METHODS=$(echo "$HEADERS" | grep -i "access-control-allow-methods" | head -n1 || true)
            ALLOW_HEADERS=$(echo "$HEADERS" | grep -i "access-control-allow-headers" | head -n1 || true)
            ALLOW_CREDENTIALS=$(echo "$HEADERS" | grep -i "access-control-allow-credentials" | head -n1 || true)
            
            if [[ -n "$ALLOW_ORIGIN" ]] && [[ -n "$ALLOW_METHODS" ]] && [[ -n "$ALLOW_HEADERS" ]]; then
                echo "✓ PASS"
                echo "    $ALLOW_ORIGIN"
                echo "    $ALLOW_METHODS"
                echo "    $ALLOW_HEADERS"
                if [[ -n "$ALLOW_CREDENTIALS" ]]; then
                    echo "    $ALLOW_CREDENTIALS"
                fi
                ((PASSED++))
            else
                echo "✗ FAIL - Missing headers"
                ((FAILED++))
            fi
        else
            echo "✗ FAIL - HTTP $HTTP_CODE"
            ((FAILED++))
        fi
    done
    echo ""
done

echo "=== Summary ==="
echo "Passed: $PASSED"
echo "Failed: $FAILED"

if [[ $FAILED -eq 0 ]]; then
    echo "✓ All CORS tests passed!"
    exit 0
else
    echo "✗ Some tests failed!"
    exit 1
fi
