package com.example.musicpalyertest;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class MusicUtils {
    public static List<String> getMusicData(Context context) {
        List<String> list = new ArrayList<>();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.AudioColumns.IS_MUSIC);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)) > 1000 * 800) {
                    list.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
                }
            }
        }
        cursor.close();
//        if (cursor != null) {
//            while (cursor.moveToNext()) {
//                Song song = new Song();
//                song.setSong( cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
//                song.setSinger( cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)));
//                song.setPath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
//                song.setDuration( cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
//                song.setSize( cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)));
//                if (song.getSize() > 1000 * 800) {//过滤掉短音频
//                    // 分离出歌曲名和歌手
//                    if (song.getSong().contains("-")) {
//                        String[] str = song.getSong().split("-");
//                        song.setSinger( str[0]);
//                        song.setSong( str[1]);
//                    }
//                    list.add(song);
//                }
//            }
//            // 释放资源
//            cursor.close();
//        }
        return list;
    }


    public static List<String> getMusicName(Context context) {
        List<String> list = new ArrayList<>();
        //扫描手机自身存储，获取音乐文件
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                MediaStore.Audio.AudioColumns.IS_MUSIC);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)) > 1000 * 800) {
                    list.add(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)));
                }
            }
        }
        cursor.close();
        return list;
    }
}
