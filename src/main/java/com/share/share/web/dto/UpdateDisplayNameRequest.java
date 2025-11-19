package com.share.share.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateDisplayNameRequest {
    
    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 1, max = 60, message = "닉네임은 1자 이상 60자 이하여야 합니다.")
    private String displayName;
}

