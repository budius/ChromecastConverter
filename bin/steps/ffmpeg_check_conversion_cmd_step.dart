import '../job.dart';
import 'dart:async';

import '../timber.dart';
import 'step.dart';

const String _TAG = 'Command';
const String _VIDEO_EXT = '.mp4';

class CheckConversionCmdStep extends Step {
  CheckConversionCmdStep() : super('FFMPEG check-cmd');

  @override
  FutureOr<Result> process(Job job) async {
    if (job.conversionArgs.isEmpty) {
      return Result.fail('No FFMPEG conversion command found');
    }

    final String args = job.conversionArgs.join(' ');
    if (args.contains('-c:v copy') &&
        args.contains('-c:a copy') &&
        job.probe.format.filename?.toLowerCase().endsWith(_VIDEO_EXT) == true &&
        job.probe.format.format_name?.toLowerCase().contains(_VIDEO_EXT) ==
            true) {
      // that means we don't have to process anything in this file
      // so we'll just check input/output folder and
      // move/copy the file depending on the `delete` flag

      if (job.input.path == job.output.path) {
        // if the input/output files are the same, we don't have to do anything
        return Result.skip('No operation needed for ${job.input.path}');
      } else {
        return _processMoveFile(job);
      }
    } else {
      Timber.i(_TAG, 'ffmpeg ${job.conversionArgs.join(' ')}');
      return Result.success();
    }
  }

  Future<Result> _processMoveFile(Job job) async {
    if (job.settings.deleteOnSuccess) {
      await job.input.rename(job.output.path);
      return Result.skip(
          'Only moved file without conversion for ${job.input.path}');
    } else {
      await job.input.copy(job.output.path);
      return Result.skip(
          'Only copied file without conversion for ${job.input.path}');
    }
  }
}
