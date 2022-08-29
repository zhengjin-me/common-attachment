package me.zhengjin.common.attachment.adapter

import me.zhengjin.common.attachment.autoconfig.AttachmentLocalFileStorageProperties
import me.zhengjin.common.attachment.controller.vo.AttachmentVO
import me.zhengjin.common.attachment.po.Attachment
import me.zhengjin.common.attachment.po.AttachmentModelHelper
import me.zhengjin.common.attachment.repository.AttachmentRepository
import cn.hutool.core.io.FileUtil
import java.io.InputStream
import java.nio.file.Paths
import java.util.Objects
import java.util.UUID

/**
 * @version V1.0
 * @Title: AttachmentLocalFileStorageAdapter
 * @Package me.zhengjin.common.service
 * @Description:
 * @Author fangzhengjin
 * @Date 2018-4-18 12:20
 */
open class AttachmentLocalFileStorageAdapter(
    private val attachmentRepository: AttachmentRepository,
    private val attachmentLocalFileStorageProperties: AttachmentLocalFileStorageProperties,
) : AttachmentStorageAdapter(attachmentRepository) {

    /**
     * 获取文件流
     */
    override fun getAttachmentFileStream(attachment: Attachment): InputStream =
        Paths.get(attachmentLocalFileStorageProperties.storagePath!!, attachment.filePath).toFile().inputStream()

    /**
     * 附件存储(最终方法)
     * @param file              文件流
     * @param module            业务模块
     * @param businessTypeCode  业务类型代码
     * @param businessTypeName  业务类型名称
     * @param pkId              业务键
     * @param originalFileName  原始文件名称
     * @param fileContentType   文件媒体类型
     * @param fileSize          文件大小(字节)
     * @param readOnly          附件是否只读 仅能预览 不能下载
     */
    override fun saveFiles(
        file: InputStream,
        module: String,
        businessTypeCode: String,
        businessTypeName: String,
        pkId: String?,
        originalFileName: String,
        fileContentType: String,
        fileSize: Long,
        readOnly: Boolean
    ): AttachmentVO {
        AttachmentModelHelper.checkRegister(module, businessTypeCode)
        val temp =
            Objects.requireNonNull<String>(originalFileName).split("\\.".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        val dateDir = dateRuleDir()
        val storagePath = Paths.get(attachmentLocalFileStorageProperties.storagePath!!, module, dateDir).toAbsolutePath()
        if (!storagePath.toFile().exists() && !storagePath.toFile().mkdirs()) {
            throw RuntimeException(
                String.format(
                    "%s path create failed",
                    storagePath.toAbsolutePath().toAbsolutePath()
                )
            )
        }
        val fileName = UUID.randomUUID().toString().replace("-".toRegex(), "") + "." + temp[temp.size - 1]

        // 自动关闭流
        FileUtil.writeFromStream(file, Paths.get(storagePath.toString(), fileName).toFile())

        var attachment = Attachment()
        if (readOnly) attachment.readOnly = true
        attachment.module = module
        attachment.businessTypeCode = if (readOnly) "${businessTypeCode}_ReadOnly" else businessTypeCode
        attachment.businessTypeName = if (readOnly) "$businessTypeName(预览)" else businessTypeName
        attachment.pkId = if (pkId.isNullOrBlank()) null else pkId.toString()
        attachment.fileOriginName = originalFileName
        attachment.fileType = fileContentType
        attachment.filePath =
            String.format("/%s/%s/%s", module, dateDir.replace("\\\\".toRegex(), "/"), fileName)
        attachment.fileSize = fileSize.toString()
        attachment = attachmentRepository.save(attachment)
        return if (!attachment.id.isNullOrBlank()) {
            AttachmentVO.transform(attachment)
        } else {
            throw RuntimeException("file save failed!")
        }
    }
}
