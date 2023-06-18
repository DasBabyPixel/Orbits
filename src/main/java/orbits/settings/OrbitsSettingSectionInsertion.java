package orbits.settings;

import gamelauncher.engine.event.EventManager;
import gamelauncher.engine.settings.AbstractSettingSection;
import gamelauncher.engine.settings.ClassBasedSettingSectionInsertion;
import gamelauncher.engine.settings.MainSettingSection;

public class OrbitsSettingSectionInsertion extends ClassBasedSettingSectionInsertion {
    private final EventManager eventManager;

    public OrbitsSettingSectionInsertion(EventManager eventManager) {
        super(MainSettingSection.class);
        this.eventManager = eventManager;
    }

    @Override
    protected void construct(AbstractSettingSection.SettingSectionConstructor constructor) {
        constructor.addSetting(OrbitsSettingSection.ORBITS, new OrbitsSettingSection(eventManager));
    }
}
