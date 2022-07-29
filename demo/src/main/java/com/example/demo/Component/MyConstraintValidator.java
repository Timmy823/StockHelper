package com.example.demo.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class MyConstraintValidator implements ConstraintValidator<SpecifiedValidator,Object> {
    private String[] strValues;
    private int[] intValues;

    @Override
    public void initialize(SpecifiedValidator constraintAnnotation) {
        strValues = constraintAnnotation.strValues();
        intValues = constraintAnnotation.intValues();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (null == value) {
            return true;
        }

        //instanceof objectA 指向 null 時這個條件判斷會回傳 false 
        if(value instanceof String){
            for(int i=0; i<strValues.length; i++){
                if(value.equals(strValues[i]))
                    return true;
            }
        }else if(value instanceof Integer){
            for(int i=0; i<intValues.length; i++){
                if(value.equals(intValues[i]))
                    return true;
            }
        }
        return false;
    }
}
