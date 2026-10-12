package com.rikkei.ss10thuchanh.dto.response;

import com.rikkei.ss10thuchanh.entity.Student;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentResponse {

    private Long id;
    private String studentCode;
    private String fullName;
    private LocalDate dateOfBirth;
    private Student.Gender gender;
    private String phoneNumber;
    private Double gpa;

    public static StudentResponse fromEntity(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .studentCode(student.getStudentCode())
                .fullName(student.getFullName())
                .dateOfBirth(student.getDateOfBirth())
                .gender(student.getGender())
                .phoneNumber(student.getPhoneNumber())
                .gpa(student.getGpa())
                .build();
    }
}
