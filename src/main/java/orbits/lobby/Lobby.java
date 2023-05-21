package orbits.lobby;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import orbits.OrbitsGame;
import orbits.data.*;
import orbits.data.level.Level;
import orbits.physics.PhysicsEngine;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.PhysicsBodySweptAABBProducer;
import org.dyn4j.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lobby {
    private final AvailableData availableData = new AvailableData();
    private final List<Player> players = new ArrayList<>();
    private final Int2ObjectMap<Entity> entities = new Int2ObjectLinkedOpenHashMap<>();
    private int entityIdCounter = 0;
    private final PhysicsEngine physicsEngine = new PhysicsEngine();
    private Level level;
    private float playerSize = 0.05F;
    private OrbitsGame orbitsGame;

    public Lobby() {
    }

    public AvailableData availableData() {
        return availableData;
    }

    public void start(OrbitsGame orbitsGame) {
        level = availableData.level;
        World<Body> world = physicsEngine.world();
        for (Wall wall : level.walls()) {
            Body body = new Body();
            Vector2 pos1 = new Vector2();
            pos1.x = level.wallPositions().get(wall.pos1Index()).x();
            pos1.y = level.wallPositions().get(wall.pos1Index()).y();
            pos1 = toWorldSpace(pos1);
            Vector2 pos2 = new Vector2();
            pos2.x = level.wallPositions().get(wall.pos2Index()).x();
            pos2.y = level.wallPositions().get(wall.pos2Index()).y();
            pos2 = toWorldSpace(pos2);

            BodyFixture f = body.addFixture(Geometry.createSegment(pos1, pos2));
            f.setFriction(0);
            body.setMass(MassType.INFINITE);
            world.addBody(body);
        }

        for (Player player : players) {
            newEntity(player);
        }
        for (Entity entity : entities.values()) {
            if (entity instanceof Ball) {
                Ball ball = (Ball) entity;
                Body body = new Body();
                BodyFixture f = body.addFixture(Geometry.createCircle(playerSize));
                f.setFriction(0);
                f.setRestitution(1);
                body.setMass(MassType.NORMAL);
                if (entity instanceof Player) {
                }
            }
        }
    }

    public PhysicsEngine physicsEngine() {
        return physicsEngine;
    }

    public Vector2 toWorldSpace(Vector2 vector) {
        vector.x = toWorldSpaceX(vector.x);
        return vector;
    }

    public Vector2 toLocalSpace(Vector2 vector) {
        vector.x = toLocalSpaceX(vector.x);
        return vector;
    }

    public double toWorldSpaceX(double worldSpace) {
        return worldSpace * level.aspectRatioWpH();
    }

    public double toLocalSpaceX(double worldSpace) {
        return worldSpace / level.aspectRatioWpH();
    }

    private void newEntity(Entity entity) {
        entities.put(entityIdCounter, entity);
        entity.entityId(entityIdCounter++);
    }

    public Int2ObjectMap<Entity> entities() {
        return entities;
    }

    public List<Player> players() {
        return players;
    }

    public Level level() {
        return level;
    }
}
