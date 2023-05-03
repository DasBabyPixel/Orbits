package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataMemory;
import gamelauncher.engine.data.Files;
import gamelauncher.engine.util.GameException;
import orbits.Orbits;
import orbits.data.level.Level;

import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class LevelStorage {

    private final Checksum checksum = new CRC32();
    private final Path folder;

    public LevelStorage(Orbits orbits) throws GameException {
        this.folder = orbits.game().directory().resolve("levels");
        Files.createDirectories(folder);
    }

    public void saveLevel(Level level) throws GameException {
        Path path = folder.resolve(level.uuid().toString());
        DataMemory memory = new DataMemory();
        DataBuffer dataBuffer = new DataBuffer(memory);
        level.write(dataBuffer);
        Files.write(path, Arrays.copyOfRange(memory.array(), 0, dataBuffer.writerIndex()));
    }

    /**
     * @return the level if found, otherwise null
     */
    public Level findLevel(UUID uuid, long checksum) throws GameException {
        Path path = folder.resolve(uuid.toString());
        if (!Files.exists(path)) return null;
        byte[] bytes = Files.readAllBytes(path);
        if (checksum != -1) {
            this.checksum.reset();
            this.checksum.update(bytes, 0, bytes.length);
            if (checksum != this.checksum.getValue()) return null;
        }
        DataBuffer dataBuffer = new DataBuffer(new DataMemory(bytes));
        Level level = new Level();
        level.read(dataBuffer);
        if (!level.uuid().equals(uuid)) return null;
        return level;
    }

}
