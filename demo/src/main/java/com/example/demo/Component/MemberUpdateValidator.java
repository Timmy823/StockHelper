package com.example.demo.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import net.sf.json.JSONObject;

public class MemberUpdateValidator implements ConstraintValidator<SpecificValidator, JSONObject> {
    private String password;
    private String name;
    private String telephone;
    private String verification;
    
    @Override
    public boolean isValid(JSONObject value, ConstraintValidatorContext context) {
        Pattern pattern = Pattern.compile("(09)+[\\d]{8}");

        password = value.getString("password");
        name = value.getString("member_name");
        telephone = value.getString("telephone");
        verification = value.getString("verification");
        System.out.println("password: "+password);

        //必須其一有值，並針對有值的欄位做驗證
        if(password.length() == 0 && name.length() == 0 && telephone.length() == 0 && verification.length() == 0) {
            return false;
        }    
        if(password.length() != 0 && (password.length()<9 || password.length()>16)) {
            return false;
        }
        if(name.length() != 0 && name.length()>50) {
            return false;
        }
        if(telephone.length() != 0 && !pattern.matcher(telephone).matches()) {
            return false;
        }
        if(verification.length() != 0 && !verification.equals("Y")){
            return false;
        }
        return true;
    }
}
