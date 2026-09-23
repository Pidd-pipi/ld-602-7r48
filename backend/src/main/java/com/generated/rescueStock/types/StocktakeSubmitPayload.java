package com.generated.rescueStock.types;

/** 仓库员提交盘点的请求体：选择批次、录入实盘数、写明盘盈盘亏依据。 */
public record StocktakeSubmitPayload(
    Long batchId,
    Integer actualQuantity,
    String reason,
    String submittedBy
) {}
