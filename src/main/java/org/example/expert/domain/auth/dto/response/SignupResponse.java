package org.example.expert.domain.auth.dto.response;

import lombok.Getter;
import org.example.expert.domain.user.entity.User;

@Getter
public class SignupResponse {

    private final Long id;
    private final String email;

    public SignupResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
    }
}
