package ru.startup.skinscan.web.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class PhotoRequest {

    @NotNull
    private final MultipartFile file;

    private final String[] tags;

    @Size(max = 50)
    private final String description;
    private final String category;

    public PhotoRequest(MultipartFile file, String[] tags, String description, String category) {
        this.file = file;
        this.tags = tags;
        this.description = description;
        this.category = category;
    }

    public MultipartFile file() {
        return file;
    }

    public String[] tags() {
        return tags;
    }

    public String description() {
        return description;
    }

    public String category() {
        return category;
    }
}
