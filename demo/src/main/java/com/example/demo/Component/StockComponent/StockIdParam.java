package com.example.demo.Component.StockComponent;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class StockIdParam {
    
    @NotEmpty(message = "stock_id不可為空")
    @NotNull(message = "stock_id can not be null.")
    public String stock_id;
}
