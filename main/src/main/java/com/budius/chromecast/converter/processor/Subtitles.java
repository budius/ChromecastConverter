package com.budius.chromecast.converter.processor;

import com.budius.chromecast.converter.Utils;
import com.budius.chromecast.converter.ffprobe_model.Stream;
import com.budius.chromecast.converter.ffprobe_model.Tags;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by budius on 28.04.16.
 */
public class Subtitles implements Processor {

   private static final String ALLOWED_CHARS = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";

   private static final String CODEC_TYPE_SUBTITLE = "subtitle";
   private static final String INVALID_SUBTITLE = "dvd_subtitle";
   private static final String SUBT_EXT = ".srt";

   @Override public Result process(Job job) {

      Map<String, SubtitlesMeta> languages = new HashMap<>();
      int subtitlesCount = 0;
      // pre-map language names to avoid duplication issues
      for (Stream s : job.ffProbe.getStreams()) {
         if (s != null &&
               CODEC_TYPE_SUBTITLE.equals(s.getCodec_type()) &&
               !INVALID_SUBTITLE.equals(s.getCodec_name())) {

            SubtitlesMeta meta = SubtitlesMeta.fromStream(s, subtitlesCount);
            subtitlesCount++;

            if (languages.containsKey(meta.language)) {
               String disambiguation = disambiguation(languages.get(meta.language), meta);
               meta = new SubtitlesMeta(disambiguation, meta.title, meta.position);
               int whileCounter = 1;
               while (languages.containsKey(meta.language)) {
                  String language = meta.language + "-" + whileCounter;
                  whileCounter++;
                  meta = new SubtitlesMeta(language, meta.title, meta.position);
               }
            }
            languages.put(meta.language, meta);
         }
      }

      for (SubtitlesMeta meta : languages.values()) {
         job.subtitles.add(getSubtitleCmd(job, meta));
      }
      
      return Result.success();
   }


   private String[] getSubtitleCmd(Job job, SubtitlesMeta meta) {

      File fileName = Utils.getFilename(job, "_" + meta.language.toUpperCase(Locale.ENGLISH) + SUBT_EXT);

      ArrayList<String> cmd = new ArrayList<String>();
      cmd.add("ffmpeg");
      cmd.add("-i");
      cmd.add(job.settings.input.getAbsolutePath());
      cmd.add("-vn");
      cmd.add("-an");
      cmd.add("-map");
      cmd.add("0:s:" + meta.position);
      cmd.add(fileName.getAbsolutePath());

      return Utils.getArray(cmd);
   }

   static String disambiguation(SubtitlesMeta existing, SubtitlesMeta added) {

      // remove duplication from title
      String posfix = existing.title != null ?
            added.title.replace(existing.title, "") :
            "";
      char[] posfixArray = posfix.toCharArray();

      // remove not allowed characters
      for (char c : posfixArray) {
         String charString = String.valueOf(c);
         if (!ALLOWED_CHARS.contains(charString)) {
            posfix = posfix.replace(charString, "");
         }
      }

      if (posfix.isEmpty()) {
         return added.language;
      } else {
         return added.language + "-" + posfix;
      }
   }

   static class SubtitlesMeta {
      final String language;
      final String title;
      final int position;

      SubtitlesMeta(String language, String title, int position) {
         this.language = language;
         this.title = title;
         this.position = position;
      }

      static SubtitlesMeta fromStream(Stream s, int position) {
         Tags t = s.getTags();
         if (t != null && t.getLanguage() != null) {
            return new SubtitlesMeta(t.getLanguage(), t.getTitle(), position);
         } else {
            return new SubtitlesMeta("DEFAULT", "DEFAULT", position);
         }
      }
   }
}
