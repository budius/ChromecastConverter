package com.budius.chromecast.converter.processor;

import com.budius.chromecast.converter.Settings;
import com.budius.chromecast.converter.ffprobe_model.Probe;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by budius on 28.04.16.
 */
public class Job {

   public Job(Settings settings, File baseFolder) {
      this.settings = settings;
      this.baseFolder = baseFolder;

      // Output location
      outputFolder = new File(
         settings.output, // base output folder
         settings.input.getParentFile()
            .getAbsolutePath()
            .replace(baseFolder.getAbsolutePath(), "")); // input relative to baseInput

   }

   public final Settings settings;
   public final File outputFolder;
   public final File baseFolder;

   public File outputFile;
   public Probe ffProbe;
   public List<String> ffmpegCmd = new ArrayList<>();
   public List<String[]> subtitles = new ArrayList<>();
}
