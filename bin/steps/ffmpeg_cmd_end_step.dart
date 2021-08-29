import 'dart:io';
import 'dart:math';

import '../job.dart';
import 'dart:async';

import 'step.dart';

class ConversionEndCmdStep extends Step {
  ConversionEndCmdStep() : super('FFMPEG cmd-end');

  final Random _random = Random();

  @override
  FutureOr<Result> process(Job job) {
    File tempFile;

    do {
      final int id = _random.nextInt(8999) + 1000; // 1000~9999
      final String tempPath = '${job.output.absolute.path}.temp_$id.mp4';
      tempFile = File(tempPath);
    } while (tempFile.existsSync());

    job.tempOutput = tempFile;
    job.conversionArgs
        .addAll(<String>['-movflags', '+faststart', tempFile.path]);

    return Result.success();
  }
}
