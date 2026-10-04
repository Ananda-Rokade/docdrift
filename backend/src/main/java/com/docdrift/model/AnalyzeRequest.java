package com.docdrift.model;

import jakarta.validation.constraints.NotBlank;

public class AnalyzeRequest {
    @NotBlank public String repositoryUrl;
}
