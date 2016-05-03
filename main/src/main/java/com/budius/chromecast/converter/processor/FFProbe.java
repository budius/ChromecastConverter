package com.budius.chromecast.converter.processor;

import com.budius.chromecast.converter.RuntimeExec;
import com.budius.chromecast.converter.ffprobe_model.Probe;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

/**
 * Created by budius on 28.04.16.
 */
public class FFProbe implements Processor {

   private final String[] CMD_JSON =
      {"ffprobe", "-v", "quiet", "-print_format", "json", "-show_format", "-show_streams", ""};

   @Override public Result process(Job job) {

      File file = job.settings.input;

      String filePath = file.getAbsolutePath();

      RuntimeExec rt;

      // real data ===============================
      CMD_JSON[CMD_JSON.length - 1] = filePath;
      rt = new RuntimeExec(CMD_JSON, null, RuntimeExec.STRING_RESPONSE);

      if (rt.execute()) {
         String response = rt.getResponse();
         Gson gson = new GsonBuilder().create();
         job.ffProbe = gson.fromJson(response, Probe.class);
         return Result.success();
      }

      return Result.fail("Failed to probe file");
   }
}
