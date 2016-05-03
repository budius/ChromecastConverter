package com.budius.chromecast.converter;

import com.budius.chromecast.converter.ffprobe_model.Probe;
import com.budius.chromecast.converter.ffprobe_model.Stream;
import com.budius.chromecast.converter.processor.Job;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by budius on 28.04.16.
 */
public class Utils {

   public static boolean supportsCodec(String codec) {
      String response = RuntimeExec.get(new String[]{"ffmpeg", "-h", String.format("encoder=%s", codec)});
      return response != null && !response.contains(String.format("Codec '%s' is not recognized by FFmpeg", codec));
   }

   public static String cmdToString(String[] cmd) {
      return Arrays.toString(cmd).replace(",", "").trim();
   }

   public static File getFilename(Job job, String extension) {
      File f = null;
      int unique = 0;
      while (f == null || f.exists()) {

         if (f != null) { // not first run anymore
            unique++;
            extension = "_" + Integer.toString(unique) + extension;
         }

         String name = job.settings.input.getName();
         String fileName = FilenameUtils.removeExtension(name) + extension;
         f = new File(job.outputFolder, fileName);
      }
      return f;
   }

   public static String[] getArray(List<String> list) {
      String[] strings = new String[list.size()];
      strings = list.toArray(strings);
      return strings;
   }

   public static Stream getStream(Probe ffProbe, String name) {
      for (int i = 0; i < ffProbe.getStreams().size(); i++)
         if (name.equals(ffProbe.getStreams().get(i).getCodec_type()))
            return ffProbe.getStreams().get(i);
      return null;
   }
}
