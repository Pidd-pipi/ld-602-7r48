package com.generated.rescueStock.types;

/** 复核动作：审批通过 / 驳回时的复核人与意见。 */
public record StockCheckReviewPayload(
    String reviewer,
    String comment
) {}
