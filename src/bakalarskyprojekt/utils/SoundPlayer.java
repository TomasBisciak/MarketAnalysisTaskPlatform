/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bakalarskyprojekt.utils;

import java.net.URL;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

/**
 *
 * @author tomas
 */
public class SoundPlayer {

    private static volatile int playing = 0;

    public static final int SOUND_TASK_FINISHED = 0;

    private static final URL[] soundFiles = {
        SoundPlayer.class.getResource("bakalarskyprojekt/resources/sounds/placeholoder.mp3")

    };

    public static boolean isPlaying() {
        return playing == 0;
    }

    private SoundPlayer() {
    }
    

    public static synchronized void playSound(int sound) {//possibility to safely play multiple sounds at once, overlapping
        playing++;
        Thread t = new Thread(new Runnable() {
            public void run() {
                Media media = new Media(soundFiles[sound].toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
                playing--;
            }
        });
        t.setDaemon(true);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();

    }

    public static synchronized void playSoundIfNotPlaying(int sound) {
        if (playing == 0) {
            playing++;
            Thread t = new Thread(() -> {
                Media media = new Media(soundFiles[sound].toString());
                MediaPlayer mediaPlayer = new MediaPlayer(media);
                mediaPlayer.play();
                playing--;
            });
            t.setDaemon(true);
            t.setPriority(Thread.MIN_PRIORITY);
            t.start();
        }
    }

}
