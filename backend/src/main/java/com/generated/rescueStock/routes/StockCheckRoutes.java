package com.generated.rescueStock.routes;

public final class StockCheckRoutes {
  public static final String PATH = "/api/stock-check";
  public static final String SUBMIT = PATH;
  public static final String LIST = PATH;
  public static final String DETAIL = PATH + "/{id}";
  public static final String APPROVE = PATH + "/{id}/approve";
  public static final String REJECT = PATH + "/{id}/reject";

  private StockCheckRoutes() {}
}
