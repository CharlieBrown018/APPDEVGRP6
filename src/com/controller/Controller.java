package com.controller;

import com.view.landingpage;

public class Controller  {

    private landingpage view;
    private MusicPlayer musicPlayer;

    public Controller(landingpage view) {
        this.view = view;
        this.musicPlayer = new MusicPlayer();
    }
}
