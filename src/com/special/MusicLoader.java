package com.special;

import appdevgrp6.EntityManagerFactoryUtil;
import com.model.*;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.FieldKey;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

public class MusicLoader {

    public static void loadMusicFromDirectory(String directoryName, List<Songs> songs) {
        try {
            // Path to the music directory
            URL url = MusicLoader.class.getResource(directoryName);
            File musicDirectory = new File(url.toURI());
            File lyricsDirectory = new File("src/lyrics");

            // Clear the existing song list before loading new songs
            songs.clear();

            // Iterate over all files in the music directory
            for (File file : musicDirectory.listFiles()) {
                // Filter .mp3 files
                if (file.getName().endsWith(".mp3")) {
                    // Use jAudioTagger library to extract metadata
                    AudioFile audioFile = AudioFileIO.read(file);
                    org.jaudiotagger.tag.Tag tag = audioFile.getTag();
                    String title = tag.getFirst(FieldKey.TITLE);
                    String artist = tag.getFirst(FieldKey.ARTIST);
                    Artwork artwork = tag.getFirstArtwork();
                    byte[] albumart;

                    String lyricsFileName = title + ".txt";
                    File lyricsFile = new File(lyricsDirectory, lyricsFileName);
                    String lyrics = readLyricsFromFile(lyricsFile);

                    if (artwork != null) {
                        // Access the image data
                        albumart = artwork.getBinaryData();
                        // You can now save or process the image data as needed
                        // For example, save it to a file or display it in your application
                    } else {
                        albumart = null;
                        System.out.println("No album artwork found for this file.");
                    }

                    // Print extracted metadata to the console for debugging
                    System.out.println("Title: " + title);
                    System.out.println("Artist: " + artist);

                    // Create a new Song
                    Songs newSong = new Songs(file.getAbsolutePath(), title, artist, albumart, lyrics);
                    // Add the new song to your song list
                    songs.add(newSong);

                    albumart = newSong.getAlbumart();
                }
            }

            // Check for duplicates and persist only the non-duplicate songs to the database
            EntityManager em = EntityManagerFactoryUtil.getEntityManager();
            em.getTransaction().begin();
            for (Songs newSong : songs) {
                if (!songAlreadyExists(em, newSong.getTitle(), newSong.getArtist())) {
                    em.persist(newSong);
                }
            }
            em.getTransaction().commit();
            em.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean songAlreadyExists(EntityManager em, String title, String artist) {
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(s) FROM Songs s WHERE s.title = :title AND s.artist = :artist", Long.class);
            query.setParameter("title", title);
            query.setParameter("artist", artist);
            Long count = query.getSingleResult();
            return count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String readLyricsFromFile(File lyricsFile) throws IOException {
        // Read the lyrics from the text file
        StringBuilder lyricsBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(lyricsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lyricsBuilder.append(line).append("\n");
            }
        }
        return lyricsBuilder.toString();
    }
}
