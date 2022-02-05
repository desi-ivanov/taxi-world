#!/bin/bash
set -eou  pipefail
javac -cp "./lib/jade.jar:./lib/commons-codec-1.15.jar:./classes" -d classes taxi/*.java
