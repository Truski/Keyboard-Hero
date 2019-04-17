package com.truskilol.keyboardhero;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class Song {
    private ArrayList<Note> notes;
    private int current_note_index = 0;

    public Song (String song_name) {
        notes = new ArrayList<Note>();
        try {
            File file = new File("songs/" + song_name + ".khb");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                float time = Float.parseFloat(tokens[0]);
                char letter = tokens[1].charAt(0);
                Note n = new Note(time, letter);
                notes.add(n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Note get_next_note(float time) {
        if (current_note_index == notes.size()) {
            return null;
        }
        Note n = notes.get(current_note_index);
        if (time + 1.75 > n.time) {
            current_note_index++;
            return notes.get(current_note_index -1);
        }
        return null;
    }
}
