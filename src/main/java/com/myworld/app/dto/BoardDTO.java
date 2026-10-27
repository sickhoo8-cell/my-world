
package com.myworld.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class BoardDTO {

    @NotBlank(message = "제목을 입력해 주세요.")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "내용을 입력해 주세요.")
    @Size(max = 2000)
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
