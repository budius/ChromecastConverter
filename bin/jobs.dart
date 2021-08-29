import 'dart:io';

import 'settings.dart';
import 'job.dart';
import 'timber.dart';

const String OUTPUT_EXTENSION = 'mp4';
const List<String> VIDEO_EXTENSION = <String>[
  'mp4',
  'mkv',
  'avi',
  'mpeg',
  'mpg',
  'mpe',
  'mov',
  'qt',
  'asf',
  'flv',
  'wmv',
  'm1v',
  'm2v',
  '3gp'
];

const String _TAG = 'Jobs';

class Jobs {
  Jobs._();

  static Future<List<Job>> extract(BatchSettings settings) async {
    final FileSystemEntity input = settings.input;
    if (input is Directory) {
      return _extractJobFromDirectory(input, settings);
    } else if (input is File) {
      final Job? job = await _extractJobFromFile(input, settings);
      if (job == null) {
        throw Exception('Invalid input ${input.path}');
      } else {
        return <Job>[job];
      }
    } else {
      throw Exception(
          'Input must be a File or Directory, but it is ${input.runtimeType}');
    }
  }

  static Future<List<Job>> _extractJobFromDirectory(
      Directory directory, BatchSettings settings) async {
    return directory
        .list(recursive: true, followLinks: true)
        .whereType<File>()
        .asyncMap((File file) => _extractJobFromFile(file, settings))
        .whereNotNull()
        .toList();
  }

  static Future<Job?> _extractJobFromFile(
      File file, BatchSettings settings) async {
    final String extension = file.path.split('.').last;
    if (!VIDEO_EXTENSION.contains(extension)) {
      return null;
    }

    Timber.d(_TAG, 'Setup job for ${file.absolute.path}');

    try {
      // find final output location
      final String baseInput =
          _resolveBaseFolder(settings.input).normalisedPath();
      final String baseOutput = settings.output.normalisedPath();
      final String filePath = file.absolute.path;
      final int extensionIndex = filePath.length - extension.length;
      final File output = File(filePath
          .replaceFirst(extension, OUTPUT_EXTENSION, extensionIndex)
          .replaceFirst(baseInput, baseOutput));

      return Job(file.absolute, output.absolute, settings.settings);
    } on Exception catch (e) {
      Timber.i(_TAG, e.toString());
    }
    return null;
  }

  static Directory _resolveBaseFolder(FileSystemEntity entity) {
    if (entity is Directory) {
      return entity;
    } else {
      return entity.parent;
    }
  }
}

extension<T> on Stream<T?> {
  Stream<E> whereType<E extends T>() {
    return where((T? event) => event is E).cast<E>();
  }

  Stream<T> whereNotNull() {
    return where((T? event) => event != null).cast<T>();
  }
}

extension on Directory {
  String normalisedPath() {
    final String path = absolute.path;
    if (path.endsWith('/')) {
      return path;
    } else {
      return '$path/';
    }
  }
}
