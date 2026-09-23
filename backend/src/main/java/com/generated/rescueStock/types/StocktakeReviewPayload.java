package com.generated.rescueStock.types;

/** 审批员复核请求体：approve=true 审批通过并落账，否则驳回；复核意见必填。 */
public record StocktakeReviewPayload(
    boolean approve,
    String reviewNote,
    String reviewedBy
) {}
