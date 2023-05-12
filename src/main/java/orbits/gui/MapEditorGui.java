package orbits.gui;

import de.dasbabypixel.api.property.NumberValue;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.gui.guis.ColorGui;
import gamelauncher.engine.gui.guis.MainScreenGui;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.MouseButtonKeybindEvent;
import gamelauncher.engine.util.text.Component;
import orbits.OrbitsGame;
import orbits.data.Position;
import orbits.data.Wall;
import orbits.data.level.Level;
import orbits.data.level.TransactionTracker;

import java.util.ArrayList;
import java.util.List;

public class MapEditorGui extends ParentableAbstractGui {
    private final OrbitsGame game;
    private final Level level;
    private final LevelGui levelGui;
    private final TransactionTracker transactionTracker;
    private final NewWallHandler newWallHandler = new NewWallHandler();
    private ButtonGui selected = null;
    private SelectedHandler selectedHandler = null;

    public MapEditorGui(OrbitsGame orbits, Level level) throws GameException {
        super(orbits.launcher());
        this.level = level;
        this.transactionTracker = new TransactionTracker();
        this.game = orbits;
        float insetF = 0.02F;
        float spacingF = 0.02F;
        int verticalCount = 7;
        NumberValue guiHeight = heightProperty().multiply(1 - insetF * 2);
        NumberValue guiY = yProperty().add(heightProperty().multiply(insetF));
        NumberValue guiX = xProperty().add(heightProperty().multiply(insetF));
        NumberValue spacing = guiHeight.multiply(spacingF);
        NumberValue rowHeight = guiHeight.subtract(spacing.multiply(verticalCount - 1)).divide(verticalCount);
        NumberValue columnWidth = rowHeight.multiply(2.5);
        NumberValue editorX = calcx(2, columnWidth, spacing, guiX);
        NumberValue editorY = calcy(0, rowHeight, spacing, guiY);
        NumberValue editorWidth = widthProperty().subtract(editorX).subtract(guiX);
        NumberValue editorHeight = heightProperty().subtract(editorY).subtract(guiY);

        ButtonGui saveAndExit = createButton();
        saveAndExit.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        saveAndExit.yProperty().bind(calcy(6, rowHeight, spacing, guiY));
        saveAndExit.widthProperty().bind(columnWidth);
        saveAndExit.heightProperty().bind(rowHeight);
        ((ButtonGui.Simple.TextForeground) saveAndExit.foreground().value()).textGui().text().value(Component.text("Save"));
        saveAndExit.onButtonPressed(event -> orbits.levelStorage().saveLevel(level));
        GUIs.add(saveAndExit);

        ButtonGui exit = createButton();
        exit.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        exit.yProperty().bind(calcy(6, rowHeight, spacing, guiY));
        exit.widthProperty().bind(columnWidth);
        exit.heightProperty().bind(rowHeight);
        ((ButtonGui.Simple.TextForeground) exit.foreground().value()).textGui().text().value(Component.text("Exit"));
        exit.onButtonPressed(event -> launcher().guiManager().openGuiByClass(framebuffer, MainScreenGui.class));
        GUIs.add(exit);

        ButtonGui newOrbit = createButton();
        newOrbit.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        newOrbit.yProperty().bind(calcy(5, rowHeight, spacing, guiY));
        newOrbit.widthProperty().bind(columnWidth);
        newOrbit.heightProperty().bind(rowHeight);
        ((ButtonGui.Simple.TextForeground) newOrbit.foreground().value()).textGui().text().value(Component.text("Orbit"));
        newOrbit.onButtonPressed(event -> select(newOrbit, null));
        GUIs.add(newOrbit);

        ButtonGui newWall = createButton();
        newWall.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        newWall.yProperty().bind(calcy(5, rowHeight, spacing, guiY));
        newWall.widthProperty().bind(columnWidth);
        newWall.heightProperty().bind(rowHeight);
        ((ButtonGui.Simple.TextForeground) newWall.foreground().value()).textGui().text().value(Component.text("Wall"));
        newWall.onButtonPressed(event -> select(newWall, newWallHandler));
        GUIs.add(newWall);

        ButtonGui newSpawnpoint = createButton();
        newSpawnpoint.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        newSpawnpoint.yProperty().bind(calcy(4, rowHeight, spacing, guiY));
        newSpawnpoint.widthProperty().bind(columnWidth);
        newSpawnpoint.heightProperty().bind(rowHeight);
        ((ButtonGui.Simple.TextForeground) newSpawnpoint.foreground().value()).textGui().text().value(Component.text("Spawn"));
        newSpawnpoint.onButtonPressed(event -> select(newSpawnpoint, null));
        GUIs.add(newSpawnpoint);

        ButtonGui grid = createButton();
        grid.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        grid.yProperty().bind(calcy(4, rowHeight, spacing, guiY));
        grid.widthProperty().bind(columnWidth);
        grid.heightProperty().bind(rowHeight);
        ((ButtonGui.Simple.TextForeground) grid.foreground().value()).textGui().text().value(Component.text("Grid"));
        GUIs.add(grid);

        ButtonGui radius = createButton();
        radius.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        radius.yProperty().bind(calcy(2, rowHeight, spacing, guiY));
        radius.widthProperty().bind(columnWidth.multiply(2).add(spacing));
        radius.heightProperty().bind(rowHeight.multiply(2).add(spacing));
        ((ButtonGui.Simple.TextForeground) radius.foreground().value()).textGui().text().value(Component.text("Radius"));
        GUIs.add(radius);

        ButtonGui undo = createButton();
        undo.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        undo.yProperty().bind(calcy(1, rowHeight, spacing, guiY));
        undo.widthProperty().bind(columnWidth);
        undo.heightProperty().bind(rowHeight);
        ((ButtonGui.Simple.TextForeground) undo.foreground().value()).textGui().text().value(Component.text("Undo"));
        GUIs.add(undo);

        ButtonGui delete = createButton();
        delete.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        delete.yProperty().bind(calcy(1, rowHeight, spacing, guiY));
        delete.widthProperty().bind(columnWidth);
        delete.heightProperty().bind(rowHeight);
        ((ButtonGui.Simple.TextForeground) delete.foreground().value()).textGui().text().value(Component.text("Del"));
        GUIs.add(delete);

        ButtonGui redo = createButton();
        redo.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        redo.yProperty().bind(calcy(0, rowHeight, spacing, guiY));
        redo.widthProperty().bind(columnWidth);
        redo.heightProperty().bind(rowHeight);
        ((ButtonGui.Simple.TextForeground) redo.foreground().value()).textGui().text().value(Component.text("Redo"));
        GUIs.add(redo);

        ButtonGui move = createButton();
        move.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        move.yProperty().bind(calcy(0, rowHeight, spacing, guiY));
        move.widthProperty().bind(columnWidth);
        move.heightProperty().bind(rowHeight);
        move.onButtonPressed(event -> select(move, null));
        ((ButtonGui.Simple.TextForeground) move.foreground().value()).textGui().text().value(Component.text("Move"));
        GUIs.add(move);

        levelGui = new LevelGui(orbits, level);
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

        GUIs.add(editorBackground);
        GUIs.add(levelGui);

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
        ((ButtonGui.Simple.TextForeground) b.foreground().value()).textGui().text().value(Component.empty());
        return b;
    }

    private interface SelectedHandler {
        void handle(KeybindEvent event) throws GameException;

        default void handleSelect() throws GameException {
        }

        default void handleDeselect() throws GameException {
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
                if (pos.x() < 0 || pos.y() < 0 || pos.x() > 1 || pos.y() > 1) return;
                if (currentPositions.size() >= 3 && currentPositions.get(0).distanceSquared(pos) < 0.005) {
                    pos = currentPositions.get(0);
                    Position last = currentPositions.get(currentPositions.size() - 1);
                    Wall wall = new Wall(level);
                    wall.pos1Index(level.wallPositions().indexOf(pos));
                    wall.pos2Index(level.wallPositions().indexOf(last));
                    wall.recalcBody();
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
                    Wall wall = new Wall(level);
                    wall.pos1Index(level.wallPositions().size() - 1);
                    wall.pos2Index(level.wallPositions().indexOf(currentPositions.get(currentPositions.size() - 2)));
                    wall.recalcBody();
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
