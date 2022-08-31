package com.example.demo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import com.example.demo.Repository.MemberRespository;

import java.util.Properties;
import java.util.Random;

import net.sf.json.JSONObject;

@Service
public class MemberService {
    @Autowired
    private MemberRespository MemberRepo;

    public JSONObject SendEmailCertification(String customer_email) {
        try {    
            //檢核會員帳號是否存在
            if(MemberRepo.existByAccount(customer_email) == 0) 
                return responseError("查無此會員帳號");
            
            //create random 6 numbers and letters
            String salt_number = getSaltString(6);
            SimpleMailMessage mail_message = new SimpleMailMessage();

            //send email to customer
            mail_message.setFrom("stockhelper.service@gmail.com");
            mail_message.setTo(customer_email);
            mail_message.setSubject("主旨：【stockhelper】驗證碼");
            mail_message.setText("您好，\n\n您使用的stockhelper驗證碼為 "+ salt_number+ " 。");

            mailSender().send(mail_message);
            return responseEmailCertification(salt_number);
        } catch (MailException e) {
            return responseError(e.getMessage());
        }
    }

    @Bean
    private JavaMailSender mailSender() {         
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setProtocol("smtp");
        javaMailSender.setHost("smtp.gmail.com");
        javaMailSender.setPort(587);
        javaMailSender.setUsername("stockhelpler.service@gmail.com");
        //預設密碼:stockhelpler.service1234
        javaMailSender.setPassword("tnzvoawqvwqqlsrv");
        Properties properties = javaMailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        javaMailSender.setJavaMailProperties(properties);;
        return javaMailSender;
    }

    private String getSaltString(int len){
        String SALTCHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder(len);
        Random number = new Random();
        while (salt.length() < len) { // length of the random string.
            salt.append(SALTCHARS.charAt(number.nextInt(SALTCHARS.length())));
        }
        return salt.toString();
    }

    private JSONObject responseEmailCertification(String saltString) {
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();
        data.put("certification_code", saltString);

        status_code.put("status", "success");
        status_code.put("desc", "");

        result.put("metadata", status_code);
        result.put("data", data);
        return result;
    }

    public JSONObject responseError(String error_msg) {
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        data.put("data","");

        status_code.put("status", "error");
        status_code.put("desc", error_msg);

        result.put("metadata", status_code);
        result.put("data", data);
        return result;
    }
}
