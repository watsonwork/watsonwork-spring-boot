package com.workspace.model;

import java.util.List;

import lombok.Data;

@Data
public class Message {

    private String id;
    private String content;
    private String type;
    private double version;
    private List<Annotation> annotations;
}
