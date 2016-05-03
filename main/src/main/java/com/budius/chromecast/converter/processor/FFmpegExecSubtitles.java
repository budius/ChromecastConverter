package com.budius.chromecast.converter.processor;

import com.budius.chromecast.converter.RuntimeExec;

/**
 * Created by budius on 28.04.16.
 */
public class FFmpegExecSubtitles implements Processor {
   @Override public Result process(Job job) {

      for (String[] cmd : job.subtitles) {
         if (cmd != null && cmd.length > 0) {
            RuntimeExec rt = new RuntimeExec(cmd, job.baseFolder, RuntimeExec.LOG);
            if (!rt.execute()) {
               return Result.abort("Failed to extract subtitle");
            }
         }
      }
      return Result.success();
   }
}
