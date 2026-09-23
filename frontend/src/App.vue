<script setup lang="ts">
import { computed, ref, shallowRef, watch } from "vue";
import { routes } from "./router/routes";
import StatusBadge from "./components/common/StatusBadge.vue";
import WarehousesPage from "./pages/WarehousesPage.vue";
import DashboardPage from "./pages/DashboardPage.vue";
import SheltersPage from "./pages/SheltersPage.vue";
import DispatchPage from "./pages/DispatchPage.vue";
import EventsPage from "./pages/EventsPage.vue";

const pageComponents: Record<string, unknown> = {
  "/dashboard": DashboardPage,
  "/warehouses": WarehousesPage,
  "/shelters": SheltersPage,
  "/dispatch": DispatchPage,
  "/events": EventsPage
};

const initial = (window.location.hash.replace(/^#/, "") || routes[0]?.route || "/dashboard");
const active = ref<string>(routes.some((route) => route.route === initial) ? initial : routes[0]?.route ?? "/dashboard");
const currentPage = shallowRef(pageComponents[active.value] ?? DashboardPage);

const current = computed(() => routes.find((route) => route.route === active.value) ?? routes[0]);

watch(active, (route) => {
  window.location.hash = route;
  currentPage.value = pageComponents[route] ?? DashboardPage;
});
</script>

<template>
  <div class="shell">
    <aside>
      <div class="brand">城市防灾应急物资调度系统</div>
      <nav>
        <button
          v-for="route in routes"
          :key="route.route"
          :class="{ active: active === route.route }"
          @click="active = route.route"
        >{{ route.name }}</button>
      </nav>
    </aside>
    <main class="page">
      <section class="page-head">
        <div><p class="eyebrow">rescue-stock</p><h1>{{ current?.name }}</h1></div>
        <StatusBadge :value="active === '/warehouses' ? 'STOCK_CHECK_ONLINE' : 'LOCAL_DATA'" />
      </section>
      <component :is="currentPage" />
    </main>
  </div>
</template>
