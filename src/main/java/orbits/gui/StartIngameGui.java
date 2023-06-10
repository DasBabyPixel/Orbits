package orbits.gui;

import gamelauncher.engine.gui.ParentableAbstractGui;
import gamelauncher.engine.gui.guis.ButtonGui;
import gamelauncher.engine.gui.guis.ColorGui;
import gamelauncher.engine.gui.guis.TextGui;
import gamelauncher.engine.util.Color;
import gamelauncher.engine.util.GameException;
import gamelauncher.engine.util.keybind.KeybindEvent;
import gamelauncher.engine.util.keybind.KeyboardKeybindEvent;
import gamelauncher.engine.util.text.Component;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import orbits.OrbitsGame;
import orbits.data.LocalPlayer;
import orbits.data.level.Level;
import orbits.lobby.Lobby;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StartIngameGui extends ParentableAbstractGui {
    protected final OrbitsGame orbits;
    protected final Random r = new Random(0);
    protected final IntList players = new IntArrayList();
    protected final List<PlayerGui> playerGuis = new ArrayList<>();
    protected final Lobby lobby;
    protected final ButtonGui back;
    protected ButtonGui start;

    public StartIngameGui(Level level, OrbitsGame orbits) throws GameException {
        super(orbits.launcher());
        this.orbits = orbits;
        lobby = new Lobby();
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
        addStart();
    }

    protected void addStart() throws GameException {
        start = launcher().guiManager().createGui(ButtonGui.class);
        start.xProperty().bind(xProperty().add(widthProperty()).subtract(10).subtract(start.widthProperty()));
        start.yProperty().bind(back.yProperty());
        start.heightProperty().bind(back.heightProperty());
        start.widthProperty().bind(back.widthProperty());
        ((ButtonGui.Simple.TextForeground) start.foreground().value()).textGui().text().value(Component.text("Start"));
        start.onButtonPressed(event -> {
            start();
        });
        addGUI(start);
    }

    protected void start() throws GameException {
        if (lobby.availableData().level.startPositions().isEmpty()) return;
        if (players.size() >= 2) {
            Lobby l = orbits.currentLobby();
            for (int i = 0; i < players.size(); i++) {
                int id = players.getInt(i);
                PlayerGui pg = playerGuis.get(i);
                LocalPlayer player = new LocalPlayer(id, pg.display.charAt(0));
                player.color().x(pg.color.x);
                player.color().y(pg.color.y);
                player.color().z(pg.color.z);
                l.players().add(player);
            }
            preStart();
            l.start(orbits);
            launcher().guiManager().openGui(startCreateGui());
        }
    }

    protected void preStart() {
    }

    protected IngameGui startCreateGui() throws GameException {
        return new IngameGui(orbits);
    }

    @Override
    protected boolean doHandle(KeybindEvent entry) throws GameException {
        if (entry instanceof KeyboardKeybindEvent.CharacterKeybindEvent) {
            KeyboardKeybindEvent.CharacterKeybindEvent ke = ((KeyboardKeybindEvent.CharacterKeybindEvent) entry);
            handleCharacter(ke);
        }
        return super.doHandle(entry);
    }

    protected void handleCharacter(KeyboardKeybindEvent.CharacterKeybindEvent event) throws GameException {
        if (players.contains(event.keybind().uniqueId())) {
            removePlayer(event.keybind().uniqueId());
        } else if (event.character() != ' ') {
            newPlayer(event.keybind().uniqueId(), event.character());
        }
    }

    protected void removePlayer(int id) {
        int index = players.indexOf(id);
        if (index == -1) return;
        players.removeInt(index);
        PlayerGui pg = playerGuis.remove(index);
        PlayerGui prev = index == 0 ? null : playerGuis.get(index - 1);
        PlayerGui next = index == playerGuis.size() ? null : playerGuis.get(index);
        if (next != null) {
            next.yProperty().unbind();
            bind(next, prev);
        }
        pg.yProperty().unbind();
        removeGUI(pg);
    }

    protected PlayerGui newPlayer(int id, char display) throws GameException {
        return newPlayer(id, display, newColor());
    }

    protected PlayerGui newPlayer(int id, char display, Vector4f color) throws GameException {
        players.add(id);
        PlayerGui pg = new PlayerGui(orbits, Character.toString(display), color);
        pg.xProperty().bind(xProperty().add(10));
        pg.widthProperty().number(50);
        pg.heightProperty().number(50);
        PlayerGui prev = playerGuis.isEmpty() ? null : playerGuis.get(playerGuis.size() - 1);
        bind(pg, prev);
        playerGuis.add(pg);
        addGUI(pg);
        return pg;
    }

    protected void bind(PlayerGui pg, PlayerGui prev) {
        if (prev != null) {
            pg.yProperty().bind(prev.yProperty().subtract(pg.heightProperty()).subtract(10));
        } else {
            pg.yProperty().bind(back.yProperty().subtract(pg.heightProperty()).subtract(10));
        }
    }

    protected Vector4f newColor() {
        return new Vector4f(r.nextFloat(), r.nextFloat(), r.nextFloat(), 1.0F);
    }

    protected static class PlayerGui extends ParentableAbstractGui {
        protected final Vector4f color;
        protected final String display;

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
