import 'dart:io';

import 'settings.dart';
import 'ffprobe.dart';

class Job {
  // immutable
  final File input;
  final File output;
  final ConversionSettings settings;

  // late or mutable
  final List<String> conversionArgs = List<String>.empty(growable: true);
  final List<List<String>> subtitlesArgs =
      List<List<String>>.empty(growable: true);
  late FFprobe probe;
  late File tempOutput;

  Job(this.input, this.output, this.settings);

  @override
  String toString() {
    return 'Job{input: $input, output: $output}';
  }
}
