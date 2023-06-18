package orbits.settings;

import gamelauncher.engine.event.EventManager;
import gamelauncher.engine.settings.AbstractSettingSection;
import gamelauncher.engine.settings.SettingPath;
import gamelauncher.engine.settings.SimpleSetting;

public class OrbitsSettingSection extends AbstractSettingSection {
    public static final SettingPath ORBITS = new SettingPath("orbits");
    public static final SettingPath SERVER_HOST = new SettingPath("server_host");
    public static final SettingPath SERVER_PORT = new SettingPath("server_port");

    public OrbitsSettingSection(EventManager eventManager) {
        super(eventManager);
    }

    @Override
    protected void addSettings(EventManager eventManager) {
        addSetting(SERVER_HOST, new SimpleSetting<>(String.class, "37.114.47.76"));
        addSetting(SERVER_PORT, new SimpleSetting<>(Integer.class, 19452));
    }
}
