package com.budius.chromecast.converter.processor;

import com.budius.chromecast.converter.Utils;

import java.io.File;

/**
 * Created by budius on 30.04.16.
 */
public class RenameTempFile implements Processor {

   private static final String VIDEO_EXT = ".mp4";

   @Override public Result process(Job job) {

      File finalOutput = Utils.getFilename(job, VIDEO_EXT);
      return job.outputFile.renameTo(finalOutput) ? Result.success() : Result.fail("Failed to rename temp file");

   }
}
