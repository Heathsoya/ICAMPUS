# 校园生活百事通前端

## 包含页面

- 登录/注册
- 首页问答
- 热点榜
- 用户贡献
- 后台审核
- 个人中心

## 对接接口

- POST /api/auth/register
- POST /api/auth/login
- POST /api/qna/ask
- GET /api/qna/hot
- POST /api/qna/feedback
- POST /api/contribution
- GET /api/admin/audit
- POST /api/admin/audit

## 使用方式

直接打开 index.html 即可查看页面。

如果后端地址不是同源，需要在 script.js 中修改：

```js
const baseUrl = "http://localhost:8080";
```

## 登录身份

页面中提供两种登录身份：

- 普通用户 USER
- 管理员 ADMIN

管理员登录后可以看到后台审核页面。
