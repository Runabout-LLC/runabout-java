# Changelog

## 1.2.1 - 2024-03-19
### Fixed
- Fixed a bug in the creation of the Runabout JSON with a null parameter. Now both type and eval for a null parameter will be "null".

## 1.2.0 - 2024-03-04
### Added
- Added a setter to the RunaboutServiceBuilder to provide a custom predicate to test StackFrames in the default CallerSupplier implementation.

## 1.1.1 - 2024-03-04
### Fixed
- Fixed bug in default CallerSupplier implementation that was picking methods from anonymous classes.

## 1.1.0 - 2024-02-28
### Added
- Added a setter to the RunaboutServiceBuilder to provide custom Method toString implementations.

### Changed
- Changed the default MethodToString implementation. Runabout will support both the old and new versions.

## 1.0.0 - 2024-02-26
Official release

### Added
- Added datetime field to the Runabout json object.
- Added a setter to the RunaboutServiceBuilder to provide custom datetime suppliers.

## 0.0.2 - 2024-02-25
### Changed
- Changed the dependencies portion of the Runabout to be a Set of Strings representing the fully qualified class names of the dependencies.

## 0.0.1 - 2024-02-25
Initial release of the runabout-java library.