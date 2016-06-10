package com.budius.chromecast.converter.processor;

/**
 * Created by budius on 28.04.16.
 */
public interface Processor {

   public Processor.Result process(Job job);

   public static class Result {

      public static final int CODE_SUCCESS = 0; // success, keep processing
      public static final int CODE_FAIL = 1; // fail, write to logger and finish job
      public static final int CODE_ABORT = 2; // abort, no action required

      public final int code;
      public final String message;

      private Result(int code, String message) {
         this.code = code;
         this.message = message;
      }

      public static Result success() {
         return new Result(CODE_SUCCESS, null);
      }

      public static Result abort(String message) {
         return new Result(CODE_ABORT, message);
      }

      public static Result abort() {
         return new Result(CODE_ABORT, null);
      }

      public static Result fail(String message) {
         return new Result(CODE_FAIL, message);
      }
   }

}
