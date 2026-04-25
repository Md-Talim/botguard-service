package me.mdtalim.botguard.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBotRequest {

    @NotBlank
    private String name;

    private String personaDescription;
}
