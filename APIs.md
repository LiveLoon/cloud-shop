# Cloud Shop APP API



## 通用说明

- **基础路径**  
  各服务独立部署，基础路径如下：  
  - 用户服务：`http://localhost:8100`  
  - 商品服务：`http://localhost:8080/product-service`  
  - 订单服务：`http://localhost:8080/order-service`  

- **统一响应格式**  
  所有接口均返回 JSON，结构如下（部分异常情况可能直接返回 Spring Boot 错误格式）：  
  ```json
  {
    "code": 200,          // 200 成功，其他值表示错误
    "message": "操作成功", // 提示信息
    "data": {}            // 业务数据，可能为 null
  }
  ```

---

## 一、用户服务 (User Service)

### 1. 根据 ID 查询用户

- **接口**：`GET /api/user/getUserById/{id}`
- **描述**：获取指定 ID 的用户详细信息

**路径参数**：

| 参数名 | 类型 | 必填 | 说明        |
| ------ | ---- | ---- | ----------- |
| id     | int  | 是   | 用户主键 ID |

**成功响应** (code = 200)：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "user1",
    "password": "1234",
    "realName": null,
    "score": 0
  }
}
```

**失败响应** (code = 404)：
```json
{
  "code": 404,
  "message": "No value present",
  "data": null
}
```

### 2. 用户注册

- **接口**：`POST /api/user/register`
- **描述**：注册新用户
- **Content-Type**：`application/json`

**请求体示例**：
```json
{
  "username": "user2",
  "password": "1234"
}
```

**请求参数说明**：

| 参数名   | 类型   | 必填 | 说明       |
| -------- | ------ | ---- | ---------- |
| username | string | 是   | 登录用户名 |
| password | string | 是   | 明文密码   |

**成功响应** (code = 200)：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 2,
    "username": "user2",
    "password": "1234",
    "realName": null,
    "score": 0
  }
}
```

**失败响应** (code = 500，用户名已存在)：
```json
{
  "code": 500,
  "message": "User already exists",
  "data": null
}
```

---

### 3. 用户登录

已有登录功能但未文档化，补充说明。

- **接口**：`POST /api/user/login`
- **描述**：验证用户名密码并返回用户信息
- **Content-Type**：`application/json`
- **请求体**：

json

```
{
  "username": "user1",
  "password": "1234"
}
```



- **成功响应** (code=200)：

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "username": "user1",
    "realName": null,
    "score": 0
  }
}
```



- **失败响应**（密码错误/用户不存在）：

json

```
{
  "code": 500,
  "message": "用户名或密码错误",
  "data": null
}
```







### 4. 更新用户信息

- **接口**：`PUT /api/user/update`
- **描述**：修改用户昵称、真实姓名等基本信息（不含密码）
- **请求体**：



```
{
  "id": 1,
  "realName": "张三",
  "username" : "life",
  "password":"newpassword",

}
```



- **成功响应**：



```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
      "id": 1,
      "username":"life",
      "realName": "张三",
      "password":"newpassword",
    "score": 100
  }
}
```



- **失败响应**（用户不存在）：



```json
{
  "code": 404,
  "message": "无效的请求",
  "data": nulljson
}
```

## 二、商品服务 (Product Service)

### 1. 根据 ID 查询商品

- **接口**：`GET /api/product/{id}`
- **描述**：获取指定 ID 的商品详情

**路径参数**：

| 参数名 | 类型 | 必填 | 说明    |
| ------ | ---- | ---- | ------- |
| id     | int  | 是   | 商品 ID |

**成功响应** (code = 200)：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "productName": "无线蓝牙耳机",
    "price": 199.99,
    "inventory": 500
  }
}
```

**失败响应** (code = 500，商品不存在)：
```json
{
  "code": 500,
  "message": "商品不存在",
  "data": null
}
```

### 2. 添加商品

- **接口**：`POST /api/product/add`
- **描述**：新增一个商品
- **Content-Type**：`application/json`

**请求体示例**：
```json
{
  "productName": "有线耳机",
  "price": 199.99,
  "inventory": 500,
   
}
```

**请求参数说明**：

| 参数名      | 类型   | 必填 | 说明     |
| ----------- | ------ | ---- | -------- |
| productName | string | 是   | 商品名称 |
| price       | number | 是   | 商品价格 |
| inventory   | int    | 是   | 库存数量 |

**成功响应** (code = 200)：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 4,
    "productName": "有线耳机",
    "price": 199.99,
    "inventory": 500
  }
}
```

**失败响应** (请求体格式错误等，返回 400)：
```json
{
  "timestamp": "2026-06-04T09:52:18.802+00:00",
  "status": 400,
  "error": "Bad Request",
  "path": "/api/product/add"
}
```

---





### 6. 增加库存（补货）

- **接口**：`PUT /api/product/updateInventory/{productId}/{buyNum}`
- **描述**：给商品增加库存（正数）
- **路径参数**：id(商品ID), amount(增加数量)
- **成功响应**：

json

```
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 7.删除产品

http

```
DELETE http://localhost:3000/api/product/5
```



**成功响应**：

json

```
{
  "code": 200,
  "message": "success",
  "data": null
}
```



### 8.修改产品（部分更新）

http

```
PUT http://localhost:3000/api/product/3
Content-Type: application/json

{
  "productName": "新名称",
  "price": 199.00,
  "imageUrl": "https://picsum.photos/id/100/200/200"
}
```



**成功响应**：

json

```
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 3,
    "productName": "新名称",
    "price": 199.00,
    "inventory": 100,
    "imageUrl": "https://picsum.photos/id/100/200/200"
  }
}
```

------



## 三、订单服务 (Order Service)

### 1. 根据 ID 查询订单

- **接口**：`GET /api/order/{id}`
- **描述**：获取订单详情，包含用户信息和订单项列表

**路径参数**：

| 参数名 | 类型 | 必填 | 说明    |
| ------ | ---- | ---- | ------- |
| id     | int  | 是   | 订单 ID |

**成功响应** (code = 200)：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "orderId": 2,
    "createTime": "2026-06-04T17:44:44.102431",
    "userInfo": {
      "userId": 1,
      "username": "user1",
      "realName": null
    },
    "itemList": [
      {
        "productId": 5002,
        "productName": "劳模并",
        "buyNum": 2
      },
      {
        "productId": 5003,
        "productName": "有限首部",
        "buyNum": 1
      }
    ]
  }
}
```

**失败响应** (订单不存在，返回 500 Internal Server Error)：
```json
{
  "timestamp": "2026-06-04T09:53:46.612+00:00",
  "status": 500,
  "error": "Internal Server Error",
  "path": "/api/order/1234234"
}
```

### 2. 创建订单

- **接口**：`POST /api/order/add`
- **描述**：创建新订单（涉及分布式事务）
- **Content-Type**：`application/json`

**请求体示例**：
```json
{
  "userId": 1,
  "itemList": [
    {
      "productId": 5002,
      "buyNum": 2,
      "productName": "劳模并",
      "price": 299.00
    },
    {
      "productId": 5003,
      "buyNum": 1,
      "productName": "有限首部",
      "price": 89.50
    }
  ]
}
```

**请求参数说明**：

| 参数名                 | 类型   | 必填 | 说明                 |
| ---------------------- | ------ | ---- | -------------------- |
| userId                 | int    | 是   | 下单用户 ID          |
| itemList               | array  | 是   | 订单项列表，不能为空 |
| itemList[].productId   | int    | 是   | 商品 ID              |
| itemList[].buyNum      | int    | 是   | 购买数量             |
| itemList[].productName | string | 是   | 商品名称（冗余）     |
| itemList[].price       | number | 是   | 商品单价             |

**成功响应** (code = 200)：
```json
{
  "code": 200,
  "message": "订单添加成功",
  "data": null
}
```

**失败响应** (code = 500，可能为用户服务调用失败或分布式事务回滚)：
```json
{
  "code": 500,
  "message": "try to proceed invocation error",
  "data": null
}
```

> **说明**：该接口依赖 `user-service` 验证用户是否存在，若用户服务不可用或熔断，会返回以上错误。请确保所有依赖服务正常运行。





### 4. 取消订单

- **接口**：`PUT /api/order/cancel/{id}`
- **描述**：取消指定订单（需恢复库存，建议加入分布式事务）
- **成功响应**：

json

```
{
  "code": 200,
  "message": "订单已取消",
  "data": null
}
```



- **失败响应**（订单不存在或状态不可取消）：

json

```
{
  "code": 500,
  "message": "订单状态不允许取消",
  "data": null
}
```







### 5. 删除订单

- **接口**：`DELETE /api/order/{id}`

- **描述**：物理删除订单记录（谨慎使用）

  - **成功响应**：

  json

  ```
  {
    "code": 200,
    "message": "订单已删除",
    "data": null
  }
  ```

  

  - **失败响应**（订单不存在或状态不可取消）：

  json

  ```
  {
    "code": 500,
    "message": "订单删除失败",
    "data": null
  }
  ```












## 三、订单服务 (Payment Service)

## 支付服务 API 文档

### 基础信息

- **Base URL**：`/api/payment`

- **请求与响应格式**：`application/json`

- **统一响应结构** `Result<T>`：

  json

  ```
  {
    "code": 200,
    "message": "success",
    "data": { ... }
  }
  ```

  

  - `code`：200 成功，非 200 失败
  - `message`：提示信息
  - `data`：返回的数据（成功时存在）

------

## 1. 创建支付订单

用户在确认支付后调用此接口，生成一笔支付记录。

### 请求

- **URL**：`POST /create`
- **请求体** `PaymentDTO`：

| 字段名         | 类型       | 必填 | 说明                                                 |
| -------------- | ---------- | ---- | ---------------------------------------------------- |
| `orderId`      | Integer    | 是   | 业务订单 ID                                          |
| `buyerAddress` | String     | 否   | 买家 ETC 地址（可留空）                              |
| `value`        | BigInteger | 是   | 预期支付金额，单位 **Wei**（例如 1 ETC = 10^18 Wei） |
| `chainId`      | BigInteger | 是   | 链 ID，ETC 主网为 `61`，测试网 `63` 等               |

**示例**：

json

```
{
  "orderId": 10086,
  "buyerAddress": "0xAbc123...",
  "value": 1000000000000000000,
  "chainId": 61
}
```



### 响应

- **成功**：返回 `Result<Payment>`，`data` 为创建的 `Payment` 实体（包含支付 ID、状态、平台收款地址等）。

**响应示例**：

json

```
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "orderId": 10086,
    "status": "CREATED",
    "paymentMessage": "等待用户支付，请在转账时填入 data: 0x4f524445525f3130303836",
    "buyerAddress": "0xAbc12345678901234567890123456789012345678",
    "sellerAddress": "0x83249283759234789283478982792835293852",
    "value": 1000000000000000000,
    "realValue": null,
    "chainId": 61,
    "tx": null,
    "txTime": null,
    "createTime": "2026-06-05T10:00:00",
    "updateTime": "2026-06-05T10:00:00"
  }
}
```



- **失败**（如订单已创建支付记录）：

json

```
{
  "code": 500,
  "message": "该订单已创建支付，不能重复创建",
  "data": null
}
```



------

## 2. 根据支付 ID 查询支付详情

获取指定支付记录的详细信息。

### 请求

- **URL**：`GET /{paymentId}`
- **路径参数**：

| 参数名      | 类型    | 必填 | 说明              |
| ----------- | ------- | ---- | ----------------- |
| `paymentId` | Integer | 是   | 支付记录的主键 ID |

**示例**：`GET /api/payment/1`

### 响应

- **成功**：返回 `Result<Payment>`，包含完整的支付实体。
- **失败**（支付记录不存在）：

json

```
{
  "code": 500,
  "message": "支付记录不存在",
  "data": null
}
```



------

## 3. 根据订单 ID 查询支付详情

通过业务订单号查询对应的支付记录（一个订单只能有一笔支付）。

### 请求

- **URL**：`GET /order/{orderId}`
- **路径参数**：

| 参数名    | 类型    | 必填 | 说明        |
| --------- | ------- | ---- | ----------- |
| `orderId` | Integer | 是   | 业务订单 ID |

**示例**：`GET /api/payment/order/10086`

### 响应

- **成功**：返回 `Result<Payment>`
- **失败**（该订单没有支付记录）：

json

```
{
  "code": 500,
  "message": "该订单无支付记录",
  "data": null
}
```



------

## 4. 支付回调接口（已注释，可启用）

> 此接口设计用于内部监听器或外部 Webhook 在确认链上交易后调用，更新支付状态。目前代码中已注释，若需要可取消注释并按以下规范调用。

- **URL**：`POST /callback`（需取消注释）
- **请求体** `PaymentCallbackRequest`（需定义）：

| 字段名      | 类型       | 说明                  |
| ----------- | ---------- | --------------------- |
| `tx`        | String     | 交易哈希              |
| `realValue` | BigInteger | 实际到账 Wei 金额     |
| `status`    | String     | `success` 或 `failed` |
| `message`   | String     | 描述信息              |

**示例**：

json

```
{
  "tx": "0xabc...123",
  "realValue": 1000000000000000000,
  "status": "success",
  "message": "交易已确认"
}
```



响应格式与其它接口一致。

------

## 字段说明：Payment 实体

| 字段            | 类型          | 说明                                                         |
| --------------- | ------------- | ------------------------------------------------------------ |
| `id`            | Integer       | 支付记录 ID                                                  |
| `orderId`       | Integer       | 关联订单 ID                                                  |
| `status`        | String        | `CREATED`（待支付）、`PAID`（已支付）、`FAILED`（失败）、`EXPIRED`（超时） |
| `buyerAddress`  | String        | 买家地址（可选）                                             |
| `sellerAddress` | String        | 平台收款地址（固定）                                         |
| `value`         | BigInteger    | 预期金额（Wei）                                              |
| `chainId`       | BigInteger    | 链 ID                                                        |
| `txHash`        | String        | 链上交易哈希（支付后赋值）                                   |
| `realValue`     | BigInteger    | 实际到账金额（Wei）                                          |
| `confirmations` | Integer       | 交易确认数                                                   |
| `message`       | String        | 状态说明                                                     |
| `createTime`    | LocalDateTime | 创建时间                                                     |
| `txTime`        | LocalDateTime | 交易确认时间                                                 |

------

## 错误码参考

| HTTP 状态码 | 业务 code | 含义                              |
| ----------- | --------- | --------------------------------- |
| 200         | 200       | 成功                              |
| 400         | 500       | 参数错误 / 业务异常（如重复创建） |
| 404         | 500       | 资源不存在                        |
| 500         | 500       | 服务器内部错误                    |

> 注：当前代码中所有异常均返回 `Result.error(e.getMessage())`，因此 `code` 字段会变为非 200，建议后续可以细化错误码。
