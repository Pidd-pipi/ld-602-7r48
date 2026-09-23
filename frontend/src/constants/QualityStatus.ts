/** 批次质检状态：与后端 constants/QualityStatus 重复定义。 */
export const QualityStatus = {
  NORMAL: "NORMAL",
  FROZEN: "FROZEN",
  EXPIRED: "EXPIRED"
} as const;

export type QualityStatusValue = keyof typeof QualityStatus;

export const QualityStatusText: Record<QualityStatusValue, string> = {
  NORMAL: "正常",
  FROZEN: "质检冻结",
  EXPIRED: "已过期"
};
