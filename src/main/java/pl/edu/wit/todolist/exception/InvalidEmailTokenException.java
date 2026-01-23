package pl.edu.wit.todolist.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidEmailTokenException extends RuntimeException {
    public InvalidEmailTokenException(String message) { super(message); }
}
