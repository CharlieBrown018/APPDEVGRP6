/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.view;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class WAVPlayer {
    private static Clip player;
     private static FloatControl volumeControl;
    public static void play(String filePath) {
        stop(); // Stop the previous audio, if any

        try {
            File audioFile = new File(filePath);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);

            // Open the audio stream as a Clip
            player = AudioSystem.getClip();
            player.open(audioStream);

            // Get the FloatControl for volume adjustment
            if (player.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                volumeControl = (FloatControl) player.getControl(FloatControl.Type.MASTER_GAIN);
            }
            
            // Start playback
            player.setFramePosition(0);
            player.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    public void setVolume(float volume) {
        if (volumeControl != null) {
            float minVolume = volumeControl.getMinimum();
            float maxVolume = volumeControl.getMaximum();
            float range = maxVolume - minVolume;
            float gain = (range * volume) + minVolume;
            volumeControl.setValue(gain);
        }
    }
    
    public static void stop() {
        if (player != null) {
            if (player.isRunning()) {
                player.stop();
            }
            player.close();
        }
    }
}

