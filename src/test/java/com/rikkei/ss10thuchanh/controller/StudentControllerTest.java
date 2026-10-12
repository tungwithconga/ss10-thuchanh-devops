package com.rikkei.ss10thuchanh.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rikkei.ss10thuchanh.dto.request.StudentRequest;
import com.rikkei.ss10thuchanh.dto.response.StudentResponse;
import com.rikkei.ss10thuchanh.entity.Student;
import com.rikkei.ss10thuchanh.exception.AppException;
import com.rikkei.ss10thuchanh.exception.GlobalExceptionHandler;
import com.rikkei.ss10thuchanh.service.StudentService;
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
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests cho StudentController.
 * Dùng MockMvc standalone (không load Spring context) để kiểm tra HTTP layer.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentController Tests")
class StudentControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController studentController;

    private StudentResponse sampleResponse;
    private StudentRequest sampleRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Truyền objectMapper có JavaTimeModule vào MockMvc để serialize LocalDate đúng
        MappingJackson2HttpMessageConverter converter =
                new MappingJackson2HttpMessageConverter(objectMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(studentController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();

        sampleResponse = StudentResponse.builder()
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
    //  POST /api/v1/students
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("POST /api/v1/students")
    class CreateEndpointTests {

        @Test
        @DisplayName("Tạo sinh viên hợp lệ → 201 Created với dữ liệu trả về")
        void create_validRequest_returns201() throws Exception {
            when(studentService.create(any(StudentRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/v1/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.data.studentCode").value("SV001"))
                    .andExpect(jsonPath("$.data.fullName").value("Nguyen Van A"))
                    .andExpect(jsonPath("$.data.gpa").value(3.5));
        }

        @Test
        @DisplayName("Tạo sinh viên với studentCode rỗng → 400 Bad Request")
        void create_missingStudentCode_returns400() throws Exception {
            StudentRequest invalidRequest = StudentRequest.builder()
                    .studentCode("")       // vi phạm @NotBlank
                    .fullName("Nguyen Van A")
                    .gpa(3.5)
                    .build();

            mockMvc.perform(post("/api/v1/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Tạo sinh viên với mã trùng → 409 Conflict")
        void create_duplicateCode_returns409() throws Exception {
            when(studentService.create(any(StudentRequest.class)))
                    .thenThrow(AppException.conflict("Mã sinh viên 'SV001' đã tồn tại"));

            mockMvc.perform(post("/api/v1/students")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(containsString("SV001")));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  GET /api/v1/students/{id}
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("GET /api/v1/students/{id}")
    class GetByIdEndpointTests {

        @Test
        @DisplayName("Lấy sinh viên tồn tại → 200 OK với dữ liệu")
        void getById_exists_returns200() throws Exception {
            when(studentService.getById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/students/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.studentCode").value("SV001"));
        }

        @Test
        @DisplayName("Lấy sinh viên không tồn tại → 404 Not Found")
        void getById_notExists_returns404() throws Exception {
            when(studentService.getById(99L))
                    .thenThrow(AppException.notFound("Không tìm thấy sinh viên với ID: 99"));

            mockMvc.perform(get("/api/v1/students/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  GET /api/v1/students/code/{studentCode}
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("GET /api/v1/students/code/{studentCode}")
    class GetByCodeEndpointTests {

        @Test
        @DisplayName("Lấy sinh viên theo mã tồn tại → 200 OK")
        void getByStudentCode_exists_returns200() throws Exception {
            when(studentService.getByStudentCode("SV001")).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/students/code/SV001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.studentCode").value("SV001"));
        }

        @Test
        @DisplayName("Lấy sinh viên theo mã không tồn tại → 404 Not Found")
        void getByStudentCode_notExists_returns404() throws Exception {
            when(studentService.getByStudentCode("UNKNOWN"))
                    .thenThrow(AppException.notFound("Không tìm thấy sinh viên với mã: UNKNOWN"));

            mockMvc.perform(get("/api/v1/students/code/UNKNOWN"))
                    .andExpect(status().isNotFound());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  GET /api/v1/students
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("GET /api/v1/students (search)")
    class SearchEndpointTests {

        @Test
        @DisplayName("Tìm kiếm không có filter → 200 OK")
        void search_noFilter_returns200() throws Exception {
            Page<StudentResponse> pageResult = new PageImpl<>(
                    List.of(sampleResponse),
                    org.springframework.data.domain.PageRequest.of(0, 10),
                    1
            );
            when(studentService.search(any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                    .thenReturn(pageResult);

            mockMvc.perform(get("/api/v1/students"))
                    .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("Tìm kiếm với keyword và gender → 200 OK")
        void search_withKeywordAndGender_returns200() throws Exception {
            Page<StudentResponse> pageResult = new PageImpl<>(
                    List.of(sampleResponse),
                    org.springframework.data.domain.PageRequest.of(0, 5),
                    1
            );
            when(studentService.search(eq("nguyen"), eq(Student.Gender.MALE),
                    anyInt(), anyInt(), anyString(), anyString()))
                    .thenReturn(pageResult);

            mockMvc.perform(get("/api/v1/students")
                            .param("keyword", "nguyen")
                            .param("gender", "MALE")
                            .param("page", "0")
                            .param("size", "5")
                            .param("sortBy", "fullName")
                            .param("sortDir", "desc"))
                    .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(200));
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  PUT /api/v1/students/{id}
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("PUT /api/v1/students/{id}")
    class UpdateEndpointTests {

        @Test
        @DisplayName("Cập nhật sinh viên hợp lệ → 200 OK")
        void update_validRequest_returns200() throws Exception {
            StudentResponse updatedResponse = StudentResponse.builder()
                    .id(1L)
                    .studentCode("SV001")
                    .fullName("Nguyen Van B")
                    .gpa(3.8)
                    .build();

            when(studentService.update(eq(1L), any(StudentRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(put("/api/v1/students/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.fullName").value("Nguyen Van B"));
        }

        @Test
        @DisplayName("Cập nhật sinh viên không tồn tại → 404 Not Found")
        void update_notFound_returns404() throws Exception {
            when(studentService.update(eq(99L), any(StudentRequest.class)))
                    .thenThrow(AppException.notFound("Không tìm thấy sinh viên với ID: 99"));

            mockMvc.perform(put("/api/v1/students/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sampleRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  DELETE /api/v1/students/{id}
    // ═══════════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("DELETE /api/v1/students/{id}")
    class DeleteEndpointTests {

        @Test
        @DisplayName("Xóa sinh viên tồn tại → 200 OK")
        void delete_exists_returns200() throws Exception {
            doNothing().when(studentService).delete(1L);

            mockMvc.perform(delete("/api/v1/students/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(containsString("Xóa sinh viên thành công")));
        }

        @Test
        @DisplayName("Xóa sinh viên không tồn tại → 404 Not Found")
        void delete_notFound_returns404() throws Exception {
            doThrow(AppException.notFound("Không tìm thấy sinh viên với ID: 99"))
                    .when(studentService).delete(99L);

            mockMvc.perform(delete("/api/v1/students/99"))
                    .andExpect(status().isNotFound());
        }
    }
}
