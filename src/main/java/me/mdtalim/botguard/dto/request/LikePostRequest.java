package me.mdtalim.botguard.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LikePostRequest {

    @NotNull
    private Long userId;
}
