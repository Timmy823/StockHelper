package com.example.demo.Controller;

import net.sf.json.JSONObject;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Component.MemberRegisterParam;
import com.example.demo.Service.MemberService;

@RestController
@EnableJpaAuditing
public class MemberController {
    @Autowired
    MemberService memberService;
    @PostMapping("/member/createMember")
    public JSONObject CheckAndCreateMember(@Valid @RequestBody MemberRegisterParam input) {
        try{
        return memberService.CheckAndCreateMember(input);
        }catch(Exception io){
            return memberService.responseError(io.toString());
        }
    }
}
