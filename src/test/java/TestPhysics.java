import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.TimeStep;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.dynamics.contact.SolvedContact;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.Mass;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.*;
import org.dyn4j.world.listener.CollisionListener;
import org.dyn4j.world.listener.ContactListener;
import org.dyn4j.world.listener.ContactListenerAdapter;
import org.dyn4j.world.listener.StepListener;

public class TestPhysics {
    public static void main(String[] args) {
        World<Body> world = new World<>();
        world.setGravity(World.ZERO_GRAVITY);
        System.out.println(1 / world.getSettings().getStepFrequency());
        Body b1 = createBall(2);
        b1.translate(-10, 0.1);
        b1.setBullet(true);
        b1.setMass(MassType.NORMAL);
        b1.applyForce(new Vector2(1000000, 0));
        Body b2 = createBall(5);
        b2.setMass(MassType.NORMAL);
        world.addBody(b1);
        world.addBody(b2);
        world.addContactListener(new ContactListenerAdapter<>() {
            @Override
            public void begin(ContactCollisionData<Body> collision, Contact contact) {
                super.begin(collision, contact);
                System.out.println("contact");
                System.out.println(collision.isNarrowphaseCollision());
            }

            @Override
            public void end(ContactCollisionData<Body> collision, Contact contact) {
                super.end(collision, contact);
                System.out.println(collision.getBody1().getLinearVelocity());
                System.out.println(collision.getBody2().getLinearVelocity());
            }
        });
        System.out.println(b1.getWorldCenter());
        System.out.println(b2.getWorldCenter());
        for (int i = 0; i < 40; i++) {
            world.step(1);
            System.out.println("step");
        }

        System.out.println(b1.getWorldCenter());
        System.out.println(b2.getWorldCenter());
    }

    private static Body createBall(int rad) {
        Body body = new Body();
        BodyFixture f = body.addFixture(Geometry.createCircle(rad));
        f.setRestitution(1);
        return body;
    }
}
