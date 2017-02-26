package com.workspace.model;

import lombok.Data;

@Data
public class Annotation {

    private String type;
    private double version;
    private String color;
    private String title;
    private String text;
    private Actor actor;
}
