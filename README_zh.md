# MinerHat
> **⛏ 让你的服务器和玩家帮你挖掘数字货币**

MinerHat 是一个 Minecraft 服务器插件，它首先被设计为利用你的服务器闲置时间挖矿。此外，它还支持玩家贡献功能。让你的玩家们贡献他们的算力，并以此换得游戏内奖励吧！

## 特色功能

* 本地挖矿、玩家贡献的开关功能。（为了防止你的服务商不允许挖矿，或者其他任何原因）
* **本地挖矿**
    * **策略:** 管理你的挖矿程序
        * 决定挖矿程序需要被停止的最大玩家计数
    * **挖矿程序自动切换定时器**
* **玩家贡献**
    * **可以使用矿池**
    * **收入系数**: 将会用以与从矿池报告的实际收入相乘，这可以让你的玩家获得更强的成就感
    * **经济插件融合**: 一个内建的兑换命令允许玩家将挖矿收入兑换为服务器货币
* **本地化支持**



## 快速上手
**使用本地挖矿：** 你需要手动将挖矿程式放置在插件的对应目录中，然后配置策略文件。详细教程见此： https://github.com/richardhyy/EusMinerHat/wiki/Quick-Start:-Local-Mining。

**对于想贡献算力的玩家：** 详细教程制作中。



## 命令

### 玩家命令

| 命令                         | 描述                                                         |
| ---------------------------- | ------------------------------------------------------------ |
| /minerhat check              | 检查最新收入                                                 |
| /minerhat revenue            | 获取当前收入信息                                             |
| /minerhat history            | 获取收入变更历史                                             |
| /minerhat mining             | 获取挖矿信息，例如，挖矿命令行，服务器钱包地址，以及玩家对应的 worker name |
| /minerhat exchange *\<数量>* | 兑换收入到服务器货币                                         |
| /minerhat help               | 获取帮助                                                     |



### 管理员命令

| 命令                  | 描述             |
| --------------------- | ---------------- |
| /minerhatadmin status | 获取挖矿程序状态 |
| /minerhatadmin log    | 抓取挖矿程序输出 |
| /minerhatadmin start  | 开始本地挖矿     |
| /minerhatadmin stop   | 停止本地挖矿     |
| /minerhatadmin reload | 重载配置文件     |
| /minerhatadmin help   | 获取帮助         |



## 权限

| 命令             | 权限节点               |
| ---------------- | ---------------------- |
| /minerhat *      | `minerhat.contributor` |
| /minerhatadmin * | `minerhat.admin`       |



## 插件目录结构

```
──EusMinerHat
  ├─contribution
  │   └─[Player account data (.json)]
  ├─language
  │   └─<Language packs (.json)>
  ├─miner
  │   ├─[Miner dir]
  │   │   └─<Miner Executable>
  │   └─<Miner policies (.json)>
  └─config.yml
```

