#!/bin/bash

cd "$(dirname "$0")"

./gradlew --no-daemon shadowJar
ln -sf $PWD/bot.properties build/libs/bot.properties


