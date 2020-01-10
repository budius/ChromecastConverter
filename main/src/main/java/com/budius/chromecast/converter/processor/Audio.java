package com.budius.chromecast.converter.processor;

import com.budius.chromecast.converter.Utils;
import com.budius.chromecast.converter.ffprobe_model.Stream;

import java.util.Arrays;
import java.util.List;

/**
 * Created by budius on 27.04.16.
 */
public class Audio implements Processor {

   private static final List<String> GOOD_CODECS = Arrays.asList("mp3", "aac");

   public static final String CODEC_TYPE_AUDIO = "audio";


   @Override public Result process(Job job) {

      job.ffmpegCmd.add("-c:a");

      Stream audioStream = Utils.getStream(job.ffProbe, CODEC_TYPE_AUDIO);
      if (audioStream == null) {
         return Result.fail("No audio stream available");
      }

      String codecName = audioStream.getCodec_name().toLowerCase();

      // no need to recode anything, just copy the audio stream
      if (GOOD_CODECS.contains(codecName) &&
            audioStream.getChannels() <= 2 &&
            !job.settings.force) {
         job.ffmpegCmd.add("copy");
         return Result.success();
      }

      // Starting from this commit
      // http://git.videolan.org/?p=ffmpeg.git;a=commit;h=d9791a8656b5580756d5b7ecc315057e8cd4255e
      // FFMPEG native AAC is the recommended way, AAC encoder
      List<Codec> codecs = Arrays.asList(
         new AAC(job.settings.quality),
         new LIBFDK_AAC(job.settings.quality), // non-free
         new MP3(job.settings.quality)
      );

      for (Codec codec : codecs) {
         if (codec.isSupported()) {
            job.ffmpegCmd.add(codec.libName);
            codec.addCmd(job.ffmpegCmd, audioStream);
            job.ffmpegCmd.add("-ac");
            job.ffmpegCmd.add("2");
            return Result.success();
         }
      }
      return Result.fail("No suitable audio encoder available");
   }

   /**
    * MP3
    * ==============================================================================================
    */
   private static class MP3 extends Codec {

      protected MP3(int quality) {
         super("libmp3lame", quality);
      }

      @Override void addCmd(List<String> cmd, Stream audioStream) {
         cmd.add("-q:a");
         int val;
         switch (quality) {
            case Q_HIGH:
               val = 1;
               break;
            case Q_MED:
               val = 4;
               break;
            default:
               val = 6;
         }
         cmd.add(Integer.toString(val));
      }
   }

   /**
    * AAC
    * ==============================================================================================
    */
   private static class AAC extends AAC_BASE_Codec {

      protected AAC(int quality) {
         super("aac", quality);
      }
   }

   private static class LIBFDK_AAC extends AAC_BASE_Codec {

      protected LIBFDK_AAC(int quality) {
         super("libfdk_aac", quality);
      }

      @Override void addCmd(List<String> cmd, Stream audioStream) {
         cmd.add("-cutoff");
         cmd.add("18000");
         super.addCmd(cmd, audioStream);
      }
   }

   private abstract static class AAC_BASE_Codec extends Codec {

      protected AAC_BASE_Codec(String libName, int quality) {
         super(libName, quality);
      }

      @Override void addCmd(List<String> cmd, Stream audioStream) {
         cmd.add("-b:a");
         int val;
         switch (quality) {
            case Q_HIGH:
               val = 80;
               break;
            case Q_MED:
               val = 64;
               break;
            case Q_LOW:
            default:
               val = 48;
         }

         long audioChannels = Math.min(2, audioStream.getChannels());
         long bitrate = val * audioChannels * 1024;

         cmd.add(Long.toString(bitrate));
      }
   }

   /**
    * GENERAL CODEC
    * ==============================================================================================
    */
   private abstract static class Codec {
      final String libName;
      static final int Q_HIGH = 1;
      static final int Q_MED = 2;
      static final int Q_LOW = 3;
      final int quality;

      protected Codec(String libName, int quality) {
         this.libName = libName;
         if (quality <= 20) {
            this.quality = Q_LOW;
         } else if (quality <= 25) {
            this.quality = Q_MED;
         } else {
            this.quality = Q_HIGH;
         }
      }

      boolean isSupported() {
         return Utils.supportsCodec(libName);
      }

      abstract void addCmd(List<String> cmd, Stream audioStream);

   }
}
