package com.rikkei.ss10thuchanh.dto.request;

import com.rikkei.ss10thuchanh.entity.Student;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {

    @NotBlank(message = "Mã sinh viên không được để trống")
    @Size(max = 20, message = "Mã sinh viên tối đa 20 ký tự")
    private String studentCode;

    @NotBlank(message = "Tên sinh viên không được để trống")
    @Size(max = 100, message = "Tên sinh viên tối đa 100 ký tự")
    private String fullName;

    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    private Student.Gender gender;

    @Pattern(regexp = "^(0|\\+84)[0-9]{9}$", message = "Số điện thoại không hợp lệ")
    private String phoneNumber;

    @DecimalMin(value = "0.0", message = "GPA phải >= 0.0")
    @DecimalMax(value = "4.0", message = "GPA phải <= 4.0")
    private Double gpa;
}
