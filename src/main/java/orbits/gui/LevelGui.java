package orbits.gui;

import de.dasbabypixel.api.property.InvalidationListener;
import de.dasbabypixel.api.property.NumberChangeListener;
import de.dasbabypixel.api.property.NumberValue;
import de.dasbabypixel.api.property.Property;
import gamelauncher.engine.gui.Gui;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ColorGui;
import gamelauncher.engine.gui.guis.LineGui;
import gamelauncher.engine.gui.guis.TextureGui;
import gamelauncher.engine.util.GameException;
import orbits.OrbitsGame;
import orbits.data.Orbit;
import orbits.data.Wall;
import orbits.data.level.Level;
import orbits.data.level.StartPosition;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LevelGui extends ParentableAbstractGui {
    private final Level level;
    private final OrbitsGame orbitsGame;
    private final Map<Wall, WallGui> walls = new HashMap<>();
    private final Map<Orbit, OrbitGui> orbits = new HashMap<>();
    private final Map<StartPosition, StartPositionGui> startPositions = new HashMap<>();
    private final Set<Gui> guis = new HashSet<>();
    private final NumberValue realX;
    private final NumberValue realY;
    private final NumberValue realWidth;
    private final NumberValue realHeight;
    private final boolean displayStartPositions;

    public LevelGui(OrbitsGame orbits, Level level, boolean displayStartPositions) throws GameException {
        super(orbits.launcher());
        this.displayStartPositions = displayStartPositions;
        this.orbitsGame = orbits;
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
        c.color().set(0.8F, 0.8F, 0.8F, 0.3F);
        addGUI(c);

        for (Wall wall : level.walls()) {
            update(wall);
        }
        for (Orbit orbit : level.orbits()) {
            update(orbit);
        }
        for (StartPosition startPosition : level.startPositions()) {
            update(startPosition);
        }
    }

    public Map<Wall, WallGui> walls() {
        return walls;
    }

    public Map<Orbit, OrbitGui> orbits() {
        return orbits;
    }

    public Set<Gui> guis() {
        return guis;
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
            guis.remove(wg);
        }
    }

    public void remove(Orbit orbit) {
        OrbitGui og = orbits.remove(orbit);
        if (og != null) {
            removeGUI(og);
            guis.remove(og);
        }
    }

    public void remove(StartPosition startPosition) {
        StartPositionGui spg = startPositions.remove(startPosition);
        if (spg != null) {
            removeGUI(spg);
            guis.remove(spg);
        }
    }

    public void updateAll() throws GameException {
        for (Wall wall : walls.keySet()) {
            update(wall);
        }
        for (Orbit orbit : orbits.keySet()) {
            update(orbit);
        }
        for (StartPosition startPosition : startPositions.keySet()) {
            update(startPosition);
        }
    }

    public void update(StartPosition startPosition) throws GameException {
        if (displayStartPositions) {
            if (!startPositions.containsKey(startPosition)) {
                StartPositionGui spg = new StartPositionGui(orbitsGame, startPosition);
                spg.xProperty().bind(realX);
                spg.yProperty().bind(realY);
                spg.widthProperty().bind(realWidth);
                spg.heightProperty().bind(realHeight);
                spg.recalc();
                startPositions.put(startPosition, spg);
                addGUI(spg);
                guis.add(spg);
            } else startPositions.get(startPosition).recalc();
        }
    }

    public void update(Orbit orbit) throws GameException {
        if (!orbits.containsKey(orbit)) {
            OrbitGui og = new OrbitGui(orbitsGame, orbit);
            og.xProperty().bind(realX);
            og.yProperty().bind(realY);
            og.widthProperty().bind(realWidth);
            og.heightProperty().bind(realHeight);
            og.recalc();
            orbits.put(orbit, og);
            addGUI(og);
            guis.add(og);
        } else orbits.get(orbit).recalc();
    }

    public void update(Wall wall) throws GameException {
        if (!walls.containsKey(wall)) {
            WallGui wg = new WallGui(orbitsGame, wall);
            wg.xProperty().bind(realX);
            wg.yProperty().bind(realY);
            wg.widthProperty().bind(realWidth);
            wg.heightProperty().bind(realHeight);
            wg.recalc();
            walls.put(wall, wg);
            addGUI(wg);
            guis.add(wg);
        } else walls.get(wall).recalc();
    }

    public Level level() {
        return level;
    }

    private class StartPositionGui extends ParentableAbstractGui {
        private final StartPosition startPosition;
        private final TextureGui textureGui;

        public StartPositionGui(OrbitsGame orbits, StartPosition startPosition) throws GameException {
            super(orbits.launcher());
            this.startPosition = startPosition;
            textureGui = launcher().guiManager().createGui(TextureGui.class);
            textureGui.texture(orbits.textureStorage().texture("spawnpoint.png"));
            addGUI(textureGui);
        }

        public void recalc() {
            textureGui.widthProperty().number(startPosition.radius() * realHeight.floatValue() * 2);
            textureGui.heightProperty().number(startPosition.radius() * realHeight.floatValue() * 2);
            textureGui.xProperty().number(startPosition.position().x() * realWidth.floatValue() + realX.floatValue() - textureGui.width() / 2);
            textureGui.yProperty().number(startPosition.position().y() * realHeight.floatValue() + realY.floatValue() - textureGui.height() / 2);
        }
    }

    private class WallGui extends ParentableAbstractGui {
        private final Wall wall;
        private final LineGui lineGui;

        public WallGui(OrbitsGame orbits, Wall wall) throws GameException {
            super(orbits.launcher());
            this.wall = wall;
            lineGui = launcher().guiManager().createGui(LineGui.class);
            lineGui.lineWidth().number(2);
            lineGui.color().set(0, 0F, 0F, 1F);
            addGUI(lineGui);
        }

        public void recalc() {
            lineGui.fromX().number(level.wallPositions().get(wall.pos1Index()).x() * realWidth.floatValue() + realX.floatValue());
            lineGui.fromY().number(level.wallPositions().get(wall.pos1Index()).y() * realHeight.floatValue() + realY.floatValue());
            lineGui.toX().number(level.wallPositions().get(wall.pos2Index()).x() * realWidth.floatValue() + realX.floatValue());
            lineGui.toY().number(level.wallPositions().get(wall.pos2Index()).y() * realHeight.floatValue() + realY.floatValue());
        }
    }

    private class OrbitGui extends ParentableAbstractGui {
        private final Orbit orbit;
        private final TextureGui textureGui;

        public OrbitGui(OrbitsGame orbits, Orbit orbit) throws GameException {
            super(orbits.launcher());
            this.orbit = orbit;
            this.textureGui = launcher().guiManager().createGui(TextureGui.class);
            textureGui.texture(orbits.textureStorage().texture("orbit.png"));
            addGUI(textureGui);
        }

        public void recalc() {
            textureGui.widthProperty().number(orbit.radius() * realHeight.floatValue() * 2);
            textureGui.heightProperty().number(orbit.radius() * realHeight.floatValue() * 2);
            textureGui.xProperty().number(orbit.position().x() * realWidth.floatValue() + realX.floatValue() - textureGui.width() / 2);
            textureGui.yProperty().number(orbit.position().y() * realHeight.floatValue() + realY.floatValue() - textureGui.height() / 2);
        }
    }
}
