package com.example.demo.Component;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class StockMarketIndexParam {
    private  String type;
    private Integer specified_date;
    
    @NotNull(message = "specified date不可為空")
    @Min(value=19110000, message="specified date格式為西元YYYYMMDD")
    public Integer get_date(){
        return specified_date;
    }

    @NotEmpty(message = "type不可為空")
    @Size(max=1, min=1,message = "type格式錯誤")
    @SpecifiedValidator(strValues={"1","2"}, message="type必須為指定1或2")
    public String get_type(){
        return type;
    }

    public StockMarketIndexParam(String id,Integer specified_date, String type){
        super();
        this.type=type;
        this.specified_date=specified_date;
    }
}
