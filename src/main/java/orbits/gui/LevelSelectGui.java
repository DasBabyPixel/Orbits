package orbits.gui;

import de.dasbabypixel.api.property.NumberValue;
import de.dasbabypixel.api.property.Property;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.gui.guis.ScrollGui;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.function.GameConsumer;
import gamelauncher.engine.util.text.Component;
import orbits.OrbitsGame;
import orbits.data.level.Level;

import java.util.UUID;
import java.util.function.Predicate;

public class LevelSelectGui extends ParentableAbstractGui {
    private final ButtonGui exit;
    private final ButtonGui newLevel;
    private final Property<GameConsumer<Level>> levelSelector = Property.empty();
    private final UUID[] levels;

    public LevelSelectGui(OrbitsGame orbits, boolean displayStartPositions) throws GameException {
        this(orbits, displayStartPositions, l -> true);
    }

    public LevelSelectGui(OrbitsGame orbits, boolean displayStartPositions, Predicate<Level> filter) throws GameException {
        super(orbits.launcher());
        exit = launcher().guiManager().createGui(ButtonGui.class);
        NumberValue inset = widthProperty().divide(70).min(heightProperty().divide(70));
        exit.xProperty().bind(xProperty().add(inset));
        exit.yProperty().bind(yProperty().add(heightProperty()).subtract(inset).subtract(exit.heightProperty()));
        exit.widthProperty().bind(inset.multiply(10));
        exit.heightProperty().bind(inset.multiply(4));
        ((ButtonGui.Simple.TextForeground) exit.foreground().value()).textGui().text().value(Component.text("Back"));
        addGUI(exit);

        newLevel = launcher().guiManager().createGui(ButtonGui.class);
        newLevel.xProperty().bind(xProperty().add(widthProperty()).subtract(inset.add(newLevel.widthProperty())));
        newLevel.yProperty().bind(exit.yProperty());
        newLevel.widthProperty().bind(exit.widthProperty().multiply(2));
        newLevel.heightProperty().bind(exit.heightProperty());
        ((ButtonGui.Simple.TextForeground) newLevel.foreground().value()).textGui().text().value(Component.text("New Level"));
//        newLevel.registerKeybindHandler(KeybindEvent.class, System.out::println);
        newLevel.onButtonPressed(event -> {
            Level level = new Level();
            level.uuid(UUID.randomUUID());
            MapEditorGui mapEditorGui = new MapEditorGui(orbits, level);
            launcher().guiManager().openGui(mapEditorGui);
        });
        addGUI(newLevel);

        levels = orbits.levelStorage().levels();
        ScrollGui scrollGui = launcher().guiManager().createGui(ScrollGui.class);
        scrollGui.xProperty().bind(xProperty().add(inset));
        scrollGui.yProperty().bind(yProperty().add(inset));
        scrollGui.widthProperty().bind(widthProperty().subtract(inset.multiply(2)));
        scrollGui.heightProperty().bind(heightProperty().subtract(scrollGui.yProperty()).add(yProperty()).subtract(inset.multiply(2)).subtract(newLevel.heightProperty()));
        scrollGui.gui().value(new LevelsGui(orbits, displayStartPositions, filter));
        scrollGui.gui().value().widthProperty().bind(widthProperty().subtract(inset.multiply(2)).subtract(17));
        addGUI(scrollGui);
    }

    public ButtonGui exit() {
        return exit;
    }

    public ButtonGui newLevel() {
        return newLevel;
    }

    public Property<GameConsumer<Level>> levelSelector() {
        return levelSelector;
    }

    private class LevelsGui extends ParentableAbstractGui {

        public LevelsGui(OrbitsGame orbits, boolean displayStartPositions, Predicate<Level> filter) throws GameException {
            super(orbits.launcher());

            NumberValue x = xProperty();
            NumberValue y = yProperty();
            NumberValue height = NumberValue.withValue(0D);
            for (UUID levelId : levels) {
                Level level = orbits.levelStorage().findLevel(levelId, -1);
                if (!filter.test(level)) continue;
                LevelGui levelGui = new LevelGui(orbits, level, displayStartPositions);
                ButtonGui button = launcher().guiManager().createGui(ButtonGui.class);

                button.xProperty().bind(x);
                button.yProperty().bind(y);
                button.widthProperty().bind(widthProperty());
                button.heightProperty().bind(widthProperty().divide(level.aspectRatioWpH()));
                button.onButtonPressed(e -> levelSelector.value().accept(level));
                button.foreground().value(levelGui);
                addGUI(button);
                height = height.add(button.heightProperty());
                if (levelId != levels[levels.length - 1]) {
                    height = height.add(50);
                    y = y.add(button.heightProperty()).add(50);
                }
            }
            heightProperty().bind(height);
        }
    }
}
