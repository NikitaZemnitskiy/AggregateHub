package com.zemnitskiy.aggregatehub.controller;

import com.zemnitskiy.aggregatehub.model.User;
import com.zemnitskiy.aggregatehub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userAggregationService;

    @Autowired
    public UserController(UserService userAggregationService) { this.userAggregationService = userAggregationService; }

    @Operation(summary = "Get all users aggregated from multiple databases",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    @GetMapping
    public List<User> getAllUsers(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String surname,
            @RequestParam(required = false) String username
    ) {
        return userAggregationService.getAllUsersFromAllDatabases(id, name, surname, username);
    }

    @PostMapping("/save")
    public ResponseEntity<User> saveUser(@RequestBody User user) {
        try {
         userAggregationService.saveUserToAllDatabases(user);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
