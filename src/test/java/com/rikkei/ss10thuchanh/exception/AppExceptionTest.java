package com.rikkei.ss10thuchanh.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests cho AppException – kiểm tra các factory method
 * và thuộc tính HTTP status trả về đúng.
 */
@DisplayName("AppException Tests")
class AppExceptionTest {

    @Test
    @DisplayName("notFound() → status 404, message đúng")
    void notFound_createsExceptionWithCorrectStatus() {
        AppException ex = AppException.notFound("Không tìm thấy bản ghi");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(ex.getMessage()).isEqualTo("Không tìm thấy bản ghi");
    }

    @Test
    @DisplayName("conflict() → status 409, message đúng")
    void conflict_createsExceptionWithCorrectStatus() {
        AppException ex = AppException.conflict("Dữ liệu đã tồn tại");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(ex.getMessage()).isEqualTo("Dữ liệu đã tồn tại");
    }

    @Test
    @DisplayName("badRequest() → status 400, message đúng")
    void badRequest_createsExceptionWithCorrectStatus() {
        AppException ex = AppException.badRequest("Yêu cầu không hợp lệ");

        assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(ex.getMessage()).isEqualTo("Yêu cầu không hợp lệ");
    }

    @Test
    @DisplayName("AppException là RuntimeException")
    void appException_isRuntimeException() {
        AppException ex = AppException.notFound("test");

        assertThat(ex).isInstanceOf(RuntimeException.class);
    }
}
