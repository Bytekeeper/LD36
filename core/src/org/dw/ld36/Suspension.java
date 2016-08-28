package org.dw.ld36;

/**
 * Created by dante on 28.08.2016.
 */
public enum Suspension implements Title {
    NONE("buttocks", 0),
    LEAF_SPRING("leaf spring", 0.2f),
    COIL_SPRING("coil spring", 0.5f),
    MACPHERSON_STRUT("MacPherson strut", 0.8f);


    public final float dampening;
    public final String name;

    Suspension(String name, float dampening) {
        this.dampening = dampening;
        this.name = name;
    }

    @Override
    public String title() {
        return name;
    }
}
