ChromecastConverter
===================

1-click solution to convert single video or entire collection to Chromecast compatible format.

 - Different from other converters because this is dedicated to Chromecast.
 - It's faster than other converters because it checks if the current video streams are compatible with Chromecast and only converts what is necessary.
 - It also extract subtitle streams (if available) so they can be send to Chromecast.

## FFMPEG:
This software is a FFMPEG wrapper and it heavily relies on ffmpeg and ffprobe be available in the path.

That means that if you type `ffmpeg` and `ffprobe` in the command line it should give an output with the installed version

FFMPEG can be freely downloaded from the official site https://ffmpeg.org/download.html

For a better audio quality it is HIGHLY recommended FFMPEG V3.0 or higher as per this commit http://git.videolan.org/?p=ffmpeg.git;a=commit;h=d9791a8656b5580756d5b7ecc315057e8cd4255e

## Installation:
Simply unzip the files and that's it.

Execute by running `./cc_converter` (or `cc_converter.bat` on Windows) from the command line

## Usage:
```
./cc_converter <opts>
   -i,--input <arg>     Input file or folder
   -o,--output <arg>    Output folder (optional)
   -q,--quality <arg>   Quality: high, normal, low. Default is high (optional)
   -s,--speed <arg>     Speed: ultrafast, superfast, veryfast, faster, fast,
                        medium, slow, slower, veryslow. Default is slow (optional)
   -f,--force           Force conversion (even if input codecs are correct)
   -d,--delete          Delete the original file upon successful conversion
      --DEBUG           Debug mode with more logs
```

For more about quality and speed parameters check the official FFMPEG docs:

https://trac.ffmpeg.org/wiki/Encode/H.264

ChromecastConverter uses CRF (constant rate factor) and defaults to good quality for both audio and video.

- high quality = video CRF 18, audio 80kb/s per channel
- normal quality = video CRF 23, audio 64kb/s per channel
- low quality = video CRF 26, audio 48kb/s per channel


## Examples:

- Default values, single video, result in same folder
```
./cc_converter -i ~/Videos/awesomeVideo.mkv
```

- Default values, single video, result in different folder
```
./cc_converter -i ~/Videos/awesomeVideo.mov -o ~/Videos/chromecast/
```

- Default values, entire collection to different folder
```
./cc_converter -i ~/Videos/ -o ~/ChromecastVideos
```

- Default values, entire collection to different folder, delete after success conversion
```
./cc_converter -i ~/Videos/ -o ~/ChromecastVideos -d
```

- Single video, result in same folder, low quality, fast conversion
```
./cc_converter -i ~/Videos/awesomeVideo.avi -q low -s fast
```

- Single video, result in different folder, lossless, very slow conversion
```
./cc_converter -i ~/Videos/awesomeVideo.3gp -o ~/Chromecast/ -q 0 -s veryslow
```

That's the base functionality that I wanted for it for my personal use, but I'll be super happy to accept PullRequest contributions.

Developed by budius:
====================
 - http://stackoverflow.com/users/906362/budius
 - https://play.google.com/store/apps/developer?id=nothing+inc
 - http://forum.xda-developers.com/showthread.php?t=2699870

