package com.example.demo.Controller;

import net.sf.json.JSONObject;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.Component.MemberUpdateParam;
import com.example.demo.Service.MemberService;

@RestController
@EnableJpaAuditing
public class MemberController {
    @Autowired
    MemberService memberService;

    @PostMapping("/member/UpdateMember")
    public JSONObject CheckAndUpdateMember(@Valid @RequestBody MemberUpdateParam input) {
        try{
            return memberService.CheckAndUpdateMember(input);
        }catch(Exception io){
            return memberService.responseError(io.toString());
        }
    }
}
