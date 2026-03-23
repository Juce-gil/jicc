module.exports = {
  lintOnSave: false,
  assetsDir: "static",
  parallel: false,
  publicPath: process.env.NODE_ENV === "production" ? "./" : "/",
  devServer: {
    host: "localhost",
    port: 21091,
    https: false,
    historyApiFallback: true,
    proxy: {
      // 转发 /api 开头的请求到后端，不重写路径（后端 context-path 为 /api/campus-product-sys/v1.0）
      "/api": {
        target: "http://localhost:21090",
        changeOrigin: true
      }
    },
    overlay: {
      warning: false,
      errors: false
    }
  }
};
