import 'dart:async';
import 'dart:convert';
import 'dart:io';

class CliRunner {
  final int exitCode;
  final String stdout;
  final String stderr;

  CliRunner(this.exitCode, this.stdout, this.stderr);

  static Future<CliRunner> run(
      String executable, List<String> arguments) async {
    final Process process =
        await Process.start(executable, arguments, runInShell: true);

    final Future<String> out = _collect(process.stdout);
    final Future<String> err = _collect(process.stderr);
    final Future<int> exitCode = process.exitCode;

    return CliRunner(await exitCode, await out, await err);
  }

  static Future<String> _collect(Stream<List<int>> stream) async {
    final List<int> data = (await stream.toList())
        .expand((List<int> element) => element)
        .toList(growable: false);
    return utf8.decode(data).trim();
  }
}
