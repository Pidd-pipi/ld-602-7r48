/** 统一请求封装：解析后端错误体 {code,message}，供页面提示冲突/校验原因。 */
export interface ApiErrorBody {
  code?: string;
  message?: string;
}

async function parseError(res: Response): Promise<Error> {
  let body: ApiErrorBody = {};
  try {
    body = (await res.json()) as ApiErrorBody;
  } catch {
    // 非 JSON 错误响应时忽略解析失败
  }
  const error = new Error(body.message || `请求失败（${res.status}）`);
  (error as Error & { code?: string; status?: number }).code = body.code;
  (error as Error & { code?: string; status?: number }).status = res.status;
  return error;
}

export async function getJson<T>(path: string): Promise<T> {
  const res = await fetch(path, { headers: { Accept: "application/json" } });
  if (!res.ok) throw await parseError(res);
  return (await res.json()) as T;
}

export async function sendJson<T>(path: string, method: "POST", body: unknown): Promise<T> {
  const res = await fetch(path, {
    method,
    headers: { "Content-Type": "application/json", Accept: "application/json" },
    body: body === undefined || body === null ? undefined : JSON.stringify(body)
  });
  if (!res.ok) throw await parseError(res);
  return (await res.json()) as T;
}
