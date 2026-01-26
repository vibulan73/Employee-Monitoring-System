package com.monitoring.dto;

import com.monitoring.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String userId;
    private String firstName;
    private String lastName;
    private String jobRole;
    private String phoneNumber;

    public static AuthResponse from(User user) {
        return new AuthResponse(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getJobRole(),
                user.getPhoneNumber());
    }
}
