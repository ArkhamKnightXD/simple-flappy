package knight.arkham.objects;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import knight.arkham.helpers.Box2DBody;
import knight.arkham.helpers.Box2DHelper;

public class Pipe extends GameObject {

    public Pipe(Rectangle bounds, boolean isRotated, World world) {
        super(
            bounds,
            world, isRotated ? "pipe-green-180.png" :"pipe-green.png",
            "die.wav"
        );
    }

    @Override
    protected Body createBody() {
        return Box2DHelper.createBody(
            new Box2DBody(actualBounds, 0, actualWorld, this)
        );
    }

    public void update() {
        body.setLinearVelocity(-4 , 0);
    }

    public Vector2 getPosition() {
        return new Vector2(body.getPosition().scl(32));
    }
}
