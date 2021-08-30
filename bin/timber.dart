import 'dart:io';

final String _CARRIAGE_RETURN = '\r';
final String _NEW_LINE = '\n';

class Timber {
  Timber._(); // static only

  static bool _isDebug = false;

  static final List<Tree> _forest = List<Tree>.empty(growable: true);

  static void plant(Tree tree) {
    _forest.add(tree);
  }

  static void setDebug(bool isDebug) {
    _isDebug = isDebug;
  }

  static void d(String tag, String msg) {
    if (_isDebug) _forest.forEach((Tree tree) => tree.d(tag, msg));
  }

  static void i(String tag, String msg) {
    _forest.forEach((Tree tree) => tree.i(tag, msg));
  }
}

abstract class Tree {
  void d(String tag, String msg);

  void i(String tag, String msg);
}

const String _FFMPEG_TAG = 'FFMPEG';
const String _FFMPEG_CONV_START = 'frame=';
const String _FFMPEG_SUBT_START = 'size=';

class StdOutLogger extends Tree {
  @override
  void d(String tag, String msg) {
    log('Debug', tag, msg);
  }

  bool lastMsgWasFfmpeg = false;
  int lastFfmpegLength = 0;

  @override
  void i(String tag, String msg) {
    if (tag == _FFMPEG_TAG &&
        (msg.startsWith(_FFMPEG_CONV_START) ||
            msg.startsWith(_FFMPEG_SUBT_START))) {
      if (lastMsgWasFfmpeg) {
        stdout.write(_CARRIAGE_RETURN);
        stdout.writeAll(List<String>.filled(lastFfmpegLength + 12, ' '));
        stdout.write(_CARRIAGE_RETURN);
      }
      lastMsgWasFfmpeg = true;
      lastFfmpegLength = msg.length;

      stdout.write('[$_FFMPEG_TAG] $msg ${' ' * 10}');
    } else {
      if (lastMsgWasFfmpeg) {
        stdout.write(_NEW_LINE);
        lastMsgWasFfmpeg = false;
      }
      log('Info ', tag, msg);
    }
  }

  void log(String prio, String tag, String msg) {
    String begin = '[$prio | ${tag.padRight(12)}] ';
    msg.split('\n').forEach((String element) {
      stdout.writeln('$begin$element');
      begin = '                       ';
    });
  }
}

class SimpleLogger extends Tree {
  @override
  void d(String tag, String msg) {
    log('Debug', tag, msg);
  }

  @override
  void i(String tag, String msg) {
    log('Info ', tag, msg);
  }

  void log(String prio, String tag, String msg) {
    String begin = '[$prio | ${tag.padRight(12)}] ';
    msg.split('\n').forEach((String element) {
      stdout.writeln('$begin$element');
      begin = '                       ';
    });
  }
}
