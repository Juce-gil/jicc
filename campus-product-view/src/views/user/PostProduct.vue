<template>
  <div class="post-product">
    <UserPageSection
      eyebrow="Publish"
      title="发布商品"
      description="补充商品信息、上传封面并设置价格，让你的闲置更快被看见。"
    >
      <el-card shadow="never" class="form-card">
        <el-form
          ref="productForm"
          :model="product"
          :rules="rules"
          label-width="110px"
        >
          <el-row :gutter="30">
            <!-- 左侧：基础信息 -->
            <el-col :span="12">
              <el-form-item label="商品名称" prop="name">
                <el-input v-model="product.name" placeholder="请输入商品名称" />
              </el-form-item>

              <el-form-item label="新旧程度" prop="oldLevel">
                <el-slider
                  v-model="product.oldLevel"
                  :min="1"
                  :max="9"
                  show-stops
                  show-input
                />
              </el-form-item>

              <el-form-item label="商品价格" prop="price">
                <el-input-number
                  v-model="product.price"
                  :min="0"
                  :precision="2"
                  controls-position="right"
                />
              </el-form-item>

              <el-form-item label="支持砍价">
                <el-switch
                  v-model="product.isBargain"
                  active-text="支持"
                  inactive-text="不支持"
                />
              </el-form-item>

              <el-form-item label="商品类别" prop="categoryId">
                <el-select
                  v-model="product.categoryId"
                  placeholder="请选择商品类别"
                >
                  <el-option
                    v-for="item in categoryList"
                    :key="item.id"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </el-form-item>

              <el-form-item label="库存数量" prop="inventory">
                <el-input-number
                  v-model="product.inventory"
                  :min="1"
                  controls-position="right"
                />
              </el-form-item>
            </el-col>

            <!-- 右侧：图片 + 描述 -->
            <el-col :span="12">
              <el-form-item label="商品图片" prop="coverList">
                <el-upload
                  class="upload"
                  :show-file-list="false"
                  :http-request="handleUploadRequest"
                  :on-success="handleUploadSuccess"
                  :on-error="handleUploadError"
                  accept="image/*"
                >
                  <img
                    v-if="product.coverList"
                    :src="coverListDisplayUrl"
                    class="product-image"
                  />
                  <i v-else class="el-icon-plus upload-icon"></i>
                </el-upload>
              </el-form-item>

              <el-form-item label="商品描述" prop="detail">
                <el-input
                  type="textarea"
                  :rows="8"
                  v-model="product.detail"
                  placeholder="请输入商品描述"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <!-- 提交按钮 -->
          <el-form-item style="text-align:center;margin-top:20px;">
            <el-button type="primary" @click="submitForm">
              发布商品
            </el-button>
            <el-button @click="resetForm">
              重置
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </UserPageSection>
  </div>
</template>

<script>
import UserPageSection from "@/components/user/UserPageSection.vue";
import { toFullImageUrl } from "@/utils/imageUrl";
import {
  resolveUploadUrl,
  uploadFileWithAxios,
  getUploadErrorMessage
} from "@/utils/upload";

export default {
  name: "PostProduct",
  components: {
    UserPageSection
  },
  data() {
    return {
      product: {
        name: "",
        detail: "",
        coverList: "",
        oldLevel: 9,
        categoryId: null,
        inventory: 1,
        price: 0,
        isBargain: false
      },
      categoryList: [],
      rules: {
        name: [{ required: true, message: "请输入商品名称", trigger: "blur" }],
        oldLevel: [
          { required: true, message: "请选择新旧程度", trigger: "change" }
        ],
        price: [{ required: true, message: "请输入商品价格", trigger: "blur" }],
        categoryId: [
          { required: true, message: "请选择商品类别", trigger: "change" }
        ],
        inventory: [{ required: true, message: "请输入库存", trigger: "blur" }],
        detail: [{ required: true, message: "请输入商品描述", trigger: "blur" }]
      }
    };
  },
  created() {
    this.fetchCategoryList();
  },
  computed: {
    coverListDisplayUrl() {
      return toFullImageUrl(this.product.coverList);
    }
  },
  methods: {
    async fetchCategoryList() {
      try {
        const res = await this.$axios.post("/category/query", { size: 200 });
        if (res.data && res.data.code === 200 && Array.isArray(res.data.data)) {
          this.categoryList = res.data.data.filter(
            item => item.isUse === true || item.isUse === 1
          );
        } else {
          this.categoryList = [];
        }
      } catch (e) {
        this.categoryList = [];
        this.$message.error("获取商品分类失败");
      }
    },

    async handleUploadRequest(options) {
      try {
        const url = await uploadFileWithAxios({
          axiosInstance: this.$axios,
          file: options.file
        });
        options.onSuccess(url);
      } catch (error) {
        options.onError(error);
      }
    },

    handleUploadSuccess(url) {
      const raw = resolveUploadUrl(url);
      if (raw && String(raw).trim()) {
        this.product.coverList = String(raw).trim();
        this.$message.success("图片上传成功");
      } else {
        this.$message.error("图片地址无效");
      }
    },

    handleUploadError(error) {
      this.$message.error(getUploadErrorMessage(error, "图片上传失败，请重试"));
    },

    submitForm() {
      this.$refs.productForm.validate(async valid => {
        if (!valid) return;
        try {
          const res = await this.$axios.post("/product/save", this.product);
          if (res.data.code === 200) {
            this.$notify.success({
              title: "发布成功",
              message: "商品发布成功"
            });
            this.resetForm();
          }
        } catch (e) {
          this.$message.error("商品发布失败");
        }
      });
    },

    resetForm() {
      this.$refs.productForm.resetFields();
      this.product.coverList = "";
    }
  }
};
</script>

<style scoped lang="scss">
.post-product {
  padding: 0;
}

.form-card {
  border-radius: 24px;
}

.upload {
  width: 140px;
  height: 140px;
  border: 1px dashed #dcdfe6;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.upload-icon {
  font-size: 32px;
  color: #999;
}

.product-image {
  width: 140px;
  height: 140px;
  object-fit: cover;
}
</style>
