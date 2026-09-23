package com.generated.rescueStock.routes;

/** 盘点差异复核接口路径常量，前端请求地址需与此保持一致。 */
public final class StocktakeOrderRoutes {
  public static final String PATH = "/api/stocktake-order";
  public static final String SUBMIT = PATH + "/submit";
  public static final String REVIEW = PATH + "/{id}/review";
  public static final String DETAIL = PATH + "/{id}";

  private StocktakeOrderRoutes() {}
}
