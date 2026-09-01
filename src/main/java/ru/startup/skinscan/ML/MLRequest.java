package ru.startup.skinscan.ML;

public class MLRequest {

    private byte[] imageData;
    private String imageUrl;
    private Metadata metadata;

    public MLRequest(byte[] imageData) {
        this.imageData = imageData;
        this.imageUrl = null;
    }

    public MLRequest(String imageUrl) {
        this.imageUrl = imageUrl;
        this.imageData = null;
    }


    public static class Metadata {
        private String photoId;
        private String userId;
        private String filename;
        private String mimeType;
        private Long fileSize;

        public String photoId() { return photoId; }
        public void setPhotoId(String photoId) { this.photoId = photoId; }
        public String userId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        public String filename() { return filename; }
        public void setFilename(String filename) { this.filename = filename; }
        public String mimeType() { return mimeType; }
        public void setMimeType(String mimeType) { this.mimeType = mimeType; }
        public Long fileSize() { return fileSize; }
        public void setFileSize(Long fileSize) { this.fileSize = fileSize; }
    }

    public byte[] imageData() { return imageData; }

    public void setImageData(byte[] imageData) { this.imageData = imageData; }

    public String imageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Metadata metadata() { return metadata; }

    public void setMetadata(Metadata metadata) { this.metadata = metadata; }

    // Проверяет, передан ли файл через байтовый массив
    public boolean isBinaryMode() {
        return imageData != null && imageData.length > 0;
    }

    // Проверяет, передан ли файл через URL
    public boolean isUrlMode() {
        return imageUrl != null && !imageUrl.isEmpty();
    }
}
