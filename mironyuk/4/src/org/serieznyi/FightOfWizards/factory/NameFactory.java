package org.serieznyi.FightOfWizards.factory;

import org.serieznyi.FightOfWizards.character.Character;
import org.serieznyi.FightOfWizards.util.Functions;

import java.util.Arrays;
import java.util.Map;

public final class NameFactory {
  private final Map<Character.Type, String[]> namesPool;

  public NameFactory(Map<Character.Type, String[]> namesPool) {
    for (Map.Entry<Character.Type, String[]> item : namesPool.entrySet()) {
      if (item.getValue().length == 0) {
        throw new IllegalArgumentException(
            "Для группы персонажей " + item.getKey() + " не заданы имена");
      }

      String[] namesForType =
          Arrays.stream(item.getValue()).sorted(Functions::randomComparator).toArray(String[]::new);

      namesPool.put(item.getKey(), namesForType);
    }

    this.namesPool = namesPool;
  }

  public String nextName(Character.Type nameFor) {
    String[] namesPool = this.namesPool.get(nameFor);

    if (0 == namesPool.length) {
      throw new RuntimeException("Не осталось доступных имен"); // TODO используй модификаторы
    }

    String selectedName = namesPool[0];

    this.namesPool.replace(nameFor, Arrays.copyOfRange(namesPool, 1, namesPool.length));

    return selectedName;
  }
}
