package models;

import lombok.Data;
@Data
public class Content {
    private String gId;
    private String name;
    private String priceCny;
    private String thumbPic;
    private String issue;//发行
    private String circulation;//流通
    private String contract;//合约地址
    private String chain;//链上标识
    private String creater;//创建者
    private String createrName;
    private String owner;//拥有者
    private String ownerName;

    private String introduction;//数字藏品介绍

    private String gDesc;//商品描述

    //private List<Trade> tradeList;
}
