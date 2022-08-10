package com.example.demo.Component;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class StockTradeInfoParam{

    @NotEmpty(message = "id不可為空")
    private String id;

    @NotEmpty(message = "type不可為空")
    @SpecificValidator(strValues={"1","2","3"}, message="type必須為指定\"1\"或\"2\"或\"3\"")
    private String type;

    @NotNull(message = "date不可為空")
    private Integer specific_date;

    public String get_stockID(){
        return id;
    }
    public Integer get_date(){
        return specific_date;
    }
    public String get_type(){
        return type;
    }

    public StockTradeInfoParam(String id,Integer specific_date, String type){
        super();
        this.id=id;
        this.type=type;
        this.specific_date=specific_date;
    }
}
