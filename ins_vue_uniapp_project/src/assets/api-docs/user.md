# 用户模块接口文档

## 1. 用户登录

- 接口名称：用户登录
- 请求路径：/api/user/login
- 请求方式：POST
- 请求参数：
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```
- 返回值：
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": {
      "token": "string",
      "userInfo": {
        "id": "string",
        "username": "string",
        "avatar": "string"
      }
    }
  }
  ```

## 2. 获取用户信息

- 接口名称：获取用户信息
- 请求路径：/api/user/info
- 请求方式：GET
- 请求参数：
  ```json
  {
    "userId": "string"
  }
  ```
- 返回值：
  ```json
  {
    "code": 200,
    "message": "success",
    "data": {
      "id": "string",
      "username": "string",
      "avatar": "string",
      "followCount": 0,
      "fansCount": 0
    }
  }
  ```
