package com.budius.chromecast.converter.processor;

import com.budius.chromecast.converter.Settings;
import com.budius.chromecast.converter.Utils;
import com.budius.chromecast.converter.ffprobe_model.Probe;
import com.budius.chromecast.converter.ffprobe_model.Stream;

import java.util.ArrayList;

/**
 * Created by budius on 28.04.16.
 */
public class Video implements Processor {

   public static final String CODEC_TYPE_VIDEO = "video";
   public static final String CODEC_TYPE_AUDIO = "audio";

   private static final String GOOD_VIDEO_CODEC = "h264";
   private static final String GOOD_VIDEO_PROFILE = "High";

   @Override public Result process(Job job) {

      // check file contains video stream
      Stream videoStream = Utils.getStream(job.ffProbe, CODEC_TYPE_VIDEO);
      if (videoStream == null) {
         return Result.fail("No video stream available");
      }

      job.ffmpegCmd.add("-c:v");

      // check video stream is already in a good codec and profile
      if (GOOD_VIDEO_CODEC.equals(videoStream.getCodec_name())
            && GOOD_VIDEO_PROFILE.equals(videoStream.getProfile())
            && !job.settings.force) {
         job.ffmpegCmd.add("copy");
         return Result.success();
      } else {

         // check ffmpeg supports libx264
         if (!Utils.supportsCodec("libx264")) {
            return Result.fail("No suitable video encoder available");
         }

         // add commands
         job.ffmpegCmd.add("libx264");
         job.ffmpegCmd.add("-profile:v");
         job.ffmpegCmd.add("high");
         job.ffmpegCmd.add("-level");
         job.ffmpegCmd.add("5");
         job.ffmpegCmd.add("-preset");
         job.ffmpegCmd.add(job.settings.speed);
         job.ffmpegCmd.add("-crf");
         job.ffmpegCmd.add(Integer.toString(job.settings.quality));

         String videoBitRate = getVideoBitrate(job.ffProbe);
         if (videoBitRate != null) {
            job.ffmpegCmd.add("-maxrate");
            job.ffmpegCmd.add(videoBitRate);
            job.ffmpegCmd.add("-bufsize");
            job.ffmpegCmd.add("5M");
         }

         return Result.success();
      }
   }

   public static boolean addCmd(Probe ffProbe, ArrayList<String> cmd, Settings settings) {

      Stream videoStream = Utils.getStream(ffProbe, CODEC_TYPE_VIDEO);
      if (videoStream == null)
         return false;

      if (GOOD_VIDEO_CODEC.equals(videoStream.getCodec_name())
            && GOOD_VIDEO_PROFILE.equals(videoStream.getProfile())) {
         cmd.add("-vcodec");
         cmd.add("copy");
      } else {

         if (!Utils.supportsCodec("libx264")) {
            return false;
         }

         cmd.add("-c:v");
         cmd.add("libx264");
         cmd.add("-profile:v");
         cmd.add("high");
         cmd.add("-level");
         cmd.add("5");
         cmd.add("-preset");
         cmd.add(settings.speed);
         cmd.add("-crf");
         cmd.add(Integer.toString(settings.quality));

         String videoBitRate = getVideoBitrate(ffProbe);
         if (videoBitRate != null) {
            cmd.add("-maxrate");
            cmd.add(videoBitRate);
            cmd.add("-bufsize");
            cmd.add("5M");
         }
      }

      return true;
   }

   private static String getVideoBitrate(Probe ffProbe) {

        /*
        I found cases where mpeg1 streams return the uncompressed bitrate, rendering absurdly high bit rates.
        So we're getting the smaller from the two
        */

      long br_stream = getVideoBitrateBasedOnVideoStreamBitRate(ffProbe);
      long br_file = getVideoBitrateBasedOnFileBitrate(ffProbe);
      long br = br_file > br_stream ? br_stream : br_file;

      if (br <= 0)
         return null;
      else
         return Long.toString(br);
   }

   private static long getVideoBitrateBasedOnFileBitrate(Probe ffProbe) {
      // file bitrate
      String fileBitrate = ffProbe.getFormat().getBit_rate();
      long fbr;
      if (fileBitrate == null || fileBitrate.trim().length() == 0) {
         return 0l;
      } else {
         try {
            fbr = Long.parseLong(fileBitrate);
         } catch (Exception e) {
            // number format exception
            return 0l;
         }
      }

      // audio bitrate
      long abr;
      Stream audioStream = Utils.getStream(ffProbe, CODEC_TYPE_AUDIO);
      if (audioStream == null) {
         abr = 0;
      } else {
         String audioBitrate = audioStream.getBit_rate();
         try {
            abr = Long.parseLong(audioBitrate);
         } catch (Exception e) {
            // number format exception
            abr = 96 * 1024; // just a wild guess on some low-ish value
         }
      }

      return fbr - abr;
   }

   private static long getVideoBitrateBasedOnVideoStreamBitRate(Probe ffProbe) {
      Stream videoStream = Utils.getStream(ffProbe, CODEC_TYPE_VIDEO);
      try {
         return Long.parseLong(videoStream.getBit_rate());
      } catch (Exception e) {
         // number format exception
         return 0l;
      }
   }
}
