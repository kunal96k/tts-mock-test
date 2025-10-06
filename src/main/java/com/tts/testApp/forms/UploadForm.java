package com.tts.testApp.forms;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadForm {

    @NotNull(message = "Please select a subject")
    private Long subjectId;

    @NotNull(message = "Please select a file to upload")
    private MultipartFile file;

}
