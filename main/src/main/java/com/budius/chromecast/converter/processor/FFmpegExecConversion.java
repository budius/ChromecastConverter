package com.budius.chromecast.converter.processor;

import com.budius.chromecast.converter.RuntimeExec;
import com.budius.chromecast.converter.Utils;

/**
 * Created by budius on 28.04.16.
 */
public class FFmpegExecConversion implements Processor {
   @Override public Result process(Job job) {

      String[] cmd = Utils.getArray(job.ffmpegCmd);

      // execute video conversion
      if (!job.outputFolder.exists()) {
         job.outputFolder.mkdirs();
      }

      RuntimeExec rt = new RuntimeExec(cmd, job.baseFolder, RuntimeExec.FFMPEG);
      if (!rt.execute()) {
         return Result.fail("Failed to process file");
      }

      // check results
      if (!job.outputFile.exists() || job.outputFile.length() == 0) {
         if (job.outputFile.exists())
            job.outputFile.delete();
         return Result.fail("Failed to process file");
      }

      return Result.success();
   }
}
