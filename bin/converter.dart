import 'settings.dart';
import 'job.dart';
import 'jobs.dart';
import 'steps/step.dart';
import 'steps.dart';
import 'timber.dart';

const String _TAG = 'Converter';

class Converter {
  final BatchSettings _settings;

  Converter(this._settings);

  void convert() async {
    Timber.d(_TAG, 'Executing for $_settings');
    final List<Job> jobs = await Jobs.extract(_settings);
    final List<Step> steps = Steps.extract(_settings);
    // use a normal for-loop to await for each job before executing the next
    Timber.d(_TAG, 'Extracted ${jobs.length} jobs to run on the batch.');
    for (Job job in jobs) {
      Timber.d(_TAG, 'Executing $job');
      await runJob(job, steps); // TODO: spawn isolates?
    }
  }

  Future<void> runJob(Job job, List<Step> steps) async {
    // use a normal for-loop to await for each step before executing the next
    for (final Step step in steps) {
      final Result result = await step.process(job);
      switch (result.code) {
        case ResultCode.Success:
          // no-op, just go to next
          Timber.d(_TAG, '${step.name} executed Success');
          break;
        case ResultCode.Fail:
          Timber.i(_TAG, 'Failure on ${step.name}: ${result.msg ?? 'Error'}');
          return;
        case ResultCode.Skip:
          Timber.d(_TAG, 'Skipping ${step.name}: ${result.msg ?? 'Error'}');
          return;
      }
    }
  }
}
