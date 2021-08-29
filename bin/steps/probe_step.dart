import '../ffprobe.dart';
import '../job.dart';
import 'step.dart';

class ProbeStep extends Step {
  ProbeStep() : super('FFProbe');

  @override
  Future<Result> process(Job job) async {
    try {
      job.probe = await FFprobe.probe(job.input);
      return Result.success();
    } on Exception catch (e) {
      return Result.fail(e.toString());
    }
  }
}
