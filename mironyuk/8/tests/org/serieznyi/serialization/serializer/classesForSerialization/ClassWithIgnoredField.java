package org.serieznyi.serialization.serializer.classesForSerialization;

import org.serieznyi.serialization.serializer.annotation.Serialize;
import org.serieznyi.serialization.serializer.annotation.SerializeIgnoreField;

@Serialize
public class ClassWithIgnoredField {
    public String fieldOne = "one";

    @SerializeIgnoreField
    public String fieldTwo = "two";
}
