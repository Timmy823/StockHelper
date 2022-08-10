package com.example.demo.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = {MyConstraintValidator.class})
public @interface SpecificValidator {
    String message() default "必須為指定值";
    String[] strValues() default{};
    int[] intValues() default {};

   /*
   使用指定枚舉
   1、枚舉重写toString方法，其返回值一般就是getCode()，將其與参數值比较。
   2、枚舉上使用ContainValidator
   */
   Class<?> enumValue() default Class.class;
   //分组
   Class<?>[] groups() default {};
   //負載
   Class<? extends Payload>[] payload() default {};
}
