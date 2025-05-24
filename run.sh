#!/bin/bash

# 财务管理系统启动脚本

echo "正在启动财务管理系统..."

# 设置Java路径
JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

# 设置JavaFX模块路径
JAVAFX_PATH=$(find ~/.m2/repository/org/openjfx -name "javafx-*-22.0.2-mac-aarch64.jar" | tr '\n' ':')

# 设置其他依赖
OTHER_DEPS=$(find ~/.m2/repository -name "jackson-databind-2.16.2.jar")
OTHER_DEPS="${OTHER_DEPS}:$(find ~/.m2/repository -name "jackson-core-2.16.2.jar")"
OTHER_DEPS="${OTHER_DEPS}:$(find ~/.m2/repository -name "jackson-annotations-2.16.2.jar")"
OTHER_DEPS="${OTHER_DEPS}:$(find ~/.m2/repository -name "commons-codec-1.16.1.jar")"
OTHER_DEPS="${OTHER_DEPS}:$(find ~/.m2/repository -name "poi-5.2.3.jar")"
OTHER_DEPS="${OTHER_DEPS}:$(find ~/.m2/repository -name "poi-ooxml-5.2.3.jar")"

# 设置完整的模块路径
MODULE_PATH="${JAVAFX_PATH}:${OTHER_DEPS}"

# 设置classpath（包含项目类和非模块依赖）
CLASSPATH="target/classes:${OTHER_DEPS}"

echo "使用Temurin Java 17和JavaFX 22.0.2 (ARM64)运行..."

# 运行应用程序
$JAVA_HOME/bin/java \
     --module-path "$MODULE_PATH" \
     --add-modules javafx.controls,javafx.fxml \
     -cp "$CLASSPATH" \
     com.finance.MainApplication

echo "应用程序已退出" 