package org.serieznyi.serialization.serializer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import data.classesForNormalization.*;
import data.classesForNormalization.supportedPrimitive.ClassWithPrimitiveDouble;
import data.classesForNormalization.supportedPrimitive.ClassWithPrimitiveFloat;
import data.classesForNormalization.supportedPrimitive.ClassWithPrimitiveInt;
import data.classesForNormalization.unsupportedPrimitive.ClassWithArray;
import data.classesForNormalization.unsupportedPrimitive.ClassWithCollection;
import org.serieznyi.serialization.serializer.exception.NormalizerException;
import org.serieznyi.serialization.serializer.value.ObjectValue;
import org.serieznyi.serialization.serializer.value.Value;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class NormalizerTest {
    private final Normalizer normalizer = new Normalizer();

    @Test
    void testWhatFailIfSerializeAnnotationNotPresentInClass()
    {
        NormalizerException thrown = assertThrows(
                NormalizerException.class,
                () -> normalizer.normalize(new ClassWithoutSerializeAnnotation()),
                "Fail if annotation not present"
        );

        assertEquals(thrown.getMessage(), "Class not marked by serialization annotation");
    }

    @Test
    void testWhatFailIfDefaultConstructorNotPresentInClass()
    {
        NormalizerException thrown = assertThrows(
                NormalizerException.class,
                () -> normalizer.normalize(new ClassWithoutDefaultConstructor("t1", "t2")),
                "Fail if default constructor not present"
        );

        assertEquals(thrown.getMessage(), "Class doesn't have default constructor");
    }

    private static List<Object> objectsWithPrimitiveTypes() {
        return Arrays.asList(
                new ClassWithPrimitiveInt(),
                new ClassWithPrimitiveDouble(),
                new ClassWithPrimitiveFloat()

        );
    }

    @ParameterizedTest
    @MethodSource("objectsWithPrimitiveTypes")
    void testWhatPrimitiveTypesNormalisationIsWork(Object object)
    {
        ObjectValue objectValue = normalizer.normalize(object);

        assertEquals(objectValue.getValue().size(), 1, "Объект содержит только одно значение");
        assertTrue(objectValue.getValue().containsKey("value"),  "Значение лежит под правильным ключем");
        assertEquals(objectValue.getValue().get("value").getType(), Value.Type.PRIMITIVE, "У значения правильный тип");
    }

    private static List<Object> objectsWithNotSupportedBasicTypes() {
        return Arrays.asList(
                new ClassWithArray(),
                new ClassWithCollection()

        );
    }

    @ParameterizedTest
    @MethodSource("objectsWithNotSupportedBasicTypes")
    void testWhatFailOnNotSupportedBasicTypes(Object object)
    {
        NormalizerException thrown = assertThrows(
                NormalizerException.class,
                () -> normalizer.normalize(object),
                "Fail on not supported basic types"
        );

        assertEquals(thrown.getMessage(), "Class not marked by serialization annotation");
    }

    @Test
    void testWhatFieldNameOverrideIsWork() {
        ObjectValue objectValue = normalizer.normalize(new ClassWithOverriddenTypeAndFieldName());

        assertTrue(objectValue.getValue().containsKey("newFieldName"),  "Значение лежит под правильным ключем");
    }

    @Test
    void testWhatTypeNameOverrideIsWork() {
        ObjectValue objectValue = normalizer.normalize(new ClassWithOverriddenTypeAndFieldName());

        assertEquals(objectValue.getTypeName(), "NewTypeName",  "Название типа контейнера переопределено");
    }

    @Test
    void testWhatIgnoreFieldIsWork() {
        ObjectValue objectValue = normalizer.normalize(new ClassWithIgnoredField());

        assertEquals(objectValue.getValue().size(), 1,  "В результате только одно поле");
        assertTrue(objectValue.getValue().containsKey("fieldOne"),  "Не игнорируемое поле присутствует");
        assertFalse(objectValue.getValue().containsKey("fieldTwo"),  "Поле которое игнорировали отсутсвует");
    }

    @Test
    void testWhatNullFieldsSkipIsWork() {
        ObjectValue objectValue = normalizer.normalize(new ClassWithNullSkipping());

        assertEquals(objectValue.getValue().size(), 1,  "В результате только одно поле");
        assertTrue(objectValue.getValue().containsKey("fieldOne"),  "Инициализированное поле присутствует");
        assertFalse(objectValue.getValue().containsKey("fieldTwo"),  "Не инициализированное поле отсутсвует");
    }

    @Test
    void testWhaNormalisationWithNullFieldIsWork() {
        ObjectValue objectValue = normalizer.normalize(new ClassWithoutNullSkipping());

        assertEquals(objectValue.getValue().size(), 2,  "В результате только одно поле");
        assertTrue(objectValue.getValue().containsKey("fieldTwo"),  "Не инициализированное поле присуствует");
        assertEquals(objectValue.getValue().get("fieldTwo").getType(), Value.Type.NULL, "Значение типа NULL");
    }
}