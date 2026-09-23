package com.generated.rescueStock.middlewares;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

/**
 * 操作日志（审计流水）。盘点提交 / 拦截 / 通过 / 驳回等写动作统一经此留痕，
 * 对应数据库 audit_log 表，服务层不得直接散写日志结构。
 */
@Component
public class AuditLogMiddleware {
  private final List<Map<String, Object>> entries = new CopyOnWriteArrayList<>();

  public void record(String actor, String action, String targetType, String targetId, String detail) {
    Map<String, Object> row = new LinkedHashMap<>();
    row.put("actor", actor);
    row.put("action", action);
    row.put("target_type", targetType);
    row.put("target_id", targetId);
    row.put("detail", detail);
    row.put("created_at", Instant.now().toString());
    entries.add(row);
  }

  public List<Map<String, Object>> list() {
    return List.copyOf(entries);
  }
}
