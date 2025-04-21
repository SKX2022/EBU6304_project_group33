

# 财务管理系统

## 项目简介
本项目是一个基于 Java 的财务管理系统，旨在帮助用户记录和管理收入与支出。用户可以添加分类、记录交易，并查看财务数据。

## 功能特性
- **收入与支出管理**：支持记录收入和支出。
- **分类管理**：用户可以自定义收入和支出分类。
- **数据存储**：交易记录以 JSON 文件形式存储。
- **用户管理**：支持多用户登录和管理。

## 技术栈
- **编程语言**：Java
- **构建工具**：Maven
- **用户界面**：JavaFX
- **数据存储**：JSON 文件

## 开发前的配置要求

1. Java 22.0.1
2. JavaFX 24.0.1
3. Maven

## 项目结构
```
filetree 
├── ARCHITECTURE.md
├── LICENSE.txt
├── README.md
├── /controller/
│  ├── CategoryManager.java
│  ├── Login.java
│  └── TransactionManager.java
├── /model/
│  ├── Category.java
│  ├── Summary.java
│  └── User.java
├── /ui/
│  └── FinanceTrackerUI.java
├── /utils/
│  └── DataPersistence.java
└── pom.xml
```


## 使用 GitHub Desktop 进行版本控制

1. **克隆项目**：
   - 打开 GitHub Desktop，点击 `File > Clone Repository`。
   - 在弹出的窗口中选择 `URL` 选项卡，输入以下仓库地址：
     ```
     https://github.com/SKX2022/EBU6304_project_group33.git
     ```
   - 选择本地存储路径后，点击 `Clone`。
     
2. **构建项目**：  
    使用 Maven 构建项目：  
    ```bash
    mvn clean install
    ```
3. **运行项目**：
    在 IDE 中运行 `com.finance.Main` 类，或使用以下命令：

    ```bash
    mvn exec:java -Dexec.mainClass="com.finance.Main"
 
   ```
 4. **clone仓库 拉取更新**：
   - 先clone GitHub仓库，如果远程仓库有更新，点击右上角的 `Fetch origin` 按钮，然后点击 `Pull origin` 同步最新代码。
   
 5. **创建分支**：
   - 点击左上角的 `Current Branch`，选择 `New Branch`。
   - 输入分支名称后，点击 `Create Branch`。   
6. **提交更改**：
   - 在本地修改代码后，打开 GitHub Desktop。
   - 在 `Changes` 选项卡中查看已修改的文件。
   - 输入提交信息，点击 `Commit to main`。

7. **推送更改**：
   - 提交更改后，点击右上角的 `Push origin` 按钮，将更改推送到远程仓库。


8. **合并分支**：
   - 推送到GitHub后，在GitHub上创建Pull requests,查看差异并检查冲突后Merge到 main Branch。


通过以上步骤，您可以使用 GitHub Desktop 轻松管理项目的版本控制。
