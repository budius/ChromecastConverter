import 'dart:math';

import '../ffmpeg_utils.dart';
import '../ffprobe.dart';
import '../job.dart';
import 'dart:async';
import '../settings.dart';
import 'step.dart';

const List<String> _GOOD_CODECS = <String>['mp3', 'aac'];
const String _CODEC_TYPE_AUDIO = 'audio';
const String _CODEC_AAC = 'aac';
const String _CODEC_MP3 = 'libmp3lame';

class ConversionAudioCmdStep extends Step {
  ConversionAudioCmdStep() : super('FFMPEG cmd-audio');

  @override
  FutureOr<Result> process(Job job) async {
    final FFmpegStream stream;
    try {
      stream = job.probe.streams
          .firstWhere((FFmpegStream s) => s.codec_type == _CODEC_TYPE_AUDIO);
    } on Exception {
      return Result.fail('No audio stream available on ${job.input}');
    }

    final String? codec = stream.codec_name?.toLowerCase();
    if (codec != null &&
        _GOOD_CODECS.contains(codec) &&
        (stream.channels ?? 2) <= 2 &&
        !job.settings.forceConversion) {
      job.conversionArgs.addAll(<String>['-c:a', 'copy']);
      return Result.success();
    }

    // Starting from this commit
    // http://git.videolan.org/?p=ffmpeg.git;a=commit;h=d9791a8656b5580756d5b7ecc315057e8cd4255e
    // FFMPEG native AAC is the recommended way
    if (await FFmpegUtils.supportsCodec(_CODEC_AAC)) {
      int channels = min(2, (stream.channels ?? 2));
      int bitRate = channels * job.settings.quality.toAACBitRate();
      job.conversionArgs.addAll(
          <String>['-c:a', _CODEC_AAC, '-b:a', '${bitRate}k', '-ac', '2']);
      return Result.success();
    }

    // fallback on lame-mp3
    if (await FFmpegUtils.supportsCodec(_CODEC_MP3)) {
      job.conversionArgs.addAll(<String>[
        '-c:a',
        _CODEC_MP3,
        '-q:a',
        job.settings.quality.toMp3Quality(),
        '-ac',
        '2'
      ]);
      return Result.success();
    }

    return Result.fail('No suitable audio encoder available');
  }
}
