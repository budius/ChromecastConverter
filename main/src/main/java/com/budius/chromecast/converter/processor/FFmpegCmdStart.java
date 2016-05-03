package com.budius.chromecast.converter.processor;

/**
 * Created by budius on 28.04.16.
 */
public class FFmpegCmdStart implements Processor {
   @Override public Result process(Job job) {

      job.ffmpegCmd.add("ffmpeg");
      job.ffmpegCmd.add("-i");
      job.ffmpegCmd.add(job.settings.input.getAbsolutePath());

      return Result.success();
   }
}
