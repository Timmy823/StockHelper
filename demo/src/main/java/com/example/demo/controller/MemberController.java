package com.example.demo.Controller;

import javax.validation.Valid;

import com.example.demo.Component.GetMemberInfoParam;
import com.example.demo.Component.MemberRegisterParam;
import com.example.demo.Component.MemberUpdateParam;
import com.example.demo.Component.SendEmailParam;
import com.example.demo.Service.MemberService;
import com.example.demo.Service.ResponseService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import net.sf.json.JSONObject;

@RestController
@EnableJpaAuditing
@Validated
public class MemberController {
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    MemberService memberService = new MemberService(stringRedisTemplate);

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/member/createMember")
    public JSONObject createMember(@Valid @RequestBody MemberRegisterParam input) {
        try {
            return memberService.createMember(input);
        } catch (Exception io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/member/getMemberInfo")
    public JSONObject getMemberInfo(@Valid @RequestBody GetMemberInfoParam input) {
        try {
            return memberService.getMemberInfo(input);
        } catch (Exception io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/member/updateMember")
    public JSONObject updateMember(@Valid @RequestBody MemberUpdateParam input) {
        try {
            return memberService.updateMember(input);
        } catch (Exception io) {
            return ResponseService.responseError("error", io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/member/sendEmailCertification")
    public JSONObject SendEmailCertification(@Valid @RequestBody SendEmailParam input) {
        try {
            return memberService.SendEmailCertification(input);
        } catch (Exception io) {
            return ResponseService.responseError("error", io.toString());
        }
    }
}
