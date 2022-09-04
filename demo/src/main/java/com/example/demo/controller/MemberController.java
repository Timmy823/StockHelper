package com.example.demo.Controller;

import net.sf.json.JSONObject;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Component.MemberRegisterParam;
import com.example.demo.Component.MemberComponent.FavoriteListNameParam;
import com.example.demo.Component.GetMemberInfoParam;

import com.example.demo.Service.MemberService;

@RestController
@EnableJpaAuditing
public class MemberController {
    @Autowired
    MemberService memberService;

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
    @PostMapping("/member/sendEmailCertification")
    public JSONObject SendEmailCertification(@RequestBody JSONObject input) {
        try {
            return memberService.SendEmailCertification(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }
}
