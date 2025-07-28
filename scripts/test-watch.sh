#!/bin/bash
# Continuously run tests when files change

echo "Watching for file changes and running tests..."
echo "Press Ctrl+C to stop"

lein midje :autotest