# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Comprehensive documentation with examples
- Test suite with clj-http-fake for mocking HTTP responses
- GitHub Actions workflow for CI/CD
- Utility functions for string/input-stream conversion
- Convenience client builders for common configurations
- Support for different response body types (string, bytes, stream, discard)

### Changed
- Updated Clojure dependency to 1.11.1
- Improved error handling
- Enhanced API with better docstrings
- Explicit Java 11+ requirement

## [0.3.0] - 2023-01-15

### Added
- Support for asynchronous requests
- Basic authentication support
- Timeout configuration

### Changed
- Improved request/response handling
- Better error reporting

## [0.2.0] - 2022-09-10

### Added
- Support for all HTTP methods
- Custom header support
- Client configuration options

## [0.1.0] - 2022-06-01

### Added
- Initial release
- Basic GET/POST functionality
- Simple client configuration


