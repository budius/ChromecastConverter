/*

import 'dart:async';
import 'dart:convert';
import 'dart:io';

import 'package:rxdart/rxdart.dart';

const int _NEW_LINE = 10;
const int _CARRIAGE_RETURN = 13;
const int _BACKSPACE = 8;

/// Indicates the source of a data
enum SourceStream { stdout, stderr }

/// A value coming from one of the standard streams.
class StreamValue {
  final SourceStream name;
  final String value;

  StreamValue(this.name, this.value)
      : assert(name != null),
        assert(value != null);

  @override
  String toString() {
    switch (name) {
      case SourceStream.stdout:
        return 'stdout: $value';
      default:
        return 'stderr: $value';
    }
  }

  /// Transformation helper
  static String transform(StreamValue streamValue) {
    return streamValue.value;
  }
}

/// Copies the idea of GNU/Screen (https://www.gnu.org/software/screen/)
/// It allows the dart code to interact simultaneously and asynchronously with different processes.
/// Each process here is represented by one screen object.
/// The [state] and [stateStream] represents the combined stdout/stderr output of the process.
/// The last [maxSize] controls how long this buffer is.
/// Every [Stream] presented by this class is a BroadcastStream,
/// allowing it to be subscribed to several times.
/// The most common way to get an instance of Screen is using the [Screen.start],
/// but it can also be accomplished using [Screen.attach].
abstract class Screen {
  //region read streams
  /// Current state of this screen. State is the combination of stdout and stderr
  List<StreamValue> get state;

  /// A broadcast Stream of the [state]
  Stream<List<StreamValue>> get stateStream;

  /// A broadcast Stream of the raw output from stdout
  Stream<List<int>> get rawStdout;

  /// A broadcast Stream of the raw output from stderr
  Stream<List<int>> get rawStderr;

  /// A broadcast Stream of the utf8 decoded output from stdout
  Stream<String> get stdout;

  /// A broadcast Stream of the utf8 decoded output from stderr
  Stream<String> get stderr;

  //endregion

  //region write streams
  /// Add the stream to the process stdin.
  Future<void> addStream(Stream<String> stream);

  /// Add the data to the process stdin.
  /// Returns a future of the flush operation.
  /// This function must not be called when a stream is
  /// currently being added using [addStream].
  Future<void> addString(String data);

  //endregion

  //region process state
  /// Indication of whether the process is still executing
  /// (exit code have not returned)
  bool get isAlive;

  /// gets the [Process.exitCode]
  Future<int> get exitCode;

  /// Kills the running process by invoking [Process.kill]
  /// Defaults to [ProcessSignal.sigterm] (same from Process class)
  /// if success, returns the process exit code
  /// if failed, returns -1
  ///
  /// Explanation on the different kill signals.
  /// from: https://www.quora.com/What-is-the-difference-between-the-SIGINT-and-SIGTERM-signals-in-Linux-What%E2%80%99s-the-difference-between-the-SIGKILL-and-SIGSTOP-signals
  ///
  /// SIGINT is the interrupt signal.
  /// The terminal sends it to the foreground process when the user presses ctrl-c.
  /// The default behavior is to terminate the process,
  /// but it can be caught or ignored.
  /// The intention is to provide a mechanism for an orderly, graceful shutdown.
  ///
  /// SIGQUIT is the dump core signal.
  /// The terminal sends it to the foreground process when the user presses ctrl-\.
  /// The default behavior is to terminate the process and dump core,
  /// but it can be caught or ignored.
  /// The intention is to provide a mechanism for the user to abort the process.
  /// You can look at SIGINT as "user-initiated happy termination"
  /// and SIGQUIT as "user-initiated unhappy termination."
  ///
  /// SIGTERM is the termination signal.
  /// The default behavior is to terminate the process,
  /// but it also can be caught or ignored.
  /// The intention is to kill the process, gracefully or not,
  /// but to first allow it a chance to cleanup.
  ///
  /// SIGKILL is the kill signal.
  /// The only behavior is to kill the process, immediately.
  /// As the process cannot catch the signal, it cannot cleanup,
  /// and thus this is a signal of last resort.
  ///
  /// SIGSTOP is the pause signal.
  /// The only behavior is to pause the process;
  /// the signal cannot be caught or ignored.
  /// The shell uses pausing (and its counterpart, resuming via SIGCONT)
  /// to implement job control.
  Future<int> kill({ProcessSignal signal = ProcessSignal.sigterm});

  //endregion

  /// Starts a process and attaches it to a Screen.
  /// [maxSize] indicates how many items will buffered on the [Screen.state]
  /// Other parameters are mirrored from [Process.start]
  static Future<Screen> start(String executable, List<String> arguments,
      {int maxSize = 250,
      String? workingDirectory,
      Map<String, String>? environment,
      bool includeParentEnvironment = true,
      bool runInShell = false}) async {
    Process process = await Process.start(executable, arguments,
        workingDirectory: workingDirectory,
        environment: environment,
        includeParentEnvironment: includeParentEnvironment,
        runInShell: runInShell);
    return _ScreenImpl(process, maxSize);
  }

  /// Attaches a process to a Screen
  /// [maxSize] indicates how many items will buffered on the [Screen.state]
  /// Important to note this process stdout/stderr will be subscribed
  /// by the Screen and should not be used externally.
  static Screen attach(Process process, {int maxSize = 250}) {
    return _ScreenImpl(process, maxSize);
  }

  static final StreamTransformer<List<StreamValue>, List<String>> _transformer =
      StreamTransformer<List<StreamValue>, List<String>>.fromHandlers(
          handleData: (List<StreamValue> data, EventSink<List<String>> sink) {
    sink.add(data.map(StreamValue.transform).toList(growable: false));
  });

  /// Helper to transform the Stream<List<StreamValue>> to Stream<List<String>>
  static StreamTransformer<List<StreamValue>, List<String>> get transformer {
    return _transformer;
  }
}

class _ScreenImpl implements Screen {
  final Process process;
  final int maxSize;
  final _StdStreamWrap out;
  final _StdStreamWrap err;
  bool alive = true;

  _StreamProcessor outBuffer;
  _StreamProcessor errBuffer;
  final List<StreamValue> stateBuffer = <StreamValue>[];

  final BehaviorSubject<List<StreamValue>> currentState =
      BehaviorSubject<List<StreamValue>>.seeded(<StreamValue>[]);

  StreamSubscription<List<int>> errSubscription;
  StreamSubscription<List<int>> outSubscription;

  _ScreenImpl(this.process, this.maxSize)
      : out = _StdStreamWrap(process.stdout),
        err = _StdStreamWrap(process.stderr) {
    _processState();
    _processAlive();
  }

  void _processState() {
    outBuffer = _StreamProcessor(
        maxSize, currentState, stateBuffer, SourceStream.stdout);
    outSubscription = out.rawStream.listen(outBuffer.onData);

    errBuffer = _StreamProcessor(
        maxSize, currentState, stateBuffer, SourceStream.stderr);
    errSubscription = err.rawStream.listen(errBuffer.onData);
  }

  void _processAlive() async {
    await process.exitCode;
    await currentState.close();
    alive = false;
  }

  @override
  Future<void> addStream(Stream<String> stream) {
    return process.stdin.addStream(stream.transform(utf8.encoder));
  }

  @override
  Future<void> addString(String data) {
    process.stdin.write(data);
    return process.stdin.flush();
  }

  @override
  Future<int> get exitCode => process.exitCode;

  @override
  bool get isAlive => alive;

  @override
  Future<int> kill({ProcessSignal signal = ProcessSignal.sigterm}) async {
    if (process.kill(signal)) {
      return process.exitCode;
    } else {
      return -1;
    }
  }

  @override
  Stream<List<int>> get rawStderr => err.rawStream;

  @override
  Stream<List<int>> get rawStdout => out.rawStream;

  @override
  List<StreamValue> get state => currentState.value;

  @override
  Stream<List<StreamValue>> get stateStream => currentState;

  @override
  Stream<String> get stderr => err.stringStream;

  @override
  Stream<String> get stdout => out.stringStream;
}

class _StdStreamWrap {
  Stream<List<int>> rawStream;
  Stream<String> stringStream;

  _StdStreamWrap(Stream<List<int>> source) {
    rawStream = source.asBroadcastStream();
    stringStream = rawStream.transform(
        StreamTransformer<List<int>, String>.fromHandlers(handleData: (List<int> data, EventSink<String> sink) {
      String value = utf8.decode(data)?.trim();
      if (value?.isNotEmpty == true) {
        sink.add(value);
      }
    })).asBroadcastStream();
  }
}

class _StreamProcessor {
  final int maxSize;
  final BehaviorSubject<List<StreamValue>> stateStream;
  final SourceStream sourceStream;
  final List<int> lineBuffer = List<int>();
  final List<StreamValue> stateBuffer;
  StreamValue dispatchedLine;
  bool dirty = false;

  _StreamProcessor(
      this.maxSize, this.stateStream, this.stateBuffer, this.sourceStream);

  void onData(List<int> data) {
    dirty = false;
    data.forEach(_onByte);
    if (dirty) {
      _dispatchBufferedLine();
    }
  }

  void _onByte(int byte) {
    switch (byte) {
      case _NEW_LINE:
        _onLine(utf8.decode(lineBuffer));
        lineBuffer.clear();
        dirty = false;
        break;
      case _CARRIAGE_RETURN:
        lineBuffer.clear();
        dirty = true;
        break;
      case _BACKSPACE:
        lineBuffer.removeLast();
        dirty = true;
        break;
      default:
        lineBuffer.add(byte);
        dirty = true;
    }
  }

  void _dispatchBufferedLine() {
    String buffered = utf8.decode(lineBuffer);
    if (dispatchedLine != null) {
      stateBuffer.remove(dispatchedLine);
      dispatchedLine = null;
    }

    // print('$sourceStream -> $buffered');

    dispatchedLine = StreamValue(sourceStream, buffered);
    stateBuffer.add(dispatchedLine);
    if (stateBuffer.length > maxSize) {
      stateBuffer.removeAt(0);
    }
    stateStream.add(List<StreamValue>.from(stateBuffer, growable: false));
  }

  void _onLine(String data) {
    if (maxSize == 0) {
      return;
    }

    // print('$sourceStream -> $data');

    String value = data?.trim();
    if (value == null || value.isEmpty) {
      return;
    }
    stateBuffer.add(StreamValue(sourceStream, value));
    if (stateBuffer.length > maxSize) {
      stateBuffer.removeAt(0);
    }
    stateStream.add(List<StreamValue>.from(stateBuffer, growable: false));
  }
}


 */