package com.generated.rescueStock.constants;

/** 库存批次质检状态：QUALIFIED 合格 / FROZEN 质检冻结。 */
public final class QualityStatus {
  public static final String QUALIFIED = "QUALIFIED";
  public static final String FROZEN = "FROZEN";

  private QualityStatus() {}

  public static boolean isFrozen(String status) {
    return FROZEN.equals(status);
  }
}
