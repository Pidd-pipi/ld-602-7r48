import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 20102,
    host: "0.0.0.0",
    proxy: {
      "/api": {
        target: "http://localhost:21102",
        changeOrigin: true
      },
      "/health": {
        target: "http://localhost:21102",
        changeOrigin: true
      }
    }
  }
});
