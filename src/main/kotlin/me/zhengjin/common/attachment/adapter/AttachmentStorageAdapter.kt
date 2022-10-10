package me.zhengjin.common.attachment.adapter

import cn.hutool.core.io.IoUtil
import me.zhengjin.common.attachment.po.Attachment
import me.zhengjin.common.attachment.po.QAttachment
import me.zhengjin.common.attachment.repository.AttachmentRepository
import me.zhengjin.common.core.jpa.JpaHelper
import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URLEncoder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.servlet.http.HttpServletResponse

/**
 * 附件服务基础实现
 */
abstract class AttachmentStorageAdapter(
    private val attachmentRepository: AttachmentRepository
) : AttachmentStorage {

    private val dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd")!!
    private val attachmentDomain = QAttachment.attachment

    /**
     * 生成时间格式的目录
     * 格式: 2016/05/06
     */
    fun dateRuleDir() = dtf.format(LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()))!!

    /**
     * 追加附件
     */
    override fun append(ids: List<Long>, pkId: Long) {
        if (ids.isEmpty()) {
            return
        }
        val attachments = attachmentRepository.findAllById(ids)
        attachments.forEach { it.pkId = pkId }
        attachmentRepository.saveAll(attachments)
    }

    /**
     * 绑定业务数据
     */
    override fun bindPkId(
        module: String,
        ids: List<Long>,
        pkId: Long,
        readOnly: Boolean,
        vararg businessTypeCode: String
    ) {
        if (ids.isEmpty()) {
            return
        }
        val oldAttachments = selectFileList(module, pkId, readOnly, *businessTypeCode)
        oldAttachments.forEach { it.delete = true }
        attachmentRepository.saveAll(oldAttachments)
        val attachments = attachmentRepository.findAllById(ids)
        if (attachments.size != ids.size) {
            throw RuntimeException("Failed to find file, expected " + ids.size + ", actual " + attachments.size)
        }
        attachments.forEach {
            it.pkId = pkId
            it.delete = false
        }
        attachmentRepository.saveAll(attachments)
    }

    /**
     * 查询附件列表
     */
    override fun selectFileList(
        module: String,
        pkId: Long?,
        searchReadOnly: Boolean,
        vararg businessTypeCode: String
    ): List<Attachment> {
        var condition = attachmentDomain.delete.isFalse
        condition = condition.and(attachmentDomain.module.eq(module))
        if (pkId != null) {
            condition = condition.and(attachmentDomain.pkId.eq(pkId))
        }
        if (businessTypeCode.isNotEmpty()) {
            condition = if (searchReadOnly) {
                // 同时查询只读附件
                val businessTypeCodeAll = mutableListOf<String>()
                businessTypeCode.forEach { businessTypeCodeAll.add("${it}_ReadOnly") }
                businessTypeCodeAll.addAll(businessTypeCode)
                condition.and(attachmentDomain.businessTypeCode.`in`(businessTypeCodeAll))
            } else {
                condition.and(attachmentDomain.businessTypeCode.`in`(*businessTypeCode))
            }
        } else {
            if (!searchReadOnly) {
                condition = condition.and(attachmentDomain.businessTypeCode.notLike("%_ReadOnly"))
            }
        }
        return JpaHelper.getJPAQueryFactory()
            .selectFrom(attachmentDomain)
            .where(condition)
            .fetch()
    }

    /**
     * 批量逻辑删除
     * @param ids 附件id集合
     */
    override fun deleteBatch(ids: List<Long>) {
        val attachments = attachmentRepository.findAllById(ids)
        attachments.forEach { it.delete = true }
        attachmentRepository.saveAll(attachments)
    }

    /**
     * 获取附件信息
     */
    override fun getAttachment(attachmentId: Long): Attachment =
        attachmentRepository.findByIdAndDeleteFalse(attachmentId) ?: throw RuntimeException("未找到附件信息")

    /**
     * 获取实际文件
     */
    override fun getAttachmentFileStream(attachmentId: Long): InputStream =
        getAttachmentFileStream(getAttachment(attachmentId))

    /**
     * 文件下载
     * @param response 响应对象
     * @param id       附件id
     * @param isDown   true 下载 false 预览
     */
    override fun download(response: HttpServletResponse, id: Long, isDown: Boolean) {
        val attachmentOptional = attachmentRepository.findById(id)
        val attachment = attachmentOptional.orElseThrow { RuntimeException("File not found") }
        val fileInputStream = getAttachmentFileStream(attachment)
        if (isDown) {
            if (attachment.readOnly) throw RuntimeException("File is readOnly, forbid download")
            response.contentType = attachment.fileType
            response.setHeader(
                "Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(attachment.fileOriginName, "UTF-8")
            )
        }
        try {
            fileInputStream.use { fis -> IoUtil.copy(fis, response.outputStream) }
        } catch (e: FileNotFoundException) {
            throw RuntimeException("File not found")
        }
    }
}
