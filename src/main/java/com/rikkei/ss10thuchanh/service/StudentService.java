package com.rikkei.ss10thuchanh.service;

import com.rikkei.ss10thuchanh.dto.request.StudentRequest;
import com.rikkei.ss10thuchanh.dto.response.StudentResponse;
import com.rikkei.ss10thuchanh.entity.Student;
import org.springframework.data.domain.Page;

public interface StudentService {

    /**
     * Tạo mới sinh viên
     */
    StudentResponse create(StudentRequest request);

    /**
     * Lấy thông tin sinh viên theo ID
     */
    StudentResponse getById(Long id);

    /**
     * Lấy thông tin sinh viên theo mã sinh viên
     */
    StudentResponse getByStudentCode(String studentCode);

    /**
     * Cập nhật thông tin sinh viên
     */
    StudentResponse update(Long id, StudentRequest request);

    /**
     * Xóa sinh viên theo ID
     */
    void delete(Long id);

    /**
     * Tìm kiếm, lọc, phân trang, sắp xếp sinh viên
     *
     * @param keyword   từ khóa tìm kiếm (theo mã SV hoặc tên)
     * @param gender    lọc theo giới tính
     * @param page      trang hiện tại (bắt đầu từ 0)
     * @param size      số lượng bản ghi mỗi trang
     * @param sortBy    trường sắp xếp (studentCode, fullName, gpa, dateOfBirth)
     * @param sortDir   chiều sắp xếp (asc / desc)
     */
    Page<StudentResponse> search(
            String keyword,
            Student.Gender gender,
            int page,
            int size,
            String sortBy,
            String sortDir
    );
}
