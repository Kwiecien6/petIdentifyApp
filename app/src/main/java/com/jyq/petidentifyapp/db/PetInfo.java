package com.jyq.petidentifyapp.db;

import com.jyq.petidentifyapp.util.DateUtil;
import java.util.Date;

/**
 *宠物信息类
 *宠物昵称、品种（猫/狗）、性别、年龄、照片
 */


public class PetInfo {
    private String petName;
    private String petType;
    private String petSex;
    private Date petBirth;
    private String petInfo;
    private Date petRegistTime;
    private Date petUpdateTime;
    private String petPicPath;

    public PetInfo(){};

    public PetInfo(String petName, String petType, String petSex, Date petBirth, String petInfo, Date petRegistTime, Date petUpdateTime, String petPicPath) {
        this.petName = petName;
        this.petType = petType;
        this.petSex = petSex;
        this.petBirth = petBirth;
        this.petInfo = petInfo;
        this.petRegistTime = petRegistTime;
        this.petUpdateTime = petUpdateTime;
        this.petPicPath = petPicPath;
    }


    public String getPetName() {
        return petName;
    }

    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetType() {
        return petType;
    }

    public void setPetType(String petType) {
        this.petType = petType;
    }

    public String getPetSex() {
        return petSex;
    }

    public void setPetSex(String petSex) {
        this.petSex = petSex;
    }

    public String getPetPicPath() {
        return petPicPath;
    }

    public void setPetPicPath(String petPicPath) {
        this.petPicPath = petPicPath;
    }

    public Date getPetBirth() {
        return petBirth;
    }

    public void setPetBirth(Date petBirth) {
        this.petBirth = petBirth;
    }

    public String getPetInfo() {
        return petInfo;
    }

    public void setPetInfo(String petInfo) {
        this.petInfo = petInfo;
    }

    public Date getPetRegistTime() {
        return petRegistTime;
    }

    public void setPetRegistTime(Date petRegistTime) {
        this.petRegistTime = petRegistTime;
    }

    public Date getPetUpdateTime() {
        return petUpdateTime;
    }

    public void setPetUpdateTime(Date petUpdateTime) {
        this.petUpdateTime = petUpdateTime;
    }

    @Override
    public String toString() {
        return "PetInfo{" +
                "petName='" + petName + '\'' +
                ", petType='" + petType + '\'' +
                ", petSex='" + petSex + '\'' +
                ", petBirth=" + petBirth +
                ", petInfo='" + petInfo + '\'' +
                ", petRegistTime=" + petRegistTime +
                ", petUpdateTime=" + petUpdateTime +
                ", petPicPath='" + petPicPath + '\'' +
                '}';
    }
}
