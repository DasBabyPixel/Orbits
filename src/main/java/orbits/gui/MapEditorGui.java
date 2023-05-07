package orbits.gui;

import de.dasbabypixel.api.property.NumberValue;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.gui.guis.ColorGui;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.text.Component;
import orbits.OrbitsGame;

public class MapEditorGui extends ParentableAbstractGui {
    private final OrbitsGame game;

    public MapEditorGui(OrbitsGame game) throws GameException {
        super(game.launcher());
        this.game = game;
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
        saveAndExit.text().value(Component.text("Save"));
        GUIs.add(saveAndExit);

        ButtonGui exit = createButton();
        exit.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        exit.yProperty().bind(calcy(6, rowHeight, spacing, guiY));
        exit.widthProperty().bind(columnWidth);
        exit.heightProperty().bind(rowHeight);
        exit.text().value(Component.text("Exit"));
        GUIs.add(exit);

        ButtonGui newOrbit = createButton();
        newOrbit.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        newOrbit.yProperty().bind(calcy(5, rowHeight, spacing, guiY));
        newOrbit.widthProperty().bind(columnWidth);
        newOrbit.heightProperty().bind(rowHeight);
        newOrbit.text().value(Component.text("Orbit"));
        GUIs.add(newOrbit);

        ButtonGui newWall = createButton();
        newWall.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        newWall.yProperty().bind(calcy(5, rowHeight, spacing, guiY));
        newWall.widthProperty().bind(columnWidth);
        newWall.heightProperty().bind(rowHeight);
        newWall.text().value(Component.text("Wall"));
        GUIs.add(newWall);

        ButtonGui newSpawnpoint = createButton();
        newSpawnpoint.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        newSpawnpoint.yProperty().bind(calcy(4, rowHeight, spacing, guiY));
        newSpawnpoint.widthProperty().bind(columnWidth);
        newSpawnpoint.heightProperty().bind(rowHeight);
        newSpawnpoint.text().value(Component.text("Spawn"));
        GUIs.add(newSpawnpoint);

        ButtonGui grid = createButton();
        grid.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        grid.yProperty().bind(calcy(4, rowHeight, spacing, guiY));
        grid.widthProperty().bind(columnWidth);
        grid.heightProperty().bind(rowHeight);
        grid.text().value(Component.text("Grid"));
        GUIs.add(grid);

        ButtonGui radius = createButton();
        radius.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        radius.yProperty().bind(calcy(2, rowHeight, spacing, guiY));
        radius.widthProperty().bind(columnWidth.multiply(2).add(spacing));
        radius.heightProperty().bind(rowHeight.multiply(2).add(spacing));
        radius.text().value(Component.text("Radius"));
        GUIs.add(radius);

        ButtonGui undo = createButton();
        undo.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        undo.yProperty().bind(calcy(1, rowHeight, spacing, guiY));
        undo.widthProperty().bind(columnWidth);
        undo.heightProperty().bind(rowHeight);
        undo.text().value(Component.text("Undo"));
        GUIs.add(undo);

        ButtonGui delete = createButton();
        delete.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        delete.yProperty().bind(calcy(1, rowHeight, spacing, guiY));
        delete.widthProperty().bind(columnWidth);
        delete.heightProperty().bind(rowHeight);
        delete.text().value(Component.text("Del"));
        GUIs.add(delete);

        ButtonGui redo = createButton();
        redo.xProperty().bind(calcx(0, columnWidth, spacing, guiX));
        redo.yProperty().bind(calcy(0, rowHeight, spacing, guiY));
        redo.widthProperty().bind(columnWidth);
        redo.heightProperty().bind(rowHeight);
        redo.text().value(Component.text("Redo"));
        GUIs.add(redo);

        ButtonGui move = createButton();
        move.xProperty().bind(calcx(1, columnWidth, spacing, guiX));
        move.yProperty().bind(calcy(0, rowHeight, spacing, guiY));
        move.widthProperty().bind(columnWidth);
        move.heightProperty().bind(rowHeight);
        move.text().value(Component.text("Move"));
        GUIs.add(move);

        ColorGui editor = launcher().guiManager().createGui(ColorGui.class);
        editor.xProperty().bind(editorX);
        editor.yProperty().bind(editorY);
        editor.widthProperty().bind(editorWidth);
        editor.heightProperty().bind(editorHeight);
        editor.color().set(0.7F, 0.7F, 0.7F, 0.8F);
        GUIs.add(editor);


    }

    private NumberValue calcy(int row, NumberValue rowHeight, NumberValue spacing, NumberValue y) {
        return y.add(rowHeight.add(spacing).multiply(row));
    }

    private NumberValue calcx(int column, NumberValue columnWidth, NumberValue spacing, NumberValue x) {
        return x.add(columnWidth.add(spacing).multiply(column));
    }

    private ButtonGui.Simple createButton() throws GameException {
        ButtonGui.Simple b = (ButtonGui.Simple) game.launcher().guiManager().createGui(ButtonGui.class);
        b.text().value(Component.empty());
        return b;
    }
}
