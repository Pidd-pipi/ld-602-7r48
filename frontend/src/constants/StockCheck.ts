export const StockCheckStatus = ["PENDING", "APPROVED", "REJECTED"] as const;
export type StockCheckStatus = (typeof StockCheckStatus)[number];
export const StockCheckStatusText: Record<StockCheckStatus, string> = {
  PENDING: "待审",
  APPROVED: "复核通过",
  REJECTED: "已驳回"
};

export const VarianceType = ["SURPLUS", "LOSS", "MATCHED"] as const;
export type VarianceType = (typeof VarianceType)[number];
export const VarianceTypeText: Record<VarianceType, string> = {
  SURPLUS: "盘盈",
  LOSS: "盘亏",
  MATCHED: "无差异"
};

export const QualityStatusList = ["QUALIFIED", "FROZEN"] as const;
export type QualityStatusValue = (typeof QualityStatusList)[number];
export const QualityStatusText: Record<QualityStatusValue, string> = {
  QUALIFIED: "合格",
  FROZEN: "质检冻结"
};
