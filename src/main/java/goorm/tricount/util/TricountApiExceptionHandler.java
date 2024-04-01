package goorm.tricount.util;

import goorm.tricount.enums.TricountApiErrorCode;
import goorm.tricount.model.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class TricountApiExceptionHandler {

    // 커스텀 예외 하나씩 정의해서 추가
    @ExceptionHandler(TricountApiException.class)
    public ApiResponse<Object> tricountApiExceptionHandler(TricountApiException e, HttpServletResponse response) {
        TricountApiErrorCode errorCode = e.getErrorCode();
        response.setStatus(errorCode.getStatus());

        return new ApiResponse<>().fail(errorCode.getCode(), e.getMessage());
    }

}
