package io.rapidpro.surveyor.extend.entity.model;

import java.util.List;

import io.rapidpro.surveyor.extend.entity.local.StoriesLocal;

public class apk_version {

    int id;
    int version_code;
    String version_name;
    int status;
    int is_mandatory;
    String file_url;

    public apk_version(int id, int version_code, String version_name, int status, int is_mandatory, String file_url) {
        this.id = id;
        this.version_code = version_code;
        this.version_name = version_name;
        this.status = status;
        this.is_mandatory = is_mandatory;
        this.file_url = file_url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVersion_code() {
        return version_code;
    }

    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getIs_mandatory() {
        return is_mandatory;
    }

    public void setIs_mandatory(int is_mandatory) {
        this.is_mandatory = is_mandatory;
    }

    public String getFile_url() {
        return file_url;
    }

    public void setFile_url(String file_url) {
        this.file_url = file_url;
    }
}
