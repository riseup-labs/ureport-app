package io.rapidpro.surveyor.extend.entity.model;

public class ureport {

    int id;
    int ureport_id;
    String flow_id;
    String data_pack;
    String my_pack;
    String en_pack;
    String data_category;
    String my_category;
    String en_category;
    String created_at;
    String updated_at;

    public ureport(int id, int ureport_id, String flow_id, String data_pack, String my_pack, String en_pack, String data_category, String my_category, String en_category, String created_at, String updated_at) {
        this.id = id;
        this.ureport_id = ureport_id;
        this.flow_id = flow_id;
        this.data_pack = data_pack;
        this.my_pack = my_pack;
        this.en_pack = en_pack;
        this.data_category = data_category;
        this.my_category = my_category;
        this.en_category = en_category;
        this.created_at = created_at;
        this.updated_at = updated_at;
    }

    public int getId() {
        return id;
    }

    public ureport setId(int id) {
        this.id = id;
        return this;
    }

    public int getUreport_id() {
        return ureport_id;
    }

    public ureport setUreport_id(int ureport_id) {
        this.ureport_id = ureport_id;
        return this;
    }

    public String getFlow_id() {
        return flow_id;
    }

    public ureport setFlow_id(String flow_id) {
        this.flow_id = flow_id;
        return this;
    }

    public String getData_pack() {
        return data_pack;
    }

    public ureport setData_pack(String data_pack) {
        this.data_pack = data_pack;
        return this;
    }

    public String getMy_pack() {
        return my_pack;
    }

    public ureport setMy_pack(String my_pack) {
        this.my_pack = my_pack;
        return this;
    }

    public String getEn_pack() {
        return en_pack;
    }

    public ureport setEn_pack(String en_pack) {
        this.en_pack = en_pack;
        return this;
    }

    public String getData_category() {
        return data_category;
    }

    public ureport setData_category(String data_category) {
        this.data_category = data_category;
        return this;
    }

    public String getMy_category() {
        return my_category;
    }

    public ureport setMy_category(String my_category) {
        this.my_category = my_category;
        return this;
    }

    public String getEn_category() {
        return en_category;
    }

    public ureport setEn_category(String en_category) {
        this.en_category = en_category;
        return this;
    }

    public String getCreated_at() {
        return created_at;
    }

    public ureport setCreated_at(String created_at) {
        this.created_at = created_at;
        return this;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public ureport setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
        return this;
    }
}
