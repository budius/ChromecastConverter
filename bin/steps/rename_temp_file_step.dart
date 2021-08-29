import '../job.dart';
import 'dart:async';

import 'step.dart';

class RenameTempFileStep extends Step {
  RenameTempFileStep() : super('Rename temp file');

  @override
  FutureOr<Result> process(Job job) async {
    try {
      await job.tempOutput.rename(job.output.path);
      return Result.success();
    } on Exception catch (e) {
      return Result.fail('Failed to rename temp ${job.tempOutput.path}');
    }
  }
}
