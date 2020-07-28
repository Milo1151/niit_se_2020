package org.serieznyi.animal;

import org.serieznyi.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
  public static void main(String[] args) {
    List<Pair<Animal, String>> animalsAndFood = new ArrayList<>();
    animalsAndFood.add(Pair.fromArgs(new Animal("Пушистик", Animal.Type.CAT), "Морковь"));
    animalsAndFood.add(Pair.fromArgs(new Animal("Цезарь", Animal.Type.DUCK), "Батон"));
    animalsAndFood.add(Pair.fromArgs(new Animal("Лапка", Animal.Type.RABBIT), "Капуста"));

    feedAnimals(animalsAndFood);
  }

  private static void feedAnimals(List<Pair<Animal, String>> animalsAndFood) {
    ThreadLocalRandom random = ThreadLocalRandom.current();

    int luckyAnimalIndex = random.nextInt(0, animalsAndFood.size());

    int index = 0;
    for (Pair<Animal, String> pair : animalsAndFood) {

      if (index == luckyAnimalIndex) {
        System.out.printf(
            "Счастливое животное \"%s\" получает двойную порцию \"%s\"\n",
            pair.getFirst().getName(), pair.getSecond());
      } else {
        System.out.printf(
            "Животное \"%s\" с радостью съедает \"%s\"\n",
            pair.getFirst().getName(), pair.getSecond());
      }

      index++;
    }
  }
}
