package me.zhengjin.common.attachment.repository

import me.zhengjin.common.attachment.po.Attachment
import me.zhengjin.common.core.repository.BaseRepository

/**
 * @author fangzhengjin
 * @version V1.0
 * @title: AttachmentRepository
 * @description:
 * @date 2019/4/8 16:48
 */
interface AttachmentRepository : BaseRepository<Attachment, String> {

    fun findByIdAndDeleteFalse(id: String): Attachment?

    fun findAllByIdInAndDeleteFalse(ids: List<String>): List<Attachment>

    fun findAllByPkIdAndModuleAndDeleteFalse(pkId: String, module: String): List<Attachment>

    fun findAllByPkIdAndModuleAndBusinessTypeCodeAndDeleteFalse(pkId: String, module: String, businessTypeCode: String): List<Attachment>
}
