package com.budius.chromecast.converter.processor;

import com.budius.chromecast.converter.Log;
import com.budius.chromecast.converter.RuntimeExec;
import com.budius.chromecast.converter.ffprobe_model.Probe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

/**
 * Created by budius on 28.04.16.
 */
public class FFProbe implements Processor {

   // for some reason `apt install ffmpeg` on latest ubuntu
   // produces executable for ffprobe like this odd way, so we'll try both
   private static final String FFPROBE_ODD = "ffmpeg.ffprobe";
   private static final String FFPROBE_NORMAL = "ffprobe";

   private final String[] CMD_JSON =
         {FFPROBE_NORMAL, "-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", ""};

   @Override public Result process(Job job) {

      File file = job.settings.input;

      String filePath = file.getAbsolutePath();

      // real data ===============================
      CMD_JSON[CMD_JSON.length - 1] = filePath;
      Result result = tryExecute(CMD_JSON, job);
      if (result.code == Result.CODE_FAIL) {

         Log.i("First attempt at ffprobe failed, try again with ffmpeg.ffprobe");

         // try again with the variation of ffprobe
         CMD_JSON[0] = FFPROBE_ODD;
         result = tryExecute(CMD_JSON, job);
      }
      return result;
   }

   private Result tryExecute(String[] cmd, Job job) {
      RuntimeExec rt = new RuntimeExec(cmd, null, RuntimeExec.STRING_RESPONSE);

      if (rt.execute()) {
         String response = rt.getResponse();
         Gson gson = new GsonBuilder().create();
         job.ffProbe = gson.fromJson(response, Probe.class);
         return Result.success();
      }

      return Result.fail("Failed to probe file");
   }
}
