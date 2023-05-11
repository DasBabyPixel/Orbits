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

public class LevelSelectGui extends ParentableAbstractGui {
    private final ButtonGui exit;
    private final ButtonGui newLevel;
    private final Property<GameConsumer<Level>> levelSelector = Property.empty();
    private final UUID[] levels;

    public LevelSelectGui(OrbitsGame orbits) throws GameException {
        super(orbits.launcher());
        exit = launcher().guiManager().createGui(ButtonGui.class);
        NumberValue inset = widthProperty().divide(70).min(heightProperty().divide(70));
        exit.xProperty().bind(xProperty().add(inset));
        exit.yProperty().bind(yProperty().add(heightProperty()).subtract(inset).subtract(exit.heightProperty()));
        exit.widthProperty().bind(inset.multiply(10));
        exit.heightProperty().bind(inset.multiply(4));
        ((ButtonGui.Simple.TextForeground) exit.foreground().value()).textGui().text().value(Component.text("Back"));
        GUIs.add(exit);

        newLevel = launcher().guiManager().createGui(ButtonGui.class);
        newLevel.xProperty().bind(xProperty().add(widthProperty()).subtract(inset.add(newLevel.widthProperty())));
        newLevel.yProperty().bind(exit.yProperty());
        newLevel.widthProperty().bind(exit.widthProperty().multiply(2));
        newLevel.heightProperty().bind(exit.heightProperty());
        ((ButtonGui.Simple.TextForeground) newLevel.foreground().value()).textGui().text().value(Component.text("New Level"));
        newLevel.onButtonPressed(event -> {
            Level level = new Level();
            level.uuid(UUID.randomUUID());
            MapEditorGui mapEditorGui = new MapEditorGui(orbits, level);
            launcher().guiManager().openGui(framebuffer, mapEditorGui);
        });
        GUIs.add(newLevel);

        levels = orbits.levelStorage().levels();
        ScrollGui scrollGui = launcher().guiManager().createGui(ScrollGui.class);
        scrollGui.xProperty().bind(xProperty().add(inset));
        scrollGui.yProperty().bind(yProperty().add(inset));
        scrollGui.widthProperty().bind(widthProperty().subtract(inset.multiply(2)));
        scrollGui.heightProperty().bind(heightProperty().subtract(scrollGui.yProperty()).add(yProperty()).subtract(inset.multiply(2)));
        scrollGui.gui().value(new LevelsGui(orbits));
        GUIs.add(scrollGui);
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

        public LevelsGui(OrbitsGame orbits) throws GameException {
            super(orbits.launcher());

            NumberValue x = xProperty();
            NumberValue y = yProperty().subtract(240);
            for (UUID levelId : levels) {
                Level level = orbits.levelStorage().findLevel(levelId, -1);
                LevelGui levelGui = new LevelGui(orbits, level);
                ButtonGui button = launcher().guiManager().createGui(ButtonGui.class);
                y = y.add(240);
                button.xProperty().bind(x);
                button.yProperty().bind(y);
                button.width((float) (200 * 16) / 9);
                button.height(200);
                button.onButtonPressed(e -> levelSelector.value().accept(level));
                button.foreground().value(levelGui);
                GUIs.add(button);
            }
            widthProperty().number(300);
            heightProperty().number(levels.length * 240);
        }
    }
}
