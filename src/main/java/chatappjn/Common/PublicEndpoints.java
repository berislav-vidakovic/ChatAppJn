package chatappjn.Common;

import java.util.List;

public final class PublicEndpoints {

    private PublicEndpoints() {} // prevent instantiation

    // List of public endpoints
    public static final List<String> ENDPOINTS = List.of(
        "/api/ping",
        "/api/pingdb",
        "/api/users/register",
        "/api/users/all",
        "/api/auth/refresh",
        "/api/auth/login",
        "/api/auth/logout",
        "/websocket"
    );
}
