package me.zhengjin.common.attachment.autoconfig

import me.zhengjin.common.attachment.adapter.AttachmentLocalFileStorageAdapter
import me.zhengjin.common.attachment.adapter.AttachmentStorage
import me.zhengjin.common.attachment.repository.AttachmentRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(AttachmentLocalFileStorageProperties::class)
@ConditionalOnProperty(prefix = "customize.common.storage", name = ["type"], havingValue = "local")
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class AttachmentLocalFileStorageAutoConfiguration(
    private val attachmentRepository: AttachmentRepository,
    private val attachmentLocalFileStorageProperties: AttachmentLocalFileStorageProperties,
) {
    private val logger = LoggerFactory.getLogger(AttachmentLocalFileStorageAutoConfiguration::class.java)

    @Bean
    @ConditionalOnMissingBean
    fun attachmentStorage(): AttachmentStorage {
        attachmentLocalFileStorageProperties.checkConfig()
        logger.info("attachment storage type: [local]")
        return AttachmentLocalFileStorageAdapter(
            attachmentRepository,
            attachmentLocalFileStorageProperties
        )
    }
}
