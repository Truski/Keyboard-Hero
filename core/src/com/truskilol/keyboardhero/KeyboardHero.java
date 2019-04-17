package com.truskilol.keyboardhero;

import com.badlogic.gdx.*;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import java.awt.*;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

public class KeyboardHero extends ApplicationAdapter implements InputProcessor {
	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;
	private BitmapFont font;
	private Song song;
	private Music music;

	private LinkedList<Note> notes;
	private LinkedList<FloatingText> floating_texts;
	private HashMap<Character, Point> char2location;

	private FileWriter fileWriter;

	private float time = 0;
	private float escape_timer;
	private int score = 0;
	private int streak = 0;
	private float music_volume = 1;
	private boolean locked = false;
	private boolean escape_pressed;


	// ApplicationAdapter Methods
	
	@Override
	public void create () {
		music = Gdx.audio.newMusic(new FileHandle("songs/demo.mp3"));
		try {
			fileWriter = new FileWriter(new File("songs/kda.khb"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		Gdx.input.setInputProcessor(this);

		font = new BitmapFont();
		font.setColor(Color.WHITE);
		font.getData().setScale(3);

		song = new Song("demo");
		char2location = new HashMap<Character, Point>();
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		shapeRenderer.setAutoShapeType(true);
		notes = new LinkedList<Note>();
		floating_texts = new LinkedList<FloatingText>();

		String[] rows = {"qwertyuiop", "asdfghjkl", "zxcvbnm"};
		for (int i = 0; i < rows.length; i++) {
			for(int j = 0; j < rows[i].length(); j++) {
				Point p = new Point(125 + j*125 + 75*i, 500 - i * 125);
				char2location.put(rows[i].charAt(j), p);
			}
		}

	}

	@Override
	public void render () {
		update();
		if(!music.isPlaying()){
			if(time > 1.8) {
				music.play();
			}
		}

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		shapeRenderer.setColor(Color.WHITE);

		shapeRenderer.begin();

		for (Note n : notes) {
			Point p = char2location.get(n.letter);
			shapeRenderer.circle(p.x, p.y, 50);
			float delta = n.time - time + -.2f;
			if (delta < 0) {
				delta = 0;
			}
			shapeRenderer.circle(p.x, p.y, 50 + 25 * delta);
		}

		shapeRenderer.end();


		batch.begin();

		font.draw(batch, "Score: " + score, 50, 60);
		font.draw(batch, "Streak: " + streak, 800, 60);

		for (Note n : notes) {
			Point p = char2location.get(n.letter);
			font.draw(batch, (""+ n.letter).toUpperCase(), p.x-13, p.y+15);
		}

		for (FloatingText f : floating_texts) {
			f.draw(batch, font);
		}

		batch.end();
	}

	@Override
	public void dispose () {
		batch.dispose();
		shapeRenderer.dispose();
		font.dispose();
	}


	// Custom methods

	private void update() {

		music.setVolume(music_volume);

		if (escape_pressed) {
			escape_timer -= Gdx.graphics.getDeltaTime();
			if (escape_timer <= 0) {
				escape_pressed = false;
			}
		}

		time +=Gdx.graphics.getDeltaTime();
		Note next = song.get_next_note(time);
		if (!locked) {
			if (next != null) {
				notes.add(next);
			}

			while (!notes.isEmpty() && notes.get(0).time < time) {
				notes.remove(0);
				break_streak();
			}

			while(!floating_texts.isEmpty() && floating_texts.get(0).end_time < time) {
				floating_texts.remove(0);
			}
		}
	}

	private void save_note(char key) {
		try {
			fileWriter.append(time + "," + key + "\n");
			fileWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private int precision2Points(Precision p) {
		switch(p) {
			case PERFECT:
				return 100;
			case OKAY:
				return 50;
			case BAD:
				return 10;
			default:
				return 0;
		}
	}

	private float precision2Duration(Precision p) {
		return .40f;
	}

	private Precision accuracy(float currentTime, float noteTime) {
		int difference = (int)((noteTime - currentTime) * 1000);
		if (difference < 400) {
			return Precision.PERFECT;
		} else if (difference < 650 ) {
			return Precision.OKAY;
		} else if (difference < 1100) {
			return Precision.BAD;
		} else {
			return Precision.MISS;
		}
	}

	private void break_streak() {
		streak = 0;
		music_volume = .25f;
	}

	private boolean hit_note(char letter) {
		locked = true;
		for (Note n : notes) {
			if (n.letter == letter) {
				float noteTime = n.time;
				Precision precision = accuracy(time, noteTime);
				if(precision != Precision.MISS) {
					streak++;
				} else {
					break_streak();
				}
				add_floating_text(precision.toString(), precision2Duration(precision), n.letter);
				score += streak * precision2Points(precision);
				if (music_volume < 1.0) {
					music_volume += .15f;
				}
				notes.remove(n);
				locked = false;
				return true;
			}
		}
		locked = false;
		return false;
	}

	private void add_floating_text(String text, float duration, char keyboard_location) {
		FloatingText floatingText = new FloatingText(text, time, duration, char2location.get(keyboard_location));
		floating_texts.add(floatingText);
	}


	// InputProcessor methods

	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Input.Keys.ESCAPE) {
			if(escape_pressed){
				Gdx.app.exit();
				return false;
			}
			escape_pressed = true;
			escape_timer = 1;
			add_floating_text("Press Escape again to quit", 1, 'r');
		}
		char letter = (char) (keycode + 68);
		if(!Character.isLowerCase(letter)) return false;
		System.out.println(letter);
		if(!hit_note(letter)) {
			break_streak();
			add_floating_text("?", precision2Duration(Precision.MISS), letter);
		}
		save_note(letter);
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		return false;
	}
}
