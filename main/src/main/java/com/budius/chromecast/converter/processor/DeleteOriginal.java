package com.budius.chromecast.converter.processor;

/**
 * Created by budius on 28.04.16.
 */
public class DeleteOriginal implements Processor {
   @Override public Result process(Job job) {

      // delete original
      if (job.settings.delete) {
         if (!job.settings.input.delete()) {
            return Result.fail("Failed to delete file");
         }
      }

      return Result.success();
   }
}
