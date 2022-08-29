# common-attachment

![GitHub Workflow Status](https://img.shields.io/github/workflow/status/zhengjin-me/common-attachment/Gradle%20Package?style=flat-square)
[![Maven Central](https://img.shields.io/maven-central/v/me.zhengjin/common-attachment.svg?style=flat-square&color=brightgreen)](https://maven-badges.herokuapp.com/maven-central/me.zhengjin/common-attachment/)
![GitHub](https://img.shields.io/github/license/zhengjin-me/common-attachment?style=flat-square)

```
dependencies {
    implementation "me.zhengjin:common-attachment:version"
}
```

### 注意
该包仅包含本地存储实现, 第三方存储实现需加载对应依赖
### 使用说明
使用前需注册使用模块
```kotlin
class Test {
    init {
        // 注册方式1 两种方式效果相同
        // 没有注册businessType, 所以不会进行校验
        AttachmentModelHelper.registerModel(
            AttachmentModel(
                "OTHER",
                "其他业务"
            )
        )
        // 注册了businessType, 上传附件时, model与businessType必须匹配
        AttachmentModelHelper.registerModel(
            AttachmentModel(
                "OTHER",
                "其他业务",
                mutableMapOf(
                    // 业务类型代码  业务类型描述
                    "OTHER1" to "其他1",
                    "OTHER2" to "其他2"
                )
            )
        )

        // 注册方式2 两种方式效果相同
        // 没有注册businessType, 所以不会进行校验
        AttachmentModelHelper.registerModel(
            "OTHER",
            "其他业务"
        )
        // 注册了businessType, 上传附件时, model与businessType必须匹配
        AttachmentModelHelper.registerModel(
            "OTHER",
            "其他业务",
            // 业务类型代码  业务类型描述
            "OTHER1" to "其他1",
            "OTHER2" to "其他2"
        )
    } 
}
```