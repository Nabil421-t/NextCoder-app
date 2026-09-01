//package com.cuet.dsa.idempotency;
//
//import java.io.Serializable;
//
//public class IdempotencyRecord implements Serializable {
//
//    private final String status;        // IN_PROGRESS, COMPLETED, FAILED
//    private final Long submissionId;
//
//    public IdempotencyRecord(String status, Long submissionId) {
//        this.status = status;
//        this.submissionId = submissionId;
//
//    }
//
//    public String status() {
//        return status;
//    }
//
//    public Long submissionId() {
//        return submissionId;
//    }
//
//
//}
package com.cuet.dsa.idempotency;

public record IdempotencyRecord(
        String status,
        Long submissionId,
        Long createdAt
) {}