package com.budius.chromecast.converter.processor;

import com.budius.chromecast.converter.Utils;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by budius on 20.05.16.
 * This processor is the last step before the time consuming conversions happen.
 * So, it basically checks the whole job to make sure all details are relevant, adequate, and will work.
 */
public class CheckConversionCmd implements Processor {

   private static final String VIDEO_EXT = ".mp4";

   @Override public Result process(Job job) {

      String[] cmd = Utils.getArray(job.ffmpegCmd);

      if (cmd == null || cmd.length == 0) {
         return Result.fail("No FFMPEG command found");
      }

      if (Utils.cmdToString(cmd).contains("-c:v copy -c:a copy")
            && job.ffProbe.getFormat().getFilename().toLowerCase().endsWith(".mp4")
            && job.ffProbe.getFormat().getFormat_name().toLowerCase().contains("mp4")) {

         // that means we don't have to process anything in this file
         // so we'll just check input/output folder and
         // move or copy the file depending on the `delete` flag

         if (job.outputFolder.equals(job.settings.input.getParentFile())) {

            // if the output folder is the same, we don't have to do anything
            return Result.abort(); // abort silently

         } else {

            // there's a lot of cases and stuff to check here,
            // so we'll break it into this separate method(s)
            return process_NoConversion_DifferentFolder(job);
         }

      } else {
         return Result.success();
      }
   }

   private Result process_NoConversion_DifferentFolder(Job job) {

      // The output file from the job is the temporary file,
      // but here we want to deal directly with the final file
      File target = Utils.getFilename(job, VIDEO_EXT);

      // if delete, try to just rename the file
      // the original will disappear and the new will be there, all good
      if (job.settings.delete && job.settings.input.renameTo(target)) {

         // renamed without issues
         return Result.abort(); // abort silently

      }

      // if not delete, or didn't successfully rename (aka.: different drive), do a copy
      else {
         try {

            FileUtils.copyFile(job.settings.input, target);

            if (job.settings.delete) {
               if (!job.settings.input.delete()) {
                  return Result.fail("Failed to delete file");
               }
            }

            // successfully copied file and deleted original (if needed)
            return Result.abort(); // abort silently

         } catch (IOException e) {
            return Result.fail("No conversion needed, but failed to copy original file to " + target.getAbsolutePath() + ". Error message: " + e.getMessage());
         }
      }
   }
}
