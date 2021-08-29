import '../job.dart';
import 'dart:async';

import 'step.dart';

class ConversionStartCmdStep extends Step {
  ConversionStartCmdStep() : super('FFMPEG cmd-start');

  @override
  FutureOr<Result> process(Job job) {
    job.conversionArgs.addAll(<String>['-i', job.input.absolute.path]);
    return Result.success();
  }
}
