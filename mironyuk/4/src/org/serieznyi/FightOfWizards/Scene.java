package org.serieznyi.FightOfWizards;

import org.serieznyi.FightOfWizards.character.Character;
import org.serieznyi.FightOfWizards.util.FightHelper;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Scene {
    public final int MIN_SCENE_SIZE = 2;
    public final int MAX_SCENE_SIZE = 20;

    private final int size;

    private final Map<Integer, Character> characters = new HashMap<>();
    private final List<Character> deadCharacters = new ArrayList<>();

    public Scene(int size) {
        if (size > MAX_SCENE_SIZE || size < MIN_SCENE_SIZE) {
            throw new IllegalArgumentException("Размер сцены должен быть не меньше " + MIN_SCENE_SIZE + " и не больше " + MAX_SCENE_SIZE);
        }
        this.size = size;
    }

    public void appendCharacterToRandomPosition(Character character)
    {
        if (characters.containsValue(character)) {
            throw new IllegalArgumentException("Персонаж уже на сцене: " + character);
        }

        boolean successfulAppend = false;
        for (int i = 0 ; i < MAX_SCENE_SIZE ; i++) {
            int potentialPosition = ThreadLocalRandom.current().nextInt(0, this.size);
            if (!characters.containsKey(potentialPosition)) {
                characters.put (potentialPosition, character);
                successfulAppend = true;
                break;
            }
        }

        if (!successfulAppend) {
            for (int i = 0 ; i < MAX_SCENE_SIZE ; i++) {
                if (!characters.containsKey(i)) {
                    characters.put (i, character);
                    successfulAppend = true;
                    break;
                }
            }
        }

        if (!successfulAppend) {
            throw new RuntimeException("Не удалось найти свободную позицию на сцене для " + character);
        }
    }

    public Character getRandomOpponentFor(Character character)
    {
        Map<Integer, Character> opponentsFor = getOpponentsFor(character);

        return FightHelper.getRandomOpponentFrom(opponentsFor);
    }

    public Map<Integer, Character> getOpponentsFor(Character character) {
        return characters
                .entrySet()
                .stream()
                .filter((Map.Entry<Integer, Character> v) -> !v.getValue().isDead())
                .filter((Map.Entry<Integer, Character> item) -> !item.getValue().equals(character))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void run()
    {
        System.out.println("Начинается великая битва!");

        if (characters.isEmpty()) {
            System.out.println("Но похоже на нее ни кто не пришел");
            return;
        }

        System.out.printf("На поле боя находится бойцов: %s\n\n", characters.size());

        for (int step = 1 ; getAliveCharacters().size() > 1 ; step++) {
            System.out.println("Шаг: " + step);
            for (Character character: getShuffledCharacters()) {
                if (!hasAnyOpponents() || character.isDead()) {
                    break;
                }

                character.action(this);

                checkBodies();
            }

            System.out.println();
        }

        showWinner();
    }

    private void showWinner() {
        List<Character> alive = getAliveCharacters();

        if (alive.size() != 1) {
            throw new RuntimeException("Что-то пошло не так");
        }

        Character winner = alive.get(0);

        System.out.printf("На поле боя остался только \"%s\"", winner.getName());
    }

    private List<Character> getAliveCharacters() {
        return characters
                .values()
                .stream()
                .filter(character -> !character.isDead())
                .collect(Collectors.toList());
    }

    private void checkBodies() {
        for (Map.Entry<Integer, Character> entry : characters.entrySet()) {
            Character character = entry.getValue();

            if (character.isDead() && !deadCharacters.contains(character)) {
                deadCharacters.add(character);
                System.out.printf("\"%s\" погибает\n", character);
            }
        }
    }

    private List<Character> getShuffledCharacters() {
        return characters
                .entrySet()
                .stream()
                .filter((Map.Entry<Integer, Character> v) -> !v.getValue().isDead())
                .sorted((Map.Entry<Integer, Character> a, Map.Entry<Integer, Character> b) -> ThreadLocalRandom.current().nextInt(-1, 2))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private boolean hasAnyOpponents() {
        return characters
                .entrySet()
                .stream()
                .filter((Map.Entry<Integer, Character> v) -> !v.getValue().isDead())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                .size() > 1;
    }

    public int getIndexFor(Character character) {
        for (Map.Entry<Integer, Character> entry: characters.entrySet()) {
            if (entry.getValue().equals(character)) {
                return entry.getKey();
            }
        }

        throw new RuntimeException("Cant find index for " + character);
    }

    public Optional<Character> findNeighboredOpponentFor(Character character)
    {
        int characterIndex = getIndexFor(character);

        Map<Integer, Character> opponents = getOpponentsFor(character);

        int[] allowedNeighborIndexes = new int[] {characterIndex - 1, characterIndex + 1};

        for (int index : allowedNeighborIndexes) {
            if (opponents.containsKey(index)) {
                return Optional.of(opponents.get(index));
            }
        }

        return Optional.empty();
    }
}
