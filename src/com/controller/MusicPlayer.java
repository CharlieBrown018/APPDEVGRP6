package com.controller;

import com.model.*;
import com.special.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import javafx.util.Duration;

public class MusicPlayer {
    private static MediaPlayer mediaPlayer = null;
    public static String currentSongFilePath = null;
    private static ProgressCallback progressCallback = null;
    private static double volume = 0.5;  // Default volume level
    private static boolean isPendingSeek = false;
    private static boolean isPlaying = false;
    private static boolean isPaused = false;
    private static double pendingSeekTo = 0;
    private static EndOfMediaCallback endOfMediaCallback = null;
    
    public static void setProgressCallback(ProgressCallback callback) {
        progressCallback = callback;
    }
    
    public static void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            isPlaying = false;
            isPaused = false;
        }
    }
    
    public static void play(Songs song) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        currentSongFilePath = song.getFilePath();

        Media media = new Media(new File(song.getFilePath()).toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        // Set the volume level for the new song
        mediaPlayer.setVolume(volume);

        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            double progress = newValue.toSeconds() / mediaPlayer.getTotalDuration().toSeconds();

            // Call the callback to report the progress
            if (progressCallback != null) {
                progressCallback.onProgressUpdate(progress);
            }
        });

        mediaPlayer.setOnReady(() -> {
            if(isPendingSeek) {
                mediaPlayer.seek(Duration.seconds(pendingSeekTo));
                isPendingSeek = false;
            }
            mediaPlayer.play();
            isPlaying = true;
            isPaused = false;
        });
        
        mediaPlayer.setOnEndOfMedia(() -> {
            if(endOfMediaCallback != null) {
                endOfMediaCallback.onEndOfMedia();
            }
        });
    }
    

    
    public static void setVolume(double volume) {
        // Store the volume level so it can be used for the next song
        MusicPlayer.volume = volume;
        
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(volume);
        }
    }
    
    public static int getTotalDuration() {
        if (mediaPlayer != null) {
            return (int)mediaPlayer.getTotalDuration().toSeconds();
        }
        return 0;
    }
    
    public static void seek(int positionInSeconds) {
        if (mediaPlayer != null) {
            Duration newPosition = Duration.seconds(positionInSeconds);
            mediaPlayer.seek(newPosition);
        }
    }
    
    public static double seekTo(double progress) {
        if (mediaPlayer != null && mediaPlayer.getStatus() != MediaPlayer.Status.UNKNOWN 
            && mediaPlayer.getStatus() != MediaPlayer.Status.DISPOSED) {
            mediaPlayer.seek(Duration.seconds(progress));
            return mediaPlayer.getTotalDuration().toSeconds();
        } else {
            isPendingSeek = true;
            pendingSeekTo = progress;
            return 0.0;
        }
    }


    public static boolean isPaused() {
        return isPaused;
    }

    public static void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isPlaying = false;
            isPaused = true;
        }
    }

    public static boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }

    public static boolean isClipOpen() {
        return mediaPlayer != null;
    }
    
    public static void resume() {
    if (mediaPlayer != null && isPaused) {
            mediaPlayer.play();
            isPlaying = true;
            isPaused = false;
        }
    }

    public static void setLoop(boolean loop) {
        if (mediaPlayer != null) {
            if (loop) {
                mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            } else {
                mediaPlayer.setCycleCount(1);
            }
        }
    }
    
    public interface EndOfMediaCallback {
        void onEndOfMedia();
    }

    public static void setEndOfMediaCallback(EndOfMediaCallback callback) {
        endOfMediaCallback = callback;
    }
}
