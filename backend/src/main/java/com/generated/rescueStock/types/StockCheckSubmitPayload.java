package com.generated.rescueStock.types;

/**
 * 仓库录入实盘数并提交盘点。
 * gainBasis 盘盈依据（盘盈必填），lossBasis 盘亏依据（盘亏必填）。
 */
public record StockCheckSubmitPayload(
    Long batchId,
    Integer actualQuantity,
    String gainBasis,
    String lossBasis,
    String submittedBy
) {}
