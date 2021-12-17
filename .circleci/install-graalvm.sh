#!/usr/bin/env bash

set -euo pipefail

INSTALL_DIR="$GRAALVM_HOME"

GRAALVM_VERSION="${GRAALVM_VERSION:-21.2.0}"

# case "$BABASHKA_PLATFORM" in
# 	macos)
# 		GRAALVM_PLATFORM="darwin"
# 		;;
# 	linux)
# 		GRAALVM_PLATFORM="linux"
# 		;;
# esac

# case "${BABASHKA_ARCH:-}" in
# 	aarch64)
# 		GRAALVM_ARCH="aarch64"
# 		;;
# 	*)
# 		GRAALVM_ARCH="amd64"
# 		;;
# esac

# # curl https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-21.3.0/graalvm-ce-java11-linux-amd64-21.3.0.tar.gz -O -J -L



GRAALVM_FILENAME="graalvm-ce-java11-linux-amd64-$GRAALVM_VERSION.tar.gz"

mkdir -p $INSTALL_DIR

pushd "$INSTALL_DIR" >/dev/null

if ! [ -d "graalvm-ce-java11-$GRAALVM_VERSION" ]; then
	echo "Downloading GraalVM $GRAALVM_PLATFORM-$GRAALVM_ARCH-$GRAALVM_VERSION on '$PWD'..."
	curl -O -sL "https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-$GRAALVM_VERSION/$GRAALVM_FILENAME"
	tar xzf "$GRAALVM_FILENAME"
fi

popd >/dev/null