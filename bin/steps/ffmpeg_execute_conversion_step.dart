import 'dart:async';

import 'step.dart';
import '../job.dart';
import 'ffmpeg_execute_helper.dart';

class FFmpegExecuteConversionStep extends Step {
  FFmpegExecuteConversionStep() : super('FFMPEG execute conversion');

  @override
  FutureOr<Result> process(Job job) async {
    final int exitCode = await FFmpegExecuteHelper.execute(job.conversionArgs);

    if (exitCode != 0) {
      return Result.fail('Returned code $exitCode for ${job.input.path}');
    }

    if (!await job.tempOutput.exists()) {
      return Result.fail('Output file was not saved for ${job.input.path}');
    }

    if (await job.tempOutput.length() == 0) {
      await job.tempOutput.delete();
      return Result.fail('Output file length is zero for ${job.input.path}');
    }

    return Result.success();
  }
}
