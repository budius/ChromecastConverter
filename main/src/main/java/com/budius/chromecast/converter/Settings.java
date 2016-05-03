package com.budius.chromecast.converter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Created by budius on 27.04.16.
 */
public class Settings {


   public static final List<String> SPEED = Arrays.asList(
      "ultrafast",
      "superfast",
      "veryfast",
      "faster",
      "fast",
      "medium",
      "slow",
      "slower",
      "veryslow"
   );

   public static final List<String> QUALITY = Arrays.asList(
      "high", "normal", "low"
   );

   public static final int[] I_ARRAY_QUALITY = {
      18, 23, 26,};

   public final File input;
   public final File output;
   public final String speed;
   public final int quality;
   public final boolean delete;
   public final boolean force;

   public static final String DEFAULT_SPEED = "slow";
   public static final String DEFAULT_QUALITY = "high";

   public Settings(File input, File output, String speed, int quality, boolean delete, boolean force) {
      this.input = input;
      this.output = output;
      this.speed = speed;
      this.quality = quality;
      this.delete = delete;
      this.force = force;
   }

   public Settings copy(File input) {
      return new Settings(input, this.output, this.speed, this.quality, this.delete, this.force);
   }
}
