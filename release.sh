#!/bin/bash

# Usage: ./release.sh 2.0.0

# --- CONFIGURATION ---
REPO_URL="https://github.com/Eclypses/socketx-client-android"
# Corrected Paths for Android Project Structure
SETTINGS_PATH="socketx-client-android/src/main/java/com/eclypses/socketx_client_android/Settings.kt"
CHANGELOG_PATH="CHANGELOG.md"
README_PATH="README.md"
BUILD_GRADLE_PATH="socketx-client-android/build.gradle.kts"
# ---------------------

# 1. Validation
if [ -z "$1" ]; then
  echo "Error: No version supplied."
  echo "Usage: ./release.sh <new_version>"
  echo "Example: ./release.sh 2.0.0"
  exit 1
fi

# STRIP 'v' if the user accidentally typed it (e.g. v2.0.0 -> 2.0.0)
CLEAN_VERSION="${1#v}"
TAG_VERSION="v$CLEAN_VERSION"
DATE=$(date +%Y-%m-%d)

echo "🚀 Preparing release: $TAG_VERSION on $DATE"

# 2. Update Settings.kt (Use CLEAN version: "2.0.0")
# Looks for: const val socketXClientVersion = "..."
sed -i '' "s/const val socketXClientVersion = \".*\"/const val socketXClientVersion = \"$CLEAN_VERSION\"/" "$SETTINGS_PATH"

# 3. Update build.gradle.kts version parameter (Use CLEAN version: "2.0.0")
# Looks for: version = "..."
sed -i '' "s/version = \".*\"/version = \"$CLEAN_VERSION\"/" "$BUILD_GRADLE_PATH"

# 4. Update README.md version parameter (Use CLEAN version: "2.0.0")
# Looks for: implementation("com.eclypses:socketx-client-android:...")
sed -i '' "s/implementation(\"com.eclypses:socketx-client-android:.*\")/implementation(\"com.eclypses:socketx-client-android:$CLEAN_VERSION\")/" "$README_PATH"
# Looks for: implementation 'com.eclypses:socketx-client-android:...'
sed -i '' "s/implementation 'com.eclypses:socketx-client-android:.*'/implementation 'com.eclypses:socketx-client-android:$CLEAN_VERSION'/" "$README_PATH"

# 5. Update CHANGELOG.md Headers
# Uses tags like [2.0.0] for headers. 
# NOTE: Requires a '## [Unreleased]' section in your CHANGELOG.md to work.
SEARCH="## \[Unreleased\]"
REPLACE="## [Unreleased]\\
\\
### Added\\
-\\
\\
### Changed\\
-\\
\\
### Fixed\\
-\\
\\
\\
## [$CLEAN_VERSION] - $DATE"

sed -i '' "s/$SEARCH/$REPLACE/" "$CHANGELOG_PATH"

# 6. Update CHANGELOG.md Reference Links
# IMPORTANT: The URL must match the Git Tag (which now has 'v')
# Link format: [2.0.0]: .../releases/tag/v2.0.0
NEW_LINK="[$CLEAN_VERSION]: $REPO_URL/releases/tag/$TAG_VERSION"

echo "" >> "$CHANGELOG_PATH"
echo "$NEW_LINK" >> "$CHANGELOG_PATH"

# 7. Git Operations
echo "📦 Committing changes..."
git add "$SETTINGS_PATH" "$BUILD_GRADLE_PATH" "$CHANGELOG_PATH" "$README_PATH"
# Commit message usually uses the clean version or the tag, preference varies.
git commit -m "chore: bump version to $CLEAN_VERSION"

echo "🏷️  Tagging version $TAG_VERSION..."
git tag "$TAG_VERSION"

echo "✅ Done! Validate the changes, then run:"
echo "   git push && git push --tags"
