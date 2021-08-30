import 'dart:io';
import 'dart:math';

import '../ffprobe.dart';
import '../job.dart';
import 'dart:async';

import '../jobs.dart';
import 'step.dart';
import '../timber.dart';

final List<int> _ALLOWED_CHARS =
    'qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM'.codeUnits;

const String _CODEC_TYPE_SUBTITLE = 'subtitle';
const String _INVALID_SUBTITLE = 'dvd_subtitle';
const String _SUBT_EXT = '.srt';

const String _TAG = 'Subtitle';

class SubtitleCmdStep extends Step {
  SubtitleCmdStep() : super('FFMPEG subtitle');

  @override
  FutureOr<Result> process(Job job) {
    final Map<String, _SubtitlesMeta> languages = Map();
    int subtitlesCount = 0;

    job.probe.streams
        .where((FFmpegStream s) => s.codec_type == _CODEC_TYPE_SUBTITLE)
        .forEach((FFmpegStream stream) {
      if (stream.codec_name == _INVALID_SUBTITLE) {
        subtitlesCount++; // count all subtitles, even invalid ones
        return;
      }

      _SubtitlesMeta meta = _SubtitlesMeta.fromStream(stream, subtitlesCount);
      subtitlesCount++;

      // disambiguation code for duplicated languages
      _SubtitlesMeta? existing = languages[meta.language];
      if (existing != null) {
        // 1st try, the cleaner way
        String disambiguation = _disambiguation(existing, meta);
        meta = _SubtitlesMeta(disambiguation, meta.title, meta.position);

        // if cleaner way fails, just add a number to it
        int whileCounter = 1;
        while (languages.containsKey(meta.language)) {
          String language = '${meta.language}-$whileCounter';
          whileCounter++;
          meta = _SubtitlesMeta(language, meta.title, meta.position);
        }
      }
      // add the language to the map
      languages[meta.language] = meta;
    });

    languages.values.forEach((_SubtitlesMeta meta) =>
        job.subtitlesArgs.add(_getSubtitleCmd(job, meta)));

    job.subtitlesArgs.forEach((List<String> args) {
      Timber.i(_TAG, 'ffmpeg ${args.join(' ')}');
    });

    return Result.success();
  }

  List<String> _getSubtitleCmd(Job job, _SubtitlesMeta meta) {
    final String outputPath = job.output.path;
    final String subtitleFile = outputPath.replaceFirst(
        OUTPUT_EXTENSION,
        '_${meta.language.toUpperCase()}$_SUBT_EXT',
        outputPath.length - OUTPUT_EXTENSION.length);

    return <String>[
      '-i',
      job.input.path,
      '-vn',
      '-an',
      '-map',
      '0:s:${meta.position}',
      subtitleFile
    ];
  }

  String _disambiguation(_SubtitlesMeta existing, _SubtitlesMeta added) {
    // remove duplication from title
    final String? existingTitle = existing.title;
    final String postfixDirty = existingTitle != null
        ? added.title?.replaceAll(existingTitle, '') ?? ''
        : '';

    final String postfix = String.fromCharCodes(postfixDirty.codeUnits
        .where((int char) => _ALLOWED_CHARS.contains(char)));

    if (postfix.isEmpty) {
      return added.language;
    } else {
      return '${added.language}-$postfix';
    }
  }
}

class _SubtitlesMeta {
  final String language;
  final String? title;
  final int position;

  _SubtitlesMeta(this.language, this.title, this.position);

  factory _SubtitlesMeta.fromStream(FFmpegStream stream, int position) {
    final StreamTags? t = stream.tags;
    final String? language = t?.language;
    if (t != null && language != null) {
      return _SubtitlesMeta(language, t.title, position);
    } else {
      return _SubtitlesMeta('DEFAULT', 'DEFAULT', position);
    }
  }
}
