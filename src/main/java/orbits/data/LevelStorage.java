package orbits.data;

import gamelauncher.engine.data.DataBuffer;
import gamelauncher.engine.data.DataMemory;
import gamelauncher.engine.data.Files;
import gamelauncher.engine.util.GameException;
import orbits.OrbitsGame;
import orbits.data.level.Level;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class LevelStorage {

    private final Checksum checksum = new CRC32();
    private final Path folder;

    public LevelStorage(OrbitsGame orbits) throws GameException {
        this.folder = orbits.directory().resolve("levels");
        Files.createDirectories(folder);
    }

    public UUID[] levels() throws GameException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(folder);
        List<UUID> uuids = new ArrayList<>();
        try {
            for (Path path : stream) {
                uuids.add(UUID.fromString(path.getFileName().toString()));
            }
            stream.close();
        } catch (IOException e) {
            throw new GameException(e);
        }
        return uuids.toArray(new UUID[0]);
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
        level.checksum(this.checksum.getValue());
        if (!level.uuid().equals(uuid)) return null;
        return level;
    }
}
