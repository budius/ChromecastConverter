import 'dart:async';

import '../job.dart';

abstract class Step {
  final String name;

  Step(this.name);

  FutureOr<Result> process(Job job);
}

class Result {
  final ResultCode code;
  final String? msg;

  Result._(this.code, this.msg);

  factory Result.success() {
    return Result._(ResultCode.Success, null);
  }

  factory Result.fail(String msg) {
    return Result._(ResultCode.Fail, msg);
  }

  factory Result.skip(String msg) {
    return Result._(ResultCode.Skip, msg);
  }
}

enum ResultCode {
  Success,
  Fail,

  /// It's a success, but the rest of the process can be skipped.
  Skip


}
