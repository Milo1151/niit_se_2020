package org.serieznyi.serialization.serializer;

import org.serieznyi.serialization.serializer.annotation.Serialize;
import org.serieznyi.serialization.serializer.annotation.SerializeIgnoreField;
import org.serieznyi.serialization.serializer.annotation.SerializeName;
import org.serieznyi.serialization.serializer.exception.NormalizerException;
import org.serieznyi.serialization.serializer.value.ListValue;
import org.serieznyi.serialization.serializer.value.MapValue;
import org.serieznyi.serialization.serializer.value.ObjectValue;
import org.serieznyi.serialization.serializer.value.Value;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

public final class Normalizer {
  ObjectValue normalize(Object objectForNormalization) {
    Class<?> clazz = objectForNormalization.getClass();

    if (!isHasDefaultConstructor(clazz)) {
      throw NormalizerException.fromMessage("Class doesn't have default constructor: " + clazz.getName());
    }

    String typeName = getTypeName(clazz);
    boolean skipNull = isSkipNull(clazz);

    ObjectValue resultObject = new ObjectValue(typeName);

    for (Field declaredField : getFields(clazz)) {
      declaredField.setAccessible(true);

      if (isIgnoredField(declaredField)) {
        continue;
      }

      try {
        Object fieldObject = declaredField.get(objectForNormalization);

        String fieldName = getFieldName(declaredField);

        if (fieldObject == null) {
          if (!skipNull) {
            resultObject.addNullValue(fieldName);
          }
        } else if (isPrimitiveTypeField(declaredField)) {
          resultObject.addPrimitiveValue(fieldName, fieldObject.toString());
        } else if (fieldObject instanceof Enum<?>) {
          resultObject.addEnumValue(fieldName, fieldObject.toString());
        } else if (fieldObject instanceof List) {
          List<ObjectValue> list = new ArrayList<>();

          for (Object o : (List<?>) fieldObject) {
            list.add(normalize(o));
          }

          resultObject.addListValue(fieldName, new ListValue(list));
        } else if (fieldObject instanceof Map) {
          Map<String, ObjectValue> map = new HashMap<>();

          for (Map.Entry<?, ?> entry: ((Map<?, ?>) fieldObject).entrySet()) {
            map.put(entry.getKey().toString(), normalize(entry.getValue()));
          }

          resultObject.addMapValue(fieldName, new MapValue(map));
        } else {
          resultObject.addObjectValue(fieldName, normalize(fieldObject));
        }
      } catch (IllegalAccessException e) {
        throw new NormalizerException(e);
      }
    }

    return resultObject;
  }

  private boolean isHasDefaultConstructor(Class<?> clazz) {
    for (Constructor<?> declaredConstructor : clazz.getDeclaredConstructors()) {
      if (declaredConstructor.getParameterCount() == 0) {
        return true;
      }
    }

    return false;
  }

  Object denormalize(ObjectValue value, Class<?> clazz) {
    try {
      return valueToObject(value, clazz);
    } catch (NoSuchMethodException
        | IllegalAccessException
        | InstantiationException
        | InvocationTargetException e) {
      throw new NormalizerException(e);
    }
  }

  private String getTypeName(Class<?> clazz) {
    Serialize serializeAnnotation = clazz.getAnnotation(Serialize.class);

    String typeName = clazz.getName();

    if (serializeAnnotation != null && !serializeAnnotation.typeName().isEmpty()) {
      typeName = serializeAnnotation.typeName();
    }

    return typeName;
  }

  private boolean isSkipNull(Class<?> clazz) {
    Serialize serializeAnnotation = clazz.getAnnotation(Serialize.class);

    return serializeAnnotation != null && serializeAnnotation.skipNull();
  }

  private boolean isIgnoredField(Field declaredField) {
    return declaredField.isAnnotationPresent(SerializeIgnoreField.class);
  }

  private String getFieldName(Field declaredField) {
    SerializeName serializeName = declaredField.getAnnotation(SerializeName.class);

    return serializeName != null ? serializeName.value() : declaredField.getName();
  }

  private Object valueToObject(ObjectValue value, Class<?> clazz)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
          InstantiationException {

    if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
      clazz = getObjectValueType(value);
    }

    Constructor<?> declaredConstructor = clazz.getDeclaredConstructor();
    declaredConstructor.setAccessible(true);

    Object object = declaredConstructor.newInstance();

    for (Field declaredField : getFields(clazz)) {
      declaredField.setAccessible(true);

      String fieldName = getFieldName(declaredField);

      Value<?> fieldValue = getFieldValue(fieldName, value);

      if (fieldValue != null) {
        declaredField.set(object, castValueTo(declaredField.getType(), fieldValue));
      }
    }

    return object;
  }

  private Object castValueTo(Class<?> declaredFieldType, Value<?> value)
      throws InvocationTargetException, NoSuchMethodException, InstantiationException,
          IllegalAccessException {
    Object result;

    switch (value.getType()) {
      case PRIMITIVE: {
        result = castPrimitiveValue(declaredFieldType, value.getValue());

        break;
      }
      case ENUM: {
        result = Enum.valueOf((Class<Enum>) declaredFieldType, (String) value.getValue());

        break;
      }
      case OBJECT: {
        result = valueToObject((ObjectValue) value, declaredFieldType);

        break;
      }
      case MAP: {
        Map<String, Object> map = new HashMap<>();

        for (Map.Entry<String, ObjectValue> entry : ((Map<String, ObjectValue>) value.getValue()).entrySet()) {
          Class<?> itemClazz = getObjectValueType(entry.getValue());

          map.put(entry.getKey(), valueToObject(entry.getValue(), itemClazz));
        }

        result = map;
        break;
      }
      case LIST: {
        List<Object> list = new ArrayList<>();

        for (ObjectValue value1 : (List<ObjectValue>) value.getValue()) {

          Class<?> itemClazz = getObjectValueType(value1);

          list.add(valueToObject(value1, itemClazz));
        }

        result = list;

        break;
      }
      default: {
        throw new NormalizerException("Неизвестный тип: " + value.getType());
      }
    }

    return result;
  }

  private Class<?> getObjectValueType(ObjectValue value1) {
    try {
      return Class.forName(value1.getTypeName());
    } catch (ClassNotFoundException e) {

      // TODO брать тип через SerializeName

      throw new NormalizerException(e);
    }
  }

  private Object castPrimitiveValue(Class<?> declaredFieldType, Object value) {
    Object result;

    if (declaredFieldType.equals(short.class) || declaredFieldType.equals(Short.class)) {
      result = Short.parseShort((String) value);
    } else if (declaredFieldType.equals(int.class) || declaredFieldType.equals(Integer.class)) {
      result = Integer.parseInt((String) value);
    } else if (declaredFieldType.equals(long.class) || declaredFieldType.equals(Long.class)) {
      result = Long.parseLong((String) value);
    } else if (declaredFieldType.equals(float.class) || declaredFieldType.equals(Float.class)) {
      result = Float.parseFloat((String) value);
    } else if (declaredFieldType.equals(double.class) || declaredFieldType.equals(Double.class)) {
      result = Double.parseDouble((String) value);
    } else if (declaredFieldType.equals(boolean.class) || declaredFieldType.equals(Boolean.class)) {
      result = Boolean.parseBoolean((String) value);
    } else if (declaredFieldType.equals(char.class)) {
      result = ((String) value).charAt(0);
    } else {
      result = declaredFieldType.cast(value);
    }

    return result;
  }

  private Value<?> getFieldValue(String fieldName, ObjectValue value) {
    return value.getValue().get(fieldName);
  }

  private boolean isPrimitiveTypeField(Field field) {
    Class<?> type = field.getType();

    return type == short.class
            || type == Short.class
            || type == int.class
            || type == Integer.class
            || type == long.class
            || type == Long.class
            || type == float.class
            || type == Float.class
            || type == double.class
            || type == Double.class
            || type == String.class
            || type == boolean.class
            || type == Boolean.class
            || type == char.class
            || type == Number.class;
  }

  private List<Field> getFields(Class<?> clazzArg) {
    List<Field> fields = new ArrayList<>();

    Class<?> clazz = clazzArg;

    do {
      fields.addAll(Arrays.asList(clazz.getDeclaredFields()));

    } while ((clazz = clazz.getSuperclass()) != null && !clazz.equals(Object.class));

    return fields;
  }
}
