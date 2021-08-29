import 'dart:io';

import 'package:args/args.dart';

final ArgParser parser = _parser();

const String _input = 'input';
const String _output = 'output';
const String _quality = 'quality';
const String _speed = 'speed';
const String _delete = 'delete';
const String _force = 'force';
const String _pi = 'pi';
const String _debug = 'DEBUG';
const String _only_subtitles = 'only-subtitles';
const String _dry_run = 'dry-run';

class ConversionSettings {
  final Quality quality;
  final String speed;
  final bool forceConversion;
  final bool isPi;
  final bool deleteOnSuccess;

  ConversionSettings(this.quality, this.speed, this.forceConversion, this.isPi,
      this.deleteOnSuccess);

  @override
  String toString() {
    return 'ConversionSettings{quality: $quality, speed: $speed, '
        'forceConversion: $forceConversion, isPi: $isPi, '
        'deleteOnSuccess: $deleteOnSuccess}';
  }
}

class BatchSettings {
  final FileSystemEntity input;
  final Directory output;
  final bool isDebug;
  final bool deleteOnSuccess;
  final bool onlySubtitles;
  final bool dryRun;
  final ConversionSettings settings;

  @override
  String toString() {
    return 'BatchSettings{input: $input, output: $output, isDebug: $isDebug, '
        'deleteOnSuccess: $deleteOnSuccess, onlySubtitles: $onlySubtitles, '
        'dryRun: $dryRun, settings: $settings}';
  }

  BatchSettings(this.input, this.output, this.isDebug, this.deleteOnSuccess,
      this.onlySubtitles, this.dryRun, this.settings);

  factory BatchSettings.parse(List<String> args) {
    ArgResults results = parser.parse(args);
    final FileSystemEntity input = _resolveFileSystem(results[_input]);
    return BatchSettings(
      input,
      _resolveDirectory(results[_output], input),
      results[_debug],
      results[_delete],
      results[_only_subtitles],
      results[_dry_run],
      ConversionSettings(
          _resolveQuality(results[_quality]),
          _resolveSpeed(results[_speed]),
          results[_force],
          results[_pi],
          results[_delete]),
    );
  }

  static FileSystemEntity _resolveFileSystem(String arg,
      {FileSystemEntityType? forceType}) {
    String path = arg;
    FileSystemEntityType inputType =
        FileSystemEntity.typeSync(path, followLinks: true);

    if (inputType == FileSystemEntityType.link) {
      final Link link = Link(path);
      path = link.resolveSymbolicLinksSync();
      inputType = FileSystemEntity.typeSync(path, followLinks: true);
    }

    if (forceType != null && inputType != forceType) {
      throw Exception('Path $arg is not a ${forceType.toString()}');
    }

    FileSystemEntity returnVal;

    if (inputType == FileSystemEntityType.file) {
      returnVal = File(path);
    } else if (inputType == FileSystemEntityType.directory) {
      returnVal = Directory(path);
      (returnVal as Directory).createSync(recursive: true);
    } else {
      throw Exception('Cannot parse $arg');
    }

    if (!returnVal.existsSync()) {
      throw Exception('Path does not exist $arg');
    }

    return returnVal;
  }

  static Directory _resolveDirectory(
      String? arg, FileSystemEntity defaultValue) {
    if (arg == null) {
      if (defaultValue is Directory) {
        return defaultValue;
      } else {
        return defaultValue.parent;
      }
    }
    Directory(arg).createSync(recursive: true);
    return _resolveFileSystem(arg, forceType: FileSystemEntityType.directory)
        as Directory;
  }

  static Quality _resolveQuality(String input) {
    if (input == 'low') {
      return Quality.low;
    } else if (input == 'normal') {
      return Quality.normal;
    } else {
      return Quality.high;
    }
  }

  static String _resolveSpeed(String input) {
    if (Speed.values.any((Speed speed) => speed.toFFMPEGValue() == input)) {
      return input;
    } else {
      throw Exception('Cannot speed: $input');
    }
  }
}

ArgParser _parser() {
  return ArgParser()
    ..addOption(_input,
        abbr: 'i',
        help: 'Input file or folder',
        valueHelp: '~/videos/awesome/my_video.mkt')
    ..addOption(_output,
        abbr: 'o',
        defaultsTo: null,
        help: '[optional] Output folder',
        valueHelp: '~/videos/converted/')
    ..addOption(_quality,
        abbr: 'q',
        defaultsTo: 'high',
        help:
            '[optional] from ${Quality.values.map((e) => e.toReadableValue())}'
            ', matches to (18, 23, 26) respectively from FFMPEG')
    ..addOption(_speed,
        abbr: 's',
        defaultsTo: 'slow',
        help: '[optional] from ${Speed.values.map((e) => e.toFFMPEGValue())}')
    ..addFlag(_delete,
        abbr: 'd',
        defaultsTo: false,
        help: 'Delete the original file upon successful conversion')
    ..addFlag(_force,
        abbr: 'f',
        defaultsTo: false,
        help: 'Force conversion (even if input codecs are correct)')
    ..addFlag(_pi,
        abbr: 'p',
        defaultsTo: false,
        help: 'Uses h264_omx to allow conversion on the Raspberry Pi')
    ..addFlag(_debug, defaultsTo: false, help: 'Debug mode with more logs')
    ..addFlag(_only_subtitles,
        defaultsTo: false,
        help: 'Do no convert anything, simply extract subtitles')
    ..addFlag(_dry_run,
        defaultsTo: false,
        help: 'Only probe and build the FFMPEG commands. '
            'Skip the actual execution and file move/deletion.');
}

enum Quality { high, normal, low }
enum Speed {
  ultraFast,
  superFast,
  veryFast,
  faster,
  fast,
  medium,
  slow,
  slower,
  verySlow
}

extension QualityHelpers on Quality {
  String toH264Quality() {
    switch (this) {
      case Quality.high:
        return '18';
      case Quality.normal:
        return '23';
      case Quality.low:
        return '26';
    }
  }

  int toAACBitRate() {
    switch (this) {
      case Quality.high:
        return 80;
      case Quality.normal:
        return 64;
      case Quality.low:
        return 48;
    }
  }

  String toMp3Quality() {
    switch (this) {
      case Quality.high:
        return '1';
      case Quality.normal:
        return '4';
      case Quality.low:
        return '6';
    }
  }

  String toReadableValue() {
    switch (this) {
      case Quality.high:
        return 'high';
      case Quality.normal:
        return 'normal';
      case Quality.low:
        return 'low';
    }
  }
}

extension SpeedHelpers on Speed {
  String toFFMPEGValue() {
    switch (this) {
      case Speed.ultraFast:
        return 'ultrafast';
      case Speed.superFast:
        return 'superfast';
      case Speed.veryFast:
        return 'veryfast';
      case Speed.faster:
        return 'faster';
      case Speed.fast:
        return 'fast';
      case Speed.medium:
        return 'medium';
      case Speed.slow:
        return 'slow';
      case Speed.slower:
        return 'slower';
      case Speed.verySlow:
        return 'veryslow';
    }
  }
}
