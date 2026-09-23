/**
 * 本地离线兜底存储：后端不可达时由前端在 localStorage 内执行同样的盘点规则，
 * 保证库存页刷新后仍可回读差异。所有键统一前缀，不接第三方 API。
 */
const PREFIX = "rescue-stock:";

export function readStore<T>(key: string, fallback: T): T {
  if (typeof localStorage === "undefined") return fallback;
  const raw = localStorage.getItem(PREFIX + key);
  if (!raw) return fallback;
  try {
    return JSON.parse(raw) as T;
  } catch {
    return fallback;
  }
}

export function writeStore<T>(key: string, value: T): void {
  if (typeof localStorage === "undefined") return;
  localStorage.setItem(PREFIX + key, JSON.stringify(value));
}
