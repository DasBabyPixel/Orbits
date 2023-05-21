package orbits.lobby;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import orbits.OrbitsGame;
import orbits.data.*;
import orbits.data.level.Level;
import orbits.data.level.StartPosition;
import orbits.physics.PhysicsEngine;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.Settings;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.World;
import org.dyn4j.world.listener.ContactListenerAdapter;
import org.joml.Math;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lobby {
    private final AvailableData availableData = new AvailableData();
    private final List<Player> players = new ArrayList<>();
    private final Int2ObjectMap<Entity> entities = new Int2ObjectLinkedOpenHashMap<>();
    private final PhysicsEngine physicsEngine = new PhysicsEngine(this);
    private float scale = 1;
    private float speed = 0.15F;
    private int entityIdCounter = 0;
    private Level level;
    private float playerSize = 0.03F;
    private OrbitsGame orbitsGame;

    public Lobby() {
    }

    public AvailableData availableData() {
        return availableData;
    }

    public void start(OrbitsGame orbitsGame) {
        level = availableData.level;
        World<Body> world = physicsEngine.world();
        world.getSettings().setBaumgarte(Settings.DEFAULT_BAUMGARTE / 20);
        world.getSettings().setLinearTolerance(Settings.DEFAULT_LINEAR_TOLERANCE / 100);
        world.addContactListener(new ContactListenerAdapter<>() {
            @Override
            public void end(ContactCollisionData<Body> collision, Contact contact) {
                collision.getBody1().getLinearVelocity().normalize();
                collision.getBody1().getLinearVelocity().multiply(speed);
                collision.getBody2().getLinearVelocity().normalize();
                collision.getBody2().getLinearVelocity().multiply(speed);

                if (collision.getBody1().getUserData() instanceof Ball) {
                    Ball ball = (Ball) collision.getBody1().getUserData();
                    ball.motion().x(toLocalSpaceX(collision.getBody1().getLinearVelocity().x));
                    ball.motion().y(collision.getBody1().getLinearVelocity().y);
                }
                if (collision.getBody2().getUserData() instanceof Ball) {
                    Ball ball = (Ball) collision.getBody2().getUserData();
                    ball.motion().x(toLocalSpaceX(collision.getBody2().getLinearVelocity().x));
                    ball.motion().y(collision.getBody2().getLinearVelocity().y);
                }
            }
        });
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

            BodyFixture f = body.addFixture(Geometry.createSegment(pos1, pos2), 1, 0, 1);
            f.setRestitutionVelocity(0);
            body.setMass(MassType.INFINITE);
            world.addBody(body);
        }

        int startPosIdx = 0;
        Map<StartPosition, List<Player>> positions = new HashMap<>();
        for (Player player : players) {
            StartPosition pos = level.startPositions().get((startPosIdx++) % level.startPositions().size());
            positions.computeIfAbsent(pos, a -> new ArrayList<>()).add(player);
            newEntity(player);
        }
        for (Map.Entry<StartPosition, List<Player>> entry : positions.entrySet()) {
            double r = Math.PI * Math.random();
            int idx = 0;
            int maxIdx = entry.getValue().size();
            for (; idx < maxIdx; idx++) {
                Player player = entry.getValue().get(idx);
                double worldX = Math.cos(2 * Math.PI * idx / maxIdx + r);
                double worldY = Math.sin(2 * Math.PI * idx / maxIdx + r);
                double x = toLocalSpaceX(worldX);
                double y = worldY;
                player.position().x(entry.getKey().position().x() + x * entry.getKey().radius());
                player.position().y(entry.getKey().position().y() + y * entry.getKey().radius());
                player.motion().x(toLocalSpaceX(worldY * speed));
                player.motion().y(-worldX * speed);
            }
        }

        for (Entity entity : entities.values()) {
            if (entity instanceof Ball) {
                Ball ball = (Ball) entity;
                Body body = new Body();
                BodyFixture f = body.addFixture(Geometry.createCircle(playerSize / 2 * scale), 1, 0, 1);
                f.setRestitutionVelocity(0);
                body.translate(toWorldSpaceX(ball.position().x()), ball.position().y());
                body.setMass(new Mass(new Vector2(), 1, 0));
                body.setBullet(true);
                body.setLinearVelocity(toWorldSpaceX(ball.motion().x()), ball.motion().y());

                if (entity instanceof Player) {
                    body.setAtRestDetectionEnabled(false);
                }
                body.setUserData(ball);
                ball.body = body;
                world.addBody(body);
            }
        }
    }

    public float speed() {
        return speed;
    }

    public float scale() {
        return scale;
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

    public float playerSize() {
        return playerSize;
    }

    public Level level() {
        return level;
    }
}
