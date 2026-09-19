#!/usr/bin/env python3
import json
import os
import re
import shutil
import subprocess
import glob
from datetime import datetime, timezone
from pathlib import Path


def main():
    version = os.environ["RELEASE_VERSION"]
    filename = os.environ["APK_FILE"]
    apk = Path(filename)
    tools = [shutil.which("aapt2"), shutil.which("aapt")]
    android_home = os.environ.get("ANDROID_HOME", "")
    if android_home:
        tools += sorted(glob.glob(f"{android_home}/build-tools/*/aapt2"), reverse=True)
        tools += sorted(glob.glob(f"{android_home}/build-tools/*/aapt"), reverse=True)
    tool = next((candidate for candidate in tools if candidate), None)
    data = {}
    if tool:
        output = subprocess.run([tool, "dump", "badging", str(apk)], capture_output=True,
                                text=True, check=False).stdout
        patterns = {"minSdk": r"(?:sdkVersion|minSdkVersion):'([^']+)'",
                    "versionCode": r"versionCode='([^']+)'",
                    "densities": r"densities: '([^']+)'",
                    "nativeLibraries": r"native-code: '([^']+)'"}
        for key, pattern in patterns.items():
            match = re.search(pattern, output)
            if match:
                data[key] = match.group(1).split() if key.endswith("s") else match.group(1)
    now = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    applied = [
        "Removed Google Play store verification requirement",
        "Removed package name lock",
        "Restored memory editor feature",
        "Removed forced version isolation",
        "CurseForge API integration",
    ]
    file_data = {
        "name": "levilaunchroid-extra", "version": version,
        "appKey": "levilaunchroid-extra", "appName": "LeviLaunchroid Extra",
        "arch": "arm64-v8a", "fileType": "APK", "brandKey": None, "brandName": None,
        "variant": None, "subVariant": None, "packageName": "org.levimc.launcher",
        "patchSources": [], "changelogs": [], "appliedPatches": applied,
        "originBuild": version, "publishedAt": now,
    }
    for key in ("minSdk", "versionCode", "densities", "nativeLibraries"):
        if key in data and data[key]:
            file_data[key] = data[key]
    manifest = {"schema": 1, "kind": "build",
                "meta": {"build": version, "channel": "stable", "publishedAt": now},
                "files": {filename: file_data}}
    Path("build.json").write_text(json.dumps(manifest, separators=(",", ":")) + "\n")


if __name__ == "__main__":
    main()
