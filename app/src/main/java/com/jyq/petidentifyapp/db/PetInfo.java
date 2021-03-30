package com.jyq.petidentifyapp.db;

import java.io.Serializable;
import java.util.Date;

/**
 *宠物信息类
 *宠物照片、编号、昵称、品种、性别、是否绝育、生日、状态（正常/走失）、主人、联系方式、
 * 注册地址、行动轨迹、日常照片、介绍、注册时间、更新时间
 */


public class PetInfo implements Serializable {
    private String petPicPath;
    private String petID;
    private String petName;
    private String petType;
    private String petSex;
    private String petSterilization;
    private Date petBirth;
    private String petState;
    private String petOwner;
    private String petOwnerPhone;
    private String petRegistLocation;
    private String petHistLocation;
    private String petDailyPicPath;
    private String petInfo;
    private Date petRegistTime;
    private Date petUpdateTime;


    public PetInfo(){};

    public PetInfo(String petPicPath, String petID, String petName, String petType, String petSex, String petSterilization, Date petBirth, String petState, String petOwner, String petOwnerPhone, String petRegistLocation, String petHistLocation, String petDailyPicPath, String petInfo, Date petRegistTime, Date petUpdateTime) {
        this.petPicPath = petPicPath;
        this.petID = petID;
        this.petName = petName;
        this.petType = petType;
        this.petSex = petSex;
        this.petSterilization = petSterilization;
        this.petBirth = petBirth;
        this.petState = petState;
        this.petOwner = petOwner;
        this.petOwnerPhone = petOwnerPhone;
        this.petRegistLocation = petRegistLocation;
        this.petHistLocation = petHistLocation;
        this.petDailyPicPath = petDailyPicPath;
        this.petInfo = petInfo;
        this.petRegistTime = petRegistTime;
        this.petUpdateTime = petUpdateTime;
    }

    public String getPetSterilization() {
        return petSterilization;
    }

    public void setPetSterilization(String petSterilization) {
        this.petSterilization = petSterilization;
    }

    public String getPetState() {
        return petState;
    }

    public void setPetState(String petState) {
        this.petState = petState;
    }

    public String getPetOwner() {
        return petOwner;
    }

    public void setPetOwner(String petOwner) {
        this.petOwner = petOwner;
    }

    public String getPetOwnerPhone() {
        return petOwnerPhone;
    }

    public void setPetOwnerPhone(String petOwnerPhone) {
        this.petOwnerPhone = petOwnerPhone;
    }

    public String getPetDailyPicPath() {
        return petDailyPicPath;
    }

    public void setPetDailyPicPath(String petDailyPicPath) {
        this.petDailyPicPath = petDailyPicPath;
    }

    public String getPetID() {
        return petID;
    }

    public void setPetID(String petID) {
        this.petID = petID;
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

    public String getPetRegistLocation() {
        return petRegistLocation;
    }

    public void setPetRegistLocation(String petRegistLocation) {
        this.petRegistLocation = petRegistLocation;
    }

    public String getPetHistLocation() {
        return petHistLocation;
    }

    public void setPetHistLocation(String petHistLocation) {
        this.petHistLocation = petHistLocation;
    }

    @Override
    public String toString() {
        return "PetInfo{" +
                "petPicPath='" + petPicPath + '\'' +
                ", petID='" + petID + '\'' +
                ", petName='" + petName + '\'' +
                ", petType='" + petType + '\'' +
                ", petSex='" + petSex + '\'' +
                ", petSterilization='" + petSterilization + '\'' +
                ", petBirth=" + petBirth +
                ", petState='" + petState + '\'' +
                ", petOwner='" + petOwner + '\'' +
                ", petOwnerPhone='" + petOwnerPhone + '\'' +
                ", petRegistLocation='" + petRegistLocation + '\'' +
                ", petHistLocation='" + petHistLocation + '\'' +
                ", petDailyPicPath='" + petDailyPicPath + '\'' +
                ", petInfo='" + petInfo + '\'' +
                ", petRegistTime=" + petRegistTime +
                ", petUpdateTime=" + petUpdateTime +
                '}';
    }
}
