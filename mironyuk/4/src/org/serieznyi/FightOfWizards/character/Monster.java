package org.serieznyi.FightOfWizards.character;

final public class Monster extends Character {
    public Monster(String name, int health) {
        super(name, health);
    }

    public String toString() {
        return "Монстр \"" + name + "\"";
    }

    public void action() {
//        "Монстр <имя> атакует <имя, цели> на <количество> единиц урона урона "
    }
}
