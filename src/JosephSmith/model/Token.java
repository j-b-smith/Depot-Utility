package JosephSmith.model;

import java.time.LocalDateTime;

public class Token {

    private String bearerToken;
    private LocalDateTime tokenExpiration;


    public Token(String bearerToken, LocalDateTime tokenExpiration){
        this.bearerToken = bearerToken;
        this.tokenExpiration = tokenExpiration;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public LocalDateTime getTokenExpiration() {
        return tokenExpiration;
    }

    public void setBearerToken(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public void setTokenExpiration(LocalDateTime tokenExpiration) {
        this.tokenExpiration = tokenExpiration;
    }
}
