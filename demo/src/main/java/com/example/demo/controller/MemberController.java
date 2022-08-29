package com.example.demo.Controller;

import net.sf.json.JSONObject;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Component.MemberRegisterParam;
import com.example.demo.Component.GetFavoriteListParam;
import com.example.demo.Service.MemberService;

@RestController
@EnableJpaAuditing
public class MemberController {
    @Autowired
    MemberService memberService;
    @PostMapping("/member/createMember")
    public JSONObject createMember(@Valid @RequestBody MemberRegisterParam input) {
        try{
            return memberService.createMember(input);
        }catch(Exception io){
            return memberService.responseError(io.toString());
        }
    }

    @PostMapping("/member/searchFavoriteList")
    @CrossOrigin(origins = "http://localhost:5277", allowedHeaders = "")
    public JSONObject getFavoriteList(@Valid @RequestBody GetFavoriteListParam input) {
        try{
            return memberService.getFavoriteList(input);
        }catch(Exception io){
            return memberService.responseError(io.toString());
        }
    }
}