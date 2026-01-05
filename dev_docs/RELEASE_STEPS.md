# Release Steps

This wiki provides guidelines to bump the library version and push **SocketX-Client-Android** to ADO and GitHub.

## 1. Automated Release Script (`release.sh`)

This project includes a helper script (`release.sh`) to automate version bumping. It updates `Settings.kt`, `build.gradle.kts`, `README.md`, and rotates the `CHANGELOG.md` headers.

### Usage
1.  **Update Changelog**: Ensure all your code changes are documented in `CHANGELOG.md` under the `## [Unreleased]` section.
2.  **Run Script**:
    ```bash
    ./release.sh <new_version>
    ```
    *   *Example:* `./release.sh v1.0.7` (The script handles the `v` prefix).
3.  **Verify**: Check that the version numbers in the project files have been updated correctly.
4.  **Push**: The script commits the changes and tags the release. You simply need to push.

---

## 2. Release Workflow

After you have run the release script and verified the changes:

1.  **Push Changes to ADO**:
    The script has already committed the changes and created the tag. Run:
    ```bash
    git push && git push --tags
    ```

2.  **Verify ADO**:
    *   Navigate to [SocketX-Client-Android on ADO](https://cos-ado.eclypses.com/DefaultCollection/Eclypses/_git/socketx-client-android).
    *   Verify the branch and tag are present.

3.  **Merge & Pipeline**:
    *   Merge as necessary in ADO.
    *   **Note:** Merge to the `master` branch will trigger a CI pipeline to push the Tag to GitHub.

4.  **Verify GitHub**:
    *   Navigate to [GitHub](https://github.com/Eclypses/socketx-client-android) and verify the public branch and tag.

5.  **Create GitHub Release**:
    *   Create a new release from the latest tag on GitHub.

---

## 3. Manual Publish to Maven Central

This section provides guidelines to publish **SocketX-Client-Android** to Maven Central to make it publicly available to customers.

### Preparation
1.  **Active Build Variant**: Ensure the Active Build Variant is set to `release` (View menu > Tool Windows > Build Variants).
    *   *This guide assumes you have already run `release.sh`, pushed to ADO, merged to master, and created the GitHub release.*

2.  **Clean Previous Builds**:
    *   Navigate to `socketx-client-android/build/repo`.
    *   **Delete** the `com` directory to remove any previous release builds.

### Build & Package
1.  **Generate Maven Artifacts**:
    *   Open Gradle menu > publishing.
    *   Run `publishReleasePublicationToLocalDirectoryRepository`.
    *   This builds all required files (AAR, POM, signatures) into `socketx-client-android/build/repo`.

2.  **Compress Artifacts**:
    *   Navigate to `socketx-client-android/build/repo`.
    *   Right-click the `com` directory and select **Compress "com"**.
    *   Rename the resulting zip file to `v<version>.zip` (e.g., `v1.0.7.zip`).
    *   *Tip: Move this file to your Desktop for easy access.*

### Upload to Sonatype (Maven Central)
1.  **Navigate**: Go to [https://central.sonatype.com/publishing](https://central.sonatype.com/publishing).
2.  **Publish Component**:
    *   Click **Publish Component**.
    *   **Name**: Copy the name from the previous release (e.g., `SocketX-Client-Android v1.0.6`) and update the version number.
    *   **Description**: Add a description (e.g., copy the latest text from `CHANGELOG.md`).
    *   **Upload**: Add the `v<version>.zip` file you just created.
    *   Click **Publish Component**.
3.  **Validation**:
    *   Wait for the status to change to **Validating**.
    *   Refresh until it shows **Validated**.
4.  **Publish**:
    *   After validation, click **Publish** to make it publicly available.
    *   *Warning: Once published, it cannot be removed from Maven Central.*
