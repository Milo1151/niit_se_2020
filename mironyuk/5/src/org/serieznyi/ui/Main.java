package org.serieznyi.ui;

import org.serieznyi.ui.element.Button;
import org.serieznyi.ui.element.CheckBox;
import org.serieznyi.ui.element.TextField;
import org.serieznyi.ui.exception.ElementsOverlapException;
import org.serieznyi.ui.exception.ReadOnlyException;
import java.util.concurrent.ThreadLocalRandom;

public final class Main {
  private static final int MAX_X_COORDINATE = 100;
  private static final int MAX_Y_COORDINATE = 100;

  public static void main(String[] args) {
    ElementFactory elementFactory = new ElementFactory(MAX_X_COORDINATE, MAX_Y_COORDINATE);

    System.out.println("\nСоздаем UI c элементами управления:\n");

    UI ui = new UI(MAX_X_COORDINATE, MAX_Y_COORDINATE);

    TextField xCoordinateTextField =
        new TextField(0, 6, 5, 5, "Координата x для нового элемента", "");
    ui.addElement(xCoordinateTextField);

    TextField yCoordinateTextField =
        new TextField(0, 12, 5, 5, "Координата y для нового элемента", "");
    ui.addElement(yCoordinateTextField);

    Button addElementButton =
        new Button(
            0,
            0,
            5,
            5,
            "Добавить элемент",
            () -> {
              Element randomElement =
                      elementFactory.createRandom(
                      Integer.parseInt(xCoordinateTextField.getValue()),
                      Integer.parseInt(yCoordinateTextField.getValue()));
              ui.addElement(randomElement);
            });
    ui.addElement(addElementButton);

    System.out.println("\nГенерируем новые элементы:\n");

    for (int i = 0; i < 10; i++) {
      xCoordinateTextField.setValue(nextRandomXCoordinate().toString());
      yCoordinateTextField.setValue(nextRandomYCoordinate().toString());

      try {
        addElementButton.click();
      } catch (ElementsOverlapException e) {
        System.out.println("Ошибка: " + e.getMessage());
      }
    }

    System.out.println("\nВзаимодействуем с элементами интерфейса:\n");

    for (Element element : ui.getAllElements()) {
      System.out.println(element);

      if (element instanceof Clickable && element != addElementButton) {
        try {
          ((Clickable) element).click();
        } catch (ReadOnlyException e) {
          System.out.println("Ошибка: " + e.getMessage());
        }
      }

      if (element instanceof CheckBox) {
        System.out.println("\tСостояние: " + ((CheckBox) element).getState());
      }

      if (element instanceof TextField) {
        System.out.println("\tТекст: " + ((TextField) element).getValue());
      }

      System.out.println();
    }
  }

  private static Integer nextRandomXCoordinate() {
    return ThreadLocalRandom.current().nextInt(0, MAX_X_COORDINATE + 1);
  }

  private static Integer nextRandomYCoordinate() {
    return ThreadLocalRandom.current().nextInt(0, MAX_Y_COORDINATE + 1);
  }
}
