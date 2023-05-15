package orbits.gui;

import de.dasbabypixel.api.property.*;
import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ColorGui;
import gamelauncher.engine.gui.guis.LineGui;
import gamelauncher.engine.gui.guis.TextureGui;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.Key;
import it.unimi.dsi.fastutil.objects.Object2IntRBTreeMap;
import orbits.OrbitsGame;
import orbits.data.Orbit;
import orbits.data.Wall;
import orbits.data.level.Level;

import java.util.HashMap;
import java.util.Map;

public class LevelGui extends ParentableAbstractGui {
    private final Level level;
    private final OrbitsGame orbitsGame;
    private final Map<Wall, WallGui> walls = new HashMap<>();
    private final Map<Orbit, OrbitGui> orbits = new HashMap<>();
    private final NumberValue realX;
    private final NumberValue realY;
    private final NumberValue realWidth;
    private final NumberValue realHeight;

    public LevelGui(OrbitsGame orbits, Level level) throws GameException {
        super(orbits.launcher());
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
        c.color().set(1, 0, 0, 0.3F);
        addGUI(c);

        for (Wall wall : level.walls()) {
            update(wall);
        }
        for (Orbit orbit : level.orbits()) {
            update(orbit);
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

    public void remove(Orbit orbit) {
        OrbitGui og = orbits.remove(orbit);
        if (og != null) removeGUI(og);
    }

    public void updateAll() throws GameException {
        for (Wall wall : walls.keySet()) {
            update(wall);
        }
        for (Orbit orbit : orbits.keySet()) {
            update(orbit);
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
            orbits.put(orbit,og);
            addGUI(og);
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

    private class OrbitGui extends ParentableAbstractGui {
        private final Orbit orbit;
        private final TextureGui textureGui;

        public OrbitGui(OrbitsGame orbits, Orbit orbit) throws GameException {
            super(orbits.launcher());
            this.orbit = orbit;
            this.textureGui = launcher().guiManager().createGui(TextureGui.class);
            System.out.println("create");
            textureGui.texture().uploadAsync(launcher().resourceLoader().resource(orbits.key().withKey("textures/ball.png").toPath(launcher().assets())).newResourceStream()).thenRun(()->{
                System.out.println("redraw");
                redraw();
            });
            addGUI(textureGui);
        }

        public void recalc() {
            System.out.println("recalc");
            textureGui.widthProperty().number(orbit.radius());
            textureGui.heightProperty().number(orbit.radius());
            textureGui.xProperty().number(orbit.position().x() * realWidth.floatValue() + realX.floatValue());
            textureGui.yProperty().number(orbit.position().y() * realHeight.floatValue() + realY.floatValue());
        }
    }
}
