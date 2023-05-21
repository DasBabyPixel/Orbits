package orbits.data;

import gamelauncher.engine.render.GameItem;

public class LocalPlayer extends Player {
    public GameItem textColor;
    private int keybindId;
    private char display;

    public int keybindId() {
        return keybindId;
    }

    public char display() {
        return display;
    }

    public LocalPlayer(int keybindId, char display) {
        this.keybindId = keybindId;
        this.display = display;
    }
}
