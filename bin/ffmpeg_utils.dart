import 'cli_runner.dart';
import 'ffprobe.dart';

class FFmpegUtils {
  FFmpegUtils._();

  static Future<bool> supportsCodec(String name) async {
    final List<String> args = <String>['-h', 'encoder=$name'];
    final CliRunner response = await CliRunner.run('ffmpeg', args);

    return !response.stdout
        .contains("Codec '$name' is not recognized by FFmpeg");
  }

  static int getStreamBitRate(FFprobe probe, String streamType) {
    try {
      final FFmpegStream stream = probe.streams
          .firstWhere((FFmpegStream s) => s.codec_type == streamType);
      final String? bitRate = stream.bit_rate;
      return bitRate != null ? int.parse(bitRate) : 0;
    } on Exception {
      return 0;
    }
  }

  static int getBitRate(FFmpegStream stream) {
    return getStringBitRate(stream.bit_rate);
  }

  static int getStringBitRate(String? bitRate) {
    try {
      return bitRate != null ? int.parse(bitRate) : 0;
    } on Exception {
      return 0;
    }
  }
}
