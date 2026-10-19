<p align="center">
  <img src="assets/SenyuShop-logo.png" width="420" alt="森屿商店 SenyuShop">
</p>

<h1 align="center">森屿商店 SenyuShop</h1>

<p align="center">
  <b>官方商店 + 玩家拍卖行 · 双界面(对话框 / 箱子)· 全中文 · 自带累进税防通胀</b><br>
  当前版本:v1.1.2 · 作者:chenzy05 · 发布页:MineBBS / GitHub Releases
</p>

> 把超市搬进服务器,把"税局"搬进拍卖行。
> 管理员一行命令上架,玩家一行命令上架、一键买卖;拍卖成交按卖家余额档收累进税,税金销毁回收,通胀?不存在的。

---

## ✨ 功能特性

**官方商店(管理员上架,玩家买卖)**

- 🏪 分类浏览:建筑方块 / 自然方块 / 食物 / 其他 + 「回收专场」(只显示可回收出售的物品)
- 💰 同一件商品可分别设置买入价与卖出价(填 0 = 该方向不开放)
- 🧭 支持分类内搜索、全场搜索,内置中文物品名对照表(`ZhNames`,也支持英文 id 如 `stone`)
- ⚡ 上架 / 改价 / 下架即时生效,无需重启无需 reload

**玩家拍卖行(玩家自由交易)**

- 🧳 手持物品 `/ah sell <价格>` 一行上架,无需打开任何界面拖拽
- ⏳ 有效期默认 7 天,到期自动下架(每 10 分钟后台清扫);每人同时在售数量可限制(默认 10)
- 📥 `/ah my` 查看自己的拍卖,点一下即可下架取回
- 🛡️ 抢购原子化:两人同时购买同一件,一人成功,另一人自动全额退款

**累进税防通胀(可选,默认开启)**

- 🧾 拍卖成交按卖家成交时的余额档收税,税金直接销毁(不进任何账户)
- 📉 档位示例(自由修改/关闭):余额 1 万以内 1%,最高到不封顶档 20%
- 🏪 官方商店不收税;想全服免税改一行配置即可

**双界面**

- 🖥️ 对话框界面:基于 Minecraft 原版 Dialog 系统,信息展示更丰富
- 📦 箱子界面:`/shopchest` 传统箱子 GUI——**左键买 1 / Shift+左键买一叠 / 右键卖 1 / Shift+右键卖光背包**

**工程质量**

- 🔒 交易前双重预检(背包空间 + 余额);装不下/买不起时按实际数量成交,绝不白扣钱
- 🧯 任何扣款/入账失败,物品原物退回;买家背包不足时差额退款,剩余物品退回卖家(在线进背包、离线自动重新上架)
- 🚀 所有数据库读写异步执行 + SQLite WAL 模式,主线程零阻塞
- 🗄️ 数据单文件存储 `plugins/SenyuShop/data.db`,备份 = 复制一个文件
- 🇨🇳 全中文交互提示

## 服务器 Logo 同款周边

| | |
| --- | --- |
| ![logo](assets/SenyuShop-icon.png) | 仓库头像 / 插件页图标已包含在 `assets/` 中 |

## 📋 环境要求

| 项目 | 要求 |
| --- | --- |
| 服务端 | Paper 系(Paper / Purpur / Leaf),Minecraft **26.2+**(使用原版对话框 API) |
| 经济 | 安装 **Vault** 或 **VaultSlim**(配合 EssentialsX 等任意经济体系);未装经济插件时仅可浏览,不可交易 |
| 构建 | JDK 21+(实测 JDK 25) |

## 🚀 安装

1. 从 Releases 下载 `SenyuShop-1.1.2.jar`,放入服务器 `plugins/` 目录
2. 确认已安装 Vault / VaultSlim 并存在可用经济体系
3. 重启服务器,首次启动自动生成 `plugins/SenyuShop/config.yml` 与 `data.db`

## 🎮 快速上手

### 玩家

```
/shop                    打开官方商店(分类 / 回收专场 / 搜索)
/shop search <关键词>    搜索商品(中文物品名或英文 id)
/shopchest [分类]        箱子版商店(左键买 / 右键卖,Shift 批量)
/ah                      浏览玩家拍卖行
/ah sell <价格>          将手持物品上架拍卖行
/ah my                   我的拍卖(下架取回)
```

箱子界面操作速记:**左键买 1 个 · Shift+左键买一叠 · 右键卖 1 个 · Shift+右键卖光背包同类**。

### 管理员(权限 `senyushop.admin`,默认 OP)

```
/shopadmin                                  上架助手:填价格与分类,直接点背包物品上架
/shopadmin addhand <买价> <卖价> [分类]      上架手持物品(add 同义)
/shopadmin addinv <买价> <卖价> [分类]       一键上架背包全部物品(自动跳过重复)
/shopadmin rm                               下架助手:点谁下谁
/shopadmin removecat <分类>                  一键下架整个分类(弹窗二次确认)
/shopadmin setcat <id> <分类>                调整分类
/shopadmin price <id> <买价> <卖价>          改价(填 - 表示不改该项)
/shopadmin remove <id>                       按 id 下架
/shopadmin list                              查看全部商品
```

> 💡 价格填 `0` 表示该方向不开放;分类固定四类:`建筑方块` `自然方块` `食物` `其他`。

## 🔐 权限

| 权限 | 说明 | 默认 |
| --- | --- | --- |
| `senyushop.use` | 使用官方商店与拍卖行 | 所有玩家 |
| `senyushop.admin` | 管理官方商店 | OP |

## ⚙️ 配置文件

`plugins/SenyuShop/config.yml`:

```yaml
page-size: 24               # 拍卖行每页条数
max-listings-per-player: 10 # 每人同时在售上限
expire-days: 7              # 拍卖有效期(天)

# 拍卖成交税:按卖家成交时的余额档收税,税金直接销毁(不进任何账户),官方商店不收税。
# 格式 "余额上限:百分比",从上到下取第一个 余额<=上限 的档位;上限写 0 表示不设上限(放 last)。
tax-enabled: true
tax-brackets:
  - "10000:1"
  - "100000:2"
  - "250000:3"
  - "500000:4"
  - "1000000:5"
  - "2500000:10"
  - "5000000:12"
  - "0:20"
```

## 🛠️ 构建与开发

```
SenyuShop/
├─ build.ps1          # Windows 一键编译脚本
├─ build-tools/       # (自行创建)放编译依赖
│  ├─ paper-api.jar            # Paper 26.2 API
│  ├─ VaultSlim.jar            # VaultSlim(或 Vault.jar)
│  └─ runtime/                 # adventure 系列依赖 jar
├─ resources/         # plugin.yml / config.yml
├─ src/senyu/shop/    # 全部 Java 源码
└─ assets/            # 插件 logo
```

1. 把 `paper-api.jar`、`VaultSlim.jar` 及 adventure/annotations/guava 等依赖放入 `build-tools/`(与 `build.ps1` 中的 classpath 对应)
2. 运行 `build.ps1`
3. 产物输出到 `build/dist/SenyuShop-1.1.2.jar`

> 编译参数与依赖列表全部写在 `build.ps1` 内,路径按需修改;CI 可直接调用同样命令。

## ❓ FAQ

<details>
<summary><b>没有经济插件能玩吗?</b></summary>

可以浏览商店与拍卖行,但交易会提示"经济系统不可用"。请安装 Vault 或 VaultSlim,并搭配 EssentialsX 等提供经济的插件。
</details>

<details>
<summary><b>税是怎么算的?会不会误伤新手?</b></summary>

按【卖家成交时】的余额查档位,例如默认配置下余额 5 万的卖家成交只收 2% 税。新手几乎无感,大户囤货越多纳税越多。税金直接销毁(货币回收)。
</details>

<details>
<summary><b>修改价格/分类需要重启吗?</b></summary>

不需要。所有改动即时生效,玩家下一次打开就是新价。
</details>

<details>
<summary><b>支持旧版本 / 基岩版吗?</b></summary>

仅支持 Minecraft 26.2+ 的 Paper 系服务端(依赖原版对话框 API);基岩版无法使用。
</details>

<!-- GitHub Releases 链接、MineBBS 链接发布后请自行补到这里 -->

## 📝 更新日志

### v1.1.2
- 新增箱子界面商店 `/shopchest`(分类目录 + 左键买 / 右键卖 + Shift 批量)
- 官方商店、拍卖行、上架/下架助手全部支持分页与即时生效
- 完善退款与背包回收的边界处理
- 性能与细节优化

## 📄 许可证

本项目基于 [MIT License](LICENSE) 开源。

## 🙏 致谢

- [Paper](https://papermc.io) / [Leaf](https://github.com/Winds-Studio/Leaf) 服务端
- [Vault / VaultSlim](https://github.com/MilkBowl/Vault) 经济桥接
- 所有提出建议与反馈的服主与玩家

---

*森屿商店 · 让服务器经济,像逛超市一样简单。*
