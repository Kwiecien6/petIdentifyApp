package com.jyq.petidentifyapp.db;


/**
 *宠物信息类
 *宠物昵称、品种（猫/狗）、性别、年龄、照片
 */


public class PetInfo {
    private String petName;
    private String petType;
    private String petSex;
    private int petAge;
    private String petPicPath;

    public PetInfo(){};

    public PetInfo(String petName, String petType, String petSex, int petAge, String petPicPath){
        this.petName = petName;
        this.petType = petType;
        this.petSex = petSex;
        this.petAge = petAge;
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

    public int getPetAge() {
        return petAge;
    }

    public void setPetAge(int petAge) {
        this.petAge = petAge;
    }

    public String getPetPicPath() {
        return petPicPath;
    }

    public void setPetPicPath(String petPicPath) {
        this.petPicPath = petPicPath;
    }


    @Override
    public String toString(){
        return "PetInfo{" +
                "name='" + petName + '\'' +
                ", type='" + petType + '\'' +
                ", sex='" + petSex + '\'' +
                ", age=" + petAge +
                ", path='" + petPicPath + '\'' +
                '}';
    }
}
