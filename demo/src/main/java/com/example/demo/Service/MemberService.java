package com.example.demo.Service;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.example.demo.Component.GetMemberInfoParam;
import com.example.demo.Component.MemberRegisterParam;
import com.example.demo.Component.MemberUpdateParam;
import com.example.demo.Component.SendEmailParam;
import com.example.demo.Entity.LoginLogModel;
import com.example.demo.Entity.MemberModel;
import com.example.demo.Repository.FavoriteListDetailRespository;
import com.example.demo.Repository.FavoriteListNameRespository;
import com.example.demo.Repository.LoginLogRespository;
import com.example.demo.Repository.MemberRespository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import lombok.Data;
import net.sf.json.JSONObject;

@Data
@Service
public class MemberService {
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private MemberRespository MemberRepo;

    @Autowired
    private LoginLogRespository LoginLogRepo;
    @Autowired
    private FavoriteListNameRespository ListNameRepo;
    @Autowired
    private FavoriteListDetailRespository ListDetailRepo;

    public MemberService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public JSONObject createMember(MemberRegisterParam data) {
        // 檢核會員帳號是否存在
        if ((MemberRepo.FindByAccount(data.getAccount())) != null) {
            return ResponseService.responseError("error", "會員帳號已創建");
        }

        // add member data
        MemberModel memberModel = new MemberModel();
        memberModel.setMember_account(data.getAccount());
        memberModel.setName(data.getName());
        memberModel.setMember_passwd(data.getPassword());
        memberModel.setTelephone(data.getTelephone());
        memberModel.setIsValid("99");
        memberModel.setCreate_user("system");
        memberModel.setUpdate_user("system");
        MemberRepo.save(memberModel);

        return ResponseService.responseSuccess(new JSONObject());
    }

    public JSONObject updateMember(MemberUpdateParam data) {
        // 檢核會員帳號是否存在
        MemberModel member = MemberRepo.FindByAccount(data.getAccount());
        if (member == null) {
            return ResponseService.responseError("error", "會員帳號或密碼錯誤");
        }

        // if input field not null ,and update member field
        if (data.getPassword().length() != 0)
            member.setMember_passwd(data.getPassword());
        if (data.getName().length() != 0)
            member.setName(data.getName());
        if (data.getTelephone().length() != 0)
            member.setTelephone(data.getTelephone());
        if (data.getVerification().equals("Y"))
            member.setIsValid("00");
        member.setUpdate_user("system");
        MemberRepo.save(member);

        return ResponseService.responseSuccess(new JSONObject());
    }

    public JSONObject getMemberInfo(GetMemberInfoParam data) {
        JSONObject response_data = new JSONObject();

        // 檢核會員帳號是否存在
        MemberModel member = MemberRepo.FindByAccountAndPassword(data.getAccount(), data.getPassword());
        if (member == null) {
            return ResponseService.responseError("error", "會員帳號或密碼錯誤");
        }

        String get_member_info_redis_key = "member_info:" + data.getAccount();
        int redis_ttl = 3600; // redis存活 1 hour

        // add member login log data
        LoginLogModel loginlogModel = new LoginLogModel();
        loginlogModel.setMid_fk(member);
        loginlogModel.setCreate_user("system");
        loginlogModel.setUpdate_user("system");
        LoginLogRepo.save(loginlogModel);

        String member_info_string = this.stringRedisTemplate.opsForValue().get(get_member_info_redis_key);
        if (member_info_string != null) {
            return ResponseService.responseSuccess(JSONObject.fromObject(member_info_string));
        }

        response_data.put("member_account", member.getMember_account());
        response_data.put("name", member.getName());
        response_data.put("telephone", member.getTelephone());
        response_data.put("member_account_verification(Y/N)", member.getIsValid().equals("99") ? "N" : "Y");
        response_data.put("member_account_create_timestamp", member.getCreate_time().toString());

        // set member info into redis
        this.stringRedisTemplate.opsForValue().setIfAbsent(get_member_info_redis_key,
                response_data.toString(), redis_ttl, TimeUnit.SECONDS);

        return ResponseService.responseSuccess(response_data);
    }

    public JSONObject SendEmailCertification(SendEmailParam data) {
        try {
            String customer_email = data.getAccount();
            // 檢核會員帳號是否存在
            if (MemberRepo.existByAccount(customer_email) == 0)
                return ResponseService.responseError("error", "查無此會員帳號");

            // create random 6 numbers and letters
            String salt_number = data.getCertification();
            SimpleMailMessage mail_message = new SimpleMailMessage();

            // send email to customer
            mail_message.setFrom("stockhelper.service@gmail.com");
            mail_message.setTo(customer_email);
            mail_message.setSubject("主旨：【stockhelper】驗證碼");
            mail_message.setText("您好，\n\n您使用的stockhelper驗證碼為 " + salt_number + " 。");

            mailSender().send(mail_message);

            return ResponseService.responseSuccess(new JSONObject());
        } catch (MailException e) {
            return ResponseService.responseError("error", e.getMessage());
        }
    }

    @Bean
    private JavaMailSender mailSender() {
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setProtocol("smtp");
        javaMailSender.setHost("smtp.gmail.com");
        javaMailSender.setPort(587);
        javaMailSender.setUsername("stockhelpler.service@gmail.com");
        // 預設密碼:stockhelpler.service1234
        javaMailSender.setPassword("tnzvoawqvwqqlsrv");
        Properties properties = javaMailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        javaMailSender.setJavaMailProperties(properties);
        return javaMailSender;
    }
}
