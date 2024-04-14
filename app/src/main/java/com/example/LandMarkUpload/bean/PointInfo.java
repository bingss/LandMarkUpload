package com.example.LandMarkUpload.bean;

import java.io.Serializable;
import java.util.List;

public class PointInfo implements Serializable {
    private Integer Num;
    private List<String> imgPath;

    public PointInfo(int num,List<String> path){
        Num = num;
        imgPath = path;
    }

    public Integer getNum() {
        return Num;
    }

    public List<String> getimgPath() {
        return imgPath;
    }

    public void setNum(Integer num) {
        Num = num;
    }

    public void setimgPath(List<String> imgPath) {
        this.imgPath = imgPath;
    }
}
