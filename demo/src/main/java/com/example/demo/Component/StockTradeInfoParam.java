package com.example.demo.Component;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class StockTradeInfoParam{

    @NotEmpty(message = "id不可為空")
    private  String id;

    @NotEmpty(message = "type不可為空")
    @SpecifiedValidator(strValues={"1","2"}, message="type必須為指定1或2或3")
    private  String type;

    //@Past
    @NotNull(message = "date不可為空")
    private Integer specified_date;

    public  String get_stockID(){
        return id;
    }
    public Integer get_date(){
        return specified_date;
    }
    public String get_type(){
        return type;
    }

    public StockTradeInfoParam(String id,Integer specified_date, String type){
        super();
        this.id=id;
        this.type=type;
        this.specified_date=specified_date;
    }
}
