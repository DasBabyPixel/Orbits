package orbits.gui;

import de.dasbabypixel.api.property.*;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ColorGui;
import gamelauncher.engine.gui.guis.LineGui;
import gamelauncher.engine.util.GameException;
import orbits.OrbitsGame;
import orbits.data.Wall;
import orbits.data.level.Level;

import java.util.HashMap;
import java.util.Map;

public class LevelGui extends ParentableAbstractGui {
    private final Level level;
    private final OrbitsGame orbits;
    private final Map<Wall, WallGui> walls = new HashMap<>();
    private final NumberValue realX;
    private final NumberValue realY;
    private final NumberValue realWidth;
    private final NumberValue realHeight;

    public LevelGui(OrbitsGame orbits, Level level) throws GameException {
        super(orbits.launcher());
        this.orbits = orbits;
        this.level = level;
        float aspectRatio = level.aspectRatioWpH();
        realWidth = widthProperty().min(heightProperty().multiply(aspectRatio));
        realHeight = heightProperty().min(widthProperty().divide(aspectRatio));
        realX = xProperty().add(widthProperty().subtract(realWidth).divide(2));
        realY = yProperty().add(heightProperty().subtract(realHeight).divide(2));
        NumberChangeListener recalcListener = (value, oldNumber, newNumber) -> {
            try {
                updateAll();
            } catch (GameException e) {
                throw new RuntimeException(e);
            }
        };
        realX.addListener((InvalidationListener) Property::value);
        realY.addListener((InvalidationListener) Property::value);
        realWidth.addListener((InvalidationListener) Property::value);
        realHeight.addListener((InvalidationListener) Property::value);
        realX.addListener(recalcListener);
        realY.addListener(recalcListener);
        realWidth.addListener(recalcListener);
        realHeight.addListener(recalcListener);

        ColorGui c = launcher().guiManager().createGui(ColorGui.class);
        c.xProperty().bind(realX);
        c.yProperty().bind(realY);
        c.widthProperty().bind(realWidth);
        c.heightProperty().bind(realHeight);
        c.color().set(1, 0, 0, 0.3F);
        addGUI(c);

        for (Wall wall : level.walls()) {
            update(wall);
        }
    }

    public NumberValue realX() {
        return realX;
    }

    public NumberValue realY() {
        return realY;
    }

    public NumberValue realWidth() {
        return realWidth;
    }

    public NumberValue realHeight() {
        return realHeight;
    }

    public void remove(Wall wall) throws GameException {
        WallGui wg = walls.remove(wall);
        if (wg != null) {
            removeGUI(wg);
        }
    }

    public void updateAll() throws GameException {
        for (Wall wall : walls.keySet()) {
            update(wall);
        }
    }

    public void update(Wall wall) throws GameException {
        if (!walls.containsKey(wall)) {
            WallGui wg = new WallGui(orbits, wall);
            wg.xProperty().bind(realX);
            wg.yProperty().bind(realY);
            wg.widthProperty().bind(realWidth);
            wg.heightProperty().bind(realHeight);
            wg.recalc();
            walls.put(wall, wg);
            addGUI(wg);
        } else walls.get(wall).recalc();
    }

    public Level level() {
        return level;
    }

    private class WallGui extends ParentableAbstractGui {
        private final Wall wall;
        private final LineGui lineGui;

        public WallGui(OrbitsGame orbits, Wall wall) throws GameException {
            super(orbits.launcher());
            this.wall = wall;
            lineGui = launcher().guiManager().createGui(LineGui.class);
            lineGui.lineWidth().number(2);
            addGUI(lineGui);
        }

        public void recalc() {
            lineGui.fromX().number(level.wallPositions().get(wall.pos1Index()).x() * realWidth.floatValue() + realX.floatValue());
            lineGui.fromY().number(level.wallPositions().get(wall.pos1Index()).y() * realHeight.floatValue() + realY.floatValue());
            lineGui.toX().number(level.wallPositions().get(wall.pos2Index()).x() * realWidth.floatValue() + realX.floatValue());
            lineGui.toY().number(level.wallPositions().get(wall.pos2Index()).y() * realHeight.floatValue() + realY.floatValue());
        }
    }
}
