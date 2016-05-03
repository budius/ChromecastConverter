package com.budius.chromecast.converter.processor;

import com.budius.chromecast.converter.Utils;

import java.util.Random;

/**
 * Created by budius on 28.04.16.
 */
public class FFmpegCmdEnd implements Processor {

   private static final String VIDEO_EXT = ".mp4";
   private static final Random RANDOM = new Random();

   @Override public Result process(Job job) {

      // movflags, faststart
      job.ffmpegCmd.add("-movflags");
      job.ffmpegCmd.add("+faststart");

      // calculate output to temporary file
      job.outputFile = Utils.getFilename(job, "_TEMP_" + RANDOM.nextInt(999) + "_" + VIDEO_EXT);

      // output file
      job.ffmpegCmd.add(job.outputFile.getAbsolutePath());

      return Result.success();
   }
}
