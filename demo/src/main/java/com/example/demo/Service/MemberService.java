package com.example.demo.Service;

import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.example.demo.Component.GetMemberInfoParam;
import com.example.demo.Component.MemberRegisterParam;
import com.example.demo.Component.MemberUpdateParam;
import com.example.demo.Component.SendEmailParam;
import com.example.demo.Component.FavoriteListComponent.FavoriteListDetailParam;
import com.example.demo.Component.FavoriteListComponent.FavoriteListNameParam;
import com.example.demo.Component.FavoriteListComponent.FavoriteListStockCommentParam;
import com.example.demo.Component.FavoriteListComponent.FavoriteListStockDeleteParam;
import com.example.demo.Component.FavoriteListComponent.UpdateFavoriteListNameParam;
import com.example.demo.Entity.FavoriteListDetailModel;
import com.example.demo.Entity.FavoriteListNameModel;
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
import net.sf.json.JSONArray;
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

    public JSONObject updateFavoriteListName(UpdateFavoriteListNameParam data) {
        MemberModel member = new MemberModel();
        ArrayList<FavoriteListNameModel> list_names = new ArrayList<FavoriteListNameModel>();
        ArrayList<FavoriteListNameModel> new_list_names = new ArrayList<FavoriteListNameModel>();
        ArrayList<FavoriteListDetailModel> list_stocks = new ArrayList<FavoriteListDetailModel>();
        ArrayList<FavoriteListDetailModel> new_list_stocks = new ArrayList<FavoriteListDetailModel>();

        // Check member exists, and get member_id.
        if ((member = MemberRepo.FindByAccount(data.getAccount())) == null) {
            return ResponseService.responseError("error", "會員帳號尚未建立");
        }

        // get old list name info.
        list_names = ListNameRepo.FindListByMemberAndListName(member.getMid(), data.getList_name());
        if (list_names.size() == 0 || !list_names.get(0).getStatus().equals("0")) {
            return ResponseService.responseError("error", "會員帳號名下查無清單 \"" + data.getList_name() + "\"");
        }

        new_list_names = ListNameRepo.FindListByMemberAndListName(member.getMid(), data.getNew_list_name());
        // new list name is not exist, old list name update to new name.
        if (new_list_names.size() == 0) {
            list_names.get(0).setFavorite_list_name(data.getNew_list_name());
            ListNameRepo.save(list_names.get(0));
            return ResponseService.responseSuccess(new JSONObject());
        }

        if (new_list_names.get(0).getStatus().equals("0"))
            return ResponseService.responseError("error", "會員帳號已創建同清單 \"" + data.getNew_list_name() + "\"");

        // if new list is invalid and old list exists, new list update to valid and old
        // list valid stock info insert into new stock info.
        list_stocks = ListDetailRepo.FindDetailByListNameId(list_names.get(0).getList_name_id());
        for (FavoriteListDetailModel stock_item : list_stocks) {
            // if new list stock exists, update to valid. Otherwise, old list stock info
            // insert into new list stock.
            if (stock_item.getStatus().equals("0")) {
                new_list_stocks = ListDetailRepo.FindListStockInfoByListNameIdAndStock(
                        new_list_names.get(0).getList_name_id(),
                        stock_item.getStock_id());
                if (new_list_stocks.size() == 0) {
                    FavoriteListDetailModel new_stock_item = new FavoriteListDetailModel();
                    new_stock_item.setList_name_id(new_list_names.get(0).getList_name_id());
                    new_stock_item.setStock_id(stock_item.getStock_id());
                    new_stock_item.setStock_name(stock_item.getStock_name());
                    new_stock_item.setComment(stock_item.getComment());
                    new_stock_item.setStatus("0");
                    new_stock_item.setCreate_user("system");
                    new_stock_item.setUpdate_user("system");
                    ListDetailRepo.save(new_stock_item);
                } else {
                    new_list_stocks.get(0).setStatus("0");
                    ListDetailRepo.save(new_list_stocks.get(0));
                }
                // update old stock list status to invalid.
                stock_item.setStatus("1");
                ListDetailRepo.save(stock_item);
            }
        }
        // old list update to invalid, and new list update to valid.
        list_names.get(0).setStatus("1");
        ListNameRepo.save(list_names.get(0));

        new_list_names.get(0).setStatus("0");
        ListNameRepo.save(new_list_names.get(0));
        return ResponseService.responseSuccess(new JSONObject());
    }

    public JSONObject createMember(MemberRegisterParam data) {
        String member_info_redis_key = "member_info:" + data.getAccount();

        // 檢核會員帳號是否存在
        String member_string = this.stringRedisTemplate.opsForValue().get(member_info_redis_key);
        if (member_string != null || (MemberRepo.FindByAccount(data.getAccount())) != null) {
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
        // 檢核會員帳號是否存在
        MemberModel member = MemberRepo.FindByAccountAndPassword(data.getAccount(), data.getPassword());
        if (member == null) {
            return ResponseService.responseError("error", "會員帳號或密碼錯誤");
        }

        String get_member_info_redis_key = "member_info:" + data.getAccount();
        int redis_ttl = 86400*3; // redis存活 3 days
        JSONObject response_data;

        // add member login log data
        LoginLogModel loginlogModel = new LoginLogModel();
        loginlogModel.setMid_fk(member);
        loginlogModel.setCreate_user("system");
        loginlogModel.setUpdate_user("system");
        LoginLogRepo.save(loginlogModel);

        //get member info by redis.
        response_data = getMemberInfoByRedis(data.getAccount());
        if (response_data != null) {
            response_data.remove("member_id");
            return ResponseService.responseSuccess(response_data);
        }

        response_data = new JSONObject();
        response_data.put("member_account", member.getMember_account());
        response_data.put("name", member.getName());
        response_data.put("telephone", member.getTelephone());
        response_data.put("member_account_verification(Y/N)", member.getIsValid().equals("99") ? "N" : "Y");
        response_data.put("member_account_create_timestamp", member.getCreate_time().toString());
        response_data.put("member_id", member.getMid());

        // set member info into redis
        this.stringRedisTemplate.opsForValue().setIfAbsent(get_member_info_redis_key,
                response_data.toString(), redis_ttl, TimeUnit.SECONDS);

        response_data.remove("member_id");
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

    public JSONObject getFavoriteList(String member_account) {
        MemberModel member = new MemberModel();
        ArrayList<FavoriteListNameModel> list_names = new ArrayList<FavoriteListNameModel>();
        ArrayList<FavoriteListDetailModel> list_details = new ArrayList<FavoriteListDetailModel>();
        JSONArray response_data = new JSONArray();

        // Check member exists, and get mid.
        if ((member = MemberRepo.FindByAccount(member_account)) == null) {
            return ResponseService.responseError("error", "會員帳號尚未建立");
        }

        list_names = ListNameRepo.FindListByMemberId(member.getMid());
        for (FavoriteListNameModel sub_list : list_names) {
            // list 無效跳過不回傳
            if (!sub_list.getStatus().equals("0"))
                continue;

            JSONObject response_item = new JSONObject();
            JSONArray item_array = new JSONArray();
            response_item.put("list_name", sub_list.getFavorite_list_name());
            list_details = ListDetailRepo.FindDetailByListNameId(sub_list.getList_name_id());

            for (FavoriteListDetailModel detail : list_details) {
                // stock id 無效跳過不回傳
                if (!detail.getStatus().equals("0"))
                    continue;

                JSONObject item_detail = new JSONObject();
                item_detail.put("stock_id", detail.getStock_id());
                item_detail.put("stock_name", detail.getStock_name());
                item_detail.put("comment", detail.getComment());
                item_array.add(item_detail);
            }
            response_item.put("stock_list", item_array);
            response_data.add(response_item);
        }

        return ResponseService.responseJSONArraySuccess(response_data);
    }

    public JSONObject addFavoriteListName(FavoriteListNameParam data) {
        MemberModel member = new MemberModel();
        ArrayList<FavoriteListNameModel> exist_list = new ArrayList<FavoriteListNameModel>();
        FavoriteListNameModel result_list = new FavoriteListNameModel();
        // 檢核會員帳號是否存在
        if ((member = MemberRepo.FindByAccount(data.getAccount())) == null) {
            return ResponseService.responseError("error", "查無會員帳號");
        }

        exist_list = ListNameRepo.FindListByMemberAndListName(member.getMid(), data.getList_name());
        if (exist_list.size() > 1) {
            return ResponseService.responseError("error",
                    "list_name: \"" + data.getList_name() + "\" 重複" + exist_list.size() + "筆");
        }
        if (exist_list.size() == 1) {
            if (exist_list.get(0).getStatus().equals("0")) {
                return ResponseService.responseError("error", "list_name: \"" + data.getList_name() + "\" 已重複");
            }
            // list is exist and status invalid, update list status to valid.
            exist_list.get(0).setStatus("0");
            ListNameRepo.save(exist_list.get(0));

            return ResponseService.responseSuccess(new JSONObject());
        }

        // insert a new list.
        result_list.setMember_id(member.getMid());
        result_list.setFavorite_list_name(data.getList_name());
        result_list.setStatus("0");
        result_list.setCreate_user("system");
        result_list.setUpdate_user("system");
        ListNameRepo.save(result_list);

        return ResponseService.responseSuccess(new JSONObject());
    }

    public JSONObject deleteFavoriteListName(FavoriteListNameParam data) {
        MemberModel member = new MemberModel();
        ArrayList<FavoriteListNameModel> exist_list = new ArrayList<FavoriteListNameModel>();
        ArrayList<FavoriteListDetailModel> stock_list = new ArrayList<FavoriteListDetailModel>();

        // check member account exists.
        if ((member = MemberRepo.FindByAccount(data.getAccount())) == null) {
            return ResponseService.responseError("error", "查無會員帳號");
        }

        // check list is exists and valid.
        exist_list = ListNameRepo.FindListByMemberAndListName(member.getMid(), data.getList_name());
        if (exist_list.size() == 0 || !exist_list.get(0).getStatus().equals("0"))
            return ResponseService.responseError("error", "list_name: \"" + data.getList_name() + "\" 尚未創建");

        // update list status into invalid.
        exist_list.get(0).setStatus("1");
        ListNameRepo.save(exist_list.get(0));

        // update valid stock_list into invalid.
        stock_list = ListDetailRepo.FindDetailByListNameId(exist_list.get(0).getList_name_id());
        for (FavoriteListDetailModel stock_item : stock_list) {
            if (!stock_item.getStatus().equals("0"))
                continue;

            stock_item.setStatus("1");
            ListDetailRepo.save(stock_item);
        }
        return ResponseService.responseSuccess(new JSONObject());
    }

    public JSONObject addFavoriteListStock(FavoriteListDetailParam data) {
        MemberModel member = new MemberModel();
        ArrayList<FavoriteListNameModel> list_name = new ArrayList<FavoriteListNameModel>();
        ArrayList<FavoriteListDetailModel> list_datail = new ArrayList<FavoriteListDetailModel>();
        FavoriteListDetailModel result_datail = new FavoriteListDetailModel();

        // 檢核會員帳號是否存在
        if ((member = MemberRepo.FindByAccount(data.getAccount())) == null) {
            return ResponseService.responseError("error", "查無會員帳號");
        }

        list_name = ListNameRepo.FindListByMemberAndListName(member.getMid(), data.getList_name());
        if (list_name.size() == 0) {
            return ResponseService.responseError("error", "查無list_name: \"" + data.getList_name() + "\"無法新增");
        }
        if (list_name.size() > 1) {
            return ResponseService.responseError("error", "favorite list name資料異常，重複共" + list_name.size() + "筆");
        }

        list_datail = ListDetailRepo.FindListStockInfoByListNameIdAndStock(list_name.get(0).getList_name_id(),
                data.getStock_id());
        if (list_datail.size() > 1) {
            return ResponseService.responseError("error", " stock id資料異常，重複共" + list_datail.size() + "筆");
        }
        if (list_datail.size() != 0) {
            if (list_datail.get(0).getStatus().equals("0")) {
                return ResponseService.responseError("error", " 資料已創建");
            }
            // list is exist and status invalid, update list status to valid.
            if (!list_name.get(0).getStatus().equals("0")) {
                list_name.get(0).setStatus("0");
                ListNameRepo.save(list_name.get(0));
            }
            list_datail.get(0).setStatus("0");
            ListDetailRepo.save(list_datail.get(0));
            return ResponseService.responseSuccess(new JSONObject());
        }

        result_datail.setList_name_id(list_name.get(0).getList_name_id());
        result_datail.setStock_id(data.getStock_id());
        result_datail.setStock_name(data.getStock_name());
        result_datail.setStatus("0");
        result_datail.setComment("");
        result_datail.setCreate_user("system");
        result_datail.setUpdate_user("system");

        ListDetailRepo.save(result_datail);

        return ResponseService.responseSuccess(new JSONObject());
    }

    public JSONObject deleteFavoriteListStock(FavoriteListStockDeleteParam data) {
        MemberModel member = new MemberModel();
        ArrayList<FavoriteListNameModel> exist_list = new ArrayList<FavoriteListNameModel>();
        ArrayList<FavoriteListDetailModel> stock_list = new ArrayList<FavoriteListDetailModel>();

        // check member account exists.
        if ((member = MemberRepo.FindByAccount(data.getAccount())) == null) {
            return ResponseService.responseError("error", " 查無會員帳號");
        }
        // check list is exists and get list_name_id.
        exist_list = ListNameRepo.FindListByMemberAndListName(member.getMid(), data.getList_name());
        if (exist_list.size() == 0) {
            return ResponseService.responseError("error", " list_name: \"" + data.getList_name() + "\" 尚未創建");
        }
        if (exist_list.size() > 1) {
            return ResponseService.responseError("error", " list_name資料異常，重複共" + exist_list.size());
        }

        // get stock_list info
        stock_list = ListDetailRepo.FindListStockInfoByListNameIdAndStock(exist_list.get(0).getList_name_id(),
                data.getStock_id());
        if (stock_list.size() > 1) {
            return ResponseService.responseError("error", " stock_id資料異常，重複共" + stock_list.size() + "筆");
        }
        if (stock_list.size() == 0 || !stock_list.get(0).getStatus().equals("0")) {
            return ResponseService.responseError("error", " list中查無此stock_id");
        }

        // update valid stock_list into invalid.
        stock_list.get(0).setStatus("1");
        ListDetailRepo.save(stock_list.get(0));

        return ResponseService.responseSuccess(new JSONObject());
    }

    public JSONObject updateFavoriteListStockComment(FavoriteListStockCommentParam data) {
        MemberModel member = new MemberModel();
        ArrayList<FavoriteListNameModel> exist_lists = new ArrayList<FavoriteListNameModel>();
        ArrayList<FavoriteListDetailModel> stock_list = new ArrayList<FavoriteListDetailModel>();

        // check member account exists.
        if ((member = MemberRepo.FindByAccount(data.getAccount())) == null) {
            return ResponseService.responseError("error", " 查無會員帳號");
        }
        // check list is exists and get each list_name_id.
        exist_lists = ListNameRepo.FindListByMemberId(member.getMid());

        // traverse all exists lists name, and get stock list info to update stock
        // comment
        for (FavoriteListNameModel list_item : exist_lists) {
            stock_list = ListDetailRepo.FindListStockInfoByListNameIdAndStock(list_item.getList_name_id(),
                    data.getStock_id());
            if (stock_list.size() == 0)
                continue;

            if (stock_list.get(0).getComment().equals(data.getStock_comment())) {
                return ResponseService.responseSuccess(new JSONObject());
            }
            // update stock comments.
            stock_list.get(0).setComment(data.getStock_comment());
            ListDetailRepo.save(stock_list.get(0));
        }
        return ResponseService.responseSuccess(new JSONObject());
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

    public MemberModel checkMember(String member_account) {
        MemberModel member = new MemberModel();
        //if member does not exist in redis, and then get member by mysql.
        if ((member = getMemberModelByRedis(member_account)) == null) {
            member = MemberRepo.FindByAccount(member_account);
        }
        return member;
    }

    private JSONObject getMemberInfoByRedis(String member_account) {
        return JSONObject.fromObject(this.stringRedisTemplate.opsForValue().get("member_info:" + member_account));
    }

    private MemberModel getMemberModelByRedis(String member_account) {
        MemberModel member = new MemberModel();
        JSONObject member_redis;
        String member_redis_string  = this.stringRedisTemplate.opsForValue().get("member_info:" + member_account);

        if(member_redis_string == null) {
            return null;
        }   

        member_redis = JSONObject.fromObject(member_redis_string);
        member.setMember_account(member_redis.getString("member_account"));
        member.setName(member_redis.getString("name"));
        member.setTelephone(member_redis.getString("telephone"));
        member.setIsValid(member_redis.getString("member_account_verification(Y/N)").equals("Y") ? "00" : "99");
        member.setMid(member_redis.getString("member_id"));
        return member;
    }
}
