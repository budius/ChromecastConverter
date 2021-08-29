import 'dart:convert';
import 'dart:io';

import '../timber.dart';

const String _TAG = 'FFMPEG';
const String _FFMPEG = 'ffmpeg';

class FFmpegExecuteHelper {
  FFmpegExecuteHelper._(); // static only

  static Future<int> execute(List<String> args) async {
    final Process ffmpeg = await Process.start(_FFMPEG, args, runInShell: true);

    final Future<void> stdout = _timberStream(ffmpeg.stdout);
    final Future<void> stderr = _timberStream(ffmpeg.stderr);
    final int exitCode = await ffmpeg.exitCode;

    await stdout;
    await stderr;

    return exitCode;
  }

  static Future<void> _timberStream(Stream<List<int>> s) {
    return s.forEach((List<int> data) {
      Timber.i(_TAG, utf8.decode(data).trim());
    });
  }
}
