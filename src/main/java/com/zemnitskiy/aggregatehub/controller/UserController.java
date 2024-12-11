package com.zemnitskiy.aggregatehub.controller;

import com.zemnitskiy.aggregatehub.exception.ErrorResponse;
import com.zemnitskiy.aggregatehub.model.User;
import com.zemnitskiy.aggregatehub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing users across multiple databases.
 * Provides endpoints to retrieve users.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userAggregationService;

    public UserController(UserService userAggregationService) {
        this.userAggregationService = userAggregationService;
    }

    /**
     * Retrieves all users from all connected databases, with optional filtering parameters.
     *
     * @param id       the ID of the user to filter by (optional)
     * @param name     the name of the user to filter by (optional)
     * @param surname  the surname of the user to filter by (optional)
     * @param username the username of the user to filter by (optional)
     * @return a list of users matching the provided criteria
     */
    @Operation(
            summary = "Retrieve all users aggregated from multiple databases",
            description = "Fetches a list of users from all configured databases. Supports optional filtering by id, name, surname, and username."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved list of users",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = User.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request due to invalid parameters",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error while retrieving users",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
            @Parameter(description = "ID of the user to filter by", example = "1")
            @RequestParam(required = false) String id,

            @Parameter(description = "Name of the user to filter by", example = "John")
            @RequestParam(required = false) String name,

            @Parameter(description = "Surname of the user to filter by", example = "Doe")
            @RequestParam(required = false) String surname,

            @Parameter(description = "Username of the user to filter by", example = "johndoe")
            @RequestParam(required = false) String username
    ) {
        logger.info("Received request to retrieve users with filters - id: {}, name: {}, surname: {}, username: {}", id, name, surname, username);
        List<User> users = userAggregationService.getAllUsersFromAllDatabases(id, name, surname, username);
        logger.info("Successfully retrieved {} users", users.size());
        return ResponseEntity.ok(users);
    }
}