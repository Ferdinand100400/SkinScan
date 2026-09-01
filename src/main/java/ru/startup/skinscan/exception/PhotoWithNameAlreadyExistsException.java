package ru.startup.skinscan.exception;

public class PhotoWithNameAlreadyExistsException extends RuntimeException {

  public PhotoWithNameAlreadyExistsException(String namePhoto) {
    super("Фото с именем " + namePhoto + " уже есть");
    System.out.println(getMessage());
  }
}
