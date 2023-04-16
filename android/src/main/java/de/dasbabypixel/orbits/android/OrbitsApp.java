package de.dasbabypixel.orbits.android;

import gamelauncher.android.AndroidGameLauncher;
import gamelauncher.android.AndroidLauncher;
import orbits.Orbits;

public class OrbitsApp extends AndroidLauncher {
	@Override
	public void init(AndroidGameLauncher launcher) {
		launcher.pluginManager().loadPlugin(new Orbits());
	}
}
