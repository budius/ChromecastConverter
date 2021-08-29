/*

import 'package:shelf/shelf.dart' as shelf;
import 'package:http/http.dart' as http;

import 'screen.dart';

const String _cc_convert_executable =
    '/home/io/cc_converter/bin/cc_converter';
const String _cc_convert_arguments =
    '-d -p -i /mnt/media/transfer/ -o /mnt/media/dlna_root/A_recent/ --rawlogs';

const String _rsync_executable = 'rsync';
const String _rsync_arguments =
    '-a --progress --chown=io:io budius@10.0.0.102:/media/minidrive/downloads/complete/ /mnt/media/transfer';

const String _transmission_executable = 'ssh';
const String _transmission_arguments =
    '-L *:9091:localhost:9091 budius@10.0.0.102 -N';

class ScreenHandler {
  final String _title;
  final String _executable;
  final List<String> _arguments;

  Screen _screen;

  factory ScreenHandler.CcConverter() => ScreenHandler('Chromecast Converter',
      _cc_convert_executable, _cc_convert_arguments.split(' '));

  factory ScreenHandler.rsync() =>
      ScreenHandler('rsync', _rsync_executable, _rsync_arguments.split(' '));

  factory ScreenHandler.transmission() => ScreenHandler(
      'Transmission Tunnel ( http://10.0.0.1:9091/transmission/web/ )',
      _transmission_executable,
      _transmission_arguments.split(' '));

  ScreenHandler(this._title, this._executable, this._arguments);

  StringBuffer _logs({StringBuffer buffer}) {
    StringBuffer value = buffer ?? StringBuffer();
    _screen?.state?.forEach(value.writeln);
    return value;
  }

  Future<shelf.Response> handler(shelf.Request request) async {
    StringBuffer response;
    switch (request.method) {
      case 'POST': // initiated new process (if nothing is running)
        response = _logs(); // previous data
        if (_screen == null || !_screen.isAlive) {
          print('Starting new $_title process');
          response.writeln('info  : Starting new $_title process');
          _screen = await Screen.start(_executable, _arguments);
          iftt_when_screen_is_over(_screen);
          response = _logs(buffer: response); // any data already in the pipe
        } else {
          response.writeln('info  : Process $_title already started');
        }
        break;
      case 'DELETE': // interrupt current process (if something is running)
        int exitCode = await _screen?.kill();
        response = _logs(); // previous log + "kill" log
        response.writeln('info  : Process $_title exited with code $exitCode');
        break;
      default:
        response = _logs();
        if (_screen == null || !_screen.isAlive) {
          response.writeln('info  : Process $_title not running');
        } else {
          response.writeln('info  : Process $_title is executing');
        }
    }

    return shelf.Response.ok(response.toString());
  }

  void iftt_when_screen_is_over(Screen screen) async {
    try {
      int exitCode = await screen.exitCode;
      await http.get(Uri.encodeFull(
          'https://maker.ifttt.com/trigger/home_server/with/key/dpMBpm_FVbhZiR0g6aZDX6?value1=$_title finished with code $exitCode'));
    } catch (e) {
      print('Failed to execute IFTTT call. $e');
    }
  }
}


 */