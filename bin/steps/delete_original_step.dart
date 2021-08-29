import '../job.dart';
import 'dart:async';

import 'step.dart';

class DeleteOriginalFileStep extends Step {
  DeleteOriginalFileStep() : super('Delete input');

  @override
  FutureOr<Result> process(Job job) {
    if (job.settings.deleteOnSuccess) {
      try {
        job.input.delete();
      } on Exception catch (e) {
        return Result.fail('Failed to delete original input ${job.input}. $e');
      }
    }
    return Result.success();
  }
}
