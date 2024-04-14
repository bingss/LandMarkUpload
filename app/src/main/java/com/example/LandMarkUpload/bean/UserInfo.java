package com.example.LandMarkUpload.bean;

import com.google.gson.annotations.SerializedName;

public class UserInfo {


    @SerializedName("ObjID")
    private Integer objID;
    @SerializedName("帳號")
    private String account;
    @SerializedName("密碼")
    private String password;
    @SerializedName("姓名")
    private String name;
    @SerializedName("機關")
    private String office;
    @SerializedName("權限")
    private String authorization;
    @SerializedName("信箱")
    private String email;
    @SerializedName("最後登入時間")
    private String lastlogin;

    public Integer getObjID() {
        return objID;
    }

    public void setObjID(Integer objID) {
        this.objID = objID;
    }

    public String getAccount() {
        return account;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getOffice() {
        return office;
    }

    public String getAuthorization() {
        return authorization;
    }

    public String getEmail() {
        return email;
    }

    public String getLastlogin() {
        return lastlogin;
    }

}
