package com.example.market;

import android.content.Context;
import android.media.MediaPlayer;
import android.content.SharedPreferences;

public class MusicManager {
    private static MediaPlayer mediaPlayer;
    private static boolean isPlaying = false;
    private static SharedPreferences prefs;

    public static void init(Context context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences("music_prefs", Context.MODE_PRIVATE);
        }

        // Восстанавливаем состояние музыки из настроек
        isPlaying = prefs.getBoolean("music_enabled", false);

        if (isPlaying) {
            playMusic(context);
        }
    }

    public static void playMusic(Context context) {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.swag_music);
            mediaPlayer.setLooping(true); // Зацикливаем музыку
        }

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            isPlaying = true;
            saveMusicState(true);
        }
    }

    public static void stopMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            saveMusicState(false);
        }
    }

    public static void toggleMusic(Context context) {
        if (isPlaying) {
            stopMusic();
        } else {
            playMusic(context);
        }
    }

    public static boolean isMusicPlaying() {
        return isPlaying;
    }

    public static String getMusicButtonText() {
        return isPlaying ? "SWAG OFF" : "SWAG ON";
    }

    public static int getMusicButtonColor() {
        return isPlaying ? R.color.swag_green_dark : R.color.swag_green;
    }

    private static void saveMusicState(boolean enabled) {
        prefs.edit().putBoolean("music_enabled", enabled).apply();
    }

    public static void release() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}