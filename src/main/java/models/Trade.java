package models;

import lombok.Data;

@Data
public class Trade {
    private String gId;
    private String userId;
    private String userName;
    private String remark;
    private String operationTime;

}
