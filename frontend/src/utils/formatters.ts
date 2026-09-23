import { StocktakeStatusText } from "../constants/StocktakeStatus";
import { StocktakeVarianceText } from "../constants/StocktakeVariance";
import { QualityStatusText } from "../constants/QualityStatus";

export const formatDate = (value: string | null | undefined) =>
  value ? new Date(value).toLocaleString("zh-CN") : "—";
export const formatStatus = (value: string) => value.replace(/_/g, " ");
export const formatNumber = (value: number) => new Intl.NumberFormat("zh-CN").format(value);
export const formatRisk = (value: string) => ({ LOW: "低", MEDIUM: "中", HIGH: "高", CRITICAL: "严重", EXTREME: "极高" }[value] ?? value);

export const formatStocktakeStatus = (value: string) =>
  StocktakeStatusText[value as keyof typeof StocktakeStatusText] ?? value;
export const formatStocktakeVariance = (value: string) =>
  StocktakeVarianceText[value as keyof typeof StocktakeVarianceText] ?? value;
export const formatQualityStatus = (value: string) =>
  QualityStatusText[value as keyof typeof QualityStatusText] ?? value;

/** 差异带符号展示：盘盈 +n，盘亏 -n，一致 0。 */
export const formatVarianceQuantity = (value: number) =>
  value > 0 ? `+${formatNumber(value)}` : formatNumber(value);
