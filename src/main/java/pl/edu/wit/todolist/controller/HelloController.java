package pl.edu.wit.todolist.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

    @RestController
    @RequestMapping("api/v1/poctaserwis/test")
    public class HelloController {

        @Operation(summary = "Returns greeting for authenticated user")
        @ApiResponse(
                responseCode = "200",
                description = "Successful greeting response",
                content = @Content(
                        mediaType = "text/plain",
                        examples = @ExampleObject(value = "Welcome in Pocta Serwis, user!")
                )
        )
        @ApiResponse(
                responseCode = "401",
                description = "Unauthorized – missing or invalid Bearer token",
                content = @Content()
        )
        @GetMapping(value = "hello", produces = "text/plain")
        @ResponseStatus(HttpStatus.OK)
        public String hello(final Authentication auth) {
            return "Welcome in Pocta Serwis, " + auth.getName() + "!";
        }
    }

