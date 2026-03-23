import Vue from "vue";
import VueRouter from "vue-router";
import ElementUI from "element-ui";
import "element-ui/lib/theme-chalk/index.css";
import echarts from "echarts";
import { clearToken, getToken } from "@/utils/storage.js";

Vue.prototype.$echarts = echarts;
Vue.use(ElementUI);
Vue.use(VueRouter);

const adminChildren = [
  {
    path: "/adminLayout",
    name: "仪表盘",
    icon: "el-icon-pie-chart",
    component: () => import("@/views/admin/Main.vue"),
    meta: { requireAuth: true }
  },
  {
    path: "/userManage",
    name: "用户管理",
    icon: "el-icon-user-solid",
    component: () => import("@/views/admin/User.vue"),
    meta: { requireAuth: true }
  },
  {
    path: "/ProductManage",
    name: "商品管理",
    icon: "el-icon-shopping-bag-1",
    component: () => import("@/views/admin/Product.vue"),
    meta: { requireAuth: true }
  },
  {
    path: "/categoryManage",
    name: "分类管理",
    icon: "el-icon-document-checked",
    component: () => import("@/views/admin/Category.vue"),
    meta: { requireAuth: true }
  },
  {
    path: "/evaluations",
    name: "评论管理",
    icon: "el-icon-chat-dot-round",
    component: () => import("@/views/admin/Evaluations.vue"),
    meta: { requireAuth: true }
  },
  {
    path: "/ordersManage",
    name: "订单管理",
    icon: "el-icon-s-order",
    component: () => import("@/views/admin/Orders.vue"),
    meta: { requireAuth: true }
  },
  {
    path: "/product-detail1",
    name: "商品详情",
    show: false,
    component: () => import("@/views/admin/ProductDetail.vue"),
    meta: { requireAuth: true }
  }
];

const userChildren = [
  {
    name: "UserProduct",
    path: "/product",
    component: () => import("@/views/user/Product.vue"),
    meta: { requireAuth: false }
  },
  {
    name: "MyProduct",
    path: "/myProduct",
    component: () => import("@/views/user/MyProduct.vue"),
    meta: { requireAuth: true }
  },
  {
    name: "MySave",
    path: "/mySave",
    component: () => import("@/views/user/MySave.vue"),
    meta: { requireAuth: true }
  },
  {
    name: "MyView",
    path: "/myView",
    component: () => import("@/views/user/MyView.vue"),
    meta: { requireAuth: true }
  },
  {
    name: "Orders",
    path: "/orders",
    component: () => import("@/views/user/Orders.vue"),
    meta: { requireAuth: true }
  },
  {
    name: "BuyOrdersCompat",
    path: "/buyOrders",
    show: false,
    component: () => import("@/views/user/BuyOrders.vue"),
    meta: { requireAuth: true }
  },
  {
    name: "MyOrdersCompat",
    path: "/myOrders",
    show: false,
    component: () => import("@/views/user/MyOrders.vue"),
    meta: { requireAuth: true }
  },
  {
    name: "PayOrderCompat",
    path: "/payOrder",
    show: false,
    component: () => import("@/views/user/PayOrder.vue"),
    meta: { requireAuth: true }
  },
  {
    name: "Message",
    path: "/message",
    component: () => import("@/views/user/Message.vue"),
    meta: { requireAuth: true }
  },
  {
    name: "Myself",
    path: "/myself",
    component: () => import("@/views/user/Myself.vue"),
    meta: { requireAuth: true }
  },
  {
    name: "Search",
    path: "/search",
    show: false,
    auth: false,
    component: () => import("@/views/user/Search.vue"),
    meta: { requireAuth: false }
  },
  {
    name: "PostProduct",
    path: "/post-product",
    component: () => import("@/views/user/PostProduct.vue"),
    meta: { requireAuth: true }
  },
  {
    name: "EditProduct",
    path: "/edit-product",
    component: () => import("@/views/user/EditProduct.vue"),
    meta: { requireAuth: true }
  },
  {
    name: "ProductDetailCompat",
    path: "/product-detail",
    component: () => import("@/views/user/ProductDetail.vue"),
    meta: { requireAuth: false }
  },
  {
    name: "ProductDetail",
    path: "/product/detail",
    component: () => import("@/views/user/ProductDetail.vue"),
    meta: { requireAuth: false }
  }
];

const routes = [
  { path: "/", component: () => import("@/views/user/Home.vue") },
  { path: "/login", component: () => import("@/views/login/Login.vue") },
  {
    path: "/register",
    component: () => import("@/views/register/Register.vue")
  },
  {
    path: "/admin",
    component: () => import("@/views/admin/Home.vue"),
    meta: { requireAuth: true },
    children: adminChildren
  },
  {
    path: "/user",
    component: () => import("@/views/user/Home.vue"),
    children: userChildren
  }
];

const router = new VueRouter({
  routes,
  mode: "history"
});

router.beforeEach((to, from, next) => {
  if (to.path === "/login" || to.path === "/register") {
    next();
    return;
  }

  if (!to.matched.some(record => record.meta.requireAuth)) {
    next();
    return;
  }

  const token = getToken();
  if (!token) {
    next({
      path: "/login",
      query: { redirect: to.fullPath }
    });
    return;
  }

  try {
    const role = Number(sessionStorage.getItem("role"));
    const rootPath = to.matched[0] && to.matched[0].path;

    if (rootPath === "/admin" && role !== 1) {
      clearToken();
      next("/login");
      return;
    }

    if (rootPath === "/user" && role !== 2) {
      clearToken();
      next("/login");
      return;
    }

    next();
  } catch (error) {
    console.error("permission check failed:", error);
    next("/login");
  }
});

export default router;
