import 'settings.dart';
import 'steps/delete_original_step.dart';
import 'steps/ffmpeg_check_conversion_cmd_step.dart';
import 'steps/ffmpeg_cmd_audio_step.dart';
import 'steps/ffmpeg_cmd_end_step.dart';
import 'steps/ffmpeg_cmd_start_step.dart';
import 'steps/ffmpeg_subtitle_cmd_step.dart';
import 'steps/ffmpeg_cmd_video_step.dart';
import 'steps/ffmpeg_execute_conversion_step.dart';
import 'steps/ffmpeg_subtitle_execute_step.dart';
import 'steps/rename_temp_file_step.dart';
import 'steps/step.dart';
import 'steps/probe_step.dart';

class Steps {
  Steps._();

  static List<Step> extract(BatchSettings settings) {
    return <Step>[
      ProbeStep(),
      for (Step s in subtitlesSteps(settings)) s,
      if (!settings.onlySubtitles)
        for (Step c in conversionSteps(settings)) c,
    ];
  }

  static List<Step> subtitlesSteps(BatchSettings s) {
    return <Step>[
      SubtitleCmdStep(),
      if (!s.dryRun) FFmpegExecuteSubtitlesStep()
    ];
  }

  static List<Step> conversionSteps(BatchSettings s) {
    return <Step>[
      ConversionStartCmdStep(),
      ConversionVideoCmdStep(),
      ConversionAudioCmdStep(),
      ConversionEndCmdStep(),
      CheckConversionCmdStep(),
      if (!s.dryRun) FFmpegExecuteConversionStep(),
      if (!s.dryRun && s.deleteOnSuccess) DeleteOriginalFileStep(),
      if (!s.dryRun) RenameTempFileStep()
    ];
  }
}
