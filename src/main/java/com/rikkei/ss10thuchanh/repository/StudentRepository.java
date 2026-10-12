package com.rikkei.ss10thuchanh.repository;

import com.rikkei.ss10thuchanh.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentCode(String studentCode);

    boolean existsByStudentCodeAndIdNot(String studentCode, Long id);

    Optional<Student> findByStudentCode(String studentCode);

    /**
     * Tìm kiếm sinh viên theo từ khóa (mã SV, tên) và lọc theo giới tính
     * Hỗ trợ phân trang và sắp xếp qua Pageable
     */
    @Query("SELECT s FROM Student s WHERE " +
           "(:keyword IS NULL OR LOWER(s.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "   OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:gender IS NULL OR s.gender = :gender)")
    Page<Student> searchStudents(
            @Param("keyword") String keyword,
            @Param("gender") Student.Gender gender,
            Pageable pageable
    );
}
