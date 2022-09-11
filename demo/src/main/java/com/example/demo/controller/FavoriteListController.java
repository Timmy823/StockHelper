package com.example.demo.Controller;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import com.example.demo.Component.FavoriteListComponent.*;
import com.example.demo.Service.FavoriteListService;
import com.example.demo.Service.ResponseService;

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
@Validated
public class FavoriteListController {

    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    FavoriteListService favoriteListService = new FavoriteListService(stringRedisTemplate);

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @GetMapping("/favorite_list/getFavoriteList")
    public JSONObject getFavoriteList (
            @RequestParam("member_account")
            @NotEmpty(message = "it can not be empty.")
            String member_account) {
        try {
            return favoriteListService.getFavoriteList(member_account);
        } catch (Exception io) {
            return ResponseService.responseError("99999", io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/favorite_list/addFavoriteListName")
    public JSONObject addFavoriteListName(@Valid @RequestBody FavoriteListNameParam input) {
        try {
            return favoriteListService.addFavoriteListName(input);
        } catch (Exception io) {
            return ResponseService.responseError("99999", io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/favorite_list/updateFavoriteListName")
    public JSONObject updateFavoriteListName(@Valid @RequestBody UpdateFavoriteListNameParam input) {
        try {
            return favoriteListService.updateFavoriteListName(input);
        } catch (Exception io) {
            return ResponseService.responseError("99999", io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/favorite_list/deleteFavoriteListName")
    public JSONObject deleteFavoriteListName(@Valid @RequestBody FavoriteListNameParam input) {
        try {
            return favoriteListService.deleteFavoriteListName(input);
        } catch (Exception io) {
            return ResponseService.responseError("99999", io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/favorite_list/addFavoriteListStock")
    public JSONObject addFavoriteListStock(@Valid @RequestBody FavoriteListDetailParam input) {
        try {
            return favoriteListService.addFavoriteListStock(input);
        } catch (Exception io) {
            return ResponseService.responseError("99999", io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/favorite_list/deleteFavoriteListStock")
    public JSONObject deleteFavoriteListStock(@Valid @RequestBody FavoriteListStockDeleteParam input) {
        try {
            return favoriteListService.deleteFavoriteListStock(input);
        } catch (Exception io) {
            return ResponseService.responseError("99999", io.toString());
        }
    }

    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @PostMapping("/favorite_list/updateFavoriteListStockComment")
    public JSONObject updateFavoriteListStockComment(@Valid @RequestBody FavoriteListStockCommentParam input) {
        try {
            return favoriteListService.updateFavoriteListStockComment(input);
        } catch (Exception io) {
            return ResponseService.responseError("99999", io.toString());
        }
    }
}
