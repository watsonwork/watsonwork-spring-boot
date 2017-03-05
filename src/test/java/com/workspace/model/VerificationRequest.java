package com.workspace.model;

import lombok.Data;

@Data
public class VerificationRequest {

    private final String type;
    private final String challenge;
}
