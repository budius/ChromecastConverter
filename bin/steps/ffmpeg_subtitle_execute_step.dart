import 'dart:async';

import 'step.dart';
import '../job.dart';
import 'ffmpeg_execute_helper.dart';

class FFmpegExecuteSubtitlesStep extends Step {
  FFmpegExecuteSubtitlesStep() : super('FFMPEG execute subtitles');

  @override
  FutureOr<Result> process(Job job) async {
    for (final List<String> args in job.subtitlesArgs) {
      final int exitCode = await FFmpegExecuteHelper.execute(args);

      if (exitCode != 0) {
        return Result.fail('Failed to extract subtitles for ${job.input.path}');
      }
    }

    return Result.success();
  }
}
