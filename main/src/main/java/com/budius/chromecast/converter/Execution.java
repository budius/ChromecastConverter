package com.budius.chromecast.converter;

import com.budius.chromecast.converter.processor.Audio;
import com.budius.chromecast.converter.processor.CheckConversionCmd;
import com.budius.chromecast.converter.processor.DeleteOriginal;
import com.budius.chromecast.converter.processor.FFProbe;
import com.budius.chromecast.converter.processor.FFmpegCmdEnd;
import com.budius.chromecast.converter.processor.FFmpegCmdStart;
import com.budius.chromecast.converter.processor.FFmpegExecConversion;
import com.budius.chromecast.converter.processor.FFmpegExecSubtitles;
import com.budius.chromecast.converter.processor.Job;
import com.budius.chromecast.converter.processor.Processor;
import com.budius.chromecast.converter.processor.RenameTempFile;
import com.budius.chromecast.converter.processor.Subtitles;
import com.budius.chromecast.converter.processor.Video;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Divide the settings into jobs and run the processors
 */
public class Execution implements Runnable {

   private final Settings initialSettings;
   private final File baseFolder;

   public Execution(Settings initialSettings) {
      this.initialSettings = initialSettings;
      baseFolder = initialSettings.input.isDirectory() ? initialSettings.input : initialSettings.input.getParentFile();
   }

   @Override public void run() {
      if (initialSettings.input.isDirectory()) {

         int count = getVideoFileCount(initialSettings.input);
         Log.i(String.format("Executing Chromecast conversion for %s files.", count));

         executeFolder(initialSettings);
      } else {
         executeFile(initialSettings);
      }
   }

   private void executeFolder(Settings settings) {
      List<File> files = Arrays.asList(settings.input.listFiles(videoFilesFilter));
      Collections.sort(files, alphabeticalOrderFiles);

      for (File f : files) {
         if (f.isDirectory()) {
            executeFolder(settings.copy(f));
         } else {
            executeFile(settings.copy(f));
         }
      }
   }

   private void executeFile(Settings settings) {

      Job job = new Job(settings, baseFolder);
      List<Item> processors = Arrays.asList(

         // Probe the input file and extract information
         new Item("FFProbe", new FFProbe()),

         // Build FFMPEG command in parts
         new Item("CmdStart", new FFmpegCmdStart()),
         new Item("Video", new Video()),
         new Item("Audio", new Audio()),
         new Item("CmdEnd", new FFmpegCmdEnd()),

         // Build Subtitle extraction commands
         new Item("Subtitles", new Subtitles()),

         // Checks if any conversion is necessary and process the `move` flag
         new Item("CheckConversionCmd", new CheckConversionCmd()),

         // Execute FFMPEG commands
         new Item("Conversion", new FFmpegExecConversion()),
         new Item("Subtitles", new FFmpegExecSubtitles()),

         // delete original
         new Item("DeleteOriginal", new DeleteOriginal()),

         // Rename temp file
         new Item("RenameTempFile", new RenameTempFile())
      );

      // process all the processors and log any errors
      ExecutionLoop:
      for (Item item : processors) {
         Processor.Result result = item.processor.process(job);
         switch (result.code) {
            case Processor.Result.CODE_SUCCESS:
               // just goes to the next processor
               break;
            case Processor.Result.CODE_FAIL:
               Log.e(String.format("%s. Failed to `%s` for file %s.", result.message, item.name, settings.input.getAbsolutePath()));
               break ExecutionLoop;
            case Processor.Result.CODE_ABORT:
               if (result.message != null && result.message.length() > 0) {
                  Log.w(String.format("%s for file %s.", result.message, settings.input.getAbsolutePath()));
               }
               break ExecutionLoop;
         }
      }

      Log.i("Complete file " + settings.input);
   }

   //
   // Filters
   // =========================================================================================================
   private static final List<String> VIDEO_EXTENSION =
      Arrays.asList("mp4", "mkv", "avi", "mpeg", "mpg", "mpe", "mov", "qt", "asf", "flv", "wmv", "m1v", "m2v", "3gp");

   private static final FileFilter videoFilesFilter = new FileFilter() {
      @Override
      public boolean accept(File pathname) {
         String ext = FilenameUtils.getExtension(pathname.getAbsolutePath()).toLowerCase();
         return pathname.isDirectory() || VIDEO_EXTENSION.contains(ext);
      }
   };

   private static final Comparator<File> alphabeticalOrderFiles = new Comparator<File>() {
      @Override
      public int compare(File o1, File o2) {
         return o1.getName().compareTo(o2.getName());
      }
   };

   //
   // Helpers
   // =========================================================================================================
   private static int getVideoFileCount(File folder) {
      int count = 0;
      for (File f : folder.listFiles(videoFilesFilter)) {
         if (f.isDirectory()) {
            count += getVideoFileCount(f);
         } else {
            count++;
         }
      }
      return count;
   }

   static class Item {
      final String name;
      final Processor processor;

      Item(String name, Processor processor) {
         this.name = name;
         this.processor = processor;
      }
   }
}
