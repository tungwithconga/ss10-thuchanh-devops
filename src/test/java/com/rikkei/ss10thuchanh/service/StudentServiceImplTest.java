package com.rikkei.ss10thuchanh.service;

import com.rikkei.ss10thuchanh.dto.request.StudentRequest;
import com.rikkei.ss10thuchanh.dto.response.StudentResponse;
import com.rikkei.ss10thuchanh.entity.Student;
import com.rikkei.ss10thuchanh.exception.AppException;
import com.rikkei.ss10thuchanh.repository.StudentRepository;
import com.rikkei.ss10thuchanh.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho StudentServiceImpl.
 * Dùng Mockito để mock StudentRepository – không cần database thực.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentServiceImpl Tests")
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @InjectMocks
    private StudentServiceImpl studentService;

    // ─── Dữ liệu dùng chung ───────────────────────────────────────────────
    private Student sampleStudent;
    private StudentRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleStudent = Student.builder()
                .id(1L)
                .studentCode("SV001")
                .fullName("Nguyen Van A")
                .dateOfBirth(LocalDate.of(2000, 1, 15))
                .gender(Student.Gender.MALE)
                .phoneNumber("0901234567")
                .gpa(3.5)
                .build();

        sampleRequest = StudentRequest.builder()
                .studentCode("SV001")
                .fullName("Nguyen Van A")
                .dateOfBirth(LocalDate.of(2000, 1, 15))
                .gender(Student.Gender.MALE)
                .phoneNumber("0901234567")
                .gpa(3.5)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  CREATE
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("create()")
    class CreateTests {

        @Test
        @DisplayName("Tạo sinh viên mới thành công")
        void create_success() {
            when(studentRepository.existsByStudentCode("SV001")).thenReturn(false);
            when(studentRepository.save(any(Student.class))).thenReturn(sampleStudent);

            StudentResponse result = studentService.create(sampleRequest);

            assertThat(result).isNotNull();
            assertThat(result.getStudentCode()).isEqualTo("SV001");
            assertThat(result.getFullName()).isEqualTo("Nguyen Van A");
            assertThat(result.getGpa()).isEqualTo(3.5);

            verify(studentRepository).existsByStudentCode("SV001");
            verify(studentRepository).save(any(Student.class));
        }

        @Test
        @DisplayName("Tạo sinh viên với mã đã tồn tại → ném AppException CONFLICT")
        void create_duplicateCode_throwsConflict() {
            when(studentRepository.existsByStudentCode("SV001")).thenReturn(true);

            assertThatThrownBy(() -> studentService.create(sampleRequest))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> {
                        AppException appEx = (AppException) ex;
                        assertThat(appEx.getStatus()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(appEx.getMessage()).contains("SV001");
                    });

            verify(studentRepository, never()).save(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  GET BY ID
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getById()")
    class GetByIdTests {

        @Test
        @DisplayName("Lấy sinh viên theo ID tồn tại → trả về StudentResponse")
        void getById_found() {
            when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent));

            StudentResponse result = studentService.getById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getStudentCode()).isEqualTo("SV001");
        }

        @Test
        @DisplayName("Lấy sinh viên theo ID không tồn tại → ném AppException NOT_FOUND")
        void getById_notFound() {
            when(studentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.getById(99L))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getStatus())
                            .isEqualTo(HttpStatus.NOT_FOUND));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  GET BY STUDENT CODE
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getByStudentCode()")
    class GetByStudentCodeTests {

        @Test
        @DisplayName("Lấy sinh viên theo mã tồn tại → trả về StudentResponse")
        void getByStudentCode_found() {
            when(studentRepository.findByStudentCode("SV001"))
                    .thenReturn(Optional.of(sampleStudent));

            StudentResponse result = studentService.getByStudentCode("SV001");

            assertThat(result.getStudentCode()).isEqualTo("SV001");
            assertThat(result.getFullName()).isEqualTo("Nguyen Van A");
        }

        @Test
        @DisplayName("Lấy sinh viên theo mã không tồn tại → ném AppException NOT_FOUND")
        void getByStudentCode_notFound() {
            when(studentRepository.findByStudentCode("UNKNOWN")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.getByStudentCode("UNKNOWN"))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getStatus())
                            .isEqualTo(HttpStatus.NOT_FOUND));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  UPDATE
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("update()")
    class UpdateTests {

        @Test
        @DisplayName("Cập nhật sinh viên thành công")
        void update_success() {
            StudentRequest updateRequest = StudentRequest.builder()
                    .studentCode("SV001")
                    .fullName("Nguyen Van B")
                    .dateOfBirth(LocalDate.of(2000, 1, 15))
                    .gender(Student.Gender.MALE)
                    .phoneNumber("0901234567")
                    .gpa(3.8)
                    .build();

            Student updatedStudent = Student.builder()
                    .id(1L)
                    .studentCode("SV001")
                    .fullName("Nguyen Van B")
                    .dateOfBirth(LocalDate.of(2000, 1, 15))
                    .gender(Student.Gender.MALE)
                    .phoneNumber("0901234567")
                    .gpa(3.8)
                    .build();

            when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent));
            when(studentRepository.existsByStudentCodeAndIdNot("SV001", 1L)).thenReturn(false);
            when(studentRepository.save(any(Student.class))).thenReturn(updatedStudent);

            StudentResponse result = studentService.update(1L, updateRequest);

            assertThat(result.getFullName()).isEqualTo("Nguyen Van B");
            assertThat(result.getGpa()).isEqualTo(3.8);
        }

        @Test
        @DisplayName("Cập nhật với mã SV trùng bản ghi khác → ném AppException CONFLICT")
        void update_duplicateCode_throwsConflict() {
            when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent));
            when(studentRepository.existsByStudentCodeAndIdNot("SV001", 1L)).thenReturn(true);

            assertThatThrownBy(() -> studentService.update(1L, sampleRequest))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getStatus())
                            .isEqualTo(HttpStatus.CONFLICT));

            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("Cập nhật sinh viên không tồn tại → ném AppException NOT_FOUND")
        void update_notFound() {
            when(studentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.update(99L, sampleRequest))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getStatus())
                            .isEqualTo(HttpStatus.NOT_FOUND));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  DELETE
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("delete()")
    class DeleteTests {

        @Test
        @DisplayName("Xóa sinh viên tồn tại → gọi repository.delete")
        void delete_success() {
            when(studentRepository.findById(1L)).thenReturn(Optional.of(sampleStudent));
            doNothing().when(studentRepository).delete(sampleStudent);

            assertThatCode(() -> studentService.delete(1L)).doesNotThrowAnyException();

            verify(studentRepository).delete(sampleStudent);
        }

        @Test
        @DisplayName("Xóa sinh viên không tồn tại → ném AppException NOT_FOUND")
        void delete_notFound() {
            when(studentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.delete(99L))
                    .isInstanceOf(AppException.class)
                    .satisfies(ex -> assertThat(((AppException) ex).getStatus())
                            .isEqualTo(HttpStatus.NOT_FOUND));

            verify(studentRepository, never()).delete(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  SEARCH
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("search()")
    class SearchTests {

        @Test
        @DisplayName("Tìm kiếm với keyword hợp lệ → trả về Page<StudentResponse>")
        void search_withKeyword_returnsPage() {
            Page<Student> mockPage = new PageImpl<>(List.of(sampleStudent));
            when(studentRepository.searchStudents(eq("nguyen"), isNull(), any(Pageable.class)))
                    .thenReturn(mockPage);

            Page<StudentResponse> result = studentService.search(
                    "nguyen", null, 0, 10, "fullName", "asc");

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getFullName()).isEqualTo("Nguyen Van A");
        }

        @Test
        @DisplayName("Tìm kiếm với sortBy không hợp lệ → fallback về 'id'")
        void search_invalidSortBy_fallbackToId() {
            Page<Student> mockPage = new PageImpl<>(List.of());
            when(studentRepository.searchStudents(isNull(), isNull(), argThat(pageable ->
                    pageable.getSort().getOrderFor("id") != null)))
                    .thenReturn(mockPage);

            Page<StudentResponse> result = studentService.search(
                    null, null, 0, 10, "invalidField", "asc");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Tìm kiếm với keyword rỗng → chuẩn hóa thành null")
        void search_blankKeyword_normalizedToNull() {
            Page<Student> mockPage = new PageImpl<>(List.of(sampleStudent));
            when(studentRepository.searchStudents(isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(mockPage);

            Page<StudentResponse> result = studentService.search(
                    "   ", null, 0, 10, "id", "asc");

            assertThat(result.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Tìm kiếm với sortDir = 'desc' → sắp xếp giảm dần")
        void search_descSort_appliedCorrectly() {
            Page<Student> mockPage = new PageImpl<>(List.of(sampleStudent));
            when(studentRepository.searchStudents(isNull(), isNull(), argThat(pageable ->
                    pageable.getSort().getOrderFor("gpa") != null &&
                    pageable.getSort().getOrderFor("gpa").isDescending())))
                    .thenReturn(mockPage);

            Page<StudentResponse> result = studentService.search(
                    null, null, 0, 10, "gpa", "desc");

            assertThat(result).isNotNull();
        }
    }
}
