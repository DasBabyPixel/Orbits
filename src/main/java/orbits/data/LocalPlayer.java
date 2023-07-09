package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.render.GameItem;

public class LocalPlayer extends Player {
    public GameItem textColor;
    private int keybindId;
    private char display;

    public LocalPlayer(int keybindId, char display) {
        this.keybindId = keybindId;
        this.display = display;
    }

    public LocalPlayer() {
    }

    public int keybindId() {
        return keybindId;
    }

    public char display() {
        return display;
    }

    public void display(char display) {
        this.display = display;
    }

    @Override
    public void write(DataBuffer buffer) {
        super.write(buffer);
        buffer.writeInt(keybindId);
    }

    @Override
    public void read(DataBuffer buffer) {
        super.read(buffer);
        keybindId = buffer.readInt();
    }
}
