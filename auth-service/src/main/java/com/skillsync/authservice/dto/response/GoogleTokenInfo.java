package com.skillsync.authservice.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoogleTokenInfo {
    private String aud;
    private String sub;
    private String email;

    @JsonProperty("email_verified")
    private String emailVerified;

    private String name;
    private String picture;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;
}
