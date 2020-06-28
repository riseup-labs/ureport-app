package io.rapidpro.surveyor.extend.entity.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ureport")
public class UReportLocal {
    @PrimaryKey(autoGenerate = true)
    public int primaryKey;
    int id;
    int ureport_id;
    String flow_id;
    String data_pack;
    String my_pack;
    String en_pack;
    String bn_pack;
    String data_category;
    String my_category;
    String en_category;
    String created_at;
    String updated_at;

    public UReportLocal(int primaryKey, int id, int ureport_id, String flow_id, String data_pack, String my_pack, String en_pack, String bn_pack, String data_category, String my_category, String en_category, String created_at, String updated_at) {
        this.primaryKey = primaryKey;
        this.id = id;
        this.ureport_id = ureport_id;
        this.flow_id = flow_id;
        this.data_pack = data_pack;
        this.my_pack = my_pack;
        this.en_pack = en_pack;
        this.bn_pack = bn_pack;
        this.data_category = data_category;
        this.my_category = my_category;
        this.en_category = en_category;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(int primaryKey) {
        this.primaryKey = primaryKey;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUreport_id() {
        return ureport_id;
    }

    public void setUreport_id(int ureport_id) {
        this.ureport_id = ureport_id;
    }

    public String getFlow_id() {
        return flow_id;
    }

    public void setFlow_id(String flow_id) {
        this.flow_id = flow_id;
    }

    public String getData_pack() {
        return data_pack;
    }

    public void setData_pack(String data_pack) {
        this.data_pack = data_pack;
    }

    public String getMy_pack() {
        return my_pack;
    }

    public void setMy_pack(String my_pack) {
        this.my_pack = my_pack;
    }

    public String getEn_pack() {
        return en_pack;
    }

    public void setEn_pack(String en_pack) {
        this.en_pack = en_pack;
    }

    public String getBn_pack() {
        return bn_pack;
    }

    public void setBn_pack(String bn_pack) {
        this.bn_pack = bn_pack;
    }

    public String getData_category() {
        return data_category;
    }

    public void setData_category(String data_category) {
        this.data_category = data_category;
    }

    public String getMy_category() {
        return my_category;
    }

    public void setMy_category(String my_category) {
        this.my_category = my_category;
    }

    public String getEn_category() {
        return en_category;
    }

    public void setEn_category(String en_category) {
        this.en_category = en_category;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
}
