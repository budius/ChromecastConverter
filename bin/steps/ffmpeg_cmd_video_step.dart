import '../settings.dart';
import '../ffmpeg_utils.dart';
import '../ffprobe.dart';
import '../job.dart';
import 'dart:async';

import 'step.dart';

const String _CODEC_TYPE_VIDEO = 'video';
const String _CODEC_TYPE_AUDIO = 'audio';

const String _GOOD_VIDEO_CODEC = 'h264';
const String _GOOD_VIDEO_PROFILE = 'High';

const String _FFMPEG_CODEC_NAME = 'libx264';
const String _FFMPEG_PI_CODEC_NAME = 'h264_omx';

const int _LOW_BITRATE_AUDIO_STREAM =
    96 * 1024; // just a wild guess on some low-ish value

class ConversionVideoCmdStep extends Step {
  ConversionVideoCmdStep() : super('FFMPEG cmd-video');

  @override
  FutureOr<Result> process(Job job) async {
    final FFmpegStream stream;
    try {
      stream = job.probe.streams
          .firstWhere((FFmpegStream s) => s.codec_type == _CODEC_TYPE_VIDEO);
    } on Exception {
      return Result.fail('No video stream available on ${job.input}');
    }

    // check if video coded is already good
    if (_GOOD_VIDEO_CODEC == stream.codec_name &&
        _GOOD_VIDEO_PROFILE == stream.profile &&
        !job.settings.forceConversion) {
      job.conversionArgs.addAll(<String>['-c:v', 'copy']);
      return Result.success();
    }

    // raspberry pi specific conversion using GPU
    if (job.settings.isPi) {
      if (!await FFmpegUtils.supportsCodec(_FFMPEG_PI_CODEC_NAME)) {
        return Result.fail(
            'No $_FFMPEG_PI_CODEC_NAME available for the Raspberry PI');
      }

      final String? bitRate = _getVideoBitRate(stream, job.probe);
      job.conversionArgs.addAll(<String>[
        '-c:v',
        _FFMPEG_PI_CODEC_NAME,
        if (bitRate != null) '-b:v',
        if (bitRate != null) bitRate
      ]);

      return Result.success();
    }

    // check ffmpeg supports libx264
    if (!await FFmpegUtils.supportsCodec(_FFMPEG_CODEC_NAME)) {
      return Result.fail('No $_FFMPEG_CODEC_NAME encoder available');
    }

    final String? bitRate = _getVideoBitRate(stream, job.probe);
    // add FFMPEG PC commands
    job.conversionArgs.addAll(<String>[
      '-c:v',
      _FFMPEG_CODEC_NAME,
      '-profile:v',
      'high',
      '-level',
      '5',
      '-preset',
      job.settings.speed,
      '-crf',
      job.settings.quality.toH264Quality(),
      if (bitRate != null) '-maxrate',
      if (bitRate != null) bitRate,
      if (bitRate != null) '-bufsize',
      if (bitRate != null) '5M'
    ]);

    return Result.success();
  }

  String? _getVideoBitRate(FFmpegStream stream, FFprobe probe) {
    int? preCalculate() {
      final int videoBitRate =
          _getVideoBitrateBasedOnVideoStreamBitRate(stream);
      final int fileBitRate = _getVideoBitrateBasedOnFileBitrate(probe);

      if (videoBitRate > 0 && fileBitRate > 0) {
        // I found cases where mpeg1 streams return the uncompressed bitrate
        // rendering absurdly high bit rates.
        // So we're getting the smaller from the two
        return fileBitRate > videoBitRate ? videoBitRate : fileBitRate;
      }

      if (videoBitRate > 0) {
        return videoBitRate;
      }

      if (fileBitRate > 0) {
        return fileBitRate;
      }

      return null;
    }

    bool isX265 = stream.codec_long_name?.contains('265') == true;
    double multiply = isX265 ? 2.4 : 1;

    final int? result = preCalculate();
    return result != null ? (result * multiply).toString() : null;
  }

  int _getVideoBitrateBasedOnVideoStreamBitRate(FFmpegStream stream) {
    return FFmpegUtils.getBitRate(stream);
  }

  int _getVideoBitrateBasedOnFileBitrate(FFprobe probe) {
    // file bitrate
    final int? fileBitrate =
        FFmpegUtils.getStringBitRate(probe.format.bit_rate);
    if (fileBitrate == null) {
      return 0;
    }

    // audio bitrate
    int abr = FFmpegUtils.getStreamBitRate(probe, _CODEC_TYPE_AUDIO);
    if (abr == 0) {
      // let's assume something
      abr = _LOW_BITRATE_AUDIO_STREAM;
    }

    return fileBitrate - abr;
  }
}
