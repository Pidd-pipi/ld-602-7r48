import type { StocktakeVarianceValue } from "../types/StocktakeOrder";

/** 盘点差异方向：与后端 constants/StocktakeVariance 重复定义。 */
export const StocktakeVariance = {
  SURPLUS: "SURPLUS",
  LOSS: "LOSS",
  MATCH: "MATCH"
} as const satisfies Record<string, StocktakeVarianceValue>;

export const StocktakeVarianceText: Record<StocktakeVarianceValue, string> = {
  SURPLUS: "盘盈",
  LOSS: "盘亏",
  MATCH: "一致"
};
