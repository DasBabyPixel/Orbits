package orbits.gui;

import gamelauncher.engine.GameLauncher;
import gamelauncher.engine.event.events.util.keybind.KeybindEntryEvent;
import gamelauncher.engine.gui.Gui;
import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.gui.guis.ColorGui;
import gamelauncher.engine.gui.guis.TextGui;
import gamelauncher.engine.util.Color;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.KeyboardKeybindEvent;
import gamelauncher.engine.util.keybind.MouseButtonKeybindEvent;
import gamelauncher.engine.util.text.Component;
import jdk.dynalink.linker.LinkerServices;
import orbits.OrbitsGame;
import orbits.data.LocalPlayer;
import orbits.data.Orbit;
import orbits.data.Player;
import orbits.data.level.Level;
import orbits.lobby.Lobby;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StartSingleplayerGui extends ParentableAbstractGui {
    private final OrbitsGame orbits;
    private final Random r = new Random(0);
    private final List<Integer> players = new ArrayList<>();
    private final List<PlayerGui> playerGuis = new ArrayList<>();
    private final ButtonGui back;
    private final ButtonGui start;

    public StartSingleplayerGui(Level level, OrbitsGame orbits) throws GameException {
        super(orbits.launcher());
        this.orbits = orbits;
        Lobby lobby = new Lobby();
        lobby.availableData().level = level;
        orbits.currentLobby(lobby);

        back = launcher().guiManager().createGui(ButtonGui.class);
        back.xProperty().bind(xProperty().add(10));
        back.yProperty().bind(yProperty().add(heightProperty()).subtract(10).subtract(back.heightProperty()));
        back.heightProperty().bind(heightProperty().divide(14));
        back.widthProperty().bind(back.heightProperty().multiply(3));
        ((ButtonGui.Simple.TextForeground) back.foreground().value()).textGui().text().value(Component.text("Back"));
        back.onButtonPressed(event -> launcher().guiManager().openGui(new OrbitsMainScreenGui(orbits)));
        addGUI(back);
        start = launcher().guiManager().createGui(ButtonGui.class);
        start.xProperty().bind(xProperty().add(widthProperty()).subtract(10).subtract(start.widthProperty()));
        start.yProperty().bind(back.yProperty());
        start.heightProperty().bind(back.heightProperty());
        start.widthProperty().bind(back.widthProperty());
        ((ButtonGui.Simple.TextForeground) start.foreground().value()).textGui().text().value(Component.text("Start"));
        start.onButtonPressed(event -> {
            if (players.size() >= 2) {
                Lobby l = orbits.currentLobby();
                for (int i = 0; i < players.size(); i++) {
                    int id = players.get(i);
                    PlayerGui pg = playerGuis.get(i);
                    LocalPlayer player = new LocalPlayer();
                    player.keybindId(id);
                    player.color().x(pg.color.x);
                    player.color().y(pg.color.y);
                    player.color().z(pg.color.z);
                    l.players().add(player);
                }
                l.start(orbits);
                launcher().guiManager().openGui(new IngameGui(orbits));
            }
        });
        addGUI(start);
    }

    @Override
    protected boolean doHandle(KeybindEvent entry) throws GameException {
        if (entry instanceof KeyboardKeybindEvent.CharacterKeybindEvent) {
            KeyboardKeybindEvent.CharacterKeybindEvent ke = ((KeyboardKeybindEvent.CharacterKeybindEvent) entry);
            if (players.contains(ke.keybind().uniqueId())) {
                int index = players.indexOf(ke.keybind().uniqueId());
                players.remove(index);
                PlayerGui pg = playerGuis.remove(index);
                PlayerGui prev = index == 0 ? null : playerGuis.get(index - 1);
                PlayerGui next = index == playerGuis.size() ? null : playerGuis.get(index);
                if (next != null) {
                    next.yProperty().unbind();
                    bind(next, prev);
                }
                pg.yProperty().unbind();
                removeGUI(pg);
            } else {
                players.add(ke.keybind().uniqueId());
                PlayerGui pg = new PlayerGui(orbits, Character.toString(ke.character()), newColor());
                pg.xProperty().bind(xProperty().add(10));
                pg.widthProperty().number(50);
                pg.heightProperty().number(50);
                PlayerGui prev = playerGuis.isEmpty() ? null : playerGuis.get(playerGuis.size() - 1);
                bind(pg, prev);
                playerGuis.add(pg);
                addGUI(pg);

            }
        }
        return super.doHandle(entry);
    }

    private void bind(PlayerGui pg, PlayerGui prev) {
        if (prev != null) {
            pg.yProperty().bind(prev.yProperty().subtract(pg.heightProperty()).subtract(10));
        } else {
            pg.yProperty().bind(back.yProperty().subtract(pg.heightProperty()).subtract(10));
        }
    }

    private Vector4f newColor() {
        return new Vector4f(r.nextFloat(), r.nextFloat(), r.nextFloat(), 1.0F);
    }

    private class PlayerGui extends ParentableAbstractGui {
        private final Vector4f color;
        private final String display;

        public PlayerGui(OrbitsGame orbits, String display, Vector4f color) throws GameException {
            super(orbits.launcher());
            this.color = color;
            this.display = display;
            ColorGui colorGui = launcher().guiManager().createGui(ColorGui.class);
            colorGui.xProperty().bind(xProperty());
            colorGui.yProperty().bind(yProperty());
            colorGui.widthProperty().bind(widthProperty());
            colorGui.heightProperty().bind(heightProperty());
            colorGui.color().set(color.x, color.y, color.z, color.w);
            addGUI(colorGui);
            TextGui textGui = launcher().guiManager().createGui(TextGui.class);
            textGui.xProperty().bind(xProperty());
            textGui.yProperty().bind(yProperty());
            textGui.heightProperty().bind(heightProperty());
            textGui.text().value(Component.text(this.display));
            addGUI(textGui);
        }
    }
}
