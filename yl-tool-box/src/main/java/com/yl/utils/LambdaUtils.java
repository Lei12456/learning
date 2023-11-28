package com.yl.utils;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import lombok.SneakyThrows;
import org.apache.ibatis.reflection.property.PropertyNamer;

import java.lang.reflect.Field;

public class LambdaUtils {

    @SneakyThrows
    public static <T> Class<?> getReturnType(SFunction<T, ?> func) {
        com.baomidou.mybatisplus.core.toolkit.support.SerializedLambda lambda = com.baomidou.mybatisplus.core.toolkit.LambdaUtils.resolve(func);
        Class<?> aClass = lambda.getInstantiatedType();
        String fieldName = PropertyNamer.methodToProperty(lambda.getImplMethodName());
        Field field = aClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.getType();
    }
}
