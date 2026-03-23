<template>
  <div>
    <Toolbar
      style="border-bottom: 1px solid #eae8e8;"
      :editor="editor"
      :defaultConfig="toolbarConfig"
      :mode="mode"
    />
    <Editor
      :style="{ height: height, overflowY: 'hidden' }"
      v-model="content"
      :defaultConfig="editorConfig"
      :mode="mode"
      @onCreated="onCreated"
    />
  </div>
</template>
<script>
import Vue from "vue";
import { Editor, Toolbar } from "@wangeditor/editor-for-vue";
import { API_BASE_URL } from "@/utils/request";
import { toFullImageUrl } from "@/utils/imageUrl";
export default Vue.extend({
  components: { Editor, Toolbar },
  props: {
    receiveContent: {
      type: String,
      default: "",
      required: true
    },
    height: {
      type: String,
      default: "calc(100vh - 100px)"
    }
  },
  data() {
    return {
      editor: null,
      content: "<p>创作内容</p>",
      toolbarConfig: {},
      editorConfig: {
        placeholder: "请输入内容...",
        MENU_CONF: {
          uploadImage: {
            server: API_BASE_URL + "/file/upload",
            fieldName: "file",
            maxFileSize: 10 * 1024 * 1024,
            maxNumberOfFiles: 10,
            // allowedFileTypes: ['image/*'],
            metaWithUrl: false,
            withCredentials: true,
            timeout: 10 * 1000,
            headers: {
              token: sessionStorage.getItem("token")
            },
            customInsert(res, insertFn) {
              const imageUrl = toFullImageUrl(res && res.data ? res.data : "");
              const finalUrl = imageUrl || (res && res.data) || "";
              insertFn(finalUrl, finalUrl, finalUrl);
            }
          }
        }
      },
      mode: "default"
    };
  },
  methods: {
    onCreated(editor) {
      this.editor = Object.seal(editor);
      this.toolbarConfig.excludeKeys = ["group-video", "group-image"];
    }
  },
  watch: {
    receiveContent: {
      handler(v1) {
        console.log("接收内容：", v1);
        this.content = v1;
      },
      deep: true, // 启用深度监听
      immediate: true
    },
    content(newVal) {
      this.$emit("on-receive", newVal);
    }
  },
  beforeDestroy() {
    const editor = this.editor;
    if (editor == null) return;
    editor.destroy();
  }
});
</script>
<style src="@wangeditor/editor/dist/css/style.css"></style>
