package com.example.demo.Controller;

import net.sf.json.JSONObject;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.*;

import com.example.demo.Component.MemberUpdateParam;
import com.example.demo.Service.MemberService;

@RestController
@EnableJpaAuditing
public class MemberController {
    @Autowired
    MemberService memberService;

    @CrossOrigin(origins = "http://localhost:5277", allowedHeaders = "")
    @PostMapping("/member/UpdateMember")
    public JSONObject updateMember(@Valid @RequestBody MemberUpdateParam input) {
        try{
            return memberService.updateMember(input);
        }catch(Exception io){
            return memberService.responseError(io.toString());
        }
    }
}
