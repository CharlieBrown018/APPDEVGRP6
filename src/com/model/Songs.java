/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.model;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author PC
 */
@Entity
@Table(name = "songs")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Songs.findAll", query = "SELECT s FROM Songs s"),
    @NamedQuery(name = "Songs.findBySongID", query = "SELECT s FROM Songs s WHERE s.songID = :songID"),
    @NamedQuery(name = "Songs.findByTitle", query = "SELECT s FROM Songs s WHERE s.title = :title"),
    @NamedQuery(name = "Songs.findByArtist", query = "SELECT s FROM Songs s WHERE s.artist = :artist"),
    @NamedQuery(name = "Songs.findByFilepath", query = "SELECT s FROM Songs s WHERE s.filepath = :filepath")})
public class Songs implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "songID")
    private Integer songID;
    @Column(name = "title")
    private String title;
    @Column(name = "artist")
    private String artist;
    @Lob
    @Column(name = "albumart")
    private byte[] albumart;
    @Lob
    @Column(name = "lyrics")
    private String lyrics;
    @Column(name = "filepath")
    private String filepath;

    public Songs() {
    }

    public Songs(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    public Songs(String filepath, String title, String artist) {
        this.filepath = filepath;
        this.title = title;
        this.artist = artist;
    }

    public Songs(String filepath, String title, String artist, byte [] albumart) {
        this.filepath = filepath;
        this.title = title;
        this.artist = artist;
        this.albumart = albumart;
    }
    
    public Songs(String filepath, String title, String artist, byte [] albumart, String lyrics) {
        this.filepath = filepath;
        this.title = title;
        this.artist = artist;
        this.albumart = albumart;
        this.lyrics = lyrics;
    }
    
    public Songs(Integer songID, String title, String artist, byte[] albumart, String filepath) {
        this.songID = songID;
        this.title = title;
        this.artist = artist;
        this.albumart = albumart;
        this.filepath = filepath;
    }

    
    public Songs(Integer songID) {
        this.songID = songID;
    }

    public Integer getSongID() {
        return songID;
    }

    public void setSongID(Integer songID) {
        this.songID = songID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public byte[] getAlbumart() {
        return albumart;
    }

    public String getFilePath() {
        return this.filepath;
    }
    
    public void setAlbumart(byte[] albumart) {
        this.albumart = albumart;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getFilepath() {
        return filepath;
    }

    public void setFilePath(String filepath) {
        this.filepath = filepath;
    }
    
    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (songID != null ? songID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Songs)) {
            return false;
        }
        Songs other = (Songs) object;
        if ((this.songID == null && other.songID != null) || (this.songID != null && !this.songID.equals(other.songID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.model.Songs[ songID=" + songID + " ]";
    }
    
}
