package orbits.settings;

import gamelauncher.engine.event.EventManager;
import gamelauncher.engine.settings.AbstractSettingSection;
import gamelauncher.engine.settings.SettingPath;
import gamelauncher.engine.settings.SimpleSetting;

public class OrbitsSettingSection extends AbstractSettingSection {
    public static final SettingPath ORBITS = new SettingPath("orbits");
    public static final SettingPath SERVER_URL = new SettingPath("server_url");

    public OrbitsSettingSection(EventManager eventManager) {
        super(eventManager);
    }

    @Override
    protected void addSettings(EventManager eventManager) {
        addSetting(SERVER_URL, new SimpleSetting<>(String.class, "https://ssh.darkcube.eu/orbits/"));
    }
}
