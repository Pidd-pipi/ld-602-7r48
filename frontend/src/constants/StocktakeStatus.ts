import type { StocktakeStatusValue } from "../types/StocktakeOrder";

/** 盘点复核状态：与后端 constants/StocktakeStatus 重复定义，改枚举需前后端同步。 */
export const StocktakeStatus = {
  PENDING: "PENDING",
  APPROVED: "APPROVED",
  REJECTED: "REJECTED"
} as const satisfies Record<string, StocktakeStatusValue>;

export const StocktakeStatusText: Record<StocktakeStatusValue, string> = {
  PENDING: "待审",
  APPROVED: "审批通过",
  REJECTED: "已驳回"
};
