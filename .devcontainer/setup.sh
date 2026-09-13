#!/bin/bash
set -e

echo "=== Installing Android SDK ==="
mkdir -p /opt/android-sdk/cmdline-tools
cd /tmp
curl -sL https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -o cmdline-tools.zip
unzip -qo cmdline-tools.zip -d /opt/android-sdk/cmdline-tools
mv /opt/android-sdk/cmdline-tools/cmdline-tools /opt/android-sdk/cmdline-tools/latest

export ANDROID_HOME=/opt/android-sdk
export ANDROID_SDK_ROOT=/opt/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

echo "=== Accepting Licenses ==="
yes | sdkmanager --licenses || true

echo "=== Installing SDK Components ==="
sdkmanager "platform-tools" "platforms;android-34" "build-tools;35.0.0" "ndk;27.2.12479018"

echo "=== Installing Xmake ==="
curl -sL https://xmake.io/shget.text | bash
export PATH=$PATH:~/.local/bin

echo "=== Installing Gradle Wrapper ==="
cd /workspaces/LeviLaunchroid
chmod +x ./gradlew

echo "=== Setup Complete ==="
echo "export ANDROID_HOME=/opt/android-sdk" >> ~/.bashrc
echo "export ANDROID_SDK_ROOT=/opt/android-sdk" >> ~/.bashrc
echo "export PATH=\$PATH:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools" >> ~/.bashrc
