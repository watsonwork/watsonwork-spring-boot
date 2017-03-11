package com.ibm.watsonwork.model;

import lombok.Data;

@Data
public class FileShareResponse {

    private String created;
    private String createdBy;
    private String id;
    private String name;
    private int size;
}
