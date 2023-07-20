package orbits.ingame;

import orbits.data.*;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.listener.ContactListenerAdapter;

public class OrbitsContactListener extends ContactListenerAdapter<Body> {
    private final Game game;

    public OrbitsContactListener(Game game) {
        this.game = game;
    }

    @Override
    public void end(ContactCollisionData<Body> collision, Contact contact) {
        Body b1 = collision.getBody1();
        Body b2 = collision.getBody2();
        end(b1);
        end(b2);
        if (b1.getUserData() instanceof Player && b2.getUserData() instanceof Orbit)
            ((Player) b1.getUserData()).currentOrbit(game.level, null);
        if (b2.getUserData() instanceof Player && b1.getUserData() instanceof Orbit)
            ((Player) b2.getUserData()).currentOrbit(game.level, null);
        if (b1.getUserData() instanceof Wall) {
            if (game.broadcast != null) game.broadcast.update((Entity) b2.getUserData());
        }
        if (b2.getUserData() instanceof Wall) {
            if (game.broadcast != null) game.broadcast.update((Entity) b1.getUserData());
        }
        end(collision.getBody1(), collision.getBody2());
    }

    private void end(Body b1, Body b2) {
        if (b1.getUserData() instanceof Player && b2.getUserData() instanceof Player) {
            if (game.broadcast != null) {
                game.broadcast.update((Entity) b1.getUserData());
                game.broadcast.update((Entity) b2.getUserData());
            }
        }
    }

    @Override
    public void begin(ContactCollisionData<Body> collision, Contact contact) {
        Body b1 = collision.getBody1();
        Body b2 = collision.getBody2();
        begin(collision, b1, b2);
        begin(collision, b2, b1);
    }

    private void begin(ContactCollisionData<Body> collision, Body b1, Body b2) {
        if (b1.getUserData() instanceof Player && b2.getUserData() instanceof Ball) {
            begin(collision, ((Player) b1.getUserData()), ((Ball) b2.getUserData()));
        } else if (b1.getUserData() instanceof Player && b2.getUserData() instanceof Orbit) {
            begin(collision, ((Player) b1.getUserData()), ((Orbit) b2.getUserData()));
        } else if (b1.getUserData() instanceof Ball) {
            Ball b = (Ball) b1.getUserData();
            if (b.projectile) {
                if (b2.getUserData() instanceof Wall) {
                    game.reset(b);
                } else if (b2.getUserData() instanceof Player) {
                    Player target = (Player) b2.getUserData();
                    Player killer = (Player) game.get(b.ownerId());
                    game.kill(target, killer);
                }
            }
        }
    }

    private void begin(ContactCollisionData<Body> collision, Player player, Orbit orbit) {
        collision.getContactConstraint().setEnabled(false);
        player.currentOrbit(game.level, orbit);
    }

    private void begin(ContactCollisionData<Body> collision, Player player, Ball ball) {
        if (player.dodgeMultiplier() > 1 || player.entityId() == 0) {
            collision.getContactConstraint().setEnabled(false);
            return;
        }
        if (ball instanceof Player) {
            Player p = (Player) ball;
            if (p.dodgeMultiplier() > 1 || p.entityId() == 0) {
                collision.getContactConstraint().setEnabled(false);
                return;
            }
            game.stopOrbit(player);
            game.stopOrbit(p);
            return;
        }
        if (ball.ownerId() != 0) {
            if (ball.ownerId() == player.entityId()) return;
            game.kill(player, (Player) game.get(ball.ownerId()));
            return;
        }
        collision.getContactConstraint().setEnabled(false);
        player.addTrail(ball);
        if (game.broadcast != null) {
            game.broadcast.addTrail(player.entityId(), ball.entityId());
        }
    }

    private void end(Body body) {
        if (body.getUserData() instanceof Player) {
            Player player = (Player) body.getUserData();
            body.getLinearVelocity().normalize();
            float speed = player.calculateSpeed(game);
            body.getLinearVelocity().multiply(speed);

            player.updateMotion(game);
        } else if (body.getUserData() instanceof Ball) {
            ((Ball) body.getUserData()).updateMotion(game);
        }
    }
}
