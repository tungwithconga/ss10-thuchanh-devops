package com.rikkei.ss10thuchanh.controller;

import com.rikkei.ss10thuchanh.dto.request.StudentRequest;
import com.rikkei.ss10thuchanh.dto.response.ApiResponse;
import com.rikkei.ss10thuchanh.dto.response.StudentResponse;
import com.rikkei.ss10thuchanh.entity.Student;
import com.rikkei.ss10thuchanh.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // ===================== CREATE =====================

    /**
     * POST /api/v1/students
     * Tạo mới một sinh viên
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StudentResponse>> create(
            @Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Tạo sinh viên thành công", response));
    }

    // ===================== READ =====================

    /**
     * GET /api/v1/students/{id}
     * Lấy thông tin sinh viên theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> getById(@PathVariable Long id) {
        StudentResponse response = studentService.getById(id);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin sinh viên thành công", response));
    }

    /**
     * GET /api/v1/students/code/{studentCode}
     * Lấy thông tin sinh viên theo mã sinh viên
     */
    @GetMapping("/code/{studentCode}")
    public ResponseEntity<ApiResponse<StudentResponse>> getByStudentCode(
            @PathVariable String studentCode) {
        StudentResponse response = studentService.getByStudentCode(studentCode);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin sinh viên thành công", response));
    }

    /**
     * GET /api/v1/students
     * Tìm kiếm, lọc, phân trang, sắp xếp danh sách sinh viên
     *
     * Query params:
     *   - keyword   : từ khóa tìm kiếm theo mã SV hoặc tên (tùy chọn)
     *   - gender    : lọc theo giới tính MALE | FEMALE | OTHER (tùy chọn)
     *   - page      : số trang, bắt đầu từ 0 (mặc định: 0)
     *   - size      : số bản ghi mỗi trang (mặc định: 10)
     *   - sortBy    : trường sắp xếp id|studentCode|fullName|gpa|dateOfBirth|gender (mặc định: id)
     *   - sortDir   : chiều sắp xếp asc|desc (mặc định: asc)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<StudentResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Student.Gender gender,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Page<StudentResponse> result = studentService.search(keyword, gender, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách sinh viên thành công", result));
    }

    // ===================== UPDATE =====================

    /**
     * PUT /api/v1/students/{id}
     * Cập nhật toàn bộ thông tin sinh viên
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StudentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request) {
        StudentResponse response = studentService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sinh viên thành công", response));
    }

    // ===================== DELETE =====================

    /**
     * DELETE /api/v1/students/{id}
     * Xóa sinh viên theo ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sinh viên thành công", null));
    }
}
