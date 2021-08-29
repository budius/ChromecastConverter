import 'dart:io';
import 'dart:convert';

import 'cli_runner.dart';
import 'timber.dart';

// for some reason `apt install ffmpeg` on latest ubuntu
// produces executable for ffprobe like this odd way, so we'll try both
const String _FFPROBE_ODD = 'ffmpeg.ffprobe';
const String _FFPROBE_NORMAL = 'ffprobe';

const String _TAG = 'FFprobe';

const List<String> _FFPROBE_ARGS = [
  '-v',
  'quiet',
  '-print_format',
  'json',
  '-show_format',
  '-show_streams'
];

class FFprobe {
  final List<FFmpegStream> streams;
  final Format format;

  FFprobe(this.streams, this.format);

  static Future<FFprobe> probe(File input) async {
    final List<String> args = _FFPROBE_ARGS.toList(growable: true)
      ..add(input.absolute.path);

    Timber.d(
        _TAG,
        '$_FFPROBE_NORMAL '
        '${args.join(' ').replaceAll('[', '').replaceAll(']', '')}');

    final CliRunner run1 = await CliRunner.run(_FFPROBE_NORMAL, args);
    if (run1.exitCode == 0) {
      return _parse(run1);
    }

    final CliRunner run2 = await CliRunner.run(_FFPROBE_ODD, args);
    if (run2.exitCode == 0) {
      return _parse(run2);
    }

    throw Exception('Failed to probe ${input.path}\n'
        'Code(${run1.exitCode}): ${run1.stderr}\n'
        'Code(${run2.exitCode}): ${run2.stderr}');
  }

  static FFprobe _parse(CliRunner run) {
    Map<String, dynamic> map = jsonDecode(run.stdout);
    return FFprobe(
        (map['streams'] as List<dynamic>)
            .map((e) => FFmpegStream._fromMap(e))
            .toList(growable: false),
        Format._fromMap(map['format']));
  }
}

class Format {
  final String? filename;
  final int? nb_streams;
  final int? nb_programs;
  final String? format_name;
  final String? format_long_name;
  final String? start_time;
  final String? duration;
  final String? size;
  final String? bit_rate;
  final int? probe_score;

  Format(
      this.filename,
      this.nb_streams,
      this.nb_programs,
      this.format_name,
      this.format_long_name,
      this.start_time,
      this.duration,
      this.size,
      this.bit_rate,
      this.probe_score);

  factory Format._fromMap(Map<String, dynamic> m) {
    return Format(
        m['filename'],
        m['nb_streams'],
        m['nb_programs'],
        m['format_name'],
        m['format_long_name'],
        m['start_time'],
        m['duration'],
        m['size'],
        m['bit_rate'],
        m['probe_score']);
  }
}

class StreamTags {
  final String? language;
  final String? title;

  StreamTags(this.language, this.title);

  factory StreamTags._fromMap(Map<String, dynamic> m) {
    return StreamTags(m['language'], m['title']);
  }
}

class FFmpegStream {
  final int index;
  final String? codec_name;
  final String? codec_long_name;
  final String? profile;
  final String? codec_type;
  final String? codec_time_base;
  final String? codec_tag_string;
  final String? codec_tag;
  final int? width;
  final int? height;
  final int? has_b_frames;
  final String? sample_aspect_ratio;
  final String? display_aspect_ratio;
  final String? pix_fmt;
  final int? level;
  final String? r_frame_rate;
  final String? avg_frame_rate;
  final String? time_base;
  final int? start_pts;
  final String? start_time;
  final String? bit_rate;
  final StreamTags? tags;
  final String? sample_fmt;
  final String? sample_rate;
  final int? channels;
  final String? channel_layout;
  final int? bits_per_sample;
  final int? duration_ts;
  final String? duration;

  FFmpegStream(
      this.index,
      this.codec_name,
      this.codec_long_name,
      this.profile,
      this.codec_type,
      this.codec_time_base,
      this.codec_tag_string,
      this.codec_tag,
      this.width,
      this.height,
      this.has_b_frames,
      this.sample_aspect_ratio,
      this.display_aspect_ratio,
      this.pix_fmt,
      this.level,
      this.r_frame_rate,
      this.avg_frame_rate,
      this.time_base,
      this.start_pts,
      this.start_time,
      this.bit_rate,
      this.tags,
      this.sample_fmt,
      this.sample_rate,
      this.channels,
      this.channel_layout,
      this.bits_per_sample,
      this.duration_ts,
      this.duration);

  factory FFmpegStream._fromMap(Map<String, dynamic> m) {
    return FFmpegStream(
        m['index'],
        m['codec_name'],
        m['codec_long_name'],
        m['profile'],
        m['codec_type'],
        m['codec_time_base'],
        m['codec_tag_string'],
        m['codec_tag'],
        m['width'],
        m['height'],
        m['has_b_frames'],
        m['sample_aspect_ratio'],
        m['display_aspect_ratio'],
        m['pix_fmt'],
        m['level'],
        m['r_frame_rate'],
        m['avg_frame_rate'],
        m['time_base'],
        m['start_pts'],
        m['start_time'],
        m['bit_rate'],
        StreamTags._fromMap(m['tags']),
        m['sample_fmt'],
        m['sample_rate'],
        m['channels'],
        m['channel_layout'],
        m['bits_per_sample'],
        m['duration_ts'],
        m['duration']);
  }
}
