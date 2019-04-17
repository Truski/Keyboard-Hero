package com.truskilol.keyboardhero.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.truskilol.keyboardhero.KeyboardHero;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 1366;
		config.height = 768;
		config.fullscreen = true;
		new LwjglApplication(new KeyboardHero(), config);
	}
}
