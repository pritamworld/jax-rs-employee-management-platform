package com.company.employee.auth;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.Map;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {
    public static class RegisterRequest { public String username; public String email; public String password; }
    public static class LoginRequest { public String username; public String password; }
    private final AuthService auth;
    @Inject public AuthResource(AuthService auth){this.auth=auth;}
    @POST @Path("/register") public Response register(RegisterRequest r){
        if(r==null)throw new IllegalArgumentException("Request body is required");
        return Response.status(201).entity(auth.register(r.username,r.email,r.password)).build();
    }
    @POST @Path("/login") public Map<String,Object> login(LoginRequest r){
        if(r==null)throw new IllegalArgumentException("Request body is required");
        return auth.login(r.username,r.password);
    }
    @GET @Path("/health") public Map<String,String> health(){return Map.of("status","UP");}
}
