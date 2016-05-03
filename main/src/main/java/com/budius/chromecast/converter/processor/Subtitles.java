package com.budius.chromecast.converter.processor;

import com.budius.chromecast.converter.Utils;
import com.budius.chromecast.converter.ffprobe_model.Stream;
import com.budius.chromecast.converter.ffprobe_model.Tags;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by budius on 28.04.16.
 */
public class Subtitles implements Processor {

   private static final String CODEC_TYPE_SUBTITLE = "subtitle";
   private static final String INVALID_SUBTITLE = "dvd_subtitle";
   private static final String SUBT_EXT = ".srt";

   @Override public Result process(Job job) {

      // check all streams for subtitles
      for (Stream s : job.ffProbe.getStreams()) {
         if (s != null &&
            CODEC_TYPE_SUBTITLE.equals(s.getCodec_type()) &&
            !INVALID_SUBTITLE.equals(s.getCodec_name())) {

            job.subtitles.add(getSubtitleCmd(job, s, job.subtitles.size()));

         }
      }

      return Result.success();
   }


   private String[] getSubtitleCmd(Job job, Stream subtitle, int position) {

      // get the language code for the filename
      String language = null;
      Tags t = subtitle.getTags();
      if (t != null) {
         language = t.getLanguage();
      }

      if (language == null)
         language = "DEFAULT";

      File fileName = Utils.getFilename(job, "_" + language.toUpperCase(Locale.ENGLISH) + SUBT_EXT);

      ArrayList<String> cmd = new ArrayList<String>();
      cmd.add("ffmpeg");
      cmd.add("-i");
      cmd.add(job.settings.input.getAbsolutePath());
      cmd.add("-vn");
      cmd.add("-an");
      cmd.add("-map");
      cmd.add("0:s:" + position);
      cmd.add(fileName.getAbsolutePath());

      return Utils.getArray(cmd);
   }
}
