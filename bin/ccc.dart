import 'dart:io';

import 'settings.dart';
import 'converter.dart';
import 'timber.dart';

void main(List<String> args) async {
  if (args.isEmpty) {
    stdout.writeln(parser.usage);
    return;
  }

  BatchSettings settings;
  try {
    settings = BatchSettings.parse(args);
  } catch (e) {
    stderr.writeln('Cannot parse arguments. ${e.toString()}');
    stdout.writeln(parser.usage);
    // 64: command line usage error
    exit(64);
  }

  Timber.setDebug(settings.isDebug);
  Timber.plant(StdOutLogger());
  //Timber.plant(SimpleLogger());

  final Converter converter = Converter(settings);
  converter.convert();
}
