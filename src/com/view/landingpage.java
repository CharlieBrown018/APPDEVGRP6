package com.view;
import appdevgrp6.EntityManagerFactoryUtil;
import com.controller.*;
import com.model.*;
import com.special.*;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;
import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import com.sun.javafx.application.PlatformImpl;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.swing.JOptionPane;
import javax.swing.JSlider;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableRowSorter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.jaudiotagger.tag.datatype.Artwork;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.util.HashMap;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;


/**
 *
 * @author user
 */
public class landingpage extends javax.swing.JFrame implements ProgressCallback {
    private Songs currentSong;

    private int currentSongIndex = -1;
    private int previousSongIndex = -1;
    private ButtonRenderer ButtonRenderer;
    private ButtonEditor ButtonEditor;
    private PackColumn PackColumn;
    private boolean isRepeating = false;
    private boolean isUserSeeking = false;
    private List<Songs> playOrder = new ArrayList<>();
    private ArrayList<Songs> songs = new ArrayList<>();
    private ArrayList<File> lyricsFiles = new ArrayList<>();
    private DefaultTableModel model = new DefaultTableModel();
    private JTable table = new JTable(model);
    public static boolean isRunning = false;
    private String currentLyrics;
    private File selectedFile = null;
    private MusicPlayer musicPlayer;
    private HashMap<Songs, File> songToLyricsFileMap = new HashMap<>();

    private List<Songs> getAllSongs() {
        return songs;
    }
    
    /**
     * Creates new form landingpage
     */
    
    
    public landingpage() {
        // Initialize the JavaFX Toolkit
        PlatformImpl.startup(() -> {});
       
        initComponents();
        table = new JTable();
        
        JPanel card2 = new JPanel(new BorderLayout());
        card2.add(new JScrollPane(table), BorderLayout.CENTER);
        WESTcardlayout.add(card2, "card2");
        
        MusicPlayer.setEndOfMediaCallback(() -> {
            if (++currentSongIndex >= getAllSongs().size()) {
                currentSongIndex = 0;  // loop back to the first song if it's the end of the list
            }

            startPlayingSong(currentSongIndex);
        });
   
        // Set the default volume to 50%
        MusicPlayer.setVolume(0.5);
        jVolumeSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                // Normalize the slider value to between 0 and 1
                double volume = source.getValue() / 100.0;
                MusicPlayer.setVolume(volume);
            }
        });
        
        JSearchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchQuery = JSearchField.getText();
                filterTable(searchQuery);
            }
        });
        
        JSearchField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (JSearchField.getText().equals("Search...")) {
                    JSearchField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (JSearchField.getText().isEmpty()) {
                    JSearchField.setText("Search...");
                }
            }
        });
        
        jSongProgressBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent me) {
                isUserSeeking = true;
            }
            
            @Override
            public void mouseReleased(MouseEvent me) {
                isUserSeeking = false;
                int mouseX = me.getX();
                double progressBarVal = (double) mouseX / (double) jSongProgressBar.getWidth();
                MusicPlayer.seekTo((int) (progressBarVal * MusicPlayer.getTotalDuration()));
                jSongProgressBar.setValue((int) (progressBarVal * jSongProgressBar.getMaximum()));
                }
            });
        
            MusicPlayer.setProgressCallback(progress -> {
                if(!isUserSeeking) {
                    jSongProgressBar.setValue((int) (progress * jSongProgressBar.getMaximum()));
                }
            });

            ShuffleButton.setEnabled(false); 
            loadData();
            jButtonAllSongsActionPerformed(null);
            playOrder = new ArrayList<>(songs);
            }
    
            public void onProgressUpdate(double progress) {
                SwingUtilities.invokeLater(() -> {
                    jSlider1.setValue((int) (progress * 100));
                    });
                }

            private void loadData() {
                // Create a SwingWorker to load the data in the background
                SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        // Load music from the music package
                        EntityManager em = EntityManagerFactoryUtil.getEntityManager();
                        TypedQuery<Songs> query = em.createQuery("SELECT s FROM Songs s", Songs.class);
                        songs.addAll(query.getResultList());
                        em.close();
                        
                        
                        //MusicLoader.loadMusicFromDirectory("/music/", songs); // Change "src/music/" for local storage purposes
                        File lyricsDirectory = new File("src/lyrics/");  // Replace with actual path
                        File[] filesInDirectory = lyricsDirectory.listFiles((dir, name) -> name.endsWith(".txt")); // Filter .txt files
                        for (File file : filesInDirectory) {
                            // Match the lyrics files with songs
                            String songTitle = file.getName().replace(".txt", "");
                            for (Songs song : songs) {
                                if (song.getTitle().equals(songTitle)) {
                                    songToLyricsFileMap.put(song, file);
                                    break;
                                }
                            }
                        }
                        // Also populate playOrder with the initial song order
                        playOrder = new ArrayList<>(songs);
                        return null;
                    }

                    @Override
                    protected void done() {
                        // Update the UI here with the loaded data
                        jButtonAllSongsActionPerformed(null);
                        // Enable the Shuffle button only after the songs are loaded
                        ShuffleButton.setEnabled(true);
                    }
                };

                // Start the SwingWorker
                worker.execute();
            }

            
            private void setAlbumCover(Songs song) {
                byte[] albumArt = song.getAlbumart();
                if (albumArt != null) {
                    try {
                        BufferedImage img = ImageIO.read(new ByteArrayInputStream(albumArt));
                        ImageIcon icon = new ImageIcon(img);
                        ImageIcon resizedIcon = resizeAlbumArt(icon);
                        jLabelAlbumCover.setIcon(resizedIcon);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    jLabelAlbumCover.setIcon(null);  // Or some default icon
                }
            }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        logobtn = new javax.swing.JLabel();
        JSearchField = new javax.swing.JTextField();
        prof = new javax.swing.JButton();
        jLabelCurrentPlaying = new javax.swing.JLabel();
        bgmusic = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jButtonAllSongs = new javax.swing.JButton();
        jButtonManageSongs = new javax.swing.JButton();
        WESTcardlayout = new javax.swing.JPanel();
        jSongProgressBar = new javax.swing.JProgressBar();
        UpNextLableTitle = new javax.swing.JLabel();
        jLabelCurrentTitle = new javax.swing.JLabel();
        upnextshp1 = new javax.swing.JLabel();
        credits = new javax.swing.JPanel();
        madeby = new java.awt.Label();
        kyle = new java.awt.Label();
        charles = new java.awt.Label();
        lex = new java.awt.Label();
        king = new java.awt.Label();
        PlayButton = new javax.swing.JButton();
        FastforwardButton = new javax.swing.JButton();
        BackwardBUtton = new javax.swing.JButton();
        ShuffleButton = new javax.swing.JButton();
        jVolumeSlider = new javax.swing.JSlider();
        UpNextLable = new javax.swing.JLabel();
        jLabelAlbumCover = new javax.swing.JLabel();
        playing_shape = new javax.swing.JLabel();
        disc = new javax.swing.JLabel();
        upnextshp = new javax.swing.JLabel();
        background = new javax.swing.JLabel();
        jSlider1 = new javax.swing.JSlider();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Turntable");
        setMaximumSize(new java.awt.Dimension(1200, 700));
        setMinimumSize(new java.awt.Dimension(1200, 700));
        setPreferredSize(new java.awt.Dimension(1200, 700));
        setResizable(false);
        setSize(new java.awt.Dimension(1200, 700));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        logobtn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/logo button.png"))); // NOI18N
        getContentPane().add(logobtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, 350, -1));

        JSearchField.setText("Search...");
        getContentPane().add(JSearchField, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 40, 300, 40));

        prof.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/profilebtn.png"))); // NOI18N
        prof.setBorder(null);
        prof.setBorderPainted(false);
        prof.setContentAreaFilled(false);
        prof.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profActionPerformed(evt);
            }
        });
        getContentPane().add(prof, new org.netbeans.lib.awtextra.AbsoluteConstraints(1110, 30, -1, -1));

        jLabelCurrentPlaying.setText("Now Playing:");
        getContentPane().add(jLabelCurrentPlaying, new org.netbeans.lib.awtextra.AbsoluteConstraints(940, 400, 80, 20));

        bgmusic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/bgmusicbtn.png"))); // NOI18N
        bgmusic.setBorder(null);
        bgmusic.setBorderPainted(false);
        bgmusic.setContentAreaFilled(false);
        bgmusic.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bgmusicActionPerformed(evt);
            }
        });
        getContentPane().add(bgmusic, new org.netbeans.lib.awtextra.AbsoluteConstraints(1040, 30, -1, -1));

        jScrollPane1.setOpaque(false);

        jTextPane1.setEditable(false);
        jTextPane1.setBackground(new java.awt.Color(140, 92, 52));
        jTextPane1.setBorder(null);
        jTextPane1.setText("No Lyrics to Display Yet");
        jTextPane1.setOpaque(false);
        jScrollPane1.setViewportView(jTextPane1);

        getContentPane().add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(880, 110, 310, 270));

        jButtonAllSongs.setBackground(new java.awt.Color(140, 92, 52));
        jButtonAllSongs.setForeground(new java.awt.Color(255, 255, 255));
        jButtonAllSongs.setText("All Songs");
        jButtonAllSongs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAllSongsActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonAllSongs, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 90, -1, -1));

        jButtonManageSongs.setBackground(new java.awt.Color(140, 92, 52));
        jButtonManageSongs.setForeground(new java.awt.Color(255, 255, 255));
        jButtonManageSongs.setText("Manage Songs");
        jButtonManageSongs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonManageSongsActionPerformed(evt);
            }
        });
        getContentPane().add(jButtonManageSongs, new org.netbeans.lib.awtextra.AbsoluteConstraints(240, 90, -1, -1));

        WESTcardlayout.setBackground(new java.awt.Color(140, 92, 52));
        WESTcardlayout.setForeground(new java.awt.Color(255, 255, 255));
        WESTcardlayout.setLayout(new java.awt.CardLayout());
        getContentPane().add(WESTcardlayout, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 120, 350, 480));
        getContentPane().add(jSongProgressBar, new org.netbeans.lib.awtextra.AbsoluteConstraints(450, 540, 340, 10));

        UpNextLableTitle.setText("...");
        getContentPane().add(UpNextLableTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(580, 630, 150, -1));

        jLabelCurrentTitle.setText("...");
        getContentPane().add(jLabelCurrentTitle, new org.netbeans.lib.awtextra.AbsoluteConstraints(1030, 400, 190, 20));

        upnextshp1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/upnxt.png"))); // NOI18N
        getContentPane().add(upnextshp1, new org.netbeans.lib.awtextra.AbsoluteConstraints(920, 390, -1, -1));

        credits.setBackground(new java.awt.Color(140, 92, 52));
        credits.setForeground(new java.awt.Color(255, 255, 255));

        madeby.setForeground(new java.awt.Color(255, 255, 255));
        madeby.setText("Made By:");
        credits.add(madeby);

        kyle.setForeground(new java.awt.Color(255, 255, 255));
        kyle.setText("Kyle Fallarme");
        credits.add(kyle);

        charles.setForeground(new java.awt.Color(255, 255, 255));
        charles.setText("Charles Monteloyola");
        credits.add(charles);

        lex.setForeground(new java.awt.Color(255, 255, 255));
        lex.setText("Lex Ogaya");
        credits.add(lex);

        king.setForeground(new java.awt.Color(255, 255, 255));
        king.setText("King Pasaron");
        credits.add(king);

        getContentPane().add(credits, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 610, 350, 60));

        PlayButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/play_button.png"))); // NOI18N
        PlayButton.setBorderPainted(false);
        PlayButton.setContentAreaFilled(false);
        PlayButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                PlayButtonActionPerformed(evt);
            }
        });
        getContentPane().add(PlayButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 560, 40, 40));

        FastforwardButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/fastforward_button.png"))); // NOI18N
        FastforwardButton.setBorderPainted(false);
        FastforwardButton.setContentAreaFilled(false);
        FastforwardButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FastforwardButtonActionPerformed(evt);
            }
        });
        getContentPane().add(FastforwardButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(650, 560, 30, 30));

        BackwardBUtton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/backward_button.png"))); // NOI18N
        BackwardBUtton.setBorderPainted(false);
        BackwardBUtton.setContentAreaFilled(false);
        BackwardBUtton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BackwardBUttonActionPerformed(evt);
            }
        });
        getContentPane().add(BackwardBUtton, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 560, 30, 30));

        ShuffleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/shuffle_button.png"))); // NOI18N
        ShuffleButton.setBorderPainted(false);
        ShuffleButton.setContentAreaFilled(false);
        ShuffleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShuffleButtonActionPerformed(evt);
            }
        });
        getContentPane().add(ShuffleButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(480, 560, 30, 30));

        jVolumeSlider.setValue(100);
        getContentPane().add(jVolumeSlider, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 570, 120, -1));

        UpNextLable.setText("Up Next:");
        getContentPane().add(UpNextLable, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 630, -1, -1));

        jLabelAlbumCover.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/albumartplaceholder.png"))); // NOI18N
        getContentPane().add(jLabelAlbumCover, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 450, 200, 200));

        playing_shape.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/playing_shape.png"))); // NOI18N
        getContentPane().add(playing_shape, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 530, -1, -1));

        disc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/disc.png"))); // NOI18N
        getContentPane().add(disc, new org.netbeans.lib.awtextra.AbsoluteConstraints(370, 90, -1, -1));

        upnextshp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/upnxt.png"))); // NOI18N
        getContentPane().add(upnextshp, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 620, -1, -1));

        background.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/landing page.png"))); // NOI18N
        getContentPane().add(background, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1200, -1));
        getContentPane().add(jSlider1, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 490, -1, -1));

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void profActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_profActionPerformed
        // TODO add your handling code here:
        //dispose();
        profile prof = new profile();
        prof.setVisible(true);
    }//GEN-LAST:event_profActionPerformed

    private void bgmusicActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bgmusicActionPerformed
        // TODO add your handling code here:
        background_music bgmusic = new background_music();
        bgmusic.setVisible(true);
    }//GEN-LAST:event_bgmusicActionPerformed

    private void filterTable(String query) {
        TableRowSorter<DefaultTableModel> trs = new TableRowSorter<>(model);
        table.setRowSorter(trs);
        trs.setRowFilter(RowFilter.regexFilter("(?i)" + query));
    }
    
    private void startPlayingSong(int songIndex) {
        // If not repeating, update the current song index; otherwise keep it the same
        if (!isRepeating) {
            currentSongIndex = songIndex;
        }
        Songs selectedSong = getAllSongs().get(currentSongIndex);
        currentSong = selectedSong;

        // Stop the current song (if any) and play the selected song
        MusicPlayer.play(selectedSong);

        // Update the album cover and lyrics
        String lyrics = selectedSong.getLyrics();
        StyledDocument doc = new DefaultStyledDocument(); // Create a new StyledDocument
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false); // Set the alignment for the entire document
        Style defaultStyle = doc.addStyle("default", null);
        
        // Apply the font family and size to the paragraph style
        Font fnregular;
        try {
            fnregular = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/font/Poppins-Regular.ttf"));
            fnregular = fnregular.deriveFont(Font.PLAIN, 15);
            StyleConstants.setFontFamily(defaultStyle, fnregular.getFamily());
            StyleConstants.setFontSize(defaultStyle, fnregular.getSize());
        } catch (FontFormatException | IOException ex) {
            ex.printStackTrace();
        }

        // Apply the styled document to the jTextPane1
        jTextPane1.setStyledDocument(doc);

        // Set the text with the lyrics
        jTextPane1.setText(lyrics);

        // Update the labels
        jLabelCurrentTitle.setText(selectedSong.getTitle());
        UpNextLableTitle.setText(getNextSongTitle());

        // Move the selection in the table to the current song
        table.getSelectionModel().setSelectionInterval(currentSongIndex, currentSongIndex);
    }

    private String getNextSongTitle() {
        // If repeating, the next song title remains the same as the current song title
        if(isRepeating) {
            return getAllSongs().get(currentSongIndex).getTitle();
        }

        int nextSongIndex = currentSongIndex + 1;

        // Loop back to the start of the song list if the end has been reached
        if (nextSongIndex >= getAllSongs().size()) {
            nextSongIndex = 0;
        }

        return getAllSongs().get(nextSongIndex).getTitle();
    }
      
    private void PlayButtonActionPerformed(java.awt.event.ActionEvent evt) {                                           
        // If no song selected, do nothing
        if (currentSongIndex < 0) { 
            return;  
        }

        ImageIcon playIcon = new javax.swing.ImageIcon(getClass().getResource("/img/play_button.png"));
        ImageIcon pauseIcon = new javax.swing.ImageIcon(getClass().getResource("/img/pause_button.png"));

        if (MusicPlayer.isPlaying()) {
            // If the player is currently playing, pause the song
            MusicPlayer.pause();
            PlayButton.setIcon(playIcon);
        } else if (MusicPlayer.isPaused() && currentSongIndex == previousSongIndex) {
            // If the player is currently paused and the current song is the one paused, resume the song
            MusicPlayer.resume();
            PlayButton.setIcon(pauseIcon);
        } else {
            // If the player isn't currently playing or a new song is selected, play the selected song
            startPlayingSong(currentSongIndex);
            PlayButton.setIcon(pauseIcon);
        }
        
        // If not repeating, update previous song index
        if(!isRepeating) {
            previousSongIndex = currentSongIndex;
        }
        
    }                                            

    private void FastforwardButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FastforwardButtonActionPerformed
        // Skip to the next song
        if (++currentSongIndex >= getAllSongs().size()) {
            currentSongIndex = 0;  // loop back to the first song if it's the end of the list
        }

        startPlayingSong(currentSongIndex);
    }//GEN-LAST:event_FastforwardButtonActionPerformed

    private void BackwardBUttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BackwardBUttonActionPerformed
        // Skip to the previous song
        if (--currentSongIndex < 0) {
            currentSongIndex = getAllSongs().size() - 1;  // loop back to the last song if it's the start of the list
        }

        startPlayingSong(currentSongIndex);
    }//GEN-LAST:event_BackwardBUttonActionPerformed

    private void ShuffleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShuffleButtonActionPerformed
        // Shuffle the play order
        Collections.shuffle(playOrder);
        currentSongIndex = 0; // Reset the current song index
        
        // Get a random song index
        Random rand = new Random();
        currentSongIndex = rand.nextInt(playOrder.size());
        
        // Play the randomly selected song
        startPlayingSong(currentSongIndex);
        
        // Update the labels
        jLabelCurrentTitle.setText(currentSong.getTitle());
        UpNextLableTitle.setText(getNextSongTitle());
        
        // Move the selection in the table to the current song
        table.getSelectionModel().setSelectionInterval(currentSongIndex, currentSongIndex);
    }//GEN-LAST:event_ShuffleButtonActionPerformed

    public ImageIcon resizeAlbumArt(ImageIcon albumArtIcon) {
        if (albumArtIcon != null) {
            Image image = albumArtIcon.getImage(); // Transform it 
            Image newimg = image.getScaledInstance(200, 200, java.awt.Image.SCALE_SMOOTH); // Scale it the smooth way 
            albumArtIcon = new ImageIcon(newimg);  // Transform it back
        }
        return albumArtIcon;
    }
    
    private void jButtonAllSongsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAllSongsActionPerformed
        // Create column names for the JTable
        String[] columnNames = {"Title", "Artist"};

        // Get all songs
        List<Songs> songsList = getAllSongs();

        // Create model for JTable
        model = new DefaultTableModel(columnNames, 0);
        table = new JTable(model);

        // Add songs data to the model
        for (Songs song : songsList) {
            model.addRow(new Object[]{song.getTitle(), song.getArtist()});
        }

        // Update the listener for the table
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                currentSongIndex = table.getSelectedRow();

                // check if currentSongIndex is not -1 before proceeding
                if (currentSongIndex != -1) {
                    // Get the selected song
                    Songs selectedSong = songsList.get(currentSongIndex);

                    // Get the album art
                    byte[] albumArtBytes = selectedSong.getAlbumart();
                    ImageIcon albumArtIcon = null;
                    if (albumArtBytes != null) {
                        // Convert byte array to ImageIcon
                        albumArtIcon = new ImageIcon(albumArtBytes);
                        albumArtIcon = resizeAlbumArt(albumArtIcon); // You'll need to modify the resizeAlbumArt method to accept ImageIcon
                    }

                    // Set album art to your JLabel
                    jLabelAlbumCover.setIcon(albumArtIcon);

                    // Load lyrics
                    try {
                        String lyrics = selectedSong.getLyrics();
                        // Set lyrics to your JTextArea or JLabel
                        jTextPane1.setText(lyrics);
                    } catch (Exception ex) {
                        // Handle exception
                        ex.printStackTrace();
                    }
                }
            }
        });

        // For resizing, you can set auto resize mode
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Pack each column
        for (int i = 0; i < table.getColumnCount(); i++) {
            PackColumn.pack(table, i, 2);
        }

        // Remove previous scroll pane if it exists
        WESTcardlayout.removeAll();

        // Add the table to a scroll pane (in case the number of songs is large)
        JScrollPane scrollPane = new JScrollPane(table);

        // Add the scroll pane to card2
        JPanel card2 = new JPanel(new BorderLayout());
        card2.add(scrollPane, BorderLayout.CENTER);

        // Add card2 to WESTcardlayout
        WESTcardlayout.add(card2, "card2");

        // Show card2
        CardLayout cl = (CardLayout) (WESTcardlayout.getLayout());
        cl.show(WESTcardlayout, "card2");

        // Repaint the WESTcardlayout
        WESTcardlayout.revalidate();
        WESTcardlayout.repaint();
    }//GEN-LAST:event_jButtonAllSongsActionPerformed

    private void deleteSong(Songs song) {
        if (song.equals(currentSong)) {
            // Check if the song to be deleted is currently playing
            if (MusicPlayer.isPlaying()) {
                MusicPlayer.stop();
            }
        }
        
        EntityManagerFactory emf = EntityManagerFactoryUtil.getEntityManagerFactory();
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
                        
        Songs managedSong = em.find(Songs.class, song.getSongID());
                        
        if (managedSong != null) {
        // Delete the actual song file
        File songFile = new File(managedSong.getFilePath());
        if (songFile.exists()) {
            songFile.delete();
        }

        // Delete the lyrics file and remove it from the lyricsFiles list
        File lyricsFile = songToLyricsFileMap.get(managedSong);
        if (lyricsFile != null && lyricsFile.exists()) {
            lyricsFile.delete();
            lyricsFiles.remove(lyricsFile); // Remove the lyrics file from the list
        }
         // Remove the entry from the map
        songToLyricsFileMap.remove(song);
        em.remove(managedSong);
        }
       
        transaction.commit();
        em.close();
        emf.close();
        
        
        // Remove the song from the songs list
        songs.remove(song);
    }


    private void jButtonManageSongsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonManageSongsActionPerformed
        // Clear the existing model first
        model.setRowCount(0);
        model.setColumnCount(0);

        String[] columnNames = {"Title", "Artist", "Actions"};
        model.setColumnIdentifiers(columnNames);

        // Add songs data to the model
        for (Songs song : songs) {
            model.addRow(new Object[]{song.getTitle(), song.getArtist(), "Delete"});
        }

        // For resizing, you can set auto resize mode
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Set a new cell renderer for the "Actions" column.
        table.getColumn("Actions").setCellRenderer(new com.special.ButtonRenderer());

        // Set a new cell editor for the "Actions" column.
        table.getColumn("Actions").setCellEditor(new com.special.ButtonEditor(table, (Object[] row) -> {
            int rowIndex = table.getSelectedRow();
            if (rowIndex != -1) {
                Songs selectedSong = songs.get(rowIndex);
                if (selectedSong.equals(currentSong)) {
                    // Check if the song to be deleted is currently playing
                    if (MusicPlayer.isPlaying()) {
                        MusicPlayer.stop();
                    }
                }

                // Delete the song and remove it from the JTable
                deleteSong(selectedSong);
                model.removeRow(rowIndex); // Remove the song from the JTable
            }
        }));

        JScrollPane scrollPane = new JScrollPane(table);

        // Add a button for adding a new song
        JButton addSongButton = new JButton("Add song");

        addSongButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("MP3 Files", "mp3"));
            int returnVal = fileChooser.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String filename = fileChooser.getSelectedFile().getName();
                String path = fileChooser.getSelectedFile().getPath(); // original path
                String songDest = "src/music/" + filename; // destination path for the song
                try {
                    Files.copy(Paths.get(path), Paths.get(songDest), StandardCopyOption.REPLACE_EXISTING); // copy the song file to the music directory
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                File selectedFile = new File(songDest);
                File lyricsFile = null;
                String lyrics = null;
                int lyricsReturnVal = JOptionPane.showConfirmDialog(null, "Would you like to add lyrics for this song?", "Add Lyrics", JOptionPane.YES_NO_OPTION);
                if (lyricsReturnVal == JOptionPane.YES_OPTION) {
                    JFileChooser lyricsFileChooser = new JFileChooser();
                    lyricsFileChooser.setFileFilter(new FileNameExtensionFilter("Text Files", "txt"));
                    int lyricsFileReturnVal = lyricsFileChooser.showOpenDialog(null);
                    if (lyricsFileReturnVal == JFileChooser.APPROVE_OPTION) {
                        String lyricsFilename = lyricsFileChooser.getSelectedFile().getName();
                        String lyricsPath = lyricsFileChooser.getSelectedFile().getPath(); // original lyrics path
                        String lyricsDest = "src/lyrics/" + lyricsFilename; // destination path for the lyrics
                        try {
                            Files.copy(Paths.get(lyricsPath), Paths.get(lyricsDest), StandardCopyOption.REPLACE_EXISTING); // copy the lyrics file to the lyrics directory
                            lyrics = new String(Files.readAllBytes(Paths.get(lyricsDest)));
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        lyricsFile = new File(lyricsDest);
                    }
                }
                try {
                    // Use jAudioTagger library to extract metadata
                    AudioFile audioFile = AudioFileIO.read(selectedFile);
                    org.jaudiotagger.tag.Tag tag = audioFile.getTag();
                    Artwork artwork = tag.getFirstArtwork();

                    String title = tag.getFirst(FieldKey.TITLE);
                    String artist = tag.getFirst(FieldKey.ARTIST);
                    byte[] albumArt = null;

                    if (artwork != null) {
                        albumArt = artwork.getBinaryData();
                    }

                    // Print extracted metadata to the console for debugging
                    System.out.println("Title: " + title);
                    System.out.println("Artist: " + artist);

                    // Create a new Song
                    Songs newSong = new Songs(title, artist);
                    newSong.setFilePath(selectedFile.getAbsolutePath());
                    if (lyrics != null) {
                        newSong.setLyrics(lyrics);
                        // Check for duplicate lyrics file before adding to the list
                        if (!lyricsFiles.contains(lyricsFile)) {
                            lyricsFiles.add(lyricsFile);
                        }
                    }

                    // Add album art to song if exists
                    if (albumArt != null) {
                        newSong.setAlbumart(albumArt);
                    }

                    // Check for duplicate song before adding
                    boolean isDuplicate = songs.stream()
                        .anyMatch(song -> song.getTitle().equals(newSong.getTitle()) && song.getArtist().equals(newSong.getArtist()));

                    if (!isDuplicate) {
                        // Add the new song to your song list
                        songs.add(newSong);
                        
                        EntityManagerFactory emf = EntityManagerFactoryUtil.getEntityManagerFactory();
                        EntityManager em = emf.createEntityManager();
                        EntityTransaction transaction = em.getTransaction();
                        transaction.begin();
                        
                        em.persist(newSong);
                        
                        transaction.commit();
                        em.close();
                        emf.close();
                        
                        // Add the new song and lyrics file to the map
                        if (lyricsFile != null) {
                            songToLyricsFileMap.put(newSong, lyricsFile);
                        }

                        // Add the new song to the table model
                        model.addRow(new Object[]{newSong.getTitle(), newSong.getArtist(), "Delete"});

                        // Resize the columns to fit the data
                        PackColumn.pack(table, 0, 2);
                        PackColumn.pack(table, 1, 2);
                        PackColumn.pack(table, 2, 20); // Adjust the margin as needed

                        // Update the JTable with the model
                        table.setModel(model);
                    }
                } catch (Exception ex) {
                    // Handle exception
                    ex.printStackTrace();
                }
            }
        });

        // Add the scroll pane and addSongButton to card3
        JPanel card3 = new JPanel(new BorderLayout()); // Use BorderLayout for a vertical stack of components
        card3.add(addSongButton, BorderLayout.NORTH);  // Place addSongButton at the top
        card3.add(scrollPane, BorderLayout.CENTER);    // Place scrollPane in the center

        // Add card3 to WESTcardlayout
        WESTcardlayout.add(card3, "card3");

        // Show card3
        CardLayout cl = (CardLayout)(WESTcardlayout.getLayout());
        cl.show(WESTcardlayout, "card3");
    }//GEN-LAST:event_jButtonManageSongsActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:

    }//GEN-LAST:event_formWindowClosing

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        // TODO add your handling code here:
        try {
            Font fnbold = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/font/Poppins-Bold.ttf"));
            Font fnregular = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/font/Poppins-Regular.ttf"));
            Font fortable = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/font/Poppins-Regular.ttf"));
            fnbold = fnbold.deriveFont(Font.PLAIN, 12);
            fnregular = fnregular.deriveFont(Font.PLAIN, 15);
            fortable = fnregular.deriveFont(Font.BOLD, 12);
            jTextPane1.setFont(fnregular);
            jLabelCurrentPlaying.setFont(fnbold);
            jLabelCurrentTitle.setFont(fnregular);
            JSearchField.setFont(fnregular);
            UpNextLable.setFont(fnbold);
            UpNextLableTitle.setFont(fnregular);
            jButtonAllSongs.setFont(fortable);
            jButtonManageSongs.setFont(fortable);
            madeby.setFont(fnregular);
            kyle.setFont(fortable);
            king.setFont(fortable);
            charles.setFont(fortable);
            lex.setFont(fortable);
        } catch (FontFormatException | IOException e) {
            JOptionPane.showMessageDialog(null, e);
        } 
        
    }//GEN-LAST:event_formWindowOpened
    

    /**
     * @param args the command line arguments
     */
    
    
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(landingpage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(landingpage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(landingpage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(landingpage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new landingpage().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BackwardBUtton;
    private javax.swing.JButton FastforwardButton;
    private javax.swing.JTextField JSearchField;
    private javax.swing.JButton PlayButton;
    private javax.swing.JButton ShuffleButton;
    private javax.swing.JLabel UpNextLable;
    private javax.swing.JLabel UpNextLableTitle;
    private javax.swing.JPanel WESTcardlayout;
    private javax.swing.JLabel background;
    private javax.swing.JButton bgmusic;
    private java.awt.Label charles;
    private javax.swing.JPanel credits;
    private javax.swing.JLabel disc;
    private javax.swing.JButton jButtonAllSongs;
    private javax.swing.JButton jButtonManageSongs;
    private javax.swing.JLabel jLabelAlbumCover;
    private javax.swing.JLabel jLabelCurrentPlaying;
    private javax.swing.JLabel jLabelCurrentTitle;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSlider jSlider1;
    private javax.swing.JProgressBar jSongProgressBar;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JSlider jVolumeSlider;
    private java.awt.Label king;
    private java.awt.Label kyle;
    private java.awt.Label lex;
    private javax.swing.JLabel logobtn;
    private java.awt.Label madeby;
    private javax.swing.JLabel playing_shape;
    private javax.swing.JButton prof;
    private javax.swing.JLabel upnextshp;
    private javax.swing.JLabel upnextshp1;
    // End of variables declaration//GEN-END:variables

}
