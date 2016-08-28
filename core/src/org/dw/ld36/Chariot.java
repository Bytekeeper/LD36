package org.dw.ld36;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;

/**
 * Created by dante on 28.08.2016.
 */
public class Chariot {
    public float timeOffset;
    public Suspension suspension = Suspension.NONE;
    public Spoke spoke = Spoke.WOOD;
    public Wagon wagon = Wagon.WOOD;
    public Wheel wheel = Wheel.WOOD;
    public float position = 10;
    public int amountOfHorses = 2;
    public int amountOfSpokes = 8;
    public Color color = Color.WHITE;
    public float hitTimer;
    public int health;

    public int getTopSpeed() {
        float speed = (float) (Math.sqrt(amountOfHorses) * 20) + 15;
        speed -= suspension.dampening * 10;
        speed -= spoke.weight * amountOfSpokes / 3;
        speed -= wheel.weight;
        return (int) Math.max(speed, 0);
    }

    public int getAttackPower() {
        float attackPower = 20;
        attackPower += suspension.dampening * 20;
        return (int) attackPower;
    }

    public int getDefense() {
        float defense = 20;
        defense -= Math.sqrt(amountOfHorses) * 5;
        defense += spoke.weight * amountOfSpokes / 3;
        defense += wheel.weight;
        return (int) defense;
    }
}
