package ec.huertos.web;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice
public class ErrorHandler {
  @ExceptionHandler({
    IllegalArgumentException.class,
    java.util.NoSuchElementException.class,
    org.springframework.web.multipart.MaxUploadSizeExceededException.class,
    org.springframework.dao.DataIntegrityViolationException.class,
    org.springframework.web.bind.MissingServletRequestParameterException.class,
    org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
  })
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String invalid(Exception e, Model m) {
    m.addAttribute(
        "message",
        e instanceof IllegalArgumentException
            ? e.getMessage()
            : "La operación no pudo completarse. Revisa los datos o recarga la página.");
    return "error";
  }

  @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public String denied(Exception e, Model m) {
    m.addAttribute("message", e.getMessage());
    return "error";
  }
}
