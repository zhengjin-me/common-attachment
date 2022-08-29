package me.zhengjin.common.attachment.autoconfig

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "customize.common.storage.local")
class AttachmentLocalFileStorageProperties {
    /**
     * 本地存储路径
     */
    var storagePath: String? = null

    fun checkConfig() {
        require(!storagePath.isNullOrBlank()) { "请配置本地存储路径" }
    }
}
