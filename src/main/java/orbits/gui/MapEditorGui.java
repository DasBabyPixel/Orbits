package orbits.gui;

import de.dasbabypixel.api.property.NumberValue;
import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.*;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.MouseButtonKeybindEvent;
import gamelauncher.engine.util.property.PropertyVector4f;
import gamelauncher.engine.util.text.Component;
import orbits.OrbitsGame;
import orbits.data.Orbit;
import orbits.data.Position;
import orbits.data.Wall;
import orbits.data.level.Level;
import orbits.data.level.StartPosition;
import orbits.data.level.TransactionTracker;

import java.util.ArrayList;
import java.util.List;

public class MapEditorGui extends ParentableAbstractGui {
    private final OrbitsGame game;
    private final Level level;
    private final LevelGui levelGui;
    private final Grid grid;
    private final TransactionTracker transactionTracker;
    private final GridGui gridGui;
    private final HighlightCircleGui highlightCircleGui;
    private final NewWallHandler newWallHandler = new NewWallHandler();
    private final NewOrbitHandler newOrbitHandler = new NewOrbitHandler();
    private final NewSpawnpointHandler newSpawnpointHandler = new NewSpawnpointHandler();
    private ButtonGui selected = null;
    private SelectedHandler selectedHandler = null;

    public MapEditorGui(OrbitsGame orbits, Level level) throws GameException {
        super(orbits.launcher());
        this.level = level;
        this.grid = new Grid();
        this.transactionTracker = new TransactionTracker();
        this.game = orbits;
        float insetF = 0.02F;
        float spacingF = 0.02F;
        int verticalCount = 7;

        TextureGui textureGui;

        NumberValue guiHeight = heightProperty().multiply(1 - insetF * 2);
        NumberValue guiY = yProperty().add(heightProperty().multiply(insetF));
        NumberValue guiX = xProperty().add(heightProperty().multiply(insetF));
        NumberValue spacing = guiHeight.multiply(spacingF);
        NumberValue rowHeight = guiHeight.subtract(spacing.multiply(verticalCount - 1)).divide(verticalCount);
        NumberValue columnWidth = rowHeight;
        NumberValue editorX = calcx(2, columnWidth, spacing, guiX);
        NumberValue editorY = calcy(0, rowHeight, spacing, guiY);
        NumberValue editorWidth = widthProperty().subtract(editorX).subtract(guiX);
        NumberValue editorHeight = heightProperty().subtract(editorY).subtract(guiY);

        ButtonGui save = createButton();
        save.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        save.yProperty().bind(calcy(6, rowHeight, spacing, guiY));
        save.widthProperty().bind(columnWidth);
        save.heightProperty().bind(rowHeight);
        textureGui = launcher().guiManager().createGui(TextureGui.class);
        textureGui.texture(orbits.textureStorage().texture("save.png"));
        save.foreground().value(textureGui);
        save.onButtonPressed(event -> orbits.levelStorage().saveLevel(level));
        addGUI(save);

        ButtonGui exit = createButton();
        exit.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        exit.yProperty().bind(calcy(6, rowHeight, spacing, guiY));
        exit.widthProperty().bind(columnWidth);
        exit.heightProperty().bind(rowHeight);
        textureGui = launcher().guiManager().createGui(TextureGui.class);
        textureGui.texture(orbits.textureStorage().texture("exit.png"));
        exit.foreground().value(textureGui);
        exit.onButtonPressed(event -> launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits)));
        addGUI(exit);

        ButtonGui newOrbit = createButton();
        newOrbit.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        newOrbit.yProperty().bind(calcy(5, rowHeight, spacing, guiY));
        newOrbit.widthProperty().bind(columnWidth);
        newOrbit.heightProperty().bind(rowHeight);
        textureGui = launcher().guiManager().createGui(TextureGui.class);
        textureGui.texture(orbits.textureStorage().texture("neworbit.png"));
        newOrbit.foreground().value(textureGui);
        newOrbit.onButtonPressed(event -> select(newOrbit, newOrbitHandler));
        addGUI(newOrbit);

        ButtonGui newWall = createButton();
        newWall.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        newWall.yProperty().bind(calcy(5, rowHeight, spacing, guiY));
        newWall.widthProperty().bind(columnWidth);
        newWall.heightProperty().bind(rowHeight);
        textureGui = launcher().guiManager().createGui(TextureGui.class);
        textureGui.texture(orbits.textureStorage().texture("wall.png"));
        newWall.foreground().value(textureGui);
        newWall.onButtonPressed(event -> select(newWall, newWallHandler));
        addGUI(newWall);

        ButtonGui newSpawnpoint = createButton();
        newSpawnpoint.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        newSpawnpoint.yProperty().bind(calcy(4, rowHeight, spacing, guiY));
        newSpawnpoint.widthProperty().bind(columnWidth);
        newSpawnpoint.heightProperty().bind(rowHeight);
        textureGui = launcher().guiManager().createGui(TextureGui.class);
        textureGui.texture(orbits.textureStorage().texture("spawnpoint.png"));
        newSpawnpoint.foreground().value(textureGui);
        newSpawnpoint.onButtonPressed(event -> select(newSpawnpoint, null));
        newSpawnpoint.onButtonPressed(event -> select(newSpawnpoint, newSpawnpointHandler));
        addGUI(newSpawnpoint);

        ButtonGui grid = createButton();
        grid.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        grid.yProperty().bind(calcy(4, rowHeight, spacing, guiY));
        grid.widthProperty().bind(columnWidth);
        grid.heightProperty().bind(rowHeight);
        ((ButtonGui.Simple.TextForeground) grid.foreground().value()).textGui().text().value(Component.text("#"));
        grid.onButtonPressed(event -> {
            if (this.grid.visible) this.grid.hide();
            else this.grid.show();
        });
        addGUI(grid);

        ButtonGui radius = createButton();
        radius.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        radius.yProperty().bind(calcy(2, rowHeight, spacing, guiY));
        radius.widthProperty().bind(columnWidth.multiply(2).add(spacing));
        radius.heightProperty().bind(rowHeight.multiply(2).add(spacing));
        ((ButtonGui.Simple.TextForeground) radius.foreground().value()).textGui().text().value(Component.text("Radius"));
        addGUI(radius);

        ButtonGui undo = createButton();
        undo.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        undo.yProperty().bind(calcy(1, rowHeight, spacing, guiY));
        undo.widthProperty().bind(columnWidth);
        undo.heightProperty().bind(rowHeight);
        textureGui = launcher().guiManager().createGui(TextureGui.class);
        textureGui.texture(orbits.textureStorage().texture("undo.png"));
        undo.foreground().value(textureGui);
        addGUI(undo);

        ButtonGui delete = createButton();
        delete.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        delete.yProperty().bind(calcy(1, rowHeight, spacing, guiY));
        delete.widthProperty().bind(columnWidth);
        delete.heightProperty().bind(rowHeight);
        textureGui = launcher().guiManager().createGui(TextureGui.class);
        textureGui.texture(orbits.textureStorage().texture("delete.png"));
        delete.foreground().value(textureGui);
        addGUI(delete);

        ButtonGui redo = createButton();
        redo.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        redo.yProperty().bind(calcy(0, rowHeight, spacing, guiY));
        redo.widthProperty().bind(columnWidth);
        redo.heightProperty().bind(rowHeight);
        textureGui = launcher().guiManager().createGui(TextureGui.class);
        textureGui.texture(orbits.textureStorage().texture("redo.png"));
        redo.foreground().value(textureGui);
        addGUI(redo);

        ButtonGui move = createButton();
        move.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        move.yProperty().bind(calcy(0, rowHeight, spacing, guiY));
        move.widthProperty().bind(columnWidth);
        move.heightProperty().bind(rowHeight);
        move.onButtonPressed(event -> select(move, null));
        textureGui = launcher().guiManager().createGui(TextureGui.class);
        textureGui.texture(orbits.textureStorage().texture("move.png"));
        move.foreground().value(textureGui);
        addGUI(move);

        levelGui = new LevelGui(orbits, level, true);
        levelGui.xProperty().bind(editorX);
        levelGui.yProperty().bind(editorY);
        levelGui.widthProperty().bind(editorWidth);
        levelGui.heightProperty().bind(editorHeight);
        levelGui.registerKeybindHandler(KeybindEvent.class, event -> {
            if (selectedHandler != null) selectedHandler.handle(event);
        });

        ColorGui editorBackground = launcher().guiManager().createGui(ColorGui.class);
        editorBackground.xProperty().bind(editorX);
        editorBackground.yProperty().bind(editorY);
        editorBackground.widthProperty().bind(editorWidth);
        editorBackground.heightProperty().bind(editorHeight);
        editorBackground.color().set(0, 0, 0, 0.7F);

        gridGui = new GridGui();
        gridGui.xProperty().bind(levelGui.realX());
        gridGui.yProperty().bind(levelGui.realY());
        gridGui.widthProperty().bind(levelGui.realWidth());
        gridGui.heightProperty().bind(levelGui.realHeight());
        this.grid.show();

        addGUI(editorBackground);
        addGUI(gridGui);
        addGUI(levelGui);

        highlightCircleGui = new HighlightCircleGui(orbits); // TODO
        highlightCircleGui.visible = false;
        addGUI(highlightCircleGui);
    }

    private void select(ButtonGui button, SelectedHandler selectedHandler) throws GameException {
        if (selected != null) {
            ((ButtonGui.Simple.ColorBackground) selected.background().value()).highlight().value(false);
            selected = null;
            if (this.selectedHandler != null) {
                this.selectedHandler.handleDeselect();
                this.selectedHandler = null;
            }
        }
        if (button != null) {
            ((ButtonGui.Simple.ColorBackground) (selected = button).background().value()).highlight().value(true);
            this.selectedHandler = selectedHandler;
            if (this.selectedHandler != null) this.selectedHandler.handleSelect();
        }
    }

    private NumberValue calcy(int row, NumberValue rowHeight, NumberValue spacing, NumberValue y) {
        return y.add(rowHeight.add(spacing).multiply(row));
    }

    private NumberValue calcx(int column, NumberValue columnWidth, NumberValue spacing, NumberValue x) {
        return x.add(columnWidth.add(spacing).multiply(column));
    }

    private ButtonGui.Simple createButton() throws GameException {
        ButtonGui.Simple b = (ButtonGui.Simple) game.launcher().guiManager().createGui(ButtonGui.class);
        ((ButtonGui.Simple.ColorBackground) b.background().value()).defaultColor().set(0.2F, 0.2F, 0.2F, 1);
        ((ButtonGui.Simple.ColorBackground) b.background().value()).highlightColor().set(0.7F, 0.7F, 0.7F, 1);
        ((ButtonGui.Simple.TextForeground) b.foreground().value()).textGui().text().value(Component.empty());
        ((ButtonGui.Simple.TextForeground) b.foreground().value()).defaultColor().set(new PropertyVector4f(0, 0, 0, 1));
        ((ButtonGui.Simple.TextForeground) b.foreground().value()).hoverColor().set(new PropertyVector4f(0, 0, 0, 1));
        return b;
    }

    private interface SelectedHandler {
        void handle(KeybindEvent event) throws GameException;

        default void handleSelect() throws GameException {
        }

        default void handleDeselect() throws GameException {
        }
    }

    private static class HighlightCircleGui extends ParentableAbstractGui {

        private boolean visible;

        public HighlightCircleGui(OrbitsGame orbits) throws GameException {
            super(orbits.launcher());
            TextureGui textureGui = launcher().guiManager().createGui(TextureGui.class);
            textureGui.texture(orbits.textureStorage().texture("highlight.png"));
            textureGui.xProperty().bind(xProperty());
            textureGui.yProperty().bind(yProperty());
            textureGui.widthProperty().bind(widthProperty());
            textureGui.heightProperty().bind(heightProperty());
            addGUI(textureGui);
        }

        @Override
        protected boolean doRender(float mouseX, float mouseY, float partialTick) throws GameException {
            if (!visible) return false;
            return super.doRender(mouseX, mouseY, partialTick);
        }
    }

    private class Grid {
        private final int rowCount = level.aspectRatio().height() * 4;
        private final int columnCount = level.aspectRatio().width() * 4;
        private boolean visible = true;

        public Position snap(Position position) {
            if (!visible) return position;
            return snapToGrid(position);
        }

        public Position snapToGrid(Position position) {
            double fx = position.x() * columnCount;
            double fy = position.y() * rowCount;
            fx = Math.round(fx);
            fy = Math.round(fy);
            position.x(fx / columnCount);
            position.y(fy / rowCount);
            return position;
        }

        public void show() {
            visible = true;
            redraw();
        }

        public void hide() {
            visible = false;
            redraw();
        }
    }

    private class GridGui extends ParentableAbstractGui {

        public GridGui() throws GameException {
            super(game.launcher());
            for (int x = 1; x < grid.columnCount; x++) {
                float fx = (float) x / grid.columnCount;
                LineGui lg = launcher().guiManager().createGui(LineGui.class);
                lg.lineWidth().number(2);
                lg.color().set(0.2F, 0.2F, 0.2F, 0.6F);
                NumberValue vx = xProperty().add(levelGui.realWidth().multiply(fx));
                lg.fromX().bind(vx);
                lg.toX().bind(vx);
                lg.fromY().bind(yProperty());
                lg.toY().bind(yProperty().add(heightProperty()));
                addGUI(lg);
            }
            for (int y = 1; y < grid.rowCount; y++) {
                float fy = (float) y / grid.rowCount;
                LineGui lg = launcher().guiManager().createGui(LineGui.class);
                lg.lineWidth().number(2);
                lg.color().set(0.2F, 0.2F, 0.2F, 0.6F);
                NumberValue vy = yProperty().add(levelGui.realHeight().multiply(fy));
                lg.fromX().bind(xProperty());
                lg.toX().bind(xProperty().add(widthProperty()));
                lg.fromY().bind(vy);
                lg.toY().bind(vy);
                addGUI(lg);
            }
        }

        @Override
        protected boolean doRender(float mouseX, float mouseY, float partialTick) throws GameException {
            if (!grid.visible) return false;
            return super.doRender(mouseX, mouseY, partialTick);
        }
    }

    private class NewSpawnpointHandler implements SelectedHandler {
        @Override
        public void handle(KeybindEvent event) throws GameException {
            if (event instanceof MouseButtonKeybindEvent) {
                MouseButtonKeybindEvent mb = (MouseButtonKeybindEvent) event;
                if (mb.type() != MouseButtonKeybindEvent.Type.PRESS) return;
                Position pos = new Position((mb.mouseX() - levelGui.realX().floatValue()) / levelGui.realWidth().floatValue(), (mb.mouseY() - levelGui.realY().floatValue()) / levelGui.realHeight().floatValue());
                grid.snap(pos);
                if (pos.x() < 0 || pos.y() < 0 || pos.x() > 1 || pos.y() > 1) return;
                StartPosition spawnpoint = new StartPosition();
                spawnpoint.radius(0.1);
                spawnpoint.position().x(pos.x());
                spawnpoint.position().y(pos.y());
                level.startPositions().add(spawnpoint);
                levelGui.update(spawnpoint);
            }
        }
    }

    private class NewOrbitHandler implements SelectedHandler {

        @Override
        public void handle(KeybindEvent event) throws GameException {
            if (event instanceof MouseButtonKeybindEvent) {
                MouseButtonKeybindEvent mb = (MouseButtonKeybindEvent) event;
                if (mb.type() != MouseButtonKeybindEvent.Type.PRESS) return;
                Position pos = new Position((mb.mouseX() - levelGui.realX().floatValue()) / levelGui.realWidth().floatValue(), (mb.mouseY() - levelGui.realY().floatValue()) / levelGui.realHeight().floatValue());
                grid.snap(pos);
                if (pos.x() < 0 || pos.y() < 0 || pos.x() > 1 || pos.y() > 1) return;
                Orbit orbit = new Orbit();
                orbit.radius(0.1);
                orbit.position().x(pos.x());
                orbit.position().y(pos.y());
                level.orbits().add(orbit);
                levelGui.update(orbit);
            }
        }
    }

    private class NewWallHandler implements SelectedHandler {

        private final List<Position> currentPositions = new ArrayList<>();
        private final List<Wall> currentWalls = new ArrayList<>();

        @Override
        public void handle(KeybindEvent event) throws GameException {
            if (event instanceof MouseButtonKeybindEvent) {
                MouseButtonKeybindEvent mb = (MouseButtonKeybindEvent) event;
                if (mb.type() != MouseButtonKeybindEvent.Type.PRESS) return;

                Position pos = new Position((mb.mouseX() - levelGui.realX().floatValue()) / levelGui.realWidth().floatValue(), (mb.mouseY() - levelGui.realY().floatValue()) / levelGui.realHeight().floatValue());
                grid.snap(pos);
                if (pos.x() < 0 || pos.y() < 0 || pos.x() > 1 || pos.y() > 1) return;
                if (currentPositions.size() >= 3 && currentPositions.get(0).distanceSquared(pos) < 0.00005) {
                    pos = currentPositions.get(0);
                    Position last = currentPositions.get(currentPositions.size() - 1);
                    Wall wall = new Wall();
                    wall.pos1Index(level.wallPositions().indexOf(pos));
                    wall.pos2Index(level.wallPositions().indexOf(last));
                    currentWalls.add(wall);
                    level.walls().add(wall);
                    levelGui.update(wall);
                    currentWalls.clear();
                    currentPositions.clear();
                    return;
                } else if (level.wallPositions().contains(pos)) {
                    return;
                }
                currentPositions.add(pos);
                level.wallPositions().add(pos);
                if (currentPositions.size() >= 2) {
                    Wall wall = new Wall();
                    wall.pos1Index(level.wallPositions().size() - 1);
                    wall.pos2Index(level.wallPositions().indexOf(currentPositions.get(currentPositions.size() - 2)));
                    currentWalls.add(wall);
                    level.walls().add(wall);
                    levelGui.update(wall);
                }
            }
        }

        @Override
        public void handleDeselect() throws GameException {
            level.wallPositions().removeAll(currentPositions);
            currentPositions.clear();
            level.walls().removeAll(currentWalls);
            for (Wall wall : currentWalls) {
                levelGui.remove(wall);
            }
            currentWalls.clear();
        }
    }
}
