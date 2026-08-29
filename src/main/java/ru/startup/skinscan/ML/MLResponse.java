package ru.startup.skinscan.ML;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MLResponse {

    @JsonProperty("status")
    private String status;

    @JsonProperty("result")
    private Result result;

    @JsonProperty("error")
    private ErrorInfo error;

    @JsonProperty("processing_time_ms")
    private Long processingTimeMs;

    @JsonProperty("timestamp")
    private String timestamp;

    public MLResponse() {}

    public MLResponse(String status, Result result, Long processingTimeMs) {
        this.status = status;
        this.result = result;
        this.processingTimeMs = processingTimeMs;
        this.timestamp = LocalDateTime.now().toString();
    }

    public static MLResponse success(Result result, Long processingTimeMs) {
        return new MLResponse("success", result, processingTimeMs);
    }

    public static MLResponse error(String errorCode, String errorMessage) {
        MLResponse response = new MLResponse();
        response.setStatus("error");
        response.setError(new ErrorInfo(errorCode, errorMessage));
        response.setTimestamp(LocalDateTime.now().toString());
        return response;
    }

    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {

        @JsonProperty("risk_percentage")
        private Double riskPercentage;

        @JsonProperty("condition")
        private String condition;

        @JsonProperty("features")
        private List<String> features;

        @JsonProperty("recommendation")
        private String recommendation;

        @JsonProperty("confidence")
        private Integer confidence;

        public Double riskPercentage() { return riskPercentage; }
        public String condition() { return condition; }
        public List<String> features() { return features; }
        public String recommendation() { return recommendation; }
        public Integer confidence() { return confidence; }
    }

    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorInfo {
        @JsonProperty("error_code")
        private String errorCode;

        @JsonProperty("error_message")
        private String errorMessage;

        @JsonProperty("error_details")
        private Object errorDetails;

        public ErrorInfo() {}

        public ErrorInfo(String errorCode, String errorMessage) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }

        public String errorCode() { return errorCode; }
        public String errorMessage() { return errorMessage; }
        public Object errorDetails() { return errorDetails; }
    }

    public String status() { return status; }
    public Result result() { return result; }
    public ErrorInfo error() { return error; }
    public Long processingTimeMs() { return processingTimeMs; }
    public String timestamp() { return timestamp; }


    public boolean isSuccess() {
        return "success".equalsIgnoreCase(status);
    }

    public boolean isPartial() {
        return "partial".equalsIgnoreCase(status);
    }

    public boolean isError() {
        return "error".equalsIgnoreCase(status);
    }
}
