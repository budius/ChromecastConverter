ChromecastConverter
===================

1-click solution to convert video collection to Chromecast compatible format.

The actual app is just a FFMPEG wrapper.
It relies on ffmpeg and ffprobe be available in the path and they must have been compiled with x264 and libfdk_aac.

Different from other converters this is dedicated to Chromecast.
It checks if the current video streams are compatible with Chromecast and only converts them if necessary.
It also extract subtitle streams so they can be injected separately.
At the end of the batch operation it deletes the original file.


That's the base functionality that I wanted for it for my personal use, but I'll be super happy to accept PullRequest contributions (specially on the GUI part) to allow it to be a bit more flexible. E.g.: save to a different folder, checkbox to delete or not the original, some base quality settings to adjust MAX and DEFAULT bitrates.


Developed by budius:
http://stackoverflow.com/users/906362/budius
https://play.google.com/store/apps/developer?id=nothing+inc
http://forum.xda-developers.com/member.php?u=2543713

