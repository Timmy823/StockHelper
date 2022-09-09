package com.example.demo.Controller;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.example.demo.Component.GetMemberInfoParam;
import com.example.demo.Component.MemberRegisterParam;
import com.example.demo.Component.MemberUpdateParam;
import com.example.demo.Component.MemberComponent.FavoriteListDetailParam;
import com.example.demo.Component.MemberComponent.FavoriteListNameParam;
import com.example.demo.Component.MemberComponent.FavoriteListStockCommentParam;
import com.example.demo.Component.MemberComponent.FavoriteListStockDeleteParam;
import com.example.demo.Component.MemberComponent.UpdateFavoriteListNameParam;
import com.example.demo.Service.MemberService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
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
            return memberService.responseError(io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/member/getMemberInfo")
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

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/member/getFavoriteList")
    public JSONObject getFavoriteList (
            @RequestParam("member_account")
            @NotEmpty(message = "it can not be empty.")
            String member_account) {
        try {
            return memberService.getFavoriteList(member_account);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/member/addFavoriteListName")
    public JSONObject addFavoriteListName(@Valid @RequestBody FavoriteListNameParam input) {
        try {
            return memberService.addFavoriteListName(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/member/updateFavoriteListName")
    public JSONObject updateFavoriteListName(@Valid @RequestBody UpdateFavoriteListNameParam input) {
        try {
            return memberService.updateFavoriteListName(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/member/deleteFavoriteListName")
    public JSONObject deleteFavoriteListName(@Valid @RequestBody FavoriteListNameParam input) {
        try {
            return memberService.deleteFavoriteListName(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/member/addFavoriteListStock")
    public JSONObject addFavoriteListStock(@Valid @RequestBody FavoriteListDetailParam input) {
        try {
            return memberService.addFavoriteListStock(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/member/deleteFavoriteListStock")
    public JSONObject deleteFavoriteListStock(@Valid @RequestBody FavoriteListStockDeleteParam input) {
        try {
            return memberService.deleteFavoriteListStock(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/member/updateFavoriteListStockComment")
    public JSONObject updateFavoriteListStockComment(@Valid @RequestBody FavoriteListStockCommentParam input) {
        try {
            return memberService.updateFavoriteListStockComment(input);
        } catch (Exception io) {
            return memberService.responseError(io.toString());
        }
    }
}
