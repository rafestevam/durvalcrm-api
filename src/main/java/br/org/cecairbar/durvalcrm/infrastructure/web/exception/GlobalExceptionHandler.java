package br.org.cecairbar.durvalcrm.infrastructure.web.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.stream.Collectors;

@Provider
@Slf4j
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        log.error("Erro capturado: ", exception);

        if (exception instanceof NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of(
                            "error", "Not Found",
                            "message", exception.getMessage(),
                            "status", 404
                    ))
                    .build();
        }

        if (exception instanceof WebApplicationException webEx) {
            return Response.status(webEx.getResponse().getStatus())
                    .entity(Map.of(
                            "error", "Application Error",
                            "message", webEx.getMessage(),
                            "status", webEx.getResponse().getStatus()
                    ))
                    .build();
        }

        if (exception instanceof ConstraintViolationException validationEx) {
            String violations = validationEx.getConstraintViolations()
                    .stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "error", "Validation Error",
                            "message", violations,
                            "status", 400
                    ))
                    .build();
        }

        // Erro genérico
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(Map.of(
                        "error", "Internal Server Error",
                        "message", "Erro interno do servidor",
                        "status", 500
                ))
                .build();
    }
}