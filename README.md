# MinerHat
> **⛏ Mining cryptocurrency with your Minecraft server (and player)**

MinerHat is a Minecraft server plugin that utilizes your server at low load to mine cryptocurrency. Even more, it supports player contribution. Just let your players share their computing power to mine for you in exchange for in-game bonus!

## Features

* Toggles for local mining and player contribution modules. (In case your server provider disallows mining on their server, or whatever other reasons.)
* **Local Mining**
    * **Policies:** Manage your miners
        * Decide the maximum player count at which the miner should be stopped
    * **Timer for auto-switching miner**
* **Player Contribution**
    * **Mining pool implement**
    * **Revenue factor**: It will be multiplied with the actual revenue reported from the mining pool API in order to give your players adorable feedbacks
    * **Economy plugin integration**: A built-in exchange command which allows players exchanging revenue to your server's currency.
* **Localization Support**



## Get Started
**TO USE LOCAL MINING:** You need to put your miner into plugin's directory manually and then configure your mining wallet in the policy file. A detailed tutorial is available at: https://github.com/richardhyy/EusMinerHat/wiki/Quick-Start:-Local-Mining.

**FOR PLAYERS WHO WANT TO CONTRIBUTE THEIR COMPUTING POWER:** A detailed tutorial will be available soon.



## Commands

### Player's Commands

| Command                        | Description                                                  |
| ------------------------------ | ------------------------------------------------------------ |
| /minerhat check                | Checkout revenue                                             |
| /minerhat revenue              | Get revenue account information                              |
| /minerhat history              | Check revenue change history                                 |
| /minerhat mining               | Get mining information, e.g. mining commandline, server's wallet address, and player's worker name |
| /minerhat exchange *\<amount>* | Exchange revenue to server's currency                        |
| /minerhat help                 | Get help for player's commands                               |



### Admin's Commands

| Command               | Description                   |
| --------------------- | ----------------------------- |
| /minerhatadmin status | Get miner status              |
| /minerhatadmin log    | Fetch miner output            |
| /minerhatadmin start  | Start local miner manually    |
| /minerhatadmin stop   | Stop local miner manually     |
| /minerhatadmin reload | Reload plugin configuration   |
| /minerhatadmin help   | Get help for admin's commands |



## Permissions

| Commands         | Permission Node        |
| ---------------- | ---------------------- |
| /minerhat *      | `minerhat.contributor` |
| /minerhatadmin * | `minerhat.admin`       |



## Plugin Directory Structure

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

