package com.example.demo.Controller;

import javax.validation.Valid;

import com.example.demo.Component.GetMemberInfoParam;
import com.example.demo.Component.MemberRegisterParam;
import com.example.demo.Component.MemberUpdateParam;
import com.example.demo.Component.MemberComponent.FavoriteListDetailParam;
import com.example.demo.Component.MemberComponent.FavoriteListNameParam;
import com.example.demo.Service.MemberService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import net.sf.json.JSONObject;

@RestController
@EnableJpaAuditing
public class MemberController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    MemberService memberService = new MemberService(stringRedisTemplate);

    @PostMapping("/member/deleteFavoriteListName")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject deleteFavoriteListName(@Valid @RequestBody FavoriteListNameParam input) {
        try {
            return memberService.deleteFavoriteListName(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }

    @PostMapping("/member/addFavoriteListName")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject addFavoriteListName(@Valid @RequestBody FavoriteListNameParam input) {
        try {
            return memberService.addFavoriteListName(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }

    @PostMapping("/member/addFavoriteListStock")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject addFavoriteListDetail(@Valid @RequestBody FavoriteListDetailParam input) {
        try {
            return memberService.addFavoriteListDetail(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }

    @PostMapping("/member/createMember")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject createMember(@Valid @RequestBody MemberRegisterParam input) {
        try {
            return memberService.createMember(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }

    @PostMapping("/member/getMemberInfo")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public JSONObject getMemberInfo(@Valid @RequestBody GetMemberInfoParam input) {
        try {
            return memberService.getMemberInfo(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/member/updateMember")
    public JSONObject updateMember(@Valid @RequestBody MemberUpdateParam input) {
        try {
            return memberService.updateMember(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/member/sendEmailCertification")
    public JSONObject SendEmailCertification(@RequestBody JSONObject input) {
        try {
            return memberService.SendEmailCertification(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }
}
