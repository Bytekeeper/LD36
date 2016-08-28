package org.dw.ld36;

/**
 * Created by dante on 28.08.2016.
 */
public enum Spoke implements Title {
    WOOD("wood", 1),
    BRONZE("bronze", 2),
    IRON("iron", 3);

    public final String name;
    public final float weight;

    Spoke(String name, float weight) {
        this.name = name;
        this.weight = weight;
    }

    @Override
    public String title() {
        return name;
    }
}
