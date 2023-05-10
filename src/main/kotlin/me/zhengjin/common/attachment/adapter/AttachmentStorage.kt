package me.zhengjin.common.attachment.adapter

import cn.hutool.core.codec.Base64
import cn.hutool.core.util.ZipUtil
import com.querydsl.core.types.Projections
import me.zhengjin.common.attachment.controller.vo.AttachmentVO
import me.zhengjin.common.attachment.controller.vo.CompleteMultipartUploadRequestVO
import me.zhengjin.common.attachment.controller.vo.MergeDownloadVO
import me.zhengjin.common.attachment.controller.vo.MultipartUploadCreateRequestVO
import me.zhengjin.common.attachment.controller.vo.MultipartUploadCreateResponseVO
import me.zhengjin.common.attachment.po.Attachment
import me.zhengjin.common.attachment.po.AttachmentModelHelper
import me.zhengjin.common.attachment.po.QAttachment
import me.zhengjin.common.core.exception.ServiceException
import me.zhengjin.common.core.jpa.JpaHelper
import org.apache.tika.Tika
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import javax.servlet.http.HttpServletResponse

interface AttachmentStorage {

    /**
     * 创建分片上传
     *
     * @return 分片上传地址1小时内有效
     */
    fun createMultipartUpload(vo: MultipartUploadCreateRequestVO): MultipartUploadCreateResponseVO {
        TODO("Not yet implemented")
    }

    /**
     * 合并分片数据
     */
    fun completeMultipartUpload(vo: CompleteMultipartUploadRequestVO) {
        TODO("Not yet implemented")
    }

    /**
     * 文件分享
     * 生成下载外链
     */
    @Transactional(readOnly = true)
    fun share(attachmentId: Long): String {
        TODO("Not yet implemented")
    }

    /**
     * 绑定业务数据
     */
    @Transactional
    fun bindPkId(
        module: String,
        id: Long,
        pkId: Long,
        readOnly: Boolean = false,
        vararg businessTypeCode: String
    ) =
        bindPkId(module, listOf(id), pkId, readOnly, *businessTypeCode)

    /**
     * 绑定业务数据
     */
    @Transactional
    fun bindPkId(
        module: String,
        ids: List<Long>,
        pkId: Long,
        readOnly: Boolean = false,
        vararg businessTypeCode: String
    )

    /**
     * 追加附件
     */
    @Transactional
    fun append(id: Long, pkId: Long) = append(listOf(id), pkId)

    /**
     * 追加附件
     */
    @Transactional
    fun append(ids: List<Long>, pkId: Long)

    /**
     * 更换绑定业务数据
     */
    @Transactional
    fun replacePkId(
        module: String,
        sourcePkId: Long,
        targetPkId: Long,
    ) = bindPkId(module, selectFileIds(module, sourcePkId), targetPkId)

    /**
     * 对外查询
     * 查询附件列表
     */
    fun list(
        module: String,
        pkId: Long?,
        searchReadOnly: Boolean = false,
        vararg businessTypeCode: String
    ): List<AttachmentVO> {
        businessTypeCode.forEach {
            AttachmentModelHelper.checkRegister(module, it)
        }
        val attachmentDomain = QAttachment.attachment
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
            .select(
                Projections.bean(
                    AttachmentVO::class.java,
                    attachmentDomain.id,
                    attachmentDomain.pkId,
                    attachmentDomain.module,
                    attachmentDomain.businessTypeCode,
                    attachmentDomain.businessTypeName,
                    attachmentDomain.fileOriginName,
                    attachmentDomain.filePath,
                    attachmentDomain.fileType,
                    attachmentDomain.fileSize,
                    attachmentDomain.readOnly,
                )
            )
            .from(attachmentDomain)
            .where(condition)
            .fetch()
    }

    /**
     * 对内查询
     * 查询附件列表
     * 不传pkId时按模块查询
     */
    @Transactional(readOnly = true)
    fun selectFileList(
        module: String,
        pkId: Long?,
        searchReadOnly: Boolean = false,
        vararg businessTypeCode: String
    ): List<Attachment>

    /**
     * 查询附件id数组
     */
    @Transactional(readOnly = true)
    fun selectFileIds(
        module: String,
        pkId: Long,
        searchReadOnly: Boolean = false,
        vararg businessTypeCode: String
    ): List<Long> {
        val attachments = selectFileList(module, pkId, searchReadOnly, *businessTypeCode)
        return attachments.map { it.id!! }.toList()
    }

    /**
     * 获取附件信息
     */
    @Transactional(readOnly = true)
    fun getAttachment(attachmentId: Long): Attachment

    /**
     * 获取实际文件
     */
    fun getAttachmentFileStream(attachmentId: Long): InputStream

    /**
     * 获取文件流 无本地存储的文件必须实现此接口!!!!!
     */
    fun getAttachmentFileStream(attachment: Attachment): InputStream

    /**
     * 文件下载
     * @param response 响应对象
     * @param id       附件id
     * @param isDown   true 下载 false 预览
     */
    @Transactional(readOnly = true)
    fun download(response: HttpServletResponse, id: Long, isDown: Boolean)

    /**
     * 文件下载合并为zip
     * @param ids      附件id数组
     * @param response 响应对象
     */
    @Transactional(readOnly = true)
    fun mergeDownload(ids: List<Long>, response: HttpServletResponse): MergeDownloadVO {
        ServiceException.requireNotNullOrEmpty(ids) { "attachment ids is empty!" }
        val attachments = ids.distinct().map { getAttachment(it) }
        val fileNames = arrayOfNulls<String>(attachments.size)
        val files = arrayOfNulls<InputStream>(attachments.size)
        attachments.forEachIndexed { i, attachment ->
            fileNames[i] = attachment.fileOriginName
            files[i] = getAttachmentFileStream(attachment)
        }
        ByteArrayOutputStream().use {
            ZipUtil.zip(it, fileNames, files)
            return MergeDownloadVO(filename = "${System.nanoTime()}.zip", content = Base64.encode(it.toByteArray()))
        }
    }

    /**
     * 批量逻辑删除
     * @param ids 附件id集合
     */
    @Transactional
    fun deleteBatch(ids: List<Long>)

    /**
     * 附件存储(MultipartFile处理流程)
     * @param file              需要存储的文件
     * @param module            业务模块
     * @param businessTypeCode  业务类型代码
     * @param businessTypeName  业务类型名称
     * @param pkId              业务键
     * @param readOnly          附件是否只读 仅能预览 不能下载
     */
    @Transactional
    fun saveFiles(
        file: MultipartFile,
        module: String,
        businessTypeCode: String,
        businessTypeName: String,
        pkId: Long? = null,
        readOnly: Boolean = false
    ): AttachmentVO = file.inputStream.use {
        saveFiles(
            it,
            module,
            businessTypeCode,
            businessTypeName,
            pkId,
            file.originalFilename!!,
            file.contentType!!,
            file.size,
            readOnly
        )
    }

    /**
     * 附件存储(File处理流程)
     * @param srcFile           需要存储的文件
     * @param module            业务模块
     * @param businessTypeCode  业务类型代码
     * @param businessTypeName  业务类型名称
     * @param pkId              业务键
     * @param readOnly          附件是否只读 仅能预览 不能下载
     */
    @Transactional
    fun saveFiles(
        srcFile: File,
        module: String,
        businessTypeCode: String,
        businessTypeName: String,
        pkId: Long? = null,
        readOnly: Boolean = false
    ): AttachmentVO = srcFile.inputStream().use {
        saveFiles(
            it,
            module,
            businessTypeCode,
            businessTypeName,
            pkId,
            srcFile.name,
            Tika().detect(srcFile),
            srcFile.length(),
            readOnly
        )
    }

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
    @Transactional
    fun saveFiles(
        file: InputStream,
        module: String,
        businessTypeCode: String,
        businessTypeName: String,
        pkId: Long? = null,
        originalFileName: String,
        fileContentType: String,
        fileSize: Long,
        readOnly: Boolean = false
    ): AttachmentVO
}
