package com.example.demo.Service;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import net.sf.json.JSONObject;

import com.example.demo.Component.MemberRegisterParam;
import com.example.demo.Component.MemberComponent.UpdateFavoriteListNameParam;
import com.example.demo.Component.GetMemberInfoParam;

import com.example.demo.Entity.MemberModel;
import com.example.demo.Entity.FavoriteListDetailModel;
import com.example.demo.Entity.FavoriteListNameModel;
import com.example.demo.Entity.LoginLogModel;

import com.example.demo.Repository.MemberRespository;
import com.example.demo.Repository.FavoriteListDetailRespository;
import com.example.demo.Repository.FavoriteListNameRespository;
import com.example.demo.Repository.LoginLogRespository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import lombok.Data;

@Data
@Service
public class MemberService {
    @Autowired
    private MemberRespository MemberRepo;
    @Autowired
    private LoginLogRespository LoginLogRepo;
    @Autowired
    private FavoriteListNameRespository ListNameRepo;
    @Autowired
    private FavoriteListDetailRespository ListDetailRepo;

    public MemberService() {
    }

    public JSONObject updateFavoriteListName(UpdateFavoriteListNameParam data) {
        MemberModel member = new MemberModel();
        ArrayList<FavoriteListNameModel> list_names = new ArrayList<FavoriteListNameModel>();
        ArrayList<FavoriteListNameModel> new_list_names = new ArrayList<FavoriteListNameModel>();
        ArrayList<FavoriteListDetailModel> list_stocks = new ArrayList<FavoriteListDetailModel>();
        ArrayList<FavoriteListDetailModel> new_list_stocks = new ArrayList<FavoriteListDetailModel>();
        
        //Check member exists, and get member_id.
        if((member = MemberRepo.FindByAccount(data.getAccount())) == null) {
            return responseError("會員帳號尚未建立");
        }

        //get old list name info.
        list_names = ListNameRepo.FindByListName(member.getMid(), data.getList_name());
        if(list_names.size() == 0 || !list_names.get(0).getStatus().equals("0")) {
            return responseError("會員帳號名下查無清單 \"" + data.getList_name() +"\"");
        }

        new_list_names = ListNameRepo.FindByListName(member.getMid(), data.getNew_list_name());
        //new list name is not exist, old list name update to new name.    
        if(new_list_names.size() == 0) {
            list_names.get(0).setFavorite_list_name(data.getNew_list_name());
            ListNameRepo.save(list_names.get(0));
            return responseSuccess();
        }

        if(new_list_names.get(0).getStatus().equals("0"))
            return responseError("會員帳號已創建同清單 \"" + data.getNew_list_name() +"\"");

        //if new list is invalid and old list exists, new list update to valid and old list valid stock info insert into new stock info.    
        list_stocks = ListDetailRepo.FindDetailByListNameId(list_names.get(0).getList_name_id());
        for(FavoriteListDetailModel stock_item : list_stocks) {
            //if new list stock exists, update to valid. Otherwise, old list stock info insert into new list stock.
            if(stock_item.getStatus().equals("0")) {
                new_list_stocks = ListDetailRepo.FindDetailByListStock(new_list_names.get(0).getList_name_id(), stock_item.getStock_id());
                if(new_list_stocks.size() == 0) {
                    FavoriteListDetailModel new_stock_item = new FavoriteListDetailModel();
                    new_stock_item.setList_name_id(new_list_names.get(0).getList_name_id());
                    new_stock_item.setStock_id(stock_item.getStock_id());
                    new_stock_item.setStock_name(stock_item.getStock_name());
                    new_stock_item.setComment(stock_item.getComment());
                    new_stock_item.setStatus("0");
                    new_stock_item.setCreate_user("system");
                    new_stock_item.setUpdate_user("system");
                    ListDetailRepo.save(new_stock_item);
                }else {
                    new_list_stocks.get(0).setStatus("0");
                    ListDetailRepo.save(new_list_stocks.get(0));
                }
                //update old stock list status to invalid.
                stock_item.setStatus("1");
                ListDetailRepo.save(stock_item);
            }
        }
        //old list update to invalid, and new list update to valid.
        list_names.get(0).setStatus("1");
        ListNameRepo.save(list_names.get(0));

        new_list_names.get(0).setStatus("0");
        ListNameRepo.save(new_list_names.get(0));
        return responseSuccess();
    }

    public JSONObject createMember(MemberRegisterParam data) {
        // 檢核會員帳號是否存在
        if ((MemberRepo.FindByAccount(data.getAccount())) != null) {
            return responseError("會員帳號已創建");
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

        return responseSuccess();
    }

    public JSONObject getMemberInfo(GetMemberInfoParam data) {
        JSONObject response_data = new JSONObject();

        // 檢核會員帳號是否存在
        MemberModel member = MemberRepo.FindByAccountAndPassword(data.getAccount(), data.getPassword());
        if (member == null) {
            return responseError("會員帳號或密碼錯誤");
        }

        // add member login log data
        LoginLogModel loginlogModel = new LoginLogModel();
        loginlogModel.setMid_fk(member);
        loginlogModel.setCreate_user("system");
        loginlogModel.setUpdate_user("system");
        LoginLogRepo.save(loginlogModel);

        response_data.put("member_account", member.getMember_account());
        response_data.put("name", member.getName());
        response_data.put("telephone", member.getTelephone());
        response_data.put("member_account_verification(Y/N)", member.getIsValid().equals("99") ? "N" : "Y");
        response_data.put("member_account_create_timestamp", member.getCreate_time().toString());

        return responseGetMemberInfoSuccess(response_data);
    }

    public JSONObject SendEmailCertification(JSONObject data) {
        try {
            String customer_email = data.getString("member_account");
            // 檢核會員帳號是否存在
            if (MemberRepo.existByAccount(customer_email) == 0)
                return responseError("查無此會員帳號");

            // create random 6 numbers and letters
            String salt_number = getSaltString(6);
            SimpleMailMessage mail_message = new SimpleMailMessage();

            // send email to customer
            mail_message.setFrom("stockhelper.service@gmail.com");
            mail_message.setTo(customer_email);
            mail_message.setSubject("主旨：【stockhelper】驗證碼");
            mail_message.setText("您好，\n\n您使用的stockhelper驗證碼為 " + salt_number + " 。");

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
        // 預設密碼:stockhelpler.service1234
        javaMailSender.setPassword("tnzvoawqvwqqlsrv");
        Properties properties = javaMailSender.getJavaMailProperties();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        javaMailSender.setJavaMailProperties(properties);
        ;
        return javaMailSender;
    }

    private String getSaltString(int len) {
        String SALTCHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder salt = new StringBuilder(len);
        Random number = new Random();
        while (salt.length() < len) { // length of the random string.
            salt.append(SALTCHARS.charAt(number.nextInt(SALTCHARS.length())));
        }
        return salt.toString();
    }

    private JSONObject responseSuccess() {
        JSONObject data = new JSONObject();
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        status_code.put("status", "success");
        status_code.put("desc", "");

        result.put("metadata", status_code);
        result.put("data", data);
        return result;
    }

    private JSONObject responseGetMemberInfoSuccess(JSONObject data) {
        JSONObject status_code = new JSONObject();
        JSONObject result = new JSONObject();

        status_code.put("status", "success");
        status_code.put("desc", "");

        result.put("metadata", status_code);
        result.put("data", data);
        return result;
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

        data.put("data", "");

        status_code.put("status", "error");
        status_code.put("desc", error_msg);

        result.put("metadata", status_code);
        result.put("data", data);
        return result;
    }
}
