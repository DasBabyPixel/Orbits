package de.dasbabypixel.orbits.android;

import android.content.pm.ActivityInfo;
import gamelauncher.android.AndroidGameLauncher;
import gamelauncher.android.AndroidLauncher;
import orbits.Orbits;

public class OrbitsApp extends AndroidLauncher {
    @Override
    public void init(AndroidGameLauncher launcher) {
        launcher.activity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE);
        launcher.pluginManager().loadPlugin(new Orbits());
    }
}
