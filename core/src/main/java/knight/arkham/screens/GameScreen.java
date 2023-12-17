package knight.arkham.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;
import knight.arkham.Space;
import knight.arkham.helpers.GameContactListener;
import knight.arkham.objects.*;
import java.util.Iterator;
import static knight.arkham.helpers.Constants.*;

public class GameScreen extends ScreenAdapter {
    private final Space game;
    private final OrthographicCamera camera;
    public SpriteBatch batch;
    private final World world;
    private final Box2DDebugRenderer debugRenderer;
    private final Texture background;
    private final Player player;
    private final Array<Pipe> pipes;
    private final Floor floor;
    private final Floor floor2;
    private final TextureAtlas numbersAtlas;
    private TextureRegion scoreNumbers;
    private TextureRegion scoreNumbersUnits;
    private final Rectangle scoreBounds;
    private final Texture startGame;
    private int score;
    private long lastPipeSpawnTime;
    private float accumulator;
    private final float TIME_STEP;
    private float stateTimer;

    public GameScreen() {

        game = Space.INSTANCE;

        game.isGameOver = false;

        camera = game.camera;

        TIME_STEP = 1/240f;

        batch = new SpriteBatch();

        world = new World(new Vector2(0, -20), true);
        world.setContactListener(new GameContactListener());

        debugRenderer = new Box2DDebugRenderer();

        player = new Player(new Vector2(game.screenWidth/2f, game.screenHeight/2f), world);

        pipes = new Array<>();

        floor = new Floor(new Rectangle(game.screenWidth/2f, 40, game.screenWidth, 80), world);
        floor2 = new Floor(new Rectangle(game.screenWidth + 240, 40, game.screenWidth, 80), world);

        background = new Texture("images/background-day.png");
        startGame = new Texture("images/message.png");

        numbersAtlas = new TextureAtlas("images/numbers.atlas");

        scoreNumbers = numbersAtlas.findRegion(String.valueOf(score));
        scoreNumbersUnits = numbersAtlas.findRegion(String.valueOf(score));

        scoreBounds = new Rectangle(
            FULL_SCREEN_WIDTH / 2f, 500 / PIXELS_PER_METER,
            scoreNumbers.getRegionWidth() / PIXELS_PER_METER,
            scoreNumbers.getRegionHeight() / PIXELS_PER_METER
        );
    }

    @Override
    public void resize(int width, int height) {
        game.viewport.update(width, height);
    }

    private void generatePipes() {

        float upPipePosition = MathUtils.random(480, game.screenHeight + 80);

        //up pipe position less pipe height less gap size.
        float downPipePosition = upPipePosition - 320 - 160;

        Pipe upPipe = new Pipe(new Rectangle(game.screenWidth, upPipePosition, 64, 320), true, world);
        Pipe downPipe = new Pipe(new Rectangle(game.screenWidth, downPipePosition, 64, 320), false, world);

        pipes.add(upPipe, downPipe);

        lastPipeSpawnTime = TimeUtils.nanoTime();
    }

    private void update(float deltaTime) {

        player.update(deltaTime);

        if(TimeUtils.nanoTime() - lastPipeSpawnTime > 2000000000)
            generatePipes();

        for (Iterator<Pipe> pipesIterator = pipes.iterator(); pipesIterator.hasNext();) {

            Pipe pipe = pipesIterator.next();

            pipe.update();

            if (pipe.getPosition().x < -32) {
                pipesIterator.remove();
                pipe.dispose();
            }
        }

        floor.update();
        floor2.update();

        stateTimer += deltaTime;

        if (stateTimer > 2) {

            stateTimer = 0;

            score++;

            if (score < 10)
                scoreNumbers = numbersAtlas.findRegion(String.valueOf(score));

            else {
                scoreNumbers = numbersAtlas.findRegion(String.valueOf(Integer.parseInt(("" + score).substring(0, 1))));
                scoreNumbersUnits = numbersAtlas.findRegion(String.valueOf(Integer.parseInt(("" + score).substring(1, 2))));
            }
        }
    }

    @Override
    public void render(float deltaTime) {

        ScreenUtils.clear(0, 0, 0, 0);

        draw();

        if (!game.isGameOver) {
            update(deltaTime);
            doPhysicsTimeStep(deltaTime);
        }
        else if (Gdx.input.isTouched())
            game.setScreen(new GameScreen());
    }

    private void doPhysicsTimeStep(float deltaTime) {

        float frameTime = Math.min(deltaTime, 0.25f);

        accumulator += frameTime;

        while(accumulator >= TIME_STEP) {
            world.step(TIME_STEP, 6,2);
            accumulator -= TIME_STEP;
        }
    }

    private void draw() {

        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        batch.draw(background, 1/PIXELS_PER_METER, 1/PIXELS_PER_METER, FULL_SCREEN_WIDTH, FULL_SCREEN_HEIGHT);

        for (Pipe pipe : pipes)
            pipe.draw(batch);

        floor.draw(batch);
        floor2.draw(batch);

        player.draw(batch);

        batch.draw(scoreNumbers, scoreBounds.x, scoreBounds.y, scoreBounds.width, scoreBounds.height);

        if (score > 9) {
            batch.draw(
                scoreNumbersUnits, scoreBounds.x + 25 / PIXELS_PER_METER,
                scoreBounds.y, scoreBounds.width, scoreBounds.height
            );
        }

        if (game.isGameOver)
            batch.draw(startGame, 1/PIXELS_PER_METER, 1/PIXELS_PER_METER, FULL_SCREEN_WIDTH, FULL_SCREEN_HEIGHT);

        batch.end();

//        debugRenderer.render(world, camera.combined);
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {

        player.dispose();
        background.dispose();
        world.dispose();
        batch.dispose();
        debugRenderer.dispose();

        scoreNumbers.getTexture().dispose();
        scoreNumbersUnits.getTexture().dispose();
    }
}
