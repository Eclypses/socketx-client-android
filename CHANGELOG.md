# Changelog

All notable changes to this project will be documented in this file.

## [Unreleased]

### Added
-

### Changed
-

### Fixed
-


## [1.2.2] - 2026-02-19

### Added
-

### Changed
- Switched azure-pipelines pool back to MaOS - Intel box

### Fixed
-


## [1.2.1] - 2026-02-19

### Added
-

### Changed
- Switched azure-pipelines pool to linux box

### Fixed
-


## [1.2.0] - 2026-02-18

### Added
- Added baseline layered unit test architecture (infrastructure fakes, fixtures, protocol/model/bridge/facade test suites).
- Added testing documentation in `dev_docs/TESTING_SUMMARY.md` and updated `dev_docs/LIBRARY_CONTEXT.md` with test architecture details.
- Added JaCoCo coverage reporting (`jacocoTestReport`) with XML/HTML outputs.

### Changed
- Updated unit test stack to JUnit5 + `kotlinx-coroutines-test` and configured Gradle to use JUnit Platform.
- Added configurable coverage threshold verification task (`jacocoTestCoverageVerification`) with `-PminLineCoverage` support (default `0.40`).
- Updated Azure Pipelines to run on `develop` and `master`; run coverage generation/verification and publish coverage only on `develop`.
- Updated `release.sh` to align with iOS-style safety preflight checks (branch/clean tree/tag checks), then simplified remote sync checks for squash-merge workflow.

### Fixed
- Fixed JVM unit-test stability issues caused by direct Android logging calls by introducing `InternalLog` wrapper usage.
- Fixed legacy URL path test expectations to match current `captureRoomPath` behavior.


## [1.0.9] - 2026-01-20

### Added
-

### Changed
- Updated README to be comprehensive implementation guide
- Updated release.sh to provide git push and push tag information

### Fixed
-


## [1.0.8] - 2026-01-05

### Added
- Added dev_docs directory with correct context and release documentation

### Changed
- Edited azure-pipelines.yml to remove dev_docs directory upon push to GitHub.

### Fixed
- 

## [1.0.7] - 2025-12-30

### Added
- 

### Changed
- Edited README to provide more accurate integration guidelines

### Fixed
- 


## [1.0.6] - 2025-12-29

### Added
- Add release script

### Changed
    
### Fixed
- Removed unnecessary "/ws" suffix from captureRoomPath method


## [1.0.5] - 2025-12-17

### Added

### Changed
- Changed MteRelay dependency from `implementation` to `api` to expose it to consumer apps.
    
### Fixed
- Fixed `NoClassDefFoundError` crash at runtime for MteBase.


## [1.0.4] - 2025-12-11

### Added

### Changed
- Changed MteRelay dependency from `implementation` to `api` to expose it to consumer apps.
    
### Fixed
- Fixed `NoClassDefFoundError` crash at runtime for MteBase.


## [1.0.3] - 2025-12-11

### Added

### Changed
Updated build.gradle to add javadocs to the bundle
    
### Fixed


## [1.0.2] - 2025-12-10

### Added
    First version published in Maven Central

### Changed
    
### Fixed


## [1.0.0] - 2025-12-09

### Added
    Initial commit

### Changed
    
### Fixed




[1.0.0]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.0.0

[1.0.2]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.0.2

[1.0.3]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.0.3

[1.0.4]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.0.4

[1.0.5]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.0.5

[1.0.6]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.0.6

[1.0.7]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.0.7

[2.0.8]: https://github.com/Eclypses/socketx-client-android/releases/tag/v2.0.8

[1.0.8]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.0.8

[1.0.8]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.0.8

[1.0.9]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.0.9

[1.0.9]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.0.9

[1.2.0]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.2.0

[1.2.1]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.2.1

[1.2.2]: https://github.com/Eclypses/socketx-client-android/releases/tag/v1.2.2
