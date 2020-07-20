package org.serieznyi.FightOfWizards;

import org.serieznyi.FightOfWizards.action.CausingDamageAction;
import org.serieznyi.FightOfWizards.character.Character;
import org.serieznyi.FightOfWizards.character.wizard.Spell;
import org.serieznyi.FightOfWizards.character.wizard.spell.FireTouchSpell;
import org.serieznyi.FightOfWizards.character.wizard.spell.HealingSpell;
import org.serieznyi.FightOfWizards.character.wizard.spell.LightningSpell;
import org.serieznyi.FightOfWizards.character.wizard.spell.UniversalSpell;
import org.serieznyi.FightOfWizards.factory.NameFactory;
import org.serieznyi.FightOfWizards.factory.SpellBagFactory;
import org.serieznyi.FightOfWizards.factory.character.CharacterFactory;
import org.serieznyi.FightOfWizards.factory.character.MonsterFactory;
import org.serieznyi.FightOfWizards.factory.character.RandomCharacterFactory;
import org.serieznyi.FightOfWizards.factory.character.WizardFactory;
import org.serieznyi.FightOfWizards.logging.Logger;
import org.serieznyi.FightOfWizards.logging.handler.OutputHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class Main {
  final static Logger LOGGER = Logger.create();

  public static void main(String[] args) {
    SceneOptions sceneOptions = SceneOptions.fromDefault();

    Scene scene = buildScene(sceneOptions);

    LOGGER.setHandler(new OutputHandler());

    scene.run();
  }

  private static Scene buildScene(SceneOptions sceneOptions) {
    CharacterFactory characterFactory = Dependencies.getCharacterFactory();

    Scene scene = new Scene(sceneOptions.getSceneSize());

    for (int i = 0; i < sceneOptions.getCharacterCount(); i++) {
      scene.appendCharacterToRandomPosition(characterFactory.create());
    }

    return scene;
  }

  private static class Dependencies {
    public static CharacterFactory getCharacterFactory() {
      NameFactory nameFactory =
          new NameFactory(
              new HashMap<Character.Type, String[]>() {
                {
                  put(
                      Character.Type.MONSTER,
                      new String[] {
                        "Сатана", "Кракен", "Голод", "Голум", "Харрун", "Смерть", "Чума", "Ворон",
                        "Крыса", "Зомби",
                      });
                  put(
                      Character.Type.WIZARD,
                      new String[] {
                        "Мерлин",
                        "Арарат",
                        "Синусин",
                        "Косинусин",
                        "Тангенсин",
                        "Байтум",
                        "Килабайтум",
                        "Мегабайтум",
                        "Гигабайтум",
                        "Терабайтум",
                        "Гендальф",
                        "Тирисиум",
                      });
                }
              });

      return new RandomCharacterFactory(
          new CharacterFactory[] {
            new MonsterFactory(nameFactory), new WizardFactory(nameFactory, getSpellBagFactory())
          });
    }

    public static SpellBagFactory getSpellBagFactory() {
      ThreadLocalRandom random = ThreadLocalRandom.current();

      final int MIN_HEALING = 25;
      final int MAX_HEALING = 50;
      final int MIN_SPELL_DAMAGE = 50;
      final int MAX_SPELL_DAMAGE = 75;

      Supplier<Integer> damageGenerator =
          () -> random.nextInt(MIN_SPELL_DAMAGE, MAX_SPELL_DAMAGE + 1);

      Set<Spell> spells = new HashSet<>();

      spells.add(new HealingSpell(random.nextInt(MIN_HEALING, MAX_HEALING + 1)));

      spells.add(new LightningSpell(damageGenerator.get()));

      spells.add(new FireTouchSpell(damageGenerator.get()));

      Spell chainLightingSpell =
          UniversalSpell.builder()
              .withName("Цепная молния")
              .withDescription("Наносит урон, всем персонажам на сцене, кроме мага, который произносит заклинание.")
              .withTargetsFinder((c, s) -> s.getOpponentsFor(c))
              .withValue(damageGenerator.get())
              .withActionCreator((character, value) -> CausingDamageAction.causeLightingDamage(value.intValue()))
              .withSuccessfulCallback(w -> s -> c -> v -> LOGGER.takeDamageTo(w, s, c, v.intValue()))
              .build();

      spells.add(chainLightingSpell);

      Spell banishingMonstersSpell =
          UniversalSpell.builder()
              .withName("Изгнание монстров")
              .withDescription("Наносит урон всем монстрам")
              .withTargetsFinder((c, s) -> s.getOpponentsFor(c, Character.Type.MONSTER))
              .withValue(damageGenerator.get())
              .withActionCreator((character, value) -> CausingDamageAction.causeMagicalDamage(value.intValue()))
              .withSuccessfulCallback(w -> s -> c -> v -> LOGGER.takeDamageTo(w, s, c, v.intValue()))
              .build();

      spells.add(banishingMonstersSpell);

      Spell migraineSpell =
          UniversalSpell.builder()
              .withName("Мигрень")
              .withDescription("Наносит урон всем магам")
              .withTargetsFinder((c, s) -> s.getOpponentsFor(c, Character.Type.WIZARD))
              .withValue(damageGenerator.get())
              .withActionCreator((character, value) -> CausingDamageAction.causeMagicalDamage(value.intValue()))
              .withSuccessfulCallback(w -> s -> c -> v -> LOGGER.takeDamageTo(w, s, c, v.intValue()))
              .build();

      spells.add(migraineSpell);

      Spell wallOfFireSpell =
          UniversalSpell.builder()
              .withName("Стена огня")
              .withDescription("Наносит урон всем персонажам на четных позициях")
              .withTargetsFinder((c, s) -> s.getOpponentsFor(c, Character.Type.MONSTER))
              .withValue(damageGenerator.get())
              .withActionCreator((character, value) -> CausingDamageAction.causeFireDamage(value.intValue()))
              .withSuccessfulCallback(w -> s -> c -> v -> LOGGER.takeDamageTo(w, s, c, v.intValue()))
              .build();

      spells.add(wallOfFireSpell);

      return new SpellBagFactory(spells);
    }
  }
}
