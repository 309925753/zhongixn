package com.vkzwbim.chat.util;

import android.media.MediaMetadataRetriever;

import java.io.File;

import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.MultimediaInfo;

public class VideoLengthUtil {
    public static String getLocalVideoDuration(String videoPath) {
        long duration=0;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(videoPath);
            duration = Integer.parseInt(mmr.extractMetadata
                    (MediaMetadataRetriever.METADATA_KEY_DURATION));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
        String videoLenght=("00:" + String.format("%02d", duration / 1000));
        return videoLenght;
    }


    public static String getLength(String filepath){
        File source = new File(filepath);
        Encoder encoder = new Encoder();
        long ls=0;
        try {
            MultimediaInfo m = encoder.getInfo(source);
            ls  = m.getDuration();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ls+"";
    }
}
