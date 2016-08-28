package org.dw.ld36;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.StringBuilder;

import java.util.EnumMap;
import java.util.Map;

import static org.dw.ld36.GameState.*;

public class Main extends ApplicationAdapter {
    public static final int WHEEL_SPINNING_SPEED = -5;
    public static final int MAX_ROUNDS = 5;
    SpriteBatch batch;
    SpriteBatch uiBatch;
    private AssetManager assetManager = new AssetManager();
    private Sprite body;
    private Sprite head;
    private Sprite sword;
    private Sprite horsePart;
    private Sprite line;
    private Texture ground;
    private TextureRegion sun;
    private float gameTime;
    private Chariot player;
    private Chariot enemy;
    private Skin skin;
    private Stage stage;
    private Table table;
    private TextButton suspensionDowngradeButton;
    private TextButton suspensionUpgradeButton;
    private TextButton addHorseButton;
    private TextButton removeHorseButton;
    private TextButton downgradeSpokesButton;
    private TextButton upgradeSpokesButton;
    private TextButton downgradeWheelButton;
    private TextButton upgradeWheelButton;
    private Map<Spoke, Sprite> spokes = new EnumMap<Spoke, Sprite>(Spoke.class);
    private Map<Wagon, Sprite> wagons = new EnumMap<Wagon, Sprite>(Wagon.class);
    private Map<Wheel, Sprite> wheels = new EnumMap<Wheel, Sprite>(Wheel.class);
    Vector2 tmp = new Vector2();
    Vector2 tmp2 = new Vector2();
    private BitmapFont font;
    private TextButton toBattleButton;
    private float stateTimer = Float.NEGATIVE_INFINITY;
    private int playerRounds;
    private int enemyRounds;
    private String enemyName;
    private GameState state = GameState.SETUP;
    private Sound hit;
    private Sound dead;
    private Sound won;

    @Override
    public void create() {
        batch = new SpriteBatch();
        uiBatch = new SpriteBatch();
        TextureLoader.TextureParameter textureParameter = new TextureLoader.TextureParameter();
        textureParameter.genMipMaps = true;
        textureParameter.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        textureParameter.magFilter = Texture.TextureFilter.MipMapLinearLinear;
        TextureLoader.TextureParameter wrappedTextureParameter = new TextureLoader.TextureParameter();
        wrappedTextureParameter.genMipMaps = true;
        wrappedTextureParameter.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        wrappedTextureParameter.magFilter = Texture.TextureFilter.MipMapLinearLinear;
        wrappedTextureParameter.wrapU = Texture.TextureWrap.Repeat;
        wrappedTextureParameter.wrapV = Texture.TextureWrap.Repeat;

        assetManager.load("body.png", Texture.class, textureParameter);
        assetManager.load("head.png", Texture.class, textureParameter);
        assetManager.load("sword.png", Texture.class, textureParameter);
        assetManager.load("horsepart.png", Texture.class, textureParameter);
        assetManager.load("ground.png", Texture.class, wrappedTextureParameter);
        assetManager.load("sun.png", Texture.class);
        assetManager.load("music.ogg", Music.class);
        assetManager.load("uiskin.json", Skin.class);
        assetManager.load("hit.wav", Sound.class);
        assetManager.load("dead.wav", Sound.class);
        assetManager.load("won.wav", Sound.class);
        loadTexturesFor(Spoke.values(), "spoke");
        loadTexturesFor(Wagon.values(), "wagon");
        loadTexturesFor(Wheel.values(), "wheel");

        assetManager.finishLoading();

        hit = assetManager.get("hit.wav");
        dead = assetManager.get("dead.wav");
        won = assetManager.get("won.wav");

        registerSprites(spokes, "spoke", Spoke.values(), 4, -10, 8, 64);
        registerSprites(wagons, "wagon", Wagon.values(), 0, 0, 300, 300);
        registerSprites(wheels, "wheel", Wheel.values(), 75, 75, 150, 150);

        sun = new TextureRegion(assetManager.<Texture>get("sun.png"));

        horsePart = new Sprite(assetManager.<Texture>get("horsepart.png"));
        line = new Sprite(assetManager.<Texture>get("horsepart.png"));
        line.setOrigin(0, 4);
        line.setColor(Color.DARK_GRAY);

        body = new Sprite(assetManager.<Texture>get("body.png"));
        body.setSize(60, 160);

        head = new Sprite(assetManager.<Texture>get("head.png"));
        head.setSize(60, 60);
        head.setOrigin(30, 30);

        sword = new Sprite(assetManager.<Texture>get("sword.png"));
        sword.setSize(20, 160);
        sword.setOrigin(10, 10);

        ground = assetManager.get("ground.png");
        Music music = assetManager.get("music.ogg");
        music.setLooping(true);
        music.play();

        player = new Chariot();
        enemy = new Chariot();
        setupEnemy();

        skin = assetManager.get("uiskin.json");

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Table root = new Table();
        table.top();
        table.add(root);

        root.row().top().left().pad(10);
        suspensionUpgradeButton = new TextButton("Upgrade", skin);
        suspensionDowngradeButton = new TextButton("Downgrade", skin);
        root.add(suspensionDowngradeButton);
        root.add(suspensionUpgradeButton);
        root.row().top().left().pad(10);
        removeHorseButton = new TextButton("Remove horse", skin);
        addHorseButton = new TextButton("Add horse", skin);
        root.add(removeHorseButton);
        root.add(addHorseButton);
        root.row().top().left().pad(10);
        downgradeSpokesButton = new TextButton("Downgrade", skin);
        upgradeSpokesButton = new TextButton("Upgrade", skin);
        root.add(downgradeSpokesButton);
        root.add(upgradeSpokesButton);
        root.row().top().left().pad(10);
        downgradeWheelButton = new TextButton("Downgrade", skin);
        upgradeWheelButton = new TextButton("Upgrade", skin);
        root.add(downgradeWheelButton);
        root.add(upgradeWheelButton);
        root.row();
        toBattleButton = new TextButton("Race!", skin);
        root.add(toBattleButton);

        toBattleButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                switchState(GameState.RACE_INTRO);
            }
        });

        downgradeWheelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (player.wheel.ordinal() > 0) {
                    player.wheel = Wheel.values()[player.wheel.ordinal() - 1];
                }
            }
        });

        upgradeWheelButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (player.wheel.ordinal() < wheels.size() - 1) {
                    player.wheel = Wheel.values()[player.wheel.ordinal() + 1];
                }
            }
        });

        downgradeSpokesButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (player.spoke.ordinal() > 0) {
                    player.spoke = Spoke.values()[player.spoke.ordinal() - 1];
                }
            }
        });

        upgradeSpokesButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (player.spoke.ordinal() < spokes.size() - 1) {
                    player.spoke = Spoke.values()[player.spoke.ordinal() + 1];
                }
            }
        });

        suspensionDowngradeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (player.suspension.ordinal() > 0) {
                    player.suspension = Suspension.values()[player.suspension.ordinal() - 1];
                }
            }
        });

        suspensionUpgradeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (player.suspension.ordinal() < Suspension.values().length - 1) {
                    player.suspension = Suspension.values()[player.suspension.ordinal() + 1];
                }
            }
        });

        addHorseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (player.amountOfHorses < 8) {
                    player.amountOfHorses++;
                }
            }
        });

        removeHorseButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (player.amountOfHorses > 1) {
                    player.amountOfHorses--;
                }
            }
        });

        font = skin.getFont("default-font");
    }

    private void switchState(GameState newState) {
        stateTimer = 0;
        switch (newState) {
            case SETUP:
                setupEnemy();
                break;
            case RACE_OUTRO:
                if (player.health < 0) {
                    player.position = -1000;
                    player.health = 1;
                }
                break;
            case RACE:
                player.health = player.getDefense();
                enemy.health = enemy.getDefense();
                playerRounds = enemyRounds = 0;
                break;
            case PLAYER_WON:
                won.play();
                break;
        }
        state = newState;
    }

    private void setupEnemy() {
        enemy.amountOfHorses = MathUtils.random(1, 8);
        enemy.spoke = Spoke.values()[MathUtils.random(Spoke.values().length - 1)];
        enemy.suspension = Suspension.values()[MathUtils.random(Suspension.values().length - 1)];
        enemy.wheel = Wheel.values()[MathUtils.random(Wheel.values().length - 1)];
        Color[] colors = {Color.RED, Color.TEAL, Color.FIREBRICK, Color.GOLD};
        enemy.color = colors[MathUtils.random(colors.length - 1)];

        String[] ranks = {"", "Centurion", "Legatus", "Optio", "Palatine", "Evocatus"};
        String[] sil1 = {"Jul", "Bru", "Hec", "Aug", "Aur", "Cla", "Dru", "Luc", "Mar"};
        String[] sil2 = {"ius", "tus", "tor", "ustus", "elius", "udius", "sus", "cus", "ina"};
        StringBuilder b = new StringBuilder();
        b.append(ranks[MathUtils.random(ranks.length - 1)]);
        if (b.length > 0) {
            b.append(' ');
        }
        b.append(sil1[MathUtils.random(sil1.length - 1)]);
        b.append(sil2[MathUtils.random(sil2.length - 1)]);
        enemyName = b.toString();
    }

    private <T extends Enum<?>> void registerSprites(Map<T, Sprite> map, String baseName, T[] values, float originX, float originY, float width, float height) {
        for (T t : values) {
            Sprite sprite = new Sprite(assetManager.<Texture>get(getFileNameOf(baseName, t)));
            sprite.setOrigin(originX, originY);
            sprite.setSize(width, height);
            map.put(t, sprite);
        }
    }

    private <T extends Enum<?>> void loadTexturesFor(T[] values, String baseName) {
        for (T t : values) {
            assetManager.load(getFileNameOf(baseName, t), Texture.class);
        }
    }

    private String getFileNameOf(String baseName, Enum<?> anEnum) {
        return baseName + "_" + anEnum.name().toLowerCase() + ".png";
    }

    @Override
    public void render() {
        float deltaTime = Gdx.graphics.getDeltaTime();
        gameTime += deltaTime;
        stateTimer += deltaTime;
        Gdx.gl.glClearColor(0, 0.3f, 0.6f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        batch.draw(sun, 3500, 1700, 500, 500);
        batch.setColor(1, 1, 1, 0.7f);
        for (int i = 0; i < 20; i++) {
            batch.draw(sun, 4050, 1955, -300, 5, 500, 10, 1, 1, i * 18 + gameTime * 30);
        }
        batch.setColor(Color.WHITE);
        batch.draw(ground, 0, 0, 1024 * 4, 64, gameTime * WHEEL_SPINNING_SPEED * player.getTopSpeed() / -200, 0, gameTime * WHEEL_SPINNING_SPEED * player.getTopSpeed() / -200 + 10, 0.75f);
        renderChariot(player);
        switch (state) {
            case SETUP:
                break;
            default:
                renderChariot(enemy);
                break;
        }
        batch.end();

        setTextAndVisibilty(suspensionDowngradeButton, suspensionUpgradeButton, "suspension", player.suspension, Suspension.values());
        setTextAndVisibilty(downgradeSpokesButton, upgradeSpokesButton, "spokes", player.spoke, Spoke.values());
        setTextAndVisibilty(downgradeWheelButton, upgradeWheelButton, "wheels", player.wheel, Wheel.values());
        addHorseButton.setVisible(player.amountOfHorses < 8);
        removeHorseButton.setVisible(player.amountOfHorses > 1);

        switch (state) {
            case SETUP:
                stage.act();
                stage.draw();

                uiBatch.begin();
                renderStats(player);
                uiBatch.end();
                break;
            case RACE_INTRO:
                if (stateTimer < 3) {
                    batch.getProjectionMatrix().setToOrtho2D(0, 0, 1024 * (1 + stateTimer), 600 * (1 + stateTimer));
                    enemy.position = 10 + 500 * (stateTimer - 3);
                    uiBatch.begin();
                    font.draw(uiBatch, "" + (int) (4 - stateTimer), 400, 300);
                    uiBatch.end();
                } else {
                    switchState(RACE);
                }
                break;
            case PLAYER_LOST:
            case PLAYER_WON:
                if (stateTimer < 5) {
                    uiBatch.begin();
                    if (state == PLAYER_WON) {
                        font.draw(uiBatch, "You won!", 400, 300);
                    } else {
                        font.draw(uiBatch, "You lost!", 400, 300);
                    }
                    uiBatch.end();
                    moveChariots();
                } else {
                    switchState(RACE_OUTRO);
                }
                break;
            case RACE_OUTRO:
                if (stateTimer < 3) {
                    batch.getProjectionMatrix().setToOrtho2D(0, 0, 1024 * (4 - stateTimer), 600 * (4 - stateTimer));
                    player.position += MathUtils.clamp(10 - player.position, -1000, 1000) * deltaTime;
                } else {
                    player.position = 10;
                    switchState(SETUP);
                }
                break;
            case RACE:
                player.position += deltaTime * player.getTopSpeed() * 10;
                enemy.position += deltaTime * enemy.getTopSpeed() * 10;
                moveChariots();
                if (enemyRounds > MAX_ROUNDS) {
                    switchState(PLAYER_LOST);
                }
                if (playerRounds > MAX_ROUNDS) {
                    switchState(PLAYER_WON);
                }
                if (Math.abs(enemy.position - player.position) < 500) {
                    if (player.hitTimer <= 0) {
                        hit.play();
                        player.hitTimer = 40 / player.getAttackPower();
                        enemy.health -= 1;
                        if (enemy.health < 0) {
                            dead.play();
                            switchState(PLAYER_WON);
                        }
                    }
                    if (enemy.hitTimer <= 0) {
                        hit.play();
                        enemy.hitTimer = 40 / enemy.getAttackPower();
                        player.health -= 1;
                        if (player.health < 0) {
                            dead.play();
                            switchState(PLAYER_LOST);
                        }
                    }
                }
                uiBatch.begin();
                renderRaceStats();
                uiBatch.end();
                break;
        }

        player.hitTimer = Math.max(0, player.hitTimer - deltaTime);
        enemy.hitTimer = Math.max(0, enemy.hitTimer - deltaTime);

    }

    private void moveChariots() {
        if (player.position > 4000) {
            player.position -= 5000;
            playerRounds++;
        }
        if (enemy.position > 4000) {
            enemyRounds++;
            enemy.position -= 5000;
        }
    }

    private void renderRaceStats() {
        font.draw(uiBatch, "Player round: " + playerRounds + " / " + MAX_ROUNDS + ", health = " + player.health, 10, 580);
        font.draw(uiBatch, enemyName + " round: " + playerRounds + " / " + MAX_ROUNDS + ", health = " + enemy.health, 10, 560);
    }

    private <T extends Enum<T> & Title> void setTextAndVisibilty(TextButton downgradeButton, TextButton upgradeButton, String name, Enum<T> enumValue, T[] values) {
        if (enumValue.ordinal() > 0) {
            downgradeButton.setText("Downgrade " + name + " to " + values[enumValue.ordinal() - 1].title());
            downgradeButton.setVisible(true);
        } else {
            downgradeButton.setVisible(false);
        }
        if (enumValue.ordinal() < values.length - 1) {
            upgradeButton.setText("Upgrade " + name + " to " + values[enumValue.ordinal() + 1].title());
            upgradeButton.setVisible(true);
        } else {
            upgradeButton.setVisible(false);
        }
    }

    private float bobbing(float gameTime, float strength) {
        float delta = 0;
        for (float f = 7; f < 100; f *= 1.7f) {
            delta += Math.cos(gameTime * f) / f * strength;
        }
        return delta;
    }

    private void renderStats(Chariot player) {
        font.draw(uiBatch, "Top speed: " + player.getTopSpeed(), 10, 580);
        font.draw(uiBatch, "Attack power: " + player.getAttackPower(), 10, 560);
        font.draw(uiBatch, "Defense: " + player.getDefense(), 10, 540);
        font.draw(uiBatch, "Suspension: " + player.suspension.title(), 10, 520);
        font.draw(uiBatch, "Spokes: " + player.spoke.title(), 10, 500);
    }

    private void renderChariot(Chariot chariot) {
        if (chariot.health < 0) {
            return;
        }
        float baseTime = this.gameTime + chariot.timeOffset;
        float strength = (1 - chariot.suspension.dampening) * 50;

        float x = chariot.position;
        float y = 10;

        Sprite chariotWheel = wheels.get(chariot.wheel);
        Sprite chariotWagon = wagons.get(chariot.wagon);

        float wheelBob = bobbing(baseTime + 0.2f, 40);
        chariotWheel.setPosition(x + 20, y + wheelBob + 30);
        float wagonBob = bobbing(baseTime, strength);
        chariotWagon.setPosition(x, y + 90 + wagonBob);
        body.setPosition(x + 40, y + 140 + bobbing(baseTime - 0.1f, strength));
        head.setPosition(x + 45, y + 300 + bobbing(baseTime - 0.13f, strength));
        head.setRotation(wagonBob / 2);
        sword.setPosition(x + 90, y + 240 + bobbing(baseTime - 0.18f, strength));
        sword.setRotation(-10 + bobbing(baseTime - 0.18f, strength) / 2 - Math.min(2, chariot.hitTimer) * 50);
        chariotWheel.rotate(Gdx.graphics.getDeltaTime() * player.getTopSpeed() * WHEEL_SPINNING_SPEED);

        body.setColor(chariot.color);
        body.draw(batch);
        head.setColor(chariot.color);
        head.draw(batch);
        sword.draw(batch);
        chariotWagon.draw(batch);
        Sprite spokeSprite = spokes.get(chariot.spoke);
        float wx = x + 90;
        float wy = y + 115 + wheelBob;
        spokeSprite.setPosition(wx, wy);
        for (int i = 0; i < chariot.amountOfSpokes; i++) {
            spokeSprite.setRotation((360 * i / chariot.amountOfSpokes + baseTime * WHEEL_SPINNING_SPEED * player.getTopSpeed()) % 360);
            spokeSprite.draw(batch);
        }
        chariotWheel.draw(batch);

        for (int i = 0; i < chariot.amountOfHorses; i++) {
            renderHorse(baseTime + i * 0.3f, x + 300 + i * 50, y, WHEEL_SPINNING_SPEED * player.getTopSpeed() / 20);
            line.setSize(230 + i * 50, 8);
            line.setPosition(x + 170, y + 170 + wagonBob);
            line.setRotation(tmp.set(230 + i * 50, 60 - wagonBob).angle());
            line.draw(batch);
        }
    }

    public void renderHorse(float baseTime, float x, float y, float speed) {
        y += 130;

        float angleDeg = (float) Math.cos(baseTime * speed) * 5;
        float span = 3 * speed;
        float fl = (float) Math.cos((baseTime - 0.1f) * speed) * span;
        float bl = (float) Math.cos((baseTime - 0.3f) * speed) * span;

        tmp.set(90, 0).rotate(angleDeg);
        horsePart.setSize(128, 16);
        horsePart.setOrigin(0, 8);
        horsePart.setPosition(x + tmp.x + 128, y + tmp.y + 40);
        horsePart.setRotation(fl - 90);

        horsePart.draw(batch);
        horsePart.setPosition(x - tmp.x + 128, y - tmp.y + 40);
        horsePart.setRotation(bl - 90);
        horsePart.draw(batch);

        horsePart.setPosition(x, y);
        horsePart.setSize(256, 128);
        horsePart.setOrigin(128, 64);
        horsePart.setRotation(angleDeg);
        horsePart.draw(batch);

        float fr = (float) Math.cos((baseTime - 0.2f) * speed) * span;
        float br = (float) Math.cos((baseTime - 0.4f) * speed) * span;

        horsePart.setSize(140, 16);
        horsePart.setOrigin(0, 8);
        horsePart.setPosition(x + tmp.x + 128, y + tmp.y + 40);
        horsePart.setRotation(fr - 90);

        horsePart.draw(batch);
        horsePart.setPosition(x - tmp.x + 128, y - tmp.y + 40);
        horsePart.setRotation(br - 90);
        horsePart.draw(batch);

        horsePart.setPosition(x - tmp.x + 95, y - tmp.y + 70);
        horsePart.setRotation(angleDeg * 3 - 120);
        horsePart.setColor(Color.GRAY);
        horsePart.draw(batch);
        horsePart.setColor(Color.WHITE);

        tmp.scl(1.1f);
        horsePart.setPosition(x + tmp.x + 120, y + tmp.y + 50);
        horsePart.setRotation(angleDeg * 2 + 60);
        horsePart.setSize(100, 60);
        horsePart.setOrigin(0, 30);
        horsePart.draw(batch);

        tmp2.set(100, 0).rotate(angleDeg * 2f + 60).add(tmp);

        // Left ear
        horsePart.setSize(32, 16);
        horsePart.setRotation(angleDeg * 2 + 75);
        horsePart.setPosition(x + tmp2.x + 105, y + tmp2.y + 70);
        horsePart.setOrigin(0, 8);
        horsePart.draw(batch);

        horsePart.setPosition(x + tmp2.x + 90, y + tmp2.y + 50);
        horsePart.setRotation(angleDeg / 2 - 20);
        horsePart.setSize(120, 50);
        horsePart.setOrigin(0, 25);
        horsePart.draw(batch);

        // Right ear
        horsePart.setSize(32, 16);
        horsePart.setRotation(angleDeg + 95);
        horsePart.setPosition(x + tmp2.x + 105, y + tmp2.y + 70);
        horsePart.setOrigin(0, 8);
        horsePart.draw(batch);
    }

    @Override
    public void dispose() {
        batch.dispose();
        assetManager.dispose();
    }
}
