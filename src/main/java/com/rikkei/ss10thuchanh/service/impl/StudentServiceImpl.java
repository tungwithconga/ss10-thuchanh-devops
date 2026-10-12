package com.rikkei.ss10thuchanh.service.impl;

import com.rikkei.ss10thuchanh.dto.request.StudentRequest;
import com.rikkei.ss10thuchanh.dto.response.StudentResponse;
import com.rikkei.ss10thuchanh.entity.Student;
import com.rikkei.ss10thuchanh.exception.AppException;
import com.rikkei.ss10thuchanh.repository.StudentRepository;
import com.rikkei.ss10thuchanh.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private static final List<String> ALLOWED_SORT_FIELDS =
            List.of("id", "studentCode", "fullName", "dateOfBirth", "gpa", "gender");

    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public StudentResponse create(StudentRequest request) {
        if (studentRepository.existsByStudentCode(request.getStudentCode())) {
            throw AppException.conflict("Mã sinh viên '" + request.getStudentCode() + "' đã tồn tại");
        }

        Student student = Student.builder()
                .studentCode(request.getStudentCode())
                .fullName(request.getFullName())
                .dateOfBirth(request.getDateOfBirth())
                .gender(request.getGender())
                .phoneNumber(request.getPhoneNumber())
                .gpa(request.getGpa())
                .build();

        Student saved = studentRepository.save(student);
        return StudentResponse.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getById(Long id) {
        Student student = findStudentById(id);
        return StudentResponse.fromEntity(student);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getByStudentCode(String studentCode) {
        Student student = studentRepository.findByStudentCode(studentCode)
                .orElseThrow(() -> AppException.notFound(
                        "Không tìm thấy sinh viên với mã: " + studentCode));
        return StudentResponse.fromEntity(student);
    }

    @Override
    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = findStudentById(id);

        // Kiểm tra mã SV trùng (ngoại trừ chính nó)
        if (studentRepository.existsByStudentCodeAndIdNot(request.getStudentCode(), id)) {
            throw AppException.conflict("Mã sinh viên '" + request.getStudentCode() + "' đã tồn tại");
        }

        student.setStudentCode(request.getStudentCode());
        student.setFullName(request.getFullName());
        student.setDateOfBirth(request.getDateOfBirth());
        student.setGender(request.getGender());
        student.setPhoneNumber(request.getPhoneNumber());
        student.setGpa(request.getGpa());

        Student updated = studentRepository.save(student);
        return StudentResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Student student = findStudentById(id);
        studentRepository.delete(student);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StudentResponse> search(
            String keyword,
            Student.Gender gender,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        // Validate sort field
        if (!ALLOWED_SORT_FIELDS.contains(sortBy)) {
            sortBy = "id";
        }

        // Build sort direction
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Normalize keyword
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        return studentRepository
                .searchStudents(normalizedKeyword, gender, pageable)
                .map(StudentResponse::fromEntity);
    }

    // ---- Helper ----

    private Student findStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> AppException.notFound("Không tìm thấy sinh viên với ID: " + id));
    }
}
